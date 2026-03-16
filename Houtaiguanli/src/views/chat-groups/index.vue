<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="群组名称">
          <el-input v-model="searchForm.name" placeholder="请输入群组名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
      >
        <el-table-column label="群组信息" min-width="300">
          <template #default="scope">
            <div class="group-info">
              <el-avatar :size="40" :src="scope.row.avatar" />
              <div class="info-content">
                <div class="group-name">{{ scope.row.name }}</div>
                <div class="group-desc">成员: {{ scope.row.memberCount }}人</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="最后消息" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <div class="message-info">
              <div class="message-content">{{ scope.row.lastMessage || '暂无消息' }}</div>
              <div class="message-time">{{ formatDate(scope.row.lastTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button type="primary" link @click="handleViewMembers(scope.row)">
                <el-icon><User /></el-icon>成员
              </el-button>
              <el-button type="primary" link @click="handleViewMessages(scope.row)">
                <el-icon><ChatDotRound /></el-icon>消息
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 成员管理对话框 -->
    <el-dialog
      v-model="membersDialogVisible"
      :title="`群组成员 - ${currentGroup.name}`"
      width="900px"
      destroy-on-close
    >
      <el-table v-loading="memberLoading" :data="membersList" style="width: 100%">
        <el-table-column type="index" width="50" label="序号" align="center" />
        <el-table-column label="用户信息" min-width="220">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :size="40" :src="scope.row.avatar" />
              <div class="info-content">
                <div class="user-name">{{ scope.row.name }}</div>
                <div class="user-id">ID: {{ scope.row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag>{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.joinTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 消息记录对话框 -->
    <el-dialog
      v-model="messagesDialogVisible"
      :title="`消息记录 - ${currentGroup.name}`"
      width="800px"
      destroy-on-close
    >
      <div class="messages-container">
        <div v-if="messagesList.length === 0" class="empty-state">
          暂无消息
        </div>
        <div v-for="message in messagesList" :key="message.id" class="message-item">
          <div class="message-header">
            <div class="user-info">
              <el-avatar :size="32" :src="message.userAvatar" />
              <span class="username">{{ message.sendNickName }}</span>
            </div>
            <span class="time">{{ formatDate(message.sendTime) }}</span>
          </div>
          <div class="message-content">{{ message.content }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshRight, User, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAllGroupList, getGroupMessages, getGroupMembers } from '@/api/chatGroup'

// 搜索表单
const searchForm = reactive({
  name: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const filteredData = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 成员管理对话框
const membersDialogVisible = ref(false)
const memberLoading = ref(false)
const membersList = ref([])
const currentGroup = reactive({
  id: null,
  name: ''
})

// 消息记录对话框
const messagesDialogVisible = ref(false)
const messagesList = ref([])

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 获取群组列表
const fetchGroupList = async () => {
  loading.value = true
  try {
    console.log('开始获取群组列表...')
    const response = await getAllGroupList()
    console.log('群组列表原始响应:', JSON.stringify(response, null, 2))
    
    if (response && Array.isArray(response)) {
      console.log('响应是数组，数量:', response.length)
      
      // 处理响应数据，为缺失的字段设置默认值
      const processedData = response.map(item => ({
        id: item.id,
        name: item.name || '未命名群组',
        avatar: item.avatar || item.headImage || '',
        memberCount: item.memberCount || 0,
        lastMessage: item.lastMessage || '',
        lastTime: item.lastTime || item.createdTime || new Date().toISOString(),
        createdTime: item.createdTime || new Date().toISOString()
      }))
      
      console.log('处理后的数据:', JSON.stringify(processedData, null, 2))
      
      filteredData.value = processedData
      pagination.total = processedData.length
      
      // 应用分页
      const start = (pagination.currentPage - 1) * pagination.pageSize
      const end = start + pagination.pageSize
      tableData.value = processedData.slice(start, end)
      
      console.log('表格数据已更新，显示数量:', tableData.value.length)
    } else if (response && typeof response === 'object') {
      console.log('响应是对象，尝试提取数据')
      // 处理可能是 { data: [], ... } 的情况
      const data = response.data || response.records || []
      if (Array.isArray(data)) {
        const processedData = data.map(item => ({
          id: item.id,
          name: item.name || '未命名群组',
          avatar: item.avatar || item.headImage || '',
          memberCount: item.memberCount || 0,
          lastMessage: item.lastMessage || '',
          lastTime: item.lastTime || item.createdTime || new Date().toISOString(),
          createdTime: item.createdTime || new Date().toISOString()
        }))
        
        filteredData.value = processedData
        pagination.total = processedData.length
        
        const start = (pagination.currentPage - 1) * pagination.pageSize
        const end = start + pagination.pageSize
        tableData.value = processedData.slice(start, end)
      }
    } else {
      console.warn('响应数据格式不符合预期:', response)
      ElMessage.warning('获取的群组数据格式异常')
    }
  } catch (error) {
    console.error('获取群组列表失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error('获取群组列表失败: ' + (error.message || ''))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  
  if (searchForm.name) {
    tableData.value = filteredData.value.filter(item => 
      item.name.includes(searchForm.name)
    )
    pagination.total = tableData.value.length
  } else {
    pagination.total = filteredData.value.length
    const start = 0
    const end = pagination.pageSize
    tableData.value = filteredData.value.slice(start, end)
  }
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  pagination.currentPage = 1
  fetchGroupList()
}

// 查看成员
const handleViewMembers = async (row) => {
  currentGroup.id = row.id
  currentGroup.name = row.name
  memberLoading.value = true
  membersList.value = []
  membersDialogVisible.value = true
  
  try {
    const response = await getGroupMembers(row.id, { 
      page: 1, 
      size: 50 
    })
    console.log('群组成员响应:', response)
    
    // 根据后端实际返回的数据格式调整
    if (response && response.records) {
      membersList.value = response.records
    } else if (response && Array.isArray(response)) {
      membersList.value = response
    }
  } catch (error) {
    console.error('获取成员列表失败:', error)
    ElMessage.error('获取成员列表失败')
  } finally {
    memberLoading.value = false
  }
}

// 查看消息
const handleViewMessages = async (row) => {
  currentGroup.id = row.id
  currentGroup.name = row.name
  messagesList.value = []
  messagesDialogVisible.value = true
  
  try {
    const response = await getGroupMessages(row.id, { 
      page: 1, 
      size: 50 
    })
    console.log('群组消息原始响应:', JSON.stringify(response, null, 2))
    
    // 根据后端实际返回的数据格式调整
    if (response && response.messages) {
      // 后端返回 { messages: [...], total: ..., ... }
      messagesList.value = response.messages.map(msg => ({
        id: msg.id,
        content: msg.content,
        userAvatar: msg.avatar,
        sendNickName: msg.senderName || msg.sendNickName,
        sendTime: msg.sendTime
      }))
    } else if (response && response.records) {
      messagesList.value = response.records
    } else if (response && Array.isArray(response)) {
      messagesList.value = response
    }
    console.log('处理后的消息列表:', messagesList.value)
  } catch (error) {
    console.error('获取消息失败:', error)
    ElMessage.error('获取消息失败: ' + (error.message || ''))
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchGroupList()
}

// 页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  const start = (val - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  tableData.value = filteredData.value.slice(start, end)
}

onMounted(() => {
  fetchGroupList()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
  background-color: #f5f7fa;

  .search-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  }

  .table-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);

    .group-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .info-content {
        flex: 1;

        .group-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          margin-bottom: 4px;
        }

        .group-desc {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .message-info {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .message-content {
        font-size: 13px;
        color: #303133;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 200px;
      }

      .message-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;

  .username {
    font-size: 14px;
    color: #606266;
  }

  .info-content {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;

    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }

    .user-id {
      font-size: 12px;
      color: #909399;
    }
  }
}

.messages-container {
  max-height: 500px;
  overflow-y: auto;
  padding: 20px;

  .empty-state {
    text-align: center;
    color: #909399;
    padding: 40px 20px;
  }

  .message-item {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
      margin-bottom: 0;
      padding-bottom: 0;
    }

    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;

        .username {
          font-weight: 500;
          color: #303133;
        }
      }

      .time {
        font-size: 12px;
        color: #909399;
      }
    }

    .message-content {
      padding-left: 40px;
      color: #303133;
      line-height: 1.5;
      font-size: 14px;
    }
  }
}
</style>
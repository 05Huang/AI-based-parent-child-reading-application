<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="家长姓名">
          <el-input v-model="searchForm.nickname" placeholder="请输入家长姓名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon"><el-icon><House /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalFamilies }}</div>
          <div class="stat-label">总家庭数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><el-icon><User /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalParents }}</div>
          <div class="stat-label">家长总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><el-icon><Reading /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalChildren }}</div>
          <div class="stat-label">孩子总数</div>
        </div>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="displayedData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="家长信息" min-width="250">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 12px;">
              <el-avatar :size="40" :src="scope.row.avatar" />
              <div>
                <div style="font-weight: 500;">{{ scope.row.nickname || scope.row.username }}</div>
                <div style="font-size: 12px; color: #909399;">{{ scope.row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="孩子数" width="80" align="center">
          <template #default="scope">
            <el-tag>{{ scope.row.childrenCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160" align="center">
          <template #default="scope">{{ formatDate(scope.row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button link type="info" size="small" @click="handleViewChildren(scope.row)">查看孩子</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="家长详情" width="600px" center>
      <div v-if="selectedFamily" class="family-detail">
        <div class="detail-header">
          <el-avatar :size="60" :src="selectedFamily.avatar" />
          <div class="info">
            <h3>{{ selectedFamily.nickname || selectedFamily.username }}</h3>
            <div class="meta">
              <span>用户名: {{ selectedFamily.username }}</span>
              <el-tag :type="selectedFamily.status === 1 ? 'success' : 'danger'">
                {{ selectedFamily.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="detail-content">
          <div class="info-section">
            <h4>基本信息</h4>
            <div class="info-item">
              <span class="label">邮箱:</span>
              <span class="value">{{ selectedFamily.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别:</span>
              <span class="value">{{ selectedFamily.sex === 1 ? '男' : selectedFamily.sex === 2 ? '女' : '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间:</span>
              <span class="value">{{ formatDate(selectedFamily.createdTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">孩子数:</span>
              <span class="value">{{ selectedFamily.childrenCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 孩子列表弹窗 -->
    <el-dialog v-model="childrenDialogVisible" :title="`${selectedFamily?.nickname || selectedFamily?.username}的孩子`" width="700px" center>
      <el-table v-loading="childrenLoading" :data="childrenList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="孩子信息" min-width="250">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 12px;">
              <el-avatar :size="32" :src="scope.row.avatar" />
              <div>
                <div style="font-weight: 500;">{{ scope.row.nickname || scope.row.username }}</div>
                <div style="font-size: 12px; color: #909399;">{{ scope.row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="关系" width="100" align="center">
          <template #default="scope">
            {{ scope.row.relationType }}
          </template>
        </el-table-column>
        <el-table-column label="绑定时间" width="160" align="center">
          <template #default="scope">{{ formatDate(scope.row.bindTime) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="childrenDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, House, User, Reading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFamilyList, getFamilyDetail, getFamilyStats } from '@/api/family'

const searchForm = reactive({
  nickname: '',
  username: ''
})

const stats = ref({
  totalFamilies: 0,
  totalParents: 0,
  totalChildren: 0
})

const loading = ref(false)
const childrenLoading = ref(false)
const tableData = ref([])
const childrenList = ref([])
const selectedFamily = ref(null)
const detailDialogVisible = ref(false)
const childrenDialogVisible = ref(false)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const displayedData = computed(() => {
  return tableData.value
})

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const fetchFamilyList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.nickname) params.nickname = searchForm.nickname
    if (searchForm.username) params.username = searchForm.username

    const response = await getFamilyList(params)
    if (response && response.records) {
      // 获取每个家长的孩子数
      const dataWithChildren = await Promise.all(
        response.records.map(async (item) => {
          let childrenCount = 0
          try {
            const familyDetail = await getFamilyDetail(item.id)
            if (familyDetail && familyDetail.children) {
              childrenCount = familyDetail.children.length
            }
          } catch (error) {
            console.error(`获取用户 ${item.id} 的孩子数失败:`, error)
          }
          return {
            ...item,
            childrenCount
          }
        })
      )
      
      tableData.value = dataWithChildren
      pagination.total = response.total
    }
  } catch (error) {
    console.error('获取家庭列表失败:', error)
    ElMessage.error('获取家庭列表失败')
  } finally {
    loading.value = false
  }
}

const fetchFamilyStats = async () => {
  try {
    const response = await getFamilyStats()
    if (response) {
      stats.value.totalFamilies = response.totalFamilies ?? 0
      stats.value.totalParents = response.totalParents ?? 0
      stats.value.totalChildren = response.totalChildren ?? 0
    }
  } catch (error) {
    console.error('获取家庭统计失败:', error)
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchFamilyList()
}

const handleReset = () => {
  searchForm.nickname = ''
  searchForm.username = ''
  pagination.currentPage = 1
  fetchFamilyList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchFamilyList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchFamilyList()
}

const handleViewDetail = (row) => {
  selectedFamily.value = row
  detailDialogVisible.value = true
}

const handleViewChildren = async (row) => {
  selectedFamily.value = row
  childrenDialogVisible.value = true
  childrenLoading.value = true
  
  try {
    // 调用API获取该家长的孩子列表
    const response = await getFamilyDetail(row.id)
    
    console.log('获取孩子列表响应:', response)
    
    if (response && response.children) {
      // 格式化子女列表，使用正确的字段
      childrenList.value = response.children.map(item => ({
        id: item.id,
        nickname: item.nickname || item.username,
        username: item.username,
        avatar: item.avatar,
        relationType: item.relationType,
        bindTime: item.bindTime,
        role: item.role
      }))
      console.log('孩子列表处理完成:', childrenList.value)
    } else {
      childrenList.value = []
      console.warn('响应中没有children字段，可能该家长没有孩子或接口返回格式不正确')
    }
  } catch (error) {
    console.error('获取孩子列表失败:', error)
    ElMessage.error('获取孩子列表失败: ' + error.message)
    childrenList.value = []
  } finally {
    childrenLoading.value = false
  }
}

onMounted(() => {
  fetchFamilyStats()
  fetchFamilyList()
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

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
      background: #fff;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        background: rgba(64, 158, 255, 0.1);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #409EFF;
        font-size: 24px;
      }

      .stat-content {
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .table-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  }

  .pagination-container {
    margin-top: 20px;
      display: flex;
    justify-content: center;
  }
}

.family-detail {
  .detail-header {
          display: flex;
          align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #EBEEF5;

    .info {
      flex: 1;

      h3 {
        margin: 0 0 8px;
            color: #303133;
        }

      .meta {
          display: flex;
          gap: 12px;
          font-size: 12px;
          color: #909399;
      }
    }
  }

  .detail-content {
    .info-section {
      margin-bottom: 20px;

      h4 {
        margin: 0 0 12px;
        color: #303133;
        font-weight: 600;
      }
    }

    .info-item {
    display: flex;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;

      .label {
        font-size: 14px;
        color: #909399;
        width: 80px;
        flex-shrink: 0;
      }

      .value {
        font-size: 14px;
        color: #303133;
        flex: 1;
      }
    }
  }
}
</style>

<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="评论内容">
          <el-input v-model="searchForm.content" placeholder="请输入评论内容" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="评论状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option label="已通过" :value="1" />
            <el-option label="待审核" :value="0" />
          </el-select>
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
        <div class="stat-icon"><el-icon><ChatDotRound /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalComments }}</div>
          <div class="stat-label">评论总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><el-icon><Clock /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingComments }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="displayedData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="用户信息" min-width="200">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-avatar :size="32" :src="scope.row.userAvatar" />
              <span>{{ scope.row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已通过' : '待审核' }}
              </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="scope">{{ formatDate(scope.row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
              <el-button 
              v-if="scope.row.status === 0"
              link 
                type="success" 
                size="small" 
              @click="handleApprove(scope.row)"
            >通过</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
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
    <el-dialog v-model="detailDialogVisible" title="评论详情" width="600px" center>
      <div v-if="selectedComment" class="comment-detail">
        <div class="detail-header">
          <el-avatar :size="60" :src="selectedComment.userAvatar" />
          <div class="user-info">
            <h3>{{ selectedComment.userName }}</h3>
            <div class="user-meta">
              <el-tag :type="selectedComment.status === 1 ? 'success' : 'warning'" size="small">
                {{ selectedComment.status === 1 ? '已通过' : '待审核' }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="detail-content">
          <div class="content-section">
            <h4>评论内容</h4>
            <div class="content-text">{{ selectedComment.content }}</div>
          </div>
          <div class="time-section">
            <h4>时间信息</h4>
              <div class="time-item">
                <span class="label">创建时间：</span>
              <span class="value">{{ formatDate(selectedComment.createdTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
          v-if="selectedComment && selectedComment.status === 0"
            type="success" 
            @click="handleApprove(selectedComment)"
        >通过审核</el-button>
        <el-button v-if="selectedComment" type="danger" @click="handleDelete(selectedComment)">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, ChatDotRound, Clock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentList, updateCommentStatus, deleteComment } from '@/api/comment'

const searchForm = reactive({
  content: '',
  username: '',
  status: ''
})

const stats = ref({
  totalComments: 0,
  pendingComments: 0
})

const loading = ref(false)
const tableData = ref([])
const selectedComment = ref(null)
const detailDialogVisible = ref(false)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const displayedData = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return tableData.value.slice(start, end)
})

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const fetchCommentList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.content) params.content = searchForm.content
    if (searchForm.username) params.userName = searchForm.username
    if (searchForm.status !== '') params.status = searchForm.status

    const response = await getCommentList(params)
    if (response && response.records) {
      tableData.value = response.records
      pagination.total = response.total
      
      stats.value.totalComments = response.total
      const pendingCount = response.records.filter(item => item.status === 0).length
      stats.value.pendingComments = pendingCount
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchCommentList()
}

const handleReset = () => {
  searchForm.content = ''
  searchForm.username = ''
  searchForm.status = ''
  pagination.currentPage = 1
  fetchCommentList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
    pagination.currentPage = 1
  fetchCommentList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchCommentList()
}

const handleViewDetail = (row) => {
  selectedComment.value = row
  detailDialogVisible.value = true
}

const handleApprove = async (row) => {
  if (row.status === 1) {
    ElMessage.warning('该评论已经通过审核')
    return
  }
  
  ElMessageBox.confirm(`确定要通过 ${row.userName} 的评论吗？`, '审核确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
  }).then(async () => {
    try {
      await updateCommentStatus(row.id, 1)
      ElMessage.success('已通过评论')
      detailDialogVisible.value = false
      fetchCommentList()
    } catch (error) {
      console.error('通过评论失败:', error)
      ElMessage.error('通过评论失败')
    }
  }).catch(() => {
    ElMessage.info('已取消')
  })
}

const handleDelete = async (row) => {
  ElMessageBox.confirm(`确定要删除 ${row.userName} 的评论吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
  }).then(async () => {
    try {
      await deleteComment(row.id)
      ElMessage.success('已删除评论')
      detailDialogVisible.value = false
      fetchCommentList()
    } catch (error) {
      console.error('删除评论失败:', error)
      ElMessage.error('删除评论失败')
    }
  }).catch(() => {
    ElMessage.info('已取消')
  })
}

onMounted(() => {
  fetchCommentList()
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
    grid-template-columns: repeat(2, 1fr);
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

.comment-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #EBEEF5;

    .user-info {
      flex: 1;

      h3 {
        margin: 0 0 8px;
        color: #303133;
      }

      .user-meta {
        display: flex;
        gap: 8px;
      }
    }
  }

  .detail-content {
    .content-section,
    .time-section {
      margin-bottom: 20px;

      h4 {
        margin: 0 0 12px;
        color: #303133;
        font-weight: 600;
      }
    }

    .content-text {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      padding: 12px;
      background: #F5F7FA;
      border-radius: 4px;
    }

      .time-item {
        display: flex;
        padding: 8px 0;

        .label {
          font-size: 14px;
          color: #909399;
          width: 80px;
        }

        .value {
          font-size: 14px;
          color: #303133;
      }
    }
  }
}
</style>





<template>
  <div class="dashboard-container">
    <!-- 数据统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" @click="navigateTo('user')">
        <div class="icon-wrapper">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="label">总用户数</div>
          <div class="value">{{ statistics.totalUsers }}</div>
        </div>
      </div>
      <div class="stat-card" @click="navigateTo('article')">
        <div class="icon-wrapper">
          <el-icon><Reading /></el-icon>
        </div>
        <div class="stat-info">
          <div class="label">文章总数</div>
          <div class="value">{{ statistics.totalArticles }}</div>
        </div>
      </div>
      <div class="stat-card" @click="navigateTo('video')">
        <div class="icon-wrapper">
          <el-icon><VideoPlay /></el-icon>
        </div>
        <div class="stat-info">
          <div class="label">视频总数</div>
          <div class="value">{{ statistics.totalVideos }}</div>
        </div>
      </div>
      <div class="stat-card" @click="navigateTo('comment')">
        <div class="icon-wrapper">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="stat-info">
          <div class="label">评论总数</div>
          <div class="value">{{ statistics.totalComments }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-container">
      <!-- 文章分类分布饼图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>文章分类分布</h3>
        </div>
        <div class="chart-content" ref="articleCategoryChart"></div>
      </div>

      <!-- 视频分类分布饼图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>视频分类分布</h3>
        </div>
        <div class="chart-content" ref="videoCategoryChart"></div>
      </div>
    </div>

    <!-- 最新动态 -->
    <div class="latest-container">
      <div class="latest-card">
        <div class="card-header">
          <h3>最新用户</h3>
          <el-link type="primary" @click="showMoreUsers">查看更多</el-link>
        </div>
        <div class="card-content">
          <el-table v-loading="latestUsersLoading" :data="latestUsers" style="width: 100%" :show-header="false">
            <el-table-column>
              <template #default="scope">
                <div class="user-item">
                  <el-avatar :size="32" :src="scope.row.avatar" />
                  <span class="username">{{ scope.row.nickname || scope.row.username }}</span>
                  <span class="time">{{ formatDate(scope.row.createdTime) }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="latest-card">
        <div class="card-header">
          <h3>最新内容</h3>
          <el-link type="primary" @click="showMoreContent">查看更多</el-link>
        </div>
        <div class="card-content">
          <el-table v-loading="latestContentLoading" :data="latestContent" style="width: 100%" :show-header="false">
            <el-table-column>
              <template #default="scope">
                <div class="content-item">
                  <el-icon :size="24" :class="scope.row.type === 1 ? 'book' : 'video'">
                    <Reading v-if="scope.row.type === 1" />
                    <VideoPlay v-else />
                  </el-icon>
                  <span class="title">{{ scope.row.title }}</span>
                  <span class="time">{{ formatDate(scope.row.createdTime) }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="userDialogVisible" title="用户列表" width="900px" center>
      <el-table v-loading="allUsersLoading" :data="allUsers" style="width: 100%">
        <el-table-column type="index" width="50" align="center" label="序号" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.role === 1 ? 'success' : scope.row.role === 2 ? 'info' : 'warning'">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160" align="center">
          <template #default="scope">{{ formatDate(scope.row.createdTime) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
          <el-button @click="userDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 内容详情对话框 -->
    <el-dialog v-model="contentDialogVisible" title="内容列表" width="900px" center>
      <el-table v-loading="allContentLoading" :data="allContent" style="width: 100%">
        <el-table-column type="index" width="50" align="center" label="序号" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="scope">
            <el-icon :size="24" :class="scope.row.type === 1 ? 'book' : 'video'">
              <Reading v-if="scope.row.type === 1" />
              <VideoPlay v-else />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="分类" width="100" align="center">
          <template #default="scope">
            {{ getCategoryName(scope.row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="scope">{{ formatDate(scope.row.createdTime) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
          <el-button @click="contentDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Reading, VideoPlay, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  getUserStats,
  getArticleStats,
  getVideoStats,
  getCommentStats,
  getLatestUsers,
  getLatestContent,
  getCategoryStats
} from '@/api/dashboard'
import request from '@/utils/request' // 引入request工具

const router = useRouter()

// 统计数据
const statistics = ref({
  totalUsers: 0,
  totalArticles: 0,
  totalVideos: 0,
  totalComments: 0
})

// 图表相关
const userTrendChart = ref(null)
const contentDistChart = ref(null)
const articleCategoryChart = ref(null)
const videoCategoryChart = ref(null)

// 最新数据
const latestUsers = ref([])
const latestContent = ref([])
const latestUsersLoading = ref(false)
const latestContentLoading = ref(false)

// 弹窗相关
const userDialogVisible = ref(false)
const contentDialogVisible = ref(false)
const allUsers = ref([])
const allContent = ref([])
const allUsersLoading = ref(false)
const allContentLoading = ref(false)

// 分类列表
const categories = ref([])

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const getRoleText = (role) => {
  const roleMap = { 0: '管理员', 1: '家长', 2: '孩子' }
  return roleMap[role] || '未知'
}

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '-'
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const [usersRes, articlesRes, videosRes, commentsRes] = await Promise.all([
      getUserStats(),
      getArticleStats(),
      getVideoStats(),
      getCommentStats()
    ])

    statistics.value.totalUsers = usersRes?.total || 0
    statistics.value.totalArticles = articlesRes?.total || 0
    statistics.value.totalVideos = videosRes?.total || 0
    statistics.value.totalComments = commentsRes?.total || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取最新用户
const fetchLatestUsers = async () => {
  latestUsersLoading.value = true
  try {
    const response = await getLatestUsers(5)
    if (response && response.records) {
      latestUsers.value = response.records
    }
  } catch (error) {
    console.error('获取最新用户失败:', error)
  } finally {
    latestUsersLoading.value = false
  }
}

// 获取最新内容
const fetchLatestContent = async () => {
  latestContentLoading.value = true
  try {
    const response = await getLatestContent(5)
    if (response && response.records) {
      latestContent.value = response.records
    }
  } catch (error) {
    console.error('获取最新内容失败:', error)
  } finally {
    latestContentLoading.value = false
  }
}

// 获取所有用户
const fetchAllUsers = async () => {
  allUsersLoading.value = true
  try {
    const response = await getLatestUsers(100)
    if (response && response.records) {
      allUsers.value = response.records
    }
  } catch (error) {
    console.error('获取所有用户失败:', error)
  } finally {
    allUsersLoading.value = false
  }
}

// 获取所有内容
const fetchAllContent = async () => {
  allContentLoading.value = true
  try {
    const response = await getLatestContent(100)
    if (response && response.records) {
      allContent.value = response.records
    }
  } catch (error) {
    console.error('获取所有内容失败:', error)
  } finally {
    allContentLoading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await getCategoryStats()
    let categoryList = Array.isArray(response) ? response : (response?.records || [])
    
    if (categoryList.length > 0) {
      // 分别获取文章和视频内容
      try {
        const [articlesData, videosData] = await Promise.all([
          // 获取所有文章内容
          (async () => {
            const allContent = []
            let currentPage = 1
            let hasMore = true
            
            while (hasMore) {
              const contentRes = await request({
                url: '/api/content/page',
                method: 'get',
                params: { 
                  current: currentPage, 
                  size: 100,
                  type: 1  // 只获取文章
                }
              })
              
              if (contentRes?.records && contentRes.records.length > 0) {
                allContent.push(...contentRes.records)
                if (contentRes.records.length < 100) {
                  hasMore = false
                } else {
                  currentPage++
                }
              } else {
                hasMore = false
              }
            }
            
            return allContent
          })(),
          // 获取所有视频内容
          (async () => {
            const allContent = []
            let currentPage = 1
            let hasMore = true
            
            while (hasMore) {
              const contentRes = await request({
                url: '/api/content/page',
                method: 'get',
                params: { 
                  current: currentPage, 
                  size: 100,
                  type: 2  // 只获取视频
                }
              })
              
              if (contentRes?.records && contentRes.records.length > 0) {
                allContent.push(...contentRes.records)
                if (contentRes.records.length < 100) {
                  hasMore = false
                } else {
                  currentPage++
                }
              } else {
                hasMore = false
              }
            }
            
            return allContent
          })()
        ])
        
        // 为分类添加统计信息
        const categoriesWithInfo = categoryList.map(category => {
          // 分别统计该分类下的文章和视频数量
          const articleCount = articlesData.filter(c => c.categoryId === category.id).length
          const videoCount = videosData.filter(c => c.categoryId === category.id).length
          
          return { 
            ...category, 
            articleCount,
            videoCount,
            totalCount: articleCount + videoCount
          }
        })
        
        categories.value = categoriesWithInfo
        console.log('分类统计数据加载完成')
        console.log('总文章数:', articlesData.length)
        console.log('总视频数:', videosData.length)
        console.log('分类详情:', categoriesWithInfo)
      } catch (error) {
        console.error('获取内容统计失败:', error)
        categories.value = categoryList.map(c => ({
          ...c,
          articleCount: 0,
          videoCount: 0,
          totalCount: 0
        }))
      }
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 查看更多用户
const showMoreUsers = async () => {
  userDialogVisible.value = true
  if (allUsers.value.length === 0) {
    await fetchAllUsers()
  }
}

// 查看更多内容
const showMoreContent = async () => {
  contentDialogVisible.value = true
  if (allContent.value.length === 0) {
    await fetchAllContent()
  }
}

// 导航到管理页面
const navigateTo = (type) => {
  let path = ''
  let typeName = ''
  
  switch (type) {
    case 'user':
      path = '/dashboard-layout/user'
      typeName = '用户'
      break
    case 'article':
      path = '/dashboard-layout/articles'
      typeName = '文章'
      break
    case 'video':
      path = '/dashboard-layout/videos'
      typeName = '视频'
      break
    case 'comment':
      path = '/dashboard-layout/comments'
      typeName = '评论'
      break
    default:
      return
  }
  
  router.push(path)
}

// 初始化图表
const initCharts = () => {
  // 初始化文章分类分布饼图
  if (articleCategoryChart.value) {
    const articleChart = echarts.init(articleCategoryChart.value)
    // 只显示有文章的分类
    const articleCategories = categories.value.filter(c => c.articleCount > 0)
    const articleData = articleCategories.map(c => ({
      name: c.name,
      value: c.articleCount || 0
    }))

    console.log('文章分类饼图数据:', articleData)
    
    articleChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', right: 10, top: 'center' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        data: articleData.length > 0 ? articleData : [{ name: '暂无数据', value: 1 }],
        label: { show: false },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // 初始化视频分类分布饼图
  if (videoCategoryChart.value) {
    const videoChart = echarts.init(videoCategoryChart.value)
    // 只显示有视频的分类
    const videoCategories = categories.value.filter(c => c.videoCount > 0)
    const videoData = videoCategories.map(c => ({
      name: c.name,
      value: c.videoCount || 0
    }))

    console.log('视频分类饼图数据:', videoData)

    videoChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', right: 10, top: 'center' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        data: videoData.length > 0 ? videoData : [{ name: '暂无数据', value: 1 }],
        label: { show: false },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }
}

let handleResize = null  // 在组件外部定义，以便在 onUnmounted 中引用

onMounted(async () => {
  // 获取所有数据
  await Promise.all([
    fetchStatistics(),
    fetchLatestUsers(),
    fetchLatestContent(),
    fetchCategories()
  ])
  
  // 初始化图表
  setTimeout(() => {
    initCharts()
  }, 100)

  // 监听窗口大小变化
  handleResize = () => {
    if (articleCategoryChart.value) {
      echarts.getInstanceByDom(articleCategoryChart.value)?.resize()
    }
    if (videoCategoryChart.value) {
      echarts.getInstanceByDom(videoCategoryChart.value)?.resize()
    }
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (handleResize) {
    window.removeEventListener('resize', handleResize)
  }
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;

  .stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;

    .stat-card {
      background: #fff;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer; /* Add cursor pointer for clickability */
      transition: background-color 0.3s ease;

      &:hover {
        background-color: #f5f7fa;
      }

      .icon-wrapper {
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

      .stat-info {
        flex: 1;

        .label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 8px;
        }

        .value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }

  .chart-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;

    .chart-card {
      background: #fff;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
      height: 400px;
      display: flex;
      flex-direction: column;

      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 500;
        }
      }

      .chart-content {
        flex: 1;
        min-height: 300px;
      }
    }
  }

  .latest-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;

    .latest-card {
      background: #fff;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
      height: 400px;
      display: flex;
      flex-direction: column;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 500;
        }
      }

      .card-content {
        flex: 1;
        overflow-y: auto;
      }

      .user-item, .content-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .username, .title {
          margin: 0 12px;
          flex: 1;
          color: #303133;
        }

        .time {
          color: #909399;
          font-size: 12px;
        }
      }
    }
  }
}

@media screen and (max-width: 1400px) {
  .dashboard-container {
    .stat-cards {
      grid-template-columns: repeat(2, 1fr);
    }

    .chart-container,
    .latest-container {
      grid-template-columns: 1fr;
    }
  }
}

@media screen and (max-width: 768px) {
  .dashboard-container {
    .stat-cards {
      grid-template-columns: 1fr;
    }
  }
}

.content-type-icon {
  font-size: 20px;
  
  &.book {
    color: #409EFF;
  }
  
  &.video {
    color: #67C23A;
  }
}
</style> 


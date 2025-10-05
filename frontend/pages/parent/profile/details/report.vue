<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">浏览报告</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 总览卡片 -->
      <view class="overview-card">
        <view class="overview-header">
          <text class="overview-title">本月浏览概览</text>
          <view class="date-picker">
            <text class="fas fa-calendar"></text>
            <text class="date-text">2024年2月</text>
          </view>
        </view>
        <view class="overview-grid">
          <view class="overview-item">
            <text class="overview-value">{{reportDisplay.totalDuration}}</text>
            <text class="overview-label">总浏览时长(分钟)</text>
          </view>
          <view class="overview-item">
            <text class="overview-value">{{reportDisplay.totalArticles}}</text>
            <text class="overview-label">浏览文章</text>
          </view>
          <view class="overview-item">
            <text class="overview-value">{{reportDisplay.interactionCount}}</text>
            <text class="overview-label">互动次数</text>
          </view>
          <view class="overview-item">
            <text class="overview-value">{{reportDisplay.activityScore}}%</text>
            <text class="overview-label">活跃度</text>
          </view>
        </view>
      </view>

      <!-- 浏览习惯分析 -->
      <view class="habits-card">
        <text class="card-title">浏览习惯分析</text>
        <view class="habits-grid">
          
          <view class="habit-item">
            <view class="habit-icon-box">
              <text class="fas fa-chart-line habit-icon"></text>
            </view>
            <view class="habit-info">
              <text class="habit-title">平均浏览时长</text>
              <text class="habit-value">{{reportDisplay.avgReadTime}}分钟</text>
            </view>
          </view>
          <view class="habit-item">
            <view class="habit-icon-box">
              <text class="fas fa-star habit-icon"></text>
            </view>
            <view class="habit-info">
              <text class="habit-title">最爱内容类型</text>
              <text class="habit-value">{{reportDisplay.favoriteCategory}}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 本月热门文章 -->
      <view class="books-card">
        <view class="card-header">
          <text class="card-title">本月热门文章</text>
          <text class="more-link">查看全部</text>
        </view>
        <scroll-view scroll-x="true" class="books-scroll">
          <view class="book-item" v-for="(article, index) in completedArticles" :key="index">
            <image :src="article.coverUrl" class="book-cover"></image>
            <view class="book-info">
              <text class="book-title">{{article.title}}</text>
              <view class="article-stats">
                <text class="stat-item">
                  <text class="fas fa-thumbs-up"></text>
                  {{article.likes}}
                </text>
                <text class="stat-item">
                  <text class="fas fa-comment"></text>
                  {{article.comments}}
                </text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 浏览建议 -->
      <view class="suggestions-card">
        <text class="card-title">内容建议</text>
        <view class="suggestion-list">
          <view class="suggestion-item">
            <text class="fas fa-lightbulb suggestion-icon"></text>
            <text class="suggestion-text">建议多关注教育心得类文章，提升育儿技巧</text>
          </view>
          <view class="suggestion-item">
            <text class="fas fa-users suggestion-icon"></text>
            <text class="suggestion-text">可以多参与评论互动，分享您的育儿经验</text>
          </view>
          <view class="suggestion-item">
            <text class="fas fa-share-alt suggestion-icon"></text>
            <text class="suggestion-text">分享优质内容，帮助更多家长</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { userBehaviorApi, userApi, viewHistoryApi } from '@/utils/api.js'

// 响应式状态
const currentUser = ref(null)
const weeklyReport = ref({
  totalReadDuration: 0,
  totalArticleCount: 0,
  interactionCount: 0,
  activityScore: 0,
  dailyAvgReadTime: 0,
  favoriteCategory: '未知'
})
const completedArticles = ref([])
const loading = ref(false)

// 计算月报统计数据显示
const reportDisplay = computed(() => {
  // 后端返回的activityScore是0-26分的绝对分数（满分26 = 20*0.3 + 20*0.4 + 40*0.3）
  // 需要转换为百分比：(分数 / 26) * 100
  const rawScore = weeklyReport.value.activityScore || 0
  const activityPercent = Math.min(Math.round((rawScore / 26) * 100), 100) // 转为百分比，最高100%
  
  console.log('活跃度计算 - 原始分数:', rawScore, '转换后百分比:', activityPercent)
  
  return {
    totalDuration: Math.floor((weeklyReport.value.totalReadDuration || 0) / 60), // 转为分钟
    totalArticles: weeklyReport.value.totalArticleCount || 0,
    interactionCount: weeklyReport.value.interactionCount || 0,
    activityScore: activityPercent, // 显示为百分比
    avgReadTime: Math.floor((weeklyReport.value.dailyAvgReadTime || 0) / 60), // 转为分钟
    favoriteCategory: weeklyReport.value.favoriteCategory || '育儿经验'
  }
})

// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    console.log('开始获取当前用户信息')
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      console.log('获取用户信息成功：', response.data)
      currentUser.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    uni.showToast({
      title: '获取用户信息失败',
      icon: 'none'
    })
  }
}

// 获取周报数据
const loadWeeklyReport = async () => {
  try {
    if (!currentUser.value?.id) {
      console.log('用户ID不存在，跳过加载周报数据')
      return
    }
    
    console.log('开始获取周报数据，用户ID：', currentUser.value.id)
    const response = await userBehaviorApi.getWeeklyReport(currentUser.value.id)
    
    console.log('周报API完整响应：', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      console.log('获取周报数据成功：', response.data)
      // 确保所有字段都有默认值
      weeklyReport.value = {
        totalReadDuration: response.data.totalReadDuration || 0,
        totalArticleCount: response.data.totalArticleCount || 0,
        interactionCount: response.data.interactionCount || 0,
        activityScore: response.data.activityScore || 0,
        dailyAvgReadTime: response.data.dailyAvgReadTime || 0,
        favoriteCategory: response.data.favoriteCategory || '育儿经验'
      }
      console.log('更新后的weeklyReport：', weeklyReport.value)
    } else {
      console.warn('周报响应格式异常或无数据，使用默认值')
    }
  } catch (error) {
    console.error('获取周报数据失败：', error)
    // 不显示错误提示，使用默认值
  }
}

// 获取热门文章（从浏览历史中提取）
const loadHotArticles = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    console.log('开始获取热门文章，用户ID：', currentUser.value.id)
    
    const response = await viewHistoryApi.getUserViewHistory(currentUser.value.id, {
      current: 1,
      size: 10
    })
    
    if (response && response.data && response.data.records) {
      console.log('获取浏览历史成功，筛选热门文章')
      
      // 转换并筛选热门文章（取前3个）
      const articles = response.data.records.slice(0, 3).map(record => ({
        id: record.id,
        title: record.contentTitle || '无标题',
        coverUrl: record.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
        duration: Math.floor(Math.random() * 120) + 60, // 模拟阅读时长（秒）
        likes: record.likeCount || Math.floor(Math.random() * 500) + 100,
        comments: record.commentCount || Math.floor(Math.random() * 100) + 10,
        contentId: record.contentId,
        contentType: record.contentType
      }))
      
      completedArticles.value = articles
      console.log('热门文章加载完成，共', articles.length, '篇')
    }
  } catch (error) {
    console.error('获取热门文章失败：', error)
    // 使用默认文章
    completedArticles.value = [
      {
        title: '如何培养孩子的学习兴趣',
        coverUrl: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
        duration: 120,
        likes: 256,
        comments: 45
      },
      {
        title: '亲子沟通的艺术',
        coverUrl: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop',
        duration: 90,
        likes: 189,
        comments: 32
      },
      {
        title: '儿童成长关键期指南',
        coverUrl: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&auto=format&fit=crop',
        duration: 150,
        likes: 324,
        comments: 56
      }
    ]
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('浏览报告页面已挂载，开始加载数据')
  await loadCurrentUser()
  await loadWeeklyReport()
  await loadHotArticles()
})
</script>

<style>
/* 引入Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  width: 100%;
  box-sizing: border-box;
}

/* 顶部导航样式 */
.nav-header {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
}

.nav-content {
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

.back-btn {
  padding: 20rpx;
  margin: -20rpx;
  margin-right: 10rpx;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
}

/* 主要内容区域 */
.main-content {
  margin-top: 88rpx;
  padding: 30rpx;
  height: calc(100vh - 88rpx);
  width: 100%;
  box-sizing: border-box;
}

/* 总览卡片样式 */
.overview-card {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  color: #ffffff;
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.overview-title {
  font-size: 32rpx;
  font-weight: bold;
}

.date-picker {
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 28rpx;
  opacity: 0.9;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
}

.overview-item {
  text-align: center;
}

.overview-value {
  font-size: 48rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
  display: block;
}

.overview-label {
  font-size: 24rpx;
  opacity: 0.9;
}

/* 趋势卡片样式 */
.trend-card, .habits-card, .books-card, .suggestions-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
}

.time-range {
  display: flex;
  gap: 16rpx;
}

.range-item {
  padding: 8rpx 24rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  color: #6b7280;
  background-color: #f3f4f6;
}

.range-item.active {
  background-color: #3b82f6;
  color: #ffffff;
}

.placeholder-chart {
  height: 300rpx;
  background-color: #f3f4f6;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-text {
  color: #6b7280;
  font-size: 28rpx;
}

/* 阅读习惯分析样式 */
.habits-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 20rpx;
}

.habit-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background-color: #f9fafb;
  border-radius: 16rpx;
}

.habit-icon-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background-color: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.habit-icon {
  font-size: 36rpx;
  color: #ffffff;
}

.habit-info {
  flex: 1;
}

.habit-title {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 8rpx;
}

.habit-value {
  font-size: 32rpx;
  font-weight: 500;
  color: #111827;
}

/* 完成书籍样式 */
.books-scroll {
  white-space: nowrap;
  margin: 0 -30rpx;
  padding: 0 30rpx;
}

.book-item {
  display: inline-block;
  width: 200rpx;
  margin-right: 20rpx;
}

.book-cover {
  width: 200rpx;
  height: 280rpx;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
}

.book-info {
  white-space: normal;
}

.book-title {
  font-size: 28rpx;
  font-weight: 500;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-duration {
  font-size: 24rpx;
  color: #6b7280;
}

/* 阅读建议样式 */
.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background-color: #f9fafb;
  border-radius: 16rpx;
}

.suggestion-icon {
  font-size: 32rpx;
  color: #3b82f6;
}

.suggestion-text {
  font-size: 28rpx;
  color: #374151;
  flex: 1;
}

.more-link {
  font-size: 28rpx;
  color: #6b7280;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}

.article-stats {
  display: flex;
  gap: 16rpx;
  margin-top: 8rpx;
  background-color: #ffffff;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  color: #6b7280;
  font-size: 24rpx;
}

.stat-item .fas {
  color: #60a5fa;
}
</style>

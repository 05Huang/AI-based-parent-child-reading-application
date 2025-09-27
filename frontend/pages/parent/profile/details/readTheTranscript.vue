<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">浏览记录</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 统计卡片 -->
      <view class="stats-card">
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.totalDuration}}</text>
            <text class="stat-label">浏览总时长(分钟)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.totalRead}}</text>
            <text class="stat-label">浏览文章</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.weeklyDays}}</text>
            <text class="stat-label">本周浏览(次)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.interactionRate}}%</text>
            <text class="stat-label">互动率</text>
          </view>
        </view>
      </view>

      <!-- 浏览记录列表 -->
      <view class="reading-list">
        <view class="section-header">
          <text class="section-title">浏览记录</text>
          <view class="filter-btn">
            <text class="fas fa-filter"></text>
            <text class="filter-text">筛选</text>
          </view>
        </view>
        
        <view class="book-records">
          <view v-for="(record, index) in readingRecords" :key="index" class="book-record-item">
            <image :src="record.coverUrl" class="book-cover"></image>
            <view class="book-info">
              <view class="book-main-info">
                <text class="book-title">{{record.title}}</text>
                <text class="book-author">{{record.author}}</text>
                <view class="reading-progress">
                  <view class="stat">
                    <text class="fas fa-thumbs-up stat-icon"></text>
                    <text class="stat-text">{{record.likes}}赞</text>
                  </view>
                  <view class="stat">
                    <text class="fas fa-comment stat-icon"></text>
                    <text class="stat-text">{{record.comments}}评论</text>
                  </view>
                </view>
              </view>
              <view class="reading-stats">
                <view class="stat">
                  <text class="fas fa-clock stat-icon"></text>
                  <text class="stat-text">{{record.duration}}分钟前</text>
                </view>
                <view class="stat">
                  <text class="fas fa-tag stat-icon"></text>
                  <text class="stat-text">{{record.category}}</text>
                </view>
              </view>
            </view>
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
const browsingStats = ref({
  totalReadDuration: 0,
  totalReadCount: 0,
  weeklyViewCount: 0,
  interactionRate: 0
})
const readingRecords = ref([])
const loading = ref(false)

// 时间范围选择器数据
const timeRanges = ['近7天', '近30天', '近3个月']
const currentTimeRange = ref('近7天')

// 计算统计数据显示
const statsDisplay = computed(() => {
  return {
    totalDuration: Math.floor((browsingStats.value.totalReadDuration || 0) / 60), // 转为分钟
    totalRead: browsingStats.value.totalReadCount || 0,
    weeklyDays: browsingStats.value.weeklyViewCount || 0,
    interactionRate: Math.round((browsingStats.value.interactionRate || 0) * 100)
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

// 获取浏览统计数据
const loadBrowsingStats = async () => {
  try {
    if (!currentUser.value?.id) return
    
    console.log('开始获取浏览统计数据，用户ID：', currentUser.value.id)
    const response = await userBehaviorApi.getBrowsingStats(currentUser.value.id)
    
    if (response && response.data) {
      console.log('获取浏览统计成功：', response.data)
      browsingStats.value = response.data
    }
  } catch (error) {
    console.error('获取浏览统计失败：', error)
    // 不显示错误提示，使用默认值
  }
}

// 获取浏览记录
const loadReadingRecords = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    console.log('开始获取浏览记录，用户ID：', currentUser.value.id)
    
    const response = await viewHistoryApi.getUserViewHistory(currentUser.value.id, {
      current: 1,
      size: 20
    })
    
    if (response && response.data && response.data.records) {
      console.log('获取浏览记录成功，共', response.data.records.length, '条')
      
      // 转换数据格式
      readingRecords.value = response.data.records.map(record => ({
        id: record.id,
        title: record.contentTitle || '无标题',
        author: record.creatorName || '佚名',
        coverUrl: record.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
        likes: record.likeCount || 0,
        comments: record.commentCount || 0,
        duration: getTimeDiffInMinutes(record.viewTime),
        category: record.categoryName || '未分类',
        contentId: record.contentId,
        contentType: record.contentType
      }))
    }
  } catch (error) {
    console.error('获取浏览记录失败：', error)
    uni.showToast({
      title: '获取记录失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 计算时间差（分钟）
const getTimeDiffInMinutes = (viewTime) => {
  if (!viewTime) return 0
  const now = new Date()
  const view = new Date(viewTime)
  const diffMs = now - view
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  return diffMinutes < 1 ? 1 : diffMinutes
}

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 时间范围变化处理
const onTimeRangeChange = (e) => {
  currentTimeRange.value = timeRanges[e.detail.value]
  console.log('时间范围改变为：', currentTimeRange.value)
  // 重新加载数据
  loadReadingRecords()
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('浏览记录页面已挂载，开始加载数据')
  await loadCurrentUser()
  await loadBrowsingStats()
  await loadReadingRecords()
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

/* 统计卡片样式 */
.stats-card {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.15);
  width: 100%;
  box-sizing: border-box;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
  width: 100%;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #ffffff;
}

.stat-value {
  font-size: 48rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 24rpx;
  opacity: 0.9;
}

/* 趋势图区域样式 */
.trend-section {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  width: 100%;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
}

.time-picker {
  font-size: 28rpx;
  color: #6b7280;
  background-color: #f3f4f6;
  padding: 8rpx 24rpx;
  border-radius: 9999rpx;
}

.trend-chart {
  height: 300rpx;
  width: 100%;
}

/* 阅读记录列表样式 */
.reading-list {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #6b7280;
  font-size: 28rpx;
}

.book-records {
  margin-top: 20rpx;
  width: 100%;
}

.book-record-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1px solid #e5e7eb;
  width: 100%;
  box-sizing: border-box;
}

.book-record-item:last-child {
  border-bottom: none;
}

.book-cover {
  width: 160rpx;
  height: 220rpx;
  border-radius: 12rpx;
  object-fit: cover;
  flex-shrink: 0;
}

.book-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.book-main-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 8rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
}

.reading-progress {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background-color: #ffffff;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
  margin: 10rpx 0;
}

.reading-stats {
  display: flex;
  gap: 30rpx;
  margin-top: 16rpx;
  background-color: #ffffff;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
}

.stat {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-icon {
  font-size: 24rpx;
  color: #60a5fa;
}

.stat-text {
  font-size: 24rpx;
  color: #6b7280;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style>

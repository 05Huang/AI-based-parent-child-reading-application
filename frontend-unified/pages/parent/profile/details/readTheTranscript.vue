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
      <view class="card stats-card">
        <view class="card-header">
          <text class="card-title">本月浏览概览</text>
          <view class="action-btn" @click="refreshCache">
            <text class="fas fa-sync-alt icon-small" :class="{ 'rotating': refreshing }"></text>
            <text>刷新数据</text>
          </view>
        </view>
        
        <view class="stats-grid">
          <!-- 核心指标 -->
          <view class="stat-group primary">
            <view class="stat-item">
              <view class="stat-icon-bg blue">
                <text class="fas fa-clock stat-icon"></text>
              </view>
              <view class="stat-info">
                <text class="stat-value">{{statsDisplay.totalDuration}}</text>
                <text class="stat-label">总浏览时长(分钟)</text>
              </view>
            </view>
            <view class="stat-item">
              <view class="stat-icon-bg green">
                <text class="fas fa-book-open stat-icon"></text>
              </view>
              <view class="stat-info">
                <text class="stat-value">{{statsDisplay.totalRead}}</text>
                <text class="stat-label">浏览文章(篇)</text>
              </view>
            </view>
          </view>
          
          <view class="divider-v"></view>
          
          <!-- 次要指标 -->
          <view class="stat-group secondary">
            <view class="stat-mini-item">
              <text class="stat-mini-label">本周浏览</text>
              <text class="stat-mini-value">{{statsDisplay.weeklyDays}}<text class="unit">次</text></text>
            </view>
            <view class="stat-mini-item">
              <text class="stat-mini-label">互动率</text>
              <text class="stat-mini-value">{{statsDisplay.interactionRate}}<text class="unit">%</text></text>
            </view>
          </view>
        </view>
      </view>

      <!-- 浏览记录列表 -->
      <view class="card reading-list-card">
        <view class="card-header">
          <text class="card-title">浏览记录</text>
          <view class="action-btn" @click="showFilter">
            <text class="fas fa-filter icon-small"></text>
            <text>筛选</text>
          </view>
        </view>
        
        <view class="book-records">
          <!-- 有记录时显示列表 -->
          <view v-if="readingRecords.length > 0">
            <view v-for="(record, index) in readingRecords" :key="index" class="record-item">
              <image :src="record.coverUrl" class="record-cover" mode="aspectFill"></image>
              <view class="record-content">
                <view class="record-header">
                  <text class="record-title">{{record.title}}</text>
                </view>
                
                <view class="record-meta">
                  <view class="meta-tag" :class="getCategoryClass(record.category)">
                    <text class="fas" :class="getCategoryIcon(record.category)"></text>
                    <text>{{record.category}}</text>
                  </view>
                  <text class="meta-time">{{record.timeDisplay}}</text>
                </view>
                
                <view class="record-footer">
                  <view class="author-info" v-if="record.author && record.author !== '佚名'">
                    <text class="fas fa-user-circle author-icon"></text>
                    <text class="author-name">{{record.author}}</text>
                  </view>
                  <view class="interaction-stats">
                    <view class="stat-pill">
                      <text class="fas fa-thumbs-up"></text>
                      <text>{{record.likes}}</text>
                    </view>
                    <view class="stat-pill">
                      <text class="fas fa-comment"></text>
                      <text>{{record.comments}}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
          
          <!-- 无记录时显示空状态 -->
          <view v-else class="empty-state">
            <view class="empty-icon-bg">
              <text class="fas fa-book-open empty-icon"></text>
            </view>
            <text class="empty-text">暂无浏览记录</text>
            <text class="empty-hint">去首页看看精彩文章吧</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
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
const refreshing = ref(false)

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
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      currentUser.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
  }
}

// 获取浏览统计数据
const loadBrowsingStats = async () => {
  try {
    if (!currentUser.value?.id) return
    const response = await userBehaviorApi.getBrowsingStats(currentUser.value.id)
    if (response && response.code === 200 && response.data) {
      browsingStats.value = {
        totalReadDuration: response.data.totalReadDuration || 0,
        totalReadCount: response.data.totalReadCount || 0,
        weeklyViewCount: response.data.weeklyViewCount || 0,
        interactionRate: response.data.interactionRate || 0
      }
    }
  } catch (error) {
    console.error('获取浏览统计失败：', error)
  }
}

// 获取浏览记录
const loadReadingRecords = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    loading.value = true
    
    const response = await viewHistoryApi.getUserViewHistory(currentUser.value.id, {
      current: 1,
      size: 20
    })
    
    if (response && response.data && response.data.records) {
      readingRecords.value = response.data.records
        .filter(record => record.contentId && record.contentTitle)
        .map(record => ({
          id: record.id,
          title: record.contentTitle,
          author: record.creatorName,
          coverUrl: record.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
          likes: record.likeCount || 0,
          comments: record.commentCount || 0,
          timeDisplay: formatTimeDisplay(record.viewTime),
          category: record.categoryName || '未分类',
          contentId: record.contentId
        }))
    }
  } catch (error) {
    console.error('获取浏览记录失败：', error)
  } finally {
    loading.value = false
  }
}

// 格式化时间显示
const formatTimeDisplay = (viewTime) => {
  if (!viewTime) return '刚刚'
  const now = new Date()
  const view = new Date(viewTime)
  const diffMs = now - view
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  
  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}小时前`
  
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) return `${diffDays}天前`
  
  return `${view.getMonth() + 1}月${view.getDate()}日`
}

// 获取分类图标
const getCategoryIcon = (category) => {
  const map = {
    '科普知识': 'fa-atom',
    '教育': 'fa-graduation-cap',
    '故事': 'fa-book-reader',
    '科技资讯': 'fa-microchip',
    '自然': 'fa-leaf'
  }
  return map[category] || 'fa-tag'
}

// 获取分类样式类
const getCategoryClass = (category) => {
  const map = {
    '科普知识': 'tag-blue',
    '教育': 'tag-indigo',
    '故事': 'tag-orange',
    '科技资讯': 'tag-cyan',
    '自然': 'tag-green'
  }
  return map[category] || 'tag-gray'
}

// 刷新缓存
const refreshCache = async () => {
  if (!currentUser.value?.id || refreshing.value) return
  refreshing.value = true
  try {
    await userBehaviorApi.refreshStatsCache(currentUser.value.id)
    await loadBrowsingStats()
    uni.showToast({ title: '刷新成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: '刷新失败', icon: 'none' })
  } finally {
    refreshing.value = false
  }
}

const showFilter = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const goBack = () => {
  uni.switchTab({ url: '/pages/parent/profile/profile' })
}

// 页面加载
const loadAllData = async () => {
  await loadCurrentUser()
  await loadBrowsingStats()
  await loadReadingRecords()
}

onMounted(loadAllData)
onShow(loadAllData)
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  width: 100%;
}

/* 导航 */
.nav-header {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 100;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  box-sizing: border-box;
}

.nav-content {
  display: flex;
  align-items: center;
}

.back-btn {
  padding: 20rpx;
  margin: -20rpx 10rpx -20rpx -20rpx;
  color: #374151;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #111827;
}

.main-content {
  margin-top: 88rpx;
  padding: 30rpx;
  height: calc(100vh - 88rpx);
  box-sizing: border-box;
}

/* 卡片通用 */
.card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
}

/* 统一按钮样式 */
.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background-color: #f3f4f6;
  border-radius: 30rpx;
  font-size: 24rpx;
  color: #4b5563;
  transition: all 0.2s;
}

.action-btn:active {
  background-color: #e5e7eb;
}

.icon-small {
  font-size: 20rpx;
}

.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 统计区域 */
.stats-grid {
  display: flex;
  align-items: stretch;
}

.stat-group {
  flex: 1;
}

.stat-group.primary {
  flex: 1.5;
  display: flex;
  gap: 24rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.stat-icon-bg {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.stat-icon-bg.blue {
  background-color: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.stat-icon-bg.green {
  background-color: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.stat-icon {
  font-size: 32rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f2937;
  display: block;
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: 22rpx;
  color: #6b7280;
}

.divider-v {
  width: 1px;
  background-color: #e5e7eb;
  margin: 0 30rpx;
}

.stat-group.secondary {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 24rpx;
}

.stat-mini-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-mini-label {
  font-size: 26rpx;
  color: #6b7280;
}

.stat-mini-value {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}

.unit {
  font-size: 22rpx;
  font-weight: normal;
  color: #9ca3af;
  margin-left: 4rpx;
}

/* 记录列表 */
.record-item {
  display: flex;
  padding: 24rpx 0;
  border-bottom: 1px solid #f3f4f6;
}

.record-item:last-child {
  border-bottom: none;
}

.record-cover {
  width: 180rpx;
  height: 135rpx;
  border-radius: 12rpx;
  background-color: #f3f4f6;
  flex-shrink: 0;
}

.record-content {
  flex: 1;
  margin-left: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 135rpx;
}

.record-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.meta-tag {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.tag-blue { background: #eff6ff; color: #3b82f6; }
.tag-indigo { background: #eef2ff; color: #6366f1; }
.tag-orange { background: #fff7ed; color: #f97316; }
.tag-cyan { background: #ecfeff; color: #06b6d4; }
.tag-green { background: #f0fdf4; color: #22c55e; }
.tag-gray { background: #f3f4f6; color: #6b7280; }

.meta-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.record-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.interaction-stats {
  display: flex;
  gap: 20rpx;
  margin-left: auto;
}

.stat-pill {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  color: #9ca3af;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}

.empty-icon-bg {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.empty-icon {
  font-size: 48rpx;
  color: #9ca3af;
}

.empty-text {
  font-size: 30rpx;
  color: #4b5563;
  margin-bottom: 8rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #9ca3af;
}
</style>
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">浏览历史</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 统计卡片 -->
      <view class="stats-card">
        <view class="stats-header">
          <text class="stats-title">浏览统计</text>
          <view class="refresh-btn" @click="refreshCache">
            <text class="fas fa-sync-alt" :class="{ 'rotating': refreshing }"></text>
            <text class="refresh-text">刷新</text>
          </view>
        </view>
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.monthlyDuration}}</text>
            <text class="stat-label">本月浏览(分钟)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.totalArticles}}</text>
            <text class="stat-label">浏览文章</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.interactedArticles}}</text>
            <text class="stat-label">互动文章</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.shareCount}}</text>
            <text class="stat-label">分享次数</text>
          </view>
        </view>
      </view>

      <!-- 历史记录列表 -->
      <view class="history-list">
        <view class="section-header">
          <text class="section-title">浏览记录</text>
          <view class="filter-btn">
            <text class="fas fa-filter"></text>
            <text class="filter-text">筛选</text>
          </view>
        </view>
        
        <view class="book-records">
          <!-- 有历史记录时显示 -->
          <view v-if="historyRecords.length > 0">
            <view v-for="(record, index) in historyRecords" :key="index" class="book-record-item">
              <image :src="record.coverUrl" class="book-cover"></image>
              <view class="book-info">
                <view class="book-main-info">
                  <text class="book-title">{{record.title}}</text>
                  <text class="book-author">{{record.author}}</text>
                  <view class="reading-time">
                    <text class="fas fa-clock time-icon"></text>
                    <text class="time-text">浏览{{record.duration}}分钟</text>
                  </view>
                </view>
                <view class="book-meta">
                  <view class="meta-item">
                    <text class="fas fa-tag meta-icon"></text>
                    <text class="meta-text">{{record.category}}</text>
                  </view>
                  <view class="meta-item">
                    <text class="fas fa-thumbs-up meta-icon"></text>
                    <text class="meta-text">{{record.likes}}赞</text>
                  </view>
                  <view class="meta-item">
                    <text class="fas fa-share-alt meta-icon"></text>
                    <text class="meta-text">{{record.shares}}分享</text>
                  </view>
                </view>
                <view class="action-buttons">
                  <view class="action-btn continue" @click="viewContent(record)">
                    <text class="fas fa-eye"></text>
                    <text>查看详情</text>
                  </view>
                  <view class="action-btn delete" @click="deleteHistoryRecord(record)">
                    <text class="fas fa-trash"></text>
                    <text>删除记录</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
          
          <!-- 无历史记录时显示 -->
          <view v-else class="empty-state">
            <view class="empty-icon">
              <text class="fas fa-history"></text>
            </view>
            <text class="empty-title">暂无浏览历史</text>
            <text class="empty-desc">去阅读一些精彩内容吧</text>
            <view class="empty-action" @click="goToDiscovery">
              <text class="action-text">去发现</text>
            </view>
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
const historyStats = ref({
  monthlyReadDuration: 0,
  totalArticleCount: 0,
  interactedArticleCount: 0,
  shareCount: 0
})
const historyRecords = ref([])
const loading = ref(false)
const refreshing = ref(false)

// 计算统计数据显示
const statsDisplay = computed(() => {
  return {
    monthlyDuration: Math.floor((historyStats.value.monthlyReadDuration || 0) / 60), // 转为分钟
    totalArticles: historyStats.value.totalArticleCount || 0,
    interactedArticles: historyStats.value.interactedArticleCount || 0,
    shareCount: historyStats.value.shareCount || 0
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

// 获取历史统计数据
const loadHistoryStats = async () => {
  try {
    if (!currentUser.value?.id) {
      console.log('用户ID不存在，跳过加载历史统计')
      return
    }
    
    console.log('开始获取历史统计数据，用户ID：', currentUser.value.id)
    const response = await userBehaviorApi.getHistoryStats(currentUser.value.id)
    
    console.log('历史统计API完整响应：', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      console.log('获取历史统计成功：', response.data)
      // 确保所有字段都有默认值
      historyStats.value = {
        monthlyReadDuration: response.data.monthlyReadDuration || 0,
        totalArticleCount: response.data.totalArticleCount || 0,
        interactedArticleCount: response.data.interactedArticleCount || 0,
        shareCount: response.data.shareCount || 0
      }
      console.log('更新后的historyStats：', historyStats.value)
    } else {
      console.warn('历史统计响应格式异常或无数据，使用默认值')
    }
  } catch (error) {
    console.error('获取历史统计失败：', error)
    // 不显示错误提示，使用默认值
  }
}

// 获取浏览历史记录
const loadHistoryRecords = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    console.log('开始获取浏览历史记录，用户ID：', currentUser.value.id)
    
    const response = await viewHistoryApi.getUserViewHistory(currentUser.value.id, {
      current: 1,
      size: 20
    })
    
    if (response && response.data && response.data.records) {
      console.log('获取浏览历史成功，共', response.data.records.length, '条')
      
      // 转换数据格式并过滤无效数据
      const validRecords = response.data.records.filter(record => {
        // 过滤条件：必须有内容ID和标题
        const isValid = record.contentId && 
                       record.contentTitle && 
                       record.contentTitle.trim() !== '' &&
                       record.contentTitle !== '无标题'
        
        if (!isValid) {
          console.log('过滤掉无效历史记录：', record)
        }
        
        return isValid
      })
      
      console.log('有效历史记录数量：', validRecords.length, '，原始数量：', response.data.records.length)
      
      historyRecords.value = validRecords.map(record => {
        console.log('处理有效历史记录：', record)
        return {
          id: record.id,
          title: record.contentTitle,
          author: record.creatorName || '佚名',
          coverUrl: record.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
          duration: record.duration || 0, // 使用真实的阅读时长
          lastRead: formatViewTime(record.viewTime),
          category: record.categoryName || '未分类',
          likes: record.likeCount || 0,
          shares: record.shareCount || 0, // 使用真实的分享数
          contentId: record.contentId,
          contentType: record.contentType
        }
      })
      
      console.log('转换后的历史记录：', historyRecords.value)
    }
  } catch (error) {
    console.error('获取浏览历史失败：', error)
    uni.showToast({
      title: '获取历史失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 格式化浏览时间
const formatViewTime = (viewTime) => {
  if (!viewTime) return '未知时间'
  
  const now = new Date()
  const view = new Date(viewTime)
  const diffMs = now - view
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  
  if (diffDays === 0) {
    if (diffHours === 0) {
      return diffMinutes < 1 ? '刚刚' : `${diffMinutes}分钟前`
    }
    return `${diffHours}小时前`
  } else if (diffDays === 1) {
    return '昨天'
  } else if (diffDays === 2) {
    return '前天'
  } else {
    return `${diffDays}天前`
  }
}

// 删除浏览记录
const deleteHistoryRecord = async (record) => {
  try {
    console.log('删除浏览记录：', record.title)
    
    // 调用删除API（假设存在批量删除接口）
    const response = await viewHistoryApi.batchDeleteViewHistory(currentUser.value.id, [record.id])
    
    if (response && response.code === 200) {
      // 从本地列表中移除
      historyRecords.value = historyRecords.value.filter(item => item.id !== record.id)
      uni.showToast({
        title: '删除成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('删除记录失败：', error)
    uni.showToast({
      title: '删除失败',
      icon: 'none'
    })
  }
}

// 查看内容详情
const viewContent = (record) => {
  console.log('查看内容：', record.title)
  
  if (record.contentType === 1) {
    // 图文内容
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${record.contentId}`
    })
  } else if (record.contentType === 2) {
    // 视频内容
    uni.navigateTo({
      url: `/pages/parent/video/video-player?id=${record.contentId}`
    })
  }
}

// 刷新缓存
const refreshCache = async () => {
  if (!currentUser.value?.id || refreshing.value) return
  
  refreshing.value = true
  console.log('开始刷新统计缓存')
  
  try {
    const response = await userBehaviorApi.refreshStatsCache(currentUser.value.id)
    
    if (response && response.code === 200) {
      console.log('缓存刷新成功，重新加载统计数据')
      
      // 重新加载统计数据
      await loadHistoryStats()
      
      uni.showToast({
        title: '数据已刷新',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('刷新缓存失败：', error)
    uni.showToast({
      title: '刷新失败',
      icon: 'none'
    })
  } finally {
    refreshing.value = false
  }
}

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 去发现页面
const goToDiscovery = () => {
  console.log('跳转到发现页面')
  uni.switchTab({
    url: '/pages/parent/home/home',
    success: () => {
      console.log('跳转到发现页面成功')
    },
    fail: (err) => {
      console.error('跳转到发现页面失败:', err)
      uni.showToast({
        title: '跳转失败',
        icon: 'none'
      })
    }
  })
}

// 加载所有数据的统一方法
const loadAllData = async () => {
  console.log('[浏览历史] 加载所有数据')
  await loadCurrentUser()
  await loadHistoryStats()
  await loadHistoryRecords()
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('[浏览历史] 页面已挂载，开始加载数据')
  await loadAllData()
})

// 页面显示时刷新数据
onShow(async () => {
  console.log('[浏览历史] 页面显示，刷新数据')
  await loadAllData()
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

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.stats-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  transition: all 0.3s ease;
}

.refresh-btn:active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.95);
}

.refresh-btn .fas {
  font-size: 24rpx;
  color: #ffffff;
}

.refresh-btn .rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.refresh-text {
  font-size: 24rpx;
  color: #ffffff;
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

/* 历史记录列表样式 */
.history-list {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #6b7280;
  font-size: 28rpx;
}

.book-records {
  width: 100%;
}

.book-record-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1px solid #e5e7eb;
}

.book-record-item:last-child {
  border-bottom: none;
}

.book-cover {
  width: 160rpx;
  height: 220rpx;
  border-radius: 12rpx;
  object-fit: cover;
}

.book-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.book-title {
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.book-author {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 12rpx;
}

.reading-time {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.time-icon {
  color: #3b82f6;
  font-size: 24rpx;
}

.time-text {
  font-size: 24rpx;
  color: #3b82f6;
}

.book-meta {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
  background-color: #ffffff;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  color: #60a5fa;
  font-size: 24rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.action-btn.continue {
  background-color: #3b82f6;
  color: #ffffff;
}

.action-btn.delete {
  background-color: #ef4444;
  color: #ffffff;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
  text-align: center;
}

.empty-icon {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
}

.empty-icon .fas {
  font-size: 48rpx;
  color: #9ca3af;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 40rpx;
  line-height: 1.5;
}

.empty-action {
  padding: 20rpx 40rpx;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 24rpx;
  transition: all 0.3s ease;
}

.empty-action:active {
  transform: scale(0.95);
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}

.action-text {
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 500;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style>

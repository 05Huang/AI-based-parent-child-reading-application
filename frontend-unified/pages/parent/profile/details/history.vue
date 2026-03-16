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
      <view class="card stats-card">
        <view class="card-header">
          <view class="header-title-group">
            <view class="title-bar blue"></view>
            <text class="card-title">浏览统计</text>
          </view>
          <view class="action-btn" @click="refreshCache">
            <text class="fas fa-sync-alt" :class="{ 'rotating': refreshing }"></text>
            <text class="btn-text">刷新</text>
          </view>
        </view>
        
        <view class="stats-grid">
          <!-- 基础数据 (时间/数量) -->
          <view class="stat-group primary">
            <view class="stat-item">
              <view class="stat-icon-bg blue">
                <text class="fas fa-clock stat-icon"></text>
              </view>
              <view class="stat-info">
                <text class="stat-value">{{statsDisplay.monthlyDuration}}</text>
                <text class="stat-label">本月浏览(分钟)</text>
              </view>
            </view>
            <view class="stat-item">
              <view class="stat-icon-bg cyan">
                <text class="fas fa-book-open stat-icon"></text>
              </view>
              <view class="stat-info">
                <text class="stat-value">{{statsDisplay.totalArticles}}</text>
                <text class="stat-label">浏览文章(篇)</text>
              </view>
            </view>
          </view>
          
          <view class="divider-v"></view>
          
          <!-- 互动数据 -->
          <view class="stat-group secondary">
            <view class="stat-mini-item">
              <text class="stat-mini-label">互动文章</text>
              <view class="stat-mini-data">
                <text class="fas fa-thumbs-up mini-icon text-orange"></text>
                <text class="stat-mini-value">{{statsDisplay.interactedArticles}}</text>
              </view>
            </view>
            <view class="stat-mini-item">
              <text class="stat-mini-label">分享次数</text>
              <view class="stat-mini-data">
                <text class="fas fa-share-alt mini-icon text-purple"></text>
                <text class="stat-mini-value">{{statsDisplay.shareCount}}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 历史记录列表 -->
      <view class="card list-card">
        <view class="card-header">
          <view class="header-title-group">
            <view class="title-bar orange"></view>
            <text class="card-title">浏览记录</text>
          </view>
          <view class="action-btn" @click="currentCategory ? toggleCategoryFilter(null) : showFilter()">
            <text class="fas" :class="currentCategory ? 'fa-times' : 'fa-filter'"></text>
            <text class="btn-text">{{ currentCategory ? '清除筛选' : '筛选' }}</text>
          </view>
        </view>
        
        <view class="book-records">
          <!-- 筛选状态提示 -->
          <view v-if="currentCategory" class="filter-status-bar">
            <text class="filter-text">正在筛选：{{ currentCategory }}</text>
            <view class="clear-filter-btn" @click="toggleCategoryFilter(null)">
              <text class="fas fa-times"></text>
            </view>
          </view>

          <!-- 有历史记录时显示 -->
          <view v-if="filteredRecords.length > 0" class="records-list">
            <view v-for="(record, index) in filteredRecords" :key="index" class="record-item" @click="viewContent(record)">
              <view class="record-image-box">
                <image :src="record.coverUrl" class="record-image" mode="aspectFill"></image>
                <view class="duration-tag">
                  <text class="fas fa-clock tag-icon"></text>
                  <text class="tag-text">{{ formatDurationDisplay(record.duration) }}</text>
                </view>
              </view>
              
              <view class="record-content">
                <view class="record-header">
                  <text class="record-title">{{record.title}}</text>
                </view>
                
                <view class="record-meta-row">
                  <view class="meta-left">
                    <text class="time-display">{{ record.lastRead }}</text>
                    <text class="meta-separator" v-if="record.author && record.author !== '佚名'">·</text>
                    <text class="author-display" v-if="record.author && record.author !== '佚名'">{{ record.author }}</text>
                  </view>
                </view>
                
                <view class="record-footer">
                  <view class="category-tag" 
                        :class="[getCategoryClass(record.category), { 'active-tag': currentCategory === record.category }]"
                        @click.stop="toggleCategoryFilter(record.category)">
                    <text class="fas" :class="getCategoryIcon(record.category)"></text>
                    <text>{{ record.category }}</text>
                  </view>
                  
                  <view class="action-buttons">
                    <view class="action-mini-btn delete" @click.stop="deleteHistoryRecord(record)">
                      <text class="fas fa-trash-alt"></text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
          
          <!-- 无历史记录时显示 -->
          <view v-else class="empty-state">
            <view class="empty-icon-bg">
              <text class="fas fa-history empty-icon"></text>
            </view>
            <text class="empty-title">{{ currentCategory ? '该分类下暂无记录' : '暂无浏览历史' }}</text>
            <text class="empty-desc">{{ currentCategory ? '尝试切换其他分类' : '去发现更多精彩内容吧' }}</text>
            <view class="empty-action-btn" @click="currentCategory ? toggleCategoryFilter(null) : goToDiscovery()">
              <text class="fas" :class="currentCategory ? 'fa-sync-alt' : 'fa-compass'"></text>
              <text class="action-text">{{ currentCategory ? '查看全部' : '去发现' }}</text>
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
const currentCategory = ref(null)
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

// 计算过滤后的记录
const filteredRecords = computed(() => {
  if (!currentCategory.value) return historyRecords.value
  return historyRecords.value.filter(record => record.category === currentCategory.value)
})

// 切换分类筛选
const toggleCategoryFilter = (category) => {
  if (currentCategory.value === category) {
    currentCategory.value = null
    uni.showToast({ title: '已取消筛选', icon: 'none' })
  } else {
    currentCategory.value = category
    if (category) {
      uni.showToast({ title: `已筛选: ${category}`, icon: 'none' })
    }
  }
}

// 获取分类图标
const getCategoryIcon = (category) => {
  const map = {
    '科普知识': 'fa-atom',
    '教育': 'fa-graduation-cap',
    '故事': 'fa-book-reader',
    '科技资讯': 'fa-microchip',
    '自然': 'fa-leaf',
    '在线学习': 'fa-laptop-code',
    '美食': 'fa-utensils'
  }
  return map[category] || 'fa-tag'
}

// 获取分类样式
const getCategoryClass = (category) => {
  const map = {
    '科普知识': 'tag-blue',
    '教育': 'tag-indigo',
    '故事': 'tag-orange',
    '科技资讯': 'tag-cyan',
    '自然': 'tag-green',
    '在线学习': 'tag-purple',
    '美食': 'tag-red'
  }
  return map[category] || 'tag-gray'
}

// 格式化时长显示
const formatDurationDisplay = (minutes) => {
  if (!minutes || minutes <= 0) return '浏览 < 1分钟'
  if (minutes < 60) return `浏览 ${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `浏览 ${hours}小时${mins > 0 ? mins + '分' : ''}`
}

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

// 获取历史统计数据
const loadHistoryStats = async () => {
  try {
    if (!currentUser.value?.id) return
    
    const response = await userBehaviorApi.getHistoryStats(currentUser.value.id)
    
    if (response && response.code === 200 && response.data) {
      historyStats.value = {
        monthlyReadDuration: response.data.monthlyReadDuration || 0,
        totalArticleCount: response.data.totalArticleCount || 0,
        interactedArticleCount: response.data.interactedArticleCount || 0,
        shareCount: response.data.shareCount || 0
      }
    }
  } catch (error) {
    console.error('获取历史统计失败：', error)
  }
}

// 获取浏览历史记录
const loadHistoryRecords = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    
    const response = await viewHistoryApi.getUserViewHistory(currentUser.value.id, {
      current: 1,
      size: 50
    })
    
    if (response && response.data && response.data.records) {
      // 转换数据格式并过滤无效数据
      const validRecords = response.data.records.filter(record => {
        return record.contentId && 
               record.contentTitle && 
               record.contentTitle.trim() !== '' &&
               record.contentTitle !== '无标题'
      })
      
      historyRecords.value = validRecords.map(record => {
        return {
          id: record.id,
          title: record.contentTitle,
          author: record.creatorName || '佚名',
          coverUrl: record.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
          duration: record.duration || 0, // 使用真实的阅读时长
          lastRead: formatViewTime(record.viewTime),
          category: record.categoryName || '未分类',
          likes: record.likeCount || 0,
          shares: record.shareCount || 0,
          contentId: record.contentId,
          contentType: record.contentType
        }
      })
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
  if (!viewTime) return ''
  
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
    const month = String(view.getMonth() + 1).padStart(2, '0')
    const day = String(view.getDate()).padStart(2, '0')
    return `${month}-${day}`
  }
}

// 删除浏览记录
const deleteHistoryRecord = async (record) => {
  uni.showModal({
    title: '删除记录',
    content: '确定要删除这条浏览记录吗？',
    confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          const response = await viewHistoryApi.batchDeleteViewHistory(currentUser.value.id, [record.id])
          
          if (response && response.code === 200) {
            historyRecords.value = historyRecords.value.filter(item => item.id !== record.id)
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
            await loadHistoryStats()
          }
        } catch (error) {
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 查看内容详情
const viewContent = (record) => {
  if (record.contentType === 1) {
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${record.contentId}`
    })
  } else if (record.contentType === 2) {
    uni.navigateTo({
      url: `/pages/parent/video/video-player?id=${record.contentId}`
    })
  } else {
    // 默认跳转
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${record.contentId}`
    })
  }
}

// 刷新缓存
const refreshCache = async () => {
  if (!currentUser.value?.id || refreshing.value) return
  
  refreshing.value = true
  
  try {
    const response = await userBehaviorApi.refreshStatsCache(currentUser.value.id)
    if (response && response.code === 200) {
      await loadAllData()
      uni.showToast({
        title: '数据已更新',
        icon: 'success'
      })
    }
  } catch (error) {
    // 静默失败
  } finally {
    refreshing.value = false
  }
}

// 显示筛选提示
const showFilter = () => {
  uni.showToast({
    title: '点击下方列表中的标签即可筛选',
    icon: 'none'
  })
}

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 去发现页面
const goToDiscovery = () => {
  uni.switchTab({
    url: '/pages/parent/home/home'
  })
}

// 加载所有数据的统一方法
const loadAllData = async () => {
  await loadCurrentUser()
  await loadHistoryStats()
  await loadHistoryRecords()
}

onMounted(async () => {
  await loadAllData()
})

onShow(async () => {
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
  padding-top: 100rpx;
  padding-bottom: 40rpx;
  box-sizing: border-box;
}

/* 顶部导航 */
.nav-header {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 100rpx;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.nav-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.back-btn {
  padding: 20rpx;
  margin: -20rpx;
  margin-right: 10rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #111827;
}

/* 主内容区域 */
.main-content {
  padding: 30rpx;
  box-sizing: border-box;
}

/* 卡片通用样式 */
.card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(229, 231, 235, 0.5);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.header-title-group {
  display: flex;
  align-items: center;
}

.title-bar {
  width: 8rpx;
  height: 32rpx;
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.title-bar.blue { background-color: #3b82f6; }
.title-bar.orange { background-color: #f59e0b; }

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
}

/* 按钮样式 */
.action-btn {
  display: flex;
  align-items: center;
  background-color: #f3f4f6;
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  transition: all 0.3s;
}

.action-btn:active {
  background-color: #e5e7eb;
}

.action-btn .fas {
  font-size: 24rpx;
  color: #6b7280;
  margin-right: 8rpx;
}

.btn-text {
  font-size: 24rpx;
  color: #4b5563;
  font-weight: 500;
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
  align-items: center;
  justify-content: space-between;
}

.stat-group {
  flex: 1;
  display: flex;
  justify-content: space-around;
}

.stat-group.primary {
  flex: 1.2;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
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

.stat-icon-bg.blue { background-color: #eff6ff; }
.stat-icon-bg.cyan { background-color: #ecfeff; }

.stat-icon {
  font-size: 36rpx;
}

.stat-icon-bg.blue .stat-icon { color: #3b82f6; }
.stat-icon-bg.cyan .stat-icon { color: #06b6d4; }

.stat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
}

.stat-label {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

.divider-v {
  width: 2rpx;
  height: 80rpx;
  background-color: #e5e7eb;
  margin: 0 20rpx;
}

.stat-mini-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-mini-label {
  font-size: 22rpx;
  color: #9ca3af;
  margin-bottom: 8rpx;
}

.stat-mini-data {
  display: flex;
  align-items: center;
}

.mini-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.text-orange { color: #f59e0b; }
.text-purple { color: #8b5cf6; }

.stat-mini-value {
  font-size: 32rpx;
  font-weight: 600;
  color: #374151;
}

/* 列表区域 */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.record-item {
  display: flex;
  background-color: #ffffff;
  padding-bottom: 30rpx;
  border-bottom: 1px solid #f3f4f6;
}

.record-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.record-image-box {
  position: relative;
  width: 220rpx;
  height: 160rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-right: 24rpx;
  flex-shrink: 0;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.1);
  background-color: #e5e7eb;
}

.record-image {
  width: 100%;
  height: 100%;
}

.duration-tag {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  background-color: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
}

.duration-tag .tag-icon {
  font-size: 18rpx;
  color: #ffffff;
  margin-right: 6rpx;
}

.duration-tag .tag-text {
  font-size: 18rpx;
  color: #ffffff;
  font-weight: 500;
}

.record-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
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
  text-overflow: ellipsis;
}

.record-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: auto;
}

.meta-left {
  display: flex;
  align-items: center;
  font-size: 22rpx;
  color: #9ca3af;
}

.meta-separator {
  margin: 0 8rpx;
}

.record-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}

.category-tag {
  display: flex;
  align-items: center;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.category-tag .fas {
  margin-right: 6rpx;
  font-size: 20rpx;
}

.tag-blue { background-color: #eff6ff; color: #3b82f6; }
.tag-indigo { background-color: #e0e7ff; color: #6366f1; }
.tag-orange { background-color: #fff7ed; color: #f97316; }
.tag-cyan { background-color: #ecfeff; color: #06b6d4; }
.tag-green { background-color: #ecfdf5; color: #10b981; }
.tag-purple { background-color: #f3e8ff; color: #a855f7; }
.tag-red { background-color: #fef2f2; color: #ef4444; }
.tag-gray { background-color: #f3f4f6; color: #6b7280; }

.action-buttons {
  display: flex;
  align-items: center;
}

.action-mini-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  color: #6b7280;
  transition: all 0.2s;
}

.action-mini-btn:active {
  background-color: #e5e7eb;
}

.action-mini-btn.delete {
  color: #9ca3af;
}

.action-mini-btn.delete:active {
  background-color: #fee2e2;
  color: #ef4444;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
}

.empty-icon-bg {
  width: 120rpx;
  height: 120rpx;
  background-color: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.empty-icon {
  font-size: 48rpx;
  color: #9ca3af;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #9ca3af;
  margin-bottom: 30rpx;
}

.empty-action-btn {
  display: flex;
  align-items: center;
  background-color: #3b82f6;
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
}

.empty-action-btn .fas {
  color: #ffffff;
  font-size: 28rpx;
  margin-right: 12rpx;
}

.empty-action-btn .action-text {
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 500;
}

/* 筛选状态栏 */
.filter-status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #eff6ff;
  padding: 16rpx 24rpx;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
  border: 1px dashed #bfdbfe;
}

.filter-text {
  font-size: 26rpx;
  color: #3b82f6;
  font-weight: 500;
}

.clear-filter-btn {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background-color: #dbeafe;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
}

.active-tag {
  background-color: #2563eb !important;
  color: #ffffff !important;
  box-shadow: 0 2rpx 8rpx rgba(37, 99, 235, 0.3);
}
</style>
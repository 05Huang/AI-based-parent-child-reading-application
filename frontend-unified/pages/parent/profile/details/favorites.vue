<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">我的收藏</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 收藏统计 -->
      <view class="card stats-card">
        <view class="card-header">
          <view class="header-title-group">
            <view class="title-bar"></view>
            <text class="card-title">收藏统计</text>
          </view>
          <view class="action-btn" @click="refreshCache">
            <text class="fas fa-sync-alt" :class="{ 'rotating': refreshing }"></text>
            <text class="btn-text">刷新</text>
          </view>
        </view>
        
        <view class="stats-grid">
          <!-- 核心指标 -->
          <view class="stat-group primary">
            <view class="stat-item">
              <view class="stat-icon-bg blue">
                <text class="fas fa-heart stat-icon"></text>
              </view>
              <view class="stat-info">
                <text class="stat-value">{{statsDisplay.totalCollections}}</text>
                <text class="stat-label">收藏总数</text>
              </view>
            </view>
            <view class="stat-item">
              <view class="stat-icon-bg green">
                <text class="fas fa-calendar-check stat-icon"></text>
              </view>
              <view class="stat-info">
                <text class="stat-value">{{statsDisplay.monthlyCollections}}</text>
                <text class="stat-label">本月收藏</text>
              </view>
            </view>
          </view>
          
          <view class="divider-v"></view>
          
          <!-- 行为指标 -->
          <view class="stat-group secondary">
            <view class="stat-mini-item">
              <text class="stat-mini-label">已分享</text>
              <view class="stat-mini-data">
                <text class="fas fa-share-alt mini-icon text-orange"></text>
                <text class="stat-mini-value">{{statsDisplay.collectionShares}}</text>
              </view>
            </view>
            <view class="stat-mini-item">
              <text class="stat-mini-label">互动数</text>
              <view class="stat-mini-data">
                <text class="fas fa-comment-dots mini-icon text-purple"></text>
                <text class="stat-mini-value">{{statsDisplay.interactionCount}}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 收藏列表 -->
      <view class="card list-card">
        <view class="card-header">
          <view class="header-title-group">
            <view class="title-bar orange"></view>
            <text class="card-title">收藏内容</text>
          </view>
          <text class="header-subtitle">共 {{ articles.length }} 条内容</text>
        </view>

        <view v-if="articles.length > 0" class="favorites-list">
          <view v-for="(article, index) in articles" :key="index" class="record-item" @click="viewArticle(article)">
            <view class="record-image-box">
              <image :src="article.coverUrl" class="record-image" mode="aspectFill"></image>
              <view class="type-tag" :class="getContentTypeClass(article.contentType)">
                <text class="fas" :class="getContentTypeIcon(article.contentType)"></text>
                <text class="type-text">{{ getContentTypeText(article.contentType) }}</text>
              </view>
            </view>
            
            <view class="record-content">
              <view class="record-header">
                <text class="record-title">{{ article.title }}</text>
              </view>
              
              <view class="record-meta">
                <view class="meta-left">
                  <text class="time-display">{{ article.timeDisplay }}</text>
                </view>
              </view>
              
              <view class="record-actions">
                <view class="action-mini-btn" @click.stop="shareArticle(article)">
                  <text class="fas fa-share-alt"></text>
                  <text>分享</text>
                </view>
                <view class="action-mini-btn delete" @click.stop="deleteArticle(article)">
                  <text class="fas fa-trash-alt"></text>
                  <text>取消收藏</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 无收藏内容时显示 -->
        <view v-else class="empty-state">
          <view class="empty-icon-bg">
            <text class="fas fa-folder-open empty-icon"></text>
          </view>
          <text class="empty-title">暂无收藏内容</text>
          <text class="empty-desc">去发现更多精彩内容吧</text>
          <view class="empty-action-btn" @click="goToDiscovery">
            <text class="fas fa-compass"></text>
            <text class="action-text">去发现</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { favoriteApi, userBehaviorApi, userApi } from '@/utils/api.js'

// 响应式状态
const currentUser = ref(null)
const collectionStats = ref({
  totalCollections: 0,
  monthlyCollections: 0,
  collectionShares: 0,
  interactionCount: 0
})
const articles = ref([])
const loading = ref(false)
const refreshing = ref(false)

// 计算统计数据显示
const statsDisplay = computed(() => {
  return {
    totalCollections: collectionStats.value.totalCollections || 0,
    monthlyCollections: collectionStats.value.monthlyCollections || 0,
    collectionShares: collectionStats.value.collectionShares || 0,
    interactionCount: collectionStats.value.interactionCount || 0
  }
})

// 格式化时间显示 (刚刚, X分钟前, MM-DD)
const formatTimeDisplay = (timeStr) => {
  if (!timeStr) return ''
  
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  // 1小时内
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
  }
  
  // 24小时内
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  
  // 超过24小时显示日期
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

// 获取内容类型文本
const getContentTypeText = (type) => {
  const map = {
    1: '图文',
    2: '视频',
    3: '音频'
  }
  return map[type] || '图文'
}

// 获取内容类型图标
const getContentTypeIcon = (type) => {
  const map = {
    1: 'fa-file-alt',
    2: 'fa-play-circle',
    3: 'fa-headphones'
  }
  return map[type] || 'fa-file-alt'
}

// 获取内容类型样式类
const getContentTypeClass = (type) => {
  const map = {
    1: 'type-blue',
    2: 'type-orange',
    3: 'type-purple'
  }
  return map[type] || 'type-blue'
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

// 获取收藏统计数据
const loadCollectionStats = async () => {
  try {
    if (!currentUser.value?.id) return
    
    const response = await userBehaviorApi.getCollectionStats(currentUser.value.id)
    
    if (response && response.code === 200 && response.data) {
      collectionStats.value = {
        totalCollections: response.data.totalCollections || 0,
        monthlyCollections: response.data.monthlyCollections || 0,
        collectionShares: response.data.collectionShares || 0,
        interactionCount: response.data.interactionCount || 0
      }
    }
  } catch (error) {
    console.error('获取收藏统计失败：', error)
  }
}

// 获取收藏列表
const loadFavorites = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    
    const queryParams = {
      userId: currentUser.value.id,
      current: 1,
      size: 50
    }
    
    const response = await favoriteApi.getUserFavorites(queryParams)
    
    if (response && response.data && response.data.records) {
      // 转换数据格式并过滤无效数据
      const validArticles = response.data.records.filter(item => {
        return item.contentId && 
               item.contentTitle && 
               item.contentTitle.trim() !== '' &&
               item.contentTitle !== '无标题'
      })
      
      articles.value = validArticles.map((item) => {
        return {
          id: item.id,
          title: item.contentTitle,
          coverUrl: item.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
          contentId: item.contentId,
          contentType: item.contentType || 1, // 默认为图文内容
          favoriteId: item.id,
          timeDisplay: formatTimeDisplay(item.createdTime),
          createdTime: item.createdTime
        }
      })
    }
  } catch (error) {
    console.error('获取收藏列表失败：', error)
    uni.showToast({
      title: '获取收藏失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack({
    delta: 1,
    fail: () => {
      uni.switchTab({
        url: '/pages/parent/profile/profile'
      })
    }
  })
}

// 去发现页面
const goToDiscovery = () => {
  uni.switchTab({
    url: '/pages/parent/home/home'
  })
}

// 查看文章
const viewArticle = (article) => {
  if (!article.contentId) return
  
  const contentType = article.contentType || 1
  
  if (contentType === 1) {
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${article.contentId}`
    })
  } else if (contentType === 2) {
    uni.navigateTo({
      url: `/pages/parent/video/video-player?id=${article.contentId}`
    })
  } else {
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${article.contentId}`
    })
  }
}

// 分享文章
const shareArticle = (article) => {
  const shareTitle = article.title || '精彩文章'
  
  // #ifdef APP-PLUS
  uni.share({
    provider: 'system',
    type: 1,
    title: shareTitle,
    summary: `来自阅桥亲子阅读APP：${shareTitle}`,
    href: `https://parentreading.com/article/${article.contentId}`,
    success: () => {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: () => {
      // 降级处理
      uni.setClipboardData({
        data: `https://parentreading.com/article/${article.contentId}`,
        success: () => {
          uni.showToast({
            title: '链接已复制',
            icon: 'none'
          })
        }
      })
    }
  })
  // #endif
  
  // #ifndef APP-PLUS
  uni.setClipboardData({
    data: `推荐阅读：《${shareTitle}》`,
    success: () => {
      uni.showToast({
        title: '内容已复制',
        icon: 'none'
      })
    }
  })
  // #endif
}

// 取消收藏
const deleteArticle = async (article) => {
  if (!currentUser.value?.id) return
  
  uni.showModal({
    title: '确认删除',
    content: `确定要取消收藏《${article.title}》吗？`,
    confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          const response = await favoriteApi.deleteFavorite(currentUser.value.id, article.contentId)
          
          if (response && response.code === 200) {
            articles.value = articles.value.filter(item => item.id !== article.id)
            uni.showToast({
              title: '已取消收藏',
              icon: 'success'
            })
            await loadCollectionStats()
          } else {
            uni.showToast({
              title: response?.message || '操作失败',
              icon: 'none'
            })
          }
        } catch (error) {
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
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

// 加载所有数据的统一方法
const loadAllData = async () => {
  await loadCurrentUser()
  await loadCollectionStats()
  await loadFavorites()
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
  padding-top: 100rpx; /* 预留导航栏高度 */
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
  background-color: #3b82f6;
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.title-bar.orange {
  background-color: #f59e0b;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
}

.header-subtitle {
  font-size: 24rpx;
  color: #9ca3af;
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

.stat-icon-bg.blue {
  background-color: #eff6ff;
}

.stat-icon-bg.green {
  background-color: #ecfdf5;
}

.stat-icon {
  font-size: 36rpx;
}

.stat-icon-bg.blue .stat-icon {
  color: #3b82f6;
}

.stat-icon-bg.green .stat-icon {
  color: #10b981;
}

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
.favorites-list {
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
}

.record-image {
  width: 100%;
  height: 100%;
  background-color: #e5e7eb;
}

.type-tag {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background-color: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  z-index: 1;
}

.type-tag .fas {
  font-size: 18rpx;
  color: #ffffff;
  margin-right: 6rpx;
}

.type-tag .type-text {
  font-size: 18rpx;
  color: #ffffff;
  font-weight: 500;
}

.type-blue { background-color: rgba(59, 130, 246, 0.9); }
.type-orange { background-color: rgba(245, 158, 11, 0.9); }
.type-purple { background-color: rgba(139, 92, 246, 0.9); }

.record-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0; /* 防止flex子项溢出 */
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

.record-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.time-display {
  font-size: 22rpx;
  color: #9ca3af;
}

.record-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.action-mini-btn {
  display: flex;
  align-items: center;
  padding: 8rpx 16rpx;
  background-color: #f3f4f6;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: #4b5563;
  transition: all 0.2s;
}

.action-mini-btn .fas {
  margin-right: 6rpx;
  font-size: 22rpx;
}

.action-mini-btn.delete {
  background-color: #fef2f2;
  color: #ef4444;
}

.action-mini-btn:active {
  opacity: 0.8;
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
</style>
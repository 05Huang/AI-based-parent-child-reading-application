<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
        <view class="back-button" @tap="goBack">
        <text class="fas fa-arrow-left back-icon"></text>
      </view>
      <text class="page-title">我的收藏</text>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content">
      <view class="content-wrapper">
        <!-- 统计栏 (简化版) -->
        <view class="stats-bar">
          <view class="stat-item">
            <text class="stat-num">{{ totalFavorites }}</text>
            <text class="stat-desc">内容收藏</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-num">{{ monthlyFavorites }}</text>
            <text class="stat-desc">本月新增</text>
          </view>
        </view>

        <!-- 分类标签 (功能化) -->
        <view class="tabs-section">
          <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
            <view class="tabs-wrapper">
              <view 
                v-for="(tab, index) in tabs" 
                :key="index"
                :class="['tab-item', currentTab === index ? 'active' : '']"
                @tap="switchTab(index)"
              >
                <text class="tab-text">{{tab}}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- 收藏列表 (列表布局) -->
        <view class="collection-list">
          <!-- 加载状态 -->
          <view v-if="loading" class="state-view">
            <view class="spinner"></view>
            <text class="state-text">加载中...</text>
          </view>
          
          <!-- 空状态 -->
          <view v-else-if="filteredFavorites.length === 0" class="state-view empty">
            <view class="empty-icon">
              <text class="fas fa-heart-broken"></text>
            </view>
            <text class="empty-title">还没有收藏哦</text>
            <text class="empty-hint">快去找找喜欢的内容吧~</text>
          </view>
          
          <!-- 列表容器 -->
          <view v-else class="list-container">
            <view 
              class="list-item" 
              v-for="(item, index) in filteredFavorites" 
              :key="index" 
              @tap="goToArticle(item.id)"
            >
              <view class="item-cover-wrapper">
                <image :src="item.cover" class="item-cover" mode="aspectFill"></image>
                <view class="type-badge" v-if="item.type === 'video'">
                  <text class="fas fa-play"></text>
                </view>
              </view>
              
              <view class="item-content">
                <text class="item-title">{{item.title}}</text>
                <view class="item-desc" v-if="item.summary">{{ item.summary }}</view>
                
                <view class="item-footer">
                  <view class="meta-left">
                    <text class="meta-text author">{{item.author}}</text>
                    <text class="meta-dot">·</text>
                    <text class="meta-text">{{item.favoriteDate}}</text>
                  </view>
                  
                  <view class="status-tag">
                    <text class="fas fa-bookmark"></text>
                    <text>已收藏</text>
                  </view>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { favoriteApi, userApi, contentApi } from '@/utils/api.js'
import eventBus, { EVENTS } from '@/utils/eventBus.js'

const currentTab = ref(0)
const tabs = ref(['全部', '文章', '视频'])

// 实时计算的收藏统计数据
const totalFavorites = ref(0)
const monthlyFavorites = ref(0)

// 收藏列表
const favorites = ref([])
const loading = ref(false)
const currentUser = ref(null)

// 过滤后的列表
const filteredFavorites = computed(() => {
  if (currentTab.value === 0) return favorites.value
  const type = currentTab.value === 1 ? 'article' : 'video'
  return favorites.value.filter(item => item.type === type)
})

// 返回上一页
const goBack = () => {
  uni.navigateBack({
    delta: 1
  })
}

// 切换标签
const switchTab = (index) => {
  currentTab.value = index
}

// 跳转到文章详情
const goToArticle = (contentId) => {
  if (!contentId) return
  uni.navigateTo({
    url: `/pages/child/article-detail?contentId=${contentId}`
  })
}

// 格式化时间
const formatFavoriteDate = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    const now = new Date()
    const diffTime = Math.abs(now - date)
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    
    if (diffDays === 0) return '今天'
    if (diffDays === 1) return '昨天'
    if (diffDays < 7) return `${diffDays}天前`
    if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`
    if (diffDays < 365) return `${Math.floor(diffDays / 30)}个月前`
    return `${Math.floor(diffDays / 365)}年前`
  } catch (error) {
    return ''
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo) {
      // uni.getStorageSync 可能返回对象或字符串，需要判断
      if (typeof userInfo === 'string') {
        currentUser.value = JSON.parse(userInfo)
      } else {
        currentUser.value = userInfo
      }
      console.log('[儿童端收藏页] 用户信息：', currentUser.value)
    }
  } catch (error) {
    console.error('[儿童端收藏页] 加载用户信息失败：', error)
  }
}

// 加载收藏列表
const loadFavorites = async () => {
  if (!currentUser.value || !currentUser.value.id) {
    console.log('[儿童端收藏页] 用户未登录')
    return
  }
  
  try {
    loading.value = true
    console.log('[儿童端收藏页] 开始加载收藏列表，用户ID：', currentUser.value.id)
    
    const response = await favoriteApi.getUserFavorites({
      userId: currentUser.value.id,
      current: 1,
      size: 100
    })
    
    if (response && response.data && response.data.records) {
      // 并行获取内容详情以补充 type 和 summary
      const records = response.data.records
      const detailedFavorites = await Promise.all(records.map(async (item) => {
        let type = 'article' // 默认为文章
        let summary = ''
        let cover = item.coverUrl
        
        try {
          // 获取内容详情
          const detailRes = await contentApi.getContentDetail(item.contentId)
          if (detailRes && detailRes.data) {
            const detail = detailRes.data
            // 假设 type=2 为视频，type=1 为文章
            if (detail.type === 2 || detail.contentType === 2) {
              type = 'video'
            }
            summary = detail.summary || detail.description || ''
            if (!cover) cover = detail.coverUrl || detail.cover
          }
        } catch (err) {
          console.warn(`[儿童端收藏页] 获取内容详情失败: ${item.contentId}`, err)
        }
        
        return {
          id: item.contentId,
          title: item.contentTitle || '未命名',
          author: item.creatorName || '匿名',
          cover: cover || 'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500&auto=format&fit=crop&q=60',
          tags: [],
          favoriteDate: formatFavoriteDate(item.createdTime),
          type: type,
          summary: summary,
          shared: false,
          discussed: false,
          recommended: false
        }
      }))
      
      favorites.value = detailedFavorites
      
      console.log('[儿童端收藏页] 收藏列表加载成功，数量：', favorites.value.length)
      
      // 计算统计数据
      calculateFavoritesStats()
    }
  } catch (error) {
    console.error('[儿童端收藏页] 加载收藏列表失败：', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 实时计算收藏统计数据
const calculateFavoritesStats = () => {
  totalFavorites.value = favorites.value.length
  
  // 计算本月收藏数
  monthlyFavorites.value = favorites.value.filter(item => {
    if (item.favoriteDate === '今天' || item.favoriteDate === '昨天') return true
    if (item.favoriteDate.includes('天前') || item.favoriteDate.includes('周前')) {
      return true
    }
    return false
  }).length
  
  console.log('[儿童端收藏页] 统计数据更新完成')
}

// 监听收藏更新事件
const handleCollectionUpdate = (data) => {
  console.log('[儿童端收藏页] 收到收藏更新事件：', data)
  
  if (data.isBookmarked) {
    // 添加到收藏列表（如果不存在）
    const exists = favorites.value.some(item => item.id === data.contentId)
    if (!exists && data.articleInfo) {
      favorites.value.unshift({
        id: data.articleInfo.id,
        title: data.articleInfo.title,
        author: data.articleInfo.author,
        cover: data.articleInfo.cover || 'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500&auto=format&fit=crop&q=60',
        tags: [],
        favoriteDate: '刚刚',
        shared: false,
        discussed: false,
        recommended: false
      })
    }
  } else {
    // 从收藏列表移除
    favorites.value = favorites.value.filter(item => item.id !== data.contentId)
  }
  
  // 重新计算统计数据
  calculateFavoritesStats()
}

onMounted(async () => {
  console.log('[儿童端收藏页] 页面加载')
  await loadUserInfo()
  await loadFavorites()
  
  // 监听收藏更新事件
  eventBus.on(EVENTS.COLLECTION_UPDATED, handleCollectionUpdate, { pageName: '儿童端收藏页' })
})

onUnmounted(() => {
  console.log('[儿童端收藏页] 页面卸载')
  // 取消监听
  eventBus.off(EVENTS.COLLECTION_UPDATED, handleCollectionUpdate)
})
</script>

<style scoped>
/* 容器 */
.container {
  min-height: 100vh;
  background-color: #f8f9fa;
  width: 100%;
  overflow-x: hidden;
}

/* 顶部导航 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 40rpx 32rpx;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  z-index: 100;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
  width: 100%;
  box-sizing: border-box;
}

.back-button {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  transition: transform 0.2s ease;
}

.back-button:active {
  transform: scale(0.9);
}

.back-icon {
  color: #ffffff;
  font-size: 36rpx;
}

.page-title {
  flex: 1;
  font-size: 34rpx;
  font-weight: 600;
  color: #ffffff;
}

/* 主内容 */
.main-content {
  margin-top: 120rpx;
  min-height: calc(100vh - 120rpx);
  box-sizing: border-box;
  padding-top: 32rpx;
}

/* 内容包装器 - 居中 */
.content-wrapper {
  padding: 0 32rpx 48rpx;
  box-sizing: border-box;
}

/* 统计栏 (简化版) */
.stats-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #6366f1;
  line-height: 1.2;
}

.stat-desc {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 8rpx;
}

.stat-divider {
  width: 2rpx;
  height: 40rpx;
  background-color: #f3f4f6;
}

/* 分类标签 */
.tabs-section {
  margin-bottom: 32rpx;
  box-sizing: border-box;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-wrapper {
  display: inline-flex;
  gap: 20rpx;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  padding: 16rpx 32rpx;
  background: #ffffff;
  border-radius: 40rpx;
  border: 2rpx solid #e5e7eb;
  transition: all 0.3s ease;
}

.tab-item.active {
  background: #6366f1;
  border-color: #6366f1;
  transform: scale(1.05);
}

.tab-text {
  font-size: 28rpx;
  color: #6b7280;
  font-weight: 500;
  line-height: 1.2;
}

.tab-item.active .tab-text {
  color: #ffffff;
}

/* 收藏列表 */
.collection-list {
  min-height: 400rpx;
  box-sizing: border-box;
}

/* 状态视图 */
.state-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
}

/* 加载动画 */
.spinner {
  width: 64rpx;
  height: 64rpx;
  border: 5rpx solid rgba(99, 102, 241, 0.2);
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: rotate 1s linear infinite;
  margin-bottom: 24rpx;
}

@keyframes rotate {
  to { transform: rotate(360deg); }
}

.state-text {
  font-size: 28rpx;
  color: #9ca3af;
}

/* 空状态 */
.empty-icon {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.empty-icon .fas {
  font-size: 56rpx;
  color: #f59e0b;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #9ca3af;
}

/* 列表容器 */
.list-container {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding-bottom: 40rpx;
}

/* 列表项 */
.list-item {
  display: flex;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.list-item:active {
  transform: scale(0.98);
  background: #f9fafb;
}

.item-cover-wrapper {
  position: relative;
  width: 200rpx;
  height: 150rpx;
  border-radius: 12rpx;
  overflow: hidden;
  flex-shrink: 0;
  margin-right: 24rpx;
}

.item-cover {
  width: 100%;
  height: 100%;
  background-color: #f3f4f6;
  object-fit: cover;
}

.type-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-badge .fas {
  color: #ffffff;
  font-size: 20rpx;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  height: 150rpx;
}

.item-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 4rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  flex: 1;
}

.item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}

.meta-left {
  display: flex;
  align-items: center;
  font-size: 22rpx;
  color: #9ca3af;
}

.meta-text {
  max-width: 160rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-dot {
  margin: 0 8rpx;
}

.status-tag {
  display: flex;
  align-items: center;
  font-size: 20rpx;
  color: #6366f1;
  background: #eef2ff;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.status-tag .fas {
  margin-right: 6rpx;
  font-size: 18rpx;
}
</style> 
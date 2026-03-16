<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-button" @tap="goBack" @touchstart="handleTouchStart">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">浏览历史</text>
        <view class="refresh-button" @tap="refreshData">
          <text class="fas fa-sync-alt"></text>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      <!-- 浏览统计卡片 -->
      <view v-if="!loading && historyRecords && historyRecords.length > 0" class="stats-card">
        <view class="stats-header">
          <text class="stats-title">浏览统计</text>
          <text class="fas fa-chart-line stats-icon"></text>
        </view>
        <view class="stats-grid">
          <view class="stat-box">
            <text class="stat-number">{{historyRecords.length}}</text>
            <text class="stat-label">浏览</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-box">
            <text class="stat-number">{{articleCount}}</text>
            <text class="stat-label">文章</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-box">
            <text class="stat-number">{{videoCount}}</text>
            <text class="stat-label">视频</text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="!historyRecords || historyRecords.length === 0" class="empty-container">
        <text class="empty-icon">📺</text>
        <text class="empty-text">还没有浏览记录</text>
        <text class="empty-subtitle">快去浏览精彩内容吧！</text>
      </view>

      <!-- 浏览记录列表 -->
      <view v-else class="history-list">
        <view 
          class="history-item" 
          v-for="(item, index) in historyRecords" 
          :key="item.id || index"
        >
          <image 
            :src="item.coverUrl" 
            class="item-cover" 
            mode="aspectFill"
            @error="handleImageError($event, index)"
          ></image>
          <view class="item-info">
            <view class="info-header">
              <text class="item-title">{{item.title}}</text>
              <view class="content-type" :class="item.contentType === 2 ? 'video' : 'article'">
                <text class="type-label">{{item.contentType === 2 ? '视频' : '文章'}}</text>
              </view>
            </view>
            <text class="item-author">{{item.author}}</text>
            <view class="meta-info">
              <text class="meta-item">
                <text class="fas fa-history"></text>
                {{item.lastRead}}
              </text>
              <text class="meta-divider">·</text>
              <text class="meta-item">
                <text class="fas fa-clock"></text>
                {{item.duration}}分钟
              </text>
            </view>
            <view class="action-buttons">
              <view class="action-btn view" @tap="goToContentDetail(item)">
                <text class="fas fa-eye"></text>
                <text>查看</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userBehaviorApi, userApi, viewHistoryApi } from '@/utils/api.js'

export default {
  setup() {
    const statusBarHeight = ref(20)
    const headerHeight = ref(100)
    const currentUser = ref(null)
    const historyRecords = ref([])
    const loading = ref(false)
    const userId = ref(null)

    // 计算文章和视频数量
    const articleCount = computed(() => {
      if (!historyRecords.value) return 0
      return historyRecords.value.filter(item => item.contentType !== 2).length
    })

    const videoCount = computed(() => {
      if (!historyRecords.value) return 0
      return historyRecords.value.filter(item => item.contentType === 2).length
    })

    // 获取系统状态栏高度
    const initSystemInfo = () => {
      try {
        const systemInfo = uni.getSystemInfoSync()
        statusBarHeight.value = systemInfo.statusBarHeight || 20
        headerHeight.value = statusBarHeight.value + 50 + 10
        console.log('[浏览历史] 系统状态栏高度:', statusBarHeight.value)
      } catch (error) {
        console.error('[浏览历史] 获取系统信息失败:', error)
      }
    }

    // 获取用户ID
    const getUserId = () => {
      userId.value = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
      console.log('[浏览历史] 获取到用户ID:', userId.value)

      if (!userId.value) {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo && userInfo.id) {
          userId.value = userInfo.id
          console.log('[浏览历史] 从userInfo获取用户ID:', userId.value)
        } else {
          console.warn('[浏览历史] 未找到用户ID')
        }
      }
    }

    // 加载当前用户信息
    const loadCurrentUser = async () => {
      try {
        console.log('[浏览历史] 开始获取当前用户信息')
        const response = await userApi.getCurrentUser()
        if (response && response.data) {
          console.log('[浏览历史] 获取用户信息成功:', response.data)
          currentUser.value = response.data
        }
      } catch (error) {
        console.error('[浏览历史] 获取用户信息失败:', error)
      }
    }

    // 加载浏览历史记录
    const loadHistoryRecords = async () => {
      try {
        if (!userId.value || loading.value) {
          console.warn('[浏览历史] 用户ID不存在或正在加载')
          return
        }

        loading.value = true
        console.log('[浏览历史] 开始加载浏览历史，用户ID:', userId.value)

        const response = await viewHistoryApi.getUserViewHistory(userId.value, {
          current: 1,
          size: 50
        })

        console.log('[浏览历史] API响应:', response)

        if (response && response.data) {
          let historyData = []

          if (response.data.records && Array.isArray(response.data.records)) {
            historyData = response.data.records
            console.log('[浏览历史] 使用分页数据格式，记录数:', historyData.length)
          } else if (Array.isArray(response.data)) {
            historyData = response.data
            console.log('[浏览历史] 使用数组数据格式，记录数:', historyData.length)
          }

          if (historyData.length > 0) {
            // 过滤有效数据
            const validRecords = historyData.filter(record => {
              const isValid = record.contentId && record.contentTitle && record.contentTitle.trim() !== ''
              if (!isValid) {
                console.log('[浏览历史] 过滤掉无效记录:', record)
              }
              return isValid
            })

            console.log('[浏览历史] 有效历史记录数量:', validRecords.length)

            historyRecords.value = validRecords.map(record => ({
              id: record.id,
              title: record.contentTitle,
              author: record.creatorName || '佚名',
              coverUrl: record.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
              duration: record.duration || 0,
              lastRead: formatViewTime(record.viewTime),
              category: record.categoryName || '未分类',
              contentId: record.contentId,
              contentType: record.contentType || 1
            }))

            console.log('[浏览历史] 处理后的历史记录:', historyRecords.value)
          } else {
            historyRecords.value = []
            console.log('[浏览历史] 没有浏览记录')
          }
        } else {
          console.log('[浏览历史] API返回数据为空')
          historyRecords.value = []
        }
      } catch (error) {
        console.error('[浏览历史] 加载失败:', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      } finally {
        loading.value = false
      }
    }

    // 格式化浏览时间
    const formatViewTime = (viewTime) => {
      if (!viewTime) return '未知时间'

      try {
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
      } catch (error) {
        console.error('[浏览历史] 时间格式化失败:', error)
        return '未知时间'
      }
    }

    // 跳转到内容详情
    const goToContentDetail = (item) => {
      if (!item.contentId && !item.id) {
        uni.showToast({
          title: '内容不存在',
          icon: 'none'
        })
        return
      }

      const contentId = item.contentId || item.id
      const contentType = item.contentType || 1

      console.log('[浏览历史] 跳转到内容详情，ID:', contentId, '类型:', contentType)

      if (contentType === 2) {
        // 视频内容
        const videoData = {
          id: contentId,
          title: item.title,
          coverUrl: item.coverUrl,
          views: '0',
          likes: '0',
          videoUrl: '',
          creatorName: item.author || '未知作者',
          creatorAvatar: '',
          followers: '0',
          description: ''
        }

        const videoInfo = encodeURIComponent(JSON.stringify(videoData))
        uni.navigateTo({
          url: `/pages/child/video-detail?videoInfo=${videoInfo}`,
          success: () => {
            console.log('[浏览历史] 成功跳转到视频详情页')
          },
          fail: (error) => {
            console.error('[浏览历史] 跳转到视频详情页失败:', error)
            uni.showToast({
              title: '跳转失败',
              icon: 'none'
            })
          }
        })
      } else {
        // 文章内容
        uni.navigateTo({
          url: `/pages/child/article-detail?id=${contentId}`,
          success: () => {
            console.log('[浏览历史] 成功跳转到文章详情页')
          },
          fail: (error) => {
            console.error('[浏览历史] 跳转到文章详情页失败:', error)
            uni.showToast({
              title: '跳转失败',
              icon: 'none'
            })
          }
        })
      }
    }

    // 图片加载错误处理
    const handleImageError = (event, index) => {
      console.log('[浏览历史] 图片加载失败，索引:', index)
      const fallbackImages = [
        'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=500&auto=format&fit=crop&q=60',
        'https://images.unsplash.com/photo-1456324504439-367cee3b3c32?w=500&auto=format&fit=crop&q=60',
        'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500&auto=format&fit=crop&q=60'
      ]

      if (historyRecords.value[index]) {
        historyRecords.value[index].coverUrl = fallbackImages[index % fallbackImages.length]
      }
    }

    // 刷新数据
    const refreshData = async () => {
      console.log('[浏览历史] 手动刷新浏览记录')
      historyRecords.value = []
      await loadHistoryRecords()
    }

    // 返回上一页
    const goBack = () => {
      console.log('[浏览历史] 点击返回按钮')
      const pages = getCurrentPages()
      console.log('[浏览历史] 当前页面栈长度:', pages.length)

      if (pages.length > 1) {
        uni.navigateBack({
          success: () => {
            console.log('[浏览历史] 返回上一页成功')
          },
          fail: (error) => {
            console.error('[浏览历史] 返回上一页失败:', error)
            uni.redirectTo({
              url: '/pages/child/profile'
            })
          }
        })
      } else {
        console.log('[浏览历史] 页面栈中无上一页，直接跳转到个人页面')
        uni.redirectTo({
          url: '/pages/child/profile'
        })
      }
    }

    // 触摸反馈
    const handleTouchStart = () => {
      console.log('[浏览历史] 返回按钮被触摸')
    }

    // 加载所有数据
    const loadAllData = async () => {
      console.log('[浏览历史] 加载所有数据')
      await loadCurrentUser()
      await loadHistoryRecords()
    }

    // 页面加载
    onMounted(async () => {
      console.log('[浏览历史] 页面已挂载')
      initSystemInfo()
      getUserId()
      await loadAllData()
    })

    // 页面显示时刷新
    onShow(async () => {
      console.log('[浏览历史] 页面显示，刷新数据')
      await loadAllData()
    })

    return {
      statusBarHeight,
      headerHeight,
      currentUser,
      historyRecords,
      loading,
      userId,
      articleCount,
      videoCount,
      loadHistoryRecords,
      goToContentDetail,
      handleImageError,
      refreshData,
      goBack,
      handleTouchStart
    }
  }
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 40rpx 32rpx;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #ffffff;
}

.back-button {
  padding: 8rpx;
  margin-right: 12rpx;
  min-width: 60rpx;
  min-height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 101;
}

.page-title {
  font-size: 32rpx;
  font-weight: bold;
  flex: 1;
  text-align: center;
}

.refresh-button {
  padding: 8rpx;
  margin-left: 12rpx;
  transition: all 0.3s ease;
}

.refresh-button:active {
  transform: rotate(180deg) scale(0.9);
}

.main-content {
  padding: 32rpx;
  box-sizing: border-box;
}

/* 统计卡片 */
.stats-card {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.3);
}

.stats-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.stats-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #ffffff;
}

.stats-icon {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.stats-grid {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10rpx);
  border-radius: 14rpx;
  padding: 20rpx 12rpx;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  flex: 1;
}

.stat-number {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
  line-height: 1.2;
}

.stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.stat-divider {
  width: 2rpx;
  height: 50rpx;
  background: rgba(255, 255, 255, 0.2);
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 120rpx 0;
  gap: 24rpx;
}

.loading-container::before {
  content: '';
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #e5e7eb;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 28rpx;
  color: #9ca3af;
  font-weight: 500;
}

/* 空状态 */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 32rpx;
  text-align: center;
  background: #ffffff;
  border-radius: 24rpx;
  margin: 40rpx 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 160rpx;
  margin-bottom: 32rpx;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

.empty-text {
  font-size: 34rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 16rpx;
}

.empty-subtitle {
  font-size: 28rpx;
  color: #9ca3af;
  line-height: 1.6;
}

/* 历史记录列表 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.history-item {
  display: flex;
  padding: 16rpx;
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.history-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.item-cover {
  width: 120rpx;
  height: 160rpx;
  border-radius: 12rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
  background: #f3f4f6;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  justify-content: space-between;
}

.info-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8rpx;
}

.item-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.content-type {
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.content-type.article {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
  color: #6366f1;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.content-type.video {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(244, 114, 94, 0.1));
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.type-label {
  font-size: 20rpx;
}

.item-author {
  font-size: 24rpx;
  color: #6b7280;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  font-size: 22rpx;
  color: #9ca3af;
}

.meta-item .fas {
  font-size: 18rpx;
}

.meta-divider {
  color: #d1d5db;
  font-size: 22rpx;
}

.action-buttons {
  display: flex;
  gap: 12rpx;
  margin-top: 8rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn.view {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #ffffff;
}

.action-btn:active {
  transform: scale(0.95);
}
</style> 
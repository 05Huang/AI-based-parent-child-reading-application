<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-button" @tap="goBack" @touchstart="handleTouchStart">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">阅读记录</text>
        <view class="refresh-button" @tap="refreshData">
          <text class="fas fa-sync-alt"></text>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      <!-- 阅读概览卡片 -->
      <view class="overview-card" v-if="readingStats">
        <view class="overview-header">
          <view class="header-left">
            <text class="fas fa-chart-pie overview-icon"></text>
            <text class="overview-title">阅读数据</text>
          </view>
          <text class="update-time">更新于 {{ formatUpdateTime() }}</text>
        </view>
        
        <view class="stats-grid">
          <view class="stat-item main-stat">
            <text class="stat-value">{{ readingStats.totalCount || 0 }}</text>
            <text class="stat-label">阅读篇数</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item main-stat">
            <text class="stat-value">{{ formatDurationDisplay(readingStats.totalDuration || 0) }}</text>
            <text class="stat-label">总时长</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ readingStats.consecutiveDays || 0 }}</text>
            <text class="stat-label">连续天数</text>
          </view>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view v-if="loading && !readingRecords.length" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 空状态 -->
      <view v-else-if="!readingRecords || readingRecords.length === 0" class="empty-container">
        <text class="empty-icon">📚</text>
        <text class="empty-text">还没有阅读记录</text>
        <text class="empty-subtitle">快去阅读精彩文章吧！</text>
      </view>
      
      <!-- 阅读记录列表 -->
      <view v-else class="reading-list">
        <view 
          class="reading-item" 
          v-for="(item, index) in readingRecords" 
          :key="item.id || index"
          @tap="goToContentDetail(item)"
        >
          <view class="item-cover-wrapper">
            <image 
              :src="fixImageUrl(item.coverUrl)" 
              class="book-cover" 
              mode="aspectFill"
              @error="handleImageError($event, index)"
            ></image>
            <view class="type-badge">
              <text :class="['fas', item.contentType === 'video' ? 'fa-play' : 'fa-book']"></text>
            </view>
          </view>
          
          <view class="book-info">
            <view class="info-header">
              <text class="book-title">{{item.title}}</text>
              <text class="time-ago">{{ formatTimeAgo(item.createTime) }}</text>
            </view>
            
            <view class="info-body">
              <view class="meta-row">
                <view class="meta-item">
                  <text class="fas fa-clock meta-icon"></text>
                  <text>{{ formatDuration(item.readingDuration || 0) }}</text>
                </view>
                <view class="meta-item" v-if="item.progress === 100">
                  <text class="fas fa-check-circle meta-icon success"></text>
                  <text class="success-text">已完成</text>
                </view>
                <view class="meta-item" v-else>
                  <text class="fas fa-spinner meta-icon"></text>
                  <text>进行中 {{ item.progress || 0 }}%</text>
                </view>
              </view>
              
              <view class="progress-bar-mini" v-if="item.progress < 100">
                <view class="progress-fill" :style="{ width: (item.progress || 0) + '%' }"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 加载更多 -->
      <view v-if="hasMore && !loading" class="load-more" @tap="loadMore">
        <text class="load-more-text">加载更多</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { viewHistoryApi, contentApi, userBehaviorApi } from '@/utils/api.js'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 100,
      readingRecords: [],
      readingStats: null,
      loading: false,
      hasMore: true,
      currentPage: 1,
      pageSize: 10,
      userId: null
    }
  },
  onLoad() {
    // 获取系统状态栏高度
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight || 20
    this.headerHeight = this.statusBarHeight + 50 + 10 // 减少header高度
    
    // 获取用户ID并加载数据
    this.getUserId()
    this.loadData()
  },
  onShow() {
    this.getUserId()
    this.loadData()
  },
  methods: {
    // 获取用户ID
    getUserId() {
      this.userId = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
      
      // 如果没有用户ID，尝试从其他地方获取
      if (!this.userId) {
        // 尝试从全局状态获取
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo && userInfo.id) {
          this.userId = userInfo.id
        }
      }
    },
    
    // 加载所有数据
    async loadData() {
      if (!this.userId) return
      
      this.loading = true
      try {
        // 并行加载统计数据和列表数据
        await Promise.all([
          this.loadStats(),
          this.loadReadingRecords()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },

    // 加载统计数据
    async loadStats() {
      try {
        const [browsingRes, daysRes] = await Promise.all([
          userBehaviorApi.getBrowsingStats(this.userId),
          userBehaviorApi.getConsecutiveReadingDays(this.userId)
        ])

        if (browsingRes?.code === 200 && browsingRes.data) {
          const totalReadCount = Number(browsingRes.data.totalReadCount) || 0
          const totalReadDurationSeconds = Number(browsingRes.data.totalReadDuration) || 0
          const totalDurationMinutes = Math.floor(totalReadDurationSeconds / 60)

          this.readingStats = {
            totalCount: totalReadCount,
            totalDuration: totalDurationMinutes,
            consecutiveDays: daysRes?.code === 200 ? (Number(daysRes.data) || 0) : 0
          }
        } else {
          this.readingStats = {
            totalCount: 0,
            totalDuration: 0,
            consecutiveDays: daysRes?.code === 200 ? (Number(daysRes.data) || 0) : 0
          }
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        // 设置默认值
        this.readingStats = {
          totalCount: 0,
          totalDuration: 0,
          consecutiveDays: 0
        }
      }
    },
    
    // 加载阅读记录
    async loadReadingRecords(isLoadMore = false) {
      if (!this.userId) {
        this.loadFallbackData()
        return
      }
      
      try {
        if (!isLoadMore) {
          this.currentPage = 1
        }
        
        // 调用浏览历史API
        const params = {
          current: this.currentPage,
          size: this.pageSize
        }
        
        console.log('调用API参数:', params)
        const response = await viewHistoryApi.getUserViewHistory(this.userId, params)
        console.log('浏览历史API响应:', response)
        
        if (response?.data) {
          let historyData = []
          
          // 处理分页数据
          if (response.data.records && Array.isArray(response.data.records)) {
            historyData = response.data.records
            this.hasMore = this.currentPage < (response.data.pages || 1)
            console.log('使用分页数据格式，记录数:', historyData.length, '总页数:', response.data.pages)
          } else if (Array.isArray(response.data)) {
            historyData = response.data
            this.hasMore = historyData.length >= this.pageSize
            console.log('使用数组数据格式，记录数:', historyData.length)
          } else {
            console.log('数据格式不符合预期:', response.data)
            historyData = []
            this.hasMore = false
          }
          
          if (historyData.length > 0) {
            console.log('开始处理历史记录数据...')
            // 处理每条记录
            const processedRecords = await this.processHistoryRecords(historyData)
            
            if (isLoadMore) {
              this.readingRecords = [...this.readingRecords, ...processedRecords]
            } else {
              this.readingRecords = processedRecords
            }
            
            console.log('处理后的浏览记录:', this.readingRecords)
          } else {
            console.log('没有浏览记录数据')
            if (!isLoadMore) {
              this.readingRecords = []
            }
            this.hasMore = false
          }
          
        } else {
          console.log('API返回数据为空或格式错误:', response)
          if (!isLoadMore && this.readingRecords.length === 0) {
            console.log('首次加载且无数据，使用fallback数据')
            this.loadFallbackData()
          }
        }
        
      } catch (error) {
        console.error('加载浏览记录失败:', error)
        console.error('错误详情:', error.message, error.stack)
        
        if (!isLoadMore && this.readingRecords.length === 0) {
          console.log('首次加载失败，使用fallback数据')
          this.loadFallbackData()
        }
        
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    // 格式化时长显示 (总时长)
    formatDurationDisplay(minutes) {
      if (!minutes) return '0分钟'
      if (minutes < 60) return `${minutes}分钟`
      const hours = Math.floor(minutes / 60)
      const mins = minutes % 60
      return mins > 0 ? `${hours}小时${mins}分` : `${hours}小时`
    },

    // 格式化时长 (单项)
    formatDuration(minutes) {
      return minutes > 0 ? `${minutes}分钟` : '< 1分钟'
    },

    // 格式化时间显示 (多久前)
    formatTimeAgo(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const now = new Date()
      const diff = now - date
      
      const minute = 60 * 1000
      const hour = 60 * minute
      const day = 24 * hour
      
      if (diff < minute) return '刚刚'
      if (diff < hour) return Math.floor(diff / minute) + '分钟前'
      if (diff < day) return Math.floor(diff / hour) + '小时前'
      if (diff < day * 30) return Math.floor(diff / day) + '天前'
      return date.toLocaleDateString()
    },

    // 格式化更新时间
    formatUpdateTime() {
      const now = new Date()
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    },
    
    // 处理历史记录数据
    async processHistoryRecords(historyData) {
      const processedRecords = []
      
      for (const record of historyData) {
        try {
          // 获取内容详情
          let contentDetail = null
          if (record.contentId) {
            try {
              const contentResponse = await contentApi.getContentDetail(record.contentId)
              if (contentResponse?.data) {
                contentDetail = contentResponse.data
              }
            } catch (contentError) {
              console.warn('获取内容详情失败:', contentError)
            }
          }
          
          // 组装记录数据
          const processedRecord = {
            id: record.id || record.contentId,
            contentId: record.contentId,
            contentType: this.detectContentType(contentDetail), // 通过内容详情检测类型
            title: contentDetail?.title || record.title || '未知标题',
            coverUrl: contentDetail?.coverUrl || record.coverUrl,
            progress: this.calculateProgress(record),
            readingDuration: this.calculateReadingTime(record),
            createTime: record.createTime || record.viewTime,
            author: contentDetail?.author || contentDetail?.creatorName,
            category: contentDetail?.category
          }
          
          processedRecords.push(processedRecord)
          
        } catch (error) {
          console.error('处理单条记录失败:', error)
          // 添加基础记录
          processedRecords.push({
            id: record.id || record.contentId,
            contentId: record.contentId,
            title: record.title || '未知标题',
            coverUrl: record.coverUrl,
            progress: 0,
            readingDuration: 0,
            createTime: record.createTime || record.viewTime
          })
        }
      }
      
      return processedRecords
    },
    
    // 检测内容类型（通过内容详情判断）
    detectContentType(contentDetail) {
      if (!contentDetail) return 'article'
      
      // 如果有videoUrl字段，说明是视频
      if (contentDetail.videoUrl || contentDetail.type === 2) {
        return 'video'
      }
      
      // 如果有duration字段且coverUrl包含video关键字，说明是视频
      if (contentDetail.duration || (contentDetail.coverUrl && contentDetail.coverUrl.includes('video'))) {
        return 'video'
      }
      
      // 默认为文章
      return 'article'
    },
    
    // 计算阅读进度
    calculateProgress(record) {
      // 这里可以根据实际的阅读记录字段来计算进度
      // 暂时使用随机值模拟
      if (record.progress !== undefined) return record.progress
      if (record.readingProgress !== undefined) return record.readingProgress
      
      // 根据浏览次数估算进度
      const viewCount = record.viewCount || 1
      return Math.min(viewCount * 20, 100)
    },
    
    // 计算阅读时长
    calculateReadingTime(record) {
      // 这里可以根据实际的阅读时长字段来计算
      if (record.readingDuration !== undefined) return record.readingDuration
      if (record.duration !== undefined) return record.duration
      
      // 根据浏览次数估算时长
      const viewCount = record.viewCount || 1
      return Math.min(viewCount * 5, 60)
    },
    
    // 加载fallback数据
    loadFallbackData() {
      console.log('使用fallback阅读记录数据')
      this.readingRecords = [
        {
          id: 1,
          title: '探索海洋生物的奥秘',
          coverUrl: 'https://images.unsplash.com/photo-1456324504439-367cee3b3c32?w=500&auto=format&fit=crop&q=60',
          progress: 100,
          createTime: new Date().toISOString(),
          readingDuration: 15
        },
        {
          id: 2,
          title: '太空探索：人类的新征程',
          coverUrl: 'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500&auto=format&fit=crop&q=60',
          progress: 85,
          createTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
          readingDuration: 20
        },
        {
          id: 3,
          title: '人工智能改变世界',
          coverUrl: 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=500&auto=format&fit=crop&q=60',
          progress: 75,
          createTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
          readingDuration: 18
        }
      ]
      this.hasMore = false
    },
    
    // 修复图片URL
    fixImageUrl(url) {
      if (!url) {
        return 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=500&auto=format&fit=crop&q=60'
      }
      
      // 保持原始URL不变，数据库中存储的就是正确的
      return url
    },
    
    // 图片加载错误处理
    handleImageError(event, index) {
      console.log('图片加载失败，索引:', index)
      const fallbackImages = [
        'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=500&auto=format&fit=crop&q=60',
        'https://images.unsplash.com/photo-1456324504439-367cee3b3c32?w=500&auto=format&fit=crop&q=60',
        'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500&auto=format&fit=crop&q=60'
      ]
      
      if (this.readingRecords[index]) {
        this.readingRecords[index].coverUrl = fallbackImages[index % fallbackImages.length]
      }
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '未知时间'
      
      try {
        const date = new Date(dateString)
        const now = new Date()
        const diffTime = now - date
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
        
        if (diffDays === 0) return '今天'
        if (diffDays === 1) return '昨天'
        if (diffDays === 2) return '前天'
        if (diffDays <= 7) return `${diffDays}天前`
        
        // 超过7天显示具体日期
        return `${date.getMonth() + 1}月${date.getDate()}日`
      } catch (error) {
        console.error('日期格式化失败:', error)
        return '未知时间'
      }
    },
    
    // 跳转到内容详情（支持文章和视频）
    goToContentDetail(item) {
      if (!item.contentId && !item.id) {
        uni.showToast({
          title: '内容不存在',
          icon: 'none'
        })
        return
      }
      
      const contentId = item.contentId || item.id
      const contentType = item.contentType || 'article' // 默认为文章类型
      
      console.log('跳转到内容详情，ID:', contentId, '类型:', contentType)
      
      if (contentType === 'video') {
        // 跳转到视频详情页
        // 需要构造视频数据对象
        const videoData = {
          id: contentId,
          title: item.title,
          coverUrl: item.coverUrl,
          category: item.category,
          views: '0',
          likes: '0',
          videoUrl: '', // 视频URL需要从API获取
          creatorName: item.author || '未知作者',
          creatorAvatar: '',
          followers: '0',
          description: ''
        }
        
        const videoInfo = encodeURIComponent(JSON.stringify(videoData))
        uni.navigateTo({
          url: `/pages/child/video-detail?videoInfo=${videoInfo}`,
          success: () => {
            console.log('成功跳转到视频详情页')
          },
          fail: (error) => {
            console.error('跳转到视频详情页失败:', error)
            uni.showToast({
              title: '跳转失败',
              icon: 'none'
            })
          }
        })
      } else {
        // 跳转到文章详情页
        uni.navigateTo({
          url: `/pages/child/article-detail?id=${contentId}`,
          success: () => {
            console.log('成功跳转到文章详情页')
          },
          fail: (error) => {
            console.error('跳转到文章详情页失败:', error)
            uni.showToast({
              title: '跳转失败',
              icon: 'none'
            })
          }
        })
      }
    },
    
    // 加载更多
    loadMore() {
      if (!this.hasMore || this.loading) return
      
      this.currentPage++
      this.loadReadingRecords(true)
    },
    
    // 刷新数据
    async refreshData() {
      console.log('手动刷新浏览记录数据')
      this.currentPage = 1
      this.hasMore = true
      this.readingRecords = []
      await this.loadData()
    },
    
    // 触摸反馈
    handleTouchStart() {
      console.log('返回按钮被触摸')
    },
    
    goBack() {
      console.log('点击返回按钮')
      
      // 检查页面栈
      const pages = getCurrentPages()
      console.log('当前页面栈长度:', pages.length)
      
      if (pages.length > 1) {
        // 有上一页，使用标准返回
        uni.navigateBack({
          success: () => {
            console.log('返回上一页成功')
          },
          fail: (error) => {
            console.error('返回上一页失败:', error)
            this.forceGoToProfile()
          }
        })
      } else {
        // 没有上一页，直接跳转到个人页面
        console.log('页面栈中无上一页，直接跳转到个人页面')
        this.forceGoToProfile()
      }
    },
    
    // 强制跳转到个人页面
    forceGoToProfile() {
      console.log('强制跳转到个人页面')
      uni.redirectTo({
        url: '/pages/child/profile',
        success: () => {
          console.log('成功跳转到个人页面')
        },
        fail: (error) => {
          console.error('跳转到个人页面失败:', error)
          uni.showToast({
            title: '返回失败',
            icon: 'none'
          })
        }
      })
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
  min-width: 60rpx;
  min-height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.refresh-button:active {
  transform: rotate(180deg) scale(0.9);
}

.refresh-button .fas {
  display: inline-block;
  transition: transform 0.3s ease;
}

.main-content {
  margin-top: 120rpx;
  padding: 32rpx;
  height: calc(100vh - 120rpx);
  box-sizing: border-box;
}

/* 概览卡片 */
.overview-card {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.3);
  color: #ffffff;
}

.overview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.overview-title {
  font-size: 30rpx;
  font-weight: 600;
}

.overview-icon {
  font-size: 32rpx;
  opacity: 0.9;
}

.update-time {
  font-size: 22rpx;
  opacity: 0.7;
}

.stats-grid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16rpx;
  padding: 24rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  font-size: 22rpx;
  opacity: 0.8;
}

.stat-divider {
  width: 1px;
  height: 40rpx;
  background: rgba(255, 255, 255, 0.2);
}

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

.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 32rpx;
  margin-top: 16rpx;
}

.load-more-text {
  font-size: 28rpx;
  color: #6366f1;
  padding: 18rpx 48rpx;
  border: 2rpx solid #6366f1;
  border-radius: 48rpx;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.05), rgba(139, 92, 246, 0.05));
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.1);
}

.load-more:active .load-more-text {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #ffffff;
  transform: scale(0.95);
}

.reading-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.reading-item {
  display: flex;
  align-items: flex-start;
  padding: 24rpx;
  background: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.reading-item:active {
  transform: scale(0.98);
  background: #f9fafb;
}

.item-cover-wrapper {
  position: relative;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.book-cover {
  width: 140rpx;
  height: 196rpx;
  border-radius: 12rpx;
  background: #f3f4f6;
  box-shadow: 0 4rpx 8rpx rgba(0,0,0,0.1);
}

.type-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-badge .fas {
  font-size: 18rpx;
  color: #ffffff;
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 196rpx;
}

.info-header {
  margin-bottom: 16rpx;
}

.book-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.time-ago {
  font-size: 22rpx;
  color: #9ca3af;
}

.info-body {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.meta-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

.meta-icon.success {
  color: #10b981;
}

.success-text {
  color: #10b981;
  font-weight: 500;
}

.progress-bar-mini {
  height: 6rpx;
  background: #e5e7eb;
  border-radius: 6rpx;
  overflow: hidden;
  width: 100%;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 6rpx;
}
</style> 

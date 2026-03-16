<template>
  <view class="container">
    <!-- 顶部搜索栏 -->
    <view class="search-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="search-bar">
        <view class="search-input-wrap">
          <text class="fas fa-search search-icon"></text>
          <input 
            class="search-input" 
            type="text" 
            v-model="searchKeyword"
            placeholder="搜索文章或视频" 
            confirm-type="search"
            @confirm="handleSearch"
            focus
          />
          <text 
            v-if="searchKeyword" 
            class="fas fa-times-circle clear-icon"
            @tap="clearSearch"
          ></text>
        </view>
        <text class="cancel-btn" @tap="goBack">取消</text>
      </view>
    </view>

    <!-- 搜索内容区域 -->
    <scroll-view 
      scroll-y 
      class="search-content"
      :style="{ paddingTop: headerHeight + 'px' }"
    >
      <!-- 搜索历史 -->
      <view class="search-section" v-if="!searchKeyword && searchHistory.length > 0">
        <view class="section-header">
          <text class="section-title">搜索历史</text>
          <text class="fas fa-trash clear-history" @tap="clearHistory"></text>
        </view>
        <view class="history-list">
          <view 
            class="history-item" 
            v-for="(item, index) in searchHistory" 
            :key="index"
            @tap="useHistoryItem(item)"
          >
            <text class="fas fa-history"></text>
            <text class="history-text">{{item}}</text>
          </view>
        </view>
      </view>

      <!-- 推荐内容 -->
      <view class="search-section" v-if="!searchKeyword">
        <view class="section-header">
          <text class="section-title">推荐内容</text>
          <text class="refresh-text" @tap="loadRecommendedContent(true)">
            <text class="fas fa-sync-alt"></text>
            换一换
          </text>
        </view>
        <view class="recommended-grid">
          <view 
            v-for="item in recommendedContent" 
            :key="item.id"
            class="recommended-item"
            @tap="goToContentDetail(item)"
          >
            <view class="recommended-cover">
              <image 
                :src="item.cover" 
                mode="aspectFill"
                class="cover-image"
              ></image>
              <!-- 内容类型标识 -->
              <view class="content-badge" :class="{ 'video-badge': item.type === 2 }">
                <text class="fas" :class="item.type === 2 ? 'fa-play' : 'fa-file-alt'"></text>
              </view>
            </view>
            <view class="recommended-info">
              <text class="recommended-title">{{ item.title }}</text>
              <view class="recommended-meta">
                <text class="recommended-author">{{ item.author }}</text>
                <view class="recommended-stats">
                  <text class="fas fa-eye"></text>
                  <text class="stats-text">{{ item.views }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 搜索结果 -->
      <view class="search-result" v-if="searchKeyword && searchResults.length > 0">
        <view 
          class="result-item" 
          v-for="(item, index) in searchResults" 
          :key="index"
          @tap="goToContentDetail(item)"
        >
          <!-- 封面图 -->
          <image 
            class="result-cover" 
            :src="item.cover" 
            mode="aspectFill"
          ></image>
          
          <view class="result-info">
            <view class="result-header">
              <text v-if="item.type === 2" class="fas fa-play-circle type-icon"></text>
              <text class="result-title">{{item.title}}</text>
            </view>
            <text class="result-summary">{{item.summary}}</text>
            <view class="result-meta">
              <text class="result-author">{{item.author}}</text>
              <view class="result-stats">
                <text class="fas fa-eye"></text>
                <text class="view-count">{{item.views}}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 无搜索结果 -->
      <view class="no-result" v-if="searchKeyword && searchResults.length === 0">
        <text class="fas fa-search-minus no-result-icon"></text>
        <text class="no-result-text">暂无相关文章</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { searchApi, contentApi, recommendationApi, userApi } from '@/utils/api.js'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 64,
      searchKeyword: '',
      searchHistory: [],
      recommendedContent: [],
      searchResults: [],
      loading: false,
      debounceTimer: null,
      currentUser: null
    }
  },
  watch: {
    // 监听搜索关键词变化实现实时搜索
    searchKeyword(newVal) {
      if (this.debounceTimer) clearTimeout(this.debounceTimer)
      
      this.debounceTimer = setTimeout(() => {
        if (newVal && newVal.trim()) {
          // 传入 true 表示是实时搜索
          this.handleSearch(true)
        }
      }, 500)
    }
  },
  onLoad() {
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight
    this.headerHeight = this.statusBarHeight + 44
    
    // 从本地存储获取搜索历史
    const history = uni.getStorageSync('searchHistory')
    if (history) {
      this.searchHistory = JSON.parse(history)
    }
    
    // 加载推荐内容
    this.loadRecommendedContent()
  },
  methods: {
    async loadCurrentUser() {
      try {
        const stored = uni.getStorageSync('userInfo')
        if (stored) {
          this.currentUser = stored
        }
      } catch (e) {
      }

      try {
        const response = await userApi.getCurrentUser()
        if (response && response.data) {
          this.currentUser = response.data
          uni.setStorageSync('userInfo', response.data)
        }
      } catch (e) {
      }
    },

    getCurrentUserId() {
      return this.currentUser?.id || uni.getStorageSync('currentUserId') || uni.getStorageSync('userId') || null
    },

    // 加载推荐内容
    async loadRecommendedContent(shuffle = false) {
      try {
        if (!this.currentUser) {
          await this.loadCurrentUser()
        }

        const userId = this.getCurrentUserId()
        if (!userId) {
          this.recommendedContent = await this.loadRecommendedContentFallback()
          return
        }

        const [articleResult, videoResult] = await Promise.allSettled([
          recommendationApi.getArticleRecommendations(userId, 3, shuffle),
          recommendationApi.getVideoRecommendations(userId, 3, shuffle)
        ])

        const articles = articleResult.status === 'fulfilled' && Array.isArray(articleResult.value?.data) ? articleResult.value.data : []
        const videos = videoResult.status === 'fulfilled' && Array.isArray(videoResult.value?.data) ? videoResult.value.data : []

        const keySet = new Set()
        const combined = []
        for (const item of [...articles, ...videos]) {
          if (!item || !item.id) continue
          const type = item.type || (item.mediaUrl ? 2 : 1)
          const key = `${type}-${item.id}`
          if (keySet.has(key)) continue
          keySet.add(key)
          combined.push({
            id: item.id,
            cover: item.coverUrl || 'https://via.placeholder.com/150',
            title: item.title || '无标题',
            author: item.creatorName || '阅读小助手',
            views: this.formatViewCount(item.viewCount || 0),
            type: type
          })
          if (combined.length >= 6) break
        }

        if (combined.length < 6) {
          const fallback = await this.loadRecommendedContentFallback()
          for (const item of fallback) {
            const key = `${item.type}-${item.id}`
            if (keySet.has(key)) continue
            keySet.add(key)
            combined.push(item)
            if (combined.length >= 6) break
          }
        }

        if (combined.length === 0) {
          this.recommendedContent = await this.loadRecommendedContentFallback()
          return
        }

        this.recommendedContent = combined
      } catch (error) {
        console.error('加载推荐内容失败：', error)
        this.recommendedContent = await this.loadRecommendedContentFallback()
      }
    },

    async loadRecommendedContentFallback() {
      try {
        const [articleRes, videoRes] = await Promise.all([
          contentApi.getContentPage({
            current: 1,
            size: 20,
            status: 1,
            type: 1,
            sortField: 'created_time',
            sortOrder: 'desc'
          }),
          contentApi.getContentPage({
            current: 1,
            size: 20,
            status: 1,
            type: 2,
            sortField: 'created_time',
            sortOrder: 'desc'
          })
        ])

        let articles = []
        let videos = []

        if (articleRes && articleRes.data && articleRes.data.records) {
          articles = articleRes.data.records.filter(item => !item.type || item.type === 1)
        }

        if (videoRes && videoRes.data && videoRes.data.records) {
          videos = videoRes.data.records.filter(item => item.type === 2)
        }

        const selectedArticles = articles.sort(() => Math.random() - 0.5).slice(0, 3)
        const selectedVideos = videos.sort(() => Math.random() - 0.5).slice(0, 3)

        let combined = [...selectedArticles, ...selectedVideos]

        if (combined.length < 6) {
          const remainingCount = 6 - combined.length
          const remainingArticles = articles.filter(a => !selectedArticles.includes(a))
          if (remainingArticles.length > 0) {
            combined = [...combined, ...remainingArticles.slice(0, remainingCount)]
          }

          if (combined.length < 6) {
            const stillNeed = 6 - combined.length
            const remainingVideos = videos.filter(v => !selectedVideos.includes(v))
            if (remainingVideos.length > 0) {
              combined = [...combined, ...remainingVideos.slice(0, stillNeed)]
            }
          }
        }

        const shuffled = combined.sort(() => Math.random() - 0.5)

        return shuffled.map(item => ({
          id: item.id,
          cover: item.coverUrl || 'https://via.placeholder.com/150',
          title: item.title || '无标题',
          author: item.creatorName || '阅读小助手',
          views: this.formatViewCount(item.viewCount || 0),
          type: item.type || 1
        }))
      } catch (error) {
        console.error('降级加载推荐内容失败：', error)
        return []
      }
    },
    
    // 执行搜索
    async handleSearch(isRealTime = false) {
      if (!this.searchKeyword || !this.searchKeyword.trim()) return
      
      const keyword = this.searchKeyword.trim()
      // 确保 isRealTime 是布尔值
      const isAutoSearch = isRealTime === true
      
      console.log('开始搜索，关键词：', keyword, '自动搜索：', isAutoSearch)
      
      try {
        this.loading = true
        
        // 保存搜索历史（仅非自动搜索时）
        if (!isAutoSearch && !this.searchHistory.includes(keyword)) {
          this.searchHistory.unshift(keyword)
          if (this.searchHistory.length > 10) {
            this.searchHistory.pop()
          }
          uni.setStorageSync('searchHistory', JSON.stringify(this.searchHistory))
        }
        
        // 调用搜索API
        const response = await searchApi.searchContent(keyword, 1, 20)
        
        if (response && response.data) {
          const records = response.data.records || []
          console.log('搜索完成，找到', response.data.total || 0, '条结果')
          
          this.searchResults = records.map(item => ({
            id: item.id,
            title: item.title,
            summary: this.stripHtml(item.description || item.content || '', 60),
            cover: item.coverUrl || 'https://via.placeholder.com/150',
            author: item.creatorName || '作者',
            category: item.tags || '推荐阅读',
            views: this.formatViewCount(item.viewCount || 0),
            type: item.type
          }))
          
          if (this.searchResults.length === 0) {
            console.log('未找到搜索结果')
          }
        } else {
          this.searchResults = []
          console.log('未找到搜索结果')
        }
      } catch (error) {
        console.error('搜索失败：', error)
        uni.showToast({
          title: '搜索失败，请重试',
          icon: 'none'
        })
        
        // 使用模拟搜索结果
        this.searchResults = [
          {
            id: 1,
            title: `与"${this.searchKeyword}"相关的文章1`,
            summary: '这是一篇与您搜索内容相关的文章，包含了丰富的知识内容...',
            author: '专业作者',
            category: '推荐阅读'
          },
          {
            id: 2,
            title: `与"${this.searchKeyword}"相关的文章2`,
            summary: '这是另一篇与您搜索内容相关的文章，提供了深入的见解...',
            author: '知名作者',
            category: '热门推荐'
          }
        ]
      } finally {
        this.loading = false
      }
    },
    
    // 格式化浏览量
    formatViewCount(count) {
      if (count < 1000) return count.toString()
      if (count < 10000) return (count / 1000).toFixed(1) + 'k'
      return (count / 10000).toFixed(1) + '万'
    },
    
    clearSearch() {
      this.searchKeyword = ''
      this.searchResults = []
    },
    
    clearHistory() {
      this.searchHistory = []
      uni.removeStorageSync('searchHistory')
    },
    
    useHistoryItem(keyword) {
      this.searchKeyword = keyword
      this.handleSearch()
    },
    
    useHotSearch(keyword) {
      this.searchKeyword = keyword
      this.handleSearch()
    },
    
    goToContentDetail(item) {
      // 记录浏览行为
      if (item.type === 2) {
        // 跳转到视频详情
        uni.navigateTo({
          url: `/pages/child/video-detail?id=${item.id}&title=${encodeURIComponent(item.title)}`
        })
      } else {
        // 跳转到文章详情
        uni.navigateTo({
          url: `/pages/child/article-detail?id=${item.id}&title=${encodeURIComponent(item.title)}`
        })
      }
    },
    
    goBack() {
      console.log('点击返回按钮')
      
      // 获取当前页面栈
      const pages = getCurrentPages()
      console.log('当前页面栈长度:', pages.length)
      
      if (pages.length > 1) {
        // 如果有上一页，直接返回
        uni.navigateBack({
          delta: 1,
          success: () => {
            console.log('返回上一页成功')
          },
          fail: (err) => {
            console.error('返回失败:', err)
            this.fallbackNavigation()
          }
        })
      } else {
        // 如果没有上一页，使用备用导航
        this.fallbackNavigation()
      }
    },
    
    // 备用导航方法
    fallbackNavigation() {
      console.log('执行备用导航')
      // 尝试返回到首页
      uni.reLaunch({
        url: '/pages/child/home',
        success: () => {
          console.log('跳转到孩子端首页成功')
        },
        fail: (err) => {
          console.error('跳转首页失败:', err)
          uni.showToast({
            title: '返回失败，请重试',
            icon: 'none',
            duration: 2000
          })
        }
      })
    },

    // 去除HTML标签
    stripHtml(html, length) {
      if (!html) return ''
      // 匹配HTML标签并替换为空字符串
      let text = html.replace(/<[^>]+>/g, '')
      // 替换HTML实体
      text = text.replace(/&nbsp;/g, ' ')
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&')
        .replace(/\s+/g, ' ') // 合并空白字符
        .trim()
      
      if (length && text.length > length) {
        return text.substring(0, length) + '...'
      }
      return text
    }
  }
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f6f6f6;
}

/* 搜索头部样式 */
.search-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 8rpx 24rpx;
  height: 88rpx;
}

.search-input-wrap {
  flex: 1;
  height: 72rpx;
  background: #f5f5f5;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  margin-right: 20rpx;
}

.search-icon {
  font-size: 32rpx;
  color: #999;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
}

.clear-icon {
  font-size: 32rpx;
  color: #999;
  padding: 10rpx;
}

.cancel-btn {
  font-size: 32rpx;
  color: #666;
}

/* 搜索内容区域样式 */
.search-content {
  padding: 32rpx;
  box-sizing: border-box;
  width: 100%;
}

.search-section {
  margin-bottom: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.clear-history {
  font-size: 32rpx;
  color: #999;
  padding: 10rpx;
}

/* 历史记录样式 */
.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
}

.history-item .fas {
  font-size: 24rpx;
  color: #999;
  margin-right: 8rpx;
}

.type-icon {
  color: #7c3aed;
  margin-right: 10rpx;
  font-size: 32rpx;
}

.history-text {
  font-size: 28rpx;
  color: #666;
}

/* 热门搜索样式 */
.hot-search-list {
  display: flex;
  flex-direction: column;
}

.hot-search-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
}

.hot-search-rank {
  width: 48rpx;
  font-size: 32rpx;
  color: #999;
  font-weight: bold;
}

.hot-search-rank.top-rank {
  color: #ff6b6b;
}

.hot-search-text {
  font-size: 28rpx;
  color: #333;
}

/* 推荐内容样式 */
.refresh-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 26rpx;
  color: #3b82f6;
  cursor: pointer;
}

.refresh-text .fas {
  font-size: 24rpx;
  margin-right: 4rpx;
}

.recommended-grid {
  column-count: 2;
  column-gap: 24rpx;
}

.recommended-item {
  background-color: white;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out;
  break-inside: avoid;
  margin-bottom: 24rpx;
  display: inline-block;
  width: 100%;
}

/* 实现错落效果：随机高度 */
.recommended-item:nth-child(6n+1) .recommended-cover {
  height: 360rpx;
}

.recommended-item:nth-child(6n+2) .recommended-cover {
  height: 320rpx;
}

.recommended-item:nth-child(6n+3) .recommended-cover {
  height: 400rpx;
}

.recommended-item:nth-child(6n+4) .recommended-cover {
  height: 280rpx;
}

.recommended-item:nth-child(6n+5) .recommended-cover {
  height: 340rpx;
}

.recommended-item:nth-child(6n) .recommended-cover {
  height: 380rpx;
}

.recommended-item:active {
  transform: scale(0.98);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.recommended-cover {
  position: relative;
  width: 100%;
  overflow: hidden;
  transition: height 0.3s ease;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 56rpx;
  height: 56rpx;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content-badge.video-badge {
  background-color: rgba(234, 179, 8, 0.9);
}

.content-badge .fas {
  color: white;
  font-size: 24rpx;
}

.recommended-info {
  padding: 24rpx;
}

.recommended-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.4;
  height: 40rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommended-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recommended-author {
  font-size: 24rpx;
  color: #6b7280;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}

.recommended-stats {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.recommended-stats .fas {
  font-size: 24rpx;
  color: #9ca3af;
}

.stats-text {
  font-size: 24rpx;
  color: #9ca3af;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media screen and (min-width: 768px) {
  .recommended-grid {
    column-count: 3;
    column-gap: 32rpx;
  }
  
  .recommended-item {
    margin-bottom: 32rpx;
  }
}

@media screen and (min-width: 1024px) {
  .recommended-grid {
    column-count: 4;
  }
}

/* 搜索结果样式 */
.search-result {
  padding-top: 12rpx;
}

.result-item {
  background: #fff;
  padding: 20rpx;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  display: flex;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.result-cover {
  width: 220rpx;
  height: 160rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
  background-color: #eee;
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
}

.result-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8rpx;
}

.type-icon {
  color: #ff6b6b;
  margin-right: 8rpx;
  font-size: 32rpx;
  flex-shrink: 0;
  margin-top: 4rpx;
}

.result-title {
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
  line-height: 1.4;
  /* 限制两行显示 */
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.result-summary {
  font-size: 24rpx;
  color: #888;
  line-height: 1.4;
  margin-bottom: 12rpx;
  /* 限制两行显示 */
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-author {
  font-size: 22rpx;
  color: #999;
}

.result-stats {
  display: flex;
  align-items: center;
}

.result-stats .fas {
  font-size: 24rpx;
  color: #999;
  margin-right: 6rpx;
}

.view-count {
  font-size: 22rpx;
  color: #999;
}

/* 无结果样式 */
.no-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 160rpx;
}

.no-result-icon {
  font-size: 96rpx;
  color: #999;
  margin-bottom: 24rpx;
}

.no-result-text {
  font-size: 28rpx;
  color: #999;
}
</style> 

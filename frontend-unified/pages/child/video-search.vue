<template>
  <view class="container">
    <!-- 顶部搜索栏 -->
    <view class="search-header">
      <view class="search-box">
        <view class="search-input-wrapper">
          <text class="fas fa-search search-icon"></text>
          <input 
            type="text" 
            v-model="searchText"
            placeholder="搜索感兴趣的视频" 
            placeholder-class="search-placeholder" 
            class="search-input"
            confirm-type="search"
            @confirm="handleSearch"
            focus
          />
          <text v-if="searchText" class="fas fa-times-circle clear-icon" @click="clearSearch"></text>
        </view>
        <text class="cancel-btn" @click="goBack">取消</text>
      </view>
    </view>

    <!-- 搜索历史和热门搜索 -->
    <view v-if="!searchText" class="search-content">
    <!-- 搜索历史 -->
      <view class="section" v-if="searchHistory.length > 0">
        <view class="section-header">
          <text class="section-title">搜索历史</text>
          <text class="fas fa-trash clear-history" @click="clearHistory"></text>
        </view>
        <view class="history-tags">
          <view 
            class="history-tag" 
            v-for="(item, index) in searchHistory" 
            :key="index"
            @click="useHistorySearch(item)"
          >
            {{ item }}
          </view>
        </view>
      </view>

      <!-- 热门搜索 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">热门视频</text>
        </view>
        <view class="hot-search-list">
          <view 
            class="hot-search-item" 
            v-for="(item, index) in hotSearchList" 
            :key="index"
            @click="useHistorySearch(item.keyword)"
          >
            <text class="hot-search-rank" :class="{'top-rank': index < 3}">{{ index + 1 }}</text>
            <text class="hot-search-keyword">{{ item.keyword }}</text>
            <text class="hot-search-count">{{ item.count }}</text>
          </view>
        </view>
      </view>

      <!-- 推荐视频 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">推荐视频</text>
          <text class="refresh-text" @click="loadRecommendedVideos">
            <text class="fas fa-sync-alt"></text>
            换一换
          </text>
        </view>
        <view class="recommended-grid">
          <view 
            v-for="item in recommendedVideos" 
            :key="item.id"
            class="recommended-item"
            @click="navigateToVideo(item)"
          >
            <view class="recommended-cover">
              <image 
                :src="item.cover" 
                mode="aspectFill"
                class="cover-image"
              ></image>
              <!-- 视频标识 -->
              <view class="video-badge">
                <text class="fas fa-play"></text>
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
    </view>

    <!-- 搜索结果 -->
    <scroll-view 
      v-else 
      scroll-y 
      class="search-results"
      @scrolltolower="loadMore"
    >
      <!-- 无搜索结果提示 -->
      <view v-if="searchResults.length === 0 && !isLoading && searchText.trim()" class="no-result">
        <text class="no-result-icon">🔍</text>
        <text class="no-result-title">没有找到相关视频</text>
        <text class="no-result-desc">试试其他关键词吧</text>
        
        <view class="suggestion-tags">
          <text class="suggestion-tag" 
                v-for="(tag, index) in suggestionTags" 
                :key="index"
                @click="useHistorySearch(tag)">
            {{ tag }}
          </text>
        </view>
      </view>

      <!-- 搜索结果列表 -->
      <template v-else>
        <view class="result-item" v-for="(item, index) in searchResults" :key="index" @click="navigateToVideo(item)">
          <view class="result-cover">
            <image :src="item.cover" mode="aspectFill" class="result-image"></image>
            <view class="play-badge">
              <text class="fas fa-play"></text>
            </view>
          </view>
          <view class="result-info">
            <text class="result-title">{{ item.title }}</text>
            <text class="result-desc">{{ item.description }}</text>
            <view class="result-stats">
              <text class="result-author">{{ item.author }}</text>
              <view class="stats">
                <text class="fas fa-eye"></text>
                <text class="stats-count">{{ item.views }}</text>
              </view>
            </view>
          </view>
        </view>
        <!-- 加载更多 -->
        <view class="loading" v-if="isLoading">
          <text class="fas fa-spinner fa-spin"></text>
          <text class="loading-text">加载中...</text>
    </view>
      </template>
    </scroll-view>
  </view>
</template>

<script>
import { searchApi, contentApi } from '@/utils/api.js'

export default {
  data() {
    return {
      // 搜索文本
      searchText: '',
      // 搜索历史（从本地存储加载）
      searchHistory: [],
      // 热门搜索
      hotSearchList: [],
      // 搜索结果
      searchResults: [],
      // 加载状态
      isLoading: false,
      // 分页信息
      currentPage: 1,
      hasMore: true,
      pageSize: 10,
      
      // 推荐标签
      suggestionTags: [
        '科普动画',
        '儿童故事',
        '趣味实验',
        '手工教程',
        '益智游戏'
      ],
      
      // 推荐视频
      recommendedVideos: []
    }
  },
  onLoad() {
    console.log('[儿童端视频搜索] 页面初始化')
    this.loadSearchHistory()
    this.loadHotSearchKeywords()
    this.loadRecommendedVideos()
  },
  methods: {
    // 从本地存储加载搜索历史
    loadSearchHistory() {
      try {
        const history = uni.getStorageSync('video_search_history')
        if (history && Array.isArray(history)) {
          this.searchHistory = history
          console.log('[儿童端视频搜索] 搜索历史加载成功：', history)
        }
      } catch (error) {
        console.error('[儿童端视频搜索] 加载搜索历史失败：', error)
      }
    },
    
    // 保存搜索历史到本地存储
    saveSearchHistory() {
      try {
        uni.setStorageSync('video_search_history', this.searchHistory)
        console.log('[儿童端视频搜索] 搜索历史保存成功')
      } catch (error) {
        console.error('[儿童端视频搜索] 保存搜索历史失败：', error)
      }
    },
    
    // 加载热门搜索关键词
    async loadHotSearchKeywords() {
      try {
        console.log('[儿童端视频搜索] 开始加载热门视频关键词')
        // 获取视频内容（type=2）
        const response = await contentApi.getContentPage({
          current: 1,
          size: 5,
          type: 2, // 视频类型
          status: 1,
          sortField: 'view_count',
          sortOrder: 'desc'
        })
        
        if (response && response.data && response.data.records) {
          this.hotSearchList = response.data.records.map((item, index) => ({
            keyword: item.title || `热门视频${index + 1}`,
            count: this.formatViewCount(item.viewCount || 0)
          }))
          console.log('[儿童端视频搜索] 热门视频关键词加载成功：', this.hotSearchList)
      } else {
          // 使用默认热门搜索
          this.hotSearchList = [
            { keyword: '科普小实验', count: '8.5w' },
            { keyword: '趣味动画故事', count: '7.2w' },
            { keyword: '儿童手工制作', count: '6.8w' },
            { keyword: '益智游戏', count: '5.6w' },
            { keyword: '自然探索', count: '4.9w' }
          ]
          console.log('[儿童端视频搜索] 使用默认热门搜索关键词')
        }
      } catch (error) {
        console.error('[儿童端视频搜索] 加载热门搜索关键词失败：', error)
        // 使用默认热门搜索
        this.hotSearchList = [
          { keyword: '科普小实验', count: '8.5w' },
          { keyword: '趣味动画故事', count: '7.2w' },
          { keyword: '儿童手工制作', count: '6.8w' },
          { keyword: '益智游戏', count: '5.6w' },
          { keyword: '自然探索', count: '4.9w' }
        ]
      }
    },
    
    // 格式化浏览量显示
    formatViewCount(count) {
      if (!count || count === 0) return '0'
      if (count < 1000) return count.toString()
      if (count < 10000) return (count / 1000).toFixed(1) + 'k'
      return (count / 10000).toFixed(1) + 'w'
    },
    
    // 处理搜索
    async handleSearch() {
      const keyword = this.searchText.trim()
      if (!keyword) return
      
      try {
        console.log('[儿童端视频搜索] 开始搜索，关键词：', keyword)
        
        // 添加到搜索历史
        if (!this.searchHistory.includes(keyword)) {
          this.searchHistory.unshift(keyword)
          // 最多保存10条历史记录
          if (this.searchHistory.length > 10) {
            this.searchHistory.pop()
          }
          this.saveSearchHistory()
        }
        
        // 重置分页信息
        this.currentPage = 1
        this.hasMore = true
        this.searchResults = []
        this.isLoading = true
        
        // 调用搜索API
        const response = await searchApi.searchContent(keyword, this.currentPage, this.pageSize)
        
        if (response && response.data) {
          const { records, total, current, size } = response.data
          
          // 只保留视频类型的内容（type=2）
          const videoRecords = records.filter(item => item.type === 2)
          
          // 转换数据格式
          this.searchResults = videoRecords.map(item => ({
            id: item.id,
            cover: item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop',
            title: item.title || '无标题',
            description: this.extractDescription(item.content) || '暂无描述',
            author: item.creatorName || '匿名作者',
            views: this.formatViewCount(item.viewCount || 0),
            videoUrl: item.videoUrl,
            createdTime: item.createdTime
          }))
          
          // 更新分页信息
          this.currentPage = current
          this.hasMore = records.length === size && (current * size) < total
          
          console.log('[儿童端视频搜索] 搜索完成，找到', total, '条结果')
          
          if (this.searchResults.length === 0) {
            console.log('[儿童端视频搜索] 未找到搜索结果')
          }
        } else {
          console.warn('[儿童端视频搜索] 搜索响应格式异常：', response)
          this.searchResults = []
        }
        
      } catch (error) {
        console.error('[儿童端视频搜索] 搜索失败：', error)
        uni.showToast({
          title: '搜索失败，请重试',
          icon: 'none'
        })
        this.searchResults = []
      } finally {
        this.isLoading = false
      }
    },
    
    // 从HTML内容中提取纯文本描述
    extractDescription(htmlContent, maxLength = 80) {
      if (!htmlContent) return ''
      
      try {
        // 移除HTML标签
        const textContent = htmlContent.replace(/<[^>]*>/g, '').trim()
        
        // 截取指定长度
        if (textContent.length > maxLength) {
          return textContent.substring(0, maxLength) + '...'
        }
        
        return textContent
      } catch (error) {
        console.error('[儿童端视频搜索] 提取描述失败：', error)
        return ''
      }
    },
    
    // 清空搜索
    clearSearch() {
      this.searchText = ''
    },
    
    // 清空历史
    clearHistory() {
      this.searchHistory = []
      this.saveSearchHistory()
      console.log('[儿童端视频搜索] 搜索历史已清空')
    },
    
    // 使用历史搜索
    useHistorySearch(keyword) {
      this.searchText = keyword
      this.handleSearch()
    },
    
    // 返回上一页
    goBack() {
      if (this.searchText) {
        this.clearSearch()
      } else {
        uni.navigateBack()
      }
    },
    
    // 加载更多
    async loadMore() {
      if (this.isLoading || !this.hasMore || !this.searchText.trim()) return
      
      try {
        console.log('[儿童端视频搜索] 开始加载更多搜索结果，页码：', this.currentPage + 1)
        this.isLoading = true
        
        const response = await searchApi.searchContent(this.searchText.trim(), this.currentPage + 1, this.pageSize)
        
        if (response && response.data) {
          const { records, total, current, size } = response.data
          
          // 只保留视频类型的内容（type=2）
          const videoRecords = records.filter(item => item.type === 2)
          
          // 转换并追加新数据
          const newResults = videoRecords.map(item => ({
            id: item.id,
            cover: item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop',
            title: item.title || '无标题',
            description: this.extractDescription(item.content) || '暂无描述',
            author: item.creatorName || '匿名作者',
            views: this.formatViewCount(item.viewCount || 0),
            videoUrl: item.videoUrl,
            createdTime: item.createdTime
          }))
          
          this.searchResults.push(...newResults)
          
          // 更新分页信息
          this.currentPage = current
          this.hasMore = records.length === size && (current * size) < total
          
          console.log('[儿童端视频搜索] 加载更多完成，新增', records.length, '条结果')
        }
        
      } catch (error) {
        console.error('[儿童端视频搜索] 加载更多失败：', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      } finally {
        this.isLoading = false
      }
    },
    
    // 加载推荐视频
    async loadRecommendedVideos() {
      try {
        console.log('[儿童端视频搜索] 开始加载推荐视频（随机）')
        
        // 先获取第一页确定总页数
        const firstResponse = await contentApi.getContentPage({
          current: 1,
          size: 10,
          type: 2, // 视频类型
          status: 1,
          sortField: 'created_time',
          sortOrder: 'desc'
        })
        
        if (firstResponse && firstResponse.data) {
          const totalPages = firstResponse.data.pages || 1
          // 随机选择一页（最多前10页）
          const randomPage = totalPages > 1 ? Math.floor(Math.random() * Math.min(totalPages, 10)) + 1 : 1
          
          // 获取随机页的数据
          const response = randomPage === 1 ? firstResponse : await contentApi.getContentPage({
            current: randomPage,
            size: 10,
            type: 2,
            status: 1,
            sortField: 'created_time',
            sortOrder: 'desc'
          })
          
          if (response.data && response.data.records) {
            // 随机打乱并选择6个
            const shuffled = response.data.records.sort(() => Math.random() - 0.5).slice(0, 6)
            
            this.recommendedVideos = shuffled.map(item => ({
              id: item.id,
              cover: item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop',
              title: item.title || '无标题',
              author: item.creatorName || '匿名作者',
              views: this.formatViewCount(item.viewCount || 0),
              videoUrl: item.videoUrl
            }))
            
            console.log('[儿童端视频搜索] 推荐视频加载成功，共', this.recommendedVideos.length, '条')
          }
        }
      } catch (error) {
        console.error('[儿童端视频搜索] 加载推荐视频失败：', error)
      }
    },
    
    // 跳转到视频详情页面
    navigateToVideo(item) {
      console.log('[儿童端视频搜索] 点击视频，跳转到详情：', item)
      
      if (!item || !item.id) {
        uni.showToast({
          title: '视频信息异常',
          icon: 'none'
        })
        return
      }
      
      try {
        // 构造视频数据
        const videoData = {
          id: item.id,
          title: item.title,
          coverUrl: item.cover,
          videoUrl: item.videoUrl || '',
          creatorName: item.author || '未知作者',
          views: item.views || '0',
          likes: '0',
          creatorAvatar: '',
          followers: '0',
          description: item.description || ''
        }
        
        const videoInfo = encodeURIComponent(JSON.stringify(videoData))
        
        // 跳转到儿童端视频详情页
        uni.navigateTo({
          url: `/pages/child/video-detail?videoInfo=${videoInfo}`,
          fail: (err) => {
            console.error('[儿童端视频搜索] 跳转视频详情页失败：', err)
            uni.showToast({
              title: '跳转失败',
              icon: 'none'
            })
          }
        })
      } catch (error) {
        console.error('[儿童端视频搜索] 跳转页面异常：', error)
        uni.showToast({
          title: '跳转异常',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
}

/* 搜索头部 - 使用紫色主题 */
.search-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: #6366f1;
  padding: 20rpx 30rpx;
  z-index: 100;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 6px 12px;
  height: 32px;
}

.search-icon {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.search-input {
  flex: 1;
  margin: 0 8px;
  color: white;
  font-size: 14px;
  height: 20px;
  line-height: 20px;
}

.search-placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.clear-icon {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.cancel-btn {
  color: white;
  font-size: 14px;
  padding: 4px 0;
  min-width: 32px;
  text-align: center;
}

/* 搜索内容区域 */
.search-content {
  margin-top: 56px;
  padding: 16px;
}

.section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

.clear-history {
  color: #6b7280;
  font-size: 14px;
}

/* 搜索历史标签 */
.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag {
  padding: 6px 12px;
  background-color: white;
  border-radius: 16px;
  font-size: 13px;
  color: #4b5563;
  transition: all 0.2s ease;
}

.history-tag:active {
  background-color: #f3f4f6;
}

/* 热门搜索列表 */
.hot-search-list {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
}

.hot-search-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 12px;
  transition: background-color 0.2s ease;
}

.hot-search-item:active {
  background-color: #f9fafb;
}

.hot-search-rank {
  width: 20px;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
}

.top-rank {
  color: #8b5cf6;
  font-weight: bold;
}

.hot-search-keyword {
  flex: 1;
  font-size: 14px;
  color: #1f2937;
}

.hot-search-count {
  font-size: 12px;
  color: #6b7280;
}

/* 推荐内容 */
.refresh-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6366f1;
  cursor: pointer;
}

.refresh-text .fas {
  font-size: 12px;
}

/* 推荐视频网格 - 瀑布流布局 */
.recommended-grid {
  column-count: 2;
  column-gap: 12px;
}

.recommended-item {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out;
  break-inside: avoid;
  margin-bottom: 12px;
  display: inline-block;
  width: 100%;
}

/* 实现错落效果：随机高度 */
.recommended-item:nth-child(6n+1) .recommended-cover {
  height: 180px;
}

.recommended-item:nth-child(6n+2) .recommended-cover {
  height: 160px;
}

.recommended-item:nth-child(6n+3) .recommended-cover {
  height: 200px;
}

.recommended-item:nth-child(6n+4) .recommended-cover {
  height: 140px;
}

.recommended-item:nth-child(6n+5) .recommended-cover {
  height: 170px;
}

.recommended-item:nth-child(6n) .recommended-cover {
  height: 190px;
}

.recommended-item:active {
  transform: scale(0.98);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
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

.video-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.4);
}

.video-badge .fas {
  color: white;
  font-size: 12px;
}

.recommended-info {
  padding: 12px;
}

.recommended-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.4;
  height: 19.6px;
  margin-bottom: 8px;
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
  font-size: 12px;
  color: #6b7280;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.recommended-stats {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.recommended-stats .fas {
  font-size: 12px;
  color: #9ca3af;
}

.stats-text {
  font-size: 12px;
  color: #9ca3af;
}

/* 搜索结果 */
.search-results {
  margin-top: 56px;
  height: calc(100vh - 56px);
  padding: 16px;
}

.no-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  text-align: center;
}

.no-result-icon {
  font-size: 80px;
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.no-result-title {
  font-size: 16px;
  color: #1f2937;
  margin-bottom: 8px;
  font-weight: 500;
}

.no-result-desc {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 24px;
}

.suggestion-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.suggestion-tag {
  padding: 6px 16px;
  background-color: #f3f4f6;
  border-radius: 16px;
  font-size: 13px;
  color: #6366f1;
  transition: all 0.2s ease;
}

.suggestion-tag:active {
  background-color: #e0e7ff;
}

/* 搜索结果列表 */
.result-item {
  display: flex;
  gap: 12px;
  background-color: white;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}

.result-item:active {
  background-color: #f9fafb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.result-cover {
  position: relative;
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.result-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-badge {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.9), rgba(99, 102, 241, 0.9));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.play-badge .fas {
  color: white;
  font-size: 16px;
  margin-left: 2px;
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.result-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-desc {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.result-author {
  font-size: 12px;
  color: #6b7280;
}

.stats {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 12px;
}

.stats .fas {
  font-size: 12px;
}

.stats-count {
  font-size: 12px;
}

/* 加载状态 */
.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #6366f1;
  gap: 8px;
}

.loading .fa-spinner {
  font-size: 18px;
}

.loading-text {
  font-size: 14px;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
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
    column-gap: 16px;
  }
  
  .recommended-item {
    margin-bottom: 16px;
  }
}

@media screen and (min-width: 1024px) {
  .recommended-grid {
    column-count: 4;
  }
}
</style> 
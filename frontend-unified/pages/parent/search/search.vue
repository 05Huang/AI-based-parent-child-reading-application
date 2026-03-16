<template>
  <view class="container">
    <!-- 搜索头部 -->
    <view class="search-header">
      <view class="search-box">
        <view class="search-input-wrapper">
          <text class="fas fa-search search-icon"></text>
          <input 
            type="text" 
            v-model="searchText"
            placeholder="搜索感兴趣的内容" 
            placeholder-class="search-placeholder" 
            class="search-input"
            confirm-type="search"
            @confirm="handleSearch"
            @input="handleInput"
            focus
          />
          <text v-if="searchText" class="fas fa-times-circle clear-icon" @click="clearSearch"></text>
        </view>
        <text class="cancel-btn" @click="goBack">取消</text>
      </view>
      
      <!-- 类型筛选 -->
      <view class="filter-bar">
        <view 
          class="filter-item" 
          :class="{ active: selectedType === 0 }"
          @click="selectType(0)"
        >全部</view>
        <view 
          class="filter-item" 
          :class="{ active: selectedType === 1 }"
          @click="selectType(1)"
        >文章</view>
        <view 
          class="filter-item" 
          :class="{ active: selectedType === 2 }"
          @click="selectType(2)"
        >视频</view>
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

      <!-- 推荐内容 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">推荐内容</text>
          <text class="refresh-text" @click="loadRecommendedContent(true)">
            <text class="fas fa-sync-alt"></text>
            换一换
          </text>
        </view>
        <view class="recommended-grid">
          <view 
            v-for="item in recommendedContent" 
            :key="item.id"
            class="recommended-item"
            @click="navigateToContent(item)"
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
        <image src="https://img.icons8.com/clouds/200/000000/search.png" class="no-result-image"></image>
        <text class="no-result-title">抱歉，没有找到相关内容</text>
        <text class="no-result-desc">尝试使用其他关键词搜索</text>
        
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
        <view class="result-item" v-for="(item, index) in searchResults" :key="index" @click="navigateToContent(item)">
          <image :src="item.cover" mode="aspectFill" class="result-image"></image>
          <view class="result-info">
            <text class="result-title">{{ item.title }}</text>
            <text class="result-desc">{{ item.description }}</text>
            <view class="result-stats">
              <text class="result-author">{{ item.author }}</text>
              <view class="stats">
                <text class="fas fa-eye"></text>
                <text class="stats-count">{{ item.views }}</text>
                <!-- 显示内容类型标识 -->
                <view class="content-type" :class="{ 'video-type': item.type === 2 }">
                  <text class="fas" :class="item.type === 2 ? 'fa-play' : 'fa-file-alt'"></text>
                  <text class="type-text">{{ item.type === 2 ? '视频' : '文章' }}</text>
                </view>
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

<script setup>
import { ref, onMounted, watch } from 'vue'
import { searchApi, contentApi, recommendationApi, userApi } from '@/utils/api.js'

// 搜索文本
const searchText = ref('')
// 搜索类型：0-全部，1-文章，2-视频
const selectedType = ref(0)
// 搜索历史（从本地存储加载）
const searchHistory = ref([])
// 搜索结果
const searchResults = ref([])
// 加载状态
const isLoading = ref(false)
// 分页信息
const currentPage = ref(1)
const hasMore = ref(true)
const pageSize = 10

// 推荐标签
const suggestionTags = ref([
  '亲子教育',
  '情绪管理',
  '早教游戏',
  '儿童成长',
  '阅桥亲子阅读APP'
])

// 推荐内容
const recommendedContent = ref([])
const currentUser = ref(null)

// 页面初始化
onMounted(async () => {
  console.log('搜索页面初始化')
  loadSearchHistory()
  await loadCurrentUser()
  await loadRecommendedContent(true)
})

const loadCurrentUser = async () => {
  try {
    const token = uni.getStorageSync('token')
    const isLoggedIn = uni.getStorageSync('isLoggedIn')
    if (!token || !isLoggedIn) {
      currentUser.value = null
      return
    }

    const userResponse = await userApi.getCurrentUser()
    if (userResponse && userResponse.data) {
      currentUser.value = userResponse.data
    } else {
      currentUser.value = null
    }
  } catch (error) {
    currentUser.value = null
  }
}

// 选择搜索类型
const selectType = (type) => {
  if (selectedType.value === type) return
  selectedType.value = type
  if (searchText.value) {
    handleSearch()
  }
}

// 从本地存储加载搜索历史
const loadSearchHistory = () => {
  try {
    const history = uni.getStorageSync('search_history')
    if (history && Array.isArray(history)) {
      searchHistory.value = history
      console.log('搜索历史加载成功：', history)
    }
  } catch (error) {
    console.error('加载搜索历史失败：', error)
  }
}

// 保存搜索历史到本地存储
const saveSearchHistory = () => {
  try {
    uni.setStorageSync('search_history', searchHistory.value)
    console.log('搜索历史保存成功')
  } catch (error) {
    console.error('保存搜索历史失败：', error)
  }
}

// 格式化浏览量显示
const formatViewCount = (count) => {
  if (!count || count === 0) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + '万'
}

// 防抖定时器
let debounceTimer = null

// 监听搜索文本变化实现实时搜索
watch(searchText, (newVal) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  
  debounceTimer = setTimeout(() => {
    if (newVal && newVal.trim()) {
      // 传入 true 表示是实时搜索，不保存历史记录
      handleSearch(true)
    }
  }, 500)
})

// 处理搜索
const handleSearch = async (isRealTime = false) => {
  const keyword = searchText.value.trim()
  if (!keyword) return
  
  // 确保 isRealTime 是布尔值（因为 @confirm 事件会传入事件对象）
  const isAutoSearch = isRealTime === true
  
  try {
    console.log('开始搜索，关键词：', keyword, '自动搜索：', isAutoSearch)
    
    // 只有非自动搜索（点击回车或搜索按钮）时才添加到搜索历史
    if (!isAutoSearch && !searchHistory.value.includes(keyword)) {
      searchHistory.value.unshift(keyword)
      // 最多保存10条历史记录
      if (searchHistory.value.length > 10) {
        searchHistory.value.pop()
      }
      saveSearchHistory()
    }
    
    // 重置分页信息
    currentPage.value = 1
    hasMore.value = true
    searchResults.value = []
    isLoading.value = true
    
    // 调用搜索API
    const response = await searchApi.searchContent(keyword, currentPage.value, pageSize, selectedType.value)
    
    if (response && response.data) {
      let { records, total, current, size } = response.data
      
      // 前端兜底过滤：如果后端未正确过滤，前端再次过滤
      if (selectedType.value !== 0) {
        records = records.filter(item => item.type === selectedType.value)
      }
      
      // 转换数据格式以适配模板
      searchResults.value = records.map(item => ({
        id: item.id,
        cover: item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop',
        title: item.title || '无标题',
        description: extractDescription(item.content) || '暂无描述',
        author: item.creatorName || '匿名作者',
        views: formatViewCount(item.viewCount || 0),
        type: item.type || 1, // 1: 文章, 2: 视频
        createdTime: item.createdTime
      }))
      
      // 更新分页信息
      currentPage.value = current
      hasMore.value = records.length === size && (current * size) < total
      
      console.log('搜索完成，找到', total, '条结果，当前页：', current)
      
      if (searchResults.value.length === 0) {
        console.log('未找到搜索结果')
      }
    } else {
      console.warn('搜索响应格式异常：', response)
      searchResults.value = []
    }
    
  } catch (error) {
    console.error('搜索失败：', error)
    uni.showToast({
      title: '搜索失败，请重试',
      icon: 'none'
    })
    searchResults.value = []
  } finally {
    isLoading.value = false
  }
}

// 从HTML内容中提取纯文本描述
const extractDescription = (htmlContent, maxLength = 100) => {
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
    console.error('提取描述失败：', error)
    return ''
  }
}

// 清空搜索
const clearSearch = () => {
  searchText.value = ''
}

// 清空历史
const clearHistory = () => {
  searchHistory.value = []
  saveSearchHistory()
  console.log('搜索历史已清空')
}

// 使用历史搜索
const useHistorySearch = (keyword) => {
  searchText.value = keyword
  handleSearch()
}

// 返回上一页
const goBack = () => {
  if (searchText.value) {
    clearSearch()
  } else {
    uni.navigateBack()
  }
}

// 加载更多
const loadMore = async () => {
  if (isLoading.value || !hasMore.value || !searchText.value.trim()) return
  
  try {
    console.log('开始加载更多搜索结果，页码：', currentPage.value + 1)
    isLoading.value = true
    
    const response = await searchApi.searchContent(searchText.value.trim(), currentPage.value + 1, pageSize, selectedType.value)
    
    if (response && response.data) {
      const { records, total, current, size } = response.data
      
      // 转换并追加新数据
      const newResults = records.map(item => ({
        id: item.id,
        cover: item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop',
        title: item.title || '无标题',
        description: extractDescription(item.content) || '暂无描述',
        author: item.creatorName || '匿名作者',
        views: formatViewCount(item.viewCount || 0),
        type: item.type || 1,
        createdTime: item.createdTime
      }))
      
      searchResults.value.push(...newResults)
      
      // 更新分页信息
      currentPage.value = current
      hasMore.value = records.length === size && (current * size) < total
      
      console.log('加载更多完成，新增', records.length, '条结果')
    }
    
  } catch (error) {
    console.error('加载更多失败：', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

const mapToRecommendedItem = (item) => ({
  id: item.id,
  cover: item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop',
  title: item.title || '无标题',
  author: item.creatorName || '匿名作者',
  views: formatViewCount(item.viewCount || 0),
  type: item.type || 1
})

const loadRecommendedContentFallback = async (excludeIds = []) => {
  try {
    console.log('开始加载推荐内容（混合文章和视频）')
    
    // 并行请求文章和视频，各获取一页数据用于随机选择
    // 使用 Promise.all 并行请求提高效率
    const [articleRes, videoRes] = await Promise.all([
      contentApi.getContentPage({
        current: 1,
        size: 20, // 获取较多数据用于随机
        status: 1,
        type: 1, // 明确请求文章
        sortField: 'created_time',
        sortOrder: 'desc'
      }),
      contentApi.getContentPage({
        current: 1,
        size: 20,
        status: 1,
        type: 2, // 明确请求视频
        sortField: 'created_time',
        sortOrder: 'desc'
      })
    ])
    
    let articles = []
    let videos = []
    
    // 处理文章响应
    if (articleRes && articleRes.data && articleRes.data.records) {
      // 再次确认类型，防止后端未过滤
      articles = articleRes.data.records.filter(item => !item.type || item.type === 1)
    }
    
    // 处理视频响应
    if (videoRes && videoRes.data && videoRes.data.records) {
      // 再次确认类型
      videos = videoRes.data.records.filter(item => item.type === 2)
    }
    
    // 随机选择：3篇不到则全选，超过3篇则随机选3篇
    const selectedArticles = articles.sort(() => Math.random() - 0.5).slice(0, 3)
    const selectedVideos = videos.sort(() => Math.random() - 0.5).slice(0, 3)
    
    // 合并结果
    let combined = [...selectedArticles, ...selectedVideos]
    
    // 如果总数不足6个，尝试补充
    if (combined.length < 6) {
      const remainingCount = 6 - combined.length
      // 优先从剩余文章中补充
      const remainingArticles = articles.filter(a => !selectedArticles.includes(a))
      if (remainingArticles.length > 0) {
        combined = [...combined, ...remainingArticles.slice(0, remainingCount)]
      }
      
      // 如果还不够，从剩余视频中补充
      if (combined.length < 6) {
        const stillNeed = 6 - combined.length
        const remainingVideos = videos.filter(v => !selectedVideos.includes(v))
        if (remainingVideos.length > 0) {
          combined = [...combined, ...remainingVideos.slice(0, stillNeed)]
        }
      }
    }
    
    // 最终打乱顺序
    const shuffled = combined.sort(() => Math.random() - 0.5)
    
    const filtered = excludeIds.length
      ? shuffled.filter(i => !excludeIds.includes(i.id))
      : shuffled

    const result = filtered.slice(0, 6).map(mapToRecommendedItem)
    console.log('推荐内容加载成功，共', result.length, '条')
    return result
    
  } catch (error) {
    console.error('加载推荐内容失败：', error)
    return []
  }
}

// 加载推荐内容 - 使用Python推荐系统，失败则降级
const loadRecommendedContent = async (shuffle = false) => {
  try {
    console.log('======================================')
    console.log('🎯 [搜索页] 开始加载推荐内容（使用Python推荐系统）')
    console.log('======================================')
    console.log('📊 推荐参数：')
    console.log('  - 用户ID:', currentUser.value?.id)
    console.log('  - 文章数量: 3')
    console.log('  - 视频数量: 3')
    console.log('  - shuffle:', shuffle)

    if (!currentUser.value?.id) {
      console.warn('⚠️ [搜索页] 用户未登录，使用降级方案')
      recommendedContent.value = await loadRecommendedContentFallback()
      return
    }

    const [articleResult, videoResult] = await Promise.allSettled([
      recommendationApi.getArticleRecommendations(currentUser.value.id, 3, shuffle),
      recommendationApi.getVideoRecommendations(currentUser.value.id, 3, shuffle)
    ])

    const combined = []
    if (articleResult.status === 'fulfilled' && articleResult.value?.data && Array.isArray(articleResult.value.data)) {
      combined.push(...articleResult.value.data)
    }
    if (videoResult.status === 'fulfilled' && videoResult.value?.data && Array.isArray(videoResult.value.data)) {
      combined.push(...videoResult.value.data)
    }

    if (combined.length === 0) {
      console.warn('⚠️ [搜索页] 推荐返回为空，使用降级方案')
      recommendedContent.value = await loadRecommendedContentFallback()
      return
    }

    const mapped = combined.map(mapToRecommendedItem)
    const shuffled = mapped.sort(() => Math.random() - 0.5)
    recommendedContent.value = shuffled.slice(0, 6)

    if (recommendedContent.value.length < 6) {
      const excludeIds = recommendedContent.value.map(i => i.id)
      const extra = await loadRecommendedContentFallback(excludeIds)
      recommendedContent.value = [...recommendedContent.value, ...extra].slice(0, 6)
    }

    console.log('✅ [搜索页] 推荐内容加载完成：', recommendedContent.value.length)
  } catch (error) {
    console.error('❌ [搜索页] 加载推荐内容失败，启动降级方案：', error?.message || error)
    recommendedContent.value = await loadRecommendedContentFallback()
  }
}

// 跳转到内容详情页面
const navigateToContent = (item) => {
  console.log('点击搜索结果，跳转到内容详情：', item)
  
  if (!item || !item.id) {
    uni.showToast({
      title: '内容信息异常',
      icon: 'none'
    })
    return
  }
  
  try {
    if (item.type === 2) {
      // 视频内容，跳转到视频播放页面
      uni.navigateTo({
        url: `/pages/parent/video/video-player?id=${item.id}`,
        fail: (err) => {
          console.error('跳转视频播放页面失败：', err)
          uni.showToast({
            title: '跳转失败',
            icon: 'none'
          })
        }
      })
    } else {
      // 文章内容，跳转到阅读页面
      uni.navigateTo({
        url: `/pages/parent/reading/reading?id=${item.id}`,
        fail: (err) => {
          console.error('跳转阅读页面失败：', err)
          uni.showToast({
            title: '跳转失败',
            icon: 'none'
          })
        }
      })
    }
  } catch (error) {
    console.error('跳转页面异常：', error)
    uni.showToast({
      title: '跳转异常',
      icon: 'none'
    })
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

.search-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: #3b82f6;
  padding: 20rpx 30rpx 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-bar {
  display: flex;
  gap: 20px;
  padding-bottom: 10px;
}

.filter-item {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  padding-bottom: 4px;
  position: relative;
}

.filter-item.active {
  color: #fff;
  font-weight: 500;
}

.filter-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #fff;
  border-radius: 2px;
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

.search-content {
  margin-top: 100px;
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
}

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
}

.hot-search-rank {
  width: 20px;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
}

.top-rank {
  color: #ef4444;
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

.search-results {
  margin-top: 100px;
  height: calc(100vh - 100px);
  padding: 16px;
}

.result-item {
  display: flex;
  gap: 12px;
  background-color: white;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.result-item:hover {
  background-color: #f9fafb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.result-item:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.result-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  flex-shrink: 0;
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

.content-type {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  background-color: #f3f4f6;
  border-radius: 8px;
  font-size: 10px;
}

.content-type.video-type {
  background-color: #fef3c7;
  color: #d97706;
}

.content-type .fas {
  font-size: 8px;
}

.type-text {
  font-size: 10px;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #6b7280;
  gap: 8px;
}

.loading-text {
  font-size: 14px;
}

/* 无搜索结果样式 */
.no-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  text-align: center;
}

.no-result-image {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 24px;
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
  color: #3b82f6;
  transition: all 0.2s ease;
}

.suggestion-tag:active {
  background-color: #dbeafe;
}

/* 推荐内容样式 */
.refresh-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #3b82f6;
  cursor: pointer;
}

.refresh-text .fas {
  font-size: 12px;
}

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

.content-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
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

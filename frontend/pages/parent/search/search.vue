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
          <text class="section-title">热门搜索</text>
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
import { ref, onMounted } from 'vue'
import { searchApi } from '@/utils/api.js'

// 搜索文本
const searchText = ref('')
// 搜索历史（从本地存储加载）
const searchHistory = ref([])
// 热门搜索
const hotSearchList = ref([])
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
  '亲子阅读'
])

// 页面初始化
onMounted(() => {
  console.log('搜索页面初始化')
  loadSearchHistory()
  loadHotSearchKeywords()
})

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

// 加载热门搜索关键词
const loadHotSearchKeywords = async () => {
  try {
    console.log('开始加载热门搜索关键词')
    const response = await searchApi.getHotSearchKeywords(5)
    
    if (response && response.data && Array.isArray(response.data)) {
      // 从热门内容中提取关键词
      hotSearchList.value = response.data.map((item, index) => ({
        keyword: item.title || `热门内容${index + 1}`,
        count: formatViewCount(item.viewCount || 0)
      }))
      console.log('热门搜索关键词加载成功：', hotSearchList.value)
    } else {
      // 使用默认热门搜索
      hotSearchList.value = [
        { keyword: '亲子教育方法', count: '12.5w' },
        { keyword: '儿童情绪管理', count: '10.2w' },
        { keyword: '早教启蒙', count: '9.8w' },
        { keyword: '亲子阅读', count: '8.6w' },
        { keyword: '家庭教育', count: '7.9w' }
      ]
      console.log('使用默认热门搜索关键词')
    }
  } catch (error) {
    console.error('加载热门搜索关键词失败：', error)
    // 使用默认热门搜索
    hotSearchList.value = [
      { keyword: '亲子教育方法', count: '12.5w' },
      { keyword: '儿童情绪管理', count: '10.2w' },
      { keyword: '早教启蒙', count: '9.8w' },
      { keyword: '亲子阅读', count: '8.6w' },
      { keyword: '家庭教育', count: '7.9w' }
    ]
  }
}

// 格式化浏览量显示
const formatViewCount = (count) => {
  if (!count || count === 0) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + '万'
}

// 处理搜索
const handleSearch = async () => {
  const keyword = searchText.value.trim()
  if (!keyword) return
  
  try {
    console.log('开始搜索，关键词：', keyword)
    
    // 添加到搜索历史
    if (!searchHistory.value.includes(keyword)) {
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
    const response = await searchApi.searchContent(keyword, currentPage.value, pageSize)
    
    if (response && response.data) {
      const { records, total, current, size } = response.data
      
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
    
    const response = await searchApi.searchContent(searchText.value.trim(), currentPage.value + 1, pageSize)
    
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
  margin-top: 56px;
  height: calc(100vh - 56px);
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
</style> 
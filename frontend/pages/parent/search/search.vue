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
      <view v-if="searchText === '#提示' || (searchResults.length === 0 && !isLoading)" class="no-result">
        <image src="https://img.icons8.com/clouds/200/000000/search.png" class="no-result-image"></image>
        <text class="no-result-title">抱歉，没有找到相关内容</text>
        <text class="no-result-desc">换个关键词试试看？</text>
        
        <!--  <view class="suggestion-tags">
          <text class="suggestion-tag" 
                v-for="(tag, index) in suggestionTags" 
                :key="index"
                @click="useHistorySearch(tag)">
            {{ tag }}
          </text>
        </view>
          -->
      </view>

      <!-- 搜索结果列表 -->
      <template v-else>
        <view class="result-item" v-for="(item, index) in searchResults" :key="index">
          <image :src="item.cover" mode="aspectFill" class="result-image"></image>
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

<script setup>
import { ref, onMounted } from 'vue'

// 搜索文本
const searchText = ref('')
// 搜索历史
const searchHistory = ref([
  '亲子教育',
  '早教方法',
  '儿童心理',
  '育儿经验'
])
// 热门搜索
const hotSearchList = ref([
  { keyword: '如何培养孩子的专注力', count: '12.5w' },
  { keyword: '儿童情绪管理', count: '10.2w' },
  { keyword: '亲子阅读技巧', count: '9.8w' },
  { keyword: '孩子教育方法', count: '8.6w' },
  { keyword: '亲子互动游戏', count: '7.9w' }
])
// 搜索结果
const searchResults = ref([])
// 加载状态
const isLoading = ref(false)

// 推荐标签
const suggestionTags = ref([
  '亲子教育',
  '情绪管理',
  '早教游戏',
  '儿童成长',
  '亲子阅读'
])

// 处理搜索
const handleSearch = () => {
  if (!searchText.value.trim()) return
  // 添加到搜索历史
  if (!searchHistory.value.includes(searchText.value)) {
    searchHistory.value.unshift(searchText.value)
    // 最多保存10条历史记录
    if (searchHistory.value.length > 10) {
      searchHistory.value.pop()
    }
  }
  // TODO: 调用搜索API
  mockSearchResults()
}

// 清空搜索
const clearSearch = () => {
  searchText.value = ''
}

// 清空历史
const clearHistory = () => {
  searchHistory.value = []
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
const loadMore = () => {
  if (isLoading.value) return
  isLoading.value = true
  // TODO: 调用加载更多API
  setTimeout(() => {
    mockLoadMore()
    isLoading.value = false
  }, 1000)
}

// Mock搜索结果数据
const mockSearchResults = () => {
  searchResults.value = [
    {
      cover: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop',
      title: '如何在3分钟内安抚孩子的小情绪',
      description: '专业育儿师分享快速有效的情绪管理技巧，让家长轻松应对孩子的闹情绪...',
      author: '育儿专家 王老师',
      views: '2.1万'
    },
    {
      cover: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&fit=crop',
      title: '培养孩子专注力的10个小游戏',
      description: '通过简单有趣的互动游戏，培养孩子的专注力和学习能力...',
      author: '儿童教育专家 李老师',
      views: '1.8万'
    }
  ]
}

// Mock加载更多数据
const mockLoadMore = () => {
  const moreResults = [
    {
      cover: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&fit=crop',
      title: '亲子阅读的正确打开方式',
      description: '科学的亲子阅读方法，让阅读成为孩子最期待的事情...',
      author: '阅读指导师 周老师',
      views: '1.5万'
    }
  ]
  searchResults.value.push(...moreResults)
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
  gap: 4px;
  color: #6b7280;
  font-size: 12px;
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
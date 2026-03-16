<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="header-left">
          <view class="back-btn" @click="goBack">
            <text class="fas fa-arrow-left"></text>
          </view>
          <text class="header-title">全部文章</text>
        </view>
        <view class="header-right">
          <view class="search-btn" @click="navigateToSearch">
            <text class="fas fa-search"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 文章列表 -->
    <scroll-view 
      scroll-y="true" 
      class="articles-container"
      @scrolltolower="loadMoreArticles"
      lower-threshold="100"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      :style="{ marginTop: headerHeight + 'px', height: `calc(100vh - ${headerHeight}px)` }"
    >
      <!-- 加载状态 -->
      <view v-if="loading && articles.length === 0" class="loading-container">
        <view class="loading-item">
          <text class="fas fa-spinner fa-spin loading-icon"></text>
          <text class="loading-text">加载中...</text>
        </view>
      </view>
      
      <!-- 文章网格 -->
      <view v-else class="articles-grid">
        <view 
          v-for="article in articles" 
          :key="article.id"
          class="article-item"
          @click="navigateToReading(article)"
        >
          <view class="article-cover">
            <image 
              :src="article.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop'" 
              mode="aspectFill"
              class="cover-image"
            ></image>
          </view>
          <view class="article-info">
            <text class="article-title">{{ article.title }}</text>
            <view class="article-meta">
              <text class="article-author">{{ article.creatorName || '匿名作者' }}</text>
              <view class="article-stats">
                <text class="fas fa-eye"></text>
                <text class="stats-count">{{ formatViewCount(article.viewCount) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 无数据提示 -->
      <view v-if="!loading && articles.length === 0" class="no-data">
        <text class="fas fa-book-open no-data-icon"></text>
        <text class="no-data-text">暂无文章</text>
      </view>
      
      <!-- 加载更多 -->
      <view v-if="loading && articles.length > 0" class="load-more">
        <text class="fas fa-spinner fa-spin"></text>
        <text class="load-more-text">加载更多...</text>
      </view>
      
      <!-- 没有更多数据 -->
      <view v-if="!hasMore && articles.length > 0" class="no-more">
        <text class="no-more-text">没有更多文章了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { contentApi } from '@/utils/api.js'

// 状态栏高度
const statusBarHeight = ref(20)
const headerHeight = ref(80)

// 响应式数据
const articles = ref([])
const loading = ref(false)
const isRefreshing = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)

// 页面加载时获取文章列表
onMounted(async () => {
  // 获取状态栏高度
  try {
    const systemInfo = uni.getSystemInfoSync()
    statusBarHeight.value = systemInfo.statusBarHeight || 20
    headerHeight.value = statusBarHeight.value + 56 // 56px 是 header-content 的高度
  } catch (e) {
    console.error('获取系统信息失败', e)
  }

  console.log('全部文章页面初始化')
  await loadArticles()
})

// 加载文章列表
const loadArticles = async (refresh = false, page = null) => {
  if (loading.value && !refresh) {
    console.log('已有请求在进行中，跳过')
    return
  }
  
  try {
    loading.value = true
    const requestPage = refresh ? 1 : (page || currentPage.value)
    console.log('开始加载文章列表，请求页码：', requestPage, '刷新模式：', refresh)
    
    const response = await contentApi.getContentPage({
      current: requestPage,
      size: pageSize.value,
      status: 1, // 只查询正常状态的内容
      type: 1, // 只查询文章内容，不包含视频（type=2）
      sortField: 'created_time',
      sortOrder: 'desc'
    })
    
    console.log('API响应：', response)
    
    if (response && response.data) {
      const { records, current, total, size, pages } = response.data
      const resolvedSize = size || pageSize.value
      const totalPages = pages || Math.ceil((total || 0) / resolvedSize) || 1
      
      console.log('收到数据：', {
        recordsCount: records?.length || 0,
        current: current,
        total: total,
        pages: totalPages,
        hasMore: current < totalPages
      })
      
      if (refresh) {
        // 刷新模式：替换所有数据
        articles.value = records || []
        currentPage.value = current || 1
        console.log('刷新完成，重置为第1页，共', articles.value.length, '条数据')
      } else {
        // 加载更多模式：追加数据
        const oldCount = articles.value.length
        articles.value.push(...(records || []))
        console.log('追加数据，从', oldCount, '条增加到', articles.value.length, '条')
        currentPage.value = current || requestPage
      }
      
      hasMore.value = (current || requestPage) < totalPages
      
      console.log('文章列表加载成功，总数据：', articles.value.length, '条，当前页：', currentPage.value, '总页数：', totalPages, '还有更多：', hasMore.value)
    } else {
      console.error('响应数据格式错误：', response)
    }
  } catch (error) {
    console.error('加载文章列表失败：', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  console.log('下拉刷新文章列表')
  isRefreshing.value = true
  loadArticles(true)
}

// 加载更多文章
const loadMoreArticles = () => {
  console.log('触发加载更多，当前状态：', {
    loading: loading.value,
    hasMore: hasMore.value,
    currentPage: currentPage.value,
    articlesCount: articles.value.length
  })
  
  if (loading.value) {
    console.log('正在加载中，跳过本次请求')
    return
  }
  
  if (!hasMore.value) {
    console.log('没有更多数据了')
    return
  }
  
  const nextPage = currentPage.value + 1
  console.log('开始加载下一页：', nextPage)
  loadArticles(false, nextPage)
}

// 格式化浏览量
const formatViewCount = (count) => {
  if (!count) return '0'
  if (count > 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  return count.toString()
}

// 返回上一页
const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack({
      delta: 1
    })
  } else {
    uni.switchTab({
      url: '/pages/child/home'
    })
  }
}

// 跳转到搜索页
const navigateToSearch = () => {
  uni.navigateTo({
    url: '/pages/child/search/search'
  })
}

// 跳转到阅读页
const navigateToReading = (article) => {
  if (!article || !article.id) return
  uni.navigateTo({
    url: `/pages/child/article-detail?id=${article.id}`
  })
}
</script>

<style>
/* 引入 Font Awesome 图标库 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #6366f1, #8b5cf6); /* 适配孩子端主题色 */
  color: #fff;
  box-shadow: 0 2px 10px rgba(99, 102, 241, 0.2);
}

.header-content {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.header-left {
  display: flex;
  align-items: center;
}

.back-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 20px;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
}

.search-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

/* 文章列表容器 */
.articles-container {
  width: 100%;
  box-sizing: border-box;
  padding: 16px;
}

/* 文章网格 */
.articles-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding-bottom: 20px;
  width: 100%;
  box-sizing: border-box;
}

.article-item {
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.article-item:active {
  transform: scale(0.98);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.article-cover {
  position: relative;
  width: 100%;
  height: 120px;
  overflow: hidden;
  background: #f0f0f0;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-info {
  padding: 12px;
}

.article-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.4;
  height: 39.2px; /* 固定高度：14px * 1.4 * 2 = 39.2px (2行) */
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-author {
  font-size: 12px;
  color: #6b7280;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-stats .fas {
  font-size: 12px;
  color: #9ca3af;
}

.stats-count {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 0;
}

/* 响应式设计 */
@media screen and (min-width: 768px) {
  .articles-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (min-width: 1024px) {
  .articles-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

/* 动画效果 */
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

/* 加载动画 */
.fa-spin {
  animation: fa-spin 1s infinite linear;
}

@keyframes fa-spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

.loading-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
}

.loading-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.loading-text {
  font-size: 14px;
}

/* 无数据提示 */
.no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #ccc;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.no-data-text {
  font-size: 16px;
}

/* 加载更多 */
.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0;
  color: #999;
  font-size: 13px;
}

.load-more .fa-spinner {
  margin-right: 6px;
}

.no-more {
  text-align: center;
  padding: 16px 0;
  color: #ccc;
  font-size: 12px;
}
</style>

<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <view class="back-btn" @click="goBack">
            <text class="fas fa-arrow-left"></text>
          </view>
          <text class="header-title">全部视频</text>
        </view>
        <view class="header-right">
          <view class="search-btn" @click="navigateToSearch">
            <text class="fas fa-search"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 视频列表 -->
    <scroll-view 
      scroll-y="true" 
      class="videos-container"
      @scrolltolower="loadMoreVideos"
      lower-threshold="100"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 加载状态 -->
      <view v-if="loading && videos.length === 0" class="loading-container">
        <view class="loading-item">
          <text class="fas fa-spinner fa-spin loading-icon"></text>
          <text class="loading-text">加载中...</text>
        </view>
      </view>
      
      <!-- 视频网格 -->
      <view v-else class="videos-grid">
        <view 
          v-for="video in videos" 
          :key="video.id"
          class="video-item"
          @click="navigateToVideoPlayer(video)"
        >
          <view class="video-cover">
            <image 
              :src="video.coverUrl || 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60'" 
              mode="aspectFill"
              class="cover-image"
            ></image>
            <!-- 视频时长 -->
            <view class="video-duration">
              <text>{{ formatDuration(video.contentLength) }}</text>
            </view>
            <!-- 播放按钮 -->
            <view class="play-button">
              <text class="fas fa-play"></text>
            </view>
          </view>
          <view class="video-info">
            <text class="video-title">{{ video.title }}</text>
            <view class="video-meta">
              <text class="video-author">{{ video.creatorName || '匿名作者' }}</text>
              <view class="video-stats">
                <text class="fas fa-play-circle"></text>
                <text class="stats-count">{{ formatViewCount(video.viewCount) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 无数据提示 -->
      <view v-if="!loading && videos.length === 0" class="no-data">
        <text class="fas fa-video no-data-icon"></text>
        <text class="no-data-text">暂无视频</text>
      </view>
      
      <!-- 加载更多 -->
      <view v-if="loading && videos.length > 0" class="load-more">
        <text class="fas fa-spinner fa-spin"></text>
        <text class="load-more-text">加载更多...</text>
      </view>
      
      <!-- 没有更多数据 -->
      <view v-if="!hasMore && videos.length > 0" class="no-more">
        <text class="no-more-text">没有更多视频了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { videoApi } from '@/utils/api.js'

// 响应式数据
const videos = ref([])
const loading = ref(false)
const isRefreshing = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)

// 页面加载时获取视频列表
onMounted(async () => {
  console.log('全部视频页面初始化')
  await loadVideos()
})

// 加载视频列表
const loadVideos = async (refresh = false) => {
  if (loading.value && !refresh) {
    console.log('已有请求在进行中，跳过')
    return
  }
  
  try {
    loading.value = true
    const requestPage = refresh ? 1 : currentPage.value
    console.log('开始加载视频列表，请求页码：', requestPage, '刷新模式：', refresh)
    
    const response = await videoApi.getVideoPage({
      current: requestPage,
      size: pageSize.value,
      status: 1, // 只查询正常状态的内容
      type: 2, // 只查询视频内容
      sortField: 'created_time',
      sortOrder: 'desc'
    })
    
    console.log('API响应：', response)
    
    if (response && response.data) {
      const { records, current, total, pages } = response.data
      
      console.log('收到数据：', {
        recordsCount: records?.length || 0,
        current: current,
        total: total,
        pages: pages,
        hasMore: current < pages
      })
      
      if (refresh) {
        // 刷新模式：替换所有数据
        videos.value = records || []
        currentPage.value = 1
        console.log('刷新完成，重置为第1页，共', videos.value.length, '条数据')
      } else {
        // 加载更多模式：追加数据
        const oldCount = videos.value.length
        videos.value.push(...(records || []))
        console.log('追加数据，从', oldCount, '条增加到', videos.value.length, '条')
        // 不要覆盖 currentPage，因为已经在 loadMoreVideos 中递增了
      }
      
      hasMore.value = current < pages
      
      console.log('视频列表加载成功，总数据：', videos.value.length, '条，当前页：', currentPage.value, '总页数：', pages, '还有更多：', hasMore.value)
    } else {
      console.error('响应数据格式错误：', response)
    }
  } catch (error) {
    console.error('加载视频列表失败：', error)
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
  console.log('下拉刷新视频列表')
  isRefreshing.value = true
  loadVideos(true)
}

// 加载更多视频
const loadMoreVideos = () => {
  console.log('触发加载更多视频，当前状态：', {
    loading: loading.value,
    hasMore: hasMore.value,
    currentPage: currentPage.value,
    videosCount: videos.value.length
  })
  
  if (loading.value) {
    console.log('正在加载中，跳过本次请求')
    return
  }
  
  if (!hasMore.value) {
    console.log('没有更多视频了')
    return
  }
  
  console.log('开始加载下一页：', currentPage.value + 1)
  currentPage.value += 1
  loadVideos(false)
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
      url: '/pages/parent/video/video'
    })
  }
}

// 跳转到搜索页面
const navigateToSearch = () => {
  uni.navigateTo({
    url: '/pages/parent/search/search'
  })
}

// 跳转到视频播放页面
const navigateToVideoPlayer = (video) => {
  if (!video || !video.id) {
    console.error('视频信息不完整，无法跳转')
    return
  }
  
  console.log('跳转到视频播放页面，视频ID：', video.id)
  
  // 跳转到视频播放页面
  uni.navigateTo({
    url: `/pages/parent/video/video-player?id=${video.id}`
  })
}

// 格式化浏览量显示
const formatViewCount = (count) => {
  if (!count || count === 0) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + '万'
}

// 格式化视频时长
const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
}

/* 顶部导航栏 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: #3b82f6;
  z-index: 50;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn .fas {
  color: #ffffff;
  font-size: 18px;
}

.header-title {
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
}

.search-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn .fas {
  color: #ffffff;
  font-size: 18px;
}

/* 视频容器 */
.videos-container {
  margin-top: 56px;
  height: calc(100vh - 56px);
  padding: 16px 16px 16px 16px;
  box-sizing: border-box;
}

/* 视频网格布局 */
.videos-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding-bottom: 20px;
  width: 100%;
  box-sizing: border-box;
}

.video-item {
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;
}

.video-item:active {
  transform: scale(0.98);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.video-cover {
  position: relative;
  width: 100%;
  height: 120px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-duration {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background-color: rgba(0, 0, 0, 0.8);
  color: #ffffff;
  padding: 2px 5px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 500;
}

.play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 36px;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.play-button .fas {
  color: #ffffff;
  font-size: 14px;
  margin-left: 2px; /* 微调播放图标位置 */
}

.video-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.video-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.4;
  height: 19.6px; /* 固定高度：14px * 1.4 * 1 = 19.6px (1行) */
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.video-author {
  font-size: 12px;
  color: #6b7280;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-stats {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-stats .fas {
  font-size: 12px;
  color: #9ca3af;
}

.stats-count {
  font-size: 12px;
  color: #9ca3af;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.loading-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-icon {
  font-size: 24px;
  color: #3b82f6;
}

.loading-text {
  font-size: 14px;
  color: #6b7280;
}

/* 加载更多 */
.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 20px;
  color: #6b7280;
}

.load-more .fas {
  font-size: 16px;
}

.load-more-text {
  font-size: 14px;
}

/* 没有更多数据 */
.no-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.no-more-text {
  font-size: 14px;
  color: #9ca3af;
}

/* 无数据状态 */
.no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 12px;
}

.no-data-icon {
  font-size: 48px;
  color: #d1d5db;
}

.no-data-text {
  font-size: 16px;
  color: #9ca3af;
}

/* 响应式设计 */
@media screen and (min-width: 768px) {
  .videos-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (min-width: 1024px) {
  .videos-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

/* 动画效果 */
.video-item {
  animation: fadeInUp 0.6s ease-out;
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

/* 播放按钮悬停效果 */
.video-item:hover .play-button {
  background-color: rgba(0, 0, 0, 0.8);
  transform: translate(-50%, -50%) scale(1.05);
}

.video-item:hover .play-button .fas {
  color: #ffffff;
}
</style> 
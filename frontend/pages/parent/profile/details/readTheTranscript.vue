<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">浏览记录</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 统计卡片 -->
      <view class="stats-card">
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">328</text>
            <text class="stat-label">浏览总时长(分钟)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">42</text>
            <text class="stat-label">浏览文章</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">12</text>
            <text class="stat-label">本周浏览(天)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">85%</text>
            <text class="stat-label">互动率</text>
          </view>
        </view>
      </view>

      <!-- 浏览记录列表 -->
      <view class="reading-list">
        <view class="section-header">
          <text class="section-title">浏览记录</text>
          <view class="filter-btn">
            <text class="fas fa-filter"></text>
            <text class="filter-text">筛选</text>
          </view>
        </view>
        
        <view class="book-records">
          <view v-for="(record, index) in readingRecords" :key="index" class="book-record-item">
            <image :src="record.coverUrl" class="book-cover"></image>
            <view class="book-info">
              <view class="book-main-info">
                <text class="book-title">{{record.title}}</text>
                <text class="book-author">{{record.author}}</text>
                <view class="reading-progress">
                  <view class="stat">
                    <text class="fas fa-thumbs-up stat-icon"></text>
                    <text class="stat-text">{{record.likes}}赞</text>
                  </view>
                  <view class="stat">
                    <text class="fas fa-comment stat-icon"></text>
                    <text class="stat-text">{{record.comments}}评论</text>
                  </view>
                </view>
              </view>
              <view class="reading-stats">
                <view class="stat">
                  <text class="fas fa-clock stat-icon"></text>
                  <text class="stat-text">{{record.duration}}分钟前</text>
                </view>
                <view class="stat">
                  <text class="fas fa-tag stat-icon"></text>
                  <text class="stat-text">{{record.category}}</text>
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
import { ref } from 'vue'

// 时间范围选择器数据
const timeRanges = ['近7天', '近30天', '近3个月']
const currentTimeRange = ref('近7天')

// 模拟浏览记录数据
const readingRecords = ref([
  {
    title: '宝宝挑食怎么办？这些方法超有效',
    author: '育儿专家 小王',
    coverUrl: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
    likes: 256,
    comments: 45,
    duration: 30,
    category: '育儿经验',
    lastRead: '今天'
  },
  {
    title: '亲子互动游戏大全：激发孩子创造力',
    author: '早教老师 小李',
    coverUrl: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop',
    likes: 189,
    comments: 32,
    duration: 45,
    category: '亲子互动',
    lastRead: '昨天'
  },
  {
    title: '如何培养孩子的阅读习惯？',
    author: '教育专家 小张',
    coverUrl: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&auto=format&fit=crop',
    likes: 145,
    comments: 28,
    duration: 60,
    category: '教育心得',
    lastRead: '2天前'
  },
  {
    title: '儿童营养餐制作指南',
    author: '营养师 小陈',
    coverUrl: 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=400&auto=format&fit=crop',
    likes: 167,
    comments: 36,
    duration: 90,
    category: '营养健康',
    lastRead: '3天前'
  }
])

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 时间范围变化处理
const onTimeRangeChange = (e) => {
  currentTimeRange.value = timeRanges[e.detail.value]
  // 这里可以添加加载对应时间范围数据的逻辑
}
</script>

<style>
/* 引入Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  width: 100%;
  box-sizing: border-box;
}

/* 顶部导航样式 */
.nav-header {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
}

.nav-content {
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

.back-btn {
  padding: 20rpx;
  margin: -20rpx;
  margin-right: 10rpx;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
}

/* 主要内容区域 */
.main-content {
  margin-top: 88rpx;
  padding: 30rpx;
  height: calc(100vh - 88rpx);
  width: 100%;
  box-sizing: border-box;
}

/* 统计卡片样式 */
.stats-card {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.15);
  width: 100%;
  box-sizing: border-box;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
  width: 100%;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #ffffff;
}

.stat-value {
  font-size: 48rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 24rpx;
  opacity: 0.9;
}

/* 趋势图区域样式 */
.trend-section {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  width: 100%;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
}

.time-picker {
  font-size: 28rpx;
  color: #6b7280;
  background-color: #f3f4f6;
  padding: 8rpx 24rpx;
  border-radius: 9999rpx;
}

.trend-chart {
  height: 300rpx;
  width: 100%;
}

/* 阅读记录列表样式 */
.reading-list {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #6b7280;
  font-size: 28rpx;
}

.book-records {
  margin-top: 20rpx;
  width: 100%;
}

.book-record-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1px solid #e5e7eb;
  width: 100%;
  box-sizing: border-box;
}

.book-record-item:last-child {
  border-bottom: none;
}

.book-cover {
  width: 160rpx;
  height: 220rpx;
  border-radius: 12rpx;
  object-fit: cover;
  flex-shrink: 0;
}

.book-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.book-main-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 8rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
}

.reading-progress {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background-color: #ffffff;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
  margin: 10rpx 0;
}

.reading-stats {
  display: flex;
  gap: 30rpx;
  margin-top: 16rpx;
  background-color: #ffffff;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
}

.stat {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-icon {
  font-size: 24rpx;
  color: #60a5fa;
}

.stat-text {
  font-size: 24rpx;
  color: #6b7280;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style>

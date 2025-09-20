<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">浏览历史</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 统计卡片 -->
      <view class="stats-card">
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">156</text>
            <text class="stat-label">本月浏览(分钟)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">28</text>
            <text class="stat-label">浏览文章</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">15</text>
            <text class="stat-label">互动文章</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">8</text>
            <text class="stat-label">分享次数</text>
          </view>
        </view>
      </view>

      <!-- 历史记录列表 -->
      <view class="history-list">
        <view class="section-header">
          <text class="section-title">浏览记录</text>
          <view class="filter-btn">
            <text class="fas fa-filter"></text>
            <text class="filter-text">筛选</text>
          </view>
        </view>
        
        <view class="book-records">
          <view v-for="(record, index) in historyRecords" :key="index" class="book-record-item">
            <image :src="record.coverUrl" class="book-cover"></image>
            <view class="book-info">
              <view class="book-main-info">
                <text class="book-title">{{record.title}}</text>
                <text class="book-author">{{record.author}}</text>
                <view class="reading-time">
                  <text class="fas fa-clock time-icon"></text>
                  <text class="time-text">浏览{{record.duration}}分钟</text>
                </view>
              </view>
              <view class="book-meta">
                <view class="meta-item">
                  <text class="fas fa-tag meta-icon"></text>
                  <text class="meta-text">{{record.category}}</text>
                </view>
                <view class="meta-item">
                  <text class="fas fa-thumbs-up meta-icon"></text>
                  <text class="meta-text">{{record.likes}}赞</text>
                </view>
                <view class="meta-item">
                  <text class="fas fa-share-alt meta-icon"></text>
                  <text class="meta-text">{{record.shares}}分享</text>
                </view>
              </view>
              <view class="action-buttons">
                <view class="action-btn continue">
                  <text class="fas fa-eye"></text>
                  <text>查看详情</text>
                </view>
                <view class="action-btn delete">
                  <text class="fas fa-trash"></text>
                  <text>删除记录</text>
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

// 模拟历史记录数据
const historyRecords = ref([
  {
    title: '3岁宝宝早教经验分享',
    author: '育儿达人 小美',
    coverUrl: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
    duration: 45,
    lastRead: '今天',
    category: '育儿经验',
    likes: 328,
    shares: 56
  },
  {
    title: '亲子阅读的10个小技巧',
    author: '阅读指导师 小林',
    coverUrl: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop',
    duration: 30,
    lastRead: '昨天',
    category: '教育心得',
    likes: 256,
    shares: 42
  },
  {
    title: '春季亲子户外活动指南',
    author: '亲子活动策划师 小sun',
    coverUrl: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&auto=format&fit=crop',
    duration: 25,
    lastRead: '2天前',
    category: '亲子互动',
    likes: 198,
    shares: 35
  },
  {
    title: '儿童免疫力提升食谱',
    author: '儿童营养师 小月',
    coverUrl: 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=400&auto=format&fit=crop',
    duration: 60,
    lastRead: '3天前',
    category: '营养健康',
    likes: 287,
    shares: 89
  }
])

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
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

/* 历史记录列表样式 */
.history-list {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #6b7280;
  font-size: 28rpx;
}

.book-records {
  width: 100%;
}

.book-record-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1px solid #e5e7eb;
}

.book-record-item:last-child {
  border-bottom: none;
}

.book-cover {
  width: 160rpx;
  height: 220rpx;
  border-radius: 12rpx;
  object-fit: cover;
}

.book-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.book-title {
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.book-author {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 12rpx;
}

.reading-time {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.time-icon {
  color: #3b82f6;
  font-size: 24rpx;
}

.time-text {
  font-size: 24rpx;
  color: #3b82f6;
}

.book-meta {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
  background-color: #ffffff;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  color: #60a5fa;
  font-size: 24rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.action-btn.continue {
  background-color: #3b82f6;
  color: #ffffff;
}

.action-btn.delete {
  background-color: #ef4444;
  color: #ffffff;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style>

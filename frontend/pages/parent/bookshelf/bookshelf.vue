<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <text class="header-title">我的书架</text>
        <view class="header-actions">
          <view class="action-btn">
            <text class="fas fa-search"></text>
          </view>
          <view class="action-btn">
            <text class="fas fa-filter"></text>
          </view>
        </view>
      </view>
      <!-- 分类标签 -->

      <view class="category-container">
        <scroll-view scroll-x="true" class="category-tabs" :show-scrollbar="false">
          <view class="tabs-wrapper">
            <view class="tab active">全部</view>
            <view class="tab">育儿经验</view>
            <view class="tab">亲子互动</view>
            <view class="tab">教育心得</view>
            <view class="tab">营养健康</view>
            <view class="tab">最近</view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 继续阅读改为最近浏览 -->
      <view class="reading-section">
        <view class="section-header">
          <text class="section-title">最近浏览</text>
          <text class="view-more" @click="navigateToMore">查看更多</text>
        </view>
        <view class="current-book">
          <image class="book-cover" :src="currentBook.cover" mode="aspectFill"></image>
          <view class="book-info">
            <view class="book-header">
              <view>
                <text class="book-title">{{ currentBook.title }}</text>
                <text class="book-author">{{ currentBook.author }}</text>
              </view>
              <text class="fas fa-heart bookmark-icon"></text>
            </view>
            <view class="progress-section">
              <view class="engagement-stats">
                <view class="stat-item">
                  <text class="fas fa-thumbs-up"></text>
                  <text>2.3k</text>
                </view>
                <view class="stat-item">
                  <text class="fas fa-comment"></text>
                  <text>128</text>
                </view>
                <view class="stat-item">
                  <text class="fas fa-bookmark"></text>
                  <text>356</text>
                </view>
              </view>
            </view>
            <view class="action-buttons">
              <button class="continue-btn" @click="continueReading">查看详情</button>
              <button class="share-btn">
                <text class="fas fa-share-alt"></text>
              </button>
            </view>
          </view>
        </view>
      </view>

      <!-- 收藏列表 -->
      <view class="books-grid">
        <view class="book-card" v-for="book in books" :key="book.id" @click="navigateToBookDetail(book.id)">
          <view class="book-cover">
            <image :src="book.cover" mode="aspectFill"></image>
          </view>
          <view class="book-card-info">
            <text class="book-card-title">{{ book.title }}</text>
            <text class="book-card-author">{{ book.author }}</text>
            <view class="book-card-stats">
              <view class="rating">
                <text class="fas fa-thumbs-up"></text>
                <text class="rating-text">{{ book.rating }}k</text>
              </view>
              <view class="views">
                <text class="fas fa-eye"></text>
                <text class="views-text">{{ book.views }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部导航栏 -->
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 当前浏览的文章
const currentBook = ref({
  title: '如何在3分钟内安抚孩子的小情绪',
  author: '育儿专家 王老师',
  cover: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop&q=80',
  likes: '2.3k',
  comments: 128,
  bookmarks: 356
})

// 收藏列表
const books = ref([
  {
    id: 1,
    title: '培养孩子专注力的10个小游戏',
    author: '儿童教育专家 李老师',
    cover: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&fit=crop&q=80',
    rating: '2.1',
    views: '2.1万'
  },
  {
    id: 2,
    title: '春季儿童营养餐搭配指南',
    author: '营养师 张医生',
    cover: 'https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=400&fit=crop&q=80',
    rating: '1.8',
    views: '1.8万'
  },
  {
    id: 3,
    title: '亲子阅读的正确打开方式',
    author: '阅读指导师 周老师',
    cover: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&fit=crop&q=80',
    rating: '1.5',
    views: '1.5万'
  },
  {
    id: 4,
    title: '让写作变成孩子的爱好',
    author: '语文教师 刘老师',
    cover: 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=400&fit=crop&q=80',
    rating: '1.2',
    views: '1.2万'
  }
])

const navigateToMore = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const continueReading = () => {
  uni.navigateTo({
    url: '/pages/parent/reading/reading',
    animationType: 'slide-in-right',
    animationDuration: 300,
    success: () => {
      console.log('跳转到阅读页面成功')
    },
    fail: (err) => {
      console.error('跳转失败:', err)
      uni.showToast({
        title: '跳转失败，请稍后重试',
        icon: 'none'
      })
    }
  })
}

const navigateToBookDetail = (bookId) => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  flex: 1;
  background-color: #f3f4f6;
  min-height: 100vh;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

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
  padding: 12px 16px;
  height: 44px;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: white;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
}

.action-btn .fas {
  color: white;
  font-size: 15px;
}

.category-container {
  background-color: white;
  padding: 8px 0;
  border-bottom: 1px solid #e5e7eb;
}

.category-tabs {
  width: 100%;
  white-space: nowrap;
}

.category-tabs ::-webkit-scrollbar {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
}

.tabs-wrapper {
  display: inline-flex;
  padding: 0 16px;
  gap: 12px;
}

.tab {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 14px;
  font-size: 13px;
  color: #6b7280;
  background-color: #f3f4f6;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tab.active {
  background-color: #3b82f6;
  color: white;
}

.main-content {
  margin-top: calc(44px + 44px); /* header-content height + category-container height */
  padding: 16px 24px; /* 左右增加更大的留白 */
  min-height: calc(100vh - 88px - env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
}

.reading-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  width: 100%;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.view-more {
  font-size: 14px;
  color: #6b7280;
}

.current-book {
  background-color: white;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  gap: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.book-cover {
  width: 90px;
  height: 120px;
  border-radius: 8px;
  flex-shrink: 0;
  object-fit: cover;
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.book-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.book-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.book-author {
  font-size: 14px;
  color: #6b7280;
}

.bookmark-icon {
  color: #f43f5e;
  font-size: 18px;
  padding: 4px;
}

.progress-section {
  margin: 12px 0;
  flex-grow: 1;
}

.engagement-stats {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 14px;
}

.stat-item .fas {
  color: #3b82f6;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.continue-btn {
  flex: 1;
  height: 36px;
  line-height: 36px;
  background-color: #3b82f6;
  color: white;
  border-radius: 18px;
  font-size: 14px;
  text-align: center;
  border: none;
  padding: 0;
}

.share-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  margin: 0;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: white;
  flex-shrink: 0;
}

.share-btn .fas {
  color: #6b7280;
  font-size: 14px;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  width: 100%;
  box-sizing: border-box;
}

.book-card {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.book-card .book-cover {
  width: 100%;
  height: 0;
  padding-bottom: 100%; /* 1:1 aspect ratio for social media style images */
  position: relative;
}

.book-card .book-cover image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-card-info {
  padding: 10px;
}

.book-card-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  height: 4.2em;
}

.book-card-author {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-card-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rating, .views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rating .fas {
  color: #f43f5e;
  font-size: 12px;
}

.rating-text, .views-text {
  font-size: 12px;
  color: #6b7280;
}

.views .fas {
  color: #6b7280;
  font-size: 12px;
}

@media screen and (min-width: 768px) {
  .books-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }

  .main-content {
    padding: 24px;
  }

  .current-book {
    padding: 24px;
    gap: 24px;
  }

  .book-cover {
    width: 120px;
    height: 160px;
  }
}

@media screen and (min-width: 1024px) {
  .books-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style> 
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">我的收藏</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 分类标签 -->
      <scroll-view scroll-x="true" class="category-scroll" show-scrollbar="false">
        <view class="category-tags">
          <view 
            v-for="(tag, index) in categories" 
            :key="index"
            class="category-tag"
            :class="{ active: currentCategory === tag }"
            @click="selectCategory(tag)"
          >
            <text>{{ tag }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 收藏统计 -->
      <view class="stats-card">
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">42</text>
            <text class="stat-label">收藏总数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">12</text>
            <text class="stat-label">本月收藏</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">8</text>
            <text class="stat-label">已分享</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">5</text>
            <text class="stat-label">互动数</text>
          </view>
        </view>
      </view>

      <!-- 收藏列表 -->
      <view class="favorites-list">
        <view v-for="(article, index) in filteredArticles" :key="index" class="book-card">
          <view class="book-cover-container">
            <image :src="article.coverUrl" class="book-cover"></image>
            <view class="book-actions">
              <view class="action-btn play" @click="viewArticle(article)">
                <text class="fas fa-eye btn-icon"></text>
                <text class="btn-text">查看</text>
              </view>
              <view class="action-btn share" @click="shareArticle(article)">
                <text class="fas fa-share-alt btn-icon"></text>
                <text class="btn-text">分享</text>
              </view>
              <view class="action-btn delete" @click="deleteArticle(article)">
                <text class="fas fa-trash-alt btn-icon"></text>
                <text class="btn-text">删除</text>
              </view>
            </view>
          </view>
          <view class="book-info">
            <text class="book-title">{{ article.title }}</text>
            <text class="book-author">{{ article.author }}</text>
            <view class="book-meta">
              <view class="meta-item">
                <text class="fas fa-tag meta-icon"></text>
                <text class="meta-text">{{ article.category }}</text>
              </view>
              <view class="meta-item">
                <text class="fas fa-thumbs-up meta-icon"></text>
                <text class="meta-text">{{ article.likes }}赞</text>
              </view>
              <view class="meta-item">
                <text class="fas fa-comment meta-icon"></text>
                <text class="meta-text">{{ article.comments }}评论</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 分类数据
const categories = ['全部', '育儿经验', '亲子互动', '教育心得', '营养健康', '生活记录']
const currentCategory = ref('全部')

// 收藏内容数据
const articles = ref([
  {
    title: '科学育儿：如何培养孩子的专注力',
    author: '儿童心理专家 王老师',
    coverUrl: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
    category: '育儿经验',
    addTime: '2024-02-20',
    likes: 568,
    comments: 89,
    shares: 125
  },
  {
    title: '10个超有趣的亲子游戏',
    author: '早教老师 李老师',
    coverUrl: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop',
    category: '亲子互动',
    addTime: '2024-02-19',
    likes: 432,
    comments: 67,
    shares: 98
  },
  {
    title: '培养阅读习惯的正确方式',
    author: '教育专家 张老师',
    coverUrl: 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400&auto=format&fit=crop',
    category: '教育心得',
    addTime: '2024-02-18',
    likes: 356,
    comments: 45,
    shares: 78
  },
  {
    title: '春季儿童营养食谱大全',
    author: '营养师 陈老师',
    coverUrl: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&auto=format&fit=crop',
    category: '营养健康',
    addTime: '2024-02-17',
    likes: 289,
    comments: 56,
    shares: 92
  }
])

// 根据分类筛选文章
const filteredArticles = computed(() => {
  if (currentCategory.value === '全部') {
    return articles.value
  }
  return articles.value.filter(article => article.category === currentCategory.value)
})

// 选择分类
const selectCategory = (category) => {
  currentCategory.value = category
}

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 查看文章
const viewArticle = (article) => {
  uni.navigateTo({
    url: `/pages/parent/article-detail/article-detail?id=${article.title}`,
    fail: (err) => {
      console.error('查看失败:', err)
      uni.showToast({
        title: '暂时无法查看',
        icon: 'none'
      })
    }
  })
}

// 分享文章
const shareArticle = (article) => {
  // H5环境下使用系统分享
  if (uni.getSystemInfoSync().platform === 'web') {
    if (navigator.share) {
      navigator.share({
        title: article.title,
        text: `分享一篇好文：《${article.title}》`,
        url: window.location.href
      }).catch((error) => {
        uni.showToast({
          title: '分享失败',
          icon: 'none'
        })
      })
    } else {
      uni.showToast({
        title: '当前环境不支持分享',
        icon: 'none'
      })
    }
  } else {
    // 非H5环境下使用原生分享
    uni.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline'],
      success: () => {
        uni.showToast({
          title: '请点击右上角分享',
          icon: 'none'
        })
      },
      fail: () => {
        uni.showToast({
          title: '分享失败',
          icon: 'none'
        })
      }
    })
  }
}

// 取消收藏
const deleteArticle = (article) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要取消收藏《${article.title}》吗？`,
    success: (res) => {
      if (res.confirm) {
        // 这里应该调用后端API删除收藏
        articles.value = articles.value.filter(item => item.title !== article.title)
        uni.showToast({
          title: '已取消收藏',
          icon: 'success'
        })
      }
    }
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

/* 分类标签样式 */
.category-scroll {
  width: 100%;
  white-space: nowrap;
  margin-bottom: 30rpx;
}

.category-tags {
  display: inline-flex;
  padding: 0 10rpx;
}

.category-tag {
  padding: 12rpx 32rpx;
  margin-right: 20rpx;
  background-color: #ffffff;
  border-radius: 9999rpx;
  font-size: 28rpx;
  color: #6b7280;
  transition: all 0.3s ease;
}

.category-tag.active {
  background-color: #3b82f6;
  color: #ffffff;
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

/* 收藏列表样式 */
.favorites-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  width: 100%;
}

.book-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.book-cover-container {
  position: relative;
  width: 100%;
  padding-top: 140%;
}

.book-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20rpx;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.book-cover-container:hover .book-actions {
  opacity: 1;
}

.action-btn {
  min-width: 120rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background-color: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 0 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.action-btn:hover {
  background-color: rgba(255, 255, 255, 0.95);
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.4);
}

.action-btn text {
  font-size: 24rpx;
}

.action-btn text.btn-icon {
  font-size: 22rpx;
}

.action-btn text.btn-text {
  font-size: 22rpx;
  font-weight: normal;
}

/* 为不同按钮添加特定颜色 */
.action-btn.play {
  background-color: #10b981;
}

.action-btn.share {
  background-color: #6366f1;
}

.action-btn.delete {
  background-color: #ef4444;
}

.action-btn text {
  color: #ffffff;
}

.book-info {
  padding: 20rpx;
}

.book-title {
  font-size: 28rpx;
  font-weight: 500;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-author {
  font-size: 24rpx;
  color: #6b7280;
  margin-bottom: 12rpx;
}

.book-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
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
  font-size: 24rpx;
  color: #60a5fa;
}

.meta-text {
  font-size: 24rpx;
  color: #6b7280;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style>

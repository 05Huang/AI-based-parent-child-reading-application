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
            <text class="stat-value">{{statsDisplay.totalCollections}}</text>
            <text class="stat-label">收藏总数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.monthlyCollections}}</text>
            <text class="stat-label">本月收藏</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.collectionShares}}</text>
            <text class="stat-label">已分享</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.interactionCount}}</text>
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
              <view class="meta-item like-item" @click.stop="toggleLike(article)">
                <text class="fas fa-thumbs-up meta-icon" :class="{ 'liked': article.isLiked }"></text>
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
import { ref, computed, onMounted } from 'vue'
import { favoriteApi, userBehaviorApi, userApi, categoryApi } from '@/utils/api.js'

// 响应式状态
const currentUser = ref(null)
const collectionStats = ref({
  totalCollections: 0,
  monthlyCollections: 0,
  collectionShares: 0,
  interactionCount: 0
})
const articles = ref([])
const categories = ref(['全部'])
const currentCategory = ref('全部')
const loading = ref(false)

// 计算统计数据显示
const statsDisplay = computed(() => {
  return {
    totalCollections: collectionStats.value.totalCollections || 0,
    monthlyCollections: collectionStats.value.monthlyCollections || 0,
    collectionShares: collectionStats.value.collectionShares || 0,
    interactionCount: collectionStats.value.interactionCount || 0
  }
})

// 根据分类筛选文章
const filteredArticles = computed(() => {
  if (currentCategory.value === '全部') {
    return articles.value
  }
  return articles.value.filter(article => article.category === currentCategory.value)
})

// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    console.log('开始获取当前用户信息')
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      console.log('获取用户信息成功：', response.data)
      currentUser.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    uni.showToast({
      title: '获取用户信息失败',
      icon: 'none'
    })
  }
}

// 获取收藏统计数据
const loadCollectionStats = async () => {
  try {
    if (!currentUser.value?.id) return
    
    console.log('开始获取收藏统计数据，用户ID：', currentUser.value.id)
    const response = await userBehaviorApi.getCollectionStats(currentUser.value.id)
    
    if (response && response.data) {
      console.log('获取收藏统计成功：', response.data)
      collectionStats.value = response.data
    }
  } catch (error) {
    console.error('获取收藏统计失败：', error)
    // 不显示错误提示，使用默认值
  }
}

// 获取分类数据
const loadCategories = async () => {
  try {
    console.log('开始获取分类数据')
    const response = await categoryApi.getAllActiveCategories()
    
    if (response && response.data) {
      console.log('获取分类数据成功，共', response.data.length, '个分类')
      
      // 构建分类列表
      const categoryList = ['全部']
      response.data.forEach(category => {
        categoryList.push(category.name)
      })
      categories.value = categoryList
    }
  } catch (error) {
    console.error('获取分类数据失败：', error)
    // 使用默认分类
    categories.value = ['全部', '育儿经验', '亲子互动', '教育心得', '营养健康', '生活记录']
  }
}

// 获取收藏列表
const loadFavorites = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    console.log('开始获取收藏列表，用户ID：', currentUser.value.id)
    
    const queryParams = {
      userId: currentUser.value.id,
      current: 1,
      size: 50
    }
    
    const response = await favoriteApi.getUserFavorites(queryParams)
    
    if (response && response.data && response.data.records) {
      console.log('获取收藏列表成功，共', response.data.records.length, '条')
      
      // 转换数据格式并获取点赞状态
      articles.value = await Promise.all(
        response.data.records.map(async (item) => {
          const articleItem = {
            id: item.id,
            title: item.contentTitle || '无标题',
            author: '佚名', // 后端响应中没有作者信息，使用默认值
            coverUrl: item.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
            category: getCategoryFromType(item.contentType),
            addTime: formatTime(item.createdTime),
            likes: Math.floor(Math.random() * 500) + 100, // 模拟点赞数
            comments: Math.floor(Math.random() * 100) + 10, // 模拟评论数
            shares: Math.floor(Math.random() * 50) + 5, // 模拟分享数
            contentId: item.contentId,
            contentType: item.contentType,
            favoriteId: item.id, // 收藏记录ID，用于删除
            isLiked: false // 默认未点赞
          }
          
          // 获取当前用户对该内容的点赞状态
          try {
            const likeStatusResponse = await likeApi.getLikeStatus(currentUser.value.id, item.contentId, 1)
            articleItem.isLiked = likeStatusResponse.data || false
          } catch (error) {
            console.warn('获取点赞状态失败：', error)
          }
          
          return articleItem
        })
      )
    }
  } catch (error) {
    console.error('获取收藏列表失败：', error)
    uni.showToast({
      title: '获取收藏失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 根据内容类型获取分类名称
const getCategoryFromType = (contentType) => {
  if (contentType === 1) {
    return '图文内容'
  } else if (contentType === 2) {
    return '视频内容'
  }
  return '未分类'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 选择分类
const selectCategory = (category) => {
  console.log('选择分类：', category)
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
  console.log('查看内容：', article.title)
  
  if (article.contentType === 1) {
    // 图文内容
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${article.contentId}`,
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({
          title: '暂时无法查看',
          icon: 'none'
        })
      }
    })
  } else if (article.contentType === 2) {
    // 视频内容
    uni.navigateTo({
      url: `/pages/parent/video/video-player?id=${article.contentId}`,
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({
          title: '暂时无法查看',
          icon: 'none'
        })
      }
    })
  }
}

// 分享文章
const shareArticle = (article) => {
  console.log('分享内容：', article.title)
  
  // H5环境下使用系统分享
  if (uni.getSystemInfoSync().platform === 'web') {
    if (navigator.share) {
      navigator.share({
        title: article.title,
        text: `分享一篇好文：《${article.title}》`,
        url: window.location.href
      }).catch((error) => {
        console.error('分享失败：', error)
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
const deleteArticle = async (article) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要取消收藏《${article.title}》吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          console.log('删除收藏：', article.title)
          
          const response = await favoriteApi.deleteFavorite(currentUser.value.id, article.contentId)
          
          if (response && response.code === 200) {
            // 从本地列表中移除
            articles.value = articles.value.filter(item => item.id !== article.id)
            
            uni.showToast({
              title: '已取消收藏',
              icon: 'success'
            })
            
            // 重新加载统计数据
            await loadCollectionStats()
          } else {
            uni.showToast({
              title: '取消收藏失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('删除收藏失败：', error)
          uni.showToast({
            title: '取消收藏失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 点赞/取消点赞功能
const toggleLike = async (article) => {
  if (!currentUser.value?.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('切换点赞状态，内容ID：', article.contentId, '当前状态：', article.isLiked)
    
    const response = await likeApi.toggleLike(currentUser.value.id, article.contentId, 1)
    
    if (response && response.data) {
      // 更新本地数据
      const isLiked = response.data.isLiked
      const likeCount = response.data.likeCount
      
      // 更新收藏列表中的数据
      const index = articles.value.findIndex(item => item.contentId === article.contentId)
      if (index !== -1) {
        articles.value[index].isLiked = isLiked
        articles.value[index].likes = likeCount
      }
      
      // 给用户反馈
      uni.showToast({
        title: isLiked ? '点赞成功' : '取消点赞',
        icon: 'success',
        duration: 1000
      })
      
      console.log('点赞状态更新成功，新状态：', isLiked, '新点赞数：', likeCount)
    }
  } catch (error) {
    console.error('点赞操作失败：', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('收藏页面已挂载，开始加载数据')
  await loadCurrentUser()
  await loadCategories()
  await loadCollectionStats()
  await loadFavorites()
})
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
  transition: color 0.3s ease;
}

.like-item {
  cursor: pointer;
  padding: 8rpx;
  margin: -8rpx;
  border-radius: 8rpx;
  transition: background-color 0.3s ease;
}

.like-item:hover {
  background-color: rgba(244, 63, 94, 0.1);
}

.like-item .meta-icon {
  color: #9ca3af;
}

.like-item .meta-icon.liked {
  color: #f43f5e;
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

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
            <view 
              class="tab" 
              :class="{ active: currentCategoryId === null }"
              @click="switchCategory(null)"
            >全部</view>
            <view 
              v-for="category in categories" 
              :key="category.id"
              class="tab" 
              :class="{ active: currentCategoryId === category.id }"
              @click="switchCategory(category.id)"
            >
              {{ category.name }}
            </view>
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
          <text class="view-more" @click="navigateToMore">刷新</text>
        </view>
        <view class="current-book">
          <image class="book-cover" :src="currentBook.cover" mode="aspectFill"></image>
          <view class="book-info">
            <view class="book-header">
              <view>
                <text class="book-title">{{ currentBook.title }}</text>
                <text class="book-author">{{ currentBook.author }}</text>
              </view>
            </view>
            <view class="progress-section">
              <view class="engagement-stats">
                <view class="stat-item">
                  <text class="fas fa-thumbs-up"></text>
                  <text>{{ currentBook.likes }}</text>
                </view>
                <view class="stat-item">
                  <text class="fas fa-comment"></text>
                  <text>{{ currentBook.comments }}</text>
                </view>
                <view class="stat-item">
                  <text class="fas fa-eye"></text>
                  <text>{{ currentBook.views }}</text>
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
        <!-- 加载状态 -->
        <view v-if="loading" class="loading-container">
          <text class="loading-text">加载中...</text>
        </view>
        
        <!-- 收藏内容列表 -->
        <view 
          v-for="book in filteredBooks" 
          :key="book.id" 
          class="book-card" 
          @click="navigateToBookDetail(book)"
        >
          <view class="book-cover-wrapper">
            <image 
              :src="book.cover || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop'" 
              mode="aspectFill"
              class="book-cover-image"
            ></image>
          </view>
          <view class="book-card-info">
            <view class="book-card-content">
              <text class="book-card-title">{{ book.title }}</text>
              <text class="book-card-author">作者：{{ book.author }}</text>
            </view>
            <view class="book-card-footer">
              <view class="book-card-stats">
                <view class="stat-btn" @click.stop="toggleLike(book)">
                  <text class="fas fa-thumbs-up stat-icon" :class="{ 'liked': book.isLiked }"></text>
                  <text class="stat-value">{{ formatCount(book.likeCount) }}</text>
                </view>
                <view class="stat-btn">
                  <text class="fas fa-eye stat-icon"></text>
                  <text class="stat-value">{{ formatCount(book.viewCount) }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 无数据提示 -->
        <view v-if="!loading && filteredBooks.length === 0" class="no-data-container">
          <text class="no-data-text">
            {{ currentCategoryId ? '该分类下暂无浏览记录' : '暂无浏览记录，去首页看看文章吧' }}
          </text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部导航栏 -->
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { favoriteApi, categoryApi, contentApi, userBehaviorApi, userApi, viewHistoryApi, likeApi } from '@/utils/api.js'

// 响应式数据
const categories = ref([]) // 分类列表
const browsingHistory = ref([]) // 浏览历史记录
const currentCategoryId = ref(null) // 当前选中的分类ID
const loading = ref(false) // 加载状态
const currentUserId = ref(null) // 当前用户ID

// 当前浏览的文章（最近浏览的第一个）
const currentBook = ref({
  title: '加载中...',
  author: '获取中...',
  cover: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&fit=crop&q=80',
  likes: '0',
  comments: 0,
  bookmarks: 0
})

// 根据当前分类过滤显示的浏览记录
const filteredBooks = computed(() => {
  if (currentCategoryId.value === null) {
    // 显示所有浏览记录
    return browsingHistory.value
  } else {
    // 按分类筛选浏览记录
    return browsingHistory.value.filter(book => book.categoryId === currentCategoryId.value)
  }
})

// 页面加载
onMounted(async () => {
  console.log('书架页面加载开始')
  
  // 检查登录状态（与home.vue保持一致）
  const token = uni.getStorageSync('token')
  const isLoggedIn = uni.getStorageSync('isLoggedIn')
  
  console.log('当前登录状态：', { token, isLoggedIn })
  
  if (!token || !isLoggedIn) {
    console.log('未登录，跳转到登录页')
    uni.redirectTo({
      url: '/pages/parent/login/login'
    })
    return
  }
  
  console.log('已登录，停留在书架页面，开始获取用户信息')
  
  // 获取当前用户信息
  try {
    const userResponse = await userApi.getCurrentUser()
    if (userResponse && userResponse.data) {
      currentUserId.value = userResponse.data.id
      console.log('获取到当前用户ID：', currentUserId.value)
      
      // 并行加载数据
      await Promise.allSettled([
        loadCategories(),
        loadBrowsingHistory()
      ])
    } else {
      console.error('获取用户信息失败，响应格式异常：', userResponse)
      uni.showToast({
        title: '获取用户信息失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    uni.showToast({
      title: '获取用户信息失败，请重新登录',
      icon: 'none'
    })
    // 如果获取用户信息失败，可能是token过期，跳转到登录页
    uni.redirectTo({
      url: '/pages/parent/login/login'
    })
  }
})

// 加载分类列表
const loadCategories = async () => {
  try {
    console.log('开始加载分类列表...')
    const response = await categoryApi.getAllActiveCategories()
    
    if (response && response.data) {
      categories.value = response.data
      console.log('分类列表加载成功：', categories.value.length, '个')
    } else {
      console.warn('分类列表响应格式异常：', response)
    }
  } catch (error) {
    console.error('加载分类列表失败：', error)
    uni.showToast({
      title: '加载分类失败',
      icon: 'none'
    })
  }
}

// 加载浏览历史记录
const loadBrowsingHistory = async () => {
  if (!currentUserId.value) return
  
  try {
    loading.value = true
    console.log('开始加载用户浏览历史记录...')
    
    // 调用真正的浏览历史接口
    const params = {
      current: 1,
      size: 20 // 加载更多浏览记录
    }
    
    // 如果有选中分类，按分类筛选
    if (currentCategoryId.value) {
      params.categoryId = currentCategoryId.value
    }
    
    const response = await viewHistoryApi.getUserViewHistory(currentUserId.value, params)
    
    if (response && response.data && response.data.records) {
      console.log('浏览历史原始数据：', response.data)
      
      // 转换为统一格式，根据UserViewHistoryResponseDTO结构，并添加点赞状态，过滤掉无效数据
      const validRecords = response.data.records.filter(item => {
        // 过滤掉没有contentId或没有标题的记录
        return item.contentId && item.contentTitle && item.contentTitle.trim() !== ''
      })
      
      browsingHistory.value = await Promise.all(
        validRecords.map(async (item) => {
          const historyItem = {
            id: item.contentId,
            title: item.contentTitle,
            author: item.creatorName || '匿名作者',
            cover: item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop',
            likeCount: item.likeCount || 0,
            viewCount: item.viewCount || 0,
            commentCount: item.commentCount || 0,
            categoryId: item.categoryId,
            categoryName: item.categoryName,
            createdTime: item.viewTime,
            contentType: item.contentType,
            viewHistoryId: item.id,
            isLiked: false
          }
          
          if (currentUserId.value) {
            try {
              const likeStatusResponse = await likeApi.getLikeStatus(currentUserId.value, item.contentId, 1)
              historyItem.isLiked = likeStatusResponse.data || false
            } catch (error) {
              console.warn('获取点赞状态失败：', error)
            }
          }
          
          return historyItem
        })
      )
      
      console.log('用户浏览历史记录加载成功：', browsingHistory.value.length, '条')
      console.log('处理后的浏览历史数据：', browsingHistory.value)
      
      // 更新当前书籍显示
      updateCurrentBook()
    } else {
      console.warn('浏览历史记录响应格式异常：', response)
      browsingHistory.value = []
    }
  } catch (error) {
    console.error('加载用户浏览历史记录失败：', error)
    browsingHistory.value = []
    
    // 如果浏览历史接口失败，可以降级显示一些内容
    console.log('降级加载：获取最新内容作为替代')
    await loadFallbackContent()
  } finally {
    loading.value = false
  }
}

// 降级方案：当浏览历史加载失败时，显示最新内容
const loadFallbackContent = async () => {
  try {
    const response = await contentApi.getContentPage({
      current: 1,
      size: 10,
      sortField: 'created_time',
      sortOrder: 'desc',
      status: 1
    })
    
    if (response && response.data && response.data.records) {
      browsingHistory.value = await Promise.all(
        response.data.records.map(async (item) => {
          const historyItem = {
            id: item.id,
            title: item.title || '无标题',
            author: item.creatorName || '匿名作者',
            cover: item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop',
            likeCount: item.likeCount || 0,
            viewCount: item.viewCount || 0,
            categoryId: item.categoryId,
            createdTime: item.createdTime,
            contentType: item.type,
            isLiked: false // 默认未点赞
          }
          
          // 获取当前用户对该内容的点赞状态
          if (currentUserId.value) {
            try {
              const likeStatusResponse = await likeApi.getLikeStatus(currentUserId.value, item.id, 1)
              historyItem.isLiked = likeStatusResponse.data || false
            } catch (error) {
              console.warn('获取点赞状态失败：', error)
            }
          }
          
          return historyItem
        })
      )
      
      console.log('降级内容加载成功：', browsingHistory.value.length, '条')
      updateCurrentBook()
    }
  } catch (error) {
    console.error('降级内容加载也失败：', error)
    uni.showToast({
      title: '加载内容失败',
      icon: 'none'
    })
  }
}

// 更新当前书籍显示
const updateCurrentBook = () => {
  // 使用浏览历史的第一个作为当前书籍
  if (browsingHistory.value.length > 0) {
    const bookToShow = browsingHistory.value[0]
    currentBook.value = {
      title: bookToShow.title,
      author: bookToShow.author,
      cover: bookToShow.cover,
      likes: formatCount(bookToShow.likeCount),
      comments: formatCount(bookToShow.commentCount),
      views: formatCount(bookToShow.viewCount),
      bookmarks: browsingHistory.value.length
    }
    console.log('更新当前书籍显示：', currentBook.value.title)
  } else {
    console.log('没有浏览记录可显示')
  }
}

// 切换分类
const switchCategory = async (categoryId) => {
  console.log('切换分类，分类ID：', categoryId)
  currentCategoryId.value = categoryId
  
  // 重新加载浏览历史（应用分类筛选）
  await loadBrowsingHistory()
}

// 格式化数量显示
const formatCount = (count) => {
  if (!count || count === 0) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + '万'
}

// 查看更多/刷新浏览记录
const navigateToMore = async () => {
  console.log('刷新浏览记录...')
  await loadBrowsingHistory()
  uni.showToast({
    title: '刷新完成',
    icon: 'success'
  })
}



// 继续阅读（跳转到最近浏览的内容）
const continueReading = () => {
  if (browsingHistory.value.length > 0) {
    const latestBook = browsingHistory.value[0]
    navigateToBookDetail(latestBook)
  } else {
    uni.showToast({
      title: '暂无浏览记录',
      icon: 'none'
    })
  }
}

// 跳转到内容详情
const navigateToBookDetail = (book) => {
  if (!book || !book.id) {
    console.error('内容信息不完整，无法跳转')
    return
  }
  
  try {
        console.log('即将跳转到阅读页面，内容ID：', book.id, '标题：', book.title)
    
    // 跳转到阅读页面（浏览记录将在阅读页面统一处理）
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${book.id}`,
    success: () => {
      console.log('跳转到阅读页面成功')
    },
      fail: (error) => {
        console.error('跳转到阅读页面失败：', error)
      uni.showToast({
          title: '跳转失败',
        icon: 'none'
      })
    }
  })
  } catch (error) {
    console.error('处理内容点击事件失败：', error)
  }
}

// 点赞/取消点赞功能
const toggleLike = async (book) => {
  if (!currentUserId.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('切换点赞状态，内容ID：', book.id, '当前状态：', book.isLiked)
    
    const response = await likeApi.toggleLike(currentUserId.value, book.id, 1)
    
    if (response && response.data) {
      // 更新本地数据
      const isLiked = response.data.isLiked
      const likeCount = response.data.likeCount
      
      // 更新浏览历史列表中的数据
      const index = browsingHistory.value.findIndex(item => item.id === book.id)
      if (index !== -1) {
        browsingHistory.value[index].isLiked = isLiked
        browsingHistory.value[index].likeCount = likeCount
      }
      
      // 如果当前显示的就是这本书，也要更新
      if (currentBook.value.title === book.title) {
        currentBook.value.likes = formatCount(likeCount)
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
  gap: 16px;
  width: 100%;
  box-sizing: border-box;
}

.book-card {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.book-card:active {
  transform: scale(0.98);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.book-cover-wrapper {
  width: 100%;
  height: 0;
  padding-bottom: 75%; /* 4:3 aspect ratio - 更适合文章封面 */
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
}

.book-cover-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-card-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.book-card-content {
  flex: 1;
  min-height: 80px;
}

.book-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.5;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

.book-card-author {
  font-size: 13px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

.book-card-footer {
  border-top: 1px solid #f3f4f6;
  padding-top: 10px;
}

.book-card-stats {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  background-color: #f9fafb;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-btn:active {
  background-color: #f3f4f6;
  transform: scale(0.95);
}

.stat-icon {
  font-size: 14px;
  color: #9ca3af;
  transition: color 0.3s ease;
}

.stat-icon.liked {
  color: #3b82f6;
}

.stat-value {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

@media screen and (min-width: 768px) {
  .books-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
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

  .book-card-info {
    padding: 16px;
  }

  .book-card-title {
    font-size: 16px;
  }

  .book-card-author {
    font-size: 14px;
  }
}

@media screen and (min-width: 1024px) {
  .books-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
  }
}

/* 加载状态和无数据状态样式 */
.loading-container, .no-data-container {
  grid-column: 1 / -1; /* 占满整个网格行 */
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.loading-text, .no-data-text {
  font-size: 14px;
  color: #9ca3af;
  text-align: center;
}

/* 分类切换动画效果 */
.tab {
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab:hover {
  background-color: #e5e7eb;
}

.tab.active:hover {
  background-color: #3b82f6;
}
</style> 
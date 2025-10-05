<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <image 
            :src="currentUser?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser?.nickname || '用户')}&background=3b82f6&color=fff&size=128`" 
            class="avatar" 
            @click="navigateToProfile"
          ></image>
        </view>
        <view class="search-box">
          <view class="search-input-wrapper" @click="navigateToSearch">
            <text class="fas fa-search search-icon"></text>
            <input 
              type="text" 
              placeholder="搜索感兴趣的内容" 
              placeholder-class="search-placeholder" 
              class="input"
              disabled
            />
          </view>
        </view>
        <view class="header-right">
          <view class="icon-wrapper" @click="navigateToNotification">
            <text class="fas fa-envelope icon"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view 
      scroll-y="true" 
      class="main-content"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 轮播图 -->
      <swiper class="banner-swiper" 
              :indicator-dots="true" 
              :autoplay="true" 
              :interval="3000" 
              :duration="500"
              circular>
        <swiper-item v-for="item in swiperList" :key="item.id" class="swiper-item">
          <view class="banner-item">
            <image :src="item.image" mode="aspectFill" class="banner-image"></image>
            <view class="banner-title">{{item.title}}</view>
          </view>
        </swiper-item>
      </swiper>

      

      <!-- 推荐阅读改为推荐内容 -->
      <view class="recommend">
        <view class="section-header">
          <text class="section-title">精选推荐</text>
          <text class="more-link" @click="navigateToAllArticles">查看更多</text>
        </view>
        <scroll-view scroll-x="true" class="book-scroll">
          <!-- 加载状态 -->
          <view v-if="loading" class="book-card loading-card">
            <view class="loading-placeholder">
              <text>加载中...</text>
            </view>
          </view>
          
          <!-- 精选推荐内容列表 -->
          <view 
            v-for="item in recommendedContents" 
            :key="item.id"
            class="book-card"
            @click="navigateToReading(item)"
          >
            <image 
              :src="item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop'" 
              class="book-cover"
              mode="aspectFill"
            ></image>
            <view class="book-info">
              <text class="book-title">{{ item.title }}</text>
              <text class="book-age">{{ item.tags || '推荐阅读' }}</text>
                          <!-- 移除浏览量显示 -->
            </view>
          </view>
          
          <!-- 无数据提示 -->
          <view v-if="!loading && recommendedContents.length === 0" class="book-card no-data-card">
            <view class="no-data-placeholder">
              <text>暂无推荐内容</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 热门阅读改为热门文章 -->
      <view class="hot-reading">
        <view class="section-header">
          <text class="section-title">热门文章</text>
          <view class="refresh" @click="refreshHotContents">
            <text class="refresh-text">换一换</text>
            <text class="fas fa-sync-alt refresh-icon"></text>
          </view>
        </view>
        <view class="hot-books">
          <!-- 加载状态 -->
          <view v-if="loading" class="hot-book-item loading-item">
            <view class="loading-placeholder">
              <text>加载中...</text>
            </view>
          </view>
          
          <!-- 热门文章列表 -->
          <view 
            v-for="item in hotContents" 
            :key="item.id"
            class="hot-book-item"
            @click="navigateToReading(item)"
          >
            <image 
              :src="item.coverUrl || 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=400&auto=format&fit=crop'" 
              class="hot-book-cover"
              mode="aspectFill"
            ></image>
            <view class="hot-book-info">
              <view class="hot-book-content">
                <text class="hot-book-title">{{ item.title }}</text>
                <text class="hot-book-author">作者：{{ item.creatorName || '匿名作者' }}</text>
              </view>
              <view class="hot-book-footer">
                <view class="hot-book-tags-wrapper">
                  <text class="hot-book-tag">{{ item.tags || '热门推荐' }}</text>
                </view>
                <view class="hot-book-stats">
                  <view class="stat-item" @click.stop="toggleLike(item, 'hot')">
                    <text class="fas fa-thumbs-up stat-icon" :class="{ 'liked': item.isLiked }"></text>
                    <text class="stat-text">{{ formatViewCount(item.likeCount) }}</text>
                  </view>
                  <view class="stat-item">
                    <text class="fas fa-eye stat-icon"></text>
                    <text class="stat-text">{{ formatViewCount(item.viewCount) }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
          
          <!-- 无数据提示 -->
          <view v-if="!loading && hotContents.length === 0" class="hot-book-item no-data-item">
            <view class="no-data-placeholder">
              <text>暂无热门内容</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部导航栏 -->
  </view>
</template>

<script setup>
import { onMounted, ref, nextTick } from 'vue'
import { contentApi, recommendationApi, userApi, likeApi } from '@/utils/api.js'

// 响应式数据
const recommendedContents = ref([]) // 精选推荐内容
const hotContents = ref([]) // 热门文章内容
const loading = ref(false) // 加载状态
const isRefreshing = ref(false) // 下拉刷新状态
const currentUser = ref(null) // 当前用户信息

// 初始化页面数据
const initPageData = async () => {
  console.log('[首页] 初始化页面数据')
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
  
  console.log('已登录，开始获取用户信息')
  
  // 获取当前用户信息
  try {
    const userResponse = await userApi.getCurrentUser()
    if (userResponse && userResponse.data) {
      console.log('获取到当前用户信息：', userResponse.data)
      currentUser.value = userResponse.data
      await loadHomeData()
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
    uni.redirectTo({
      url: '/pages/parent/login/login'
    })
  }
}

// 页面加载时检查登录状态并获取数据
onMounted(async () => {
  console.log('[首页] 页面已挂载')
  await initPageData()
})

// 下拉刷新
const onRefresh = async () => {
  console.log('[首页] 下拉刷新')
  isRefreshing.value = true
  try {
    await loadHomeData()
    uni.showToast({
      title: '刷新成功',
      icon: 'success',
      duration: 1000
    })
  } catch (error) {
    console.error('刷新失败：', error)
  } finally {
    isRefreshing.value = false
  }
}

// 加载首页数据
const loadHomeData = async () => {
  try {
    loading.value = true
    console.log('开始加载首页数据...')
    
    // 并行请求推荐内容和热门内容
    const [recommendedResult, hotResult] = await Promise.allSettled([
      loadRecommendedContents(),
      loadHotContents()
    ])
    
    if (recommendedResult.status === 'rejected') {
      console.error('加载推荐内容失败：', recommendedResult.reason)
    }
    
    if (hotResult.status === 'rejected') {
      console.error('加载热门内容失败：', hotResult.reason)
    }
    
    console.log('首页数据加载完成')
  } catch (error) {
    console.error('加载首页数据失败：', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 加载精选推荐内容
const loadRecommendedContents = async () => {
  try {
    console.log('开始加载精选推荐内容（随机排序）...')
    
    // 先获取总页数
    const firstResponse = await contentApi.getContentPage({
      current: 1,
      size: 6,
      sortField: 'created_time',
      sortOrder: 'desc',
      status: 1,
      type: 1
    })
    
    if (!firstResponse || !firstResponse.data || !firstResponse.data.records) {
      console.warn('精选推荐内容响应格式异常：', firstResponse)
      return
    }
    
    const totalPages = firstResponse.data.pages || 1
    console.log('总页数：', totalPages)
    
    // 随机选择页码，但不超过总页数
    const randomPage = totalPages > 1 ? Math.floor(Math.random() * Math.min(totalPages, 3)) + 1 : 1
    console.log('随机选择第', randomPage, '页')
    
    const response = randomPage === 1 ? firstResponse : await contentApi.getContentPage({
      current: randomPage,
      size: 6,
      sortField: 'created_time',
      sortOrder: 'desc',
      status: 1,
      type: 1
    })
    
    if (response && response.data && response.data.records && response.data.records.length > 0) {
      // 随机打乱数组
      const shuffled = response.data.records.sort(() => Math.random() - 0.5)
      
      recommendedContents.value = await Promise.all(
        shuffled.map(async (item) => {
          const contentWithLikeStatus = { ...item, isLiked: false }
          
          if (currentUser.value?.id) {
            try {
              const likeStatusResponse = await likeApi.getLikeStatus(currentUser.value.id, item.id, 1)
              contentWithLikeStatus.isLiked = likeStatusResponse.data || false
            } catch (error) {
              console.warn('获取点赞状态失败：', error)
            }
          }
          
          return contentWithLikeStatus
        })
      )
      console.log('精选推荐内容加载成功（随机）：', recommendedContents.value.length, '条')
    } else {
      console.warn('精选推荐内容为空')
    }
  } catch (error) {
    console.error('加载精选推荐内容失败：', error)
    await loadContentsFallback('recommended')
  }
}

// 加载热门文章内容
const loadHotContents = async () => {
  try {
    console.log('开始加载热门文章内容...')
    // 直接使用降级方案，避免推荐服务连接问题
    await loadContentsFallback('hot')
  } catch (error) {
    console.error('加载热门内容失败：', error)
  }
}

// 降级方案：使用普通分页查询（随机）
const loadContentsFallback = async (type) => {
  try {
    console.log(`使用降级方案加载${type}内容（随机）...`)
    
    // 先获取第一页确定总页数
    const firstParams = {
      current: 1,
      size: type === 'recommended' ? 6 : 8,
      status: 1,
      type: 1,
      sortField: type === 'recommended' ? 'created_time' : 'view_count',
      sortOrder: 'desc'
    }
    
    const firstResponse = await contentApi.getContentPage(firstParams)
    
    if (!firstResponse || !firstResponse.data || !firstResponse.data.records) {
      console.warn(`${type}内容响应格式异常`)
      return
    }
    
    const totalPages = firstResponse.data.pages || 1
    console.log(`${type}总页数：`, totalPages)
    
    // 随机选择页码，但不超过总页数
    const randomPage = totalPages > 1 ? Math.floor(Math.random() * Math.min(totalPages, 5)) + 1 : 1
    console.log(`${type}随机选择第`, randomPage, '页')
    
    const response = randomPage === 1 ? firstResponse : await contentApi.getContentPage({
      ...firstParams,
      current: randomPage
    })
    
    if (response && response.data && response.data.records && response.data.records.length > 0) {
      // 随机打乱数组
      const shuffled = response.data.records.sort(() => Math.random() - 0.5)
      
      // 热门只取前4个
      const finalRecords = type === 'hot' ? shuffled.slice(0, 4) : shuffled
      
      const contentsWithLikeStatus = await Promise.all(
        finalRecords.map(async (item) => {
          const contentWithLikeStatus = { ...item, isLiked: false }
          
          if (currentUser.value?.id) {
            try {
              const likeStatusResponse = await likeApi.getLikeStatus(currentUser.value.id, item.id, 1)
              contentWithLikeStatus.isLiked = likeStatusResponse.data || false
            } catch (error) {
              console.warn('获取点赞状态失败：', error)
            }
          }
          
          return contentWithLikeStatus
        })
      )
      
      if (type === 'recommended') {
        recommendedContents.value = contentsWithLikeStatus
      } else {
        hotContents.value = contentsWithLikeStatus
      }
      console.log(`${type}内容降级加载成功（随机）：`, contentsWithLikeStatus.length, '条')
    } else {
      console.warn(`${type}内容为空`)
    }
  } catch (error) {
    console.error(`降级加载${type}内容失败：`, error)
  }
}

// 跳转到个人中心页面
const navigateToProfile = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 跳转到消息通知页面
const navigateToNotification = () => {
  uni.navigateTo({
    url: '/pages/parent/notification/notification'
  })
}

// 跳转到搜索页面
const navigateToSearch = () => {
  // 添加触觉反馈
  uni.vibrateShort({
    success: function () {
      // 跳转到搜索页面
      uni.navigateTo({
        url: '/pages/parent/search/search',
        animationType: 'fade-in',
        animationDuration: 200,
        success: () => {
          // 可以在这里添加跳转成功后的回调
        }
      })
    }
  })
}

// 跳转到全部文章页面
const navigateToAllArticles = () => {
  uni.navigateTo({
    url: '/pages/parent/bookshelf/all-articles'
  })
}

// 轮播图数据
const swiperList = ref([
  {
    id: 1,
    image: 'https://images.unsplash.com/photo-1536640712-4d4c36ff0e4e?w=800&auto=format&fit=crop',
    title: '亲子教育专题：如何培养孩子的学习兴趣'
  },
  {
    id: 2,
    image: 'https://images.unsplash.com/photo-1596464716127-f2a82984de30?w=800&auto=format&fit=crop',
    title: '早教专题：0-3岁宝宝成长指南'
  },
  {
    id: 3,
    image: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800&auto=format&fit=crop',
    title: '专家讲座：儿童心理健康'
  },
  {
    id: 4,
    image: 'https://images.unsplash.com/photo-1577896851231-70ef18881754?w=800&auto=format&fit=crop',
    title: '亲子美食：健康营养食谱'
  },
  
])



// 格式化浏览量显示
const formatViewCount = (count) => {
  if (!count || count === 0) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + '万'
}

// 跳转到阅读页面
const navigateToReading = async (item) => {
  if (!item || !item.id) {
    console.error('文章信息不完整，无法跳转')
    return
  }
  
  try {
    console.log('即将跳转到阅读页面，文章ID：', item.id, '标题：', item.title)
    
    // 跳转到阅读页面（浏览记录将在阅读页面统一处理）
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${item.id}`,
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
    console.error('处理文章点击事件失败：', error)
  }
}

// 刷新热门内容
const refreshHotContents = async () => {
  console.log('刷新热门内容...')
  await loadHotContents()
}

// 点赞/取消点赞功能
const toggleLike = async (item, listType) => {
  if (!currentUser.value?.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('切换点赞状态，内容ID：', item.id, '当前状态：', item.isLiked)
    
    const response = await likeApi.toggleLike(currentUser.value.id, item.id, 1)
    
    if (response && response.data) {
      // 更新本地数据
      const isLiked = response.data.isLiked
      const likeCount = response.data.likeCount
      
      // 根据列表类型更新对应的数据
      if (listType === 'recommended') {
        const index = recommendedContents.value.findIndex(content => content.id === item.id)
        if (index !== -1) {
          recommendedContents.value[index].isLiked = isLiked
          recommendedContents.value[index].likeCount = likeCount
        }
      } else if (listType === 'hot') {
        const index = hotContents.value.findIndex(content => content.id === item.id)
        if (index !== -1) {
          hotContents.value[index].isLiked = isLiked
          hotContents.value[index].likeCount = likeCount
        }
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
  min-height: 100vh;
  background-color: #f3f4f6;
  padding-bottom: 100rpx;
  animation: homePageFadeIn 0.4s ease-out;
}

@keyframes homePageFadeIn {
  0% {
    opacity: 0;
    transform: scale(0.98);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 顶部导航 */
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
  align-items: center;
  padding: 8px 16px;
  box-sizing: border-box;
  width: 100%;
  height: 56px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.search-box {
  flex: 1;
  margin: 0 12px;
  height: 36px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 0 12px;
  height: 100%;
  cursor: pointer;
}

.search-icon {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  width: 14px;
  text-align: center;
}

.input {
  flex: 1;
  margin: 0 8px;
  color: white;
  font-size: 14px;
  height: 100%;
  line-height: 36px;
  pointer-events: none;
}

.search-placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.header-right {
  display: flex;
  align-items: center;
}

.icon-wrapper {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-size: 20px;
  color: #ffffff;
}



/* 主要内容区域 */
.main-content {
  margin-top: 120rpx;
  height: calc(100vh - 120rpx);
}

/* 轮播图 */
.banner-swiper {
  width: 100%;
  height: 360rpx;
  margin-bottom: 20rpx;
}

.swiper-item {
  width: 100%;
  height: 100%;
}

.banner-item {
  position: relative;
  width: 100%;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
}

.banner-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 500;
  background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
  border-radius: 0 0 16rpx 16rpx;
}

/* 修改指示点样式 */
:deep(.uni-swiper-dots-horizontal) {
  bottom: 20rpx;
}

:deep(.uni-swiper-dot) {
  width: 8rpx;
  height: 8rpx;
  border-radius: 4rpx;
  background-color: rgba(255,255,255,0.6);
  transition: all 0.2s ease;
}

:deep(.uni-swiper-dot-active) {
  width: 16rpx;
  background-color: #ffffff;
}

/* 功能区 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  padding: 0 16px;
  margin-bottom: 24px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.feature-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.blue {
  background-color: #dbeafe;
}

.purple {
  background-color: #ede9fe;
}

.green {
  background-color: #d1fae5;
}

.yellow {
  background-color: #fef3c7;
}

.feature-icon {
  font-size: 18px;
}

.blue .feature-icon {
  color: #3b82f6;
}

.purple .feature-icon {
  color: #8b5cf6;
}

.green .feature-icon {
  color: #10b981;
}

.yellow .feature-icon {
  color: #f59e0b;
}

.feature-text {
  font-size: 12px;
  color: #4b5563;
  text-align: center;
}

/* 推荐阅读 */
.recommend {
  margin-bottom: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
}

.more-link {
  font-size: 14px;
  color: #3b82f6;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.more-link:hover {
  color: #1d4ed8;
}

.more-link:active {
  opacity: 0.7;
}

.book-scroll {
  padding: 0 30rpx;
  white-space: nowrap;
}

.book-card {
  display: inline-block;
  width: 240rpx;
  margin-right: 20rpx;
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  height: 460rpx;
}

.book-cover {
  width: 100%;
  height: 300rpx;
  object-fit: cover;
}

.book-info {
  padding: 20rpx;
  height: 120rpx;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 28rpx;
  font-weight: 500;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  white-space: normal;
  line-height: 1.4;
  height: 3.9em;
}

.book-age {
  font-size: 24rpx;
  color: #6b7280;
}

.book-stats {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
}

.stats-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

.stats-text {
  font-size: 24rpx;
  color: #9ca3af;
  margin-left: 10rpx;
}

/* 热门阅读 */
.hot-reading {
  padding: 0 30rpx;
}

.refresh {
  font-size: 14px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
}

.refresh-text {
  font-size: 14px;
  color: #6b7280;
  margin-right: 10rpx;
}

.refresh-icon {
  font-size: 14px;
  color: #6b7280;
}

.hot-books {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.hot-book-item {
  display: flex;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.hot-book-item:hover {
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transform: translateY(-2rpx);
}

.hot-book-cover {
  width: 200rpx;
  height: 260rpx;
  border-radius: 12rpx;
  object-fit: cover;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.hot-book-info {
  flex: 1;
  margin-left: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.hot-book-content {
  flex: 1;
}

.hot-book-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.5;
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.hot-book-author {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 12rpx;
  display: block;
}

.hot-book-footer {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.hot-book-tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.hot-book-tag {
  display: inline-block;
  padding: 8rpx 20rpx;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #1e40af;
  font-size: 22rpx;
  border-radius: 24rpx;
  font-weight: 500;
}

.hot-book-stats {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background-color: #f9fafb;
  border-radius: 20rpx;
  transition: all 0.3s ease;
}

.stat-item:active {
  background-color: #f3f4f6;
  transform: scale(0.95);
}

.stat-icon {
  font-size: 24rpx;
  color: #9ca3af;
  transition: color 0.3s ease;
}

.stat-icon.liked {
  color: #3b82f6;
}

.stat-text {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 500;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}

/* 加载状态和无数据状态 */
.loading-card, .no-data-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200rpx;
}

.loading-item, .no-data-item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120rpx;
  background-color: #fff;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
}

.loading-placeholder, .no-data-placeholder {
  text-align: center;
  color: #9ca3af;
  font-size: 28rpx;
}

/* 添加点击效果 */
.book-card:active, .hot-book-item:active {
  transform: scale(0.98);
  transition: transform 0.1s ease;
}

.refresh:active {
  opacity: 0.7;
  transition: opacity 0.1s ease;
}
</style> 
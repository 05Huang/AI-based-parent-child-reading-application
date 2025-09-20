<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <image src="https://ui-avatars.com/api/?name=爸爸&background=3b82f6&color=fff&size=128" class="avatar" @click="navigateToProfile"></image>
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
      <!-- 分类导航 -->
      <scroll-view 
        scroll-x="true" 
        class="category-nav" 
        :scroll-left="scrollLeft"
        :show-scrollbar="false"
        :scroll-with-animation="true"
        :scroll-animation-duration="300"
        @scroll="onScroll"
        ref="scrollView"
      >
        <view class="tabs-wrapper">
          <view 
            v-for="(tab, index) in tabs" 
            :key="index"
            class="tab"
            :class="{ active: currentTab === index }"
            :id="'tab-' + index"
            @click="switchTab(index)"
          >
            {{ tab.name }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content">
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
          <text class="more-link">查看更多</text>
        </view>
        <scroll-view scroll-x="true" class="book-scroll">
          <view class="book-card">
            <image src="https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop" class="book-cover"></image>
            <view class="book-info">
              <text class="book-title">10分钟亲子互动游戏</text>
              <text class="book-age">亲子互动</text>
              <view class="book-stats">
                <text class="fas fa-eye stats-icon"></text>
                <text class="stats-text">2.1万</text>
              </view>
            </view>
          </view>
          <view class="book-card">
            <image src="https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&auto=format&fit=crop" class="book-cover"></image>
            <view class="book-info">
              <text class="book-title">如何培养孩子的专注力</text>
              <text class="book-age">教育心得</text>
              <view class="book-stats">
                <text class="fas fa-eye stats-icon"></text>
                <text class="stats-text">1.8万</text>
              </view>
            </view>
          </view>
          <view class="book-card">
            <image src="https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400&auto=format&fit=crop" class="book-cover"></image>
            <view class="book-info">
              <text class="book-title">春游必备：亲子户外活动指南</text>
              <text class="book-age">户外活动</text>
              <view class="book-stats">
                <text class="fas fa-eye stats-icon"></text>
                <text class="stats-text">1.5万</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 热门阅读改为热门文章 -->
      <view class="hot-reading">
        <view class="section-header">
          <text class="section-title">热门文章</text>
          <view class="refresh">
            <text class="refresh-text">换一换</text>
            <text class="fas fa-sync-alt refresh-icon"></text>
          </view>
        </view>
        <view class="hot-books">
          <view class="hot-book-item">
            <image src="https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=400&auto=format&fit=crop" class="hot-book-cover"></image>
            <view class="hot-book-info">
              <text class="hot-book-title">让孩子爱上学习的秘密</text>
              <text class="hot-book-author">育儿专家 王老师</text>
              <text class="hot-book-tags">教育心得 · 学习方法 · 家长经验</text>
              <view class="hot-book-stats">
                <view class="rating">
                  <text class="fas fa-thumbs-up rating-icon"></text>
                  <text class="rating-text">2.3k</text>
                </view>
                <view class="views">
                  <text class="fas fa-eye views-icon"></text>
                  <text class="views-text">3.2万</text>
                </view>
              </view>
            </view>
          </view>
          <view class="hot-book-item">
            <image src="https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=400&auto=format&fit=crop" class="hot-book-cover"></image>
            <view class="hot-book-info">
              <text class="hot-book-title">亲子厨房：让烘焙成为周末的仪式感</text>
              <text class="hot-book-author">美食达人 李妈妈</text>
              <text class="hot-book-tags">亲子互动 · 美食 · 生活技能</text>
              <view class="hot-book-stats">
                <view class="rating">
                  <text class="fas fa-thumbs-up rating-icon"></text>
                  <text class="rating-text">1.8k</text>
                </view>
                <view class="views">
                  <text class="fas fa-eye views-icon"></text>
                  <text class="views-text">2.8万</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
  </view>
</template>

<script setup>
import { onMounted, ref, nextTick } from 'vue'

// 页面加载时检查登录状态
onMounted(() => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.redirectTo({
      url: '/pages/parent/login/login'
    })
  }
})

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

// 导航标签数据
const tabs = ref([
  { name: '推荐', id: 'recommend' },
  { name: '热门', id: 'hot' },
  { name: '亲子互动', id: 'interaction' },
  { name: '育儿经验', id: 'parenting' },
  { name: '教育心得', id: 'education' },
  { name: '亲子游戏', id: 'games' },
  { name: '营养健康', id: 'health' },
  { name: '心理成长', id: 'psychology' }
])

// 当前选中的标签索引
const currentTab = ref(0)
// 滚动位置
const scrollLeft = ref(0)
// scroll-view 引用
const scrollView = ref(null)

// 切换标签
const switchTab = async (index) => {
  if (currentTab.value === index) return
  
  currentTab.value = index
  
  // 等待DOM更新
  await nextTick()
  
  // 获取被点击的标签元素
  const query = uni.createSelectorQuery().in(this)
  query.select(`#tab-${index}`).boundingClientRect()
  query.select('.category-nav').boundingClientRect()
  
  query.exec((res) => {
    if (!res[0] || !res[1]) return
    
    const tabElement = res[0]
    const scrollView = res[1]
    
    // 计算需要滚动的距离
    // 目标：将选中的标签滚动到最左侧
    const newScrollLeft = tabElement.left - scrollView.left
    
    // 设置滚动位置（会触发动画）
    scrollLeft.value = newScrollLeft
  })
}

// 监听滚动事件
const onScroll = (e) => {
  scrollLeft.value = e.detail.scrollLeft
}
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding-bottom: 100rpx;
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

/* 分类导航样式 */
.category-nav {
  width: 100%;
  background-color: #f3f4f6;
  padding: 8px 0;
  white-space: nowrap;
  border-bottom: 1px solid #e5e7eb;
  overflow: hidden;
}

.tabs-wrapper {
  display: inline-flex;
  padding: 0 16px;
  gap: 12px;
  transition: transform 0.3s ease;
}

/* 隐藏滚动条但保持可滚动 */
.category-nav ::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}

/* 适配不同浏览器 */
.category-nav {
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}

.tab {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 14px;
  font-size: 13px;
  color: #6b7280;
  background-color: #ffffff;
  transition: all 0.3s ease;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
}

.tab::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background-color: #3b82f6;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.tab.active {
  background-color: #3b82f6;
  color: white;
  transform: scale(1.05);
}

.tab.active::after {
  width: 50%;
}

/* 主要内容区域 */
.main-content {
  margin-top: 200rpx;
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
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
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
  height: 440rpx;
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
  border-radius: 20rpx;
  padding: 20rpx;
}

.hot-book-cover {
  width: 180rpx;
  height: 240rpx;
  border-radius: 10rpx;
  object-fit: cover;
}

.hot-book-info {
  flex: 1;
  margin-left: 20rpx;
}

.hot-book-title {
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 12rpx;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-book-author {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 10rpx;
}

.hot-book-tags {
  font-size: 24rpx;
  color: #9ca3af;
  margin-bottom: 20rpx;
}

.hot-book-stats {
  display: flex;
  align-items: center;
}

.rating {
  display: flex;
  align-items: center;
  margin-right: 30rpx;
}

.rating-icon {
  color: #f43f5e;
  font-size: 24rpx;
}

.rating-text {
  font-size: 24rpx;
  color: #6b7280;
  margin-left: 10rpx;
}

.views {
  display: flex;
  align-items: center;
}

.views-icon {
  color: #9ca3af;
  font-size: 24rpx;
}

.views-text {
  font-size: 24rpx;
  color: #6b7280;
  margin-left: 10rpx;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}
</style> 
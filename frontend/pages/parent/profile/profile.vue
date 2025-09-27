<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-content">
        <text class="header-title">个人中心</text>
        <view class="header-actions">
          <text class="fas fa-cog" @click="navigateToSettings"></text>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 用户信息卡片 -->
      <view class="user-card">
        <view class="user-info">
          <image class="avatar" :src="currentUser.avatar" mode="aspectFill"></image>
          <view class="user-details">
            <text class="nickname">{{currentUser.nickname}}</text>
            <text class="username">@{{currentUser.username}}</text>
          </view>
        </view>
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-number">{{userStats.followingCount}}</text>
            <text class="stat-label">关注</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{userStats.followersCount}}</text>
            <text class="stat-label">粉丝</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{userStats.readingLevel}}</text>
            <text class="stat-label">阅读等级</text>
          </view>
        </view>
      </view>

      <!-- 功能区 -->
      <view class="feature-grid">
        <view class="feature-item" @click="navigateToReadingRecord">
          <view class="feature-icon blue">
            <text class="fas fa-book"></text>
          </view>
          <text class="feature-text">阅读记录</text>
        </view>
        <view class="feature-item" @click="navigateToFavorites">
          <view class="feature-icon pink">
            <text class="fas fa-star"></text>
          </view>
          <text class="feature-text">我的收藏</text>
        </view>
        <view class="feature-item" @click="navigateToHistory">
          <view class="feature-icon purple">
            <text class="fas fa-history"></text>
          </view>
          <text class="feature-text">阅读历史</text>
        </view>
        <view class="feature-item" @click="navigateToReport">
          <view class="feature-icon green">
            <text class="fas fa-chart-line"></text>
          </view>
          <text class="feature-text">阅读报告</text>
        </view>
      </view>

    

      <!-- 亲密度排行 -->
      <view class="intimacy-ranking">
        <view class="section-header">
          <text class="section-title">亲密度排行</text>
          <view class="view-all" @click="navigateToIntimacyRanking">
            <text class="view-all-text">查看全部</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="ranking-list">
          <view v-for="(item, index) in intimacyRankingPreview" :key="index" class="ranking-item">
            <view class="ranking-left">
              <view class="rank-number" :class="{'rank-first': item.rank === 1}">{{item.rank}}</view>
              <image class="rank-avatar" :src="item.avatar" mode="aspectFill"></image>
              <view class="rank-info">
                <text class="rank-name">{{item.name}}</text>
                <text class="rank-intimacy">亲密度: {{item.intimacy}}%</text>
              </view>
            </view>
            <view class="trend-tag" :class="item.trend">
              <text class="fas" :class="item.trend === 'up' ? 'fa-arrow-up' : 'fa-arrow-down'"></text>
              <text>{{item.trend === 'up' ? '上升' : '下降'}}</text>
            </view>
          </view>
        </view>
      </view>

      
    </view>

    <!-- 底部导航栏 -->
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi, intimacyApi, userBehaviorApi } from '@/utils/api.js'

// 响应式状态
const currentUser = ref({
  nickname: '张爸爸',
  username: 'zhangbaba',
  id: 888888,
  avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=dad123&backgroundColor=b6e3f4'
})

const userStats = ref({
  followingCount: 0,
  followersCount: 0,
  readingLevel: 0
})

const intimacyRankingPreview = ref([])

// 页面跳转方法
const navigateToIntimacyRanking = () => {
  console.log('即将跳转到亲密度排行榜页面')
  uni.navigateTo({
    url: '/pages/parent/intimacy-ranking/intimacy-ranking',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加阅读记录跳转方法
const navigateToReadingRecord = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/readTheTranscript',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加收藏页面跳转方法
const navigateToFavorites = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/favorites',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加阅读历史跳转方法
const navigateToHistory = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/history',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加阅读报告跳转方法
const navigateToReport = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/report',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加设置页面跳转方法
const navigateToSettings = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/setting',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    console.log('开始获取当前用户信息')
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      console.log('获取用户信息成功：', response.data)
      currentUser.value = {
        nickname: response.data.nickname || '张爸爸',
        username: response.data.username || 'zhangbaba',
        id: response.data.id || 888888,
        avatar: response.data.avatar || 'https://api.dicebear.com/7.x/bottts/svg?seed=dad123&backgroundColor=b6e3f4'
      }
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    uni.showToast({
      title: '获取用户信息失败',
      icon: 'none'
    })
  }
}

// 获取用户统计数据
const loadUserStats = async () => {
  try {
    if (!currentUser.value.id) return
    
    console.log('开始获取用户统计数据，用户ID：', currentUser.value.id)
    
    // 获取浏览统计 - 这里可以作为阅读等级的参考
    const browsingStats = await userBehaviorApi.getBrowsingStats(currentUser.value.id)
    console.log('获取浏览统计成功：', browsingStats)
    
    if (browsingStats && browsingStats.data) {
      // 根据阅读时长和阅读数量计算阅读等级
      const readingLevel = Math.min(100, Math.floor((browsingStats.data.totalReadingTime || 0) / 60) + (browsingStats.data.totalReadCount || 0))
      userStats.value.readingLevel = readingLevel
    }
    
    // 这里关注和粉丝数量暂时设为固定值，后续可以根据实际业务需求调整
    userStats.value.followingCount = 128
    userStats.value.followersCount = 256
    
  } catch (error) {
    console.error('获取用户统计数据失败：', error)
    // 不显示错误提示，使用默认值
  }
}

// 获取亲密度排行榜预览数据
const loadIntimacyRankingPreview = async () => {
  try {
    if (!currentUser.value.id) {
      console.log('用户ID不存在，跳过获取亲密度排行榜预览')
      return
    }
    
    console.log('开始获取亲密度排行榜预览数据，用户ID：', currentUser.value.id)
    const response = await intimacyApi.getUserRanking(currentUser.value.id)
    
    console.log('亲密度接口响应：', response)
    
    if (response && response.data && response.data.ranking) {
      console.log('获取亲密度排行榜成功，数据：', response.data.ranking)
      
      // 过滤掉可能的孩子用户，然后只取前2名用于预览
      const filteredRanking = response.data.ranking.filter(item => {
        // 简单过滤：排除昵称中包含"儿子"、"女儿"、"孩子"等关键词的用户
        const childKeywords = ['儿子', '女儿', '孩子', '宝宝', '小朋友'];
        const nickname = item.nickname || '';
        return !childKeywords.some(keyword => nickname.includes(keyword));
      });
      
      intimacyRankingPreview.value = filteredRanking.slice(0, 2).map((item, index) => ({
        rank: index + 1,
        name: item.nickname || `用户${item.rank}`,
        avatar: item.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${index}`,
        intimacy: Math.round(item.percentage || 0),
        trend: index === 0 ? 'up' : 'down' // 简单的趋势显示
      }))
      
      console.log('亲密度排行榜预览数据处理完成：', intimacyRankingPreview.value)
    } else {
      console.log('响应数据格式不正确或为空，使用默认数据')
      intimacyRankingPreview.value = [
        {
          rank: 1,
          name: '小明',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming345',
          intimacy: 98,
          trend: 'up'
        },
        {
          rank: 2,
          name: '妈妈',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom456',
          intimacy: 95,
          trend: 'down'
        }
      ]
    }
  } catch (error) {
    console.error('获取亲密度排行榜预览失败，错误详情：', error)
    
    // 使用默认数据，不显示错误提示
    intimacyRankingPreview.value = [
      {
        rank: 1,
        name: '小明',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming345',
        intimacy: 98,
        trend: 'up'
      },
      {
        rank: 2,
        name: '妈妈',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom456',
        intimacy: 95,
        trend: 'down'
      }
    ]
  }
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('个人中心页面已挂载，开始加载数据')
  await loadCurrentUser()
  await loadUserStats()
  await loadIntimacyRankingPreview()
})
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* 顶部导航栏样式 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: rgba(59, 130, 246, 0.98);  /* 略微透明效果 */
  backdrop-filter: blur(10px);  /* 毛玻璃效果 */
  -webkit-backdrop-filter: blur(10px);
  z-index: 50;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  max-width: 1200rpx;  /* 内容最大宽度限制 */
  margin: 0 auto;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
}

.header-actions {
  font-size: 40rpx;
  color: #ffffff;
}

/* 主要内容区域 */
.main-content {
  margin-top: 88rpx;
}

/* 用户信息卡片 */
.user-card {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  padding: 40rpx 32rpx 32rpx 32rpx;
  margin: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.15);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);  /* 头像阴影 */
}

.user-details {
  margin-left: 32rpx;
  flex: 1;
}

.nickname {
  font-size: 40rpx;
  font-weight: 600;
  letter-spacing: -0.5rpx;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.username {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 400;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  backdrop-filter: blur(10px);
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-number {
  font-size: 32rpx;
  font-weight: 700;
  display: block;
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: 22rpx;
  opacity: 0.9;
  font-weight: 400;
}



/* 功能区 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background-color: #ffffff;
  margin: 24rpx;
  padding: 40rpx 20rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  gap: 20rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 8rpx;
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.feature-item:active {
  transform: scale(0.95);
  background-color: #f8fafc;
}

.feature-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  font-size: 40rpx;
  transition: all 0.3s ease;
}

.feature-icon.blue {
  background-color: #eff6ff;
  color: #3b82f6;
}

.feature-icon.pink {
  background-color: #fce7f3;
  color: #ec4899;
}

.feature-icon.purple {
  background-color: #f3e8ff;
  color: #9333ea;
}

.feature-icon.green {
  background-color: #ecfdf5;
  color: #10b981;
}

.feature-text {
  font-size: 26rpx;
  color: #4b5563;
  font-weight: 500;
  text-align: center;
  line-height: 1.2;
}

/* 阅读成就 */
.achievements {
  background-color: #ffffff;
  margin: 24rpx;  /* 添加外边距 */
  border-radius: 24rpx;  /* 统一的圆角 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);  /* 柔和的阴影 */
  overflow: hidden;  /* 确保圆角生效 */
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid rgba(229, 231, 235, 0.6);
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  letter-spacing: -0.5rpx;
}

.achievement-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32rpx;
  padding: 32rpx;
}

.achievement-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.achievement-icon {
  width: 144rpx;  /* 略微增大图标 */
  height: 144rpx;
  border-radius: 32rpx;  /* 方形圆角 */
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);  /* 添加阴影 */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  font-size: 48rpx;
}

.achievement-icon.yellow {
  background-color: #fef3c7;
  color: #f59e0b;
}

.achievement-icon.blue {
  background-color: #eff6ff;
  color: #3b82f6;
}

.achievement-icon.green {
  background-color: #ecfdf5;
  color: #10b981;
}

.achievement-text {
  font-size: 28rpx;
}

/* 亲密度排行 */
.intimacy-ranking {
  background-color: #ffffff;
  margin: 24rpx;  /* 添加外边距 */
  border-radius: 24rpx;  /* 统一的圆角 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);  /* 柔和的阴影 */
  overflow: hidden;
}

.view-all {
  display: flex;
  align-items: center;
  color: #9ca3af;
}

.view-all-text {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.view-all .fas {
  font-size: 24rpx;
}

.ranking-list {
  padding: 32rpx;
}

.ranking-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.ranking-left {
  display: flex;
  align-items: center;
}

.rank-number {
  width: 56rpx;  /* 稍微缩小数字圆圈 */
  height: 56rpx;
  border-radius: 16rpx;  /* 方形圆角 */
  background-color: #e5e7eb;
  color: #4b5563;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 28rpx;
  margin-right: 24rpx;
}

.rank-number.rank-first {
  background-color: #fbbf24;
  color: #ffffff;
}

.rank-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 22rpx;  /* 方形圆角 */
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);  /* 添加阴影 */
  margin-right: 24rpx;
}

.rank-info {
  display: flex;
  flex-direction: column;
}

.rank-name {
  font-size: 30rpx;
  font-weight: 500;
  letter-spacing: -0.3rpx;
}

.rank-intimacy {
  font-size: 24rpx;
  color: #6b7280;
}

.trend-tag {
  padding: 8rpx 20rpx;
  border-radius: 12rpx;  /* 更小的圆角 */
  font-size: 24rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.trend-tag.up {
  background-color: #ecfdf5;
  color: #10b981;
}

.trend-tag.down {
  background-color: #fef2f2;
  color: #ef4444;
}

.trend-tag .fas {
  margin-right: 8rpx;
}

/* 设置选项 */
.settings {
  background-color: #ffffff;
  margin: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  padding: 8rpx 32rpx;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid rgba(229, 231, 235, 0.6);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-left {
  display: flex;
  align-items: center;
}

.setting-icon {
  width: 48rpx;
  font-size: 36rpx;
  color: #9ca3af;
  opacity: 0.7;
  text-align: center;
}

.setting-text {
  margin-left: 24rpx;
  font-size: 32rpx;
  color: #1f2937;
}

.arrow-icon {
  color: #9ca3af;
  font-size: 28rpx;
  opacity: 0.7;
  width: 40rpx;
  text-align: right;
  margin-left: auto;
}

/* 添加一些交互效果 */
.feature-icon:active,
.achievement-icon:active {
  transform: scale(0.96);
}

/* 优化字体层级 */
.section-title {
  font-size: 34rpx;
  font-weight: 600;
  letter-spacing: -0.5rpx;
}

.rank-name {
  font-size: 30rpx;
  font-weight: 500;
  letter-spacing: -0.3rpx;
}

/* 统一图标颜色透明度 */
.setting-icon,
.arrow-icon {
  opacity: 0.7;
}
</style> 
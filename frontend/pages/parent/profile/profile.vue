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
          <image class="avatar" src="https://api.dicebear.com/7.x/bottts/svg?seed=dad123&backgroundColor=b6e3f4" mode="aspectFill"></image>
          <view class="user-details">
            <text class="username">张爸爸</text>
            <text class="user-id">ID: 888888</text>
          </view>
        </view>
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-number">128</text>
            <text class="stat-label">关注</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">256</text>
            <text class="stat-label">粉丝</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">98</text>
            <text class="stat-label">阅读等级</text>
          </view>
        </view>
        <button class="edit-profile-btn">编辑资料</button>
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
          <view class="ranking-item">
            <view class="ranking-left">
              <view class="rank-number rank-first">1</view>
              <image class="rank-avatar" src="https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming345" mode="aspectFill"></image>
              <view class="rank-info">
                <text class="rank-name">小明</text>
                <text class="rank-intimacy">亲密度: 98%</text>
              </view>
            </view>
            <view class="trend-tag up">
              <text class="fas fa-arrow-up"></text>
              <text>上升</text>
            </view>
          </view>
          <view class="ranking-item">
            <view class="ranking-left">
              <view class="rank-number">2</view>
              <image class="rank-avatar" src="https://api.dicebear.com/7.x/avataaars/svg?seed=mom456" mode="aspectFill"></image>
              <view class="rank-info">
                <text class="rank-name">妈妈</text>
                <text class="rank-intimacy">亲密度: 95%</text>
              </view>
            </view>
            <view class="trend-tag down">
              <text class="fas fa-arrow-down"></text>
              <text>下降</text>
            </view>
          </view>
        </view>
      </view>

      
    </view>

    <!-- 底部导航栏 -->
  </view>
</template>

<script setup>
// 页面跳转方法
const navigateToIntimacyRanking = () => {
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
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f5f7fa;  /* 更柔和的背景色 */
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom)); /* 增加底部padding */
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
  background: linear-gradient(135deg, #3b82f6, #60a5fa);  /* 更动感的渐变角度 */
  padding: 40rpx 32rpx;  /* 增加上下内边距 */
  margin: 24rpx;  /* 添加外边距 */
  border-radius: 24rpx;  /* 更大的圆角 */
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.15);  /* 柔和的阴影 */
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
}

.username {
  font-size: 40rpx;  /* 略微增大字号 */
  font-weight: 600;  /* 使用更现代的字重 */
  letter-spacing: -0.5rpx;  /* 字母间距调整 */
  color: #ffffff;
}

.user-id {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.stats-row {
  display: flex;
  justify-content: space-between;
  color: #ffffff;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 36rpx;
  font-weight: bold;
}

.stat-label {
  font-size: 24rpx;
  opacity: 0.8;
}

.edit-profile-btn {
  margin-top: 32rpx;
  width: 100%;
  background-color: rgba(255, 255, 255, 0.15);  /* 更透明的背景 */
  color: #ffffff;
  padding: 16rpx;
  border-radius: 16rpx;  /* 更小的圆角 */
  font-size: 28rpx;
  text-align: center;
  backdrop-filter: blur(5px);  /* 毛玻璃效果 */
  -webkit-backdrop-filter: blur(5px);
  transition: all 0.3s ease;  /* 添加过渡效果 */
}

/* 功能区 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background-color: #ffffff;
  margin: 24rpx;  /* 添加外边距 */
  padding: 32rpx;
  border-radius: 24rpx;  /* 统一的圆角 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);  /* 柔和的阴影 */
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.feature-icon {
  width: 108rpx;  /* 略微增大图标 */
  height: 108rpx;
  border-radius: 24rpx;  /* 方形圆角 */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  font-size: 44rpx;
  transition: transform 0.3s ease;  /* 添加过渡效果 */
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
  font-size: 24rpx;
  color: #4b5563;
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
  padding: 32rpx;
  border-bottom: 1rpx solid rgba(229, 231, 235, 0.6);  /* 更细腻的分割线 */
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
.achievement-icon:active,
.edit-profile-btn:active {
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
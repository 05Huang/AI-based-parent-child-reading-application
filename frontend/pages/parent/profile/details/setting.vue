<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">设置</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 账号设置 -->
      <view class="settings-section">
        <text class="section-title">账号设置</text>
        <view class="settings-list">
          <view class="settings-item" @click="handleSettingClick('profile')">
            <view class="item-left">
              <text class="fas fa-user item-icon"></text>
              <text class="item-text">个人资料</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @click="handleSettingClick('bindChild')">
            <view class="item-left">
              <text class="fas fa-child item-icon"></text>
              <text class="item-text">绑定家人</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @click="handleSettingClick('security')">
            <view class="item-left">
              <text class="fas fa-shield-alt item-icon"></text>
              <text class="item-text">账号安全</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @click="handleSettingClick('notification')">
            <view class="item-left">
              <text class="fas fa-bell item-icon"></text>
              <text class="item-text">消息通知</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 阅读设置 -->
      <view class="settings-section">
        <text class="section-title">阅读设置</text>
        <view class="settings-list">
          <view class="settings-item" @click="handleSettingClick('readingMode')">
            <view class="item-left">
              <text class="fas fa-book-reader item-icon"></text>
              <text class="item-text">阅读模式</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @click="handleSettingClick('readingReminder')">
            <view class="item-left">
              <text class="fas fa-clock item-icon"></text>
              <text class="item-text">阅读提醒</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @click="handleSettingClick('fontSetting')">
            <view class="item-left">
              <text class="fas fa-font item-icon"></text>
              <text class="item-text">字体设置</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 隐私设置 -->
      <view class="settings-section">
        <text class="section-title">隐私设置</text>
        <view class="settings-list">
          <view class="settings-item" @click="handleSettingClick('readingVisibility')">
            <view class="item-left">
              <text class="fas fa-eye item-icon"></text>
              <text class="item-text">阅读记录可见性</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @click="handleSettingClick('friendPermission')">
            <view class="item-left">
              <text class="fas fa-user-friends item-icon"></text>
              <text class="item-text">好友权限</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="settings-section">
        <text class="section-title">其他设置</text>
        <view class="settings-list">
          <view class="settings-item" @click="handleClearCache">
            <view class="item-left">
              <text class="fas fa-download item-icon"></text>
              <text class="item-text">清除缓存</text>
            </view>
            <text class="cache-size">23.5MB</text>
          </view>
          <view class="settings-item" @click="handleSettingClick('about')">
            <view class="item-left">
              <text class="fas fa-info-circle item-icon"></text>
              <text class="item-text">关于我们</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 退出登录按钮 -->
      <view class="logout-section">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 退出登录处理
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // 清除登录状态
        uni.removeStorageSync('token')
        // 跳转到登录页
        uni.reLaunch({
          url: '/pages/parent/login/login'
        })
      }
    }
  })
}

// 处理设置项点击
const handleSettingClick = (type) => {
  const routes = {
    profile: '/pages/parent/profile/details/profile',
    bindChild: '/pages/parent/profile/details/bind-child',
    security: '/pages/parent/profile/details/security',
    notification: '/pages/parent/profile/details/notification',
    readingMode: '/pages/parent/profile/details/reading-mode',
    readingReminder: '/pages/parent/profile/details/reading-reminder',
    fontSetting: '/pages/parent/profile/details/font-setting',
    readingVisibility: '/pages/parent/profile/details/reading-visibility',
    friendPermission: '/pages/parent/profile/details/friend-permission',
    about: '/pages/parent/profile/details/about'
  }

  if (routes[type]) {
    uni.navigateTo({
      url: routes[type]
    })
  }
}

// 处理清除缓存
const handleClearCache = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清除缓存吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '清除中...'
        })
        
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '清除成功',
            icon: 'success'
          })
        }, 1500)
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

/* 设置区块样式 */
.settings-section {
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
  padding: 0 20rpx;
}

.settings-list {
  background-color: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.settings-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.item-icon {
  width: 40rpx;
  font-size: 36rpx;
  color: #3b82f6;
  text-align: center;
}

.item-text {
  font-size: 30rpx;
  color: #111827;
}

.arrow-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

.cache-size {
  font-size: 28rpx;
  color: #6b7280;
}

/* 退出登录按钮样式 */
.logout-section {
  margin-top: 60rpx;
  padding: 0 20rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #ef4444;
  color: #ffffff;
  font-size: 32rpx;
  border-radius: 12rpx;
  text-align: center;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style>

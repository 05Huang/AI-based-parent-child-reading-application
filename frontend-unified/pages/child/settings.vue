<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <text class="fas fa-arrow-left back-icon" @tap="goBack"></text>
        <text class="page-title">设置</text>
        <text class="placeholder"></text>
      </view>
    </view>

    <!-- 设置列表 -->
    <view class="settings-content">
      <!-- 账号与安全 -->
      <view class="settings-section">
        <view class="section-title">账号与安全</view>
        <view class="settings-list">
          <view class="settings-item" @tap="navigateTo('/pages/child/settings/profile')">
            <view class="item-left">
              <text class="fas fa-user item-icon blue"></text>
              <text class="item-title">个人资料</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="navigateTo('/pages/child/settings/security')">
            <view class="item-left">
              <text class="fas fa-shield-alt item-icon green"></text>
              <text class="item-title">账号安全</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="navigateTo('/pages/child/settings/privacy')">
            <view class="item-left">
              <text class="fas fa-lock item-icon purple"></text>
              <text class="item-title">隐私设置</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 阅读设置 -->
      <view class="settings-section">
        <view class="section-title">阅读设置</view>
        <view class="settings-list">
          <view class="settings-item" @tap="navigateTo('/pages/child/settings/reading-mode')">
            <view class="item-left">
              <text class="fas fa-book-reader item-icon orange"></text>
              <text class="item-title">阅读模式</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="navigateTo('/pages/child/settings/font')">
            <view class="item-left">
              <text class="fas fa-font item-icon blue"></text>
              <text class="item-title">字体设置</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="settings-section">
        <view class="section-title">通知设置</view>
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-bell item-icon yellow"></text>
              <text class="item-title">推送通知</text>
            </view>
            <switch :checked="notificationsEnabled" @change="toggleNotifications" color="#6366f1" />
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-volume-up item-icon green"></text>
              <text class="item-title">声音提醒</text>
            </view>
            <switch :checked="soundEnabled" @change="toggleSound" color="#6366f1" />
          </view>
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="settings-section">
        <view class="section-title">其他</view>
        <view class="settings-list">
          <view class="settings-item" @tap="navigateTo('/pages/child/settings/about')">
            <view class="item-left">
              <text class="fas fa-info-circle item-icon purple"></text>
              <text class="item-title">关于我们</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="navigateTo('/pages/child/settings/help')">
            <view class="item-left">
              <text class="fas fa-question-circle item-icon blue"></text>
              <text class="item-title">帮助与反馈</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 退出登录按钮 -->
      <view class="logout-button" @tap="handleLogout">
        退出登录
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      notificationsEnabled: true,
      soundEnabled: false
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    navigateTo(url) {
      uni.navigateTo({ url })
    },
    toggleNotifications(e) {
      this.notificationsEnabled = e.detail.value
    },
    toggleSound(e) {
      this.soundEnabled = e.detail.value
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // TODO: 实现退出登录逻辑
            uni.reLaunch({
              url: '/pages/index/index'
            })
          }
        }
      })
    }
  }
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding-bottom: 40rpx;
}

.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 88rpx 32rpx 32rpx;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-icon {
  font-size: 40rpx;
  color: #ffffff;
  width: 40rpx;
}

.page-title {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
}

.placeholder {
  width: 40rpx;
}

.settings-content {
  margin-top: 180rpx;
  padding: 0 32rpx;
}

.settings-section {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
  padding: 0 16rpx;
}

.settings-list {
  background-color: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 24rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.settings-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.item-icon {
  font-size: 40rpx;
  width: 40rpx;
  text-align: center;
}

.blue {
  color: #3b82f6;
}

.green {
  color: #10b981;
}

.purple {
  color: #8b5cf6;
}

.orange {
  color: #f59e0b;
}

.yellow {
  color: #fbbf24;
}

.item-title {
  font-size: 30rpx;
  color: #1f2937;
}

.arrow-icon {
  color: #9ca3af;
  font-size: 28rpx;
}

.logout-button {
  margin: 60rpx 32rpx;
  background-color: #ffffff;
  color: #ef4444;
  font-size: 32rpx;
  text-align: center;
  padding: 24rpx 0;
  border-radius: 16rpx;
}
</style> 
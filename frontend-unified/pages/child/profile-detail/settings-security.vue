<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-btn" @tap="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">账号安全</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      <view class="section">
        <view class="settings-list">
          <view class="settings-item" @tap="changePassword">
            <view class="item-left">
              <text class="fas fa-lock item-icon"></text>
              <text class="item-title">修改密码</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-mobile-alt item-icon"></text>
              <text class="item-title">绑定手机</text>
            </view>
            <text class="status-text">已绑定</text>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-shield-alt item-icon"></text>
              <text class="item-title">登录保护</text>
            </view>
            <switch checked color="#6366f1" @change="handleLoginProtection" />
          </view>
        </view>
      </view>

      <view class="tips">
        <text class="fas fa-info-circle"></text>
        <text class="tips-text">定期修改密码可以提高账号安全性</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 100
    }
  },
  onLoad() {
    // 获取系统状态栏高度
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight || 20
    this.headerHeight = this.statusBarHeight + 64 + 32
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    changePassword() {
      uni.navigateTo({
        url: '/pages/child/profile-detail/change-password'
      })
    },
    handleLoginProtection(e) {
      console.log('登录保护：', e.detail.value)
    }
  }
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
}

/* 顶部导航栏样式 */
.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 40rpx 32rpx;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 36rpx;
  transition: all 0.3s ease;
}

.back-btn:active {
  transform: scale(0.9);
  opacity: 0.8;
}

.page-title {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
  flex: 1;
  text-align: center;
}

.header-placeholder {
  width: 64rpx;
}

/* 主内容区域样式 */
.main-content {
  padding: 24rpx;
  padding-bottom: 40rpx;
  width: 100%;
  box-sizing: border-box;
}

.section {
  margin-bottom: 32rpx;
}

.settings-list {
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.settings-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
}

.item-icon {
  font-size: 40rpx;
  color: #6366f1;
  margin-right: 24rpx;
  width: 40rpx;
  text-align: center;
}

.item-title {
  font-size: 28rpx;
  color: #1f2937;
}

.arrow-icon {
  color: #d1d5db;
  font-size: 32rpx;
}

.status-text {
  font-size: 24rpx;
  color: #6b7280;
}

.tips {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f3e8ff;
  border-radius: 16rpx;
  margin-top: 48rpx;
}

.tips .fa-info-circle {
  color: #8b5cf6;
  font-size: 32rpx;
  margin-right: 16rpx;
}

.tips-text {
  font-size: 24rpx;
  color: #6b7280;
}
</style> 
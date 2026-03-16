<template>
  <view class="container">
    <view class="header">
      <view class="header-content">
        <view class="back-button" @tap="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">关于我们</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-content">
      <view class="app-info">
        <image src="/static/images/common/child-logo.svg" class="app-logo"></image>
        <text class="app-name">阅桥亲子阅读APP</text>
        <text class="app-version">版本 1.0.0</text>
      </view>

      <view class="section">
        <view class="settings-list">
          <view class="settings-item" @tap="checkUpdate">
            <view class="item-left">
              <text class="fas fa-sync-alt item-icon"></text>
              <text class="item-title">检查更新</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="goToPrivacyPolicy">
            <view class="item-left">
              <text class="fas fa-shield-alt item-icon"></text>
              <text class="item-title">隐私政策</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="goToUserAgreement">
            <view class="item-left">
              <text class="fas fa-file-alt item-icon"></text>
              <text class="item-title">用户协议</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="contactUs">
            <view class="item-left">
              <text class="fas fa-envelope item-icon"></text>
              <text class="item-title">联系我们</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <view class="app-description">
        <text class="description-text">阅桥亲子阅读APP是一款专注于提升亲子关系的阅读应用。我们致力于为家长和孩子提供优质的阅读内容和互动体验，让阅读成为增进亲子感情的桥梁。</text>
      </view>

      <view class="copyright">
        <text class="copyright-text">© 2024 阅桥亲子阅读APP. All rights reserved.</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    checkUpdate() {
      uni.showLoading({
        title: '检查更新中'
      })
      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({
          title: '已是最新版本',
          icon: 'success'
        })
      }, 1500)
    },
    goToPrivacyPolicy() {
      uni.navigateTo({
        url: '/pages/common/privacy-policy'
      })
    },
    goToUserAgreement() {
      uni.navigateTo({
        url: '/pages/common/user-agreement'
      })
    },
    contactUs() {
      uni.showModal({
        title: '联系我们',
        content: '邮箱：msong@jsnu.edu.cn\n电话：18012046881',
        showCancel: false
      })
    }
  }
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 40rpx 32rpx;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  position: relative;
}

.back-button {
  padding: 16rpx;
  position: absolute;
  left: 0;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
}

.main-content {
  margin-top: 120rpx;
  padding: 32rpx;
  height: calc(100vh - 120rpx);
  box-sizing: border-box;
}

.app-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 0;
}

.app-logo {
  width: 200rpx;
  height: 200rpx;
  border-radius: 32rpx;
  margin-bottom: 24rpx;
}

.app-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8rpx;
}

.app-version {
  font-size: 24rpx;
  color: #6b7280;
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

.app-description {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.description-text {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.6;
}

.copyright {
  text-align: center;
  padding: 32rpx 0;
}

.copyright-text {
  font-size: 24rpx;
  color: #9ca3af;
}
</style> 

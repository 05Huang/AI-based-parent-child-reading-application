# 创建新的账号安全页面
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">账号安全</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 登录密码 -->
      <view class="security-section">
        <view class="security-item" @click="handlePasswordChange">
          <view class="item-left">
            <text class="fas fa-lock item-icon"></text>
            <view class="item-info">
              <text class="item-title">登录密码</text>
              <text class="item-desc">建议定期更换密码，确保账号安全</text>
            </view>
          </view>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>

        <!-- 手机号码 -->
        <view class="security-item" @click="handlePhoneChange">
          <view class="item-left">
            <text class="fas fa-mobile-screen item-icon"></text>
            <view class="item-info">
              <text class="item-title">手机号码</text>
              <text class="item-desc">已绑定：{{phoneNumber}}</text>
            </view>
          </view>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>

        <!-- 邮箱绑定 -->
        <view class="security-item" @click="handleEmailChange">
          <view class="item-left">
            <text class="fas fa-envelope item-icon"></text>
            <view class="item-info">
              <text class="item-title">邮箱绑定</text>
              <text class="item-desc">{{email || '未绑定'}}</text>
            </view>
          </view>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>
      </view>

      <!-- 安全设置 -->
      <view class="security-section">
        <view class="section-title">安全设置</view>
        
        <!-- 指纹解锁 -->
        <view class="security-item">
          <view class="item-left">
            <text class="fas fa-fingerprint item-icon"></text>
            <view class="item-info">
              <text class="item-title">指纹解锁</text>
              <text class="item-desc">使用指纹快速登录</text>
            </view>
          </view>
          <switch :checked="fingerprintEnabled" @change="handleFingerprintChange" color="#3b82f6" />
        </view>

        <!-- 人脸解锁 -->
        <view class="security-item">
          <view class="item-left">
            <text class="fas fa-face-smile item-icon"></text>
            <view class="item-info">
              <text class="item-title">人脸解锁</text>
              <text class="item-desc">使用人脸快速登录</text>
            </view>
          </view>
          <switch :checked="faceIdEnabled" @change="handleFaceIdChange" color="#3b82f6" />
        </view>

        <!-- 登录验证 -->
        <view class="security-item">
          <view class="item-left">
            <text class="fas fa-shield-halved item-icon"></text>
            <view class="item-info">
              <text class="item-title">登录验证</text>
              <text class="item-desc">异地登录时需要验证</text>
            </view>
          </view>
          <switch :checked="loginVerificationEnabled" @change="handleLoginVerificationChange" color="#3b82f6" />
        </view>
      </view>

      <!-- 账号注销 -->
      <view class="delete-account" @click="handleDeleteAccount">
        <text class="delete-text">注销账号</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 用户安全信息
const phoneNumber = ref('138****8888')
const email = ref('exa***@example.com')
const fingerprintEnabled = ref(false)
const faceIdEnabled = ref(false)
const loginVerificationEnabled = ref(true)

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 修改密码
const handlePasswordChange = () => {
  uni.showModal({
    title: '修改密码',
    content: '确定要修改密码吗？',
    success: (res) => {
      if (res.confirm) {
        // TODO: 实现密码修改逻辑
        uni.showToast({
          title: '功能开发中',
          icon: 'none'
        })
      }
    }
  })
}

// 修改手机号
const handlePhoneChange = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 修改邮箱
const handleEmailChange = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 处理指纹解锁开关
const handleFingerprintChange = (e) => {
  fingerprintEnabled.value = e.detail.value
  uni.showToast({
    title: fingerprintEnabled.value ? '已开启指纹解锁' : '已关闭指纹解锁',
    icon: 'none'
  })
}

// 处理人脸解锁开关
const handleFaceIdChange = (e) => {
  faceIdEnabled.value = e.detail.value
  uni.showToast({
    title: faceIdEnabled.value ? '已开启人脸解锁' : '已关闭人脸解锁',
    icon: 'none'
  })
}

// 处理登录验证开关
const handleLoginVerificationChange = (e) => {
  loginVerificationEnabled.value = e.detail.value
  uni.showToast({
    title: loginVerificationEnabled.value ? '已开启登录验证' : '已关闭登录验证',
    icon: 'none'
  })
}

// 处理账号注销
const handleDeleteAccount = () => {
  uni.showModal({
    title: '注销账号',
    content: '注销后，账号将无法恢复，确定要注销吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '功能开发中',
          icon: 'none'
        })
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

/* 安全设置区块样式 */
.security-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
  overflow: hidden;
}

.section-title {
  font-size: 28rpx;
  color: #6b7280;
  padding: 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.security-item:last-child {
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

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.item-title {
  font-size: 30rpx;
  color: #111827;
}

.item-desc {
  font-size: 24rpx;
  color: #6b7280;
}

.arrow-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 注销账号按钮 */
.delete-account {
  background-color: #ffffff;
  padding: 30rpx;
  border-radius: 16rpx;
  text-align: center;
  margin-top: 60rpx;
}

.delete-text {
  color: #ef4444;
  font-size: 30rpx;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
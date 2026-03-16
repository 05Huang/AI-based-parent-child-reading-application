# 创建好友权限页面的基本模板
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">好友权限</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 添加好友设置 -->
      <view class="settings-section">
        <text class="section-title">添加好友设置</text>
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-qrcode item-icon"></text>
              <text class="item-text">通过二维码添加</text>
            </view>
            <switch :checked="addByQrcode" @change="handleQrcodeChange" color="#3b82f6" />
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-search item-icon"></text>
              <text class="item-text">通过手机号搜索</text>
            </view>
            <switch :checked="addByPhone" @change="handlePhoneChange" color="#3b82f6" />
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-id-card item-icon"></text>
              <text class="item-text">通过ID搜索</text>
            </view>
            <switch :checked="addById" @change="handleIdChange" color="#3b82f6" />
          </view>
        </view>
      </view>

      <!-- 验证设置 -->
      <view class="settings-section">
        <text class="section-title">验证设置</text>
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-user-check item-icon"></text>
              <text class="item-text">需要验证信息</text>
            </view>
            <switch :checked="requireVerification" @change="handleVerificationChange" color="#3b82f6" />
          </view>
          <view class="verification-message" v-if="requireVerification">
            <text class="message-label">验证问题</text>
            <input 
              type="text" 
              class="message-input"
              v-model="verificationMessage"
              placeholder="请输入验证问题"
            />
          </view>
        </view>
      </view>

      <!-- 互动权限 -->
      <view class="settings-section">
        <text class="section-title">互动权限</text>
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-comment-dots item-icon"></text>
              <text class="item-text">允许好友评论</text>
            </view>
            <switch :checked="allowComments" @change="handleCommentsChange" color="#3b82f6" />
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-share-alt item-icon"></text>
              <text class="item-text">允许好友分享</text>
            </view>
            <switch :checked="allowSharing" @change="handleSharingChange" color="#3b82f6" />
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-bell item-icon"></text>
              <text class="item-text">接收好友动态提醒</text>
            </view>
            <switch :checked="receiveNotifications" @change="handleNotificationsChange" color="#3b82f6" />
          </view>
        </view>
      </view>

      <!-- 黑名单管理 -->
      <view class="settings-section">
        <text class="section-title">黑名单管理</text>
        <view class="settings-list">
          <view class="settings-item" @click="navigateToBlacklist">
            <view class="item-left">
              <text class="fas fa-ban item-icon"></text>
              <text class="item-text">管理黑名单</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 保存按钮 -->
      <view class="save-section">
        <button class="save-btn" @click="handleSave">保存设置</button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 添加好友设置
const addByQrcode = ref(true)
const addByPhone = ref(true)
const addById = ref(true)

const handleQrcodeChange = (e) => {
  addByQrcode.value = e.detail.value
}

const handlePhoneChange = (e) => {
  addByPhone.value = e.detail.value
}

const handleIdChange = (e) => {
  addById.value = e.detail.value
}

// 验证设置
const requireVerification = ref(true)
const verificationMessage = ref('请输入验证信息')

const handleVerificationChange = (e) => {
  requireVerification.value = e.detail.value
}

// 互动权限
const allowComments = ref(true)
const allowSharing = ref(true)
const receiveNotifications = ref(true)

const handleCommentsChange = (e) => {
  allowComments.value = e.detail.value
}

const handleSharingChange = (e) => {
  allowSharing.value = e.detail.value
}

const handleNotificationsChange = (e) => {
  receiveNotifications.value = e.detail.value
}

// 导航到黑名单页面
const navigateToBlacklist = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/blacklist'
  })
}

// 保存设置
const handleSave = () => {
  // 保存设置到本地存储
  uni.setStorageSync('friendPermissionSettings', {
    addByQrcode: addByQrcode.value,
    addByPhone: addByPhone.value,
    addById: addById.value,
    requireVerification: requireVerification.value,
    verificationMessage: verificationMessage.value,
    allowComments: allowComments.value,
    allowSharing: allowSharing.value,
    receiveNotifications: receiveNotifications.value
  })
  
  uni.showToast({
    title: '设置已保存',
    icon: 'success'
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
  padding: 24rpx 20rpx;
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
  color: #3b82f6;
  font-size: 36rpx;
  width: 40rpx;
  text-align: center;
}

.item-text {
  font-size: 30rpx;
  color: #111827;
}

.arrow-icon {
  color: #9ca3af;
  font-size: 24rpx;
}

/* 验证消息样式 */
.verification-message {
  padding: 20rpx;
  background-color: #f9fafb;
  margin: 0 20rpx 20rpx;
  border-radius: 12rpx;
}

.message-label {
  display: block;
  font-size: 28rpx;
  color: #4b5563;
  margin-bottom: 12rpx;
}

.message-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background-color: #ffffff;
  border: 1rpx solid #e5e7eb;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #111827;
}

/* 保存按钮样式 */
.save-section {
  margin-top: 60rpx;
  padding: 0 20rpx;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #3b82f6;
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
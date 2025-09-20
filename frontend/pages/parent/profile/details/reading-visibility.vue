<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">阅读记录可见性</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 可见性设置 -->
      <view class="settings-section">
        <text class="section-title">阅读记录可见性</text>
        <view class="settings-list">
          <view class="visibility-item" :class="{ active: visibility === 'public' }" @click="setVisibility('public')">
            <view class="item-left">
              <text class="fas fa-globe item-icon"></text>
              <view class="item-info">
                <text class="item-title">公开</text>
                <text class="item-desc">所有人可见</text>
              </view>
            </view>
            <text class="fas fa-check check-icon" v-if="visibility === 'public'"></text>
          </view>
          <view class="visibility-item" :class="{ active: visibility === 'friends' }" @click="setVisibility('friends')">
            <view class="item-left">
              <text class="fas fa-user-friends item-icon"></text>
              <view class="item-info">
                <text class="item-title">仅好友可见</text>
                <text class="item-desc">仅对好友公开阅读记录</text>
              </view>
            </view>
            <text class="fas fa-check check-icon" v-if="visibility === 'friends'"></text>
          </view>
          <view class="visibility-item" :class="{ active: visibility === 'private' }" @click="setVisibility('private')">
            <view class="item-left">
              <text class="fas fa-lock item-icon"></text>
              <view class="item-info">
                <text class="item-title">私密</text>
                <text class="item-desc">仅自己可见</text>
              </view>
            </view>
            <text class="fas fa-check check-icon" v-if="visibility === 'private'"></text>
          </view>
        </view>
      </view>

      <!-- 详细设置 -->
      <view class="settings-section">
        <text class="section-title">详细设置</text>
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-chart-line item-icon"></text>
              <text class="item-text">显示阅读进度</text>
            </view>
            <switch :checked="showProgress" @change="handleProgressChange" color="#3b82f6" />
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-clock item-icon"></text>
              <text class="item-text">显示阅读时长</text>
            </view>
            <switch :checked="showDuration" @change="handleDurationChange" color="#3b82f6" />
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-comment-dots item-icon"></text>
              <text class="item-text">显示读后感</text>
            </view>
            <switch :checked="showReviews" @change="handleReviewsChange" color="#3b82f6" />
          </view>
        </view>
      </view>

      <!-- 自动分享设置 -->
      <view class="settings-section">
        <text class="section-title">自动分享设置</text>
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-share-alt item-icon"></text>
              <text class="item-text">完成阅读时自动分享</text>
            </view>
            <switch :checked="autoShare" @change="handleAutoShareChange" color="#3b82f6" />
          </view>
          <view class="auto-share-options" v-if="autoShare">
            <view class="share-option">
              <checkbox :checked="shareToTimeline" @change="handleTimelineChange" color="#3b82f6" />
              <text class="option-text">分享到朋友圈</text>
            </view>
            <view class="share-option">
              <checkbox :checked="shareToFriends" @change="handleFriendsChange" color="#3b82f6" />
              <text class="option-text">分享给好友</text>
            </view>
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

// 可见性设置
const visibility = ref('friends')
const setVisibility = (type) => {
  visibility.value = type
}

// 详细设置
const showProgress = ref(true)
const showDuration = ref(true)
const showReviews = ref(true)

const handleProgressChange = (e) => {
  showProgress.value = e.detail.value
}

const handleDurationChange = (e) => {
  showDuration.value = e.detail.value
}

const handleReviewsChange = (e) => {
  showReviews.value = e.detail.value
}

// 自动分享设置
const autoShare = ref(false)
const shareToTimeline = ref(false)
const shareToFriends = ref(false)

const handleAutoShareChange = (e) => {
  autoShare.value = e.detail.value
}

const handleTimelineChange = (e) => {
  shareToTimeline.value = e.detail.value
}

const handleFriendsChange = (e) => {
  shareToFriends.value = e.detail.value
}

// 保存设置
const handleSave = () => {
  // 保存设置到本地存储
  uni.setStorageSync('visibilitySettings', {
    visibility: visibility.value,
    showProgress: showProgress.value,
    showDuration: showDuration.value,
    showReviews: showReviews.value,
    autoShare: autoShare.value,
    shareToTimeline: shareToTimeline.value,
    shareToFriends: shareToFriends.value
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

/* 可见性选项样式 */
.visibility-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.visibility-item:last-child {
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

.item-info {
  display: flex;
  flex-direction: column;
}

.item-title {
  font-size: 30rpx;
  color: #111827;
}

.item-desc {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

.check-icon {
  color: #3b82f6;
  font-size: 24rpx;
}

/* 设置项样式 */
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

.item-text {
  font-size: 30rpx;
  color: #111827;
  margin-left: 20rpx;
}

/* 自动分享选项样式 */
.auto-share-options {
  padding: 20rpx;
  background-color: #f9fafb;
  margin: 0 20rpx 20rpx;
  border-radius: 12rpx;
}

.share-option {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.share-option:last-child {
  margin-bottom: 0;
}

.option-text {
  font-size: 28rpx;
  color: #4b5563;
  margin-left: 12rpx;
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
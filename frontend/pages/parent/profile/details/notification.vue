# 创建新的消息通知页面
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">消息通知</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 通知开关设置 -->
      <view class="notification-section">
        <view class="section-title">通知设置</view>
        
        <!-- 新消息通知 -->
        <view class="notification-item">
          <view class="item-left">
            <text class="fas fa-bell item-icon"></text>
            <view class="item-info">
              <text class="item-title">新消息通知</text>
              <text class="item-desc">接收新消息时通知提醒</text>
            </view>
          </view>
          <switch :checked="newMessageEnabled" @change="handleNewMessageChange" color="#3b82f6" />
        </view>

        <!-- 声音提醒 -->
        <view class="notification-item">
          <view class="item-left">
            <text class="fas fa-volume-high item-icon"></text>
            <view class="item-info">
              <text class="item-title">声音提醒</text>
              <text class="item-desc">收到消息时播放提示音</text>
            </view>
          </view>
          <switch :checked="soundEnabled" @change="handleSoundChange" color="#3b82f6" />
        </view>

        <!-- 振动提醒 -->
        <view class="notification-item">
          <view class="item-left">
            <text class="fas fa-mobile item-icon"></text>
            <view class="item-info">
              <text class="item-title">振动提醒</text>
              <text class="item-desc">收到消息时手机振动</text>
            </view>
          </view>
          <switch :checked="vibrationEnabled" @change="handleVibrationChange" color="#3b82f6" />
        </view>
      </view>

      <!-- 消息类型设置 -->
      <view class="notification-section">
        <view class="section-title">接收通知类型</view>
        
        <!-- 阅读提醒 -->
        <view class="notification-item">
          <view class="item-left">
            <text class="fas fa-book item-icon"></text>
            <view class="item-info">
              <text class="item-title">阅读提醒</text>
              <text class="item-desc">每日阅读目标提醒</text>
            </view>
          </view>
          <switch :checked="readingReminderEnabled" @change="handleReadingReminderChange" color="#3b82f6" />
        </view>

        <!-- 互动消息 -->
        <view class="notification-item">
          <view class="item-left">
            <text class="fas fa-comments item-icon"></text>
            <view class="item-info">
              <text class="item-title">互动消息</text>
              <text class="item-desc">收到评论和点赞提醒</text>
            </view>
          </view>
          <switch :checked="interactionEnabled" @change="handleInteractionChange" color="#3b82f6" />
        </view>

        <!-- 系统通知 -->
        <view class="notification-item">
          <view class="item-left">
            <text class="fas fa-gear item-icon"></text>
            <view class="item-info">
              <text class="item-title">系统通知</text>
              <text class="item-desc">系统更新和活动提醒</text>
            </view>
          </view>
          <switch :checked="systemNotificationEnabled" @change="handleSystemNotificationChange" color="#3b82f6" />
        </view>
      </view>

      <!-- 免打扰时间设置 -->
      <view class="notification-section">
        <view class="section-title">免打扰时间</view>
        
        <!-- 免打扰模式 -->
        <view class="notification-item">
          <view class="item-left">
            <text class="fas fa-moon item-icon"></text>
            <view class="item-info">
              <text class="item-title">免打扰模式</text>
              <text class="item-desc">开启后在设定时间段内不接收通知</text>
            </view>
          </view>
          <switch :checked="doNotDisturbEnabled" @change="handleDoNotDisturbChange" color="#3b82f6" />
        </view>

        <!-- 时间段设置 -->
        <view class="time-setting" v-if="doNotDisturbEnabled">
          <view class="time-item" @click="handleStartTimeClick">
            <text class="time-label">开始时间</text>
            <view class="time-value">
              <text>{{startTime}}</text>
              <text class="fas fa-chevron-right arrow-icon"></text>
            </view>
          </view>
          <view class="time-item" @click="handleEndTimeClick">
            <text class="time-label">结束时间</text>
            <view class="time-value">
              <text>{{endTime}}</text>
              <text class="fas fa-chevron-right arrow-icon"></text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 通知设置状态
const newMessageEnabled = ref(true)
const soundEnabled = ref(true)
const vibrationEnabled = ref(true)
const readingReminderEnabled = ref(true)
const interactionEnabled = ref(true)
const systemNotificationEnabled = ref(true)
const doNotDisturbEnabled = ref(false)
const startTime = ref('22:00')
const endTime = ref('07:00')

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 新消息通知开关
const handleNewMessageChange = (e) => {
  newMessageEnabled.value = e.detail.value
  uni.showToast({
    title: newMessageEnabled.value ? '已开启新消息通知' : '已关闭新消息通知',
    icon: 'none'
  })
}

// 声音提醒开关
const handleSoundChange = (e) => {
  soundEnabled.value = e.detail.value
  uni.showToast({
    title: soundEnabled.value ? '已开启声音提醒' : '已关闭声音提醒',
    icon: 'none'
  })
}

// 振动提醒开关
const handleVibrationChange = (e) => {
  vibrationEnabled.value = e.detail.value
  uni.showToast({
    title: vibrationEnabled.value ? '已开启振动提醒' : '已关闭振动提醒',
    icon: 'none'
  })
}

// 阅读提醒开关
const handleReadingReminderChange = (e) => {
  readingReminderEnabled.value = e.detail.value
  uni.showToast({
    title: readingReminderEnabled.value ? '已开启阅读提醒' : '已关闭阅读提醒',
    icon: 'none'
  })
}

// 互动消息开关
const handleInteractionChange = (e) => {
  interactionEnabled.value = e.detail.value
  uni.showToast({
    title: interactionEnabled.value ? '已开启互动消息' : '已关闭互动消息',
    icon: 'none'
  })
}

// 系统通知开关
const handleSystemNotificationChange = (e) => {
  systemNotificationEnabled.value = e.detail.value
  uni.showToast({
    title: systemNotificationEnabled.value ? '已开启系统通知' : '已关闭系统通知',
    icon: 'none'
  })
}

// 免打扰模式开关
const handleDoNotDisturbChange = (e) => {
  doNotDisturbEnabled.value = e.detail.value
  uni.showToast({
    title: doNotDisturbEnabled.value ? '已开启免打扰模式' : '已关闭免打扰模式',
    icon: 'none'
  })
}

// 设置开始时间
const handleStartTimeClick = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 设置结束时间
const handleEndTimeClick = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
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

/* 通知设置区块样式 */
.notification-section {
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

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.notification-item:last-child {
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

/* 时间设置样式 */
.time-setting {
  padding: 0 20rpx;
}

.time-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.time-item:last-child {
  border-bottom: none;
}

.time-label {
  font-size: 28rpx;
  color: #111827;
}

.time-value {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.arrow-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
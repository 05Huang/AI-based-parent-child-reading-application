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

      <!-- 重置设置按钮 -->
      <view class="reset-section">
        <view class="reset-btn" @click="resetToDefault">
          <text class="fas fa-refresh reset-icon"></text>
          <text class="reset-text">重置为默认设置</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

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

// 本地存储的key
const STORAGE_KEY = 'notification_settings'

// 默认设置
const defaultSettings = {
  newMessage: true,
  sound: true,
  vibration: true,
  readingReminder: true,
  interaction: true,
  systemNotification: true,
  doNotDisturb: false,
  startTime: '22:00',
  endTime: '07:00'
}

// 保存设置到本地存储
const saveSettings = () => {
  console.log('开始保存通知设置到本地存储')
  try {
    const settings = {
      newMessage: newMessageEnabled.value,
      sound: soundEnabled.value,
      vibration: vibrationEnabled.value,
      readingReminder: readingReminderEnabled.value,
      interaction: interactionEnabled.value,
      systemNotification: systemNotificationEnabled.value,
      doNotDisturb: doNotDisturbEnabled.value,
      startTime: startTime.value,
      endTime: endTime.value
    }
    
    uni.setStorageSync(STORAGE_KEY, settings)
    console.log('通知设置保存成功:', settings)
  } catch (error) {
    console.error('保存通知设置失败:', error)
    uni.showToast({
      title: '设置保存失败',
      icon: 'error'
    })
  }
}

// 从本地存储读取设置
const loadSettings = () => {
  console.log('开始从本地存储读取通知设置')
  try {
    const savedSettings = uni.getStorageSync(STORAGE_KEY)
    
    if (savedSettings) {
      console.log('读取到已保存的设置:', savedSettings)
      // 更新各个设置项
      newMessageEnabled.value = savedSettings.newMessage ?? defaultSettings.newMessage
      soundEnabled.value = savedSettings.sound ?? defaultSettings.sound
      vibrationEnabled.value = savedSettings.vibration ?? defaultSettings.vibration
      readingReminderEnabled.value = savedSettings.readingReminder ?? defaultSettings.readingReminder
      interactionEnabled.value = savedSettings.interaction ?? defaultSettings.interaction
      systemNotificationEnabled.value = savedSettings.systemNotification ?? defaultSettings.systemNotification
      doNotDisturbEnabled.value = savedSettings.doNotDisturb ?? defaultSettings.doNotDisturb
      startTime.value = savedSettings.startTime ?? defaultSettings.startTime
      endTime.value = savedSettings.endTime ?? defaultSettings.endTime
    } else {
      console.log('未找到已保存的设置，使用默认设置')
      // 首次使用，保存默认设置
      saveSettings()
    }
  } catch (error) {
    console.error('读取通知设置失败:', error)
    uni.showToast({
      title: '设置读取失败，使用默认设置',
      icon: 'none'
    })
  }
}

// 页面初始化时读取设置
onMounted(() => {
  console.log('通知设置页面初始化，开始读取本地设置')
  loadSettings()
})

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 新消息通知开关
const handleNewMessageChange = (e) => {
  console.log('新消息通知设置变更:', e.detail.value)
  newMessageEnabled.value = e.detail.value
  saveSettings() // 自动保存设置
  uni.showToast({
    title: newMessageEnabled.value ? '已开启新消息通知' : '已关闭新消息通知',
    icon: 'none'
  })
}

// 声音提醒开关
const handleSoundChange = (e) => {
  console.log('声音提醒设置变更:', e.detail.value)
  soundEnabled.value = e.detail.value
  saveSettings() // 自动保存设置
  uni.showToast({
    title: soundEnabled.value ? '已开启声音提醒' : '已关闭声音提醒',
    icon: 'none'
  })
}

// 振动提醒开关
const handleVibrationChange = (e) => {
  console.log('振动提醒设置变更:', e.detail.value)
  vibrationEnabled.value = e.detail.value
  saveSettings() // 自动保存设置
  uni.showToast({
    title: vibrationEnabled.value ? '已开启振动提醒' : '已关闭振动提醒',
    icon: 'none'
  })
}

// 阅读提醒开关
const handleReadingReminderChange = (e) => {
  console.log('阅读提醒设置变更:', e.detail.value)
  readingReminderEnabled.value = e.detail.value
  saveSettings() // 自动保存设置
  uni.showToast({
    title: readingReminderEnabled.value ? '已开启阅读提醒' : '已关闭阅读提醒',
    icon: 'none'
  })
}

// 互动消息开关
const handleInteractionChange = (e) => {
  console.log('互动消息设置变更:', e.detail.value)
  interactionEnabled.value = e.detail.value
  saveSettings() // 自动保存设置
  uni.showToast({
    title: interactionEnabled.value ? '已开启互动消息' : '已关闭互动消息',
    icon: 'none'
  })
}

// 系统通知开关
const handleSystemNotificationChange = (e) => {
  console.log('系统通知设置变更:', e.detail.value)
  systemNotificationEnabled.value = e.detail.value
  saveSettings() // 自动保存设置
  uni.showToast({
    title: systemNotificationEnabled.value ? '已开启系统通知' : '已关闭系统通知',
    icon: 'none'
  })
}

// 免打扰模式开关
const handleDoNotDisturbChange = (e) => {
  console.log('免打扰模式设置变更:', e.detail.value)
  doNotDisturbEnabled.value = e.detail.value
  saveSettings() // 自动保存设置
  uni.showToast({
    title: doNotDisturbEnabled.value ? '已开启免打扰模式' : '已关闭免打扰模式',
    icon: 'none'
  })
}

// 设置开始时间
const handleStartTimeClick = () => {
  console.log('点击设置免打扰开始时间')
  // TODO: 后续可以添加时间选择器
  uni.showToast({
    title: '时间选择功能开发中',
    icon: 'none'
  })
}

// 设置结束时间
const handleEndTimeClick = () => {
  console.log('点击设置免打扰结束时间')
  // TODO: 后续可以添加时间选择器
  uni.showToast({
    title: '时间选择功能开发中',
    icon: 'none'
  })
}

// 重置所有设置为默认值
const resetToDefault = () => {
  console.log('重置通知设置为默认值')
  
  // 显示确认对话框
  uni.showModal({
    title: '确认重置',
    content: '确定要将所有通知设置重置为默认值吗？',
    success: (res) => {
      if (res.confirm) {
        console.log('用户确认重置设置')
        newMessageEnabled.value = defaultSettings.newMessage
        soundEnabled.value = defaultSettings.sound
        vibrationEnabled.value = defaultSettings.vibration
        readingReminderEnabled.value = defaultSettings.readingReminder
        interactionEnabled.value = defaultSettings.interaction
        systemNotificationEnabled.value = defaultSettings.systemNotification
        doNotDisturbEnabled.value = defaultSettings.doNotDisturb
        startTime.value = defaultSettings.startTime
        endTime.value = defaultSettings.endTime
        
        saveSettings()
        uni.showToast({
          title: '已重置为默认设置',
          icon: 'success'
        })
      } else {
        console.log('用户取消重置设置')
      }
    }
  })
}

// 获取当前所有设置（用于调试）
const getCurrentSettings = () => {
  const currentSettings = {
    newMessage: newMessageEnabled.value,
    sound: soundEnabled.value,
    vibration: vibrationEnabled.value,
    readingReminder: readingReminderEnabled.value,
    interaction: interactionEnabled.value,
    systemNotification: systemNotificationEnabled.value,
    doNotDisturb: doNotDisturbEnabled.value,
    startTime: startTime.value,
    endTime: endTime.value
  }
  console.log('当前通知设置:', currentSettings)
  return currentSettings
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

/* 重置设置区域样式 */
.reset-section {
  margin-top: 30rpx;
  padding: 0 30rpx;
}

.reset-btn {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15rpx;
  border: 2rpx solid #e5e7eb;
  transition: all 0.3s ease;
}

.reset-btn:active {
  background-color: #f9fafb;
  transform: scale(0.98);
}

.reset-icon {
  font-size: 32rpx;
  color: #6b7280;
}

.reset-text {
  font-size: 28rpx;
  color: #6b7280;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
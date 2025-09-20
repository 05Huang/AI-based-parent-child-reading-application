# 创建字体设置页面的基本模板
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">字体设置</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 字体大小设置 -->
      <view class="settings-section">
        <text class="section-title">字体大小</text>
        <view class="settings-list">
          <view class="font-size-setting">
            <view class="size-preview">
              <text class="preview-text small">小</text>
              <text class="preview-text medium">中</text>
              <text class="preview-text large">大</text>
            </view>
            <slider 
              :value="fontSize" 
              :min="12" 
              :max="24" 
              :step="2"
              @change="handleFontSizeChange"
              show-value
              class="size-slider"
            />
          </view>
          <view class="preview-content">
            <text class="preview-title">预览效果</text>
            <text class="preview-paragraph" :style="{ fontSize: fontSize + 'px' }">
              在清晨的阳光下，小溪流淌着欢快的音符，树叶随风轻轻摇曳，空气中弥漫着泥土和青草的芬芳。这是一个适合阅读的美好时光，让我们一起徜徉在书海中，感受文字的魅力。
            </text>
          </view>
        </view>
      </view>

      <!-- 字体样式设置 -->
      <view class="settings-section">
        <text class="section-title">字体样式</text>
        <view class="settings-list">
          <view class="font-style-item" :class="{ active: currentFont === 'system' }" @click="selectFont('system')">
            <view class="style-left">
              <text class="fas fa-font item-icon"></text>
              <text class="style-name">系统默认</text>
            </view>
            <text class="fas fa-check check-icon" v-if="currentFont === 'system'"></text>
          </view>
          <view class="font-style-item" :class="{ active: currentFont === 'serif' }" @click="selectFont('serif')">
            <view class="style-left">
              <text class="fas fa-pen-fancy item-icon"></text>
              <text class="style-name">宋体</text>
            </view>
            <text class="fas fa-check check-icon" v-if="currentFont === 'serif'"></text>
          </view>
          <view class="font-style-item" :class="{ active: currentFont === 'sans' }" @click="selectFont('sans')">
            <view class="style-left">
              <text class="fas fa-pen item-icon"></text>
              <text class="style-name">黑体</text>
            </view>
            <text class="fas fa-check check-icon" v-if="currentFont === 'sans'"></text>
          </view>
        </view>
      </view>

      <!-- 其他阅读设置 -->
      <view class="settings-section">
        <text class="section-title">其他设置</text>
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-moon item-icon"></text>
              <text class="item-text">深色模式</text>
            </view>
            <switch :checked="isDarkMode" @change="handleDarkModeChange" color="#3b82f6" />
          </view>
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-paragraph item-icon"></text>
              <text class="item-text">段落间距</text>
            </view>
            <view class="spacing-control">
              <text class="spacing-btn" @click="decreaseSpacing">-</text>
              <text class="spacing-value">{{ lineSpacing }}</text>
              <text class="spacing-btn" @click="increaseSpacing">+</text>
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

// 字体大小设置
const fontSize = ref(16)
const handleFontSizeChange = (e) => {
  fontSize.value = e.detail.value
}

// 字体样式设置
const currentFont = ref('system')
const selectFont = (font) => {
  currentFont.value = font
}

// 深色模式设置
const isDarkMode = ref(false)
const handleDarkModeChange = (e) => {
  isDarkMode.value = e.detail.value
}

// 行间距设置
const lineSpacing = ref(1.5)
const decreaseSpacing = () => {
  if (lineSpacing.value > 1) {
    lineSpacing.value = Number((lineSpacing.value - 0.1).toFixed(1))
  }
}
const increaseSpacing = () => {
  if (lineSpacing.value < 2) {
    lineSpacing.value = Number((lineSpacing.value + 0.1).toFixed(1))
  }
}

// 保存设置
const handleSave = () => {
  // 保存设置到本地存储
  uni.setStorageSync('fontSettings', {
    fontSize: fontSize.value,
    fontFamily: currentFont.value,
    isDarkMode: isDarkMode.value,
    lineSpacing: lineSpacing.value
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
  padding: 20rpx;
}

/* 字体大小设置样式 */
.font-size-setting {
  padding: 20rpx 0;
}

.size-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.preview-text {
  color: #4b5563;
}

.preview-text.small {
  font-size: 24rpx;
}

.preview-text.medium {
  font-size: 32rpx;
}

.preview-text.large {
  font-size: 40rpx;
}

.size-slider {
  margin: 0;
  padding: 0;
}

.preview-content {
  margin-top: 30rpx;
  padding: 20rpx;
  background-color: #f9fafb;
  border-radius: 12rpx;
}

.preview-title {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
  display: block;
}

.preview-paragraph {
  color: #1f2937;
  line-height: 1.6;
}

/* 字体样式设置 */
.font-style-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.font-style-item:last-child {
  border-bottom: none;
}

.style-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.item-icon {
  color: #3b82f6;
  font-size: 36rpx;
}

.style-name {
  font-size: 30rpx;
  color: #111827;
}

.check-icon {
  color: #3b82f6;
  font-size: 24rpx;
}

/* 其他设置项样式 */
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

.item-text {
  font-size: 30rpx;
  color: #111827;
}

.spacing-control {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.spacing-btn {
  width: 48rpx;
  height: 48rpx;
  background-color: #f3f4f6;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #4b5563;
}

.spacing-value {
  min-width: 60rpx;
  text-align: center;
  font-size: 30rpx;
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
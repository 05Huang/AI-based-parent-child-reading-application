<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-content">
        <view class="back-button" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">阅读模式</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 主题模式 -->
      <view class="section">
        <view class="section-title">主题模式</view>
        <view class="theme-grid">
          <view 
            v-for="theme in themes" 
            :key="theme.id"
            class="theme-item"
            :class="{ active: currentTheme === theme.id }"
            @click="handleThemeChange(theme.id)"
          >
            <view class="theme-preview" :style="{ backgroundColor: theme.backgroundColor, color: theme.textColor }">
              <text class="theme-preview-text">Aa</text>
            </view>
            <text class="theme-name">{{ theme.name }}</text>
          </view>
        </view>
      </view>

      <!-- 字体大小 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">字体大小</text>
          <text class="font-size-value">{{ fontSize }}px</text>
        </view>
        <view class="font-size-control">
          <text class="fas fa-minus control-btn" @click="decreaseFontSize"></text>
              <slider 
            :value="fontSize" 
            :min="12" 
            :max="24" 
            :step="1"
            @change="handleFontSizeChange"
            class="font-size-slider"
            activeColor="#6366f1"
                backgroundColor="#e5e7eb"
                block-size="24"
              />
          <text class="fas fa-plus control-btn" @click="increaseFontSize"></text>
        </view>
        <view class="preview-text" :style="{ fontSize: fontSize + 'px' }">
          这是字体大小预览效果。通过调整滑块可以改变字体大小，让阅读更加舒适。选择适合自己的字体大小，享受更好的阅读体验。
            </view>
          </view>

      <!-- 行间距 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">行间距</text>
          <text class="line-height-value">{{ lineHeight }}倍</text>
        </view>
        <slider 
          :value="lineHeight * 10" 
          :min="10" 
          :max="20" 
          :step="1"
          @change="handleLineHeightChange"
          class="line-height-slider"
          activeColor="#8b5cf6"
          backgroundColor="#e5e7eb"
          block-size="24"
        />
        <view class="preview-text" :style="{ lineHeight: lineHeight }">
          这是预览文本效果<br>
          可以查看行间距的显示效果<br>
          通过调整滑块来改变行间距大小
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="section">
        <view class="section-title">其他设置</view>
        
        <!-- 横屏锁定 -->
        <view class="setting-item">
          <view class="setting-left">
            <text class="fas fa-mobile-screen-button setting-icon"></text>
            <view class="setting-info">
              <text class="setting-name">横屏锁定</text>
              <text class="setting-desc">
                {{ isScreenOrientationSupported ? '阅读时保持横屏显示' : '阅读时保持横屏显示(当前平台不支持)' }}
              </text>
            </view>
          </view>
          <switch 
            :checked="landscapeLocked" 
            @change="handleLandscapeLockChange" 
            :disabled="!isScreenOrientationSupported"
            color="#6366f1"
          />
        </view>

        <!-- 屏幕常亮 -->
        <view class="setting-item">
          <view class="setting-left">
            <text class="fas fa-sun setting-icon"></text>
            <view class="setting-info">
              <text class="setting-name">屏幕常亮</text>
              <text class="setting-desc">
                {{ isKeepScreenOnSupported ? '阅读时保持屏幕不息屏' : '阅读时保持屏幕不息屏(当前平台不支持)' }}
              </text>
            </view>
          </view>
          <switch 
            :checked="keepScreenOn" 
            @change="handleKeepScreenOnChange" 
            :disabled="!isKeepScreenOnSupported"
            color="#6366f1"
          />
        </view>

        <!-- 翻页动画 -->
        <view class="setting-item">
          <view class="setting-left">
            <text class="fas fa-book-open setting-icon"></text>
            <view class="setting-info">
              <text class="setting-name">翻页动画</text>
              <text class="setting-desc">开启翻页过渡动画</text>
            </view>
          </view>
          <switch 
            :checked="pageAnimation" 
            @change="handlePageAnimationChange" 
            color="#6366f1"
          />
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
import readingModeManager from '@/utils/readingModeManager.js'

// 从管理器导入主题配置
const themes = readingModeManager.themes

// 状态管理
const currentTheme = ref('default')
const fontSize = ref(16)
const lineHeight = ref(1.8)
const landscapeLocked = ref(false)
const keepScreenOn = ref(true)
const pageAnimation = ref(true)

// 平台支持状态
const isKeepScreenOnSupported = ref(true)
const isScreenOrientationSupported = ref(true)

// 页面加载时读取设置
onMounted(() => {
  console.log('[儿童端] 阅读模式设置页面初始化')
  
  // 检测平台支持情况
  isKeepScreenOnSupported.value = readingModeManager.isKeepScreenOnSupported()
  isScreenOrientationSupported.value = readingModeManager.isScreenOrientationSupported()
  
  console.log('[儿童端] 平台支持检测 - 屏幕常亮:', isKeepScreenOnSupported.value, '横屏锁定:', isScreenOrientationSupported.value)
  
  loadSettings()
})

// 读取设置
const loadSettings = () => {
  console.log('[儿童端] 开始读取阅读模式设置')
  const settings = readingModeManager.loadSettings()
  
  // 更新页面状态
  currentTheme.value = settings.theme
  fontSize.value = settings.fontSize
  lineHeight.value = settings.lineHeight
  landscapeLocked.value = settings.landscapeLocked
  keepScreenOn.value = settings.keepScreenOn
  pageAnimation.value = settings.pageAnimation
  
  console.log('[儿童端] 阅读模式设置加载完成:', settings)
}

// 保存设置
const saveSettings = () => {
  const settings = {
    theme: currentTheme.value,
    fontSize: fontSize.value,
    lineHeight: lineHeight.value,
    landscapeLocked: landscapeLocked.value,
    keepScreenOn: keepScreenOn.value,
    pageAnimation: pageAnimation.value
  }
  
  console.log('[儿童端] 保存阅读模式设置:', settings)
  readingModeManager.saveSettings(settings)
  
  // 立即应用设置
  readingModeManager.applySettings(settings)
}

// 返回上一页
const goBack = () => {
      uni.navigateBack()
}

// 切换主题
const handleThemeChange = (themeId) => {
  console.log('[儿童端] 切换主题:', themeId)
  currentTheme.value = themeId
  saveSettings()
  
  uni.showToast({
    title: '主题已切换',
    icon: 'success',
    duration: 1000
  })
}

// 字体大小调整
const decreaseFontSize = () => {
  if (fontSize.value > 12) {
    fontSize.value--
    saveSettings()
  }
}

const increaseFontSize = () => {
  if (fontSize.value < 24) {
    fontSize.value++
    saveSettings()
  }
}

const handleFontSizeChange = (e) => {
  console.log('[儿童端] 字体大小调整:', e.detail.value)
  fontSize.value = e.detail.value
  saveSettings()
}

// 行间距调整
const handleLineHeightChange = (e) => {
  const value = e.detail.value / 10
  console.log('[儿童端] 行间距调整:', value)
  lineHeight.value = value
  saveSettings()
}

// 横屏锁定切换
const handleLandscapeLockChange = (e) => {
  console.log('[儿童端] 横屏锁定切换:', e.detail.value)
  landscapeLocked.value = e.detail.value
  saveSettings()
}

// 屏幕常亮切换
const handleKeepScreenOnChange = (e) => {
  console.log('[儿童端] 屏幕常亮切换:', e.detail.value)
  keepScreenOn.value = e.detail.value
  saveSettings()
}

// 翻页动画切换
const handlePageAnimationChange = (e) => {
  console.log('[儿童端] 翻页动画切换:', e.detail.value)
  pageAnimation.value = e.detail.value
  saveSettings()
  
  uni.showToast({
    title: pageAnimation.value ? '已开启翻页动画' : '已关闭翻页动画',
    icon: 'success',
    duration: 1000
  })
}

// 重置为默认设置
const resetToDefault = () => {
  uni.showModal({
    title: '重置确认',
    content: '确定要重置为默认设置吗？',
    confirmText: '确定',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        console.log('[儿童端] 重置为默认设置')
        const defaultSettings = readingModeManager.resetToDefault()
        
        // 更新页面状态
        currentTheme.value = defaultSettings.theme
        fontSize.value = defaultSettings.fontSize
        lineHeight.value = defaultSettings.lineHeight
        landscapeLocked.value = defaultSettings.landscapeLocked
        keepScreenOn.value = defaultSettings.keepScreenOn
        pageAnimation.value = defaultSettings.pageAnimation
        
        // 应用设置
        readingModeManager.applySettings(defaultSettings)
        
        uni.showToast({
          title: '已重置为默认设置',
          icon: 'success'
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
}

/* 顶部导航 */
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
  align-items: center;
  justify-content: center;
  color: #ffffff;
  position: relative;
}

.back-button {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.2);
  position: absolute;
  left: 0;
  transition: background-color 0.2s ease;
}

.back-button:active {
  background-color: rgba(255, 255, 255, 0.3);
}

.back-button .fas {
  font-size: 32rpx;
}

.page-title {
  font-size: 36rpx;
  font-weight: 600;
  text-align: center;
}

/* 主要内容区域 */
.main-content {
  margin-top: 120rpx;
  padding: 32rpx;
  height: calc(100vh - 120rpx);
  box-sizing: border-box;
}

/* 节区样式 */
.section {
  margin-bottom: 48rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.font-size-value,
.line-height-value {
  font-size: 28rpx;
  color: #6366f1;
  font-weight: 600;
}

/* 主题网格 */
.theme-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.theme-item {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  border: 3rpx solid transparent;
  transition: all 0.3s ease;
}

.theme-item.active {
  border-color: #6366f1;
  box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.2);
}

.theme-preview {
  height: 100rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.06);
}

.theme-preview-text {
  font-size: 40rpx;
  font-weight: 600;
}

.theme-name {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 500;
}

/* 字体大小控制 */
.font-size-control {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 24rpx;
}

.control-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border-radius: 50%;
  color: #6366f1;
  font-size: 24rpx;
  transition: all 0.2s ease;
}

.control-btn:active {
  background: #e9d5ff;
  transform: scale(0.95);
}

.font-size-slider,
.line-height-slider {
  flex: 1;
}

/* 预览文本 */
.preview-text {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  color: #374151;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

/* 设置项 */
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.setting-left {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  flex: 1;
}

.setting-icon {
  font-size: 40rpx;
  color: #6366f1;
  width: 40rpx;
  text-align: center;
}

.setting-info {
  flex: 1;
}

.setting-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 8rpx;
  display: block;
}

.setting-desc {
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.5;
}

/* 重置设置按钮 */
.reset-section {
  margin-top: 64rpx;
  margin-bottom: 48rpx;
}

.reset-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  background: #ffffff;
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  padding: 32rpx;
  color: #6b7280;
  transition: all 0.3s ease;
}

.reset-btn:active {
  background: #f9fafb;
  border-color: #6366f1;
  color: #6366f1;
}

.reset-icon {
  font-size: 32rpx;
}

.reset-text {
  font-size: 28rpx;
  font-weight: 500;
}
</style> 
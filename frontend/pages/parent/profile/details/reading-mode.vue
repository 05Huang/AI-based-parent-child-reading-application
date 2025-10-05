<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">阅读模式</text>
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
            activeColor="#3b82f6"
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
          activeColor="#3b82f6"
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
            color="#3b82f6"
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
            color="#3b82f6"
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
            color="#3b82f6"
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
  console.log('阅读模式设置页面初始化')
  
  // 检测平台支持情况
  isKeepScreenOnSupported.value = readingModeManager.isKeepScreenOnSupported()
  isScreenOrientationSupported.value = readingModeManager.isScreenOrientationSupported()
  
  console.log('平台支持检测 - 屏幕常亮:', isKeepScreenOnSupported.value, '横屏锁定:', isScreenOrientationSupported.value)
  
  loadSettings()
})

// 读取设置
const loadSettings = () => {
  console.log('开始读取阅读模式设置')
  const settings = readingModeManager.loadSettings()
  
  // 更新页面状态
  currentTheme.value = settings.theme
  fontSize.value = settings.fontSize
  lineHeight.value = settings.lineHeight
  landscapeLocked.value = settings.landscapeLocked
  keepScreenOn.value = settings.keepScreenOn
  pageAnimation.value = settings.pageAnimation
  
  console.log('阅读模式设置加载完成:', settings)
}

// 保存设置
const saveSettings = () => {
  console.log('开始保存阅读模式设置')
  const settings = {
    theme: currentTheme.value,
    fontSize: fontSize.value,
    lineHeight: lineHeight.value,
    landscapeLocked: landscapeLocked.value,
    keepScreenOn: keepScreenOn.value,
    pageAnimation: pageAnimation.value
  }
  
  const success = readingModeManager.saveSettings(settings)
  if (success) {
    console.log('阅读模式设置保存成功')
  } else {
    console.error('阅读模式设置保存失败')
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 主题切换
const handleThemeChange = (themeId) => {
  console.log('切换主题:', themeId)
  currentTheme.value = themeId
  saveSettings() // 自动保存设置
  uni.showToast({
    title: '主题已更改',
    icon: 'success'
  })
}

// 字体大小调整
const handleFontSizeChange = (e) => {
  console.log('字体大小变更:', e.detail.value)
  fontSize.value = e.detail.value
  saveSettings() // 自动保存设置
}

const decreaseFontSize = () => {
  if (fontSize.value > 12) {
    fontSize.value--
    console.log('减小字体:', fontSize.value)
    saveSettings() // 自动保存设置
  }
}

const increaseFontSize = () => {
  if (fontSize.value < 24) {
    fontSize.value++
    console.log('增大字体:', fontSize.value)
    saveSettings() // 自动保存设置
  }
}

// 行间距调整
const handleLineHeightChange = (e) => {
  const newLineHeight = e.detail.value / 10
  console.log('行间距变更:', newLineHeight)
  lineHeight.value = newLineHeight
  saveSettings() // 自动保存设置
}

// 横屏锁定设置
const handleLandscapeLockChange = (e) => {
  console.log('横屏锁定设置变更:', e.detail.value)
  landscapeLocked.value = e.detail.value
  saveSettings() // 自动保存设置
  
  // 检查平台是否支持
  if (!readingModeManager.isScreenOrientationSupported()) {
    console.warn('当前平台不支持屏幕方向设置')
    uni.showToast({
      title: '当前平台不支持此功能',
      icon: 'none',
      duration: 2000
    })
    return
  }
  
  // 应用横屏锁定设置
  const success = readingModeManager.applyScreenOrientation(landscapeLocked.value)
  
  if (success) {
    uni.showToast({
      title: landscapeLocked.value ? '横屏锁定已开启' : '横屏锁定已关闭',
      icon: 'none'
    })
  } else {
    uni.showToast({
      title: '当前平台不支持此功能',
      icon: 'none',
      duration: 2000
    })
  }
}

// 屏幕常亮设置（修复bug：先更新值再调用API，添加平台检测）
const handleKeepScreenOnChange = (e) => {
  console.log('屏幕常亮设置变更:', e.detail.value)
  keepScreenOn.value = e.detail.value // 修复：先更新值
  saveSettings() // 自动保存设置
  
  // 检查平台是否支持
  if (!readingModeManager.isKeepScreenOnSupported()) {
    console.warn('当前平台不支持屏幕常亮功能')
    uni.showToast({
      title: '当前平台不支持此功能',
      icon: 'none',
      duration: 2000
    })
    return
  }
  
  // 应用屏幕常亮设置
  const success = readingModeManager.applyKeepScreenOn(keepScreenOn.value)
  
  if (success) {
    uni.showToast({
      title: keepScreenOn.value ? '屏幕常亮已开启' : '屏幕常亮已关闭',
      icon: 'none'
    })
  } else {
    uni.showToast({
      title: '当前平台不支持此功能',
      icon: 'none',
      duration: 2000
    })
  }
}

// 翻页动画设置
const handlePageAnimationChange = (e) => {
  console.log('翻页动画设置变更:', e.detail.value)
  pageAnimation.value = e.detail.value
  saveSettings() // 自动保存设置
  uni.showToast({
    title: pageAnimation.value ? '翻页动画已开启' : '翻页动画已关闭',
    icon: 'none'
  })
}

// 重置为默认设置
const resetToDefault = () => {
  console.log('重置阅读模式为默认设置')
  
  uni.showModal({
    title: '确认重置',
    content: '确定要将阅读模式重置为默认设置吗？',
    success: (res) => {
      if (res.confirm) {
        console.log('用户确认重置')
        const defaultSettings = readingModeManager.resetToDefault()
        
        // 更新页面状态
        currentTheme.value = defaultSettings.theme
        fontSize.value = defaultSettings.fontSize
        lineHeight.value = defaultSettings.lineHeight
        landscapeLocked.value = defaultSettings.landscapeLocked
        keepScreenOn.value = defaultSettings.keepScreenOn
        pageAnimation.value = defaultSettings.pageAnimation
        
        uni.showToast({
          title: '已重置为默认设置',
          icon: 'success'
        })
      } else {
        console.log('用户取消重置')
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

/* 设置区块样式 */
.section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #111827;
  margin-bottom: 20rpx;
}

/* 主题网格样式 */
.theme-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.theme-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.theme-preview {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #e5e7eb;
}

.theme-item.active .theme-preview {
  border: 2rpx solid #3b82f6;
}

.theme-preview-text {
  font-size: 36rpx;
  font-weight: bold;
}

.theme-name {
  font-size: 24rpx;
  color: #6b7280;
}

/* 字体大小控制样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.font-size-value, .line-height-value {
  font-size: 28rpx;
  color: #6b7280;
}

.font-size-control {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.control-btn {
  width: 60rpx;
  height: 60rpx;
  background-color: #f3f4f6;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.font-size-slider, .line-height-slider {
  flex: 1;
}

.preview-text {
  margin-top: 20rpx;
  padding: 20rpx;
  background-color: #f9fafb;
  border-radius: 12rpx;
  color: #111827;
}

/* 设置项样式 */
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f3f4f6;
}

.setting-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.setting-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.setting-icon {
  font-size: 40rpx;
  color: #3b82f6;
  width: 40rpx;
  text-align: center;
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.setting-name {
  font-size: 30rpx;
  color: #111827;
}

.setting-desc {
  font-size: 24rpx;
  color: #6b7280;
}

/* 禁用状态样式 */
.setting-item switch[disabled] {
  opacity: 0.5;
}

.setting-item:has(switch[disabled]) .setting-desc {
  color: #9ca3af;
}

.setting-item:has(switch[disabled]) .setting-icon {
  color: #9ca3af !important;
}

/* 重置设置区域样式 */
.reset-section {
  margin-top: 30rpx;
  padding-bottom: 30rpx;
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
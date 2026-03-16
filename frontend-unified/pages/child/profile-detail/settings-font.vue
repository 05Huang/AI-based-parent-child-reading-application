<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @tap="goBack" hover-class="back-btn-hover">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">字体设置</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 字体样式设置 -->
      <view class="settings-section">
        <text class="section-title">选择字体</text>
        <view class="settings-list">
          <view 
            v-for="font in fontList" 
            :key="font.id"
            class="font-style-item" 
            :class="{ active: currentFont === font.id }" 
            @tap="selectFont(font.id)"
          >
            <view class="style-left">
              <text class="fas fa-font item-icon"></text>
              <view class="font-info">
                <text class="style-name">{{ font.name }}</text>
                <text class="style-desc">{{ font.desc }}</text>
              </view>
          </view>
            <text class="fas fa-check check-icon" v-if="currentFont === font.id"></text>
          </view>
        </view>
      </view>

      <!-- 预览效果 -->
      <view class="settings-section">
        <text class="section-title">预览效果</text>
        <view class="preview-content" :style="{ fontFamily: getFontFamily(currentFont) }">
          <text class="preview-paragraph">
            在清晨的阳光下，小溪流淌着欢快的音符，树叶随风轻轻摇曳，空气中弥漫着泥土和青草的芬芳。这是一个适合阅读的美好时光，让我们一起徜徉在书海中，感受文字的魅力。
          </text>
          <text class="preview-english">
            The quick brown fox jumps over the lazy dog. Reading is a journey of discovery and wonder.
          </text>
        </view>
      </view>

      <!-- 提示信息 -->
      <view class="tip-section">
        <view class="tip-box">
          <text class="fas fa-lightbulb tip-icon"></text>
          <text class="tip-text">字体大小、主题、行间距等设置请前往"阅读模式"页面</text>
        </view>
      </view>

      <!-- 保存按钮 -->
      <view class="save-section">
        <button class="save-btn" @tap="handleSave">保存设置</button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 返回上一页
const goBack = () => {
  console.log('[儿童端字体设置] 点击返回按钮')
  
  // 获取页面栈
  const pages = getCurrentPages()
  console.log('[儿童端字体设置] 当前页面栈长度:', pages.length)
  
  if (pages.length > 1) {
    // 有上一页，执行返回
    console.log('[儿童端字体设置] 执行返回操作')
    uni.navigateBack({
      delta: 1
    })
  } else {
    // 没有上一页，跳转到设置页面
    console.log('[儿童端字体设置] 页面栈为空，跳转到设置页面')
    uni.redirectTo({
      url: '/pages/child/profile-detail/settings'
    })
  }
}

// 主流字体列表
const fontList = [
  {
    id: 'system',
    name: '系统默认',
    desc: '跟随系统字体设置',
    family: 'system-ui, -apple-system, sans-serif'
  },
  {
    id: 'songti',
    name: '宋体',
    desc: '经典衬线字体，适合长文阅读',
    family: 'SimSun, STSong, serif'
  },
  {
    id: 'heiti',
    name: '黑体',
    desc: '现代无衬线字体，清晰易读',
    family: 'SimHei, STHeiti, sans-serif'
  },
  {
    id: 'kaiti',
    name: '楷体',
    desc: '手写风格，优雅舒适',
    family: 'KaiTi, STKaiti, cursive'
  },
  {
    id: 'fangsong',
    name: '仿宋',
    desc: '书法风格，古朴典雅',
    family: 'FangSong, STFangsong, serif'
  },
  {
    id: 'yahei',
    name: '微软雅黑',
    desc: '圆润舒适，适合屏幕阅读',
    family: '"Microsoft YaHei", sans-serif'
  },
  {
    id: 'pingfang',
    name: '苹方',
    desc: '现代设计，简洁优雅',
    family: 'PingFang SC, sans-serif'
  },
  {
    id: 'noto',
    name: 'Noto Sans',
    desc: '谷歌字体，支持多语言',
    family: '"Noto Sans SC", sans-serif'
  },
  {
    id: 'source',
    name: '思源黑体',
    desc: '开源字体，专业设计',
    family: '"Source Han Sans SC", sans-serif'
  }
]

// 当前选中的字体
const currentFont = ref('system')

// 获取字体family
const getFontFamily = (fontId) => {
  const font = fontList.find(f => f.id === fontId)
  return font ? font.family : fontList[0].family
}

// 选择字体
const selectFont = (fontId) => {
  console.log('[儿童端字体设置] 选择字体:', fontId)
  currentFont.value = fontId
  
  const font = fontList.find(f => f.id === fontId)
  uni.showToast({
    title: `已切换至${font.name}`,
    icon: 'success'
  })
}

// 页面加载时读取设置
onMounted(() => {
  console.log('[儿童端字体设置] 页面初始化')
  loadFontSettings()
})

// 读取字体设置
const loadFontSettings = () => {
  console.log('[儿童端字体设置] 开始读取字体设置')
  
  try {
    const savedFont = uni.getStorageSync('fontFamily')
    if (savedFont) {
      currentFont.value = savedFont
      console.log('[儿童端字体设置] 读取到字体设置:', savedFont)
    }
  } catch (error) {
    console.error('[儿童端字体设置] 读取字体设置失败:', error)
  }
}

// 保存设置
const handleSave = () => {
  console.log('[儿童端字体设置] 保存字体设置 - 字体:', currentFont.value)
  
  try {
    // 保存字体设置
    uni.setStorageSync('fontFamily', currentFont.value)
    
    // 同时保存字体family到全局，供阅读页面使用
    const fontFamily = getFontFamily(currentFont.value)
    uni.setStorageSync('fontFamilyCSS', fontFamily)
    
    console.log('[儿童端字体设置] 字体设置保存成功 - ID:', currentFont.value, 'Family:', fontFamily)
        
        uni.showToast({
          title: '设置已保存',
      icon: 'success',
      duration: 2000
    })
  } catch (error) {
    console.error('[儿童端字体设置] 保存字体设置失败:', error)
        uni.showToast({
      title: '保存失败',
      icon: 'error'
        })
  }
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

/* 顶部导航样式 - 紫色主题 */
.nav-header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 40rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
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
  justify-content: center;
  width: 100%;
  box-sizing: border-box;
  position: relative;
}

.back-btn {
  padding: 16rpx;
  position: absolute;
  left: 0;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 60rpx;
  min-height: 60rpx;
  z-index: 10;
  pointer-events: auto;
}

.back-btn text {
  font-size: 32rpx;
  pointer-events: none;
}

.back-btn:active,
.back-btn-hover {
  opacity: 0.7;
  transform: scale(0.95);
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
  text-align: center;
}

/* 主要内容区域 */
.main-content {
  margin-top: 120rpx;
  padding: 32rpx;
  height: calc(100vh - 120rpx);
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
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

/* 预览内容样式 */
.preview-content {
  background-color: #ffffff;
  padding: 30rpx;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.preview-paragraph {
  color: #1f2937;
  line-height: 1.8;
  font-size: 30rpx;
}

.preview-english {
  color: #6b7280;
  line-height: 1.6;
  font-size: 26rpx;
  margin-top: 10rpx;
}

/* 字体样式设置 - 紫色主题 */
.font-style-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 24rpx;
  border-bottom: 1rpx solid #f3f4f6;
  transition: background-color 0.2s ease;
}

.font-style-item:active {
  background-color: #f9fafb;
}

.font-style-item.active {
  background-color: #f5f3ff;
}

.font-style-item:last-child {
  border-bottom: none;
}

.style-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
}

.item-icon {
  color: #6366f1;
  font-size: 36rpx;
  width: 40rpx;
  text-align: center;
  flex-shrink: 0;
}

.font-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  flex: 1;
}

.style-name {
  font-size: 30rpx;
  color: #111827;
  font-weight: 500;
}

.style-desc {
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.4;
}

.check-icon {
  color: #6366f1;
  font-size: 32rpx;
  flex-shrink: 0;
}

/* 提示信息样式 - 紫色主题 */
.tip-section {
  margin-bottom: 30rpx;
  padding: 0 20rpx;
}

.tip-box {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border-radius: 16rpx;
  border-left: 4rpx solid #6366f1;
}

.tip-icon {
  font-size: 32rpx;
  color: #6366f1;
  flex-shrink: 0;
}

.tip-text {
  font-size: 26rpx;
  color: #5b21b6;
  line-height: 1.6;
  flex: 1;
}

/* 保存按钮样式 - 紫色主题 */
.save-section {
  margin-top: 60rpx;
  padding: 0 20rpx;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 44rpx;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.2);
  transition: all 0.3s ease;
}

.save-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.3);
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
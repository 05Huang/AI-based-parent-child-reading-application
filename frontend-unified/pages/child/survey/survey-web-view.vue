<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="back-btn" @tap="goBack">
        <view class="back-icon"></view>
      </view>
      <text class="title">{{ surveyTitle || '问卷调查' }}</text>
      <!-- 返回按钮（顶部右侧） -->
      <view v-if="showReturnButton" class="header-return-btn" @tap="returnToList">
        <view class="check-icon"></view>
      </view>
      <view v-else class="spacer"></view>
    </view>

    <!-- 问卷内容 -->
    <view class="web-view-wrapper">
      <web-view 
        :src="surveyUrl" 
        class="survey-content"
        @message="handleWebViewMessage"
      ></web-view>
    </view>
    
    <!-- 快速返回按钮（始终显示在右下方） -->
    <view class="quick-return-btn" @tap="returnToList">
      <view class="arrow-left-icon"></view>
      <text class="btn-text">返回</text>
    </view>
    
    <!-- 浮动返回按钮（在底部） -->
    <view v-if="showReturnButton" class="return-button-container">
      <view class="return-button" @tap="returnToList">
        <view class="return-icon"></view>
        <text class="return-text">返回问卷列表</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request.js'

const surveyUrl = ref('')
const surveyTitle = ref('')
const showReturnButton = ref(false)
const currentSurveyId = ref(null)
const currentUserId = ref(null)
const isOnCompletionPage = ref(false) // 是否在完成页面（抽奖页面）
const hasAutoMarked = ref(false) // 是否已经自动标记完成
let checkInterval = null // 检测定时器

onMounted(() => {
  // 方式1：尝试从 uni.$route 获取（H5）
  let surveyId = undefined
  let title = undefined
  
  // H5环境
  if (uni.$route) {
    surveyId = uni.$route.query?.surveyId
    title = uni.$route.query?.title
    console.log('📄 H5环境 - 路由参数：surveyId=', surveyId, 'title=', title)
  }
  
  // 小程序环境 - 使用 getCurrentPages
  if (!surveyId) {
    try {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      if (currentPage && currentPage.options) {
        surveyId = currentPage.options.surveyId
        title = currentPage.options.title
        console.log('📄 小程序环境 - 页面参数：surveyId=', surveyId, 'title=', title)
      }
    } catch (error) {
      console.error('❌ 获取页面参数失败：', error)
    }
  }
  
  console.log('📄 加载问卷页面，问卷ID：', surveyId, '标题：', title)
  
  if (surveyId) {
    currentSurveyId.value = surveyId
    generateSurveyLink(surveyId, title)
  } else {
    uni.showToast({
      title: '问卷ID丢失',
      icon: 'none'
    })
  }
  
  if (title) {
    surveyTitle.value = decodeURIComponent(title)
  } else {
    surveyTitle.value = '问卷调查'
  }
  
  // 启动问卷完成检测
  startSurveyCompletionDetection()
})

const generateSurveyLink = async (surveyId, title) => {
  try {
    const userId = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
    
    if (!userId) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
      return
    }
    
    currentUserId.value = userId
    console.log('🔗 生成问卷链接，问卷ID:', surveyId, '用户ID:', userId)
    
    // 调用后端生成问卷链接
    const response = await request.get('/api/surveystar/generate-link', {
      surveyId: surveyId,
      userId: userId,
      source: 'child-app'
    })
    
    console.log('✅ 问卷链接生成响应：', response)
    
    // request.js 返回的是 response.data，包含 code, message, data
    if (response && response.code === 200) {
      // 获取链接：可能在 response.data.link 或 response.data 中
      const link = response.data?.link || response.data
      
      if (link && typeof link === 'string' && link.startsWith('http')) {
        surveyUrl.value = link
        console.log('🌐 问卷URL：', surveyUrl.value)
      } else {
        throw new Error('问卷链接格式不正确')
      }
    } else {
      throw new Error(response?.message || response?.msg || '获取问卷链接失败')
    }
  } catch (error) {
    console.error('❌ 生成问卷链接失败：', error)
    uni.showToast({
      title: error.message || '加载问卷失败，请重试',
      icon: 'none',
      duration: 2000
    })
    
    // 2秒后返回
    setTimeout(() => {
      uni.navigateBack()
    }, 2000)
  }
}

const handleWebViewMessage = (event) => {
  try {
    console.log('💬 接收到web-view消息：', event)
    
    // 处理不同平台的消息格式
    let messageData = null
    if (event.detail && event.detail.data) {
      messageData = event.detail.data
    } else if (event.detail) {
      messageData = event.detail
    } else if (event.data) {
      messageData = event.data
    }
    
    if (messageData) {
      // 处理数组格式的消息
      if (Array.isArray(messageData) && messageData.length > 0) {
        const data = messageData[0]
        console.log('📨 消息数据：', data)
        
        // 检查是否包含完成标志
        if (data && (data.completed || data.success)) {
          console.log('🎉 通过消息检测到问卷完成')
          handleCompletionDetected()
        }
      } else if (typeof messageData === 'object') {
        // 处理对象格式的消息
        if (messageData.completed || messageData.success) {
          console.log('🎉 通过消息检测到问卷完成')
          handleCompletionDetected()
        }
      }
    }
  } catch (error) {
    // 静默处理消息解析错误，避免影响用户体验
    console.warn('⚠️ 处理web-view消息时出错（可忽略）：', error.message)
  }
}

// 处理检测到完成页面的逻辑
const handleCompletionDetected = async () => {
  if (isOnCompletionPage.value) {
    return // 已经检测过了，避免重复处理
  }
  
  console.log('🎉 检测到问卷完成页面')
  isOnCompletionPage.value = true
  showReturnButton.value = true
  
  // 自动标记完成（只标记一次）
  if (!hasAutoMarked.value) {
    hasAutoMarked.value = true
    await markSurveyCompleted(true) // true 表示自动标记，不显示提示
  }
  
  // 停止检测
  if (checkInterval) {
    clearInterval(checkInterval)
    checkInterval = null
  }
}

// 检测是否在完成页面
const checkIfOnCompletionPage = () => {
  try {
    // #ifdef H5
    const webView = document.querySelector('.survey-content')
    if (webView && webView.contentWindow) {
      try {
        const url = webView.contentWindow.location?.href || ''
        const title = webView.contentWindow.document?.title || ''
        
        // 检测是否包含完成页面的关键词
        const completionKeywords = [
          '抽奖', 'lottery', 'draw', 
          '完成', 'complete', 'finished',
          '提交成功', 'submit', 'submitted',
          '感谢', 'thank', 'thanks',
          '恭喜', 'congratulations', 'reward',
          '已提交', 'success'
        ]
        
        const urlLower = url.toLowerCase()
        const titleLower = title.toLowerCase()
        
        const isCompletion = completionKeywords.some(keyword => 
          urlLower.includes(keyword.toLowerCase()) || 
          titleLower.includes(keyword.toLowerCase())
        )
        
        if (isCompletion && !isOnCompletionPage.value) {
          console.log('🎉 检测到完成页面，URL:', url, '标题:', title)
          handleCompletionDetected()
          return true
        }
      } catch (e) {
        // 跨域限制，无法访问 - 这是正常的
        // console.log('⚠️ 无法访问 web-view 内容（跨域限制）')
      }
    }
    // #endif
    
    // #ifdef MP-WEIXIN || MP-ALIPAY || MP-BAIDU || MP-TOUTIAO || MP-QQ
    // 小程序环境下，web-view 的 URL 变化可以通过其他方式检测
    // 这里暂时无法直接检测，依赖消息机制
    // #endif
  } catch (error) {
    console.warn('⚠️ 检测完成页面时出错：', error)
  }
  return false
}

const startSurveyCompletionDetection = () => {
  console.log('🔍 启动问卷完成检测')
  
  // 页面加载3秒后开始检测
  setTimeout(() => {
    console.log('✅ 问卷页面已加载，开始检测完成状态')
    
    // 立即检测一次
    checkIfOnCompletionPage()
    
    // 每 2 秒检测一次是否进入完成页面（仅 H5 环境）
    // #ifdef H5
    checkInterval = setInterval(() => {
      if (!isOnCompletionPage.value) {
        checkIfOnCompletionPage()
      } else {
        // 已经检测到完成页面，停止检测
        if (checkInterval) {
          clearInterval(checkInterval)
          checkInterval = null
        }
      }
    }, 2000)
    // #endif
    
    // 尝试通过 JavaScript 注入，为问卷星页面添加顶部 padding
    // 注意：由于跨域限制，这可能无法生效，但尝试一下
    try {
      // #ifdef H5
      const webView = document.querySelector('.survey-content')
      if (webView && webView.contentWindow) {
        const headerHeight = 100 // rpx 转 px 大约 50px
        const script = `
          (function() {
            try {
              const style = document.createElement('style');
              style.innerHTML = 'body { padding-top: ${headerHeight}px !important; }';
              document.head.appendChild(style);
            } catch(e) {
              console.log('无法注入样式:', e);
            }
          })();
        `
        try {
          webView.contentWindow.eval(script)
        } catch (e) {
          // 跨域限制，无法执行
        }
      }
      // #endif
    } catch (error) {
      console.warn('⚠️ 无法注入样式到问卷星页面（跨域限制）:', error)
    }
  }, 3000)
  
  // 页面卸载时清理定时器
  // #ifdef H5
  if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', () => {
      if (checkInterval) {
        clearInterval(checkInterval)
        checkInterval = null
      }
    })
  }
  // #endif
}

const returnToList = () => {
  console.log('🔙 返回问卷列表')
  console.log('🔍 当前是否在完成页面:', isOnCompletionPage.value)
  
  // 如果检测到在完成页面，或者已经自动标记完成，直接返回（因为已经自动标记了）
  if (isOnCompletionPage.value || hasAutoMarked.value) {
    console.log('✅ 在完成页面或已自动标记，直接返回')
    uni.navigateBack()
    return
  }
  
  // 如果不在完成页面，再次尝试检测一次（可能检测有延迟）
  const isCompletion = checkIfOnCompletionPage()
  if (isCompletion || hasAutoMarked.value) {
    console.log('✅ 最后检测到完成页面，直接返回')
    uni.navigateBack()
    return
  }
  
  // 不在完成页面，询问用户是否已完成问卷
  console.log('📝 在填写页面，询问用户是否完成')
  uni.showModal({
    title: '提示',
    content: '您是否已完成该问卷？',
    confirmText: '已完成',
    cancelText: '未完成',
    success: async (res) => {
      if (res.confirm) {
        // 用户确认已完成，调用接口记录
        await markSurveyCompleted(false) // false 表示手动标记，显示提示
      }
      // 无论是否完成，都返回列表
      uni.navigateBack()
    },
    fail: () => {
      // 取消时也返回
      uni.navigateBack()
    }
  })
}

// 标记问卷已完成
// isAuto: true 表示自动标记（不显示提示），false 表示手动标记（显示提示）
const markSurveyCompleted = async (isAuto = false) => {
  if (!currentSurveyId.value || !currentUserId.value) {
    console.warn('⚠️ 无法标记完成：缺少问卷ID或用户ID')
    return
  }
  
  // 如果已经标记过，不再重复标记
  if (hasAutoMarked.value && isAuto) {
    console.log('ℹ️ 已经自动标记过，跳过')
    return
  }
  
  try {
    console.log('✅ 标记问卷完成，问卷ID:', currentSurveyId.value, '用户ID:', currentUserId.value, '自动标记:', isAuto)
    
    const response = await request.post('/api/surveystar/mark-completed', null, {
      questId: currentSurveyId.value,
      userId: currentUserId.value
    })
    
    if (response && response.code === 200) {
      console.log('✅ 问卷完成状态已记录')
      hasAutoMarked.value = true // 标记为已记录
      
      // 只有手动标记时才显示提示
      if (!isAuto) {
        uni.showToast({
          title: '已完成记录',
          icon: 'success',
          duration: 1500
        })
      }
    } else {
      console.warn('⚠️ 标记完成失败:', response?.message)
    }
  } catch (error) {
    console.error('❌ 标记问卷完成失败:', error)
    // 静默失败，不影响用户体验
  }
}

const goBack = () => {
  console.log('🔙 返回上一页')
  // 调用统一的返回逻辑
  returnToList()
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (checkInterval) {
    clearInterval(checkInterval)
    checkInterval = null
  }
})
</script>

<style scoped>
.container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  position: relative;
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  padding-top: calc(20rpx + var(--status-bar-height, 0px));
  height: calc(100rpx + var(--status-bar-height, 0px));
  min-height: 100rpx;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  color: #ffffff;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.15);
  z-index: 1000;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  flex-shrink: 0;
}

.back-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.back-icon {
  width: 24rpx;
  height: 24rpx;
  border-left: 3rpx solid #ffffff;
  border-bottom: 3rpx solid #ffffff;
  transform: rotate(45deg);
}

.title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.spacer {
  width: 80rpx;
  flex-shrink: 0;
}

.header-return-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.header-return-btn:active {
  transform: scale(0.9);
}

.check-icon {
  width: 32rpx;
  height: 32rpx;
  position: relative;
}

.check-icon::before {
  content: '';
  position: absolute;
  width: 20rpx;
  height: 3rpx;
  background: #ffffff;
  top: 12rpx;
  left: 4rpx;
  transform: rotate(45deg);
  border-radius: 2rpx;
}

.check-icon::after {
  content: '';
  position: absolute;
  width: 12rpx;
  height: 3rpx;
  background: #ffffff;
  top: 18rpx;
  left: 8rpx;
  transform: rotate(-45deg);
  border-radius: 2rpx;
}

.web-view-wrapper {
  flex: 1;
  width: 100%;
  height: 0; /* 使用flex布局自动计算高度 */
  margin-top: calc(100rpx + var(--status-bar-height, 0px));
  position: relative;
  overflow: hidden;
}

.survey-content {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

/* 快速返回按钮 */
.quick-return-btn {
  position: fixed;
  bottom: 60rpx;
  right: 32rpx;
  z-index: 99;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  color: #ffffff;
  border-radius: 50%;
  box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.3);
  transition: all 0.3s ease;
}

.quick-return-btn:active {
  transform: scale(0.95);
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.4);
}

.arrow-left-icon {
  width: 24rpx;
  height: 24rpx;
  position: relative;
}

.arrow-left-icon::before {
  content: '';
  position: absolute;
  width: 12rpx;
  height: 12rpx;
  border-right: 2rpx solid #ffffff;
  border-top: 2rpx solid #ffffff;
  top: 50%;
  left: 50%;
  transform: translate(-30%, -50%) rotate(-135deg);
}

.quick-return-btn .btn-text {
  font-size: 20rpx;
  font-weight: 600;
  color: #ffffff;
}

/* 返回按钮容器 */
.return-button-container {
  position: fixed;
  bottom: 32rpx;
  left: 32rpx;
  right: 32rpx;
  z-index: 100;
  display: flex;
  justify-content: center;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(50rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.return-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #ffffff;
  padding: 20rpx 40rpx;
  border-radius: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(16, 185, 129, 0.3);
  transition: all 0.3s ease;
}

.return-button:active {
  transform: translateY(-2rpx);
  box-shadow: 0 12rpx 32rpx rgba(16, 185, 129, 0.4);
}

.return-icon {
  width: 28rpx;
  height: 28rpx;
  position: relative;
}

.return-icon::before {
  content: '';
  position: absolute;
  width: 18rpx;
  height: 3rpx;
  background: #ffffff;
  top: 10rpx;
  left: 2rpx;
  transform: rotate(45deg);
  border-radius: 2rpx;
}

.return-icon::after {
  content: '';
  position: absolute;
  width: 10rpx;
  height: 3rpx;
  background: #ffffff;
  top: 16rpx;
  left: 6rpx;
  transform: rotate(-45deg);
  border-radius: 2rpx;
}

/* 优化快速返回按钮位置，避免被底部导航栏遮挡 */
.quick-return-btn {
  bottom: calc(60rpx + 120rpx + env(safe-area-inset-bottom));
}

.return-text {
  font-size: 28rpx;
  font-weight: 600;
}
</style>

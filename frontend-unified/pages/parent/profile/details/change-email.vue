<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">修改邮箱</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <view class="form-container">
        <!-- 当前邮箱显示 -->
        <view class="current-info">
          <text class="current-label">当前邮箱</text>
          <text class="current-value">{{currentEmail}}</text>
        </view>

        <!-- 新邮箱 -->
        <view class="form-item">
          <text class="form-label">新邮箱</text>
          <input 
            v-model="formData.newEmail"
            type="text"
            placeholder="请输入新邮箱地址"
            class="form-input"
          />
        </view>

        <!-- 验证码 -->
        <view class="form-item">
          <text class="form-label">验证码</text>
          <view class="verify-wrapper">
            <input 
              v-model="formData.verifyCode"
              type="number"
              placeholder="请输入验证码"
              class="verify-input"
              maxlength="6"
            />
            <button 
              class="verify-btn"
              :class="{ 'disabled': !canSendCode || countdown > 0 }"
              :disabled="!canSendCode || countdown > 0"
              @click="sendVerifyCode"
            >
              <text v-if="countdown > 0">{{countdown}}s后重发</text>
              <text v-else>{{sendingCode ? '发送中...' : '获取验证码'}}</text>
            </button>
          </view>
        </view>

        <!-- 提交按钮 -->
        <button 
          class="submit-btn" 
          :class="{ 'disabled': !canSubmit || loading }"
          :disabled="!canSubmit || loading"
          @click="handleSubmit"
        >
          <text v-if="loading">修改中...</text>
          <text v-else>确认修改</text>
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { userApi } from '@/utils/api.js'
import captcha from '@/utils/captcha.js'

// 表单数据
const formData = ref({
  newEmail: '',
  verifyCode: ''
})

const currentEmail = ref('')
const userInfo = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

// 邮箱格式验证正则
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// 计算是否可以发送验证码
const canSendCode = computed(() => {
  return emailRegex.test(formData.value.newEmail)
})

// 计算是否可以提交
const canSubmit = computed(() => {
  return canSendCode.value && 
         formData.value.verifyCode.length >= 4 && 
         formData.value.verifyCode.length <= 6
})

// 页面加载时获取用户信息
onMounted(() => {
  console.log('修改邮箱页面加载，开始获取用户信息')
  loadUserInfo()
})

// 页面卸载时清除定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

// 获取用户信息
const loadUserInfo = async () => {
  try {
    console.log('调用获取当前用户信息接口')
    
    // 先获取当前用户的基本信息（包含ID）
    const currentUserResponse = await userApi.getCurrentUser()
    console.log('获取当前用户基本信息成功：', currentUserResponse)
    
    if (currentUserResponse.code === 200 && currentUserResponse.data && currentUserResponse.data.id) {
      const userId = currentUserResponse.data.id
      console.log('当前用户ID：', userId)
      
      // 使用用户ID获取完整的用户信息（包含phone和email）
      const fullUserResponse = await userApi.getUserById(userId)
      console.log('获取完整用户信息成功：', fullUserResponse)
      
      if (fullUserResponse.code === 200 && fullUserResponse.data) {
        userInfo.value = fullUserResponse.data
        console.log('用户邮箱：', fullUserResponse.data.email)
        
        // 格式化邮箱显示（隐藏用户名部分）
        if (fullUserResponse.data.email) {
          // 检查是否是临时邮箱（@temp.com格式）
          if (fullUserResponse.data.email.endsWith('@temp.com')) {
            currentEmail.value = '未绑定邮箱'
            console.log('用户使用临时邮箱，显示为未绑定')
          } else {
            const emailParts = fullUserResponse.data.email.split('@')
            if (emailParts.length === 2) {
              const username = emailParts[0]
              const domain = emailParts[1]
              const hiddenUsername = username.length > 3 
                ? username.substring(0, 3) + '***' 
                : username.substring(0, 1) + '***'
              currentEmail.value = hiddenUsername + '@' + domain
              console.log('格式化后的邮箱：', currentEmail.value)
            } else {
              currentEmail.value = fullUserResponse.data.email
            }
          }
        } else {
          currentEmail.value = '未绑定邮箱'
          console.log('用户未绑定邮箱')
        }
      } else {
        console.error('获取完整用户信息失败：', fullUserResponse.message)
        uni.showToast({
          title: fullUserResponse.message || '获取用户详细信息失败',
          icon: 'none'
        })
      }
    } else {
      console.error('获取当前用户基本信息失败：', currentUserResponse.message)
      uni.showToast({
        title: currentUserResponse.message || '获取用户信息失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('获取用户信息异常：', error)
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    })
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 发送验证码
const sendVerifyCode = async () => {
  if (!canSendCode.value || countdown.value > 0) {
    return
  }

  console.log('用户点击发送验证码，邮箱：', formData.value.newEmail)

  try {
    sendingCode.value = true
    
    // 1. 先进行人机验证
    console.log('开始人机验证')
    uni.showLoading({
      title: '正在加载验证码...',
      mask: true
    })
    
    let captchaResult
    try {
      captchaResult = await captcha.show()
      console.log('人机验证成功，ticket：', captchaResult.ticket)
    } catch (captchaError) {
      console.error('人机验证失败：', captchaError)
      uni.hideLoading()
      
      // 如果是用户取消，不显示错误提示
      if (captchaError.message && captchaError.message.includes('取消')) {
        console.log('用户取消了人机验证')
        return
      }
      
      uni.showToast({
        title: captchaError.message || '验证失败，请重试',
        icon: 'none',
        duration: 2000
      })
      return
    }
    
    uni.hideLoading()
    
    // 2. 人机验证成功后，调用后端接口发送验证码
    console.log('调用发送邮箱验证码接口')
    
    const response = await userApi.sendEmailCodeWithCaptcha(
      formData.value.newEmail, 
      captchaResult.ticket, 
      captchaResult.randstr
    )
    console.log('发送验证码响应：', response)
    
    if (response.code === 200 || response === '验证码发送成功') {
      uni.showToast({
        title: '验证码已发送',
        icon: 'success'
      })
      
      // 开始倒计时
      startCountdown()
    } else {
      console.error('发送验证码失败：', response.message || response)
      uni.showToast({
        title: response.message || response || '发送失败，请稍后重试',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('发送验证码异常：', error)
    
    let errorMessage = '网络错误，请稍后重试'
    
    if (error.response && error.response.data) {
      const responseData = error.response.data
      if (responseData.message) {
        errorMessage = responseData.message
      }
    }
    
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
  } finally {
    sendingCode.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

// 验证邮箱格式
const validateEmail = (email) => {
  if (!emailRegex.test(email)) {
    return '请输入正确的邮箱格式'
  }
  return null
}

// 提交修改
const handleSubmit = async () => {
  console.log('用户提交修改邮箱表单')
  
  // 验证表单
  const emailError = validateEmail(formData.value.newEmail)
  if (emailError) {
    uni.showToast({
      title: emailError,
      icon: 'none'
    })
    return
  }
  
  if (formData.value.verifyCode.length < 4 || formData.value.verifyCode.length > 6) {
    uni.showToast({
      title: '请输入4-6位验证码',
      icon: 'none'
    })
    return
  }

  // 检查是否与当前邮箱相同（临时邮箱除外）
  if (userInfo.value && userInfo.value.email && 
      !userInfo.value.email.endsWith('@temp.com') && 
      userInfo.value.email === formData.value.newEmail) {
    uni.showToast({
      title: '新邮箱不能与当前邮箱相同',
      icon: 'none'
    })
    return
  }

  try {
    loading.value = true
    console.log('开始修改邮箱流程')
    
    if (!userInfo.value || !userInfo.value.id) {
      uni.showToast({
        title: '用户信息获取失败，请重试',
        icon: 'none'
      })
      return
    }
    
    // 构建更新数据（包含验证码）
    const updateData = {
      id: userInfo.value.id,
      email: formData.value.newEmail,
      verificationCode: formData.value.verifyCode
    }
    
    console.log('调用更新用户信息接口修改邮箱，数据：', updateData)
    const response = await userApi.updateUser(updateData)
    console.log('修改邮箱响应：', response)
    
    if (response.code === 200) {
      uni.showToast({
        title: '邮箱修改成功',
        icon: 'success'
      })
      
      // 延迟返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      console.error('修改邮箱失败：', response.message)
      uni.showToast({
        title: response.message || '修改失败，请稍后重试',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('修改邮箱异常：', error)
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
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
  cursor: pointer;
}

.back-btn text {
  color: #6b7280;
  font-size: 32rpx;
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

/* 表单容器 */
.form-container {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 40rpx;
}

/* 当前信息显示 */
.current-info {
  padding: 30rpx;
  background-color: #f9fafb;
  border-radius: 12rpx;
  margin-bottom: 40rpx;
}

.current-label {
  display: block;
  font-size: 24rpx;
  color: #6b7280;
  margin-bottom: 8rpx;
}

.current-value {
  font-size: 32rpx;
  color: #111827;
  font-weight: 500;
}

/* 表单项 */
.form-item {
  margin-bottom: 40rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #374151;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  border: 1px solid #e5e7eb;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #3b82f6;
  outline: none;
}

/* 验证码输入区域 */
.verify-wrapper {
  display: flex;
  gap: 20rpx;
}

.verify-input {
  flex: 1;
  height: 88rpx;
  padding: 0 24rpx;
  border: 1px solid #e5e7eb;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.verify-input:focus {
  border-color: #3b82f6;
  outline: none;
}

.verify-btn {
  padding: 24rpx 32rpx;
  background-color: #3b82f6;
  color: #ffffff;
  border: none;
  border-radius: 12rpx;
  font-size: 24rpx;
  white-space: nowrap;
  -webkit-tap-highlight-color: transparent;
}

.verify-btn.disabled {
  background-color: #d1d5db !important;
  color: #9ca3af !important;
  pointer-events: none;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 28rpx;
  background-color: #3b82f6;
  color: #ffffff;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
  margin-top: 40rpx;
  -webkit-tap-highlight-color: transparent;
}

.submit-btn.disabled {
  background-color: #d1d5db !important;
  color: #9ca3af !important;
  pointer-events: none;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style>

<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">修改手机号</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <view class="form-container">
        <!-- 当前手机号显示 -->
        <view class="current-info">
          <text class="current-label">当前手机号</text>
          <text class="current-value">{{currentPhone}}</text>
        </view>

        <!-- 新手机号 -->
        <view class="form-item">
          <text class="form-label">新手机号</text>
          <input 
            v-model="formData.newPhone"
            type="number"
            placeholder="请输入新手机号"
            class="form-input"
            maxlength="11"
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

// 表单数据
const formData = ref({
  newPhone: '',
  verifyCode: ''
})

const currentPhone = ref('')
const userInfo = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

// 计算是否可以发送验证码
const canSendCode = computed(() => {
  return /^1[3-9]\d{9}$/.test(formData.value.newPhone)
})

// 计算是否可以提交
const canSubmit = computed(() => {
  return canSendCode.value && 
         formData.value.verifyCode.length >= 4 && 
         formData.value.verifyCode.length <= 6
})

// 页面加载时获取用户信息
onMounted(() => {
  console.log('修改手机号页面加载，开始获取用户信息')
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
        console.log('用户手机号：', fullUserResponse.data.phone)
        
        // 格式化手机号显示（隐藏中间4位）
        if (fullUserResponse.data.phone) {
          currentPhone.value = fullUserResponse.data.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
          console.log('格式化后的手机号：', currentPhone.value)
        } else {
          currentPhone.value = '未绑定手机号'
          console.log('用户未绑定手机号')
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

  console.log('用户点击发送验证码，手机号：', formData.value.newPhone)

  try {
    sendingCode.value = true
    console.log('调用发送手机验证码接口')
    
    // 使用登录页面相同的发送验证码接口
    const response = await userApi.sendSmsCode(formData.value.newPhone)
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
    // 检查是否是成功的响应但格式不同
    if (error && typeof error === 'string' && error.includes('验证码发送成功')) {
      uni.showToast({
        title: '验证码已发送',
        icon: 'success'
      })
      startCountdown()
    } else {
      uni.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    }
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

// 验证手机号格式
const validatePhone = (phone) => {
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    return '请输入正确的手机号格式'
  }
  return null
}

// 提交修改
const handleSubmit = async () => {
  console.log('用户提交修改手机号表单')
  
  // 验证表单
  const phoneError = validatePhone(formData.value.newPhone)
  if (phoneError) {
    uni.showToast({
      title: phoneError,
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

  // 检查是否与当前手机号相同
  if (userInfo.value && userInfo.value.phone === formData.value.newPhone) {
    uni.showToast({
      title: '新手机号不能与当前手机号相同',
      icon: 'none'
    })
    return
  }

  try {
    loading.value = true
    console.log('开始修改手机号流程')
    
    if (!userInfo.value || !userInfo.value.id) {
      uni.showToast({
        title: '用户信息获取失败，请重试',
        icon: 'none'
      })
      return
    }
    
    // 构建更新数据
    const updateData = {
      id: userInfo.value.id,
      phone: formData.value.newPhone
    }
    
    console.log('调用更新用户信息接口修改手机号')
    const response = await userApi.updateUser(updateData)
    console.log('修改手机号响应：', response)
    
    if (response.code === 200) {
      uni.showToast({
        title: '手机号修改成功',
        icon: 'success'
      })
      
      // 延迟返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      console.error('修改手机号失败：', response.message)
      uni.showToast({
        title: response.message || '修改失败，请稍后重试',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('修改手机号异常：', error)
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

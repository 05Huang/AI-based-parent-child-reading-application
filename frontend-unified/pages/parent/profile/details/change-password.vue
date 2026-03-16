<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">修改密码</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <view class="form-container">
        <!-- 当前密码 -->
        <view class="form-item">
          <text class="form-label">当前密码</text>
          <view class="input-wrapper">
            <input 
              v-model="formData.currentPassword"
              :type="showCurrentPassword ? 'text' : 'password'"
              placeholder="请输入当前密码"
              class="form-input"
              maxlength="20"
            />
            <text 
              :class="['password-toggle', showCurrentPassword ? 'fas fa-eye-slash' : 'fas fa-eye']"
              @click="toggleCurrentPasswordVisibility"
            ></text>
          </view>
        </view>

        <!-- 新密码 -->
        <view class="form-item">
          <text class="form-label">新密码</text>
          <view class="input-wrapper">
            <input 
              v-model="formData.newPassword"
              :type="showNewPassword ? 'text' : 'password'"
              placeholder="请输入新密码（6-20位）"
              class="form-input"
              maxlength="20"
            />
            <text 
              :class="['password-toggle', showNewPassword ? 'fas fa-eye-slash' : 'fas fa-eye']"
              @click="toggleNewPasswordVisibility"
            ></text>
          </view>
          <text class="form-hint">密码长度为6-20位，建议包含字母、数字和符号</text>
        </view>

        <!-- 确认新密码 -->
        <view class="form-item">
          <text class="form-label">确认新密码</text>
          <view class="input-wrapper">
            <input 
              v-model="formData.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入新密码"
              class="form-input"
              maxlength="20"
            />
            <text 
              :class="['password-toggle', showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye']"
              @click="toggleConfirmPasswordVisibility"
            ></text>
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
import { ref, computed } from 'vue'
import { userApi } from '@/utils/api.js'

// 表单数据
const formData = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码显示状态
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)

// 计算是否可以提交
const canSubmit = computed(() => {
  return formData.value.currentPassword.length >= 6 &&
         formData.value.newPassword.length >= 6 &&
         formData.value.confirmPassword.length >= 6 &&
         formData.value.newPassword === formData.value.confirmPassword
})

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 切换当前密码显示状态
const toggleCurrentPasswordVisibility = () => {
  showCurrentPassword.value = !showCurrentPassword.value
}

// 切换新密码显示状态
const toggleNewPasswordVisibility = () => {
  showNewPassword.value = !showNewPassword.value
}

// 切换确认密码显示状态
const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

// 验证密码格式
const validatePassword = (password) => {
  if (password.length < 6 || password.length > 20) {
    return '密码长度必须在6-20位之间'
  }
  return null
}

// 提交修改
const handleSubmit = async () => {
  console.log('用户提交修改密码表单')
  
  // 验证表单
  const newPasswordError = validatePassword(formData.value.newPassword)
  if (newPasswordError) {
    uni.showToast({
      title: newPasswordError,
      icon: 'none'
    })
    return
  }
  
  if (formData.value.newPassword !== formData.value.confirmPassword) {
    uni.showToast({
      title: '两次输入的新密码不一致',
      icon: 'none'
    })
    return
  }
  
  if (formData.value.currentPassword === formData.value.newPassword) {
    uni.showToast({
      title: '新密码不能与当前密码相同',
      icon: 'none'
    })
    return
  }

  try {
    loading.value = true
    console.log('开始修改密码流程')
    
    // 获取当前用户信息
    const userResponse = await userApi.getCurrentUser()
    if (userResponse.code !== 200 || !userResponse.data) {
      uni.showToast({
        title: '获取用户信息失败',
        icon: 'none'
      })
      return
    }
    
    const userId = userResponse.data.id
    const username = userResponse.data.username
    console.log('当前用户ID：', userId, '用户名：', username)
    
    // 第一步：验证当前密码是否正确
    console.log('验证当前密码是否正确')
    try {
      const loginData = {
        userName: username,
        password: formData.value.currentPassword,
        terminal: 1
      }
      
      const loginResponse = await userApi.verifyPassword(loginData)
      console.log('密码验证响应：', loginResponse)
      
      if (loginResponse.code !== 200) {
        uni.showToast({
          title: '当前密码错误',
          icon: 'none'
        })
        return
      }
    } catch (error) {
      console.error('密码验证失败：', error)
      uni.showToast({
        title: '当前密码错误',
        icon: 'none'
      })
      return
    }
    
    // 第二步：密码验证通过，更新为新密码
    console.log('当前密码验证通过，开始更新密码')
    const updateData = {
      id: userId,
      password: formData.value.newPassword
    }
    
    console.log('调用更新用户信息接口修改密码')
    const response = await userApi.updateUser(updateData)
    console.log('修改密码响应：', response)
    
    if (response.code === 200) {
      uni.showToast({
        title: '密码修改成功',
        icon: 'success'
      })
      
      // 延迟返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      console.error('修改密码失败：', response.message)
      uni.showToast({
        title: response.message || '修改失败，请稍后重试',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('修改密码异常：', error)
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

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 80rpx 0 24rpx;
  border: 1px solid #e5e7eb;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #3b82f6;
  outline: none;
}

.password-toggle {
  position: absolute;
  right: 24rpx;
  font-size: 32rpx;
  color: #6b7280;
  z-index: 2;
  padding: 10rpx;
  -webkit-tap-highlight-color: transparent;
}

.form-hint {
  display: block;
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 12rpx;
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

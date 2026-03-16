<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">修改密码</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      <view class="form-card">
        <!-- 当前密码 -->
        <view class="form-item">
          <view class="form-label-wrapper">
            <text class="form-icon fas fa-lock"></text>
            <text class="form-label">当前密码</text>
          </view>
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
          <view class="form-label-wrapper">
            <text class="form-icon fas fa-key"></text>
            <text class="form-label">新密码</text>
          </view>
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
          <view class="form-label-wrapper">
            <text class="form-icon fas fa-check-circle"></text>
            <text class="form-label">确认新密码</text>
          </view>
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
        <view class="button-container">
          <view 
            class="submit-btn" 
            :class="{ 'disabled': !canSubmit || loading }"
            @click="handleSubmit"
          >
            <text class="fas fa-check submit-icon"></text>
            <text class="submit-text">{{ loading ? '修改中...' : '确认修改' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@/utils/api.js'

// 系统状态栏高度
const statusBarHeight = ref(20)
const headerHeight = ref(100)

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

// 页面加载时获取状态栏高度
onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  headerHeight.value = statusBarHeight.value + 64 + 32
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
  console.log('[儿童端修改密码] 用户提交修改密码表单')
  
  if (!canSubmit.value || loading.value) {
    return
  }
  
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
    console.log('[儿童端修改密码] 开始修改密码流程')
    
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
    console.log('[儿童端修改密码] 当前用户ID：', userId, '用户名：', username)
    
    // 第一步：验证当前密码是否正确
    console.log('[儿童端修改密码] 验证当前密码是否正确')
    try {
      const loginData = {
        userName: username,
        password: formData.value.currentPassword,
        terminal: 1
      }
      
      const loginResponse = await userApi.verifyPassword(loginData)
      console.log('[儿童端修改密码] 密码验证响应：', loginResponse)
      
      if (loginResponse.code !== 200) {
        uni.showToast({
          title: '当前密码错误',
          icon: 'none'
        })
        return
      }
    } catch (error) {
      console.error('[儿童端修改密码] 密码验证失败：', error)
      uni.showToast({
        title: '当前密码错误',
        icon: 'none'
      })
      return
    }
    
    // 第二步：密码验证通过，更新为新密码
    console.log('[儿童端修改密码] 当前密码验证通过，开始更新密码')
    const updateData = {
      id: userId,
      password: formData.value.newPassword
    }
    
    console.log('[儿童端修改密码] 调用更新用户信息接口修改密码')
    const response = await userApi.updateUser(updateData)
    console.log('[儿童端修改密码] 修改密码响应：', response)
    
    if (response.code === 200) {
      uni.showToast({
        title: '密码修改成功',
        icon: 'success',
        duration: 1500
      })
      
      // 延迟返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({
        title: response.message || '修改密码失败，请重试',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('[儿童端修改密码] 修改密码异常：', error)
    uni.showToast({
      title: error?.message || '修改密码失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
}

/* 顶部导航栏样式 */
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
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 36rpx;
  transition: all 0.3s ease;
}

.back-btn:active {
  transform: scale(0.9);
  opacity: 0.8;
}

.page-title {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
  flex: 1;
  text-align: center;
}

.header-placeholder {
  width: 64rpx;
}

/* 主内容区域样式 */
.main-content {
  padding: 24rpx;
  padding-bottom: 40rpx;
  width: 100%;
  box-sizing: border-box;
}

.form-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-item:last-of-type {
  margin-bottom: 0;
}

.form-label-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.form-icon {
  font-size: 28rpx;
  color: #6366f1;
}

.form-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
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
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  font-size: 28rpx;
  background-color: #f9fafb;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.form-input:focus {
  border-color: #6366f1;
  background-color: #ffffff;
  box-shadow: 0 0 0 4rpx rgba(99, 102, 241, 0.1);
}

.password-toggle {
  position: absolute;
  right: 24rpx;
  font-size: 32rpx;
  color: #9ca3af;
  transition: all 0.3s ease;
}

.password-toggle:active {
  color: #6366f1;
  transform: scale(0.9);
}

.form-hint {
  display: block;
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 12rpx;
  line-height: 1.5;
}

.button-container {
  margin-top: 48rpx;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
  transition: all 0.3s ease;
}

.submit-btn.disabled {
  background: #d1d5db;
  box-shadow: none;
  opacity: 0.6;
}

.submit-btn:active:not(.disabled) {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.3);
}

.submit-icon {
  font-size: 32rpx;
  color: #ffffff;
}

.submit-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}
</style>


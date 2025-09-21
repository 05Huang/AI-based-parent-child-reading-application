<template>
  <view class="login-page">
    <!-- Logo和标题 -->
    <view class="header">
      <view class="logo-box">
        <text class="fa-solid fa-book-reader"></text>
      </view>
      <text class="title">亲子阅读</text>
      <text class="subtitle">与AI一起探索阅读的乐趣</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-container">
      <view class="input-box">
        <view class="phone-input">
          <text class="country-code">+86</text>
          <text class="divider">|</text>
          <input type="number" placeholder="请输入手机号" class="phone-number" v-model="phoneNumber" />
        </view>
      </view>
      
      <view class="input-box">
        <view class="verify-input">
          <input type="text" placeholder="请输入验证码" class="verify-code" v-model="verifyCode" />
          <text 
            class="verify-btn" 
            :class="{ 'verify-btn-active': phoneNumber.length === 11 && countdown === 0, 'verify-btn-disabled': countdown > 0 }"
            @click="getVerifyCode"
          >
            {{ countdown > 0 ? `${countdown}s后重新获取` : '获取验证码' }}
          </text>
        </view>
      </view>

      <button class="login-btn" @click="handleLogin">登录</button>
      
      <!-- 注册入口 -->
      <view class="register-entry">
        <text class="register-text" @click="goToRegister">还没有账号？立即注册</text>
      </view>
      
      <!-- 其他登录选项 -->
      <view class="other-login">
        <text class="other-title">其他登录方式</text>
        <view class="social-buttons">
          <view class="social-btn">
            <text class="fab fa-weixin"></text>
          </view>
          <view class="social-btn">
            <text class="fab fa-qq"></text>
          </view>
          <view class="social-btn">
            <text class="fab fa-apple"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部协议 -->
    <view class="footer">
      <text class="agreement">
        登录即表示同意
        <text class="link">用户协议</text>
        和
        <text class="link">隐私政策</text>
      </text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'

// 定义响应式变量
const phoneNumber = ref('')
const verifyCode = ref('')
const countdown = ref(0)
const timer = ref(null)

// 验证手机号格式
const validatePhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

// 获取验证码
const getVerifyCode = async () => {
  console.log('开始获取验证码流程')
  
  // 验证手机号
  if (!phoneNumber.value) {
    uni.showToast({
      title: '请输入手机号',
      icon: 'none'
    })
    return
  }
  
  if (!validatePhone(phoneNumber.value)) {
    uni.showToast({
      title: '请输入正确的手机号格式',
      icon: 'none'
    })
    return
  }

  try {
    console.log('发送获取验证码请求，手机号：', phoneNumber.value)
    
    // 调用后端接口获取验证码
    const res = await request.get('/api/user/smscode', {
      phone: phoneNumber.value
    })
    
    console.log('获取验证码响应：', res)
    
    uni.showToast({
      title: '验证码已发送',
      icon: 'success'
    })
    
    // 开始倒计时
    countdown.value = 60
    timer.value = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value--
      } else {
        clearInterval(timer.value)
      }
    }, 1000)
  } catch (error) {
    console.error('获取验证码出错：', error)
    
    // 解析错误信息
    let errorMessage = '获取验证码失败，请稍后重试'
    
    if (error.response && error.response.data) {
      const responseData = error.response.data
      
      // 根据后端返回的错误码或消息显示不同的提示
      if (responseData.code === 400) {
        if (responseData.message.includes('手机号')) {
          errorMessage = responseData.message || '手机号格式不正确'
        }
      } else if (responseData.code === 429) {
        errorMessage = '发送过于频繁，请稍后再试'
      }
    }
    
    // 显示错误提示
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
    
    // 如果是发送频繁的错误，重置倒计时
    if (errorMessage.includes('频繁')) {
      countdown.value = 0
      if (timer.value) {
        clearInterval(timer.value)
        timer.value = null
      }
    }
  }
}

// 登录处理
const handleLogin = async () => {
  console.log('开始登录流程')
  
  // 验证输入
  if (!phoneNumber.value) {
    uni.showToast({
      title: '请输入手机号',
      icon: 'none'
    })
    return
  }
  
  if (!validatePhone(phoneNumber.value)) {
    uni.showToast({
      title: '请输入正确的手机号格式',
      icon: 'none'
    })
    return
  }
  
  if (!verifyCode.value) {
    uni.showToast({
      title: '请输入验证码',
      icon: 'none'
    })
    return
  }

  try {
    console.log('发送登录请求，手机号：', phoneNumber.value, '验证码：', verifyCode.value)
    
    // 调用后端登录接口
    const res = await request.post('/api/user/login-by-phone', {
      phone: phoneNumber.value,
      verificationCode: verifyCode.value,
      terminal: 1  // 终端类型：1表示移动端
    })
    
    console.log('登录响应：', res)
    
    // 保存token和登录状态
    console.log('登录成功，保存token：', res.data)
    uni.setStorageSync('token', res.data.accessToken)
    uni.setStorageSync('isLoggedIn', true)
    
    // 显示登录成功提示并跳转
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500,
      success: () => {
        // 等待提示显示完毕后跳转
        setTimeout(() => {
          console.log('准备跳转到首页')
          // 尝试使用switchTab跳转
          uni.switchTab({
            url: '/pages/parent/home/home',
            success: () => {
              console.log('跳转到首页成功')
            },
            fail: (error) => {
              console.error('switchTab跳转失败，尝试使用navigateTo', error)
              // 如果switchTab失败，尝试使用navigateTo
              uni.navigateTo({
                url: '/pages/parent/home/home',
                success: () => {
                  console.log('navigateTo跳转成功')
                },
                fail: (navError) => {
                  console.error('所有跳转方式都失败', navError)
                  // 最后尝试使用reLaunch
                  uni.reLaunch({
                    url: '/pages/parent/home/home'
                  })
                }
              })
            }
          })
        }, 1500)
      }
    })
  } catch (error) {
    console.error('登录出错：', error)
    
    // 解析错误信息
    let errorMessage = '登录失败，请稍后重试'
    
    if (error.response && error.response.data) {
      const responseData = error.response.data
      
      // 根据后端返回的错误码或消息显示不同的提示
      if (responseData.code === 400) {
        if (responseData.message.includes('验证码')) {
          errorMessage = responseData.message || '验证码错误或已过期'
        } else if (responseData.message.includes('手机号')) {
          errorMessage = responseData.message || '手机号格式不正确'
        } else if (responseData.message.includes('不存在')) {
          errorMessage = '该手机号尚未注册，请先注册'
        }
      } else if (responseData.code === 500) {
        if (responseData.message.includes('禁用')) {
          errorMessage = '账号已被禁用，请联系客服'
        }
      }
    }
    
    // 显示错误提示
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
  }
}

const goToRegister = () => {
  uni.navigateTo({
    url: '../register/register'
  })
}
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.login-page {
  min-height: 100vh;
  background-color: #F9FAFB;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
}

.header {
  text-align: center;
  margin-bottom: 96rpx;
  margin-top: 32rpx;
}

.logo-box {
  width: 160rpx;
  height: 160rpx;
  background-color: rgb(59, 130, 246);
  border-radius: 32rpx;
  margin: 0 auto 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-box .fa-book-reader {
  color: #FFFFFF;
  font-size: 48rpx;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  display: block;
}

.subtitle {
  font-size: 28rpx;
  color: #6B7280;
  margin-top: 16rpx;
  display: block;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  margin: 0 auto;
  width: 85%;
  max-width: 600rpx;
}

.input-box {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
}

.phone-input, .verify-input {
  display: flex;
  align-items: center;
  height: 48rpx;
}

.country-code {
  color: #6B7280;
  font-size: 28rpx;
  flex-shrink: 0;
}

.divider {
  color: #E5E7EB;
  margin: 0 16rpx;
  flex-shrink: 0;
}

.phone-number, .verify-code {
  flex: 1;
  font-size: 28rpx;
  min-width: 0; /* 防止输入框溢出 */
}

.verify-btn {
  color: #9CA3AF;
  font-size: 28rpx;
  flex-shrink: 0;
  padding-left: 16rpx;
  transition: all 0.3s ease;
  cursor: not-allowed;
}

.verify-btn-active {
  color: rgb(59, 130, 246);
  cursor: pointer;
}

.verify-btn-disabled {
  color: #9CA3AF;
  cursor: not-allowed;
}

.login-btn {
  background: linear-gradient(135deg, rgb(59, 130, 246) 0%, rgb(96, 165, 250) 100%);
  color: #FFFFFF;
  padding: 28rpx;
  border-radius: 100rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  width: 100%;
  box-shadow: 0 8rpx 16rpx rgba(59, 130, 246, 0.2);
  transition: all 0.3s ease;
  margin-top: 16rpx;
  position: relative;
  overflow: hidden;
}

.login-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 8rpx rgba(59, 130, 246, 0.15);
}

/* 添加按钮点击涟漪效果 */
.login-btn::after {
  content: '';
  display: block;
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
  background-image: radial-gradient(circle, rgba(255, 255, 255, 0.3) 10%, transparent 10.01%);
  background-repeat: no-repeat;
  background-position: 50%;
  transform: scale(10, 10);
  opacity: 0;
  transition: transform .5s, opacity 1s;
}

.login-btn:active::after {
  transform: scale(0, 0);
  opacity: .3;
  transition: 0s;
}

.register-entry {
  text-align: center;
  margin-top: 24rpx;
}

.register-text {
  color: rgb(59, 130, 246);
  font-size: 28rpx;
}

.other-login {
  text-align: center;
  margin-top: 64rpx;
  width: 85%;
  max-width: 600rpx;
  margin-left: auto;
  margin-right: auto;
}

.other-title {
  color: #9CA3AF;
  font-size: 28rpx;
  margin-bottom: 32rpx;
  display: block;
}

.social-buttons {
  display: flex;
  justify-content: center;
  gap: 64rpx;
}

.social-btn {
  width: 96rpx;
  height: 96rpx;
  background: #FFFFFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.social-btn .fa-weixin {
  color: #07C160;
  font-size: 40rpx;
}

.social-btn .fa-qq {
  color: rgb(59, 130, 246);
  font-size: 40rpx;
}

.social-btn .fa-apple {
  color: #000000;
  font-size: 40rpx;
}

.footer {
  margin-top: auto;
  text-align: center;
  padding: 32rpx 0;
}

.agreement {
  color: #9CA3AF;
  font-size: 24rpx;
}

.link {
  color: rgb(59, 130, 246);
}

/* 移除输入框默认样式 */
input {
  outline: none;
  border: none;
}
</style> 
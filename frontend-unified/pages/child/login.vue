<template>
  <view class="login-page">
    <view class="login-container">
      <!-- Logo和标题 -->
      <view class="header">
        <view class="logo-box">
          <image src="/static/images/common/child-logo.svg" class="logo-img" mode="aspectFit"></image>
        </view>
        <text class="title">阅桥亲子阅读APP</text>
        <text class="subtitle">小朋友，欢迎来到奇妙的阅读世界！</text>
      </view>

      <!-- 登录表单 -->
      <view class="form-container">
        <!-- 验证码登录 -->
        <view v-if="loginMode === 'code'" class="login-form">
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
          
          <!-- 切换到密码登录 -->
          <view class="switch-mode">
            <text class="switch-text" @click="switchLoginMode('password')">使用密码登录</text>
          </view>
        </view>

        <!-- 密码登录 -->
        <view v-else class="login-form">
          <view class="input-box">
            <view class="username-input">
              <text class="fas fa-user input-icon"></text>
              <input type="text" placeholder="请输入用户名或手机号" class="username-field" v-model="username" />
            </view>
          </view>
          
          <view class="input-box">
            <view class="password-input">
              <text class="fas fa-lock input-icon"></text>
              <input 
                :type="showPassword ? 'text' : 'password'" 
                placeholder="请输入密码" 
                class="password-field" 
                v-model="password" 
              />
              <text 
                class="fas input-icon toggle-password" 
                :class="showPassword ? 'fa-eye-slash' : 'fa-eye'"
                @click="togglePasswordVisibility"
              ></text>
            </view>
          </view>

          <button class="login-btn" @click="handlePasswordLogin">登录</button>
          
          <!-- 切换到验证码登录 -->
          <view class="switch-mode">
            <text class="switch-text" @click="switchLoginMode('code')">使用验证码登录</text>
          </view>
        </view>
        
        <!-- 注册入口 -->
        <view class="register-entry">
          <text class="register-text" @click="goToRegister">还没有账号？立即注册</text>
        </view>
        
        <!-- 其他登录选项 -->
        <view class="other-login">
          <text class="other-title">其他登录方式</text>
          <view class="social-buttons">
            <view class="social-btn" @click="handleWechatLogin">
              <text class="fab fa-weixin"></text>
            </view>
            <view class="social-btn" @click="handleQQLogin">
              <text class="fab fa-qq"></text>
            </view>
            <view class="social-btn" @click="showEmailLoginDialog">
              <text class="fas fa-envelope"></text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部协议 -->
      <view class="footer">
        <text class="agreement">
          登录即表示同意
          <text class="link" @click="goToUserAgreement">用户协议</text>
          和
          <text class="link" @click="goToPrivacyPolicy">隐私政策</text>
        </text>
      </view>
    </view>

    <!-- 邮箱验证码登录弹窗 -->
    <view v-if="emailDialogVisible" class="email-dialog-mask" @click="closeEmailDialog">
      <view class="email-dialog" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">邮箱验证码登录</text>
          <text class="fas fa-times close-btn" @click="closeEmailDialog"></text>
        </view>
        <view class="dialog-body">
          <view class="input-box">
            <view class="email-input">
              <text class="fas fa-envelope input-icon"></text>
              <input type="text" placeholder="请输入邮箱" class="email-field" v-model="email" />
            </view>
          </view>
          
          <view class="input-box">
            <view class="verify-input">
              <input type="text" placeholder="请输入验证码" class="verify-code" v-model="emailVerifyCode" />
              <text 
                class="verify-btn" 
                :class="{ 'verify-btn-active': email && emailCountdown === 0, 'verify-btn-disabled': emailCountdown > 0 }"
                @click="getEmailVerifyCode"
              >
                {{ emailCountdown > 0 ? `${emailCountdown}s后重新获取` : '获取验证码' }}
              </text>
            </view>
          </view>
        </view>
        <view class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="closeEmailDialog">取消</button>
          <button class="dialog-btn confirm-btn" @click="handleEmailLogin">登录</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'
import captcha from '@/utils/captcha'
import { userBehaviorApi } from '@/utils/api.js'


// 登录模式：'code' 验证码登录，'password' 密码登录
const loginMode = ref('code')

// 验证码登录相关变量
const phoneNumber = ref('')
const verifyCode = ref('')
const countdown = ref(0)
const timer = ref(null)

// 密码登录相关变量
const username = ref('')
const password = ref('')
const showPassword = ref(false)

// 邮箱验证码登录相关变量
const emailDialogVisible = ref(false)
const email = ref('')
const emailVerifyCode = ref('')
const emailCountdown = ref(0)
const emailTimer = ref(null)

// 验证手机号格式
const validatePhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

// 获取验证码
const getVerifyCode = async () => {
  console.log('孩子端：开始获取验证码流程')
  
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
  
  // 如果正在倒计时，不允许重复获取
  if (countdown.value > 0) {
    console.log('验证码倒计时中，无法重复获取')
    return
  }

  try {
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
    
    // 2. 人机验证成功后，调用后端接口获取验证码
    console.log('发送获取验证码请求，手机号：', phoneNumber.value, '票据：', captchaResult.ticket)
    
    const res = await request.get('/api/user/smscode', {
      phone: phoneNumber.value,
      ticket: captchaResult.ticket,
      randstr: captchaResult.randstr
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
        } else if (responseData.message.includes('验证')) {
          errorMessage = responseData.message || '人机验证失败'
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
  console.log('孩子端：开始登录流程')
  
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
    console.log('发送孩子端登录请求，手机号：', phoneNumber.value, '验证码：', verifyCode.value)
    
    // 调用后端登录接口
    const res = await request.post('/api/user/login-by-phone', {
      phone: phoneNumber.value,
      verificationCode: verifyCode.value,
      terminal: 1,  // 终端类型：1表示移动端
      role: 2  // 标识为孩子端用户
    })
    
    console.log('孩子端登录响应：', res)
    
    // 保存token和登录状态
    console.log('登录成功，保存token：', res.data)
    uni.setStorageSync('token', res.data.accessToken)
    uni.setStorageSync('isLoggedIn', true)
    uni.setStorageSync('userRole', 'child')
    
    // 显示登录成功提示并跳转
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 800
    })
    
    // 获取并输出连续登录天数
    try {
      if (res.data && res.data.id) {
        // 使用返回的用户ID或当前存储的ID
        const userId = res.data.id
        userBehaviorApi.getConsecutiveReadingDays(userId).then(daysRes => {
          if (daysRes.code === 200) {
            console.log(`登录成功！用户已连续登录 ${daysRes.data} 天`)
          }
        }).catch(e => console.error('获取连续登录天数失败', e))
      }
    } catch (e) {
      console.error('记录连续登录日志失败', e)
    }

    // 缩短等待时间，快速跳转减少白屏感知
    setTimeout(() => {
      console.log('准备跳转到孩子端首页')
      // 使用 reLaunch 并配置过渡动画
      uni.reLaunch({
        url: '/pages/child/home',
        success: () => {
          console.log('跳转到孩子端首页成功')
        },
        fail: (error) => {
          console.error('跳转失败', error)
        }
      })
    }, 600)
  } catch (error) {
    console.error('孩子端登录出错：', error)
    
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

// 切换登录模式
const switchLoginMode = (mode) => {
  console.log('切换登录模式到：', mode)
  loginMode.value = mode
  
  // 清空输入
  if (mode === 'code') {
    username.value = ''
    password.value = ''
  } else {
    phoneNumber.value = ''
    verifyCode.value = ''
  }
}

// 切换密码可见性
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

// 密码登录处理
const handlePasswordLogin = async () => {
  console.log('孩子端：开始密码登录流程')
  
  // 验证输入
  if (!username.value) {
    uni.showToast({
      title: '请输入用户名或手机号',
      icon: 'none'
    })
    return
  }
  
  if (!password.value) {
    uni.showToast({
      title: '请输入密码',
      icon: 'none'
    })
    return
  }

  try {
    console.log('发送密码登录请求，用户名：', username.value)
    
    // 调用后端密码登录接口
    const res = await request.post('/api/user/login', {
      userName: username.value,
      password: password.value,
      terminal: 1,  // 终端类型：1表示移动端
      role: 2  // 标识为孩子端用户
    })
    
    console.log('密码登录响应：', res)
    
    // 保存token和登录状态
    console.log('登录成功，保存token：', res.data)
    uni.setStorageSync('token', res.data.accessToken)
    uni.setStorageSync('isLoggedIn', true)
    uni.setStorageSync('userRole', 'child')
    
    // 显示登录成功提示并跳转
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 800
    })
    
    // 获取并输出连续登录天数
    try {
      if (res.data && res.data.id) {
        const userId = res.data.id
        userBehaviorApi.getConsecutiveReadingDays(userId).then(daysRes => {
          if (daysRes.code === 200) {
            console.log(`登录成功！用户已连续登录 ${daysRes.data} 天`)
          }
        }).catch(e => console.error('获取连续登录天数失败', e))
      }
    } catch (e) {
      console.error('记录连续登录日志失败', e)
    }

    setTimeout(() => {
      console.log('准备跳转到孩子端首页')
      uni.reLaunch({
        url: '/pages/child/home',
        success: () => {
          console.log('跳转到首页成功')
        },
        fail: (error) => {
          console.error('跳转失败', error)
        }
      })
    }, 600)
  } catch (error) {
    console.error('密码登录出错：', error)
    
    let errorMessage = '登录失败，请检查用户名和密码'
    
    if (error.response && error.response.data && error.response.data.message) {
      errorMessage = error.response.data.message
    } else if (error.data && error.data.message) {
       errorMessage = error.data.message
    } else if (error.message) {
       errorMessage = error.message
    }
    
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
  }
}

// 显示邮箱登录弹窗
const showEmailLoginDialog = () => {
  console.log('显示邮箱验证码登录弹窗')
  emailDialogVisible.value = true
}

// 关闭邮箱登录弹窗
const closeEmailDialog = () => {
  console.log('关闭邮箱验证码登录弹窗')
  emailDialogVisible.value = false
  email.value = ''
  emailVerifyCode.value = ''
}

// 验证邮箱格式
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 获取邮箱验证码
const getEmailVerifyCode = async () => {
  console.log('开始获取邮箱验证码流程')
  
  if (!email.value) {
    uni.showToast({
      title: '请输入邮箱',
      icon: 'none'
    })
    return
  }
  
  if (!validateEmail(email.value)) {
    uni.showToast({
      title: '请输入正确的邮箱格式',
      icon: 'none'
    })
    return
  }
  
  // 如果正在倒计时，不允许重复获取
  if (emailCountdown.value > 0) {
    console.log('验证码倒计时中，无法重复获取')
    return
  }

  try {
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
    
    // 2. 人机验证成功后，调用后端接口获取验证码
    console.log('发送获取邮箱验证码请求，邮箱：', email.value, '票据：', captchaResult.ticket)
    
    const res = await request.post('/api/user/send-email-code', {
      email: email.value,
      ticket: captchaResult.ticket,
      randstr: captchaResult.randstr
    })
    
    console.log('获取邮箱验证码响应：', res)
    
    uni.showToast({
      title: '验证码已发送',
      icon: 'success'
    })
    
    // 开始倒计时
    emailCountdown.value = 60
    emailTimer.value = setInterval(() => {
      if (emailCountdown.value > 0) {
        emailCountdown.value--
      } else {
        clearInterval(emailTimer.value)
      }
    }, 1000)
  } catch (error) {
    console.error('获取邮箱验证码出错：', error)
    
    let errorMessage = '获取验证码失败，请稍后重试'
    
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
  }
}

// 邮箱验证码登录
const handleEmailLogin = async () => {
  console.log('开始邮箱验证码登录流程')
  
  if (!email.value) {
    uni.showToast({
      title: '请输入邮箱',
      icon: 'none'
    })
    return
  }
  
  if (!validateEmail(email.value)) {
    uni.showToast({
      title: '请输入正确的邮箱格式',
      icon: 'none'
    })
    return
  }
  
  if (!emailVerifyCode.value) {
    uni.showToast({
      title: '请输入验证码',
      icon: 'none'
    })
    return
  }

  try {
    console.log('发送邮箱登录请求，邮箱：', email.value)
    
    // 调用后端邮箱验证码登录接口
    const res = await request.post('/api/user/login-by-email', {
      email: email.value,
      verificationCode: emailVerifyCode.value,
      terminal: 1,
      role: 2  // 标识为孩子端用户
    })
    
    console.log('邮箱登录响应：', res)
    
    // 保存token和登录状态
    console.log('登录成功，保存token：', res.data)
    uni.setStorageSync('token', res.data.accessToken)
    uni.setStorageSync('isLoggedIn', true)
    uni.setStorageSync('userRole', 'child')
    
    // 获取并输出连续登录天数
    try {
      if (res.data && res.data.id) {
        const userId = res.data.id
        userBehaviorApi.getConsecutiveReadingDays(userId).then(daysRes => {
          if (daysRes.code === 200) {
            console.log(`登录成功！用户已连续登录 ${daysRes.data} 天`)
          }
        }).catch(e => console.error('获取连续登录天数失败', e))
      }
    } catch (e) {
      console.error('记录连续登录日志失败', e)
    }

    // 关闭弹窗
    closeEmailDialog()
    
    // 显示登录成功提示并跳转
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 800
    })
    
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/child/home',
        success: () => {
          console.log('跳转到首页成功')
        },
        fail: (error) => {
          console.error('跳转失败', error)
        }
      })
    }, 600)
  } catch (error) {
    console.error('邮箱登录出错：', error)
    
    let errorMessage = '登录失败，请检查邮箱和验证码'
    
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
  }
}

// 微信登录（暂未实现）
const handleWechatLogin = () => {
  uni.showToast({
    title: '微信登录功能即将上线',
    icon: 'none'
  })
}

// QQ登录（暂未实现）
const handleQQLogin = () => {
  uni.showToast({
    title: 'QQ登录功能即将上线',
    icon: 'none'
  })
}

const goToRegister = () => {
  uni.navigateTo({
    url: '../register'
  })
}

// 跳转到用户协议
const goToUserAgreement = () => {
  console.log('跳转到用户协议页面')
  uni.navigateTo({
    url: '/pages/child/agreement/user-agreement'
  })
}

// 跳转到隐私政策
const goToPrivacyPolicy = () => {
  console.log('跳转到隐私政策页面')
  uni.navigateTo({
    url: '/pages/child/agreement/privacy-policy'
  })
}
</script>

<style scoped>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

/* 禁止页面滚动 */
page {
  overflow: hidden;
  height: 100vh;
}

.login-page {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #e9d5ff, #f3e8ff);
  overflow-y: auto;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  animation: pageSlideIn 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes pageSlideIn {
  0% {
    opacity: 0;
    transform: translateX(100%);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

.login-container {
  min-height: 100vh;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.header {
  text-align: center;
  margin-bottom: 96rpx;
  margin-top: 32rpx;
  animation: headerFadeIn 0.8s ease-out 0.3s backwards;
}

@keyframes headerFadeIn {
  0% {
    opacity: 0;
    transform: translateY(-40rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-box {
  margin: 0 auto 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: logoScale 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) 0.4s backwards;
}

@keyframes logoScale {
  0% {
    opacity: 0;
    transform: scale(0.3) rotate(-10deg);
  }
  100% {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

.logo-img {
  width: 140rpx;
  height: 140rpx;
}

.title {
  font-size: 44rpx;
  font-weight: 800;
  display: block;
  color: #1f2937;
  letter-spacing: 1rpx;
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
  margin: 0 auto;
  width: 85%;
  max-width: 600rpx;
  animation: formSlideUp 0.8s ease-out 0.5s backwards;
}

@keyframes formSlideUp {
  0% {
    opacity: 0;
    transform: translateY(60rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.input-box {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  animation: inputFadeIn 0.6s ease-out backwards;
}

.input-box:nth-child(1) {
  animation-delay: 0.6s;
}

.input-box:nth-child(2) {
  animation-delay: 0.7s;
}

@keyframes inputFadeIn {
  0% {
    opacity: 0;
    transform: translateX(-30rpx);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

.input-box:hover {
  box-shadow: 0 8rpx 30rpx rgba(99, 102, 241, 0.1);
  transform: translateY(-2rpx);
}

.phone-input, .verify-input, .username-input, .password-input, .email-input {
  display: flex;
  align-items: center;
  min-height: 48rpx;
}

.input-icon {
  color: #9CA3AF;
  font-size: 32rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.toggle-password {
  margin-right: 0;
  margin-left: 16rpx;
  cursor: pointer;
}

.username-field, .password-field, .email-field {
  flex: 1;
  font-size: 28rpx;
  min-width: 0;
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
  color: rgb(99, 102, 241);
  cursor: pointer;
}

.verify-btn-disabled {
  color: #9CA3AF;
  cursor: not-allowed;
}

.login-btn {
  background: linear-gradient(135deg, rgb(99, 102, 241) 0%, rgb(139, 92, 246) 100%);
  color: #FFFFFF;
  padding: 28rpx;
  border-radius: 100rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  width: 100%;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.3);
  transition: all 0.3s ease;
  margin-top: 16rpx;
  position: relative;
  overflow: hidden;
  animation: buttonFadeIn 0.6s ease-out 0.8s backwards;
}

@keyframes buttonFadeIn {
  0% {
    opacity: 0;
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.login-btn:hover {
  box-shadow: 0 12rpx 32rpx rgba(99, 102, 241, 0.4);
  transform: translateY(-4rpx);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.2);
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

.switch-mode {
  text-align: center;
  margin-top: 16rpx;
  animation: fadeIn 0.6s ease-out 0.9s backwards;
}

.switch-text {
  color: rgb(99, 102, 241);
  font-size: 28rpx;
  text-decoration: underline;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.switch-text:hover {
  opacity: 0.7;
}

.register-entry {
  text-align: center;
  margin-top: 32rpx;
  animation: fadeIn 0.6s ease-out 1s backwards;
}

.register-text {
  color: rgb(99, 102, 241);
  font-size: 28rpx;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.register-text:hover {
  opacity: 0.7;
}

.other-login {
  text-align: center;
  margin-top: 64rpx;
  width: 85%;
  max-width: 600rpx;
  margin-left: auto;
  margin-right: auto;
  animation: fadeIn 0.8s ease-out 1.1s backwards;
}

@keyframes fadeIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
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
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  animation: socialBtnPop 0.5s ease-out backwards;
}

.social-btn:nth-child(1) {
  animation-delay: 1.2s;
}

.social-btn:nth-child(2) {
  animation-delay: 1.3s;
}

.social-btn:nth-child(3) {
  animation-delay: 1.4s;
}

@keyframes socialBtnPop {
  0% {
    opacity: 0;
    transform: scale(0) rotate(-180deg);
  }
  70% {
    transform: scale(1.1) rotate(10deg);
  }
  100% {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

.social-btn:hover {
  transform: translateY(-8rpx) scale(1.1);
  box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.15);
}

.social-btn:active {
  transform: translateY(-4rpx) scale(1.05);
}

.social-btn .fa-weixin {
  color: #07C160;
  font-size: 40rpx;
}

.social-btn .fa-qq {
  color: rgb(99, 102, 241);
  font-size: 40rpx;
}

.social-btn .fa-envelope {
  color: #EF4444;
  font-size: 40rpx;
}

.footer {
  margin-top: auto;
  text-align: center;
  padding: 32rpx 0;
  animation: fadeIn 0.8s ease-out 1.5s backwards;
}

.agreement {
  color: #9CA3AF;
  font-size: 24rpx;
  line-height: 1.6;
}

.link {
  color: rgb(99, 102, 241);
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.link:hover {
  opacity: 0.7;
  text-decoration: underline;
}

/* 移除输入框默认样式 */
input {
  outline: none;
  border: none;
}

/* 邮箱验证码登录弹窗 */
.email-dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.email-dialog {
  width: 85%;
  max-width: 600rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.dialog-header {
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #E5E7EB;
}

.dialog-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1F2937;
}

.close-btn {
  font-size: 40rpx;
  color: #9CA3AF;
  cursor: pointer;
}

.dialog-body {
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.dialog-footer {
  padding: 24rpx 32rpx;
  display: flex;
  gap: 24rpx;
  border-top: 1rpx solid #E5E7EB;
}

.dialog-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 100rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  padding: 0;
}

.cancel-btn {
  background: #F3F4F6;
  color: #6B7280;
}

.confirm-btn {
  background: linear-gradient(135deg, rgb(99, 102, 241) 0%, rgb(139, 92, 246) 100%);
  color: #FFFFFF;
  box-shadow: 0 8rpx 16rpx rgba(99, 102, 241, 0.2);
}

.confirm-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 8rpx rgba(99, 102, 241, 0.15);
}
</style>

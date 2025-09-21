<template>
  <view class="register-page">
    <!-- 顶部导航栏 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">创建账号</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 头像上传 -->
      <view class="avatar-section" @click="chooseAvatar">
        <view class="avatar-container">
          <image class="avatar-img" :src="avatarUrl || defaultAvatarUrl" mode="aspectFill"></image>
          <view class="camera-btn">
            <text class="fas fa-camera"></text>
          </view>
        </view>
        <text class="avatar-tip">{{ avatarUrl ? '点击更换头像' : '点击上传头像' }}</text>
      </view>

      <!-- 注册表单 -->
      <view class="form-container">
        <view class="input-box">
          <input type="text" placeholder="请输入昵称" class="input-field" v-model="nickname" />
        </view>

        <view class="input-box">
          <view class="phone-input">
            <text class="country-code">+86</text>
            <text class="divider">|</text>
            <input type="number" placeholder="请输入手机号" class="input-field" v-model="phoneNumber" />
          </view>
        </view>

        <view class="input-box">
          <view class="verify-input">
            <input type="text" placeholder="请输入验证码" class="input-field" v-model="verifyCode" />
            <text 
              class="verify-btn" 
              :class="{ 'verify-btn-active': phoneNumber.length === 11 && countdown === 0, 'verify-btn-disabled': countdown > 0 }"
              @click="getVerifyCode"
            >
              {{ countdown > 0 ? `${countdown}s后重新获取` : '获取验证码' }}
            </text>
          </view>
        </view>

        <view class="input-box">
          <view class="password-input">
            <input :type="showPassword ? 'text' : 'password'" placeholder="请设置密码（不少于8位）" class="input-field" v-model="password" />
            <text class="eye-btn" @click="togglePassword">
              <text :class="['far', showPassword ? 'fa-eye-slash' : 'fa-eye']"></text>
            </text>
          </view>
        </view>

        <!-- 兴趣选择 -->
        <view class="interest-section">
          <text class="interest-title">选择你感兴趣的内容（可多选）</text>
          <view class="interest-tags">
            <text 
              v-for="(tag, index) in interests" 
              :key="index" 
              class="interest-tag"
              :class="{ 'active': tag.active }"
              @click="toggleInterest(index)"
            >{{ tag.name }}</text>
          </view>
        </view>

        <button class="register-btn" @click="handleRegister">完成注册</button>
      </view>

      <!-- 底部协议 -->
      <view class="footer">
        <text class="agreement">
          注册即表示同意
          <text class="link">用户协议</text>
          和
          <text class="link">隐私政策</text>
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'

// 表单数据
const nickname = ref('')
const phoneNumber = ref('')
const verifyCode = ref('')
const password = ref('')
const showPassword = ref(false)
const countdown = ref(0)
const timer = ref(null)

// 头像相关
const defaultAvatarUrl = 'http://127.0.0.1:9000/qz-sns/image/register/avatar/default-avatar.jpg'
const avatarUrl = ref('')
const avatarFile = ref(null)

// 兴趣标签
const interests = ref([
  { name: '文学', active: false },
  { name: '科普', active: false },
  { name: '历史', active: false },
  { name: '艺术', active: false },
  { name: '哲学', active: false },
  { name: '心理学', active: false }
])

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const toggleInterest = (index) => {
  interests.value[index].active = !interests.value[index].active
  
  // 使用JSON.stringify来打印更清晰的状态
  const currentTag = interests.value[index]
  console.log(`切换兴趣标签：${JSON.stringify({
    name: currentTag.name,
    active: currentTag.active
  }, null, 2)}`)
  
  // 打印所有标签的当前状态
  console.log('当前所有兴趣标签状态：', JSON.stringify(
    interests.value.map(tag => ({
      name: tag.name,
      active: tag.active
    })),
    null, 2
  ))
}

// 验证手机号格式
const validatePhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

// 验证密码格式
const validatePassword = (pwd) => {
  return pwd.length >= 8
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
    uni.showToast({
      title: '获取验证码失败，请稍后重试',
      icon: 'none'
    })
  }
}

// 注册处理
const handleRegister = async () => {
  console.log('开始注册流程')
  
  // 验证输入
  if (!nickname.value) {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    })
    return
  }
  
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
  
  if (!password.value) {
    uni.showToast({
      title: '请设置密码',
      icon: 'none'
    })
    return
  }
  
  if (!validatePassword(password.value)) {
    uni.showToast({
      title: '密码长度不能少于8位',
      icon: 'none'
    })
    return
  }

   try {
     console.log('发送注册请求')
     
     // 检查头像是否已上传
     if (!avatarUrl.value) {
       uni.showToast({
         title: '请上传头像',
         icon: 'none'
       })
       return
     }
     
     // 获取选中的兴趣标签
     console.log('注册前所有兴趣标签状态：', JSON.stringify(
       interests.value.map(tag => ({
         name: tag.name,
         active: tag.active
       })),
       null, 2
     ))
     
     // 过滤出选中的标签
     const activeInterests = interests.value.filter(tag => tag.active)
     console.log('选中的标签：', JSON.stringify(
       activeInterests.map(tag => ({
         name: tag.name,
         active: tag.active
       })),
       null, 2
     ))
     
     // 提取标签名称
     const selectedInterests = activeInterests.map(tag => tag.name)
     console.log('选中的标签名称：', selectedInterests)
     
     // 检查是否至少选择了一个兴趣标签
     if (selectedInterests.length === 0) {
       uni.showToast({
         title: '请至少选择一个兴趣标签',
         icon: 'none'
       })
       return
     }
     
     // 将选中的标签转换为字符串
     const interestsString = selectedInterests.join(',')
     console.log('最终的兴趣标签字符串：', interestsString)
     
     // 生成用户名（使用昵称+随机数）
     const username = `user_${Math.floor(Math.random() * 10000)}`;
     
     // 构建请求数据
     const requestData = {
       username: username,
       nickname: nickname.value,
       phone: phoneNumber.value,
       verificationCode: verifyCode.value,
       password: password.value,
       avatar: avatarUrl.value, // 使用上传后的头像URL
       role: 1, // 家长角色
       status: 1, // 正常状态
       interests: interestsString // 将兴趣爱好数组转为逗号分隔的字符串
     }
     
     // 打印请求数据
     console.log('发送注册请求数据：', JSON.stringify(requestData, null, 2))
     
     // 调用后端注册接口
     const res = await request.post('/api/user/register-by-phone', requestData)
    
    console.log('注册响应：', res)
    
    // 保存token
    uni.setStorageSync('token', res.data.token)
    
    // 显示注册成功提示
    uni.showToast({
      title: '注册成功',
      icon: 'success'
    })
    
    // 跳转到首页
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/parent/home/home'
      })
    }, 1500)
  } catch (error) {
    console.error('注册出错：', error)
    uni.showToast({
      title: '注册失败，请稍后重试',
      icon: 'none'
    })
  }
}

// 选择头像
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      console.log('选择头像成功：', res)
      const tempFilePath = res.tempFilePaths[0]
      
      try {
        console.log('开始上传头像')
        
        // 上传文件到MinIO
        const uploadRes = await new Promise((resolve, reject) => {
          uni.uploadFile({
            url: 'http://127.0.0.1:8888/api/file/upload',
            filePath: tempFilePath,
            name: 'file',
            formData: {
              type: 'image',
              path: 'register/avatar' // 指定上传到的子目录
            },
            success: (res) => {
              if (res.statusCode === 200) {
                try {
                  const data = JSON.parse(res.data)
                  if (data.code === 200) {
                    resolve(data)
                  } else {
                    reject(new Error(data.message || '上传失败'))
                  }
                } catch (e) {
                  reject(new Error('解析响应失败'))
                }
              } else {
                reject(new Error('上传失败'))
              }
            },
            fail: (err) => {
              reject(err)
            }
          })
        })
        
        console.log('头像上传响应：', uploadRes)
        
        avatarUrl.value = uploadRes.data.originUrl
        uni.showToast({
          title: '头像上传成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('头像上传失败：', error)
        uni.showToast({
          title: '头像上传失败',
          icon: 'none'
        })
      }
    },
    fail: (err) => {
      console.error('选择头像失败：', err)
      uni.showToast({
        title: '选择头像失败',
        icon: 'none'
      })
    }
  })
}

const goBack = () => {
  uni.navigateBack({
    delta: 1
  })
}
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.register-page {
  min-height: 100vh;
  background-color: #F9FAFB;
}

.nav-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: #FFFFFF;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  z-index: 50;
}

.nav-content {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
}

.back-btn {
  font-size: 36rpx;
  color: #666;
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  margin-left: 16rpx;
}

.main-content {
  margin-top: 128rpx;
  padding: 32rpx;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 64rpx;
}

.avatar-container {
  position: relative;
  width: 192rpx;
  height: 192rpx;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 999px;
}

.camera-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 64rpx;
  height: 64rpx;
  background: rgb(59, 130, 246);
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 6rpx rgba(0, 0, 0, 0.1);
}

.camera-btn .fas {
  color: #FFFFFF;
  font-size: 24rpx;
}

.avatar-tip {
  font-size: 24rpx;
  color: #6B7280;
  margin-top: 16rpx;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.input-box {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
}

.input-field {
  width: 100%;
  font-size: 28rpx;
}

.phone-input, .verify-input, .password-input {
  display: flex;
  align-items: center;
}

.country-code {
  color: #6B7280;
  font-size: 28rpx;
}

.divider {
  color: #E5E7EB;
  margin: 0 16rpx;
}

.verify-btn {
  color: #9CA3AF;
  font-size: 28rpx;
  padding-left: 16rpx;
  white-space: nowrap;
  flex-shrink: 0;
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

.eye-btn {
  color: #9CA3AF;
  padding-left: 16rpx;
}

.interest-section {
  margin-top: 48rpx;
}

.interest-title {
  font-size: 24rpx;
  color: #6B7280;
  margin-bottom: 24rpx;
}

.interest-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.interest-tag {
  padding: 16rpx 32rpx;
  background: #F3F4F6;
  color: #6B7280;
  border-radius: 999px;
  font-size: 24rpx;
}

.interest-tag.active {
  background: #EBF5FF;
  color: rgb(59, 130, 246);
}

.register-btn {
  background: linear-gradient(135deg, rgb(59, 130, 246) 0%, rgb(96, 165, 250) 100%);
  color: #FFFFFF;
  padding: 28rpx;
  border-radius: 100rpx;
  font-size: 32rpx;
  font-weight: 600;
  margin-top: 64rpx;
  width: 100%;
  border: none;
  box-shadow: 0 8rpx 16rpx rgba(59, 130, 246, 0.2);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 8rpx rgba(59, 130, 246, 0.15);
}

/* 添加按钮点击涟漪效果 */
.register-btn::after {
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

.register-btn:active::after {
  transform: scale(0, 0);
  opacity: .3;
  transition: 0s;
}

.footer {
  text-align: center;
  margin-top: 48rpx;
}

.agreement {
  font-size: 24rpx;
  color: #9CA3AF;
}

.link {
  color: rgb(59, 130, 246);
}
</style> 
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
      <view class="avatar-section">
        <view class="avatar-container">
          <image class="avatar-img" src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500&auto=format&fit=crop&q=60" mode="aspectFill"></image>
          <view class="camera-btn">
            <text class="fas fa-camera"></text>
          </view>
        </view>
        <text class="avatar-tip">点击更换头像</text>
      </view>

      <!-- 注册表单 -->
      <view class="form-container">
        <view class="input-box">
          <input type="text" placeholder="请输入昵称" class="input-field" />
        </view>

        <view class="input-box">
          <view class="phone-input">
            <text class="country-code">+86</text>
            <text class="divider">|</text>
            <input type="number" placeholder="请输入手机号" class="input-field" />
          </view>
        </view>

        <view class="input-box">
          <view class="verify-input">
            <input type="text" placeholder="请输入验证码" class="input-field" />
            <text class="verify-btn" @click="getVerifyCode">获取验证码</text>
          </view>
        </view>

        <view class="input-box">
          <view class="password-input">
            <input :type="showPassword ? 'text' : 'password'" placeholder="请设置密码（不少于8位）" class="input-field" />
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

const showPassword = ref(false)
const interests = ref([
  { name: '文学', active: true },
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
}

const getVerifyCode = () => {
  // TODO: 实现获取验证码逻辑
  console.log('获取验证码')
}

const handleRegister = () => {
  // TODO: 实现注册逻辑
  console.log('注册')
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
  color: rgb(59, 130, 246);
  font-size: 28rpx;
  padding-left: 16rpx;
  white-space: nowrap;
  flex-shrink: 0;
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
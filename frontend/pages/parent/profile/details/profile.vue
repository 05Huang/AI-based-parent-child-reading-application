# 创建新的个人资料页面
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">个人资料</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 头像设置 -->
      <view class="profile-item" @click="handleAvatarClick">
        <text class="item-label">头像</text>
        <view class="item-right">
          <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>
      </view>

      <!-- 昵称设置 -->
      <view class="profile-item" @click="handleNicknameClick">
        <text class="item-label">昵称</text>
        <view class="item-right">
          <text class="item-value">{{userInfo.nickname || '未设置'}}</text>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>
      </view>

      <!-- 性别设置 -->
      <view class="profile-item" @click="handleGenderClick">
        <text class="item-label">性别</text>
        <view class="item-right">
          <text class="item-value">{{genderText}}</text>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>
      </view>

      <!-- 生日设置 -->
      <view class="profile-item" @click="handleBirthdayClick">
        <text class="item-label">生日</text>
        <view class="item-right">
          <text class="item-value">{{userInfo.birthday || '未设置'}}</text>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>
      </view>

      <!-- 地区设置 -->
      <view class="profile-item" @click="handleRegionClick">
        <text class="item-label">地区</text>
        <view class="item-right">
          <text class="item-value">{{userInfo.region || '未设置'}}</text>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>
      </view>

      <!-- 个性签名 -->
      <view class="profile-item" @click="handleSignatureClick">
        <text class="item-label">个性签名</text>
        <view class="item-right">
          <text class="item-value signature">{{userInfo.signature || '未设置'}}</text>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 用户信息
const userInfo = ref({
  avatar: '',
  nickname: '阅读达人',
  gender: 0, // 0: 未设置, 1: 男, 2: 女
  birthday: '',
  region: '',
  signature: ''
})

// 性别显示文本
const genderText = computed(() => {
  const genderMap = {
    0: '未设置',
    1: '男',
    2: '女'
  }
  return genderMap[userInfo.value.gender]
})

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 处理头像点击
const handleAvatarClick = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // 这里应该先上传图片到服务器，获取URL后再更新
      userInfo.value.avatar = res.tempFilePaths[0]
    }
  })
}

// 处理昵称点击
const handleNicknameClick = () => {
  uni.showModal({
    title: '修改昵称',
    editable: true,
    placeholderText: '请输入昵称',
    content: userInfo.value.nickname,
    success: (res) => {
      if (res.confirm && res.content) {
        userInfo.value.nickname = res.content
      }
    }
  })
}

// 处理性别点击
const handleGenderClick = () => {
  uni.showActionSheet({
    itemList: ['男', '女'],
    success: (res) => {
      userInfo.value.gender = res.tapIndex + 1
    }
  })
}

// 处理生日点击
const handleBirthdayClick = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 处理地区点击
const handleRegionClick = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 处理个性签名点击
const handleSignatureClick = () => {
  uni.showModal({
    title: '修改个性签名',
    editable: true,
    placeholderText: '请输入个性签名',
    content: userInfo.value.signature,
    success: (res) => {
      if (res.confirm) {
        userInfo.value.signature = res.content
      }
    }
  })
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

/* 个人资料项样式 */
.profile-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 20rpx;
  background-color: #ffffff;
  margin-bottom: 2rpx;
}

.profile-item:first-child {
  border-top-left-radius: 16rpx;
  border-top-right-radius: 16rpx;
}

.profile-item:last-child {
  border-bottom-left-radius: 16rpx;
  border-bottom-right-radius: 16rpx;
  margin-bottom: 0;
}

.item-label {
  font-size: 30rpx;
  color: #111827;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
}

.item-value {
  font-size: 28rpx;
  color: #6b7280;
  max-width: 400rpx;
  text-align: right;
}

.signature {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
<template>
  <view class="container profile-container">
    <view class="user-info">
      <image class="avatar" src="/static/default-avatar.png" mode="aspectFill"></image>
      <view class="user-details">
        <text class="username">小朋友昵称</text>
      </view>
    </view>

    <view class="menu-list">
      <view class="menu-item">
        <text class="menu-title">我的成就</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item">
        <text class="menu-title">阅读记录</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item">
        <text class="menu-title">个人设置</text>
        <text class="menu-arrow">></text>
      </view>
    </view>

    <button class="logout-btn" type="warn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup>
const handleLogout = () => {
  // TODO: 实现退出登录逻辑
  console.log('孩子退出登录')
}
</script>

<style>
.profile-container {
  padding: 20rpx;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 40rpx 20rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-right: 30rpx;
}

.username {
  font-size: 36rpx;
  font-weight: bold;
}

.menu-list {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1rpx solid #eee;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-title {
  font-size: 30rpx;
}

.menu-arrow {
  color: #999;
}

.logout-btn {
  margin-top: 60rpx;
}
</style> 
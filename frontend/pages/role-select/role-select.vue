<template>
  <view class="role-select">
    <view class="title">
      <text class="main-title">亲子阅读</text>
      <text class="sub-title">请选择您的身份</text>
    </view>
    
    <view class="role-cards">
      <!-- 家长端卡片 -->
      <view class="role-card parent-card" @click="navigateToParent">
        <view class="card-bg">
          <view class="icon-wrapper">
            <image class="role-icon" src="/static/images/icons/parent/家长.svg" mode="aspectFit"></image>
          </view>
        </view>
        <view class="card-content">
          <text class="role-name">家长端</text>
          <text class="role-desc">监督阅读进度，设置目标</text>
          <button class="role-btn">进入家长端</button>
        </view>
      </view>

      <!-- 孩子端卡片 -->
      <view class="role-card child-card" @click="navigateToChild">
        <view class="card-bg">
          <view class="icon-wrapper">
            <image class="role-icon" src="/static/images/icons/child/孩子.svg" mode="aspectFit"></image>
          </view>
        </view>
        <view class="card-content">
          <text class="role-name">孩子端</text>
          <text class="role-desc">阅读故事，获得成就</text>
          <button class="role-btn">进入孩子端</button>
        </view>
      </view>
    </view>

    <view class="footer">
      <text class="copyright">© 2024 亲子阅读应用</text>
    </view>
  </view>
</template>

<script setup>
const navigateToParent = () => {
  uni.navigateTo({
    url: '/pages/parent/login/login'
  })
}

const navigateToChild = () => {
  uni.navigateTo({
    url: '/pages/child/login/login'
  })
}
</script>

<style>
.role-select {
  min-height: 100vh;
  background: #EBF5FF;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
  padding: 0 40rpx;
}

.title {
  text-align: center;
  margin: 10vh 0 8vh;
}

.main-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.sub-title {
  font-size: 32rpx;
  color: #666;
}

.role-cards {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.role-card {
  width: 100%;
  border-radius: 24rpx;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.role-card:hover {
  transform: translateY(-8rpx);
  box-shadow: 0 12rpx 24rpx rgba(0, 0, 0, 0.1);
}

.role-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.08);
}

.parent-card .card-bg {
  background: rgb(36, 89, 223);
}

.child-card .card-bg {
  background: #7C3AED;
}

.card-bg {
  height: 240rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  width: 120rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.role-icon {
  width: 60rpx;
  height: 60rpx;
  /* 将 SVG 图标设置为白色 */
  filter: brightness(0) invert(1);
}

.icon-placeholder {
  width: 60rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.5);
}

.card-content {
  background: #FFFFFF;
  padding: 40rpx;
  text-align: center;
}

.role-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.role-desc {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 30rpx;
}

.role-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 8rpx;
  font-size: 32rpx;
  color: #FFFFFF;
  border: none;
}

.parent-card .role-btn {
  background: rgb(36, 89, 223);
}

.child-card .role-btn {
  background: #7C3AED;
}

.footer {
  margin-top: auto;
  padding: 40rpx 0;
}

.copyright {
  font-size: 24rpx;
  color: #999;
}

/* 响应式调整 */
@media screen and (min-height: 700px) {
  .title {
    margin: 15vh 0 10vh;
  }
}

@media screen and (max-height: 600px) {
  .title {
    margin: 5vh 0 4vh;
  }
  
  .card-bg {
    height: 200rpx;
  }
  
  .icon-wrapper {
    width: 100rpx;
    height: 100rpx;
  }
  
  .card-content {
    padding: 30rpx;
  }
}
</style> 
<template>
  <view class="role-select">
    <view class="container">
      <view class="title">
        <text class="main-title">亲子阅读</text>
        <text class="sub-title">请选择您的身份</text>
      </view>
      
      <view class="role-cards">
        <!-- 家长端卡片 -->
        <view class="role-card parent-card" @click="navigateToParent">
          <view class="card-bg parent-gradient">
            <view class="icon-wrapper">
              <text class="fas fa-user-tie icon-font"></text>
            </view>
          </view>
          <view class="card-content">
            <text class="role-name">家长端</text>
            <text class="role-desc">监督阅读进度，设置目标</text>
            <button class="role-btn parent-btn">进入家长端</button>
          </view>
        </view>

        <!-- 孩子端卡片 -->
        <view class="role-card child-card" @click="navigateToChild">
          <view class="card-bg child-gradient">
            <view class="icon-wrapper">
              <text class="fas fa-child icon-font"></text>
            </view>
          </view>
          <view class="card-content">
            <text class="role-name">孩子端</text>
            <text class="role-desc">阅读故事，获得成就</text>
            <button class="role-btn child-btn">进入孩子端</button>
          </view>
        </view>
      </view>

      <view class="footer">
        <text class="copyright">© 2025 江苏师范大学智慧教育学院-亲子阅读应用</text>
      </view>
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

<style scoped>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

/* 禁止页面滚动 */
page {
  overflow: hidden;
  height: 100vh;
}

.role-select {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.container {
  width: 100%;
  max-width: 1200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 32rpx;
  box-sizing: border-box;
}

.title {
  text-align: center;
  margin-bottom: 80rpx;
}

.main-title {
  font-size: 60rpx;
  font-weight: bold;
  color: #1f2937;
  display: block;
  margin-bottom: 24rpx;
}

.sub-title {
  font-size: 32rpx;
  color: #6b7280;
  display: block;
}

.role-cards {
  width: 100%;
  max-width: 1000rpx;
  display: flex;
  flex-direction: row;
  gap: 48rpx;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 60rpx;
}

.role-card {
  width: 512rpx;
  max-width: 100%;
  border-radius: 32rpx;
  overflow: hidden;
  background: #FFFFFF;
  box-shadow: 0 20rpx 50rpx -10rpx rgba(0, 0, 0, 0.1), 0 16rpx 20rpx -10rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.role-card:hover {
  transform: translateY(-20rpx);
  box-shadow: 0 40rpx 50rpx -10rpx rgba(0, 0, 0, 0.1), 0 20rpx 20rpx -10rpx rgba(0, 0, 0, 0.04);
}

.role-card:active {
  transform: translateY(-10rpx);
}

.card-bg {
  padding: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.parent-gradient {
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
}

.child-gradient {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.icon-wrapper {
  width: 128rpx;
  height: 128rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-font {
  font-family: 'Font Awesome 6 Free', 'FontAwesome';
  font-weight: 900;
  font-size: 48rpx;
  color: #FFFFFF;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.card-content {
  background: #FFFFFF;
  padding: 32rpx;
  text-align: center;
}

.role-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #1f2937;
  display: block;
  margin-bottom: 8rpx;
}

.role-desc {
  font-size: 28rpx;
  color: #6b7280;
  display: block;
  margin-bottom: 24rpx;
}

.role-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 9999rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #FFFFFF;
  border: none;
  padding: 0;
}

.parent-btn {
  background: #2563eb;
}

.child-btn {
  background: #6366f1;
}

.footer {
  text-align: center;
}

.copyright {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 移动端适配 - 小屏幕垂直排列 */
@media screen and (max-width: 750px) {
  .role-cards {
    flex-direction: column;
    gap: 32rpx;
  }
  
  .role-card {
    width: 100%;
    max-width: 512rpx;
  }
  
  .title {
    margin-bottom: 60rpx;
  }
  
  .main-title {
    font-size: 52rpx;
  }
  
  .sub-title {
    font-size: 28rpx;
  }
}

/* 超小屏幕优化 */
@media screen and (max-height: 800px) {
  .title {
    margin-bottom: 40rpx;
  }
  
  .main-title {
    font-size: 48rpx;
    margin-bottom: 16rpx;
  }
  
  .role-cards {
    margin-bottom: 40rpx;
  }
  
  .card-bg {
    padding: 24rpx;
  }
  
  .icon-wrapper {
    width: 100rpx;
    height: 100rpx;
  }
  
  .icon-font {
    font-size: 40rpx;
  }
  
  .card-content {
    padding: 24rpx;
  }
}
</style> 
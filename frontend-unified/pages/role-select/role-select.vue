<template>
  <view class="role-select">
    <view class="container">
      <view class="title">
        <text class="main-title">阅桥亲子阅读APP</text>
        <text class="sub-title">请选择您的身份</text>
      </view>
      
      <view class="role-cards">
        <!-- 家长端卡片 -->
        <view 
          class="role-card parent-card" 
          :class="{ 'card-clicked': clickedCard === 'parent', 'card-fade': clickedCard && clickedCard !== 'parent' }"
          @click="navigateToParent"
        >
          <view class="card-bg parent-gradient">
            <view class="icon-wrapper" :class="{ 'icon-pulse': clickedCard === 'parent' }">
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
        <view 
          class="role-card child-card" 
          :class="{ 'card-clicked': clickedCard === 'child', 'card-fade': clickedCard && clickedCard !== 'child' }"
          @click="navigateToChild"
        >
          <view class="card-bg child-gradient">
            <view class="icon-wrapper" :class="{ 'icon-pulse': clickedCard === 'child' }">
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
        <text class="copyright">© 2025 江苏师范大学智慧教育学院-阅桥亲子阅读APP应用</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const clickedCard = ref('')

const navigateToParent = () => {
  console.log('点击家长端卡片')
  clickedCard.value = 'parent'
  
  // 延迟跳转，让动画播放完成
  setTimeout(() => {
    console.log('跳转到家长登录页面')
    uni.navigateTo({
      url: '/pages/parent/login/login'
    })
    // 重置状态
    setTimeout(() => {
      clickedCard.value = ''
    }, 300)
  }, 600)
}

const navigateToChild = () => {
  console.log('点击孩子端卡片')
  clickedCard.value = 'child'
  
  // 延迟跳转，让动画播放完成
  setTimeout(() => {
    console.log('跳转到孩子登录页面')
    uni.navigateTo({
      url: '/pages/child/login/login'
    })
    // 重置状态
    setTimeout(() => {
      clickedCard.value = ''
    }, 300)
  }, 600)
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
  min-height: 100vh;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  box-sizing: border-box;
}

.container {
  width: 100%;
  max-width: 1400rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 60rpx;
  box-sizing: border-box;
}

.title {
  text-align: center;
  margin-bottom: 120rpx;
  animation: titleFadeIn 0.8s ease-out;
}

@keyframes titleFadeIn {
  0% {
    opacity: 0;
    transform: translateY(-30rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.main-title {
  font-size: 72rpx;
  font-weight: 800;
  color: #1f2937;
  display: block;
  margin-bottom: 20rpx;
  letter-spacing: 2rpx;
}

.sub-title {
  font-size: 32rpx;
  color: #6b7280;
  display: block;
  font-weight: 400;
}

.role-cards {
  width: 100%;
  max-width: 1200rpx;
  display: flex;
  flex-direction: row;
  gap: 60rpx;
  justify-content: center;
  align-items: stretch;
  margin-bottom: 100rpx;
}

.role-card {
  flex: 1;
  max-width: 480rpx;
  min-width: 380rpx;
  border-radius: 24rpx;
  overflow: hidden;
  background: #FFFFFF;
  box-shadow: 0 20rpx 60rpx -10rpx rgba(0, 0, 0, 0.1), 0 10rpx 30rpx -10rpx rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  animation: cardFadeInUp 0.6s ease-out backwards;
}

.parent-card {
  animation-delay: 0.2s;
}

.child-card {
  animation-delay: 0.3s;
}

@keyframes cardFadeInUp {
  0% {
    opacity: 0;
    transform: translateY(40rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.role-card:hover {
  transform: translateY(-20rpx) scale(1.02);
  box-shadow: 0 40rpx 60rpx -10rpx rgba(0, 0, 0, 0.15), 0 20rpx 30rpx -10rpx rgba(0, 0, 0, 0.08);
}

.role-card:active {
  transform: translateY(-10rpx) scale(0.98);
}

/* 点击动画 - 卡片放大并居中 */
.card-clicked {
  animation: cardZoomIn 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  z-index: 100;
}

/* 未选中的卡片淡出 */
.card-fade {
  animation: cardFadeOut 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes cardZoomIn {
  0% {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  50% {
    transform: translateY(-40rpx) scale(1.1);
    opacity: 1;
  }
  100% {
    transform: translateY(-40rpx) scale(1.15);
    opacity: 0;
  }
}

@keyframes cardFadeOut {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(0.9);
  }
}

.card-bg {
  padding: 60rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.parent-gradient {
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
}

.child-gradient {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.icon-wrapper {
  width: 140rpx;
  height: 140rpx;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10rpx);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

/* 图标脉冲动画 */
.icon-pulse {
  animation: iconPulse 0.6s ease-in-out;
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
    background: rgba(255, 255, 255, 0.2);
  }
  25% {
    transform: scale(1.2);
    background: rgba(255, 255, 255, 0.4);
  }
  50% {
    transform: scale(0.9);
    background: rgba(255, 255, 255, 0.3);
  }
  75% {
    transform: scale(1.1);
    background: rgba(255, 255, 255, 0.5);
  }
}

.icon-font {
  font-family: 'Font Awesome 6 Free', 'FontAwesome';
  font-weight: 900;
  font-size: 56rpx;
  color: #FFFFFF;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  transition: transform 0.3s ease;
}

.card-content {
  background: #FFFFFF;
  padding: 48rpx 40rpx;
  text-align: center;
}

.role-name {
  font-size: 44rpx;
  font-weight: 700;
  color: #1f2937;
  display: block;
  margin-bottom: 12rpx;
  letter-spacing: 1rpx;
}

.role-desc {
  font-size: 28rpx;
  color: #6b7280;
  display: block;
  margin-bottom: 36rpx;
  line-height: 1.6;
}

.role-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #FFFFFF;
  border: none;
  padding: 0;
  transition: all 0.3s ease;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.12);
}

.role-btn:active {
  transform: scale(0.96);
  box-shadow: 0 3rpx 10rpx rgba(0, 0, 0, 0.15);
}

.parent-btn {
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
}

.child-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.footer {
  text-align: center;
  animation: footerFadeIn 1s ease-out 0.5s backwards;
}

@keyframes footerFadeIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

.copyright {
  font-size: 24rpx;
  color: #9ca3af;
  letter-spacing: 0.5rpx;
}

/* 移动端适配 - 小屏幕垂直排列 */
@media screen and (max-width: 750px) {
  .role-select {
    padding: 60rpx 0;
  }
  
  .container {
    padding: 0 48rpx;
  }
  
  .role-cards {
    flex-direction: column;
    gap: 40rpx;
    max-width: 600rpx;
  }
  
  .role-card {
    width: 100%;
    min-width: auto;
    max-width: 100%;
  }
  
  .title {
    margin-bottom: 60rpx;
  }
  
  .main-title {
    font-size: 56rpx;
  }
  
  .sub-title {
    font-size: 28rpx;
  }
  
  .card-bg {
    padding: 48rpx 32rpx;
  }
  
  .icon-wrapper {
    width: 120rpx;
    height: 120rpx;
  }
  
  .icon-font {
    font-size: 48rpx;
  }
  
  .card-content {
    padding: 36rpx 32rpx;
  }
  
  .role-name {
    font-size: 38rpx;
  }
  
  .role-desc {
    font-size: 26rpx;
    margin-bottom: 28rpx;
  }
  
  .role-btn {
    height: 76rpx;
    line-height: 76rpx;
    font-size: 28rpx;
  }
}

/* 超小屏幕优化（高度受限） */
@media screen and (max-height: 800px) {
  .role-select {
    padding: 40rpx 0;
  }
  
  .title {
    margin-bottom: 50rpx;
  }
  
  .main-title {
    font-size: 52rpx;
    margin-bottom: 12rpx;
  }
  
  .sub-title {
    font-size: 26rpx;
  }
  
  .role-cards {
    margin-bottom: 50rpx;
    gap: 32rpx;
  }
  
  .card-bg {
    padding: 40rpx 32rpx;
  }
  
  .icon-wrapper {
    width: 100rpx;
    height: 100rpx;
  }
  
  .icon-font {
    font-size: 42rpx;
  }
  
  .card-content {
    padding: 32rpx 28rpx;
  }
  
  .role-name {
    font-size: 36rpx;
  }
  
  .role-desc {
    font-size: 24rpx;
    margin-bottom: 24rpx;
  }
  
  .role-btn {
    height: 72rpx;
    line-height: 72rpx;
    font-size: 26rpx;
  }
}

/* 超小屏幕且移动端 */
@media screen and (max-width: 750px) and (max-height: 800px) {
  .role-select {
    padding: 30rpx 0;
  }
  
  .container {
    padding: 0 40rpx;
  }
  
  .title {
    margin-bottom: 40rpx;
  }
  
  .main-title {
    font-size: 48rpx;
  }
  
  .role-cards {
    gap: 28rpx;
    max-width: 550rpx;
  }
  
  .card-bg {
    padding: 36rpx 28rpx;
  }
  
  .card-content {
    padding: 28rpx 24rpx;
  }
}
</style> 
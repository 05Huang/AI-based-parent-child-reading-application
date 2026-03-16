<template>
  <view class="tab-bar">
    <view 
      v-for="(item, index) in list" 
      :key="index" 
      class="tab-item"
      :class="{ active: current === index }"
      @click="switchTab(item.pagePath, index)"
    >
      <view class="tab-icon">
        <image :src="getIconPath(index)" mode="aspectFit" style="width: 48rpx; height: 48rpx;"></image>
      </view>
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const current = ref(0)

const list = [
  { pagePath: '/pages/parent/home/home', text: '首页' },
  { pagePath: '/pages/parent/bookshelf/bookshelf', text: '阅读' },
  { pagePath: '/pages/parent/video/video', text: '视频' },
  { pagePath: '/pages/parent/ai-chat/ai-chat', text: 'AI对话' },
  { pagePath: '/pages/parent/profile/profile', text: '我的' }
]

const getIconPath = (index) => {
  const icons = [
    '/static/images/tabbar/home.png',
    '/static/images/tabbar/book.png',
    '/static/images/tabbar/video.png',
    '/static/images/tabbar/chat.png',
    '/static/images/tabbar/user.png'
  ]
  return icons[index]
}

const switchTab = (url, index) => {
  current.value = index
  uni.switchTab({
    url,
    fail: (err) => {
      console.error('切换页面失败：', err)
      // 如果switchTab失败，尝试使用navigateTo
      uni.navigateTo({
        url,
        fail: (navErr) => {
          console.error('导航失败：', navErr)
        }
      })
    }
  })
}

// 监听页面显示
uni.onShow(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const route = '/' + page.route
  const index = list.findIndex(item => item.pagePath === route)
  if (index !== -1) {
    current.value = index
  }
})
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background-color: #ffffff;
  display: flex;
  padding-bottom: env(safe-area-inset-bottom);
  border-top: 1rpx solid #eee;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  padding-top: 10rpx;
}

.tab-icon {
  margin-bottom: 6rpx;
}

.tab-text {
  font-size: 24rpx;
}

.tab-item.active {
  color: #4080ff;
}

.tab-item.active .tab-icon image {
  opacity: 1;
}

.tab-icon image {
  opacity: 0.7;
}
</style> 
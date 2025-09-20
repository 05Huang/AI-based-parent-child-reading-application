<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <text class="fas fa-chevron-left back-icon" @click="goBack"></text>
        </view>
        <text class="header-title">消息</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      

      <!-- 消息列表 -->
      <view class="chat-list">
        <!-- 家庭群聊 -->
        <view class="chat-item" @click="navigateToChat('family')">
          <view class="chat-avatar family-group">
            <image src="https://ui-avatars.com/api/?name=幸福家庭&background=3b82f6&color=fff&size=128" mode="aspectFill"></image>
          </view>
          <view class="chat-content">
            <view class="chat-header">
              <text class="chat-name">幸福家庭群</text>
              <text class="chat-time">12:30</text>
            </view>
            <view class="chat-info">
              <text class="chat-message">小明：今天我读完了《小王子》</text>
              <view class="unread-badge">2</view>
            </view>
          </view>
        </view>

        <!-- AI助手 -->
        <view class="chat-item" @click="navigateToAIChat">
          <view class="chat-avatar ai-assistant">
            <text class="fas fa-robot"></text>
          </view>
          <view class="chat-content">
            <view class="chat-header">
              <text class="chat-name">AI阅读助手</text>
              <text class="chat-time">11:45</text>
            </view>
            <view class="chat-info">
              <text class="chat-message">根据您的阅读习惯，为您推荐...</text>
            </view>
          </view>
        </view>

        <!-- 私聊列表 -->
        <view 
          class="chat-item" 
          v-for="chat in chatList" 
          :key="chat.id"
          @click="navigateToChat(chat.id)"
        >
          <view class="chat-avatar">
            <image :src="chat.avatar" mode="aspectFill"></image>
            <view class="online-status" v-if="chat.online"></view>
          </view>
          <view class="chat-content">
            <view class="chat-header">
              <text class="chat-name">{{ chat.name }}</text>
              <text class="chat-time">{{ chat.lastTime }}</text>
            </view>
            <view class="chat-info">
              <text class="chat-message">{{ chat.lastMessage }}</text>
              <view class="unread-badge" v-if="chat.unread">{{ chat.unread }}</view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 聊天列表数据
const chatList = ref([
  {
    id: '1',
    name: '小明',
    avatar: 'https://ui-avatars.com/api/?name=小明&background=3b82f6&color=fff&size=128',
    lastMessage: '爸爸，我们今晚一起读书吧！',
    lastTime: '10:30',
    unread: 1,
    online: true
  },
  {
    id: '2',
    name: '妈妈',
    avatar: 'https://ui-avatars.com/api/?name=妈妈&background=10b981&color=fff&size=128',
    lastMessage: '小明今天的阅读报告不错呢',
    lastTime: '09:15',
    unread: 0,
    online: true
  },
  {
    id: '3',
    name: '爷爷',
    avatar: 'https://ui-avatars.com/api/?name=爷爷&background=6366f1&color=fff&size=128',
    lastMessage: '给小明讲了一个有趣的故事',
    lastTime: '昨天',
    unread: 0,
    online: false
  }
])

// 跳转到聊天页面
const navigateToChat = (chatId) => {
  if (chatId === 'family') {
    uni.navigateTo({
      url: `/pages/parent/chat/group-chat?id=${chatId}`
    })
  } else {
    uni.navigateTo({
      url: `/pages/parent/chat/private-chat?id=${chatId}`
    })
  }
}

// 跳转到AI聊天页面
const navigateToAIChat = () => {
  uni.switchTab({
    url: '/pages/parent/ai-chat/ai-chat'
  })
}

// 返回上一页
const goBack = () => {
  uni.navigateBack({
    delta: 1
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
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: rgba(59, 130, 246, 0.98);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  z-index: 50;
}

.header-content {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  max-width: 1200rpx;
  margin: 0 auto;
  position: relative;
}

.header-left {
  width: 60rpx;
  position: absolute;
  left: 32rpx;
  z-index: 1;
}

.back-icon {
  font-size: 36rpx;
  color: #ffffff;
  padding: 20rpx;
  margin: -20rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
  flex: 1;
  text-align: center;
}

.action-icon {
  font-size: 40rpx;
  color: #ffffff;
  padding: 20rpx;
  margin: -20rpx;
}

/* 主要内容区域 */
.main-content {
  margin-top: 88rpx;
  height: calc(100vh - 88rpx);
  width: 100%;
  box-sizing: border-box;
}

/* 搜索框样式 */
.search-box {
  padding: 20rpx 32rpx;
  background-color: #f3f4f6;
}

.search-input {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
}

.search-icon {
  color: #9ca3af;
  font-size: 32rpx;
  margin-right: 16rpx;
}

.search-input input {
  flex: 1;
  font-size: 28rpx;
  color: #111827;
}

.search-placeholder {
  color: #9ca3af;
}

/* 聊天列表样式 */
.chat-list {
  background-color: #ffffff;
}

.chat-item {
  display: flex;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid rgba(229, 231, 235, 0.6);
  align-items: center;
}

.chat-avatar {
  position: relative;
  width: 96rpx;
  height: 96rpx;
  margin-right: 24rpx;
}

.chat-avatar image {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
}

.chat-avatar.family-group image {
  background-color: #3b82f6;
  border-radius: 16rpx;
}

.chat-avatar.ai-assistant {
  background-color: #eff6ff;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-avatar.ai-assistant .fas {
  font-size: 48rpx;
  color: #3b82f6;
}

.online-status {
  position: absolute;
  bottom: 4rpx;
  right: 4rpx;
  width: 16rpx;
  height: 16rpx;
  background-color: #10b981;
  border-radius: 50%;
  border: 3rpx solid #ffffff;
}

.chat-content {
  flex: 1;
  min-width: 0; /* 防止文本溢出 */
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.chat-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #111827;
}

.chat-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.chat-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-message {
  font-size: 28rpx;
  color: #6b7280;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-badge {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  background-color: #ef4444;
  border-radius: 18rpx;
  color: #ffffff;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 16rpx;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
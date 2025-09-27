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
      <!-- 加载中状态 -->
      <view v-if="loading" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 消息列表 -->
      <view v-else class="chat-list">
        <!-- 遍历聊天列表 -->
        <view 
          class="chat-item" 
          v-for="chat in chatList" 
          :key="chat.id + '_' + chat.type"
          @click="navigateToChat(chat)"
        >
          <view class="chat-avatar" :class="getChatAvatarClass(chat.type)">
            <image v-if="chat.type !== 'ai'" :src="chat.avatar" mode="aspectFill"></image>
            <text v-else class="fas fa-robot"></text>
          </view>
          <view class="chat-content">
            <view class="chat-header">
              <text class="chat-name">{{ chat.name }}</text>
              <text class="chat-time">{{ formatTime(chat.lastTime) }}</text>
            </view>
            <view class="chat-info">
              <text class="chat-message">{{ chat.lastMessage || '暂无消息' }}</text>
              <view v-if="chat.unread > 0" class="unread-badge">{{ chat.unread }}</view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { chatApi } from '../../../utils/api.js'

// 响应式数据
const chatList = ref([])
const loading = ref(true)

// 页面加载时获取聊天列表
onMounted(() => {
  console.log('notification页面加载，开始获取聊天列表')
  loadChatList()
})

// 获取聊天列表
const loadChatList = async () => {
  try {
    console.log('开始调用聊天列表API')
    loading.value = true
    
    const response = await chatApi.getChatList()
    console.log('聊天列表API响应：', response)
    
    if (response.code === 200 && response.data) {
      chatList.value = response.data.chatList || []
      console.log('聊天列表加载成功，数量：', chatList.value.length)
    } else {
      console.error('获取聊天列表失败：', response.message || '未知错误')
      uni.showToast({
        title: response.message || '获取聊天列表失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('获取聊天列表异常：', error)
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    })
    
    // 如果API失败，显示默认数据以保证基本功能
    loadDefaultChatList()
  } finally {
    loading.value = false
  }
}

// 加载默认聊天列表（API失败时的备用方案）
const loadDefaultChatList = () => {
  console.log('加载默认聊天列表作为备用方案')
  chatList.value = [
    {
      id: 'family',
      type: 'group',
      name: '幸福家庭群',
      avatar: 'https://ui-avatars.com/api/?name=幸福家庭&background=3b82f6&color=fff&size=128',
      lastMessage: '小明：今天我读完了《小王子》',
      lastTime: new Date(),
      unread: 2,
      memberCount: 4
    },
    {
      id: -1,
      type: 'ai',
      name: 'AI阅读助手',
      avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=ai123',
      lastMessage: '根据您的阅读习惯，为您推荐...',
      lastTime: new Date(),
      unread: 0
    }
  ]
}

// 获取聊天头像样式类
const getChatAvatarClass = (type) => {
  switch (type) {
    case 'group':
      return 'family-group'
    case 'ai':
      return 'ai-assistant'
    default:
      return ''
  }
}

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return ''
  
  try {
    const date = new Date(time)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
    
    if (messageDate.getTime() === today.getTime()) {
      // 今天，显示时间
      return date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: false 
      })
    } else if (messageDate.getTime() === today.getTime() - 24 * 60 * 60 * 1000) {
      // 昨天
      return '昨天'
    } else {
      // 更早的日期，显示月日
      return date.toLocaleDateString('zh-CN', { 
        month: '2-digit', 
        day: '2-digit' 
      })
    }
  } catch (error) {
    console.error('时间格式化错误：', error)
    return ''
  }
}

// 跳转到聊天页面
const navigateToChat = (chat) => {
  console.log('点击聊天项，准备跳转：', chat)
  
  try {
    if (chat.type === 'group') {
      // 跳转到群聊页面
      console.log('跳转到群聊页面，群组ID：', chat.id)
      uni.navigateTo({
        url: `/pages/parent/chat/group-chat?id=${chat.id}&name=${encodeURIComponent(chat.name)}`
      })
    } else if (chat.type === 'private') {
      // 跳转到私聊页面
      console.log('跳转到私聊页面，联系人ID：', chat.id)
      uni.navigateTo({
        url: `/pages/parent/chat/private-chat?id=${chat.id}&name=${encodeURIComponent(chat.name)}`
      })
    } else if (chat.type === 'ai') {
      // 跳转到AI聊天页面
      console.log('跳转到AI聊天页面')
      uni.switchTab({
        url: '/pages/parent/ai-chat/ai-chat'
      })
    }
  } catch (error) {
    console.error('页面跳转错误：', error)
    uni.showToast({
      title: '页面跳转失败',
      icon: 'none'
    })
  }
}

// 返回上一页
const goBack = () => {
  console.log('返回上一页')
  
  try {
    // 获取当前页面栈
    const pages = getCurrentPages()
    console.log('当前页面栈长度：', pages.length)
    
    if (pages.length > 1) {
      // 如果页面栈中有上一页，直接返回
      console.log('使用navigateBack返回上一页')
      uni.navigateBack({
        delta: 1,
        success: () => {
          console.log('成功返回上一页')
        },
        fail: (error) => {
          console.error('返回失败：', error)
          // 如果返回失败，跳转到首页
          fallbackToHome()
        }
      })
    } else {
      // 如果没有上一页，跳转到首页
      console.log('没有上一页，跳转到首页')
      fallbackToHome()
    }
  } catch (error) {
    console.error('返回处理异常：', error)
    fallbackToHome()
  }
}

// 备用方案：跳转到首页
const fallbackToHome = () => {
  console.log('使用备用方案跳转到首页')
  uni.switchTab({
    url: '/pages/parent/home/home',
    success: () => {
      console.log('成功跳转到首页')
    },
    fail: (error) => {
      console.error('跳转到首页失败：', error)
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

/* 加载状态样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
}

.loading-text {
  color: #9ca3af;
  font-size: 28rpx;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
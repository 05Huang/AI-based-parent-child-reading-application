<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <text class="header-title">AI阅读助手</text>
        <view class="header-actions">
          <text class="fas fa-history"></text>
          <text class="fas fa-ellipsis-v"></text>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 - 聊天界面 -->
    <scroll-view scroll-y="true" class="chat-container" :scroll-top="scrollTop">
      <!-- 欢迎消息 -->
      <view class="message-group">
        <view class="avatar">
          <image src="https://api.dicebear.com/7.x/bottts/svg?seed=ai123" mode="aspectFill"></image>
        </view>
        <view class="message-bubble ai">
          <view class="message-content">
            <text class="message-text">你好！我是你的AI阅读助手 (｡･∀･)ﾉﾞ</text>
            <view class="feature-list">
              <view class="feature-item">
                <text class="fas fa-book"></text>
                <text class="feature-text">解答阅读中的问题</text>
              </view>
              <view class="feature-item">
                <text class="fas fa-graduation-cap"></text>
                <text class="feature-text">讲解故事中的难词</text>
              </view>
              <view class="feature-item purple">
                <text class="fas fa-magic"></text>
                <text class="feature-text">创作有趣的故事</text>
              </view>
              <view class="feature-item green">
                <text class="fas fa-lightbulb"></text>
                <text class="feature-text">提供阅读建议</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 消息列表 -->
      <view v-for="(message, index) in messages" :key="index" class="message-group" :class="{'justify-end': message.type === 'user'}">
        <template v-if="message.type === 'user'">
          <view class="message-bubble user">
            <view class="message-content user">
              <text class="message-text">{{ message.content }}</text>
            </view>
          </view>
          <view class="avatar">
            <image src="https://api.dicebear.com/7.x/avataaars/svg?seed=user123" mode="aspectFill"></image>
          </view>
        </template>
        <template v-else>
          <view class="avatar">
            <image src="https://api.dicebear.com/7.x/bottts/svg?seed=ai123" mode="aspectFill"></image>
          </view>
          <view class="message-bubble ai">
            <view class="message-content">
              <text class="message-text">{{ message.content }}</text>
            </view>
          </view>
        </template>
      </view>

      <!-- 正在输入指示器 -->
      <view class="message-group" v-if="isTyping">
        <view class="avatar">
          <image src="https://api.dicebear.com/7.x/bottts/svg?seed=ai123" mode="aspectFill"></image>
        </view>
        <view class="message-bubble ai">
          <view class="typing-indicator">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 消息输入框 -->
    <view class="message-input">
      <view class="input-container">
        <text class="fas fa-microphone input-icon"></text>
        <input type="text" 
               v-model="messageText" 
               placeholder="和AI助手聊聊吧..." 
               class="message-field"
               @confirm="sendMessage">
        <view class="input-actions">
          <text class="fas fa-image input-icon"></text>
          <text class="fas fa-face-smile input-icon"></text>
          <button class="send-button" @click="sendMessage">发送</button>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
  </view>
</template>

<script setup>
import { ref } from 'vue'

// DeepSeek API配置
const API_KEY = 'sk-a1f425a6ae214572a0bb981c72af206e'
const API_URL = 'https://api.deepseek.com/chat/completions'

// 响应式状态
const messageText = ref('')
const isTyping = ref(false)
const scrollTop = ref(0)
const messages = ref([])
const messageHistory = ref([
  {
    role: "system",
    content: "你是一个专注于亲子阅读的AI助手，擅长讲故事、解释词汇、回答阅读相关问题，以及提供适合儿童的阅读建议。请使用简单易懂的语言，适合与孩子一起阅读。回答时要活泼可爱，适当使用emoji表情。"
  }
])

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim()) return

  // 添加用户消息到显示列表
  messages.value.push({
    type: 'user',
    content: messageText.value
  })

  // 添加用户消息到历史记录
  messageHistory.value.push({
    role: "user",
    content: messageText.value
  })

  // 清空输入框
  const userMessage = messageText.value
  messageText.value = ''

  // 显示AI正在输入
  isTyping.value = true

  try {
    // 调用API获取回复
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${API_KEY}`
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: messageHistory.value,
        stream: false
      })
    })

    if (!response.ok) {
      throw new Error('API请求失败')
    }

    const data = await response.json()
    const aiResponse = data.choices[0].message.content

    // 添加AI回复到显示列表
    messages.value.push({
      type: 'ai',
      content: aiResponse
    })

    // 添加AI回复到历史记录
    messageHistory.value.push({
      role: "assistant",
      content: aiResponse
    })

  } catch (error) {
    console.error('API请求错误:', error)
    // 显示错误消息
    messages.value.push({
      type: 'ai',
      content: '抱歉，我遇到了一些问题，请稍后再试。(｡•́︿•̀｡)'
    })
  } finally {
    // 隐藏输入指示器
    isTyping.value = false
    // 滚动到底部
    scrollTop.value = 9999
  }
}
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
  animation: pageSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes pageSlideIn {
  0% {
    opacity: 0;
    transform: translateY(30rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 顶部导航栏样式 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: #3b82f6;
  z-index: 50;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  height: 44px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.header-actions .fas {
  color: #ffffff;
  font-size: 20px;
}

/* 聊天容器样式 */
.chat-container {
  height: calc(100vh - 140px);
  margin-top: 44px;
  padding: 16px 8px;
  padding-bottom: 120px;
}

.message-group {
  display: flex;
  margin-bottom: 24px;
  padding: 0;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-bubble {
  margin-left: 6px;
  max-width: 70%;
  position: relative;
}

.message-bubble.ai::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 12px;
  width: 0;
  height: 0;
  border: 8px solid transparent;
  border-right-color: #ffffff;
}

.message-content {
  background-color: #ffffff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-text {
  color: #1f2937;
  margin-bottom: 12px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-item {
  display: flex;
  align-items: center;
  padding: 8px;
  background-color: #eff6ff;
  border-radius: 8px;
}

.feature-item.purple {
  background-color: #f5f3ff;
}

.feature-item.green {
  background-color: #f0fdf4;
}

.feature-item .fas {
  color: #3b82f6;
  margin-right: 8px;
  font-size: 16px;
}

.feature-item.purple .fas {
  color: #8b5cf6;
}

.feature-item.green .fas {
  color: #22c55e;
}

.feature-text {
  color: #4b5563;
  font-size: 14px;
}

/* 输入指示器样式 */
.typing-indicator {
  background-color: #ffffff;
  padding: 12px;
  border-radius: 12px;
  display: flex;
  gap: 4px;
}

.dot {
  width: 10px;
  height: 10px;
  background-color: #9ca3af;
  border-radius: 50%;
  opacity: 0.4;
  animation: blink 1s infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  50% {
    opacity: 1;
  }
}

/* 消息输入框样式 */
.message-input {
  position: fixed;
  bottom: 60px;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 12px 8px;
  border-top: 1px solid #e5e7eb;
}

.input-container {
  display: flex;
  align-items: center;
  background-color: #f3f4f6;
  border-radius: 24px;
  padding: 4px 8px;
  margin: 0;
}

.input-icon {
  color: #6b7280;
  font-size: 20px;
  padding: 8px;
}

.message-field {
  flex: 1;
  background: transparent;
  border: none;
  padding: 8px;
  font-size: 14px;
}

.input-actions {
  display: flex;
  align-items: center;
}

.send-button {
  background-color: #3b82f6;
  color: #ffffff;
  padding: 6px 16px;
  border-radius: 16px;
  margin-left: 8px;
  font-size: 14px;
}

/* 底部导航栏样式 */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #6b7280;
}

.tab-item.active {
  color: #3b82f6;
}

.tab-item .fas {
  font-size: 20px;
  margin-bottom: 4px;
}

.tab-text {
  font-size: 12px;
}

.message-bubble.user {
  margin-right: 8px;
  margin-left: 0;
}

.message-bubble.user::before {
  content: '';
  position: absolute;
  right: -8px;
  top: 12px;
  width: 0;
  height: 0;
  border: 8px solid transparent;
  border-left-color: #3b82f6;
}

.message-content.user {
  background-color: #3b82f6;
  color: white;
}

.message-content.user .message-text {
  color: white;
}

.justify-end {
  justify-content: flex-end;
}

/* AI消息样式 */
.message-group:not(.justify-end) {
  padding-left: 4px;
}

/* 用户消息样式 */
.message-group.justify-end {
  padding-right: 8px;
}
</style> 
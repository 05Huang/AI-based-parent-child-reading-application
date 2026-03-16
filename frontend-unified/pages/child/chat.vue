<template>
  <view class="container">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <text class="header-title">AI阅读助手</text>
        <view class="header-actions">
          <text class="fas fa-history" @tap="goHistory"></text>
          <text class="fas fa-ellipsis-v" @tap="toggleMenu"></text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y="true" class="chat-container" :scroll-top="scrollTop" :style="chatContainerStyle">
      <view class="message-group">
        <view class="avatar">
          <image :src="aiAvatar" mode="aspectFill"></image>
        </view>
        <view class="message-bubble ai">
          <view class="message-content">
            <text class="message-text">你好！我是你的AI阅读助手 (｡･∀･)ﾉﾞ</text>
            <view class="feature-list">
              <view class="feature-item" @tap="selectFeature('解答阅读中的问题', false)">
                <text class="fas fa-book"></text>
                <text class="feature-text">解答阅读中的问题</text>
              </view>
              <view class="feature-item" @tap="selectFeature('讲解故事中的难词', true)">
                <text class="fas fa-graduation-cap"></text>
                <text class="feature-text">讲解故事中的难词</text>
              </view>
              <view class="feature-item purple" @tap="selectFeature('创作有趣的故事', true)">
                <text class="fas fa-magic"></text>
                <text class="feature-text">创作有趣的故事</text>
              </view>
              <view class="feature-item green" @tap="selectFeature('提供阅读建议', true)">
                <text class="fas fa-lightbulb"></text>
                <text class="feature-text">提供阅读建议</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-for="(message, index) in messages" :key="index" class="message-group" :class="{ 'justify-end': message.type === 'user' }">
        <template v-if="message.type === 'user'">
          <view class="message-bubble user">
            <view class="message-content user">
              <text class="message-text">{{ message.content }}</text>
            </view>
          </view>
          <view class="avatar">
            <image :src="userAvatar" mode="aspectFill"></image>
          </view>
        </template>
        <template v-else>
          <view class="avatar">
            <image :src="aiAvatar" mode="aspectFill"></image>
          </view>
          <view class="message-bubble ai">
            <view class="message-content">
              <text class="message-text">{{ message.content }}</text>
            </view>
          </view>
        </template>
      </view>

      <view class="message-group" v-if="isTyping">
        <view class="avatar">
          <image :src="aiAvatar" mode="aspectFill"></image>
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

    <view class="message-input">
      <view class="input-container">
        <text class="fas fa-microphone input-icon"></text>
        <input
          type="text"
          v-model="messageText"
          placeholder="和AI助手聊聊吧..."
          class="message-field"
          @confirm="sendMessage"
        />
        <view class="input-actions">
          <button class="send-button" @click="sendMessage">发送</button>
        </view>
      </view>
    </view>

    <view class="tab-bar">
      <view class="tab-item" @tap="switchTab('/pages/child/home')">
        <text class="fas fa-home"></text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/reading')">
        <text class="fas fa-book"></text>
        <text class="tab-text">阅读</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/video')">
        <text class="fas fa-video"></text>
        <text class="tab-text">视频</text>
      </view>
      <view class="tab-item active" @tap="switchTab('/pages/child/chat')">
        <text class="fas fa-comments"></text>
        <text class="tab-text">对话</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/profile')">
        <text class="fas fa-user"></text>
        <text class="tab-text">我的</text>
      </view>
    </view>

    <view v-if="menuVisible" class="menu-mask" @tap="closeMenu"></view>
    <view v-if="menuVisible" class="floating-menu">
      <view class="menu-item danger" @tap="confirmClearHistory">
        <text class="fas fa-trash-can"></text>
        <text>清空对话记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { aiApi, userApi } from '@/utils/api.js'

const SYSTEM_PROMPT =
  '你是一个专注于阅桥亲子阅读APP的AI助手，擅长讲故事、解释词汇、回答阅读相关问题，以及提供适合儿童的阅读建议。请使用简单易懂的语言，适合与孩子一起阅读。回答时要活泼可爱，适当使用emoji表情。'

const TAB_BAR_HEIGHT = 60
const INPUT_HEIGHT = 60

const statusBarHeight = ref(20)
const headerHeight = ref(60)

const messageText = ref('')
const isTyping = ref(false)
const scrollTop = ref(0)
const messages = ref([])
const messageHistory = ref([
  {
    role: 'system',
    content: SYSTEM_PROMPT
  }
])

const aiAvatar = ref('/static/images/avatars/AI机器人.svg')
const userAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=child123')
const menuVisible = ref(false)
const sessionId = ref(null)

const chatContainerStyle = computed(() => {
  return {
    marginTop: `${headerHeight.value}px`,
    height: `calc(100vh - ${headerHeight.value + TAB_BAR_HEIGHT + INPUT_HEIGHT}px)`
  }
})

const loadUserInfo = async () => {
  try {
    const res = await userApi.getCurrentUser()
    if (res && res.code === 200 && res.data?.avatar) {
      userAvatar.value = res.data.avatar
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

const scrollToBottom = async () => {
  await nextTick()
  scrollTop.value = 999999
}

const resetLocalChat = () => {
  messages.value = []
  messageHistory.value = [{ role: 'system', content: SYSTEM_PROMPT }]
}

const sendMessage = async () => {
  const content = messageText.value.trim()
  if (!content) return

  messages.value.push({ type: 'user', content })
  messageHistory.value.push({ role: 'user', content })

  messageText.value = ''
  isTyping.value = true
  await scrollToBottom()

  try {
    const res = await aiApi.chat(messageHistory.value)
    const aiResponse = res?.choices?.[0]?.message?.content
    if (!aiResponse) throw new Error('AI响应格式错误')

    messages.value.push({ type: 'ai', content: aiResponse })
    messageHistory.value.push({ role: 'assistant', content: aiResponse })
    try {
      await aiApi.savePersonalRecord({
        userQuestion: content,
        aiReply: aiResponse,
        sessionId: sessionId.value,
        tokensUsed: res?.usage?.total_tokens
      })
    } catch (e) {
      console.error('保存对话记录失败', e)
    }
  } catch (error) {
    console.error('消息发送错误:', error)
    messages.value.push({ type: 'ai', content: '抱歉，我遇到了一些问题，请稍后再试。(｡•́︿•̀｡)' })
  } finally {
    isTyping.value = false
    await scrollToBottom()
  }
}

const selectFeature = (featureText, autoSend = true) => {
  if (autoSend) {
    messageText.value = featureText
    sendMessage()
    return
  }

  if (featureText === '解答阅读中的问题') {
    messageText.value = '我在阅读中遇到了一个问题：'
  } else {
    messageText.value = featureText
  }
}

const switchTab = (url) => {
  if (url === '/pages/child/chat') return
  uni.switchTab({
    url,
    fail: () => {
      uni.reLaunch({ url })
    }
  })
}

const goHistory = () => {
  uni.navigateTo({ url: '/pages/common/ai-history' })
}

const toggleMenu = () => {
  menuVisible.value = !menuVisible.value
}

const closeMenu = () => {
  menuVisible.value = false
}

const confirmClearHistory = () => {
  closeMenu()
  uni.showModal({
    title: '提示',
    content: '确定清空全部对话记录吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        uni.showLoading({ title: '清空中...' })
        const r = await aiApi.clearPersonalHistory()
        if (r && r.code === 200) {
          resetLocalChat()
          const nextSessionId = Date.now()
          sessionId.value = nextSessionId
          uni.setStorageSync('aiPersonalSessionId_child', nextSessionId)
          uni.showToast({ title: '已清空', icon: 'success' })
        } else {
          uni.showToast({ title: r?.msg || '清空失败', icon: 'none' })
        }
      } catch (e) {
        console.error('清空失败', e)
        uni.showToast({ title: '清空失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  headerHeight.value = statusBarHeight.value + 56
  const stored = uni.getStorageSync('aiPersonalSessionId_child')
  if (stored) {
    sessionId.value = stored
  } else {
    const sid = Date.now()
    sessionId.value = sid
    uni.setStorageSync('aiPersonalSessionId_child', sid)
  }
  loadUserInfo()
})
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: #8b5cf6;
  z-index: 50;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  height: 56px;
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

.menu-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  z-index: 99;
}

.floating-menu {
  position: fixed;
  top: calc(var(--status-bar-height) + 56px);
  right: 12px;
  width: 160px;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  z-index: 100;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
}

.menu-item {
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #111827;
  font-size: 14px;
}

.menu-item + .menu-item {
  border-top: 1px solid #f3f4f6;
}

.menu-item.danger {
  color: #ef4444;
}

.menu-item .fas {
  font-size: 16px;
}

.chat-container {
  padding: 16px 8px;
  padding-bottom: 140px;
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
  border-left-color: #8b5cf6;
}

.message-content {
  background-color: #ffffff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-content.user {
  background-color: #8b5cf6;
  color: white;
}

.message-content.user .message-text {
  color: #ffffff;
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
  background-color: #f5f3ff;
  border-radius: 8px;
}

.feature-item.purple {
  background-color: #f5f3ff;
}

.feature-item.green {
  background-color: #f0fdf4;
}

.feature-item .fas {
  color: #8b5cf6;
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
  background-color: #8b5cf6;
  color: #ffffff;
  padding: 6px 16px;
  border-radius: 16px;
  margin-left: 8px;
  font-size: 14px;
}

.justify-end {
  justify-content: flex-end;
}

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 8px 0;
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.tab-item.active {
  color: #8b5cf6;
}

.tab-item .fas {
  font-size: 20px;
  margin-bottom: 4px;
}

.tab-text {
  font-size: 12px;
}
</style> 

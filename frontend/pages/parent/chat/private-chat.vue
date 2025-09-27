<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <text class="fas fa-arrow-left" @click="goBack"></text>
          <view class="user-info">
            <text class="user-name">{{ chatInfo.name }}</text>
            <text class="user-status" v-if="chatInfo.online">在线</text>
          </view>
        </view>
        <view class="header-actions">
          <text class="fas fa-ellipsis-v"></text>
        </view>
      </view>
    </view>

    <!-- 聊天内容区域 -->
    <scroll-view 
      scroll-y="true" 
      class="chat-container"
      :scroll-top="scrollTop"
      :scroll-into-view="lastMessageId"
      @scrolltoupper="loadMoreMessages"
    >
      <!-- 加载更多提示 -->
      <view class="loading-more" v-if="isLoading">
        <text class="fas fa-spinner fa-spin"></text>
      </view>

      <!-- 消息列表 -->
      <view 
        v-for="message in messages" 
        :key="message.id"
        :id="'msg-' + message.id"
        class="message-group"
      >
        <!-- 时间戳 -->
        <view class="time-stamp" v-if="message.showTime">
          {{ message.time }}
        </view>

        <view class="message-row" :class="{'justify-end': message.isSelf}">
          <template v-if="message.isSelf">
            <view class="message-bubble user">
              <view class="message-content user">
                <!-- 文本消息 -->
                <text v-if="message.type === 'text'" class="message-text">{{ message.content }}</text>
                <!-- 图片消息 -->
                <image 
                  v-else-if="message.type === 'image'" 
                  class="image-message" 
                  :src="message.content"
                  mode="widthFix"
                  @tap="previewImage(message.content)"
                ></image>
              </view>
            </view>
            <view class="avatar">
              <image :src="userInfo.avatar" mode="aspectFill"></image>
            </view>
          </template>
          <template v-else>
            <view class="avatar">
              <image :src="chatInfo.avatar" mode="aspectFill"></image>
            </view>
            <view class="message-bubble">
              <view class="message-content">
                <!-- 文本消息 -->
                <text v-if="message.type === 'text'" class="message-text">{{ message.content }}</text>
                <!-- 图片消息 -->
                <image 
                  v-else-if="message.type === 'image'" 
                  class="image-message" 
                  :src="message.content"
                  mode="widthFix"
                  @tap="previewImage(message.content)"
                ></image>
              </view>
            </view>
          </template>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="message-input">
      <view class="input-container">
        <text class="fas fa-microphone input-icon"></text>
        <input 
          type="text" 
          v-model="inputMessage"
          placeholder="输入消息..."
          class="message-field"
          @confirm="sendMessage"
        />
        <view class="input-actions">
          <text class="fas fa-image input-icon" @tap="chooseImage"></text>
          <text class="fas fa-face-smile input-icon"></text>
          <button class="send-button" @click="sendMessage">发送</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { chatApi } from '../../../utils/api.js'

// 聊天信息
const chatInfo = ref({
  id: '',
  name: '加载中...',
  avatar: '',
  online: true
})

// 用户信息
const userInfo = ref({
  avatar: 'https://ui-avatars.com/api/?name=我&background=3b82f6&color=fff&size=128'
})

// 消息列表
const messages = ref([])
const isLoading = ref(false)
const scrollTop = ref(0)
const lastMessageId = ref('')
const inputMessage = ref('')

// 分页参数
const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 获取路由参数
onMounted(() => {
  console.log('私聊页面加载')
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const contactId = currentPage.options.id
  const contactName = currentPage.options.name
  
  console.log('联系人参数：', { contactId, contactName })
  
  if (contactId) {
    chatInfo.value.id = contactId
    if (contactName) {
      chatInfo.value.name = decodeURIComponent(contactName)
    }
    
    // 加载私聊数据
    loadChatData()
  } else {
    console.error('缺少联系人ID参数')
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
  }
})

// 加载私聊数据
const loadChatData = async () => {
  try {
    console.log('开始加载私聊数据，联系人ID：', chatInfo.value.id)
    
    // 加载消息列表
    await loadMessages()
    
  } catch (error) {
    console.error('加载私聊数据失败：', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
    
    // 加载默认数据
    loadDefaultData()
  }
}

// 加载消息历史
const loadMessages = async (page = 1) => {
  try {
    if (isLoading.value) return
    
    console.log('开始加载私聊消息，页码：', page)
    isLoading.value = true
    
    const response = await chatApi.getPrivateMessages(chatInfo.value.id, page, pageSize.value)
    console.log('私聊消息API响应：', response)
    
    if (response.code === 200 && response.data) {
      const data = response.data
      
      // 更新联系人信息
      if (data.contactInfo) {
        chatInfo.value = {
          ...chatInfo.value,
          ...data.contactInfo
        }
      }
      
      // 处理消息列表
      if (data.messages) {
        const newMessages = data.messages.map(msg => ({
          id: msg.id,
          type: msg.type ? 'image' : 'text', // false=文字, true=图片
          content: msg.content,
          time: formatMessageTime(msg.sendTime),
          isSelf: msg.isSelf,
          showTime: true // 可以根据时间间隔优化
        }))
        
        if (page === 1) {
          // 第一页，直接替换
          messages.value = newMessages.reverse() // 反转顺序，最新的在下面
        } else {
          // 后续页，添加到前面
          messages.value = [...newMessages.reverse(), ...messages.value]
        }
        
        // 更新分页信息
        hasMore.value = data.hasMore || false
        currentPage.value = page
        
        // 滚动到最新消息（仅第一页）
        if (page === 1 && newMessages.length > 0) {
          lastMessageId.value = 'msg-' + newMessages[newMessages.length - 1].id
          nextTick(() => {
            scrollTop.value = 9999
          })
        }
      }
      
      console.log('私聊消息加载成功，当前消息数：', messages.value.length)
    } else {
      throw new Error(response.message || '获取消息失败')
    }
  } catch (error) {
    console.error('加载私聊消息失败：', error)
    
    if (page === 1) {
      // 第一页加载失败，显示默认数据
      loadDefaultData()
    } else {
      uni.showToast({
        title: '加载更多失败',
        icon: 'none'
      })
    }
  } finally {
    isLoading.value = false
  }
}

// 加载默认数据（API失败时的备用方案）
const loadDefaultData = () => {
  console.log('加载默认私聊数据')
  
  chatInfo.value = {
    id: chatInfo.value.id,
    name: chatInfo.value.name || '小明',
    avatar: 'https://ui-avatars.com/api/?name=小明&background=6366f1&color=fff&size=128',
    online: true
  }
  
  messages.value = [
    {
      id: 1,
      type: 'text',
      content: '爸爸，我们今晚一起读书吧！',
      time: '10:30',
      isSelf: false,
      showTime: true
    },
    {
      id: 2,
      type: 'text',
      content: '好啊，你想读什么书？',
      time: '10:31',
      isSelf: true,
      showTime: false
    }
  ]
}

// 格式化消息时间
const formatMessageTime = (time) => {
  if (!time) return ''
  
  try {
    const date = new Date(time)
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false 
    })
  } catch (error) {
    console.error('时间格式化错误：', error)
    return ''
  }
}

// 加载更多消息
const loadMoreMessages = () => {
  if (isLoading.value || !hasMore.value) return
  
  console.log('加载更多消息，下一页：', currentPage.value + 1)
  loadMessages(currentPage.value + 1)
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  console.log('准备发送私聊消息：', inputMessage.value)
  
  try {
    const content = inputMessage.value.trim()
    const tempMessage = {
      id: Date.now(), // 临时ID
      type: 'text',
      content: content,
      time: formatMessageTime(new Date()),
      isSelf: true,
      showTime: true,
      sending: true // 标记为发送中
    }
    
    // 先添加到界面显示
    messages.value.push(tempMessage)
    inputMessage.value = ''
    
    // 滚动到底部
    nextTick(() => {
      scrollTop.value = 9999
    })
    
    // 调用API发送消息
    const response = await chatApi.sendPrivateMessage(chatInfo.value.id, content, 0)
    console.log('发送私聊消息API响应：', response)
    
    if (response.code === 200 && response.data) {
      // 更新消息状态
      const messageIndex = messages.value.findIndex(msg => msg.id === tempMessage.id)
      if (messageIndex !== -1) {
        messages.value[messageIndex] = {
          ...tempMessage,
          id: response.data.id,
          sending: false
        }
      }
      
      console.log('私聊消息发送成功')
    } else {
      throw new Error(response.message || '发送失败')
    }
  } catch (error) {
    console.error('发送私聊消息失败：', error)
    
    // 移除失败的消息或标记为失败
    const messageIndex = messages.value.findIndex(msg => msg.sending)
    if (messageIndex !== -1) {
      messages.value.splice(messageIndex, 1)
    }
    
    uni.showToast({
      title: error.message || '发送失败',
      icon: 'none'
    })
    
    // 恢复输入框内容
    inputMessage.value = content
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // 这里应该先上传图片，获取到URL后再发送消息
      const newMessage = {
        id: Date.now(),
        type: 'image',
        content: tempFilePath,
        time: formatMessageTime(new Date()),
        isSelf: true,
        showTime: true
      }
      messages.value.push(newMessage)
      
      nextTick(() => {
        scrollTop.value = 9999
      })
    }
  })
}

// 预览图片
const previewImage = (url) => {
  uni.previewImage({
    urls: [url]
  })
}
</script>

<style>
/* 引入Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
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

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left .fas {
  color: #ffffff;
  font-size: 20px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.user-status {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
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

.loading-more {
  text-align: center;
  padding: 20rpx 0;
  color: #9ca3af;
}

.time-stamp {
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 8px;
}

.message-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 24px;
}

.message-row {
  display: flex;
  padding: 0 4px;
}

.message-row.justify-end {
  justify-content: flex-end;
  padding-right: 8px;
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

.message-bubble::before {
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
  left: auto;
  right: -8px;
  border-right-color: transparent;
  border-left-color: #3b82f6;
}

.message-content {
  background-color: #ffffff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-content.user {
  background-color: #3b82f6;
}

.message-text {
  color: #1f2937;
  font-size: 14px;
  line-height: 1.4;
}

.message-content.user .message-text {
  color: #ffffff;
}

.image-message {
  max-width: 200px;
  border-radius: 8px;
}

/* 输入区域样式 */
.message-input {
  position: fixed;
  bottom: 0;
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
</style> 
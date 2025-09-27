<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <text class="fas fa-arrow-left" @click="goBack"></text>
          <view class="group-info">
            <text class="group-name">{{ groupInfo.name }}</text>
            <text class="member-count">{{ groupInfo.memberCount }}人</text>
          </view>
        </view>
        <view class="header-actions">
          <text class="fas fa-users" @click="showMembers"></text>
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
                <!-- 阅读进度分享 -->
                <view 
                  v-else-if="message.type === 'reading-progress'" 
                  class="reading-progress"
                  @tap="viewReadingProgress(message)"
                >
                  <view class="progress-header">
                    <text class="fas fa-book-reader"></text>
                    <text class="progress-title">阅读进度分享</text>
                  </view>
                  <view class="progress-content">
                    <text class="book-name">{{ message.bookName }}</text>
                    <text class="progress-text">已读完第{{ message.chapter }}章</text>
                  </view>
                </view>
              </view>
            </view>
            <view class="avatar-wrapper">
              <image class="avatar" :src="userInfo.avatar" mode="aspectFill"></image>
              <text class="sender-name">{{ userInfo.name }}</text>
            </view>
          </template>
          <template v-else>
            <view class="avatar-wrapper">
              <image class="avatar" :src="message.avatar" mode="aspectFill"></image>
              <text class="sender-name">{{ message.senderName }}</text>
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
                <!-- 阅读进度分享 -->
                <view 
                  v-else-if="message.type === 'reading-progress'" 
                  class="reading-progress"
                  @tap="viewReadingProgress(message)"
                >
                  <view class="progress-header">
                    <text class="fas fa-book-reader"></text>
                    <text class="progress-title">阅读进度分享</text>
                  </view>
                  <view class="progress-content">
                    <text class="book-name">{{ message.bookName }}</text>
                    <text class="progress-text">已读完第{{ message.chapter }}章</text>
                  </view>
                </view>
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

      <!-- 更多功能面板 -->
      <view class="more-panel" v-if="showMorePanel">
        <view class="more-item" @tap="chooseImage">
          <view class="more-icon-wrapper">
            <text class="fas fa-image"></text>
          </view>
          <text class="more-text">图片</text>
        </view>
        <view class="more-item" @tap="startReading">
          <view class="more-icon-wrapper">
            <text class="fas fa-book-reader"></text>
          </view>
          <text class="more-text">共读绘本</text>
        </view>
        <view class="more-item" @tap="shareProgress">
          <view class="more-icon-wrapper">
            <text class="fas fa-share"></text>
          </view>
          <text class="more-text">分享进度</text>
        </view>
        <view class="more-item" @tap="openAIChat">
          <view class="more-icon-wrapper">
            <text class="fas fa-robot"></text>
          </view>
          <text class="more-text">AI助手</text>
        </view>
      </view>
    </view>

    <!-- 群成员面板 -->
    <view class="member-panel" v-if="showMemberPanel">
      <view class="panel-header">
        <text class="panel-title">群成员 ({{ groupInfo.memberCount }})</text>
        <text class="fas fa-times" @click="showMemberPanel = false"></text>
      </view>
      <scroll-view scroll-y="true" class="member-list">
        <view 
          class="member-item" 
          v-for="member in groupMembers" 
          :key="member.id"
        >
          <image class="member-avatar" :src="member.avatar" mode="aspectFill"></image>
          <view class="member-info">
            <text class="member-name">{{ member.name }}</text>
            <text class="member-role">{{ member.role }}</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { chatApi } from '../../../utils/api.js'

// 群组信息
const groupInfo = ref({
  id: '',
  name: '加载中...',
  memberCount: 0
})

// 用户信息
const userInfo = ref({
  id: '',
  name: '我',
  avatar: 'https://ui-avatars.com/api/?name=我&background=3b82f6&color=fff&size=128'
})

// 群成员列表
const groupMembers = ref([])

// 消息列表
const messages = ref([])
const isLoading = ref(false)
const scrollTop = ref(0)
const lastMessageId = ref('')
const inputMessage = ref('')
const showMorePanel = ref(false)
const showMemberPanel = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 获取路由参数
onMounted(() => {
  console.log('群聊页面加载')
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const groupId = currentPage.options.id
  const groupName = currentPage.options.name
  
  console.log('群组参数：', { groupId, groupName })
  
  if (groupId) {
    groupInfo.value.id = groupId
    if (groupName) {
      groupInfo.value.name = decodeURIComponent(groupName)
    }
    
    // 加载群聊数据
    loadGroupData()
  } else {
    console.error('缺少群组ID参数')
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
  }
})

// 加载群聊数据
const loadGroupData = async () => {
  try {
    console.log('开始加载群聊数据，群组ID：', groupInfo.value.id)
    
    // 加载消息列表
    await loadMessages()
    
  } catch (error) {
    console.error('加载群聊数据失败：', error)
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
    
    console.log('开始加载群消息，页码：', page)
    isLoading.value = true
    
    const response = await chatApi.getGroupMessages(groupInfo.value.id, page, pageSize.value)
    console.log('群消息API响应：', response)
    
    if (response.code === 200 && response.data) {
      const data = response.data
      
      // 更新群组信息
      if (data.groupInfo) {
        groupInfo.value = {
          ...groupInfo.value,
          ...data.groupInfo
        }
      }
      
      // 更新群成员列表
      if (data.members) {
        groupMembers.value = data.members
      }
      
      // 处理消息列表
      if (data.messages) {
        const newMessages = data.messages.map(msg => ({
          id: msg.id,
          type: msg.type ? 'image' : 'text', // false=文字, true=图片
          content: msg.content,
          time: formatMessageTime(msg.sendTime),
          isSelf: msg.isSelf,
          showTime: true, // 可以根据时间间隔优化
          senderName: msg.senderName,
          avatar: msg.avatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(msg.senderName)
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
      
      console.log('群消息加载成功，当前消息数：', messages.value.length)
    } else {
      throw new Error(response.message || '获取消息失败')
    }
  } catch (error) {
    console.error('加载群消息失败：', error)
    
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
  console.log('加载默认群聊数据')
  
  groupInfo.value = {
    id: groupInfo.value.id,
    name: groupInfo.value.name || '幸福家庭群',
    memberCount: 4
  }
  
  groupMembers.value = [
    {
      id: 'user123',
      name: '爸爸',
      role: '家长',
      avatar: 'https://ui-avatars.com/api/?name=爸爸&background=3b82f6&color=fff&size=128'
    },
    {
      id: 'mom456',
      name: '妈妈',
      role: '家长',
      avatar: 'https://ui-avatars.com/api/?name=妈妈&background=10b981&color=fff&size=128'
    },
    {
      id: 'child789',
      name: '小明',
      role: '孩子',
      avatar: 'https://ui-avatars.com/api/?name=小明&background=6366f1&color=fff&size=128'
    }
  ]
  
  messages.value = [
    {
      id: 1,
      type: 'text',
      content: '今天我们一起读《小王子》吧！',
      time: '10:30',
      isSelf: false,
      showTime: true,
      senderName: '小明',
      avatar: 'https://ui-avatars.com/api/?name=小明&background=6366f1&color=fff&size=128'
    }
  ]
  
  userInfo.value = {
    id: 'user123',
    name: '爸爸',
    avatar: 'https://ui-avatars.com/api/?name=爸爸&background=3b82f6&color=fff&size=128'
  }
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
  
  console.log('准备发送群消息：', inputMessage.value)
  
  try {
    const content = inputMessage.value.trim()
    const tempMessage = {
      id: Date.now(), // 临时ID
      type: 'text',
      content: content,
      time: formatMessageTime(new Date()),
      isSelf: true,
      showTime: true,
      senderName: userInfo.value.name,
      avatar: userInfo.value.avatar,
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
    const response = await chatApi.sendGroupMessage(groupInfo.value.id, content, 0)
    console.log('发送群消息API响应：', response)
    
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
      
      console.log('群消息发送成功')
    } else {
      throw new Error(response.message || '发送失败')
    }
  } catch (error) {
    console.error('发送群消息失败：', error)
    
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

// 显示群成员
const showMembers = () => {
  showMemberPanel.value = true
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 显示更多功能面板
const showMore = () => {
  showMorePanel.value = !showMorePanel.value
}

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // 这里应该先上传图片，获取到URL后再发送消息
      const newMessage = {
        id: messages.value.length + 1,
        type: 'image',
        content: tempFilePath,
        time: formatMessageTime(new Date()),
        isSelf: true,
        showTime: true,
        senderName: userInfo.value.name,
        avatar: userInfo.value.avatar
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

// 开始共读
const startReading = () => {
  uni.navigateTo({
    url: '/pages/parent/reading/reading'
  })
}

// 分享阅读进度
const shareProgress = () => {
  // 模拟分享进度
  const newMessage = {
    id: messages.value.length + 1,
    type: 'reading-progress',
    bookName: '小王子',
    chapter: 3,
    time: formatMessageTime(new Date()),
    isSelf: true,
    showTime: true,
    senderName: userInfo.value.name,
    avatar: userInfo.value.avatar
  }
  messages.value.push(newMessage)
  
  nextTick(() => {
    scrollTop.value = 9999
  })
}

// 查看阅读进度
const viewReadingProgress = (message) => {
  uni.navigateTo({
    url: `/pages/parent/reading/progress?book=${message.bookName}`
  })
}

// 打开AI助手
const openAIChat = () => {
  uni.switchTab({
    url: '/pages/parent/ai-chat/ai-chat'
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

.group-info {
  display: flex;
  flex-direction: column;
}

.group-name {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.member-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
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

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-bottom: 4px;
  flex-shrink: 0;
}

.sender-name {
  font-size: 12px;
  color: #6b7280;
  max-width: 48px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

/* 阅读进度分享样式 */
.reading-progress {
  background-color: #f8fafc;
  border-radius: 8px;
  padding: 12px;
}

.message-content.user .reading-progress {
  background-color: #dbeafe;
}

.progress-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.progress-header .fas {
  font-size: 16px;
  color: #3b82f6;
  margin-right: 8px;
}

.progress-title {
  font-size: 14px;
  color: #3b82f6;
  font-weight: 500;
}

.progress-content {
  display: flex;
  flex-direction: column;
}

.book-name {
  font-size: 16px;
  color: #111827;
  margin-bottom: 4px;
}

.progress-text {
  font-size: 14px;
  color: #6b7280;
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

/* 更多功能面板样式 */
.more-panel {
  display: flex;
  padding: 16px;
  background-color: #ffffff;
  border-top: 1px solid #e5e7eb;
}

.more-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 24px;
}

.more-icon-wrapper {
  width: 48px;
  height: 48px;
  background-color: #f3f4f6;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
}

.more-icon-wrapper .fas {
  font-size: 24px;
  color: #3b82f6;
}

.more-text {
  font-size: 12px;
  color: #6b7280;
}

/* 群成员面板样式 */
.member-panel {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 300px;
  background-color: #ffffff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.panel-title {
  font-size: 16px;
  font-weight: 500;
  color: #111827;
}

.panel-header .fas {
  font-size: 20px;
  color: #6b7280;
  padding: 8px;
}

.member-list {
  height: calc(100% - 56px);
  padding: 16px;
}

.member-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  margin-right: 12px;
}

.member-info {
  display: flex;
  flex-direction: column;
}

.member-name {
  font-size: 14px;
  color: #111827;
  margin-bottom: 2px;
}

.member-role {
  font-size: 12px;
  color: #6b7280;
}
</style> 
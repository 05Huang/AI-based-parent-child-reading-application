<template>
  <view class="container" :style="themeStyle">
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
          <text class="fas fa-ellipsis-v" @click="openPrivateSettings"></text>
        </view>
      </view>
    </view>

    <scroll-view 
      scroll-y="true" 
      class="chat-container"
      :scroll-top="scrollTop"
      :scroll-into-view="lastMessageId"
      @scrolltoupper="loadMoreMessages"
      @scroll="handleScroll"
    >
      <view class="loading-more" v-if="isLoading">
        <text class="fas fa-spinner fa-spin"></text>
      </view>

      <view 
        v-for="message in messages" 
        :key="message.id"
        :id="'msg-' + message.id"
        class="message-group"
      >
        <view class="time-stamp" v-if="message.showTime">
          {{ message.time }}
        </view>

        <view class="message-row" :class="{'justify-end': message.isSelf}">
          <template v-if="message.isSelf">
            <view class="message-bubble user">
              <view class="message-content user">
                <text v-if="message.type === 'text'" class="message-text">{{ message.content }}</text>
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
                <text v-if="message.type === 'text'" class="message-text">{{ message.content }}</text>
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

    <view 
      v-if="newMessageCount > 0" 
      class="new-message-tip" 
      @tap="scrollToBottom"
    >
      <text class="new-message-text">{{ newMessageCount }}条新消息，点击查看</text>
    </view>

    <view class="message-input">
      <view class="input-container">
        <text class="fas fa-microphone input-icon" @tap="toggleRecording"></text>
        <input 
          type="text" 
          v-model="inputMessage"
          placeholder="输入消息..."
          class="message-field"
          @confirm="sendMessage"
        />
        <view class="input-actions">
          <text class="fas fa-image input-icon" @tap="chooseImage"></text>
          <text class="fas fa-face-smile input-icon" @tap="toggleEmojiPanel"></text>
          <button class="send-button" @click="sendMessage">发送</button>
        </view>
      </view>
      <view class="emoji-panel" v-if="showEmojiPanel">
        <view class="emoji-item" v-for="e in emojis" :key="e" @tap="addEmoji(e)">{{ e }}</view>
      </view>
      <view class="recording-indicator" v-if="isRecording">
        <text class="recording-text">正在录音...</text>
        <button class="stop-record-button" @tap="stopRecording">结束</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, onUnmounted } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { privateChatApi, fileApi, userApi } from '@/utils/api.js'
import request from '@/utils/request.js'

const chatInfo = ref({
  id: '',
  name: '加载中...',
  avatar: '',
  online: true
})

const userInfo = ref({
  avatar: 'https://ui-avatars.com/api/?name=我&background=3b82f6&color=fff&size=128',
  role: null
})

const messages = ref([])
const isLoading = ref(false)
const scrollTop = ref(0)
const lastMessageId = ref('')
const newMessageCount = ref(0)
const isAtBottom = ref(true)
const inputMessage = ref('')
const showEmojiPanel = ref(false)
const isRecording = ref(false)
const recorderReady = ref(false)
const recorder = ref(null)
const emojis = ref(['😀','�','😄','�','�','🥹','�','🤣','😊','�','🙂','🙃','😉','😌','�','🥰','😘','😗','😙','😚','😋','😛','😝','😜','🤪','🤨','🧐','🤓','😎','🥳','😏','😒','😞','😔','😟','😕','🙁','☹️','😣','😖','😫','😩','🥺','😭','😤','😠','😡','�','🤯','😳','🥵','🥶','😱','😨','😰','😥','😓','🤗','🤔','🫡','🤭','🫢','🫣','😶','😐','😑','😬','🙄','😯','😦','😧','😮','😲','🥱','😴','🤤','😪','😵','🤐','🤒','🤕','🤢','🤮','🤧','😷','🤠','👻','💀','☠️','👽','👾','🤖','🎃','😺','😸','😹','😻','😼','😽','🙀','😿','😾','👋','🤚','✋','🖐️','👌','🤏','✌️','🤞','🫰','🤟','🤘','🤙','👈','👉','👆','🖕','👇','☝️','👍','👎','✊','👊','🤛','🤜','👏','🙌','🫶','👐','🤲','🙏','💪','🦵','🦶','👣','👀','👁️','👅','👄','🫦','💋','❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💖','💗','💓','💞','�','💘','💝','💟','💔','❤️‍🔥','❤️‍�','💯','💢','💥','💫','🕳️','💦','�','🫧','�','⭐','🌟','✨','🎉','🎊','🎁'])

const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

const socket = ref(null)
const wsConnected = ref(false)

onShow(() => {
  if (chatInfo.value.id) {
    currentPage.value = 1
    hasMore.value = true
    loadMessages(1)
  }
})

onHide(() => {
  closeWebSocket()
})

onMounted(() => {
  try {
    const u = uni.getStorageSync('userInfo')
    if (u && u.id) {
      userInfo.value = {
        id: u.id,
        name: u.nickname || u.username || '我',
        avatar: u.avatar || u.avatarThumb || userInfo.value.avatar,
        role: u.role
      }
    }
  } catch (e) {}
  if (!userInfo.value.id || !userInfo.value.avatar) {
    userApi.getCurrentUser().then(res => {
      if (res && res.code === 200 && res.data) {
        userInfo.value = {
          id: res.data.id,
          name: res.data.nickname || res.data.username || '我',
          avatar: res.data.avatar || res.data.avatarThumb || userInfo.value.avatar,
          role: res.data.role
        }
        try { uni.setStorageSync('userInfo', res.data) } catch (e) {}
      }
    }).catch(() => {})
  }
  if (userInfo.value.role === undefined || userInfo.value.role === null) {
    try {
      const storedRole = uni.getStorageSync('userRole')
      if (storedRole) {
        userInfo.value.role = storedRole
      }
    } catch (e) {}
  }
  const pages = getCurrentPages()
  const currentPageIns = pages[pages.length - 1]
  const contactId = currentPageIns.options.id
  const contactName = currentPageIns.options.name
  
  if (contactId) {
    chatInfo.value.id = contactId
    if (contactName) {
      chatInfo.value.name = decodeURIComponent(contactName)
    }
    loadChatData()
    initWebSocket()
  } else {
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
  }
})

const themeStyle = computed(() => {
  const role = userInfo.value.role
  const isChild = role === 2 || role === '2' || role === 'child'
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})

const loadChatData = async () => {
  try {
    await loadMessages()
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
    loadDefaultData()
  }
}

const TIME_GAP_MINUTES = 5

const buildWsUrl = () => {
  let base = ''
  const httpBase = request.backendBaseUrl || request.baseUrl || ''
  if (httpBase) {
    if (httpBase.startsWith('https')) {
      base = httpBase.replace(/^https/, 'wss')
    } else if (httpBase.startsWith('http')) {
      base = httpBase.replace(/^http/, 'ws')
    } else {
      base = 'ws://localhost:8888'
    }
  } else if (typeof window !== 'undefined' && window.location) {
    const protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://'
    base = protocol + window.location.host
  } else {
    base = 'ws://localhost:8888'
  }
  return base + '/ws/chat'
}

const initWebSocket = () => {
  if (socket.value) {
    console.log('私聊 WebSocket 已存在，跳过重连')
    return
  }
  const baseUrl = buildWsUrl()
  console.log('私聊准备建立 WebSocket 连接，baseUrl=', baseUrl)
  const token = uni.getStorageSync('token')
  const options = {
    url: baseUrl,
    header: {}
  }
  if (token) {
    const sep = baseUrl.includes('?') ? '&' : '?'
    options.url = baseUrl + sep + 'accessToken=' + encodeURIComponent(token)
    options.header.accessToken = token
  }
  console.log('私聊 WebSocket 连接参数:', options)
  let ws
  // #ifdef H5
  ws = new WebSocket(options.url)
  // #endif
  // #ifndef H5
  ws = uni.connectSocket(options)
  // #endif
  socket.value = ws
  const handleOpen = () => {
    console.log('私聊 WebSocket 已连接:', options.url)
    wsConnected.value = true
  }
  const handleClose = () => {
    console.log('私聊 WebSocket 已关闭')
    wsConnected.value = false
    socket.value = null
  }
  const handleError = (err) => {
    console.log('私聊 WebSocket 发生错误', err)
    wsConnected.value = false
  }
  const handleMessage = (e) => {
    console.log('收到私聊 WebSocket 消息:', e && e.data)
    if (!e || !e.data) return
    let data
    try {
      data = JSON.parse(e.data)
    } catch {
      return
    }
    if (!data || data.type !== 'private') return
    if (String(data.chatId) !== String(chatInfo.value.id)) return
    const msg = data.message || {}
    const rawTime = msg.sendTime || msg.createTime || msg.time || new Date()
    const isImage = msg.type === true || msg.type === 1
    const m = {
      id: msg.id,
      type: isImage ? 'image' : 'text',
      content: msg.content,
      time: formatMessageTime(rawTime),
      timestamp: rawTime,
      isSelf: msg.senderId !== undefined ? String(msg.senderId) === String(userInfo.value.id) : false,
      showTime: true
    }
    messages.value.push(m)
    messages.value = applyMessageTimeGrouping(messages.value)
    if (isAtBottom.value) {
      lastMessageId.value = 'msg-' + m.id
      nextTick(() => { scrollTop.value = Date.now() })
    } else {
      newMessageCount.value = newMessageCount.value + 1
    }
  }
  // #ifdef H5
  if (ws) {
    ws.onopen = handleOpen
    ws.onclose = handleClose
    ws.onerror = handleError
    ws.onmessage = (event) => handleMessage({ data: event.data })
  }
  // #endif
  // #ifndef H5
  if (ws && typeof ws.onOpen === 'function') {
    ws.onOpen(handleOpen)
    ws.onClose(handleClose)
    ws.onError(handleError)
    ws.onMessage(handleMessage)
  } else if (ws) {
    ws.onopen = handleOpen
    ws.onclose = handleClose
    ws.onerror = handleError
    ws.onmessage = (event) => handleMessage({ data: event.data })
  }
  // #endif
}

const closeWebSocket = () => {
  if (socket.value) {
    try {
      socket.value.close()
    } catch (e) {}
    socket.value = null
    wsConnected.value = false
  }
}

const applyMessageTimeGrouping = (list) => {
  if (!Array.isArray(list)) return []
  let lastTs = 0
  const gap = TIME_GAP_MINUTES * 60 * 1000
  return list.map((m, index) => {
    const ts = m.timestamp ? new Date(m.timestamp).getTime() : NaN
    let show = false
    if (!lastTs || !ts || Number.isNaN(ts) || index === 0) {
      show = true
    } else if (ts - lastTs >= gap) {
      show = true
    }
    if (!Number.isNaN(ts)) {
      lastTs = ts
    }
    return { ...m, showTime: show }
  })
}

const loadMessages = async (page = 1) => {
  try {
    if (isLoading.value) return
    isLoading.value = true
    
    const response = await privateChatApi.getPrivateMessages(chatInfo.value.id, { page, size: pageSize.value })
    
    if (response.code === 200 && response.data) {
      let messageList = []
      let hasMoreData = false
      
      if (Array.isArray(response.data)) {
        messageList = response.data
      } else if (response.data.list) {
        messageList = response.data.list
        hasMoreData = response.data.total > page * pageSize.value
      } else if (response.data.records) {
        messageList = response.data.records
        hasMoreData = response.data.total > page * pageSize.value
      } else if (response.data.messages) {
        messageList = response.data.messages
        hasMoreData = (response.data.hasMore === true) || (response.data.total > page * pageSize.value)
        if (response.data.contactInfo) {
          chatInfo.value = {
            id: chatInfo.value.id,
            name: response.data.contactInfo.name || chatInfo.value.name,
            avatar: response.data.contactInfo.avatar || chatInfo.value.avatar,
            online: response.data.contactInfo.online ?? chatInfo.value.online
          }
        }
      }
      
      const isImageLike = (c) => {
        if (!c || typeof c !== 'string') return false
        const lowered = c.toLowerCase()
        return (
          lowered.startsWith('http') ||
          lowered.startsWith('blob:') ||
          lowered.endsWith('.png') ||
          lowered.endsWith('.jpg') ||
          lowered.endsWith('.jpeg') ||
          lowered.endsWith('.gif') ||
          lowered.endsWith('.webp')
        )
      }
      
      const newMessages = messageList.map(msg => {
        const contentStr = msg.content || ''
        const finalType = isImageLike(contentStr) ? 'image' : 'text'
        const rawTime = msg.sendTime || msg.createTime || msg.time || null
        return {
          id: msg.id,
          type: finalType,
          content: contentStr,
          time: formatMessageTime(rawTime),
          timestamp: rawTime,
          isSelf: msg.isSelf === true,
          showTime: true
        }
      })
        
      if (page === 1) {
        messages.value = applyMessageTimeGrouping(newMessages.reverse())
      } else {
        const merged = [...newMessages.reverse(), ...messages.value]
        messages.value = applyMessageTimeGrouping(merged)
      }
        
        hasMore.value = hasMoreData
        currentPage.value = page
        
        if (page === 1 && newMessages.length > 0) {
          lastMessageId.value = 'msg-' + newMessages[newMessages.length - 1].id
          nextTick(() => {
            scrollTop.value = Date.now()
          })
          isAtBottom.value = true
          newMessageCount.value = 0
        }
    } else {
      throw new Error(response.message || '获取消息失败')
    }
  } catch (error) {
    if (page === 1) {
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

const loadDefaultData = () => {
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

const formatMessageTime = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false 
    })
  } catch {
    return ''
  }
}

const loadMoreMessages = () => {
  if (isLoading.value || !hasMore.value) return
  loadMessages(currentPage.value + 1)
}

const handleScroll = (e) => {
  const detail = e && e.detail ? e.detail : e
  if (!detail) return
  const top = typeof detail.scrollTop === 'number' ? detail.scrollTop : 0
  const scrollHeight = typeof detail.scrollHeight === 'number' ? detail.scrollHeight : 0
  const clientHeight = typeof detail.clientHeight === 'number'
    ? detail.clientHeight
    : (typeof detail.height === 'number' ? detail.height : 0)
  if (!scrollHeight || !clientHeight) return
  const gap = scrollHeight - top - clientHeight
  const threshold = 80
  if (gap <= threshold) {
    if (!isAtBottom.value) {
      isAtBottom.value = true
      newMessageCount.value = 0
    }
  } else {
    isAtBottom.value = false
  }
}

const scrollToBottom = () => {
  if (!messages.value.length) return
  const last = messages.value[messages.value.length - 1]
  lastMessageId.value = 'msg-' + last.id
  newMessageCount.value = 0
  isAtBottom.value = true
  nextTick(() => {
    scrollTop.value = Date.now()
  })
}

const toggleEmojiPanel = () => {
  showEmojiPanel.value = !showEmojiPanel.value
  nextTick(() => { scrollTop.value = Date.now() })
}

const addEmoji = (e) => {
  inputMessage.value = (inputMessage.value || '') + e
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  console.log('私聊发送消息前，wsConnected=', wsConnected.value, 'socket 存在=', !!socket.value)
  let content = ''
  
  try {
    content = inputMessage.value.trim()
    const now = new Date()
    const last = messages.value.length > 0 ? messages.value[messages.value.length - 1] : null
    let showTime = true
    if (last && last.timestamp) {
      const lastTs = new Date(last.timestamp).getTime()
      if (!Number.isNaN(lastTs) && now.getTime() - lastTs < TIME_GAP_MINUTES * 60 * 1000) {
        showTime = false
      }
    }
    const tempMessage = {
      id: Date.now(),
      type: 'text',
      content: content,
      time: formatMessageTime(now),
      timestamp: now,
      isSelf: true,
      showTime,
      sending: true
    }
    messages.value.push(tempMessage)
    lastMessageId.value = 'msg-' + tempMessage.id
    inputMessage.value = ''
    nextTick(() => { scrollTop.value = Date.now() })
    
    const response = await privateChatApi.sendPrivateMessage({
      recvId: chatInfo.value.id,
      content: content,
      type: 0
    })
    
    if (response.code === 200 && response.data) {
      const idx = messages.value.findIndex(msg => msg.id === tempMessage.id)
      if (idx !== -1) {
        messages.value[idx] = { ...tempMessage, id: response.data.id, sending: false }
        lastMessageId.value = 'msg-' + response.data.id
        nextTick(() => { scrollTop.value = Date.now() })
      }
    } else {
      throw new Error(response.message || '发送失败')
    }
  } catch (error) {
    const idx = messages.value.findIndex(msg => msg.sending)
    if (idx !== -1) messages.value.splice(idx, 1)
    uni.showToast({ title: error.message || '发送失败', icon: 'none' })
    inputMessage.value = content
  }
}

const goBack = () => { uni.navigateBack() }

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      const tempId = Date.now()
      const now = new Date()
      const last = messages.value.length > 0 ? messages.value[messages.value.length - 1] : null
      let showTime = true
      if (last && last.timestamp) {
        const lastTs = new Date(last.timestamp).getTime()
        if (!Number.isNaN(lastTs) && now.getTime() - lastTs < TIME_GAP_MINUTES * 60 * 1000) {
          showTime = false
        }
      }
      const tempMessage = {
        id: tempId,
        type: 'image',
        content: tempFilePath,
        time: formatMessageTime(now),
        timestamp: now,
        isSelf: true,
        showTime,
        sending: true
      }
      messages.value.push(tempMessage)
      lastMessageId.value = 'msg-' + tempId
      nextTick(() => { scrollTop.value = Date.now() })
      
      try {
        const uploadRes = await fileApi.uploadImage(tempFilePath, 'chat/private')
        if (uploadRes.code === 200 && uploadRes.data) {
          const imageUrl = uploadRes.data
          const response = await privateChatApi.sendPrivateMessage({
             recvId: chatInfo.value.id,
             content: imageUrl,
             type: 2
          })
          if (response.code === 200 && response.data) {
            const idx = messages.value.findIndex(msg => msg.id === tempId)
            if (idx !== -1) {
              messages.value[idx] = { ...tempMessage, id: response.data.id, content: imageUrl, sending: false }
              lastMessageId.value = 'msg-' + response.data.id
              nextTick(() => { scrollTop.value = Date.now() })
            }
          } else {
            throw new Error(response.message || '发送失败')
          }
        } else {
          throw new Error(uploadRes.message || '图片上传失败')
        }
      } catch (error) {
        const idx = messages.value.findIndex(msg => msg.id === tempId)
        if (idx !== -1) messages.value.splice(idx, 1)
        uni.showToast({ title: error.message || '发送失败', icon: 'none' })
      }
    }
  })
}

const previewImage = (url) => {
  uni.previewImage({ urls: [url] })
}

const openPrivateSettings = () => {
  try {
    const id = chatInfo.value.id
    const name = encodeURIComponent(chatInfo.value.name || '')
    if (!id) {
      uni.showToast({ title: '缺少好友ID', icon: 'none' })
      return
    }
    uni.navigateTo({ url: `/pages/common/private-settings?id=${id}&name=${name}` })
  } catch (e) {
    uni.showToast({ title: '无法打开设置', icon: 'none' })
  }
}

const ensureRecorder = () => {
  if (recorderReady.value && recorder.value) return true
  let manager = null
  try {
    if (typeof uni.getRecorderManager === 'function') {
      manager = uni.getRecorderManager()
    }
  } catch (e) {
    manager = null
  }
  if (!manager || typeof manager.onStop !== 'function' || typeof manager.start !== 'function') {
    uni.showToast({ title: '当前平台不支持语音录制', icon: 'none' })
    return false
  }
  recorder.value = manager
  recorder.value.onStop((res) => {
    isRecording.value = false
    if (res && res.tempFilePath) {
      uni.showToast({ title: '录音完成', icon: 'none' })
    }
  })
  recorder.value.onError(() => {
    isRecording.value = false
    uni.showToast({ title: '录音失败', icon: 'none' })
  })
  recorderReady.value = true
  return true
}

const toggleRecording = () => {
  if (!isRecording.value) {
    startRecording()
  } else {
    stopRecording()
  }
}

const startRecording = () => {
  if (!ensureRecorder()) return
  try {
    recorder.value.start({ format: 'mp3' })
    isRecording.value = true
  } catch (e) {
    isRecording.value = false
    uni.showToast({ title: '录音启动失败', icon: 'none' })
  }
}

const stopRecording = () => {
  if (!recorderReady.value || !recorder.value) {
    isRecording.value = false
    return
  }
  try {
    recorder.value.stop()
  } catch (e) {
    isRecording.value = false
  }
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container { min-height: 100vh; background-color: #f3f4f6; position: relative; }
.header { position: fixed; top: 0; left: 0; right: 0; background-color: var(--primary-color); z-index: 50; }
.header-content { display: flex; justify-content: space-between; align-items: center; padding: 12px 24px; height: 44px; }
.header-left { display: flex; align-items: center; gap: 12px; }
.header-left .fas { color: #ffffff; font-size: 20px; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-size: 18px; font-weight: 600; color: #ffffff; }
.user-status { font-size: 12px; color: rgba(255, 255, 255, 0.8); }
.header-actions .fas { color: #ffffff; font-size: 20px; }
.chat-container { height: calc(100vh - 140px); margin-top: 44px; padding: 16px 8px; padding-bottom: 120px; }
.loading-more { text-align: center; padding: 20rpx 0; color: #9ca3af; }
.time-stamp { text-align: center; font-size: 12px; color: #9ca3af; margin-bottom: 8px; }
.message-group { display: flex; flex-direction: column; margin-bottom: 24px; }
.message-row { display: flex; padding: 0 4px; }
.message-row.justify-end { justify-content: flex-end; padding-right: 8px; }
.avatar { width: 32px; height: 32px; border-radius: 50%; overflow: hidden; flex-shrink: 0; }
.avatar image { width: 100%; height: 100%; object-fit: cover; }
.message-bubble { margin-left: 6px; max-width: 70%; position: relative; }
.message-bubble::before { content: ''; position: absolute; left: -8px; top: 12px; width: 0; height: 0; border: 8px solid transparent; border-right-color: #ffffff; }
.message-bubble.user { margin-right: 8px; margin-left: 0; }
.message-bubble.user::before { left: auto; right: -8px; border-right-color: transparent; border-left-color: #3b82f6; }
.message-content { background-color: #ffffff; padding: 16px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); }
.message-content.user { background-color: #3b82f6; }
.message-text { color: #1f2937; font-size: 14px; line-height: 1.4; }
.message-content.user .message-text { color: #ffffff; }
.image-message { max-width: 200px; border-radius: 8px; }
.message-input { position: fixed; bottom: 0; left: 0; right: 0; background-color: #ffffff; padding: 12px 8px; border-top: 1px solid #e5e7eb; }
.input-container { display: flex; align-items: center; background-color: #f3f4f6; border-radius: 24px; padding: 4px 8px; }
.input-icon { color: #6b7280; font-size: 20px; padding: 8px; }
.message-field { flex: 1; background: transparent; border: none; padding: 8px; font-size: 14px; }
.input-actions { display: flex; align-items: center; }
.send-button { background-color: #3b82f6; color: #ffffff; padding: 6px 16px; border-radius: 16px; margin-left: 8px; font-size: 14px; }
.emoji-panel { display: flex; flex-wrap: wrap; gap: 8px; padding: 8px; background-color: #ffffff; border-top: 1px solid #e5e7eb; max-height: 160px; overflow-y: auto; }
.emoji-item { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; font-size: 20px; border-radius: 8px; background-color: #f3f4f6; }
.recording-indicator { display: flex; align-items: center; justify-content: space-between; padding: 8px; background-color: #fff7ed; border-top: 1px solid #fde68a; }
.recording-text { color: #ea580c; font-size: 14px; }
.stop-record-button { background-color: #ef4444; color: #ffffff; padding: 6px 12px; border-radius: 12px; font-size: 12px; }
.new-message-tip { position: fixed; left: 50%; transform: translateX(-50%); bottom: 80px; background-color: rgba(37, 99, 235, 0.95); color: #ffffff; padding: 6px 12px; border-radius: 16px; font-size: 12px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15); z-index: 60; }
.new-message-text { color: #ffffff; }
</style>

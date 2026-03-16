<template>
  <view class="container" :style="themeStyle">
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
          <text class="fas fa-ellipsis-v" @click="openGroupSettings"></text>
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
            <view v-if="message.type === 'text'" class="message-text">
              <text 
                v-for="(part, index) in parseMentions(message.content)" 
                :key="index"
                :class="part.isMention ? 'mention-text' : ''"
              >{{ part.text }}</text>
            </view>
            <image 
              v-else-if="message.type === 'image'" 
                  class="image-message" 
                  :src="message.content"
                  mode="widthFix"
                  @tap="previewImage(message.content)"
                ></image>
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
              <image class="avatar" :src="message.avatar || userInfo.avatar" mode="aspectFill"></image>
              <text class="sender-name">{{ message.senderName }}</text>
            </view>
          </template>
          <template v-else>
            <view class="avatar-wrapper">
              <image class="avatar" :src="message.avatar" mode="aspectFill"></image>
              <text class="sender-name">{{ message.senderName }}</text>
            </view>
            <view class="message-bubble">
              <view class="message-content">
                <view v-if="message.type === 'text'" class="message-text">
                  <text 
                    v-for="(part, index) in parseMentions(message.content)" 
                    :key="index"
                    :class="part.isMention ? 'mention-text' : ''"
                  >{{ part.text }}</text>
                </view>
                <image 
                  v-else-if="message.type === 'image'" 
                  class="image-message" 
                  :src="message.content"
                  mode="widthFix"
                  @tap="previewImage(message.content)"
                ></image>
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

    <view 
      v-if="newMessageCount > 0" 
      class="new-message-tip" 
      @tap="scrollToBottom"
    >
      <text class="new-message-text">{{ newMessageCount }}条新消息，点击查看</text>
    </view>

    <view class="message-input">
      <view class="ai-mention-tip">
        <text class="fas fa-robot tip-icon"></text>
        <text class="tip-text">在群聊中输入 @阅读助手 可以向AI机器人提问</text>
      </view>
      <view class="input-container">
        <text class="fas fa-microphone input-icon" @tap="toggleRecording"></text>
        <input 
          type="text" 
          v-model="inputMessage"
          placeholder="输入消息..."
          class="message-field"
          @input="onInputChange"
          @confirm="sendMessage"
        />
        <view class="input-actions">
          <text class="fas fa-image input-icon" @tap="chooseImage"></text>
          <text class="fas fa-face-smile input-icon" @tap="toggleEmojiPanel"></text>
          <button class="send-button" @click="sendMessage">发送</button>
        </view>
      </view>

      <view class="mention-panel" v-if="showMentionPanel">
        <scroll-view scroll-y="true" class="mention-list">
          <view 
            class="mention-item" 
            v-for="member in mentionCandidates" 
            :key="member.id"
            @tap="selectMention(member)"
          >
            <image class="mention-avatar" :src="member.avatar" mode="aspectFill"></image>
            <view class="mention-info">
              <text class="mention-name">@{{ member.name }}</text>
              <text class="mention-role" v-if="member.isAi">阅读助手</text>
              <text class="mention-role" v-else>{{ member.role }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="emoji-panel" v-if="showEmojiPanel">
        <view class="emoji-item" v-for="e in emojis" :key="e" @tap="addEmoji(e)">{{ e }}</view>
      </view>

      <view class="recording-indicator" v-if="isRecording">
        <text class="recording-text">正在录音...</text>
        <button class="stop-record-button" @tap="stopRecording">结束</button>
      </view>

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
import { ref, onMounted, nextTick, computed, onUnmounted } from 'vue'
import { groupApi, fileApi, userApi, aiApi } from '@/utils/api.js'
import request from '@/utils/request.js'
import { onShow, onHide } from '@dcloudio/uni-app'

const groupInfo = ref({
  id: '',
  name: '加载中...',
  memberCount: 0
})

const userInfo = ref({
  id: '',
  name: '我',
  avatar: 'https://ui-avatars.com/api/?name=我&background=3b82f6&color=fff&size=128',
  role: null
})

const groupMembers = ref([])
const myGroupNickName = ref('')

const messages = ref([])
const isLoading = ref(false)
const scrollTop = ref(0)
const lastMessageId = ref('')
const newMessageCount = ref(0)
const isAtBottom = ref(true)
const inputMessage = ref('')
const showMorePanel = ref(false)
const showMemberPanel = ref(false)
const showEmojiPanel = ref(false)
const isRecording = ref(false)
const recorderReady = ref(false)
const recorder = ref(null)
const emojis = ref(['😀','😃','😄','😁','😆','🥹','😂','🤣','😊','😇','🙂','🙃','😉','😌','😍','🥰','😘','😗','😙','😚','😋','😛','😝','😜','🤪','🤨','🧐','🤓','😎','🥳','😏','😒','😞','😔','😟','😕','🙁','☹️','😣','😖','😫','😩','🥺','😭','😤','😠','😡','🤬','🤯','😳','🥵','🥶','😱','😨','😰','😥','😓','🤗','🤔','🫡','🤭','🫢','🫣','😶','😐','😑','😬','🙄','😯','😦','😧','😮','😲','🥱','😴','🤤','😪','😵','🤐','🤒','🤕','🤢','🤮','🤧','😷','🤠','👻','💀','☠️','👽','👾','🤖','🎃','😺','😸','😹','😻','😼','😽','🙀','😿','😾','👋','🤚','✋','🖐️','👌','🤏','✌️','🤞','🫰','🤟','🤘','🤙','👈','👉','👆','🖕','👇','☝️','👍','👎','✊','👊','🤛','🤜','👏','🙌','🫶','👐','🤲','🙏','💪','🦵','🦶','👣','👀','👁️','👅','👄','🫦','💋','❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💖','💗','💓','💞','💕','💘','💝','💟','💔','❤️‍🔥','❤️‍🩹','💯','💢','💥','💫','🕳️','💦','💨','🫧','🔥','⭐','🌟','✨','🎉','🎊','🎁'])

const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

const showMentionPanel = ref(false)
const mentionKeyword = ref('')

const socket = ref(null)
const wsConnected = ref(false)

const aiAssistant = computed(() => ({
  id: 'ai-assistant',
  name: '阅读助手',
  avatar: '/static/images/avatars/AI机器人.svg',
  role: 'AI助手',
  isAi: true
}))

const mentionCandidates = computed(() => {
  const base = Array.isArray(groupMembers.value) ? groupMembers.value : []
  const list = [aiAssistant.value, ...base]
  if (!mentionKeyword.value) {
    return list
  }
  const kw = mentionKeyword.value.toLowerCase()
  return list.filter(m => (m.name || '').toLowerCase().includes(kw))
})

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const groupId = currentPage.options.id
  const groupName = currentPage.options.name
  try {
    const u = uni.getStorageSync('userInfo')
    if (u && u.id) {
      userInfo.value.id = u.id
      userInfo.value.name = u.nickname || u.username || userInfo.value.name
      userInfo.value.avatar = u.avatar || u.avatarThumb || userInfo.value.avatar
      if (u.role !== undefined && u.role !== null) {
        userInfo.value.role = u.role
      }
    }
    if (!userInfo.value.role) {
      const storedRole = uni.getStorageSync('userRole')
      if (storedRole) {
        userInfo.value.role = storedRole
      }
    }
  } catch (e) {}
  if (!userInfo.value.id || !userInfo.value.avatar) {
    userApi.getCurrentUser().then(res => {
      if (res && res.code === 200 && res.data) {
        userInfo.value.id = res.data.id
        userInfo.value.name = res.data.nickname || res.data.username || userInfo.value.name
        userInfo.value.avatar = res.data.avatar || res.data.avatarThumb || userInfo.value.avatar
        if (res.data.role !== undefined && res.data.role !== null) {
          userInfo.value.role = res.data.role
        }
        try { uni.setStorageSync('userInfo', res.data) } catch (e) {}
      }
    }).catch(() => {})
  }
  
  if (groupId) {
    groupInfo.value.id = groupId
    if (groupName) {
      groupInfo.value.name = decodeURIComponent(groupName)
    }
    
    loadGroupData()
    initWebSocket()
  } else {
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
  }
})

onShow(() => {
  if (groupInfo.value.id) {
    loadMessages(1)
    loadMyMemberInfo()
  }
})

onHide(() => {
  closeWebSocket()
})

onUnmounted(() => {
  closeWebSocket()
})

const themeStyle = computed(() => {
  const role = userInfo.value.role
  const isChild = role === 2 || role === 'child'
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})

const loadGroupData = async () => {
  try {
    await loadMessages()
    await loadMyMemberInfo()
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
    loadDefaultData()
  }
}

const loadMyMemberInfo = async () => {
  try {
    const res = await groupApi.getMyMemberInfo(groupInfo.value.id)
    if (res && res.code === 200 && res.data) {
      const me = res.data
      myGroupNickName.value = me.userNickName || me.user_nick_name || me.remarkNickName || me.remark_nick_name || userInfo.value.name
    }
  } catch (e) {}
}

const loadMembers = async () => {
  try {
    const res = await groupApi.getGroupMembers(groupInfo.value.id, {})
    if (res && res.code === 200 && Array.isArray(res.data)) {
      groupMembers.value = res.data.map(m => ({
        id: m.id,
        name: m.name || '成员',
        avatar: m.avatar || ('https://ui-avatars.com/api/?name=' + encodeURIComponent(m.name || '成员') + '&background=3b82f6&color=fff&size=128'),
        role: m.role || '成员'
      }))
      groupInfo.value.memberCount = groupMembers.value.length
    } else {
      uni.showToast({ title: res?.message || '获取成员失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '获取成员失败', icon: 'none' })
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
    console.log('群聊 WebSocket 已存在，跳过重连')
    return
  }
  const baseUrl = buildWsUrl()
  console.log('群聊准备建立 WebSocket 连接，baseUrl=', baseUrl)
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
  console.log('群聊 WebSocket 连接参数:', options)
  let ws
  // #ifdef H5
  ws = new WebSocket(options.url)
  // #endif
  // #ifndef H5
  ws = uni.connectSocket(options)
  // #endif
  socket.value = ws
  const handleOpen = () => {
    console.log('WebSocket 已连接:', options.url)
    wsConnected.value = true
  }
  const handleClose = () => {
    console.log('WebSocket 已关闭')
    wsConnected.value = false
    socket.value = null
  }
  const handleError = (err) => {
    console.log('WebSocket 发生错误', err)
    wsConnected.value = false
  }
  const handleMessage = (e) => {
    console.log('收到 WebSocket 消息:', e && e.data)
    if (!e || !e.data) return
    let data
    try {
      data = JSON.parse(e.data)
    } catch {
      return
    }
    if (!data || data.type !== 'group') return
    if (String(data.chatId) !== String(groupInfo.value.id)) return
    const msg = data.message || {}
    const rawTime = msg.sendTime || msg.createTime || msg.time || new Date()
    const isImage = msg.type === true || msg.type === 1 || msg.msgType === 'image'
    const m = {
      id: msg.id,
      type: isImage ? 'image' : 'text',
      content: msg.content,
      time: formatMessageTime(rawTime),
      timestamp: rawTime,
      isSelf: msg.senderId !== undefined ? String(msg.senderId) === String(userInfo.value.id) : false,
      showTime: true,
      senderName: msg.senderName || msg.sender || '未知用户',
      avatar: msg.avatar || ('https://ui-avatars.com/api/?name=' + encodeURIComponent(msg.senderName || 'U'))
    }
    messages.value.push(m)
    messages.value = applyMessageTimeGrouping(messages.value)
    if (isAtBottom.value) {
      lastMessageId.value = 'msg-' + m.id
      nextTick(() => {
        scrollTop.value = Date.now()
      })
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
    
    const response = await groupApi.getGroupMessages(groupInfo.value.id, { page, size: pageSize.value })
    
    if (response.code === 200 && response.data) {
      let messageList = []
      let hasMoreData = false
      const data = response.data
      if (Array.isArray(data)) {
        messageList = data
      } else if (data.messages) {
        messageList = data.messages
        hasMoreData = !!data.hasMore
        if (data.groupInfo) {
          groupInfo.value.name = data.groupInfo.name || groupInfo.value.name
          groupInfo.value.memberCount = data.groupInfo.memberCount || groupInfo.value.memberCount
        }
        if (Array.isArray(data.members)) {
          groupMembers.value = data.members.map(m => ({
            id: m.id,
            name: m.name || '成员',
            avatar: m.avatar || ('https://ui-avatars.com/api/?name=' + encodeURIComponent(m.name || '成员') + '&background=3b82f6&color=fff&size=128'),
            role: m.role || '成员'
          }))
        }
      } else if (data.list) {
        messageList = data.list
        hasMoreData = data.total > page * pageSize.value
      } else if (data.records) {
        messageList = data.records
        hasMoreData = data.total > page * pageSize.value
      }
      
      const newMessages = messageList.map(msg => {
        const rawTime = msg.sendTime || msg.createTime || msg.time || null
        return {
          id: msg.id,
          type: (msg.type === true || msg.type === 1 || msg.msgType === 'image') ? 'image' : 'text',
          content: msg.content,
          time: formatMessageTime(rawTime),
          timestamp: rawTime,
          isSelf: typeof msg.isSelf === 'boolean' ? msg.isSelf : (msg.senderId !== undefined ? (String(msg.senderId) === String(userInfo.value.id)) : false),
          showTime: true,
          senderName: msg.senderName || msg.sender || '未知用户',
          avatar: msg.avatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(msg.senderName || 'U')
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

const onInputChange = (e) => {
  const value = e.detail && typeof e.detail.value === 'string' ? e.detail.value : ''
  inputMessage.value = value
  const cursor = e.detail && typeof e.detail.cursor === 'number' ? e.detail.cursor : value.length
  const textBeforeCursor = value.slice(0, cursor)
  const atIndex = textBeforeCursor.lastIndexOf('@')
  if (atIndex !== -1) {
    const afterAt = textBeforeCursor.slice(atIndex + 1)
    if (!afterAt.includes(' ')) {
      showMentionPanel.value = true
      mentionKeyword.value = afterAt
      return
    }
  }
  showMentionPanel.value = false
  mentionKeyword.value = ''
}

const selectMention = (member) => {
  const value = inputMessage.value || ''
  const atIndex = value.lastIndexOf('@')
  const name = member && member.name ? member.name : ''
  const mentionText = '@' + name + ' '
  if (atIndex === -1) {
    inputMessage.value = value + mentionText
  } else {
    const before = value.slice(0, atIndex)
    inputMessage.value = before + mentionText
  }
  showMentionPanel.value = false
  mentionKeyword.value = ''
}

const sendMessage = async () => {
  let content = inputMessage.value.trim()
  if (!content) return
  try {
    console.log('群聊发送消息前，wsConnected=', wsConnected.value, 'socket 存在=', !!socket.value)
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
      senderName: myGroupNickName.value || userInfo.value.name,
      avatar: userInfo.value.avatar,
      sending: true
  }
  messages.value.push(tempMessage)
  lastMessageId.value = 'msg-' + tempMessage.id
    inputMessage.value = ''
    showMentionPanel.value = false
    mentionKeyword.value = ''
    nextTick(() => {
      scrollTop.value = Date.now()
    })
    const response = await groupApi.sendGroupMessage({
      groupId: groupInfo.value.id,
      content: content,
      type: 0
    })
    if (response.code === 200 && response.data) {
      const messageIndex = messages.value.findIndex(msg => msg.id === tempMessage.id)
      if (messageIndex !== -1) {
        messages.value[messageIndex] = {
          ...tempMessage,
          id: response.data.id,
          sending: false,
          senderName: response.data.senderName || tempMessage.senderName,
          avatar: response.data.avatar || tempMessage.avatar
        }
        lastMessageId.value = 'msg-' + response.data.id
        nextTick(() => {
          scrollTop.value = Date.now()
        })
      }
      if (content.indexOf('@阅读助手') !== -1) {
        triggerAiAssistant(response.data)
      }
    } else {
      throw new Error(response.message || '发送失败')
    }
  } catch (error) {
    const messageIndex = messages.value.findIndex(msg => msg.sending)
    if (messageIndex !== -1) {
      messages.value.splice(messageIndex, 1)
    }
    uni.showToast({
      title: error.message || '发送失败',
      icon: 'none'
    })
    inputMessage.value = content
  }
}

const triggerAiAssistant = async (serverMessage) => {
  try {
    const res = await aiApi.groupChat({
      groupId: groupInfo.value.id,
      messageId: serverMessage.id
    })
    if (res && res.code === 200 && res.data) {
      const msg = res.data
      const rawTime = msg.sendTime || new Date()
      const aiMsg = {
        id: msg.id,
        type: msg.type === true || msg.type === 1 ? 'image' : 'text',
        content: msg.content,
        time: formatMessageTime(rawTime),
        timestamp: rawTime,
        isSelf: false,
        showTime: true,
        senderName: msg.senderName || 'AI阅读助手',
        avatar: msg.avatar || '/static/images/avatars/AI机器人.svg'
      }
      messages.value.push(aiMsg)
      messages.value = applyMessageTimeGrouping(messages.value)
      if (isAtBottom.value) {
        lastMessageId.value = 'msg-' + aiMsg.id
        nextTick(() => {
          scrollTop.value = Date.now()
        })
      } else {
        newMessageCount.value = newMessageCount.value + 1
      }
    }
  } catch (e) {
    console.error('调用AI助手失败', e)
  }
}

const showMembers = async () => {
  await loadMembers()
  showMemberPanel.value = true
}

const goBack = () => {
  uni.navigateBack()
}

const showMore = () => {
  showMorePanel.value = !showMorePanel.value
}

const toggleEmojiPanel = () => {
  showEmojiPanel.value = !showEmojiPanel.value
  nextTick(() => { scrollTop.value = Date.now() })
}

const addEmoji = (e) => {
  inputMessage.value = (inputMessage.value || '') + e
}

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
        senderName: myGroupNickName.value || userInfo.value.name,
        avatar: userInfo.value.avatar,
        sending: true
      }
      messages.value.push(tempMessage)
      lastMessageId.value = 'msg-' + tempId
      nextTick(() => {
        scrollTop.value = Date.now()
      })
      
      try {
        const uploadRes = await fileApi.uploadImage(tempFilePath, 'chat/group')
        
        if (uploadRes.code === 200 && uploadRes.data) {
          const imageUrl = typeof uploadRes.data === 'string' 
            ? uploadRes.data 
            : (uploadRes.data.originUrl || uploadRes.data.url || uploadRes.data.thumbUrl)
          
          if (!imageUrl || typeof imageUrl !== 'string') {
            throw new Error('图片地址无效')
          }
          
          const response = await groupApi.sendGroupMessage({
            groupId: groupInfo.value.id,
            content: imageUrl,
            type: 1
          })
          
          if (response.code === 200 && response.data) {
            const messageIndex = messages.value.findIndex(msg => msg.id === tempId)
            if (messageIndex !== -1) {
              messages.value[messageIndex] = {
                ...tempMessage,
                id: response.data.id,
                content: imageUrl,
                sending: false,
                senderName: response.data.senderName || tempMessage.senderName,
                avatar: response.data.avatar || tempMessage.avatar
              }
              lastMessageId.value = 'msg-' + response.data.id
              nextTick(() => {
                scrollTop.value = Date.now()
              })
            }
          } else {
            throw new Error(response.message || '发送失败')
          }
        } else {
          throw new Error(uploadRes.message || '图片上传失败')
        }
      } catch (error) {
        const messageIndex = messages.value.findIndex(msg => msg.id === tempId)
        if (messageIndex !== -1) {
          messages.value.splice(messageIndex, 1)
        }
        uni.showToast({
          title: error.message || '发送失败',
          icon: 'none'
        })
      }
    }
  })
}

const previewImage = (url) => {
  uni.previewImage({
    urls: [url]
  })
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

const startReading = () => {
  uni.navigateTo({
    url: '/pages/parent/reading/reading'
  })
}

const shareProgress = () => {
  const newMessage = {
    id: messages.value.length + 1,
    type: 'reading-progress',
    bookName: '小王子',
    chapter: 3,
    time: formatMessageTime(new Date()),
    isSelf: true,
    showTime: true,
    senderName: myGroupNickName.value || userInfo.value.name,
    avatar: userInfo.value.avatar
  }
  messages.value.push(newMessage)
  
  nextTick(() => {
    scrollTop.value = Date.now()
  })
}

const viewReadingProgress = (message) => {
  uni.navigateTo({
    url: `/pages/parent/reading/progress?book=${message.bookName}`
  })
}

const openAIChat = () => {
  uni.switchTab({
    url: '/pages/parent/ai-chat/ai-chat'
  })
}

const openGroupSettings = () => {
  const name = encodeURIComponent(groupInfo.value.name || '')
  uni.navigateTo({
    url: `/pages/common/group-settings?id=${groupInfo.value.id}&name=${name}`
  })
}

const parseMentions = (content) => {
  if (!content) return []
  const result = []
  const text = String(content)
  const regex = /@[^@\s]{1,20}/g
  let lastIndex = 0
  let match
  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      result.push({ text: text.slice(lastIndex, match.index), isMention: false })
    }
    result.push({ text: match[0], isMention: true })
    lastIndex = match.index + match[0].length
  }
  if (lastIndex < text.length) {
    result.push({ text: text.slice(lastIndex), isMention: false })
  }
  return result
}
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
  background-color: var(--primary-color);
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
  padding: 0 2px;
}

.message-row.justify-end {
  justify-content: flex-end;
  padding-right: 8px;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 36px;
  flex-shrink: 0;
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
  display: flex;
  flex-wrap: wrap;
}

.message-content.user .message-text {
  color: #ffffff;
}

.mention-text {
  color: #2563eb;
  font-weight: 600;
}

.message-content.user .mention-text {
  color: #facc15;
}

.image-message {
  max-width: 200px;
  border-radius: 8px;
}

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

.message-input {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 6px 8px 12px;
  border-top: 1px solid #e5e7eb;
}

.ai-mention-tip {
  display: flex;
  align-items: center;
  padding: 4px 8px 6px;
}

.tip-icon {
  color: #8b5cf6;
  font-size: 16px;
  margin-right: 4px;
}

.tip-text {
  font-size: 12px;
  color: #6b7280;
}

.emoji-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  background-color: #ffffff;
  border-top: 1px solid #e5e7eb;
  max-height: 160px;
  overflow-y: auto;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  border-radius: 8px;
  background-color: #f3f4f6;
}

.recording-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px;
  background-color: #fff7ed;
  border-top: 1px solid #fde68a;
}

.recording-text {
  color: #ea580c;
  font-size: 14px;
}

.stop-record-button {
  background-color: #ef4444;
  color: #ffffff;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.new-message-tip {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  bottom: 80px;
  background-color: rgba(37, 99, 235, 0.95);
  color: #ffffff;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  z-index: 60;
}

.new-message-text {
  color: #ffffff;
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

.mention-panel {
  margin-top: 8px;
  max-height: 180px;
  background-color: #ffffff;
  border-top: 1px solid #e5e7eb;
}

.mention-list {
  max-height: 180px;
}

.mention-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
}

.mention-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.mention-info {
  display: flex;
  flex-direction: column;
}

.mention-name {
  font-size: 14px;
  color: #111827;
}

.mention-role {
  font-size: 12px;
  color: #9ca3af;
}

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

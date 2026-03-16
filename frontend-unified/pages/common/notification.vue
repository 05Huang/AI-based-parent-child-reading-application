 <template>
  <view class="container" :style="themeStyle">
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <text class="fas fa-chevron-left back-icon" @click="goBack"></text>
        </view>
        <text class="header-title">消息</text>
        <view class="header-right">
          <view class="icon-btn" @click="onAddActionClick">
            <text class="fas fa-plus"></text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view scroll-y="true" class="main-content">
      <view v-if="loading" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else class="chat-list">
        <view 
          class="chat-item" 
          v-for="chat in chatList" 
          :key="chat.id + '_' + chat.type"
          @click="navigateToChat(chat)"
        >
          <view class="chat-avatar-wrapper">
            <view class="chat-avatar" :class="getChatAvatarClass(chat.type)">
              <image v-if="chat.type !== 'ai'" :src="chat.avatar" mode="aspectFill"></image>
              <text v-else class="fas fa-robot"></text>
            </view>
            <view v-if="chat.unread > 0" class="unread-dot">{{ chat.unread }}</view>
          </view>
          
          <view class="chat-content">
            <view class="chat-header">
              <text class="chat-name">{{ chat.name }}</text>
              <text class="chat-time">{{ formatTime(chat.lastTime) }}</text>
            </view>
            <view class="chat-footer">
              <text class="chat-message">{{ chat.lastMessage || '暂无消息' }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { chatApi, userApi } from '@/utils/api.js'

const chatList = ref([])
const loading = ref(true)
const userRole = ref(null)
 
const formatTime = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
    if (messageDate.getTime() === today.getTime()) {
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
    } else if (messageDate.getTime() === today.getTime() - 24 * 60 * 60 * 1000) {
      return '昨天'
    } else {
      return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
    }
  } catch (error) {
    return ''
  }
}

const buildPreviewMessage = (message) => {
  if (!message) return ''
  let text = String(message)
  text = text.replace(/<[^>]+>/g, '')
  text = text.replace(/[\r\n]+/g, ' ')
  text = text.replace(/\s+/g, ' ').trim()
  const maxLen = 40
  if (text.length > maxLen) {
    return text.slice(0, maxLen) + '...'
  }
  return text
}

onMounted(() => {
  loadUserRole()
  loadChatList()
})

onShow(() => {
  loadUserRole()
  loadChatList()
})

const loadUserRole = async () => {
  try {
    const res = await userApi.getCurrentUser()
    if (res && res.code === 200 && res.data) {
      userRole.value = res.data.role
    }
  } catch (e) {}
}

const loadChatList = async () => {
  try {
    loading.value = true
    
    const response = await chatApi.getChatList()
    const groupsRes = await chatApi.getGroupChats()

    if (response.code === 200 && response.data) {
      const baseList = (response.data.chatList || []).map(item => ({
        ...item,
        lastMessage: buildPreviewMessage(item.lastMessage || '')
      }))
      const key = (item) => `${item.id}_${item.type || 'group'}`
      const map = new Map()
      baseList.forEach(item => map.set(key(item), item))

      if (groupsRes && groupsRes.code === 200 && Array.isArray(groupsRes.data)) {
        groupsRes.data.forEach(g => {
          const item = {
            id: g.id,
            type: 'group',
            name: g.name || g.groupName,
            avatar: g.avatar,
            lastMessage: buildPreviewMessage(g.lastMessage || ''),
            lastTime: g.lastTime || g.createdTime,
            unread: g.unread || 0,
            memberCount: g.memberCount,
            pinned: !!g.pinned
          }
          const k = key(item)
          const existed = map.get(k)
          if (existed) {
            map.set(k, { ...existed, ...item })
          } else {
            map.set(k, item)
          }
        })
      }

      chatList.value = Array.from(map.values()).sort((a, b) => {
        const pa = (a.pinned && a.type === 'private') ? 2 : (a.pinned ? 1 : 0)
        const pb = (b.pinned && b.type === 'private') ? 2 : (b.pinned ? 1 : 0)
        if (pa !== pb) return pb - pa
        const ta = a.lastTime ? new Date(a.lastTime).getTime() : 0
        const tb = b.lastTime ? new Date(b.lastTime).getTime() : 0
        return tb - ta
      })
    } else {
      uni.showToast({
        title: response.message || '获取聊天列表失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    })
    loadDefaultChatList()
  } finally {
    loading.value = false
  }
}

const loadDefaultChatList = () => {
  chatList.value = [
    {
      id: 'family',
      type: 'group',
      name: '幸福家庭群',
      avatar: 'https://ui-avatars.com/api/?name=%E5%B9%B8%E7%A6%8F%E5%AE%B6%E5%BA%AD&background=3b82f6&color=fff&size=128',
      lastMessage: '小明：今天我读完了《小王子》',
      lastTime: new Date(),
      unread: 2,
      memberCount: 4
    },
    {
      id: -1,
      type: 'ai',
      name: 'AI阅读助手',
      avatar: '/static/images/avatars/AI机器人.svg',
      lastMessage: '根据您的阅读习惯，为您推荐...',
      lastTime: new Date(),
      unread: 0
    }
  ]
}

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

const navigateToChat = (chat) => {
  try {
    if (chat.type === 'group') {
      uni.navigateTo({
        url: `/pages/common/group-chat?id=${chat.id}&name=${encodeURIComponent(chat.name)}`
      })
    } else if (chat.type === 'private') {
      uni.navigateTo({
        url: `/pages/common/private-chat?id=${chat.id}&name=${encodeURIComponent(chat.name)}`
      })
    } else if (chat.type === 'ai') {
      if (userRole.value === 2) {
        uni.navigateTo({ url: '/pages/child/chat' })
      } else {
        uni.switchTab({ url: '/pages/parent/ai-chat/ai-chat' })
      }
    } else if (chat.type === 'application') {
      uni.navigateTo({ url: '/pages/common/application-list' })
    }
  } catch (error) {
    uni.showToast({
      title: '页面跳转失败',
      icon: 'none'
    })
  }
}

const onAddActionClick = () => {
  try {
    const isChild = userRole.value === 2
    const itemList = isChild
      ? ['创建群聊', '添加好友', '绑定家人', '查看申请']
      : ['创建群聊', '添加好友', '绑定孩子', '查看申请']
    uni.showActionSheet({
      itemList,
      success: (res) => {
        if (res.tapIndex === 0) {
          uni.navigateTo({ url: '/pages/common/create-group' })
        } else if (res.tapIndex === 1) {
          uni.navigateTo({ url: '/pages/common/add-friend' })
        } else if (res.tapIndex === 2) {
          if (isChild) {
            uni.navigateTo({ url: '/pages/child/profile-detail/bind-family' })
          } else {
            uni.navigateTo({ url: '/pages/parent/profile/details/bind-child' })
          }
        } else if (res.tapIndex === 3) {
          uni.navigateTo({ url: '/pages/common/application-list' })
        }
      }
    })
  } catch (error) {}
}

const themeStyle = computed(() => {
  const isChild = userRole.value === 2
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})

const goBack = () => {
  try {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      uni.navigateBack({
        delta: 1,
        fail: () => {
          fallbackToHome()
        }
      })
    } else {
      fallbackToHome()
    }
  } catch (error) {
    fallbackToHome()
  }
}

const fallbackToHome = () => {
  const homeUrl = userRole.value === 2 ? '/pages/child/home' : '/pages/parent/home/home'
  uni.switchTab({
    url: homeUrl
  })
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #ffffff;
  width: 100%;
  box-sizing: border-box;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: var(--primary-color);
  z-index: 50;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.25);
  padding-top: var(--status-bar-height);
}

.header-content {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 32rpx;
  position: relative;
}

.header-left {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.header-right {
  width: 80rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.back-icon {
  font-size: 36rpx;
  color: #ffffff;
  padding: 10rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #ffffff;
  flex: 1;
  text-align: center;
}

.icon-btn {
  width: 64rpx;
  height: 64rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn:active {
  background-color: rgba(255, 255, 255, 0.3);
}

.icon-btn .fas {
  font-size: 32rpx;
  color: #ffffff;
}

.main-content {
  padding-top: calc(var(--status-bar-height) + 88rpx);
  height: 100vh;
  box-sizing: border-box;
  background-color: #ffffff;
}

.chat-list {
  padding: 0 32rpx;
}

.chat-item {
  display: flex;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
  align-items: center;
  position: relative;
}

.chat-item:active {
  background-color: #fafafa;
}

.chat-avatar-wrapper {
  position: relative;
  margin-right: 24rpx;
}

.chat-avatar {
  width: 104rpx;
  height: 104rpx;
  border-radius: 20rpx;
  overflow: hidden;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-avatar image {
  width: 100%;
  height: 100%;
}

.chat-avatar.family-group image {
  background-color: #3b82f6;
}

.chat-avatar.ai-assistant {
  background-color: #eff6ff;
}

.chat-avatar.ai-assistant .fas {
  font-size: 52rpx;
  color: #3b82f6;
}

.chat-avatar.application-request image {
  background-color: #f59e0b;
}

.unread-dot {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 8rpx;
  background-color: #ef4444;
  border: 4rpx solid #ffffff;
  border-radius: 18rpx;
  color: #ffffff;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  font-weight: 600;
  z-index: 10;
}

.chat-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8rpx;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.chat-name {
  font-size: 34rpx;
  font-weight: 700;
  color: #111827;
  line-height: 1.4;
}

.chat-time {
  font-size: 24rpx;
  color: #9ca3af;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.chat-footer {
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
  line-height: 1.4;
}

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

::-webkit-scrollbar {
  display: none;
}
</style>

<template>
  <view class="container" :style="themeStyle">
    <view class="status-bar"></view>
    <view class="nav-header">
      <view class="back-btn" @click="goBack">
        <text class="fas fa-chevron-left"></text>
      </view>
      <text class="title">历史对话</text>
      <view class="right-placeholder"></view>
    </view>

    <view class="content">
      <view v-if="loading" class="loading">
        <text class="fas fa-spinner loading-icon"></text>
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else-if="sessions.length === 0" class="empty-state">
        <text class="fas fa-inbox empty-icon"></text>
        <text class="empty-text">暂无历史对话</text>
      </view>

      <view v-else class="list-container">
        <view
          v-for="item in sessions"
          :key="item.sessionId"
          class="session-item"
          @tap="toggleSession(item.sessionId)"
        >
          <view class="session-main">
            <view class="session-title-row">
              <text class="session-title">{{ item.title || '未命名会话' }}</text>
              <view class="session-right">
                <text class="session-time">{{ item.displayTime }}</text>
                <text class="fas fa-chevron-down session-arrow" :class="{ open: expandedSessionId === item.sessionId }"></text>
              </view>
            </view>
            <text class="session-preview">
              {{ item.preview }}
            </text>
          </view>

          <view v-if="expandedSessionId === item.sessionId" class="session-detail" @tap.stop="">
            <view v-if="loadingSessionId === item.sessionId" class="detail-loading">
              <text class="fas fa-spinner detail-loading-icon"></text>
              <text class="detail-loading-text">加载对话...</text>
            </view>
            <view v-else class="detail-list">
              <view
                v-for="row in (detailsBySessionId[item.sessionId] || [])"
                :key="row.id"
                class="detail-row"
              >
                <view class="bubble user">
                  <text class="bubble-text">{{ row.userQuestion }}</text>
                </view>
                <view class="bubble ai">
                  <text class="bubble-text">{{ row.aiReply }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { aiApi, userApi } from '@/utils/api.js'

const sessions = ref([])
const loading = ref(false)
const currentUser = ref(null)
const expandedSessionId = ref(null)
const loadingSessionId = ref(null)
const detailsBySessionId = ref({})

const themeStyle = computed(() => {
  const role = currentUser.value?.role
  const isChild = role === 2
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    if (Number.isNaN(date.getTime())) return ''
    const now = new Date()
    const isToday = date.toDateString() === now.toDateString()
    if (isToday) {
      const h = String(date.getHours()).padStart(2, '0')
      const m = String(date.getMinutes()).padStart(2, '0')
      return h + ':' + m
    }
    const month = date.getMonth() + 1
    const day = date.getDate()
    return month + '-' + day
  } catch (e) {
    return ''
  }
}

const loadSessions = async () => {
  loading.value = true
  try {
    const res = await aiApi.getPersonalSessions()
    if (res && res.code === 200 && Array.isArray(res.data)) {
      sessions.value = res.data.map((it) => {
        const lastQuestion = it.lastQuestion || ''
        const lastReply = it.lastReply || ''
        const previewSource = lastReply || lastQuestion
        let preview = previewSource || ''
        preview = String(preview)
        if (preview.length > 40) {
          preview = preview.slice(0, 40) + '...'
        }
        return {
          sessionId: it.sessionId,
          title: it.title,
          lastTime: it.lastTime,
          displayTime: formatTime(it.lastTime),
          lastQuestion,
          lastReply,
          preview
        }
      })
    } else {
      sessions.value = []
    }
  } catch (e) {
    console.error('获取历史对话失败', e)
    sessions.value = []
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadCurrentUser = async () => {
  try {
    const res = await userApi.getCurrentUser()
    if (res && res.code === 200) {
      currentUser.value = res.data
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

const goBack = () => {
  uni.navigateBack()
}

const toggleSession = async (sessionId) => {
  if (!sessionId) return
  if (expandedSessionId.value === sessionId) {
    expandedSessionId.value = null
    return
  }
  expandedSessionId.value = sessionId
  const cache = detailsBySessionId.value[sessionId]
  if (Array.isArray(cache) && cache.length > 0) return
  loadingSessionId.value = sessionId
  try {
    const res = await aiApi.getPersonalSessionDetail(sessionId)
    if (res && res.code === 200 && Array.isArray(res.data)) {
      detailsBySessionId.value = {
        ...detailsBySessionId.value,
        [sessionId]: res.data
      }
    } else {
      detailsBySessionId.value = {
        ...detailsBySessionId.value,
        [sessionId]: []
      }
    }
  } catch (e) {
    console.error('获取会话详情失败', e)
    detailsBySessionId.value = {
      ...detailsBySessionId.value,
      [sessionId]: []
    }
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loadingSessionId.value = null
  }
}

onMounted(async () => {
  await loadCurrentUser()
  await loadSessions()
})
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f8f8f8;
  display: flex;
  flex-direction: column;
}

.status-bar {
  height: var(--status-bar-height);
  background-color: #fff;
}

.nav-header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background-color: var(--primary-color);
  position: sticky;
  top: var(--status-bar-height);
  z-index: 100;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.right-placeholder {
  width: 32px;
}

.content {
  flex: 1;
  padding: 16px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 80px;
  color: #666;
}

.loading-icon {
  font-size: 32px;
  margin-bottom: 12px;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 14px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 80px;
  color: #999;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.session-item {
  background-color: #fff;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.session-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.session-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.session-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.session-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-right: 8px;
}

.session-time {
  font-size: 12px;
  color: #9ca3af;
}

.session-arrow {
  font-size: 14px;
  color: #9ca3af;
  transition: transform 0.15s ease;
}

.session-arrow.open {
  transform: rotate(180deg);
}

.session-preview {
  font-size: 14px;
  color: #4b5563;
}

.session-detail {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f3f4f6;
}

.detail-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6b7280;
  padding: 8px 0;
}

.detail-loading-icon {
  font-size: 16px;
  animation: spin 1s linear infinite;
}

.detail-loading-text {
  font-size: 13px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bubble {
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
}

.bubble.user {
  align-self: flex-end;
  max-width: 86%;
  background: var(--primary-color);
  color: #fff;
}

.bubble.ai {
  align-self: flex-start;
  max-width: 86%;
  background: #f3f4f6;
  color: #111827;
}

.bubble-text {
  color: inherit;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

<template>
  <view class="container" :style="themeStyle">
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <text class="fas fa-arrow-left" @click="goBack"></text>
          <text class="page-title">私聊设置</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y="true" class="content">
      <view class="content-inner">
        <view class="friend-basic">
          <image class="friend-avatar" :src="friendInfo.avatar || defaultAvatar" mode="aspectFill"></image>
          <view class="friend-name-row">
            <text class="friend-name">{{ friendInfo.name || '好友' }}</text>
          </view>
        </view>

        <view class="setting-item">
          <view class="item-left">
            <text class="item-title">置顶好友</text>
            <text class="item-desc">置顶后该好友会显示在列表顶部</text>
          </view>
          <switch :checked="pinned" @change="togglePin"></switch>
        </view>

        <view class="setting-item edit-item">
          <view class="item-left">
            <text class="item-title">备注名</text>
            <text class="item-desc">设置仅本人可见的好友备注</text>
          </view>
          <view class="item-right">
            <input
              class="edit-input"
              v-model="remark"
              placeholder="输入备注名"
              @confirm="saveRemark"
            />
            <button class="primary-btn" @tap="saveRemark">保存</button>
          </view>
        </view>

        <view class="setting-item" @tap="confirmClear">
          <view class="item-left">
            <text class="item-title">清除私聊消息</text>
            <text class="item-desc">仅清除本人可见的历史消息</text>
          </view>
          <text class="fas fa-trash"></text>
        </view>

        <view class="setting-item" @tap="openComplaint">
          <view class="item-left">
            <text class="item-title">投诉</text>
            <text class="item-desc">反馈不当内容或行为</text>
          </view>
          <text class="fas fa-exclamation-circle"></text>
        </view>

        <view class="danger-item" @tap="confirmDelete">
          <text class="danger-text">删除好友</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="showComplaint" class="modal-mask" @tap="closeComplaint">
      <view class="modal" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">提交投诉</text>
          <text class="fas fa-times" @tap="closeComplaint"></text>
        </view>
        <textarea class="modal-textarea" v-model="complaintText" placeholder="请描述问题（不少于10字）"></textarea>
        <view class="modal-actions">
          <button class="cancel-btn" @tap="closeComplaint">取消</button>
          <button class="confirm-btn" @tap="submitComplaint">提交</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { friendApi, groupApi, userApi } from '@/utils/api.js'

const defaultAvatar = 'https://api.dicebear.com/7.x/bottts/svg?seed=friend&backgroundColor=c7d2fe'

const friendInfo = ref({
  id: '',
  name: '',
  avatar: ''
})

const pinned = ref(false)
const remark = ref('')

const showComplaint = ref(false)
const complaintText = ref('')

const themeStyle = computed(() => {
  const info = uni.getStorageSync('userInfo') || {}
  const isChild = info.role === 2
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const fid = page.options.id
  const fname = page.options.name
  if (!fid) {
    uni.showToast({ title: '缺少好友ID', icon: 'none' })
    return
  }
  friendInfo.value.id = fid
  if (fname) friendInfo.value.name = decodeURIComponent(fname)
  await initUser()
  await loadMyFriendInfo()
})

const initUser = async () => {
  try {
    const u = uni.getStorageSync('userInfo')
    if (!u || !u.id) {
      const res = await userApi.getCurrentUser()
      if (res && res.code === 200 && res.data) {
        try { uni.setStorageSync('userInfo', res.data) } catch (e) {}
      }
    }
  } catch (e) {}
}

const loadMyFriendInfo = async () => {
  try {
    const res = await friendApi.getMyFriendInfo(friendInfo.value.id)
    if (res && res.code === 200 && res.data) {
      pinned.value = !!(res.data.pinned || false)
      remark.value = res.data.remark || ''
      if (!friendInfo.value.name) {
        friendInfo.value.name = res.data.friendNickName || friendInfo.value.name
      }
      friendInfo.value.avatar = res.data.friendHeadImage || friendInfo.value.avatar
    }
  } catch (e) {}
}

const goBack = () => { uni.navigateBack() }

const togglePin = async (e) => {
  const checked = !!(e?.detail?.value ?? !pinned.value)
  try {
    const res = await friendApi.pinFriend(friendInfo.value.id, checked)
    if (res && res.code === 200) {
      pinned.value = checked
    } else {
      uni.showToast({ title: res?.message || '操作失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const saveRemark = async () => {
  const name = String(remark.value || '').trim()
  try {
    const res = await friendApi.updateFriendRemark(friendInfo.value.id, name)
    if (res && res.code === 200) {
      uni.showToast({ title: '已保存', icon: 'none' })
    } else {
      uni.showToast({ title: res?.message || '保存失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const confirmClear = () => {
  uni.showModal({
    title: '清除私聊消息',
    content: '确认清除本人可见的与该好友的历史消息？此操作不可恢复。',
    success: async ({ confirm }) => {
      if (confirm) {
        try {
          const res = await friendApi.clearPrivateMessages(friendInfo.value.id)
          if (res && res.code === 200) {
            uni.showToast({ title: '已清除', icon: 'none' })
          } else {
            uni.showToast({ title: res?.message || '清除失败', icon: 'none' })
          }
        } catch {
          uni.showToast({ title: '网络错误', icon: 'none' })
        }
      }
    }
  })
}

const openComplaint = () => { showComplaint.value = true }
const closeComplaint = () => { showComplaint.value = false; complaintText.value = '' }

const submitComplaint = async () => {
  const text = String(complaintText.value || '').trim()
  if (text.length < 10) {
    uni.showToast({ title: '请详细描述问题', icon: 'none' })
    return
  }
  try {
    const res = await groupApi.submitComplaint({
      targetType: 2,
      targetId: friendInfo.value.id,
      content: text
    })
    if (res && res.code === 200) {
      uni.showToast({ title: '已提交', icon: 'none' })
      closeComplaint()
    } else {
      uni.showToast({ title: res?.message || '提交失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const confirmDelete = () => {
  uni.showModal({
    title: '删除好友',
    content: '确认删除该好友吗？删除后将相互解除好友关系。',
    success: async ({ confirm }) => {
      if (confirm) {
        try {
          const res = await friendApi.deleteFriend(friendInfo.value.id)
          if (res && res.code === 200) {
            uni.showToast({ title: '已删除', icon: 'none' })
            goBack()
          } else {
            uni.showToast({ title: res?.message || '删除失败', icon: 'none' })
          }
        } catch {
          uni.showToast({ title: '网络错误', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');
.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  --header-height: 68px;
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
  align-items: center;
  padding: 12px 16px;
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
.page-title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
}
.content {
  margin-top: var(--header-height);
  padding: 0;
  box-sizing: border-box;
}
.content-inner {
  width: 100%;
  max-width: 760px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 16px;
  box-sizing: border-box;
}
.friend-basic {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 12px;
}
.friend-avatar {
  width: 96px;
  height: 96px;
  border-radius: 12px;
  background-color: #e5e7eb;
}
.friend-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.friend-name {
  font-size: 16px;
  color: #111827;
  font-weight: 600;
}
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  margin-top: 12px;
}
.item-left {
  display: flex;
  flex-direction: column;
}
.item-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.edit-item {
  flex-wrap: wrap;
  gap: 8px;
}
.item-title {
  font-size: 14px;
  color: #111827;
}
.item-desc {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}
.edit-input {
  flex: 1;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 14px;
}
.primary-btn {
  background-color: #3b82f6;
  color: #ffffff;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 14px;
}
.danger-item {
  background-color: #ffffff;
  border: 1px solid #fecaca;
  border-radius: 12px;
  padding: 12px;
  margin-top: 12px;
}
.danger-text {
  color: #ef4444;
  text-align: center;
  width: 100%;
}
.modal-mask {
  position: fixed;
  inset: 0;
  background-color: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  width: 88%;
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
}
.modal-title {
  font-size: 16px;
  color: #111827;
  font-weight: 600;
}
.modal-textarea {
  min-height: 120px;
  padding: 12px;
  font-size: 14px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #e5e7eb;
}
.cancel-btn {
  background-color: #e5e7eb;
  color: #111827;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
}
.confirm-btn {
  background-color: #3b82f6;
  color: #ffffff;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
}
</style>

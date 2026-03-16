<template>
  <view class="container" :style="themeStyle">
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <text class="fas fa-arrow-left" @click="goBack"></text>
          <text class="page-title">群设置</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y="true" class="content">
      <view class="content-inner">
      <view v-if="debug" class="debug">
        <view class="debug-line">isOwner: {{ isOwner }}</view>
        <view class="debug-line">ownerId: {{ groupInfo.ownerId }}</view>
        <view class="debug-line">userId: {{ userInfo.id }}</view>
        <view class="debug-line" v-for="(line, idx) in debugLines" :key="idx">{{ line }}</view>
      </view>
      <view class="group-basic">
        <image class="group-avatar" :src="groupInfo.avatar || defaultAvatar" mode="aspectFill" @tap="onChangeAvatar"></image>
        <view class="group-name-row">
          <input
            class="group-name-input"
            v-model="editableGroupName"
            :disabled="!isOwner"
            placeholder="群名"
            @confirm="saveGroupName"
          />
          <text v-if="isOwner" class="fas fa-pen" @tap="saveGroupName"></text>
        </view>
        <text v-if="!isOwner" class="remark-tip">群主可修改群名，成员可设置显示群名</text>
        <view v-if="!isOwner" class="remark-row">
          <input
            class="remark-input"
            v-model="remarkGroupName"
            placeholder="显示群名（仅本人可见）"
            @confirm="saveRemarkGroupName"
          />
          <button class="small-btn" @tap="saveRemarkGroupName">保存</button>
        </view>
      </view>

      <view class="setting-item">
        <view class="item-left">
          <text class="item-title">置顶群聊</text>
          <text class="item-desc">置顶后该群聊会显示在列表顶部</text>
        </view>
        <switch :checked="pinned" @change="togglePin"></switch>
      </view>

      <view class="setting-item" @tap="confirmClear">
        <view class="item-left">
          <text class="item-title">清除群消息</text>
          <text class="item-desc">仅清除本人可见的聊天记录</text>
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

      <view class="setting-item edit-item">
        <view class="item-left">
          <text class="item-title">我在群中的昵称</text>
          <text class="item-desc">设置属于我的群内昵称</text>
        </view>
        <view class="item-right">
          <input
            class="edit-input"
            v-model="myNickname"
            placeholder="输入群内昵称"
            @confirm="saveMyNickname"
          />
          <button class="primary-btn" @tap="saveMyNickname">保存</button>
        </view>
      </view>

      <view v-if="!isOwner" class="danger-item" @tap="confirmLeave">
        <text class="danger-text">退出群聊</text>
      </view>
      <view v-if="isOwner" class="danger-item" @tap="confirmDissolve">
        <text class="danger-text">解散群聊</text>
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
import { ref, computed, onMounted, watch } from 'vue'
import { groupApi, fileApi, userApi } from '@/utils/api.js'
import { onShow } from '@dcloudio/uni-app'

const defaultAvatar = 'https://api.dicebear.com/7.x/bottts/svg?seed=group&backgroundColor=c7d2fe'

const groupInfo = ref({
  id: '',
  name: '',
  avatar: '',
  ownerId: '',
  memberCount: 0
})

const userInfo = ref({
  id: '',
  name: '',
  avatar: '',
  role: null
})

const isOwner = computed(() => {
  const ownerId = groupInfo.value.ownerId || groupInfo.value.owner_id
  return String(ownerId || '') === String(userInfo.value.id || '')
})

const editableGroupName = ref('')
const pinned = ref(false)
const myNickname = ref('')
const remarkGroupName = ref('')

const showComplaint = ref(false)
const complaintText = ref('')
const debug = ref(false)
const debugLines = ref([])
const pushDebug = (label, data) => {
  try {
    console.log('[GroupSettings]', label, data)
    const s = typeof data === 'string' ? data : JSON.stringify(data)
    debugLines.value = [...debugLines.value, `${label}: ${s}`].slice(-50)
  } catch (e) {
    console.log('[GroupSettings]', label)
    debugLines.value = [...debugLines.value, label].slice(-50)
  }
}

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const gid = page.options.id
  const gname = page.options.name
  const dbg = page.options.debug
  debug.value = dbg === '1' || dbg === 'true'
  if (!gid) {
    uni.showToast({ title: '缺少群ID', icon: 'none' })
    return
  }
  groupInfo.value.id = gid
  if (gname) editableGroupName.value = decodeURIComponent(gname)
  pushDebug('mounted-options', { id: gid, name: gname, debug: debug.value, storageUser: uni.getStorageSync('userInfo') })
  await initUser()
  await loadGroup()
  await loadMyMemberInfo()
  pushDebug('identity', { isOwner: isOwner.value, ownerId: groupInfo.value.ownerId, userId: userInfo.value.id })
})

onShow(() => {
  // 回到页面时同步一次
  if (groupInfo.value.id) {
    loadGroup()
    loadMyMemberInfo()
  }
})

const themeStyle = computed(() => {
  const role = userInfo.value.role
  const isChild = role === 2 || role === '2' || role === 'child'
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})

const initUser = async () => {
  try {
    const u = uni.getStorageSync('userInfo')
    if (u && u.id) {
      userInfo.value.id = u.id
      userInfo.value.name = u.nickname || u.username || '我'
      userInfo.value.avatar = u.avatar || u.avatarThumb || userInfo.value.avatar
      userInfo.value.role = u.role
      pushDebug('user-storage', userInfo.value)
    }
  } catch (e) {}
  try {
    const res = await userApi.getCurrentUser()
    if (res && res.code === 200 && res.data) {
      userInfo.value.id = res.data.id
      userInfo.value.name = res.data.nickname || res.data.username || '我'
      userInfo.value.avatar = res.data.avatar || res.data.avatarThumb || userInfo.value.avatar
      userInfo.value.role = res.data.role
      try { uni.setStorageSync('userInfo', res.data) } catch (e) {}
      pushDebug('user-api', userInfo.value)
    }
  } catch (e) {}
  if (userInfo.value.role === undefined || userInfo.value.role === null) {
    try {
      const storedRole = uni.getStorageSync('userRole')
      if (storedRole) {
        userInfo.value.role = storedRole
      }
    } catch (e) {}
  }
}

const loadGroup = async () => {
  try {
    const res = await groupApi.getGroupDetail(groupInfo.value.id)
    if (res && res.code === 200) {
      const payload = res.data || {}
      const g = payload.groupInfo || payload
      const ownerId = g.ownerId || g.owner_id || groupInfo.value.ownerId || ''
      groupInfo.value.ownerId = ownerId
      const isOwn = String(ownerId || '') === String(userInfo.value.id || '')
      const rawName = g.rawName || g.name
      const displayName = g.name || rawName
      groupInfo.value.name = isOwn ? rawName || '群聊' : displayName || '群聊'
      editableGroupName.value = isOwn ? (rawName || '') : (displayName || '')
      groupInfo.value.avatar = g.avatar || g.headImage || g.head_image || ''
      groupInfo.value.memberCount = g.memberCount || payload.members?.length || g.members?.length || 0
      pushDebug('group-info', { id: groupInfo.value.id, name: groupInfo.value.name, ownerId: groupInfo.value.ownerId, memberCount: groupInfo.value.memberCount })
    }
  } catch (e) {}
}

const loadMyMemberInfo = async () => {
  try {
    const res = await groupApi.getMyMemberInfo(groupInfo.value.id)
    if (res && res.code === 200 && res.data) {
      const me = res.data
      myNickname.value = me.userNickName || me.user_nick_name || me.remarkNickName || me.remark_nick_name || userInfo.value.name
      remarkGroupName.value = me.remarkGroupName || me.remark_group_name || ''
      pinned.value = !!(me.pinned || false)
      pushDebug('member-me', { myNickname: myNickname.value, remarkGroupName: remarkGroupName.value, pinned: pinned.value })
    }
  } catch (e) {}
}

watch([() => userInfo.value.id, () => groupInfo.value.ownerId], () => {
  pushDebug('identity-watch', { isOwner: isOwner.value, ownerId: groupInfo.value.ownerId, userId: userInfo.value.id })
})

const goBack = () => {
  uni.navigateBack()
}

const saveGroupName = async () => {
  if (!isOwner.value) {
    uni.showToast({ title: '只有群主可以改群名', icon: 'none' })
    return
  }
  const name = String(editableGroupName.value || '').trim()
  if (!name) {
    uni.showToast({ title: '群名不能为空', icon: 'none' })
    return
  }
  try {
    const res = await groupApi.updateGroupName(groupInfo.value.id, name)
    if (res && res.code === 200) {
      uni.showToast({ title: '已更新群名', icon: 'none' })
      groupInfo.value.name = name
      try {
        await groupApi.updateMyGroupRemarkName(groupInfo.value.id, name)
        pushDebug('sync-remark-group-name', { remark: name })
      } catch (e) {}
      try { await loadGroup() } catch (e) {}
      pushDebug('update-group-name', { name })
    } else {
      uni.showToast({ title: res?.message || '更新失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const onChangeAvatar = () => {
  if (!isOwner.value) {
    uni.showToast({ title: '只有群主可改头像', icon: 'none' })
    return
  }
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      try {
        const uploadRes = await fileApi.uploadImage(tempFilePath, 'group/avatar')
        if (uploadRes.code === 200 && uploadRes.data) {
          const url = typeof uploadRes.data === 'string'
            ? uploadRes.data
            : (uploadRes.data.originUrl || uploadRes.data.url || uploadRes.data.thumbUrl)
          const r = await groupApi.updateGroupAvatar(groupInfo.value.id, url)
          if (r && r.code === 200) {
            groupInfo.value.avatar = url
            uni.showToast({ title: '头像已更新', icon: 'none' })
            pushDebug('update-group-avatar', { url })
          } else {
            uni.showToast({ title: r?.message || '更新失败', icon: 'none' })
          }
        } else {
          uni.showToast({ title: uploadRes.message || '上传失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    }
  })
}

const togglePin = async (e) => {
  const checked = !!(e?.detail?.value ?? !pinned.value)
  try {
    const res = await groupApi.pinGroup(groupInfo.value.id, checked)
    if (res && res.code === 200) {
      pinned.value = checked
      pushDebug('pin', { pinned: checked })
    } else {
      uni.showToast({ title: res?.message || '操作失败', icon: 'none' })
    }
  } catch (err) {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const confirmClear = () => {
  uni.showModal({
    title: '清除群消息',
    content: '确认清除本人可见的历史消息？此操作不可恢复。',
    success: async ({ confirm }) => {
      if (confirm) {
        try {
          const res = await groupApi.clearGroupMessages(groupInfo.value.id)
          if (res && res.code === 200) {
            uni.showToast({ title: '已清除', icon: 'none' })
            pushDebug('clear', { groupId: groupInfo.value.id })
          } else {
            uni.showToast({ title: res?.message || '清除失败', icon: 'none' })
          }
        } catch (e) {
          uni.showToast({ title: '网络错误', icon: 'none' })
        }
      }
    }
  })
}

const openComplaint = () => {
  showComplaint.value = true
  pushDebug('open-complaint', {})
}
const closeComplaint = () => {
  showComplaint.value = false
  complaintText.value = ''
}
const submitComplaint = async () => {
  const text = String(complaintText.value || '').trim()
  if (text.length < 10) {
    uni.showToast({ title: '请详细描述问题', icon: 'none' })
    return
  }
  try {
    const res = await groupApi.submitComplaint({
      targetType: 1,
      targetId: groupInfo.value.id,
      groupId: groupInfo.value.id,
      content: text
    })
    if (res && res.code === 200) {
      uni.showToast({ title: '已提交', icon: 'none' })
      closeComplaint()
      pushDebug('submit-complaint', { groupId: groupInfo.value.id })
    } else {
      uni.showToast({ title: res?.message || '提交失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const saveMyNickname = async () => {
  const name = String(myNickname.value || '').trim()
  if (!name) {
    uni.showToast({ title: '昵称不能为空', icon: 'none' })
    return
  }
  try {
    const res = await groupApi.updateMyNickname(groupInfo.value.id, name)
    if (res && res.code === 200) {
      uni.showToast({ title: '已保存', icon: 'none' })
      myNickname.value = name
      try {
        await loadMyMemberInfo()
      } catch (e) {}
      pushDebug('update-my-nickname', { nickname: name })
    } else {
      uni.showToast({ title: res?.message || '保存失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const saveRemarkGroupName = async () => {
  const name = String(remarkGroupName.value || '').trim()
  try {
    const res = await groupApi.updateMyGroupRemarkName(groupInfo.value.id, name)
    if (res && res.code === 200) {
      uni.showToast({ title: '已保存', icon: 'none' })
      pushDebug('update-group-remark', { remark: name })
    } else {
      uni.showToast({ title: res?.message || '保存失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const confirmLeave = () => {
  uni.showModal({
    title: '退出群聊',
    content: '确认退出该群聊吗？退出后将不再接收消息。',
    success: async ({ confirm }) => {
      if (confirm) {
        try {
          const res = await groupApi.leaveGroup(groupInfo.value.id)
          if (res && res.code === 200) {
            uni.showToast({ title: '已退出', icon: 'none' })
            goBack()
            pushDebug('leave-group', { groupId: groupInfo.value.id })
          } else {
            uni.showToast({ title: res?.message || '操作失败', icon: 'none' })
          }
        } catch (e) {
          uni.showToast({ title: '网络错误', icon: 'none' })
        }
      }
    }
  })
}

const confirmDissolve = () => {
  uni.showModal({
    title: '解散群聊',
    content: '确认解散该群聊吗？群内所有成员都将被移除。',
    success: async ({ confirm }) => {
      if (confirm) {
        try {
          const res = await groupApi.dissolveGroup(groupInfo.value.id)
          if (res && res.code === 200) {
            uni.showToast({ title: '已解散', icon: 'none' })
            goBack()
            pushDebug('dissolve-group', { groupId: groupInfo.value.id })
          } else {
            uni.showToast({ title: res?.message || '操作失败', icon: 'none' })
          }
        } catch (e) {
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
.group-basic {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 12px;
}
.group-avatar {
  width: 96px;
  height: 96px;
  border-radius: 12px;
  background-color: #e5e7eb;
}
.group-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.group-name-input {
  min-width: 180px;
  max-width: 240px;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 14px;
}
.remark-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}
.remark-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.remark-input {
  flex: 1;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 14px;
}
.small-btn {
  background-color: #3b82f6;
  color: #ffffff;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
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
.edit-row {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  margin-top: 8px;
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
.debug {
  margin: 12px 16px;
  background-color: #fff7ed;
  border: 1px solid #fde68a;
  border-radius: 10px;
  padding: 10px;
}
.debug-line {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.6;
}
</style>

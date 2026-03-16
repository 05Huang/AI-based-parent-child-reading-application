<template>
  <view class="container" :style="themeStyle">
    <view class="header">
      <view class="header-content">
        <view class="header-left" @click="goBack">
          <text class="fas fa-chevron-left back-icon"></text>
        </view>
        <text class="header-title">创建群聊</text>
      </view>
    </view>
    
    <scroll-view scroll-y="true" class="main-content">
      <view class="card-section">
        <view class="section-header">
          <text class="section-title">群聊信息</text>
          <text class="required-mark">*</text>
        </view>
        <view class="avatar-upload">
          <view class="avatar-preview">
            <image v-if="groupAvatar" :src="groupAvatar" class="group-avatar" mode="aspectFill" />
            <view v-else class="group-avatar placeholder"></view>
          </view>
          <button 
            class="action-btn-small" 
            :class="{ disabled: avatarUploading }"
            @click="chooseAvatar"
            :disabled="avatarUploading"
          >{{ avatarUploading ? '上传中...' : '选择头像' }}</button>
        </view>
        <view class="input-wrapper">
          <input 
            v-model="groupName" 
            class="std-input" 
            placeholder="请输入群聊名称（必填）" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <view class="card-section">
        <view class="section-header">
          <text class="section-title">选择家庭成员</text>
          <text class="section-subtitle">已选 {{ selectedCount }} 人</text>
        </view>
        <view class="member-list">
          <view 
            v-for="m in familyMembers" 
            :key="m.id" 
            class="member-item" 
            @click="toggleSelect(m)"
          >
            <image :src="m.avatar" class="member-avatar" mode="aspectFill" />
            <view class="member-info">
              <text class="member-name">{{ m.name }}</text>
              <text class="member-relation">{{ m.relation }}</text>
            </view>
            <view class="checkbox">
              <text v-if="isSelected(m.id)" class="fas fa-check-circle checked"></text>
              <text v-else class="far fa-circle unchecked"></text>
            </view>
          </view>
        </view>
      </view>

      <view class="card-section secondary-section">
        <view class="section-header">
          <text class="section-title">邀请更多好友</text>
          <text class="section-subtitle">通过ID查找</text>
        </view>
        <view class="search-row">
          <view class="input-wrapper flex-1">
            <input 
              v-model="searchQuery" 
              class="std-input" 
              placeholder="输入用户ID" 
              placeholder-class="placeholder-style"
              confirm-type="search"
              @confirm="onSearch"
            />
          </view>
          <button 
            class="action-btn-small" 
            :class="{ disabled: !searchQuery.trim() }"
            @click="onSearch"
            :disabled="!searchQuery.trim()"
          >搜索</button>
        </view>
        
        <view class="result-list" v-if="hasSearched">
          <view v-if="searchResults.length > 0">
            <view class="result-card" v-for="u in searchResults" :key="u.id">
              <image :src="u.avatar" class="result-avatar" mode="aspectFill" />
              <view class="result-info">
                <text class="result-name">{{ u.name }}</text>
                <text class="result-id">ID: {{ u.id }}</text>
              </view>
              <button class="invite-btn" @click="addToApplicants(u)">邀请</button>
            </view>
          </view>
          <view v-else class="empty-state">
            <text class="fas fa-user-slash empty-icon"></text>
            <text class="empty-text">未找到该用户</text>
          </view>
        </view>
        
        <view class="applicants-list" v-if="applicants.length > 0">
          <view class="applicant-item" v-for="(a, idx) in applicants" :key="a.id">
            <view class="applicant-info">
              <image :src="a.avatar || ('https://ui-avatars.com/api/?name=' + encodeURIComponent(a.name || ('用户 ' + a.id)))" class="applicant-avatar" mode="aspectFill" />
              <text class="applicant-id">{{ a.name || ('用户 ' + a.id) }}</text>
            </view>
            <text class="fas fa-times remove-icon" @click="removeApplicant(idx)"></text>
          </view>
        </view>
      </view>

      <view class="bottom-spacer"></view>
    </scroll-view>

    <view class="footer-bar">
      <view class="footer-info">
        <text class="summary-text">已选 {{ totalSelected }} 人</text>
      </view>
      <button 
        class="create-btn" 
        :class="{ disabled: !canCreate }" 
        @click="createGroup"
      >
        立即创建
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { groupApi, fileApi, friendApi, familyApi, userApi } from '@/utils/api.js'

const groupName = ref('')
const familyMembers = ref([])
const selected = ref([])
const applicants = ref([])
const currentUser = ref(null)
const groupAvatar = ref('')
const avatarUploading = ref(false)
const searchQuery = ref('')
const hasSearched = ref(false)
const searchResults = ref([])

onMounted(async () => {
  try {
    const u = uni.getStorageSync('userInfo')
    if (u) {
      currentUser.value = u
    }
  } catch (e) {}
  try {
    if (!currentUser.value || !currentUser.value.id || currentUser.value.role === undefined || currentUser.value.role === null) {
      const res = await userApi.getCurrentUser()
      if (res && res.code === 200 && res.data) {
        currentUser.value = {
          ...(currentUser.value || {}),
          id: res.data.id,
          name: res.data.nickname || res.data.username || (currentUser.value && currentUser.value.name) || '',
          avatar: res.data.avatar || res.data.avatarThumb || (currentUser.value && currentUser.value.avatar) || '',
          role: res.data.role
        }
        try { uni.setStorageSync('userInfo', res.data) } catch (e) {}
      }
    }
  } catch (e) {}
  try {
    if (!currentUser.value || currentUser.value.role === undefined || currentUser.value.role === null) {
      const storedRole = uni.getStorageSync('userRole')
      if (storedRole) {
        currentUser.value = {
          ...(currentUser.value || {}),
          role: storedRole
        }
      }
    }
  } catch (e) {}
  if (currentUser.value) {
    await loadFamilyMembers()
  }
})

const themeStyle = computed(() => {
  const role = currentUser.value?.role
  const isChild = role === 2 || role === '2' || role === 'child'
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})

const loadFamilyMembers = async () => {
  try {
    const res = await familyApi.getFamilyMembers()
    if (res?.code === 200 && res?.data) {
      const d = res.data
      const all = []
      const pushArr = (arr, relation) => {
        if (Array.isArray(arr)) {
          arr.forEach(m => {
            const id = m.id || m.userId || m.memberId
            all.push({
              id,
              name: m.nickname || m.username || m.name || ('成员 ' + (id ?? '')),
              relation: m.relationType || relation || '家人',
              avatar: m.avatar || m.avatarThumb || ('https://api.dicebear.com/7.x/avataaars/svg?seed=' + (id ?? 'fam'))
            })
          })
        }
      }
      pushArr(d.children, '孩子')
      pushArr(d.parents, '家长')
      if (d.spouse) pushArr([d.spouse], '配偶')
      pushArr(d.siblings, '兄弟姐妹')
      pushArr(d.grandparents, '祖辈')
      pushArr(d.grandchildren, '孙辈')
      pushArr(d.others, '家人')
      familyMembers.value = all
    }
  } catch (e) {
    uni.showToast({ title: '获取家庭成员失败', icon: 'none' })
  }
}

const selectedCount = computed(() => selected.value.length)
const totalSelected = computed(() => selected.value.length + applicants.value.length)
const canCreate = computed(() => groupName.value.trim().length > 0 && totalSelected.value > 0)

const isSelected = (id) => selected.value.some(s => s.id === id)

const toggleSelect = (m) => {
  if (isSelected(m.id)) {
    selected.value = selected.value.filter(s => s.id !== m.id)
  } else {
    selected.value = [...selected.value, m]
  }
}

const onSearch = async () => {
  const q = searchQuery.value.trim()
  if (!q) {
    uni.showToast({ title: '请输入用户ID', icon: 'none' })
    return
  }
  uni.showLoading({ title: '搜索中...' })
  try {
    const res = await friendApi.searchUser(q, 'id')
    uni.hideLoading()
    if (res.code === 200) {
      const users = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : [])
      searchResults.value = users.map(u => ({
        id: u.id,
        name: u.nickname || u.username || '未知用户',
        avatar: u.avatar || u.avatar_thumb || `https://ui-avatars.com/api/?name=${encodeURIComponent(u.nickname || u.username || '用户')}&background=3b82f6&color=fff&size=128`
      }))
      hasSearched.value = true
    } else {
      searchResults.value = []
      hasSearched.value = true
      uni.showToast({ title: res.message || '搜索失败', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    searchResults.value = []
    hasSearched.value = true
    uni.showToast({ title: '搜索出错', icon: 'none' })
  }
}

const addToApplicants = (u) => {
  if (applicants.value.some(a => a.id === u.id)) {
    uni.showToast({ title: '已在列表中', icon: 'none' })
    return
  }
  applicants.value.push({ id: u.id, name: u.name, avatar: u.avatar })
  uni.showToast({ title: '已添加待邀请', icon: 'none' })
}

const removeApplicant = (idx) => {
  applicants.value.splice(idx, 1)
}

const chooseAvatar = async () => {
  if (avatarUploading.value) return
  avatarUploading.value = true
  try {
    const res = await new Promise((resolve, reject) => {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: resolve,
        fail: reject
      })
    })
    let filePath = ''
    if (res.tempFilePaths && res.tempFilePaths.length > 0) {
      filePath = res.tempFilePaths[0]
    } else if (res.tempFiles && res.tempFiles.length > 0) {
      filePath = res.tempFiles[0].path || res.tempFiles[0]
    }
    const upload = await fileApi.uploadImage(filePath, 'group/avatar')
    if (upload && upload.code === 200 && upload.data && upload.data.originUrl) {
      groupAvatar.value = upload.data.originUrl
      uni.showToast({ title: '上传成功', icon: 'success' })
    } else {
      uni.showToast({ title: upload?.message || '上传失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '上传失败', icon: 'none' })
  } finally {
    avatarUploading.value = false
  }
}

const createGroup = async () => {
  if (!canCreate.value) {
    if (!groupName.value.trim()) {
      uni.showToast({ title: '请输入群聊名称', icon: 'none' })
    } else if (totalSelected.value === 0) {
      uni.showToast({ title: '请至少选择一个成员', icon: 'none' })
    }
    return
  }
  
  uni.showLoading({ title: '创建中...' })
  
  try {
    const memberIds = [
      ...selected.value.map(m => m.id)
    ]
    
    const data = {
      name: groupName.value,
      memberIds: memberIds,
      avatar: groupAvatar.value || undefined
    }

    const res = await groupApi.createGroup(data)
    
    uni.hideLoading()
    if (res.code === 200) {
      try {
        const gid = res.data && (res.data.id || res.data.groupId)
        if (gid && applicants.value.length > 0) {
          const inviteeIds = applicants.value.map(a => a.id)
          console.log('准备发送群聊邀请：', { groupId: gid, inviteeIds })
          const giRes = await groupApi.inviteMembers(gid, inviteeIds)
          console.log('群聊邀请接口响应：', giRes)
          if (giRes && giRes.code === 200) {
            const created = (giRes.data && giRes.data.created) ? giRes.data.created : 0
            uni.showToast({ title: created > 0 ? `已发送${created}个邀请` : '未发送邀请', icon: created > 0 ? 'success' : 'none' })
          } else {
            const msg = (giRes && (giRes.message || giRes.msg)) || '邀请发送失败'
            uni.showToast({ title: msg, icon: 'none' })
            console.error('邀请发送失败：', msg)
          }
        } else if (!gid) {
          uni.showToast({ title: '创建成功但群ID缺失', icon: 'none' })
          console.error('创建群聊后未获取到群ID')
        }
      } catch (e) {
        uni.showToast({ title: '邀请发送失败', icon: 'none' })
        console.error('邀请接口异常：', e)
      }
      uni.showToast({ title: '创建成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1000)
    } else {
      uni.showToast({ title: res.message || '创建失败', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '创建失败', icon: 'none' })
  }
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container { 
  min-height: 100vh; 
  background-color: #f3f4f6; 
  display: flex; 
  flex-direction: column; 
}

.header { 
  background-color: var(--primary-color); 
  padding-top: var(--status-bar-height);
  position: sticky;
  top: 0;
  z-index: 50;
}
.header-content { 
  height: 88rpx; 
  display: flex; 
  align-items: center; 
  padding: 0 32rpx; 
}
.header-left { 
  width: 60rpx; 
  height: 100%; 
  display: flex; 
  align-items: center; 
}
.back-icon { 
  color: #ffffff; 
  font-size: 36rpx; 
}
.header-title { 
  flex: 1; 
  text-align: center; 
  color: #ffffff; 
  font-size: 34rpx; 
  font-weight: 600; 
  margin-right: 60rpx; 
}

.main-content { 
  flex: 1; 
  padding: 24rpx; 
  box-sizing: border-box; 
}

.card-section { 
  background-color: #ffffff; 
  border-radius: 24rpx; 
  padding: 32rpx; 
  margin-bottom: 24rpx; 
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.02); 
}

.section-header { 
  display: flex; 
  align-items: baseline; 
  margin-bottom: 24rpx; 
}
.section-title { 
  font-size: 32rpx; 
  font-weight: 600; 
  color: #111827; 
}
.required-mark { 
  color: #ef4444; 
  margin-left: 8rpx; 
  font-size: 32rpx; 
}
.section-subtitle { 
  font-size: 24rpx; 
  color: #6b7280; 
  margin-left: 16rpx; 
}

.input-wrapper { 
  background-color: #f9fafb; 
  border: 2rpx solid #e5e7eb; 
  border-radius: 16rpx; 
  padding: 20rpx 24rpx; 
  transition: all 0.2s; 
}
.input-wrapper:focus-within { 
  border-color: #3b82f6; 
  background-color: #ffffff; 
}
.std-input { 
  font-size: 30rpx; 
  color: #111827; 
  width: 100%; 
}
.placeholder-style { 
  color: #9ca3af; 
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;
}
.avatar-preview {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  background-color: #f3f4f6;
  overflow: hidden;
}
.group-avatar {
  width: 100%;
  height: 100%;
}
.group-avatar.placeholder {
  background: linear-gradient(135deg, #e5e7eb, #f3f4f6);
}

.member-list { 
  display: flex; 
  flex-direction: column; 
  gap: 20rpx; 
}
.member-item { 
  display: flex; 
  align-items: center; 
  padding: 20rpx; 
  background-color: #f8fafc; 
  border-radius: 16rpx; 
  border: 2rpx solid transparent; 
  transition: all 0.2s; 
}
.member-item:active { 
  background-color: #eff6ff; 
}
.member-avatar { 
  width: 88rpx; 
  height: 88rpx; 
  border-radius: 50%; 
  margin-right: 24rpx; 
}
.member-info { 
  flex: 1; 
}
.member-name { 
  font-size: 30rpx; 
  font-weight: 500; 
  color: #1f2937; 
  display: block; 
}
.member-relation { 
  font-size: 24rpx; 
  color: #6b7280; 
  margin-top: 4rpx; 
  display: block; 
}
.checkbox { 
  font-size: 40rpx; 
  margin-left: 20rpx; 
}
.checked { 
  color: #3b82f6; 
}
.unchecked { 
  color: #cbd5e1; 
}

.secondary-section { 
  margin-top: 32rpx; 
}
.search-row { 
  display: flex; 
  gap: 20rpx; 
}
.flex-1 { 
  flex: 1; 
}
.action-btn-small { 
  background-color: #3b82f6; 
  color: #ffffff; 
  font-size: 28rpx; 
  padding: 0 32rpx; 
  border-radius: 16rpx; 
  line-height: 88rpx; 
  margin: 0; 
}
.action-btn-small.disabled { 
  background-color: #93c5fd; 
  opacity: 0.7; 
}

.applicants-list { 
  margin-top: 24rpx; 
  display: flex; 
  flex-wrap: wrap; 
  gap: 16rpx; 
}
.applicant-item { 
  display: flex; 
  align-items: center; 
  background-color: #f1f5f9; 
  padding: 12rpx 20rpx; 
  border-radius: 40rpx; 
}
.applicant-info { 
  display: flex; 
  align-items: center; 
  margin-right: 16rpx; 
}
.applicant-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  margin-right: 12rpx;
  background-color: #e5e7eb;
}
.avatar-placeholder { 
  width: 40rpx; 
  height: 40rpx; 
  background-color: #cbd5e1; 
  border-radius: 50%; 
  color: #ffffff; 
  font-size: 24rpx; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  margin-right: 12rpx; 
  text-transform: uppercase; 
}
.applicant-id { 
  font-size: 26rpx; 
  color: #475569; 
}
.remove-icon { 
  font-size: 28rpx; 
  color: #94a3b8; 
  padding: 4rpx; 
}

.result-list {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.result-card {
  display: flex;
  align-items: center;
  background-color: #f9fafb;
  border-radius: 16rpx;
  padding: 20rpx;
}
.result-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  margin-right: 20rpx;
  background-color: #e5e7eb;
}
.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.result-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}
.result-id {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 4rpx;
}
.invite-btn {
  background-color: #3b82f6;
  color: #ffffff;
  font-size: 26rpx;
  padding: 0 24rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
}

.bottom-spacer { 
  height: 120rpx; 
}

.footer-bar { 
  position: fixed; 
  bottom: 0; 
  left: 0; 
  right: 0; 
  background-color: #ffffff; 
  padding: 24rpx 32rpx; 
  padding-bottom: calc(24rpx + constant(safe-area-inset-bottom)); 
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom)); 
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05); 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  z-index: 100; 
}
.footer-info { 
  flex: 1; 
}
.summary-text { 
  font-size: 28rpx; 
  color: #4b5563; 
}
.create-btn { 
  background-color: #3b82f6; 
  color: #ffffff; 
  font-size: 32rpx; 
  font-weight: 600; 
  padding: 0 64rpx; 
  height: 88rpx; 
  line-height: 88rpx; 
  border-radius: 44rpx; 
  margin: 0; 
  transition: all 0.3s; 
}
.create-btn.disabled { 
  background-color: #e5e7eb; 
  color: #9ca3af; 
}
</style>

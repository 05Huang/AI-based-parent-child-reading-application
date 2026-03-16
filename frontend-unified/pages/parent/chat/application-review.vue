<template>
  <view class="container">
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <text class="fas fa-chevron-left back-icon" @click="goBack"></text>
        </view>
        <text class="header-title">申请详情</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-content">
      <view class="card">
        <text class="card-title">申请信息</text>
        <view class="row">
          <text class="label">申请ID</text>
          <text class="value">{{ applyId }}</text>
        </view>
        <view class="row">
          <text class="label">申请人</text>
          <view style="display: flex; align-items: center;">
            <image v-if="applicantAvatar" :src="applicantAvatar" mode="aspectFill" style="width: 40rpx; height: 40rpx; border-radius: 50%; margin-right: 10rpx;"></image>
            <text class="value">{{ applicantName || applicantId }}</text>
          </view>
        </view>
        <view class="row">
          <text class="label">申请类型</text>
          <text class="value">{{ requestType }}</text>
        </view>
        <view class="row" v-if="requestType === '入群申请'">
          <text class="label">目标群</text>
          <text class="value">{{ targetGroupName || '未知群组' }}</text>
        </view>
        <view class="row">
          <text class="label">备注</text>
          <text class="value">{{ note || '无' }}</text>
        </view>
      </view>

      <view class="actions" v-if="status === 'pending'">
        <button class="btn approve" @click="approve">同意</button>
        <button class="btn reject" @click="reject">拒绝</button>
      </view>
      <view class="actions" v-else>
         <button class="btn" style="background-color: #e5e7eb; color: #9ca3af;">{{ status === 'accepted' ? '已同意' : '已拒绝' }}</button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { friendApi, userApi } from '@/utils/api.js'

const applyId = ref('')
const applicantId = ref('')
const applicantName = ref('')
const applicantAvatar = ref('')
const requestType = ref('好友申请') // 默认为好友申请
const targetGroupName = ref('')
const note = ref('')
const status = ref('pending') // pending, accepted, rejected

const currentUser = ref(null)

onLoad((options) => {
  if (options.id) {
    applyId.value = options.id
  }
})

onMounted(async () => {
  await loadCurrentUser()
  if (currentUser.value && applyId.value) {
    await loadRequestDetail()
  }
})

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

const loadRequestDetail = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await friendApi.getFriendRequests(currentUser.value.id)
    if (res && res.code === 200) {
      const requests = res.data || []
      // 尝试查找匹配的申请ID
      // 注意：这里假设 applyId 是唯一的
      const request = requests.find(r => String(r.id) === String(applyId.value))
      
      if (request) {
        applicantId.value = request.userId
        applicantName.value = request.nickname || request.username || '未知用户'
        applicantAvatar.value = request.avatar || request.headImage || ''
        note.value = request.remark || request.note || request.message || ''
        
        // 状态映射: 0-待处理, 1-已同意, 2-已拒绝
        if (request.status === 1) status.value = 'accepted'
        else if (request.status === 2) status.value = 'rejected'
        else status.value = 'pending'
        
        // 处理申请类型 (如果API支持)
        if (request.type === 2) {
          requestType.value = '入群申请'
          targetGroupName.value = request.groupName || ''
        } else {
          requestType.value = '好友申请'
        }
      } else {
        uni.showToast({ title: '未找到申请信息', icon: 'none' })
      }
    }
  } catch (e) {
    console.error('获取申请详情失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const goBack = () => { uni.navigateBack() }

const approve = async () => {
  if (!applyId.value) return
  
  try {
    uni.showLoading({ title: '处理中...' })
    const res = await friendApi.handleFriendRequest(applyId.value, 1) // 1 for accept
    uni.hideLoading()
    
    if (res && res.code === 200) {
      uni.showToast({ title: '已同意申请', icon: 'success' })
      status.value = 'accepted'
      setTimeout(() => goBack(), 1500)
    } else {
      uni.showToast({ title: res.message || '操作失败', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    console.error('同意申请失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const reject = async () => {
  if (!applyId.value) return
  
  try {
    uni.showLoading({ title: '处理中...' })
    const res = await friendApi.handleFriendRequest(applyId.value, 2) // 2 for reject
    uni.hideLoading()
    
    if (res && res.code === 200) {
      uni.showToast({ title: '已拒绝申请', icon: 'none' })
      status.value = 'rejected'
      setTimeout(() => goBack(), 1500)
    } else {
      uni.showToast({ title: res.message || '操作失败', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    console.error('拒绝申请失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container { min-height: 100vh; background-color: #f3f4f6; }
.header { position: fixed; top: 0; left: 0; right: 0; background-color: rgba(59, 130, 246, 0.98); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); z-index: 50; }
.header-content { display: flex; align-items: center; padding: 24rpx 32rpx; position: relative; }
.header-left { width: 60rpx; position: absolute; left: 32rpx; z-index: 1; }
.back-icon { font-size: 36rpx; color: #ffffff; padding: 20rpx; margin: -20rpx; }
.header-title { font-size: 36rpx; font-weight: bold; color: #ffffff; flex: 1; text-align: center; }
.main-content { margin-top: 88rpx; padding: 24rpx 32rpx; }

.card { background-color: #ffffff; border-radius: 16rpx; padding: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05); }
.card-title { font-size: 30rpx; color: #111827; font-weight: 600; margin-bottom: 16rpx; }
.row { display: flex; justify-content: space-between; padding: 12rpx 0; border-bottom: 1rpx solid #f1f5f9; }
.row:last-child { border-bottom: none; }
.label { font-size: 26rpx; color: #6b7280; }
.value { font-size: 28rpx; color: #111827; }

.actions { display: flex; gap: 16rpx; margin-top: 24rpx; }
.btn { flex: 1; font-size: 28rpx; padding: 16rpx; border-radius: 12rpx; }
.approve { background-color: #10b981; color: #ffffff; }
.reject { background-color: #ef4444; color: #ffffff; }
</style>


<template>
  <view class="container">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-btn" @tap="goBack">
          <text class="fas fa-chevron-left"></text>
        </view>
        <text class="header-title">申请详情</text>
        <view class="header-right-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="main-content" :style="{ paddingTop: headerHeight + 'px' }">
      <view class="card">
        <text class="card-title">申请信息</text>
        <view class="row">
          <text class="label">申请ID</text>
          <text class="value">{{ applyId }}</text>
        </view>
        <view class="row">
          <text class="label">申请人</text>
          <text class="value">{{ applicantId }}</text>
        </view>
        <view class="row">
          <text class="label">申请类型</text>
          <text class="value">{{ requestType }}</text>
        </view>
        <view class="row">
          <text class="label">备注</text>
          <text class="value">{{ note || '无' }}</text>
        </view>
      </view>

      <view class="actions">
        <button class="btn approve" @tap="approve">同意</button>
        <button class="btn reject" @tap="reject">拒绝</button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { friendApi } from '@/utils/api.js'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 64,
      applyId: '',
      applicantId: '', // 显示用，可以是名字
      applicantName: '',
      applicantAvatar: '',
      targetGroup: '', // 如果是群申请
      note: '',
      status: 'pending' // pending, accepted, rejected
    }
  },
  onLoad(options) {
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight
    this.headerHeight = this.statusBarHeight + 44
    
    // 从参数获取数据
    this.applyId = options.id || ''
    this.applicantId = options.applicantId || ''
    this.applicantName = options.name || '未知用户'
    this.applicantAvatar = options.avatar || ''
    this.targetGroup = options.targetGroup || '好友申请'
    this.note = options.note || '无'
    
    // 如果没有详细信息，尝试获取（这里假设没有单条获取接口，需调用列表接口查找，或者由上级页面传递）
    if (!this.applicantName && this.applyId) {
        this.fetchDetail()
    }
  },
  methods: {
    goBack() { uni.navigateBack() },
    
    async fetchDetail() {
      if (!this.applyId) return
      try {
        uni.showLoading({ title: '加载中...' })
        // 获取当前用户ID (需要引入 userApi 或从本地存储获取)
        // 这里假设已经有 currentUser 或从 storage 获取
        let userId = ''
        try {
            const userRes = await uni.getStorageSync('userInfo')
            if (userRes) userId = userRes.id
        } catch (e) {}
        
        if (!userId) {
            // 尝试调用 API
            // import { userApi } from '@/utils/api.js' // 需要确保引入
            // const res = await userApi.getCurrentUser()
            // userId = res.data.id
            console.warn('无法获取用户ID，跳过详情加载')
            return
        }

        const res = await friendApi.getFriendRequests(userId)
        if (res && res.code === 200) {
          const request = res.data.find(r => String(r.id) === String(this.applyId))
          if (request) {
            this.applicantId = request.userId
            this.applicantName = request.nickname || request.username || '未知用户'
            this.applicantAvatar = request.avatar || request.headImage || ''
            this.note = request.message || '申请添加好友'
            this.status = request.status === 1 ? 'accepted' : (request.status === 2 ? 'rejected' : 'pending')
          }
        }
      } catch (e) {
        console.error('获取申请详情失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },

    async approve() {
      if (!this.applyId) return
      try {
        uni.showLoading({ title: '处理中' })
        const res = await friendApi.handleFriendRequest(this.applyId, 1)
        if (res && res.code === 200) {
            uni.showToast({ title: '已同意申请', icon: 'success' })
            this.status = 'accepted'
            setTimeout(() => this.goBack(), 1500)
        } else {
            uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
        }
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    
    async reject() {
      if (!this.applyId) return
      try {
        uni.showLoading({ title: '处理中' })
        const res = await friendApi.handleFriendRequest(this.applyId, 2)
        if (res && res.code === 200) {
            uni.showToast({ title: '已拒绝申请', icon: 'none' })
            this.status = 'rejected'
            setTimeout(() => this.goBack(), 1500)
        } else {
            uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
        }
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  }
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container { min-height: 100vh; background-color: #ffffff; }
.header { position: fixed; top: 0; left: 0; right: 0; background: linear-gradient(135deg, #6366f1, #8b5cf6); padding: 40rpx 32rpx; z-index: 100; box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2); }
.header-content { display: flex; align-items: center; }
.back-btn { width: 60rpx; height: 88rpx; display: flex; align-items: center; justify-content: center; }
.back-btn .fas { color: #ffffff; font-size: 36rpx; }
.header-title { flex: 1; font-size: 36rpx; font-weight: 600; color: #ffffff; text-align: center; }
.header-right-placeholder { width: 60rpx; flex-shrink: 0; }
.main-content { padding: 24rpx 32rpx; }

.card { background-color: #ffffff; border-radius: 16rpx; padding: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05); }
.card-title { font-size: 30rpx; color: #1f2937; font-weight: 600; margin-bottom: 16rpx; }
.row { display: flex; justify-content: space-between; padding: 12rpx 0; border-bottom: 1rpx solid #f1f5f9; }
.row:last-child { border-bottom: none; }
.label { font-size: 26rpx; color: #6b7280; }
.value { font-size: 28rpx; color: #111827; }

.actions { display: flex; gap: 16rpx; margin-top: 24rpx; }
.btn { flex: 1; font-size: 28rpx; padding: 16rpx; border-radius: 12rpx; }
.approve { background-color: #10b981; color: #ffffff; }
.reject { background-color: #ef4444; color: #ffffff; }
</style>


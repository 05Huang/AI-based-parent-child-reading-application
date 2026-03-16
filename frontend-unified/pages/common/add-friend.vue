<template>
  <view class="container" :style="themeStyle">
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <text class="fas fa-chevron-left back-icon" @click="goBack"></text>
        </view>
        <text class="header-title">添加好友</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-content">
      <view class="section core-section">
        <view class="section-header">
          <text class="section-title">查找用户</text>
          <text class="section-subtitle">通过手机号或 ID 精确查找</text>
        </view>
        
        <view class="search-tabs">
          <view 
            class="tab-item" 
            :class="{ active: searchType === 'phone' }" 
            @click="setSearchType('phone')"
          >
            <text class="tab-text">手机号</text>
            <view class="tab-indicator" v-if="searchType === 'phone'"></view>
          </view>
          <view 
            class="tab-item" 
            :class="{ active: searchType === 'id' }" 
            @click="setSearchType('id')"
          >
            <text class="tab-text">用户 ID</text>
            <view class="tab-indicator" v-if="searchType === 'id'"></view>
          </view>
        </view>

        <view class="search-box">
          <view class="input-wrapper">
            <text class="fas fa-search search-icon"></text>
            <input 
              v-model="searchQuery" 
              class="form-input" 
              :placeholder="searchType === 'phone' ? '请输入对方手机号' : '请输入对方用户 ID'" 
              confirm-type="search"
              @confirm="onSearch"
            />
            <text v-if="searchQuery" class="fas fa-times-circle clear-icon" @click="searchQuery = ''"></text>
          </view>
          <button 
            class="action-btn search-btn" 
            :class="{ disabled: !canSearch }"
            :disabled="!canSearch"
            @click="onSearch"
          >
            搜索
          </button>
        </view>

        <view class="divider">
          <text class="divider-text">或</text>
        </view>

        <button class="action-btn scan-btn" @click="scanToAdd">
          <text class="fas fa-qrcode btn-icon"></text>
          <text>扫一扫添加</text>
        </button>
      </view>

      <view class="section result-section" v-if="hasSearched">
        <view class="section-header">
          <text class="section-title">搜索结果</text>
        </view>
        <view v-if="searchResults.length > 0" class="friend-list">
          <view class="friend-card" v-for="u in searchResults" :key="u.id">
            <image :src="u.avatar" class="friend-avatar" mode="aspectFill" />
            <view class="friend-info">
              <text class="friend-name">{{ u.name }}</text>
              <text class="friend-id">{{ searchType === 'phone' ? '手机号: ' + (u.phone || '—') : 'ID: ' + u.id }}</text>
            </view>
            <button 
              v-if="u.status === 'none'" 
              class="add-btn" 
              @click="sendRequest(u)"
            >
              添加
            </button>
            <view v-else-if="u.status === 'applied'" class="status-tag applied">
              <text class="fas fa-clock"></text> 已申请
            </view>
            <view v-else class="status-tag friend">
              <text class="fas fa-check"></text> 已是好友
            </view>
          </view>
        </view>
        <view v-else class="empty-state">
          <text class="fas fa-user-slash empty-icon"></text>
          <text class="empty-text">未找到该用户</text>
        </view>
      </view>

      <view class="section recommend-section">
        <view class="section-header">
          <text class="section-title">已添加好友</text>
          <text class="section-subtitle">与你已建立联系的成员</text>
        </view>
        <scroll-view scroll-x class="recommend-scroll" show-scrollbar="false">
          <view class="recommend-row">
            <view class="recommend-card" v-for="f in friends" :key="f.id">
              <image :src="f.avatar" class="recommend-avatar" mode="aspectFill" />
              <text class="recommend-name">{{ f.name }}</text>
              <button class="mini-add-btn" @click="openChat(f)">
                <text class="fas fa-comments"></text> 聊天
              </button>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="footer-tip" @click="goBindChild">
        <text class="tip-text">想添加家庭成员？去绑定孩子</text>
        <text class="fas fa-chevron-right tip-icon"></text>
      </view>
      
      <view class="bottom-spacer"></view>
    </scroll-view>
  </view>
</template>

<script>
import { friendApi, privateChatApi, userApi } from '@/utils/api.js'

export default {
  data() {
    return {
      searchType: 'phone',
      searchQuery: '',
      hasSearched: false,
      searchResults: [],
      friends: [],
      userRole: null
    }
  },
  async onShow() {
    try {
      const res = await userApi.getCurrentUser()
      if (res && res.code === 200 && res.data) {
        this.userRole = res.data.role
      }
    } catch (e) {}
    this.fetchFriends()
  },
  computed: {
    canSearch() {
      return this.searchQuery.trim().length > 0
    },
    themeStyle() {
      const isChild = this.userRole === 2
      return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
    }
  },
  methods: {
    goBack() { uni.navigateBack() },
    setSearchType(t) { 
      this.searchType = t
      this.searchQuery = ''
      this.hasSearched = false
      this.searchResults = []
    },
    async onSearch() {
      const q = this.searchQuery.trim()
      if (!q) { 
        uni.showToast({ title: '请输入内容', icon: 'none' })
        return 
      }
      uni.showLoading({ title: '搜索中...' })
      try {
        const res = await friendApi.searchUser(q, this.searchType)
        uni.hideLoading()
        if (res.code === 200) {
          const users = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : [])
          this.searchResults = users.map(u => ({
            id: u.id,
            name: u.nickname || u.username || '未知用户',
            phone: u.phone,
            avatar: u.avatar || u.avatar_thumb || `https://ui-avatars.com/api/?name=${encodeURIComponent(u.nickname || u.username || '用户')}&background=3b82f6&color=fff&size=128`,
            status: u.isFriend ? 'friend' : 'none'
          }))
          this.hasSearched = true
        } else {
          uni.showToast({ title: res.message || '搜索失败', icon: 'none' })
          this.searchResults = []
          this.hasSearched = true
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '搜索出错', icon: 'none' })
        this.searchResults = []
        this.hasSearched = true
      }
    },
    scanToAdd() {
      uni.scanCode({
        success: (res) => {
          if (res.result) {
             this.searchType = 'id'
             this.searchQuery = res.result
             this.onSearch()
          }
        },
        fail: () => {
          uni.showToast({ title: '扫码失败', icon: 'none' })
        }
      })
    },
    async sendRequest(u) {
      uni.showLoading({ title: '发送中...' })
      try {
        const currentUser = uni.getStorageSync('userInfo')
        const data = {
          userId: currentUser ? currentUser.id : undefined,
          friendId: u.id,
          remark: '请求添加好友'
        }
        const res = await friendApi.addFriend(data)
        uni.hideLoading()
        if (res.code === 200) {
          u.status = 'applied'
          uni.showToast({ title: '已发送申请', icon: 'success' })
        } else {
          uni.showToast({ title: res.message || '申请失败', icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '发送申请失败', icon: 'none' })
      }
    },
    async quickAdd(r) {
      await this.sendRequest(r)
    },
    async fetchFriends() {
      try {
        const res = await privateChatApi.getContacts()
        if (res && res.code === 200) {
          const list = Array.isArray(res.data) ? res.data : []
          this.friends = list.map(it => ({
            id: it.id,
            name: it.name || '未命名',
            avatar: it.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(it.name || '用户')}&background=3b82f6&color=fff&size=128`
          }))
        } else {
          this.friends = []
        }
      } catch (e) {
        this.friends = []
      }
    },
    openChat(f) {
      const nameParam = encodeURIComponent(f.name || '')
      uni.navigateTo({ url: `/pages/common/private-chat?id=${f.id}&name=${nameParam}` })
    },
    goBindChild() { uni.navigateTo({ url: '/pages/parent/profile/details/bind-child' }) }
  }
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
  z-index: 100;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.15);
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.header-left {
  position: absolute;
  left: 32rpx;
  height: 100%;
  display: flex;
  align-items: center;
}

.back-icon {
  font-size: 36rpx;
  color: #ffffff;
  padding: 20rpx;
  margin: -20rpx;
}

.header-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #ffffff;
}

.main-content {
  flex: 1;
  padding: 32rpx;
}

.section {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.section-header {
  margin-bottom: 24rpx;
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4rpx;
}

.section-subtitle {
  font-size: 24rpx;
  color: #6b7280;
}

.search-tabs {
  display: flex;
  margin-bottom: 24rpx;
  border-bottom: 2rpx solid #e5e7eb;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  position: relative;
  color: #6b7280;
  font-size: 28rpx;
  transition: all 0.3s;
}

.tab-item.active {
  color: #3b82f6;
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  bottom: -2rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 4rpx;
  background-color: #3b82f6;
  border-radius: 4rpx;
}

.search-box {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.input-wrapper {
  flex: 1;
  height: 88rpx;
  background-color: #f3f4f6;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  border-color: #93c5fd;
  background-color: #ffffff;
}

.search-icon {
  font-size: 32rpx;
  color: #9ca3af;
  margin-right: 16rpx;
}

.clear-icon {
  font-size: 32rpx;
  color: #d1d5db;
  padding: 10rpx;
}

.form-input {
  flex: 1;
  font-size: 30rpx;
  color: #1f2937;
}

.action-btn {
  height: 88rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.search-btn {
  background-color: #3b82f6;
  color: #ffffff;
  padding: 0 32rpx;
  min-width: 140rpx;
}

.search-btn.disabled {
  background-color: #d1d5db;
  color: #9ca3af;
}

.scan-btn {
  background-color: #ffffff;
  color: #3b82f6;
  border: 2rpx solid #bfdbfe;
  width: 100%;
  margin-top: 24rpx;
}

.scan-btn:active {
  background-color: #eff6ff;
}

.btn-icon {
  margin-right: 12rpx;
  font-size: 32rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin: 32rpx 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 2rpx;
  background-color: #f3f4f6;
}

.divider-text {
  padding: 0 24rpx;
  color: #9ca3af;
  font-size: 24rpx;
}

.friend-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.friend-card {
  background-color: #f9fafb;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
}

.friend-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  background-color: #e5e7eb;
}

.friend-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.friend-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8rpx;
}

.friend-id {
  font-size: 24rpx;
  color: #6b7280;
}

.add-btn {
  background-color: #3b82f6;
  color: #ffffff;
  font-size: 26rpx;
  padding: 12rpx 32rpx;
  border-radius: 40rpx;
  font-weight: 600;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 32rpx;
  font-weight: 500;
}

.status-tag.applied {
  background-color: #fffbeb;
  color: #d97706;
}

.status-tag.friend {
  background-color: #ecfdf5;
  color: #059669;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64rpx 0;
  color: #9ca3af;
}

.empty-icon {
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
}

.recommend-scroll {
  width: 100%;
  white-space: nowrap;
}

.recommend-row {
  display: flex;
  gap: 24rpx;
  padding: 4rpx;
}

.recommend-card {
  width: 220rpx;
  background-color: #ffffff;
  border: 2rpx solid #f3f4f6;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  display: inline-flex;
}

.recommend-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-bottom: 16rpx;
  background-color: #e5e7eb;
}

.recommend-name {
  font-size: 28rpx;
  color: #1f2937;
  font-weight: 600;
  margin-bottom: 20rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  text-align: center;
}

.mini-add-btn {
  width: 100%;
  background-color: #eff6ff;
  color: #3b82f6;
  font-size: 24rpx;
  padding: 10rpx 0;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.footer-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  gap: 8rpx;
  opacity: 0.8;
}

.tip-text {
  font-size: 26rpx;
  color: #6b7280;
}

.tip-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

.bottom-spacer {
  height: 40rpx;
}
</style>

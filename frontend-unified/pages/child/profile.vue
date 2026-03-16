<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <text class="page-title">个人中心</text>
        <view class="header-right">
          <view class="icon-btn" @tap="goToSettings">
            <text class="fas fa-cog settings-icon"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      <!-- 个人信息卡片 -->
      <view class="profile-section">
        <view class="profile-card">
          <view class="profile-main">
            <view class="avatar-container" @click="showMyQRCode">
              <image 
                :src="userInfo.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(userInfo.nickname || userInfo.username || '小朋友')}&background=8b5cf6&color=fff&size=128`" 
                class="avatar" 
                mode="aspectFill"
              ></image>
              <view class="qr-badge">
                <text class="fas fa-qrcode"></text>
              </view>
            </view>
            <view class="user-info">
              <view class="name-row">
                <text class="username">{{userInfo.nickname}}</text>
                <view class="level-tag">
                  <text class="fas fa-medal"></text>
                  <text>Lv.{{userInfo.level}}</text>
                </view>
              </view>
              <text class="user-title">{{userInfo.title}}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 功能导航入口 -->
      <view class="nav-grid">
        <view class="nav-item" @tap="goToReadingRecord">
          <view class="nav-icon-bg blue">
            <text class="fas fa-book nav-icon"></text>
          </view>
          <text class="nav-text">阅读记录</text>
        </view>
        <view class="nav-item" @tap="goToMyFavorites">
          <view class="nav-icon-bg pink">
            <text class="fas fa-star nav-icon"></text>
          </view>
          <text class="nav-text">我的收藏</text>
        </view>
        <view class="nav-item" @tap="goToBrowseHistory">
          <view class="nav-icon-bg purple">
            <text class="fas fa-history nav-icon"></text>
          </view>
          <text class="nav-text">浏览历史</text>
        </view>
        <view class="nav-item" @tap="goToReadingStats">
          <view class="nav-icon-bg green">
            <text class="fas fa-chart-pie nav-icon"></text>
          </view>
          <text class="nav-text">阅读报告</text>
        </view>
      </view>

      <!-- 阅读统计 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">阅读统计</text>
          <view class="more-btn" @tap="goToReadingStats">
            <text>查看全部</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="stats-grid">
          <view class="stat-card">
            <view class="stat-header">
              <text class="stat-label">本周阅读</text>
              <view class="stat-badge blue-badge" @tap="openTargetModal">
                <text>目标 {{targetMinutes}}min</text>
              </view>
            </view>
            <view class="stat-body">
              <view class="stat-value-group">
                <text class="stat-value">{{weeklyReadingMinutes}}</text>
                <text class="stat-unit">分钟</text>
              </view>
              <view class="progress-ring">
                <!-- 这里可以用canvas画圆环，简化起见用CSS圆角 -->
                <view class="ring-bg">
                  <view class="ring-fill" :style="{ height: weeklyGoalProgress + '%' }"></view>
                </view>
                <text class="ring-text">{{weeklyGoalProgress}}%</text>
              </view>
            </view>
          </view>
          <view class="stat-card">
            <view class="stat-header">
              <text class="stat-label">连续阅读</text>
              <view class="stat-badge orange-badge">
                <text>加油!</text>
              </view>
            </view>
            <view class="stat-body">
              <view class="stat-value-group">
                <text class="stat-value">{{continuousDays}}</text>
                <text class="stat-unit">天</text>
              </view>
              <view class="stat-icon-container">
                <text class="fas fa-fire stat-icon-large"></text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 阅读排行榜 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">阅读时长排行榜</text>
          <view class="more-btn" @tap="goToReadingRank">
            <text>查看全部</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="leaderboard-card">
          <view 
            v-for="(item, index) in leaderboardList" 
            :key="item.userId || index"
            class="leaderboard-item"
            :class="{'top-three': index < 3}"
          >
            <view class="rank-badge" :class="'rank-' + (index + 1)">
              <text>{{ index + 1 }}</text>
            </view>
            <image 
              :src="item.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(item.name || '用户')}&background=6366f1&color=fff&size=128`" 
              class="rank-avatar"
              mode="aspectFill"
            ></image>
            <text class="rank-name">{{ item.name }}</text>
            <view class="rank-score">
              <text class="score-num">{{ item.minutes }}</text>
              <text class="score-unit">分钟</text>
            </view>
          </view>
          <view v-if="leaderboardList.length === 0 && !loadingLeaderboard" class="empty-state">
            <text class="empty-text">暂无排行榜数据</text>
          </view>
        </view>
      </view>

      <!-- 家庭关系分析 -->
      <view class="section family-section">
        <view class="section-header">
          <text class="section-title">我的家庭</text>
          <view class="more-btn" @tap="goToFamilyAnalysis">
            <text>查看全部</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="family-card" @tap="goToFamilyAnalysis">
          <view class="family-info">
            <view class="family-icon-box">
              <text class="fas fa-home"></text>
            </view>
            <view class="family-texts">
              <text class="family-name">家庭关系分析</text>
              <text class="family-sub">了解与家人的亲密互动</text>
            </view>
          </view>
          <view class="family-avatars">
             <image 
              v-for="(member, index) in familyMembers.slice(0, 3)" 
              :key="index"
              :src="member.avatar" 
              class="mini-avatar"
              :style="{ zIndex: 3 - index, marginLeft: index > 0 ? '-16rpx' : '0' }"
            ></image>
            <view class="more-member-badge" v-if="familyMembers.length > 3">
              <text class="fas fa-ellipsis-h"></text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 二维码弹窗 -->
    <view v-if="showQRModal" class="qr-modal" @click="closeQRModal">
      <view class="qr-modal-content" @click.stop>
        <view class="qr-bg-decoration qr-bg-circle-1"></view>
        <view class="qr-bg-decoration qr-bg-circle-2"></view>
        <view class="qr-close-btn" @click="closeQRModal">
          <text class="fas fa-times"></text>
        </view>
        <view class="qr-modal-body">
          <text class="qr-modal-title">我的二维码</text>
          <text class="qr-subtitle">扫码绑定家庭成员</text>
          <view class="qr-code-box">
            <image v-if="qrCodeUrl" :src="qrCodeUrl" class="qr-img" mode="aspectFit"></image>
            <view v-else class="qr-loading"><text>生成中...</text></view>
          </view>
          <view class="qr-user-row">
             <image :src="userInfo.avatar" class="qr-user-avatar"></image>
             <text class="qr-user-name">{{userInfo.nickname}}</text>
          </view>
          <button class="save-btn" @click="saveQRCode">保存二维码</button>
        </view>
      </view>
    </view>

    <!-- 目标阅读时间弹窗 -->
    <view v-if="showTargetModal" class="target-modal" @click="closeTargetModal">
      <view class="target-modal-content" @click.stop>
        <view class="target-modal-header">
          <text class="target-modal-title">设置目标阅读时间</text>
          <view class="target-close-btn" @click="closeTargetModal">
            <text class="fas fa-times"></text>
          </view>
        </view>
        <view class="target-modal-body">
          <view class="target-input-row">
            <text class="target-input-label">每周目标</text>
            <input class="target-input" type="number" v-model="editTargetMinutes" placeholder="请输入分钟数" />
            <text class="target-input-unit">分钟</text>
          </view>
        </view>
        <view class="target-modal-footer">
          <view class="target-btn cancel" @click="closeTargetModal">取消</view>
          <view class="target-btn confirm" @click="saveTargetMinutes" :class="{ disabled: savingTarget }">
            {{ savingTarget ? '保存中' : '确定' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view class="tab-item" @tap="switchTab('/pages/child/home')">
        <text class="fas fa-home"></text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/reading')">
        <text class="fas fa-book"></text>
        <text class="tab-text">阅读</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/video')">
        <text class="fas fa-video"></text>
        <text class="tab-text">视频</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/chat')">
        <text class="fas fa-comments"></text>
        <text class="tab-text">对话</text>
      </view>
      <view class="tab-item active" @tap="switchTab('/pages/child/profile')">
        <text class="fas fa-user"></text>
        <text class="tab-text">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
import { userApi, userBehaviorApi, familyApi, intimacyApi } from '@/utils/api.js'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 100,
      userInfo: {
        id: null,
        username: '小明',
        nickname: '小明',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming345',
        title: '阅读探险家',
        level: 1
      },
      loading: false,
      weeklyReadingMinutes: 0,
      targetMinutes: 30, // 目标阅读时长
      activityScore: 0,
      continuousDays: 0,
      familyMembers: [],
      showQRModal: false,
      qrCodeUrl: '',
      leaderboardList: [],
      loadingLeaderboard: false,
      showTargetModal: false,
      editTargetMinutes: 30,
      savingTarget: false
    }
  },
  computed: {
    weeklyGoalProgress() {
      const minutes = Number(this.weeklyReadingMinutes) || 0
      const target = Number(this.targetMinutes) || 0
      if (target <= 0) return 0
      return Math.min(Math.round((minutes / target) * 100), 100)
    }
  },
  onLoad() {
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight || 20
    this.headerHeight = this.statusBarHeight + 50 // 调整头部高度
    
    this.loadUserInfo()
  },
  onShow() {
    this.loadWeeklyStats()
    this.loadReadingTarget()
    this.loadFamilyMembers()
    this.loadLeaderboard()
  },
  methods: {
    async loadUserInfo() {
      try {
        this.loading = true
        let userId = uni.getStorageSync('userId') || uni.getStorageSync('currentUserId')
        
        if (!userId) {
          try {
            const currentUserResponse = await userApi.getCurrentUser()
            if (currentUserResponse?.data) {
              userId = currentUserResponse.data.id
              this.updateUserInfo(currentUserResponse.data)
            }
          } catch (e) {
            console.warn('获取当前用户失败', e)
          }
        }
        
        if (userId) {
          const userResponse = await userApi.getUserById(userId)
          if (userResponse?.data) {
            this.updateUserInfo(userResponse.data)
            uni.setStorageSync('currentUserId', userId)
          }
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.loadUserInfoFromStorage()
      } finally {
        this.loading = false
      }
    },
    
    updateUserInfo(apiUserData) {
      const displayName = apiUserData.nickname || apiUserData.username || '用户'
      this.userInfo = {
        id: apiUserData.id,
        username: apiUserData.username || apiUserData.nickname || '用户',
        nickname: apiUserData.nickname || apiUserData.username || '用户',
        avatar: apiUserData.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=8b5cf6&color=fff&size=128`,
        title: this.getUserTitle(apiUserData),
        level: apiUserData.level || 1
      }
      uni.setStorageSync('userInfo', this.userInfo)
    },
    
    getUserTitle(userData) {
      const readingCount = userData.readingCount || 0
      if (readingCount >= 500) return '阅读大师'
      if (readingCount >= 100) return '阅读达人'
      if (readingCount >= 50) return '阅读爱好者'
      if (readingCount >= 10) return '阅读探险家'
      return '阅读新手'
    },
    
    loadUserInfoFromStorage() {
      const storedUserInfo = uni.getStorageSync('userInfo')
      if (storedUserInfo) {
        this.userInfo = storedUserInfo
      }
    },
    
    async loadWeeklyStats() {
      try {
        let userId = this.userInfo?.id || uni.getStorageSync('currentUserId')
        if (!userId) return
        
        // 调用无参的 getWeeklyReport 获取本周（自然周）的数据
        const response = await userBehaviorApi.getWeeklyReport(userId)
        
        if (response?.code === 200 && response.data) {
          this.weeklyReadingMinutes = Math.floor((response.data.totalReadDuration || 0) / 60)
          this.activityScore = Math.min(Math.round(((response.data.activityScore || 0) / 26) * 100), 100)
        }
        
        await this.loadConsecutiveDays()
      } catch (error) {
        console.error('加载周统计失败:', error)
      }
    },

    async loadReadingTarget() {
      try {
        const userId = this.userInfo?.id || uni.getStorageSync('currentUserId')
        if (!userId) return
        const res = await userBehaviorApi.getWeeklyReadingTargetMinutes(userId)
        if (res?.code === 200) {
          const v = Number(res.data)
          if (Number.isFinite(v) && v > 0) {
            this.targetMinutes = Math.round(v)
            this.editTargetMinutes = Math.round(v)
          }
        }
      } catch (e) {
      }
    },
    
    async loadConsecutiveDays() {
      try {
        let userId = this.userInfo?.id || uni.getStorageSync('currentUserId')
        if (!userId) return
        
        const response = await userBehaviorApi.getConsecutiveReadingDays(userId)
        if (response?.code === 200) {
          this.continuousDays = response.data || 0
        }
      } catch (error) {
        console.error('加载连续阅读天数失败:', error)
      }
    },
    
    async loadFamilyMembers() {
      try {
        const response = await intimacyApi.getIntimacyAnalysis()
        if (response?.code === 200 && response.data?.members) {
          this.familyMembers = response.data.members.map(member => ({
            name: member.nickname || '家庭成员',
            avatar: member.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(member.nickname || '用户')}&background=8b5cf6&color=fff&size=128`,
            intimacyScore: Math.round(member.intimacyPercentage || 0),
            relation: member.relationType || '家庭成员'
          }))
        } else {
           this.setDefaultFamilyMembers()
        }
      } catch (error) {
        console.error('加载家庭成员失败:', error)
        this.setDefaultFamilyMembers()
      }
    },
    
    setDefaultFamilyMembers() {
      this.familyMembers = [
        { name: '爸爸', avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=dad123', relation: '父亲' },
        { name: '妈妈', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom456', relation: '母亲' }
      ]
    },

    async loadLeaderboard() {
      try {
        this.loadingLeaderboard = true
        const response = await userBehaviorApi.getWeeklyReadingRanking({ limit: 3 })
        
        let rankingData = []
        if (response?.code === 200 && response.data) {
          rankingData = Array.isArray(response.data) ? response.data : (Array.isArray(response.data.records) ? response.data.records : [])
        } else if (Array.isArray(response)) {
          rankingData = response
        }
        
        if (rankingData.length > 0) {
          this.leaderboardList = rankingData.slice(0, 3).map((item, index) => {
            const minutes = Math.round((item.readDuration || item.totalReadingTime || 0) / 60)
            
            // 数据同步：如果是当前用户，更新统计数据
            if (item.userId === this.userInfo.id || item.id === this.userInfo.id) {
              if (this.weeklyReadingMinutes !== minutes) {
                console.log('同步排行榜数据到个人统计:', minutes)
                this.weeklyReadingMinutes = minutes
              }
            }
            
            return {
              rank: index + 1,
              userId: item.userId || item.id,
              name: item.nickname || item.name || `用户${index + 1}`,
              avatar: item.avatar || item.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(item.nickname || '用户')}&background=6366f1&color=fff&size=128`,
              minutes: minutes
            }
          })
        } else {
          this.leaderboardList = []
        }
      } catch (error) {
        console.error('加载排行榜失败:', error)
        this.leaderboardList = []
      } finally {
        this.loadingLeaderboard = false
      }
    },

    switchTab(url) {
      if (url === '/pages/child/profile') return
      const path = url.replace('/pages/child/', '')
      uni.redirectTo({ url: `/pages/child/${path}` })
    },
    
    goToReadingStats() { uni.navigateTo({ url: '/pages/child/profile-detail/reading-stats' }) },
    goToReadingRecord() { uni.navigateTo({ url: '/pages/child/profile-detail/reading-record' }) },
    goToMyFavorites() { uni.navigateTo({ url: '/pages/child/profile-detail/my-favorites' }) },
    goToBrowseHistory() { uni.navigateTo({ url: '/pages/child/profile-detail/browse-history' }) },
    goToReadingRank() { uni.navigateTo({ url: '/pages/child/profile-detail/reading-rank' }) },
    goToSettings() { uni.navigateTo({ url: '/pages/child/profile-detail/settings' }) },
    goToFamilyAnalysis() { uni.navigateTo({ url: '/pages/child/profile-detail/family-analysis' }) },
    
    showMyQRCode() {
      if (!this.userInfo.id) return uni.showToast({ title: '请先登录', icon: 'none' })
      this.showQRModal = true
      this.generateQRCode()
    },
    closeQRModal() { this.showQRModal = false },
    
    generateQRCode() {
      const qrContent = `qz-family-bind:${this.userInfo.id}:${this.userInfo.username}`
      this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=${encodeURIComponent(qrContent)}`
    },
    
    saveQRCode() {
      if (!this.qrCodeUrl) return
      uni.downloadFile({
        url: this.qrCodeUrl,
        success: (res) => {
          if (res.statusCode === 200) {
            uni.saveImageToPhotosAlbum({
              filePath: res.tempFilePath,
              success: () => uni.showToast({ title: '已保存', icon: 'success' }),
              fail: () => uni.showToast({ title: '保存失败', icon: 'none' })
            })
          }
        }
      })
    }
    ,
    openTargetModal() {
      this.editTargetMinutes = Number(this.targetMinutes) || 30
      this.showTargetModal = true
    },
    closeTargetModal() {
      this.showTargetModal = false
      this.editTargetMinutes = Number(this.targetMinutes) || 30
    },
    async saveTargetMinutes() {
      if (this.savingTarget) return
      const userId = this.userInfo?.id || uni.getStorageSync('currentUserId')
      if (!userId) return uni.showToast({ title: '请先登录', icon: 'none' })

      const minutes = Number(this.editTargetMinutes)
      if (!Number.isFinite(minutes) || minutes <= 0) {
        return uni.showToast({ title: '请输入正确的分钟数', icon: 'none' })
      }
      if (minutes > 10000) {
        return uni.showToast({ title: '目标过大，请重新设置', icon: 'none' })
      }

      try {
        this.savingTarget = true
        const res = await userBehaviorApi.setWeeklyReadingTargetMinutes(userId, Math.round(minutes))
        if (res?.code === 200) {
          this.targetMinutes = Math.round(minutes)
          this.showTargetModal = false
          uni.showToast({ title: '已保存', icon: 'success' })
        } else {
          uni.showToast({ title: res?.msg || '保存失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '保存失败', icon: 'none' })
      } finally {
        this.savingTarget = false
      }
    }
  }
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f8faff;
  padding-bottom: 140rpx;
}

/* 顶部导航栏 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: #f8faff;
  padding: 0 32rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1f2937;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.settings-icon {
  font-size: 32rpx;
  color: #4b5563;
}

/* 主要内容区 */
.main-content {
  padding: 0 32rpx;
  box-sizing: border-box;
}

.section {
  margin-bottom: 40rpx;
}

/* 个人信息卡片 - 优化版 */
.profile-section {
  margin-bottom: 40rpx;
}

.profile-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(139, 92, 246, 0.08);
}

.profile-main {
  display: flex;
  align-items: center;
}

.avatar-container {
  position: relative;
  margin-right: 24rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.qr-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  background: #8b5cf6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #fff;
}

.qr-badge .fas {
  color: #fff;
  font-size: 20rpx;
}

.user-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.username {
  font-size: 36rpx;
  font-weight: 800;
  color: #1f2937;
  margin-right: 16rpx;
}

.level-tag {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.level-tag text {
  color: #fff;
  font-size: 20rpx;
  font-weight: bold;
}

.user-title {
  font-size: 24rpx;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4rpx 16rpx;
  border-radius: 12rpx;
  display: inline-block;
}

/* 功能导航网格 */
.nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
  margin-bottom: 48rpx;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.nav-icon-bg {
  width: 96rpx;
  height: 96rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
  transition: transform 0.2s;
}

.nav-item:active .nav-icon-bg {
  transform: scale(0.95);
}

.nav-icon {
  font-size: 40rpx;
}

.blue { background: #eff6ff; color: #3b82f6; }
.pink { background: #fff1f2; color: #ec4899; }
.purple { background: #f5f3ff; color: #8b5cf6; }
.green { background: #ecfdf5; color: #10b981; }

.nav-text {
  font-size: 24rpx;
  color: #4b5563;
  font-weight: 500;
}

/* 统一区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.more-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: #f3f4f6;
  border-radius: 24rpx;
}

.more-btn text {
  font-size: 22rpx;
  color: #6b7280;
}

/* 统计卡片 - 优化版 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.stat-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.stat-label {
  font-size: 26rpx;
  color: #6b7280;
  font-weight: 500;
}

.stat-badge {
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  font-size: 18rpx;
}

.blue-badge { background: #eff6ff; color: #3b82f6; }
.orange-badge { background: #fff7ed; color: #f97316; }

.stat-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.stat-value-group {
  display: flex;
  align-items: baseline;
}

.stat-value {
  font-size: 44rpx;
  font-weight: 800;
  color: #1f2937;
  line-height: 1;
}

.stat-unit {
  font-size: 22rpx;
  color: #9ca3af;
  margin-left: 6rpx;
}

/* 进度环简化 */
.progress-ring {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #f3f4f6;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.ring-bg {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #e5e7eb;
  z-index: 1;
}

.ring-fill {
  width: 100%;
  background: #3b82f6;
  position: absolute;
  bottom: 0;
  left: 0;
  transition: height 0.5s ease;
}

.ring-text {
  position: relative;
  z-index: 2;
  font-size: 20rpx;
  font-weight: bold;
  color: #1f2937;
  background: rgba(255,255,255,0.8);
  padding: 2rpx 6rpx;
  border-radius: 8rpx;
}

.stat-icon-container {
  width: 64rpx;
  height: 64rpx;
  background: #fff7ed;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-large {
  color: #f97316;
  font-size: 32rpx;
}

/* 排行榜 */
.leaderboard-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 0 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 2rpx solid #f3f4f6;
}

.leaderboard-item:last-child {
  border-bottom: none;
}

.rank-badge {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 24rpx;
  color: #6b7280;
  font-weight: bold;
}

.rank-1 { background: #fbbf24; color: #fff; }
.rank-2 { background: #9ca3af; color: #fff; }
.rank-3 { background: #d97706; color: #fff; }

.rank-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.rank-name {
  flex: 1;
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
}

.rank-score {
  text-align: right;
}

.score-num {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.score-unit {
  font-size: 22rpx;
  color: #9ca3af;
  margin-left: 4rpx;
}

.empty-state {
  padding: 40rpx;
  text-align: center;
  color: #9ca3af;
  font-size: 26rpx;
}

/* 家庭卡片 */
.family-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.family-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.family-icon-box {
  width: 80rpx;
  height: 80rpx;
  background: #f5f3ff;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.family-icon-box .fas {
  color: #8b5cf6;
  font-size: 36rpx;
}

.family-texts {
  display: flex;
  flex-direction: column;
}

.family-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #1f2937;
}

.family-sub {
  font-size: 22rpx;
  color: #9ca3af;
  margin-top: 4rpx;
}

.family-avatars {
  display: flex;
  align-items: center;
}

.mini-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
}

.more-member-badge {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #fff;
  margin-left: -16rpx;
  z-index: 0;
}

.more-member-badge .fas {
  color: #6b7280;
  font-size: 20rpx;
}

/* 底部导航栏 */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: #ffffff;
  border-top: 2rpx solid #f3f4f6;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.02);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.tab-item.active {
  color: #8b5cf6;
}

.tab-item .fas {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.tab-text {
  font-size: 20rpx;
  font-weight: 500;
}

/* 二维码弹窗样式简化 */
.qr-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.qr-modal-content {
  width: 600rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  position: relative;
  overflow: hidden;
}

.qr-close-btn {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  padding: 16rpx;
  z-index: 10;
}

.qr-modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 2;
  position: relative;
}

.qr-modal-title { font-size: 36rpx; font-weight: 800; margin-bottom: 8rpx; }
.qr-subtitle { font-size: 24rpx; color: #6b7280; margin-bottom: 32rpx; }
.qr-code-box { width: 400rpx; height: 400rpx; background: #f8fafc; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; margin-bottom: 32rpx; }
.qr-img { width: 360rpx; height: 360rpx; }
.qr-user-row { display: flex; align-items: center; gap: 16rpx; margin-bottom: 40rpx; }
.qr-user-avatar { width: 64rpx; height: 64rpx; border-radius: 50%; }
.qr-user-name { font-size: 28rpx; font-weight: 600; }
.save-btn { width: 100%; background: #8b5cf6; color: #fff; border-radius: 44rpx; font-size: 30rpx; }
.qr-bg-decoration { position: absolute; border-radius: 50%; opacity: 0.1; }
.qr-bg-circle-1 { width: 200rpx; height: 200rpx; background: #8b5cf6; top: -50rpx; right: -50rpx; }
.qr-bg-circle-2 { width: 150rpx; height: 150rpx; background: #8b5cf6; bottom: -30rpx; left: -30rpx; }

.target-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.target-modal-content {
  width: 620rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 28rpx;
  position: relative;
  box-sizing: border-box;
}

.target-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.target-modal-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #1f2937;
}

.target-close-btn {
  padding: 12rpx;
  color: #6b7280;
}

.target-modal-body {
  padding: 8rpx 0 24rpx;
}

.target-input-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #f8fafc;
  border-radius: 20rpx;
  padding: 20rpx;
}

.target-input-label {
  font-size: 26rpx;
  color: #374151;
  font-weight: 600;
}

.target-input {
  flex: 1;
  height: 72rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 0 16rpx;
  font-size: 28rpx;
  color: #111827;
  box-sizing: border-box;
}

.target-input-unit {
  font-size: 24rpx;
  color: #6b7280;
}

.target-modal-footer {
  display: flex;
  gap: 16rpx;
  justify-content: flex-end;
}

.target-btn {
  min-width: 160rpx;
  height: 72rpx;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
}

.target-btn.cancel {
  background: #f3f4f6;
  color: #374151;
}

.target-btn.confirm {
  background: #8b5cf6;
  color: #fff;
}

.target-btn.confirm.disabled {
  opacity: 0.6;
}

</style>

<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <view class="back-button" @tap="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">阅读时长排行榜</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content">
      <!-- 加载状态 -->
      <view v-if="loading" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 我的排名 -->
      <view v-if="!loading && myRank" class="my-rank">
        <view class="section-title">我的排名</view>
        <view class="rank-card">
          <view class="rank-info">
            <view class="my-rank-badge">
              <text class="my-rank-number">{{myRank.rank}}</text>
            </view>
            <image :src="myRank.avatar" class="rank-avatar"></image>
            <view class="user-info">
              <text class="username username-strong">{{myRank.name}}</text>
              <view class="reading-line">
                <text class="reading-prefix">本周阅读</text>
                <text class="reading-minutes">{{myRank.minutes}}</text>
                <text class="reading-suffix">分钟</text>
              </view>
            </view>
          </view>
          <view class="trend-chip" :class="myRank.trend === 'up' ? 'trend-chip-up' : myRank.trend === 'down' ? 'trend-chip-down' : 'trend-chip-equal'">
            <text class="trend-label">较上周</text>
            <view class="trend-value">
              <text :class="['fas', myRank.trend === 'up' ? 'fa-arrow-up' : myRank.trend === 'down' ? 'fa-arrow-down' : 'fa-minus']"></text>
              <text class="trend-value-text">{{getTrendText(myRank.trend, myRank.trendNumber)}}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 排行榜列表 -->
      <view v-if="!loading && displayRankList && displayRankList.length > 0" class="section-title">全网榜单</view>
      <view v-if="!loading && displayRankList && displayRankList.length > 0" class="rank-list">
        <view 
          v-for="item in displayRankList" 
          :key="item.rank" 
          class="rank-item"
          :class="{ 'first': item.rank === 1, 'second': item.rank === 2, 'third': item.rank === 3 }"
        >
          <view class="rank-info">
            <view class="rank-number-container">
              <image v-if="item.rank <= 3" :src="getRankMedalImage(item.rank - 1)" class="rank-medal"></image>
              <view v-else class="rank-number-badge">
                <text class="rank-number">{{item.rank}}</text>
              </view>
            </view>
            <image :src="item.avatar" class="rank-avatar"></image>
            <view class="user-info">
              <text class="username username-strong">{{item.name}}</text>
              <view class="reading-line">
                <text class="reading-prefix">本周阅读</text>
                <text class="reading-minutes">{{item.minutes}}</text>
                <text class="reading-suffix">分钟</text>
              </view>
            </view>
          </view>
          <view class="trend-chip" :class="item.trend === 'up' ? 'trend-chip-up' : item.trend === 'down' ? 'trend-chip-down' : 'trend-chip-equal'">
            <text class="trend-label">较上周</text>
            <view class="trend-value">
              <text :class="['fas', item.trend === 'up' ? 'fa-arrow-up' : item.trend === 'down' ? 'fa-arrow-down' : 'fa-minus']"></text>
              <text class="trend-value-text">{{getTrendText(item.trend, item.trendNumber)}}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && (!rankList || rankList.length === 0)" class="empty-container">
        <text class="empty-icon">📊</text>
        <text class="empty-text">暂无排行榜数据</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { userApi, userBehaviorApi } from '@/utils/api.js'

export default {
  data() {
    return {
      myRank: null,
      rankList: [],
      loading: false,
      currentUser: null
    }
  },
  computed: {
    displayRankList() {
      const currentUserId = this.currentUser?.id || this.currentUser?.userId
      if (!currentUserId) return this.rankList
      if (!this.myRank) return this.rankList
      return (this.rankList || []).filter(item => item.userId !== currentUserId)
    }
  },
  methods: {
    // 获取排名奖牌图片
    getRankMedalImage(index) {
      const medals = [
        '/static/images/rank-1.png',
        '/static/images/rank-2.png',
        '/static/images/rank-3.png'
      ]
      return medals[index] || ''
    },
    getTrendText(trend, trendNumber) {
      const v = Number(trendNumber || 0)
      if (trend === 'up') return `上升${v}`
      if (trend === 'down') return `下降${v}`
      return '持平'
    },

    // 加载当前用户信息
    async loadCurrentUser() {
      try {
        console.log('[阅读时长排行榜] 开始获取当前用户信息')
        const response = await userApi.getCurrentUser()
        if (response && response.data) {
          this.currentUser = response.data
          console.log('[阅读时长排行榜] 获取当前用户成功:', this.currentUser.nickname)
        }
      } catch (error) {
        console.error('[阅读时长排行榜] 获取当前用户失败:', error)
      }
    },

    // 加载排行榜数据
    async loadRankingData() {
      try {
        this.loading = true
        console.log('[阅读时长排行榜] 开始加载排行榜数据')

        // 调用API获取周阅读排行榜
        const response = await userBehaviorApi.getWeeklyReadingRanking({
          limit: 20
        })

        console.log('[阅读时长排行榜] API响应:', response)

        // 处理不同的响应格式
        let rankingData = null
        
        // 检查响应格式：可能是 { code: 200, data: [...] } 或直接是数组
        if (response && response.code === 200 && response.data) {
          rankingData = response.data
        } else if (response && Array.isArray(response)) {
          rankingData = response
        } else if (response && response.data && Array.isArray(response.data)) {
          rankingData = response.data
        } else if (response && response.records && Array.isArray(response.records)) {
          rankingData = response.records
        }

        if (rankingData && Array.isArray(rankingData) && rankingData.length > 0) {
          // 处理排行榜数据
          this.rankList = rankingData.map((item, index) => {
            // 处理阅读时长：可能是秒、分钟或其他格式
            let readDurationSeconds = 0
            if (item.readDuration !== undefined && item.readDuration !== null) {
              readDurationSeconds = item.readDuration
            } else if (item.totalReadingTime !== undefined && item.totalReadingTime !== null) {
              readDurationSeconds = item.totalReadingTime
            } else if (item.readingMinutes !== undefined && item.readingMinutes !== null) {
              // 如果已经是分钟，转换为秒
              readDurationSeconds = item.readingMinutes * 60
            } else if (item.duration !== undefined && item.duration !== null) {
              readDurationSeconds = item.duration
            }

            // 转换为分钟
            const minutes = Math.round(readDurationSeconds / 60)

            // 处理排名趋势
            let trend = 'equal'
            let trendNumber = 0
            if (item.trend !== undefined && item.trend !== null) {
              trend = item.trend
              trendNumber = item.trendNumber || item.rankChange || 0
            } else if (item.rankChange !== undefined && item.rankChange !== null) {
              if (item.rankChange > 0) {
                trend = 'up'
                trendNumber = item.rankChange
              } else if (item.rankChange < 0) {
                trend = 'down'
                trendNumber = Math.abs(item.rankChange)
              } else {
                trend = 'equal'
              }
            }

            return {
              rank: item.rank || index + 1,
              name: item.nickname || item.name || item.username || `用户${index + 1}`,
              avatar: item.avatar || item.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(item.nickname || item.name || `用户${index + 1}`)}&background=6366f1&color=fff&size=128`,
              minutes: minutes,
              trend: trend,
              trendNumber: trendNumber,
              userId: item.userId || item.id
            }
          })

          // 按排名排序（确保顺序正确）
          this.rankList.sort((a, b) => a.rank - b.rank)

          console.log('[阅读时长排行榜] 处理后的排行榜数据:', this.rankList)

          // 如果当前用户在排行榜中，获取其排名信息
          const currentUserId = this.currentUser?.id || this.currentUser?.userId
          if (currentUserId) {
            const userRankIndex = this.rankList.findIndex(item => item.userId === currentUserId)
            if (userRankIndex >= 0) {
              this.myRank = { ...this.rankList[userRankIndex] }
              console.log('[阅读时长排行榜] 当前用户排名:', this.myRank)
            } else {
              this.setDefaultMyRank()
            }
          }
        } else {
          console.warn('[阅读时长排行榜] API返回数据为空或格式不正确')
          this.setDefaultData()
        }
      } catch (error) {
        console.error('[阅读时长排行榜] 加载排行榜数据失败:', error)
        uni.showToast({
          title: '加载排行榜失败',
          icon: 'none'
        })
        this.setDefaultData()
      } finally {
        this.loading = false
      }
    },

    // 设置默认的我的排名
    setDefaultMyRank() {
      if (this.currentUser) {
        this.myRank = {
          rank: 999,
          name: this.currentUser.nickname || this.currentUser.name || '我',
          avatar: this.currentUser.avatar || this.currentUser.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(this.currentUser.nickname || '我') + '&background=6366f1&color=fff&size=128',
          minutes: 0,
          trend: 'equal'
        }
      }
    },

    // 设置默认数据（API失败时的备用方案）
    setDefaultData() {
      console.warn('[阅读时长排行榜] 使用默认数据')
      this.rankList = [
        {
          rank: 1,
          name: '张小明',
          minutes: 320,
          avatar: 'https://ui-avatars.com/api/?name=张小明&background=6366f1&color=fff&size=128',
          trend: 'equal',
          userId: null
        },
        {
          rank: 2,
          name: '李华',
          minutes: 280,
          avatar: 'https://ui-avatars.com/api/?name=李华&background=6366f1&color=fff&size=128',
          trend: 'up',
          trendNumber: 1,
          userId: null
        },
        {
          rank: 3,
          name: '王小红',
          minutes: 245,
          avatar: 'https://ui-avatars.com/api/?name=王小红&background=6366f1&color=fff&size=128',
          trend: 'down',
          trendNumber: 1,
          userId: null
        },
        {
          rank: 4,
          name: '刘晓晓',
          minutes: 180,
          avatar: 'https://ui-avatars.com/api/?name=刘晓晓&background=6366f1&color=fff&size=128',
          trend: 'up',
          trendNumber: 2,
          userId: null
        },
        {
          rank: 5,
          name: '陈明明',
          minutes: 165,
          avatar: 'https://ui-avatars.com/api/?name=陈明明&background=6366f1&color=fff&size=128',
          trend: 'down',
          trendNumber: 1,
          userId: null
        }
      ]

      // 设置默认的我的排名
      this.setDefaultMyRank()
    },

    // 加载所有数据
    async loadAllData() {
      console.log('[阅读时长排行榜] 加载所有数据')
      await this.loadCurrentUser()
      await this.loadRankingData()
    },

    goBack() {
      console.log('[阅读时长排行榜] 返回上一页')
      uni.navigateBack()
    }
  },

  onLoad() {
    console.log('[阅读时长排行榜] 页面加载')
    this.loadAllData()
  },

  onShow() {
    console.log('[阅读时长排行榜] 页面显示')
    this.loadAllData()
  }
}
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 40rpx 32rpx;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  color: #ffffff;
}

.header-content {
  display: flex;
  align-items: center;
}

.back-button {
  padding: 16rpx;
  margin-right: 16rpx;
  font-size: 28rpx;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
}

.main-content {
  margin-top: 120rpx;
  padding: 32rpx;
  height: calc(100vh - 120rpx);
  box-sizing: border-box;
}

.section-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #374151;
  margin: 8rpx 0 16rpx;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 120rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #9ca3af;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 32rpx;
  text-align: center;
}

.empty-icon {
  font-size: 160rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 34rpx;
  font-weight: bold;
  color: #1f2937;
}

/* 我的排名样式 */
.my-rank {
  margin-bottom: 32rpx;
}

.rank-card {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12), rgba(139, 92, 246, 0.08));
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10rpx 30rpx rgba(17, 24, 39, 0.08);
  border: 2rpx solid rgba(99, 102, 241, 0.18);
}

/* 排行榜列表样式 */
.rank-list {
  padding: 0;
  background: transparent;
  border-radius: 24rpx;
}

.rank-item {
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 10rpx 30rpx rgba(17, 24, 39, 0.06);
  border: 2rpx solid rgba(99, 102, 241, 0.08);
  margin-bottom: 18rpx;
}

.rank-item:last-child {
  margin-bottom: 0;
}

.rank-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.rank-number-container {
  width: 60rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.rank-number-badge {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(99, 102, 241, 0.10);
  border: 2rpx solid rgba(99, 102, 241, 0.18);
}

.rank-number {
  font-size: 26rpx;
  font-weight: 800;
  color: #4f46e5;
}

.rank-medal {
  width: 48rpx;
  height: 48rpx;
}

.rank-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  flex-shrink: 0;
  border: 2rpx solid rgba(255, 255, 255, 0.9);
  background: #f3f4f6;
}

.user-info {
  flex: 1;
  min-width: 0;
  margin-right: 24rpx;
}

.username {
  font-size: 28rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 4rpx;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.username-strong {
  font-weight: 800;
  font-size: 30rpx;
}

.reading-line {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.reading-prefix,
.reading-suffix {
  font-size: 24rpx;
  color: #6b7280;
}

.reading-minutes {
  font-size: 30rpx;
  font-weight: 900;
  color: #111827;
}

.my-rank-badge {
  width: 68rpx;
  height: 68rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 10rpx 24rpx rgba(99, 102, 241, 0.28);
}

.my-rank-number {
  font-size: 34rpx;
  font-weight: 900;
  color: #ffffff;
}

.trend-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
  min-width: 160rpx;
  gap: 6rpx;
}

.trend-label {
  font-size: 22rpx;
  color: #6b7280;
}

.trend-value {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 14rpx;
  border-radius: 999rpx;
  border: 2rpx solid transparent;
  font-size: 24rpx;
  font-weight: 800;
}

.trend-value-text {
  font-size: 24rpx;
  font-weight: 800;
}

.trend-chip-up .trend-value {
  background: rgba(16, 185, 129, 0.10);
  border-color: rgba(16, 185, 129, 0.22);
  color: #059669;
}

.trend-chip-down .trend-value {
  background: rgba(239, 68, 68, 0.10);
  border-color: rgba(239, 68, 68, 0.22);
  color: #dc2626;
}

.trend-chip-equal .trend-value {
  background: rgba(107, 114, 128, 0.10);
  border-color: rgba(107, 114, 128, 0.20);
  color: #4b5563;
}

/* 特殊排名样式 */
.first {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.14), rgba(255, 255, 255, 1));
  border-color: rgba(245, 158, 11, 0.18);
}

.second {
  background: linear-gradient(135deg, rgba(156, 163, 175, 0.18), rgba(255, 255, 255, 1));
  border-color: rgba(156, 163, 175, 0.22);
}

.third {
  background: linear-gradient(135deg, rgba(205, 127, 50, 0.16), rgba(255, 255, 255, 1));
  border-color: rgba(205, 127, 50, 0.20);
}
</style> 

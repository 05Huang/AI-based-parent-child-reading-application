<template>
  <view class="container">
    <view class="header">
      <view class="header-content">
        <view class="back-button" @tap="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">家庭关系分析</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-content">
      <!-- 总体亲密度 -->
      <view class="section">
        <view class="intimacy-card">
          <view class="intimacy-header">
            <text class="intimacy-title">家庭总体亲密度</text>
            <text class="intimacy-score">{{ intimacyScore }}%</text>
          </view>
          <view class="intimacy-progress">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: intimacyScore + '%' }"></view>
            </view>
          </view>
          <text class="intimacy-desc">你的家庭关系非常和谐，继续保持！</text>
        </view>
      </view>

      <!-- 家庭活跃度排行 -->
      <view class="section">
        <view class="section-title">本周活跃度排行</view>
        <view class="activity-ranking">
          <view class="ranking-header">
            <text class="ranking-subtitle">基于群聊互动、阅读参与等综合指标</text>
            <text class="update-time">更新时间：{{ updateTime }}</text>
          </view>
          <view class="ranking-list">
            <view v-for="(item, index) in familyRanking" :key="index" class="ranking-item">
              <view class="rank-number" :class="{ 'top-three': index < 3 }">
                <text v-if="index < 3" class="fas" :class="getRankIcon(index)"></text>
                <text v-else>{{ index + 1 }}</text>
              </view>
              <image :src="item.avatar" class="family-avatar"></image>
              <view class="family-info">
                <text class="family-name">{{ item.name }}</text>
                <text class="activity-detail">群聊{{ item.chatMessages }}条 · 互动{{ item.interactions }}次</text>
              </view>
              <view class="activity-score">
                <text class="score-value">{{ item.activityScore }}</text>
                <text class="score-unit">分</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 家庭成员列表 -->
      <view class="section">
        <view class="section-title">家庭成员互动详情</view>
        <view class="member-list">
          <view class="member-card" v-for="(member, index) in familyMembers" :key="index">
            <view class="member-info">
              <image :src="member.avatar" class="member-avatar"></image>
              <view class="member-details">
                <text class="member-name">{{member.name}}</text>
                <text class="member-relation">{{member.relation}}</text>
              </view>
              <view class="member-score">
                <text class="score-number">{{member.intimacyScore}}%</text>
                <text class="score-label">亲密度</text>
              </view>
            </view>
            <view class="interaction-stats">
              <view class="stat-item">
                <text class="stat-number">{{member.sharedBooks}}</text>
                <text class="stat-label">共读书籍</text>
              </view>
              <view class="stat-item">
                <text class="stat-number">{{member.interactions}}</text>
                <text class="stat-label">互动次数</text>
              </view>
              <view class="stat-item">
                <text class="stat-number">{{member.interactionQuality}}%</text>
                <text class="stat-label">互动质量</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 互动建议 -->
      <view class="section">
        <view class="section-title">互动建议</view>
        <view class="suggestion-list">
          <view class="suggestion-item">
            <text class="fas fa-lightbulb suggestion-icon"></text>
            <text class="suggestion-text">建议每天固定时间与爷爷一起阅读，增进感情</text>
          </view>
          <view class="suggestion-item">
            <text class="fas fa-star suggestion-icon"></text>
            <text class="suggestion-text">可以尝试与妈妈一起参与更多互动性阅读活动</text>
          </view>
          <view class="suggestion-item">
            <text class="fas fa-heart suggestion-icon"></text>
            <text class="suggestion-text">继续保持与爸爸的良好互动，分享更多阅读心得</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { userApi, familyApi, intimacyApi } from '@/utils/api.js'

export default {
  data() {
    return {
      currentUser: null,
      intimacyScore: 92, // 家庭总体亲密度
      familyRanking: [], // 家庭活跃度排行
      familyMembers: [], // 家庭成员详细信息
      loading: false,
      updateTime: '' // 更新时间
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    getRankIcon(index) {
      const icons = ['fa-crown', 'fa-medal', 'fa-award']
      return icons[index] || ''
    },
    // 加载当前用户信息
    async loadCurrentUser() {
      try {
        console.log('[家庭关系分析] 开始获取当前用户信息')
        const response = await userApi.getCurrentUser()
        if (response && response.data) {
          console.log('[家庭关系分析] 获取用户信息成功:', response.data)
          this.currentUser = response.data
        }
      } catch (error) {
        console.error('[家庭关系分析] 获取用户信息失败:', error)
      }
    },
    // 加载家庭关系数据（真实API）
    async loadFamilyData() {
      try {
        if (!this.currentUser?.id) {
          console.warn('[家庭关系分析] 用户ID不存在，使用默认数据')
          this.setDefaultFamilyData()
          return
        }
        
        this.loading = true
        console.log('[家庭关系分析] 开始加载家庭数据，用户ID:', this.currentUser.id)
        
        // 调用API获取家庭关系亲密度分析（包含总体亲密度、成员详情、活跃度排行）
        const intimacyResponse = await intimacyApi.getIntimacyAnalysis()
        console.log('[家庭关系分析] 获取亲密度分析数据:', intimacyResponse)
        
        if (intimacyResponse && intimacyResponse.data) {
          const data = intimacyResponse.data
          
          // 1. 设置总体亲密度
          if (data.overallIntimacy !== undefined && data.overallIntimacy !== null) {
            this.intimacyScore = Math.round(data.overallIntimacy)
          }
          
          // 2. 处理家庭成员互动详情
          if (data.members && Array.isArray(data.members)) {
            this.familyMembers = data.members.map(member => ({
              name: member.nickname || '家庭成员',
              relation: member.relationType || '家庭成员',
              avatar: member.avatar || member.avatarThumb || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default',
              intimacyScore: Math.round(member.intimacyPercentage || 0),
              sharedBooks: member.stats?.sharedReadingCount || 0,
              interactions: member.stats?.interactionCount || 0,
              interactionQuality: member.stats ? 
                Math.round((member.stats.interactionCount || 0) / Math.max(1, (member.stats.privateMessageCount || 0) + (member.stats.groupMessageCount || 0)) * 100) : 0
            }))
            console.log('[家庭关系分析] 处理后的家庭成员:', this.familyMembers)
          }
          
          // 3. 处理本周活跃度排行
          if (data.activityRanking && Array.isArray(data.activityRanking)) {
            this.familyRanking = data.activityRanking.map((item, index) => ({
              name: item.nickname || `成员${index + 1}`,
              avatar: item.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=member${index}`,
              activityScore: item.activityScore || 0,
              chatMessages: item.chatMessages || 0,
              interactions: item.interactions || 0
            }))
            console.log('[家庭关系分析] 获取活跃度排行成功，数据个数:', this.familyRanking.length)
          } else {
            console.warn('[家庭关系分析] 活跃度排行数据为空')
            this.familyRanking = []
          }
          
          // 如果没有数据则使用默认数据
          if (this.familyMembers.length === 0 && this.familyRanking.length === 0) {
            console.log('[家庭关系分析] 数据为空，使用默认数据')
            this.setDefaultFamilyData()
          }
        } else {
          console.warn('[家庭关系分析] 亲密度分析数据为空，使用默认数据')
          this.setDefaultFamilyData()
        }
        
      } catch (error) {
        console.error('[家庭关系分析] 加载家庭数据失败:', error)
        this.setDefaultFamilyData()
      } finally {
        this.loading = false
      }
    },
    // 设置默认家庭数据
    setDefaultFamilyData() {
      // 家庭活跃度排行
      this.familyRanking = [
        {
          name: '张家',
          avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=zhang-family',
          activityScore: 95,
          chatMessages: 156,
          interactions: 42,
          weeklyReadingTime: 320,
          readingParticipation: 89
        },
        {
          name: '李家',
          avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=li-family',
          activityScore: 88,
          chatMessages: 134,
          interactions: 38,
          weeklyReadingTime: 285,
          readingParticipation: 85
        },
        {
          name: '王家',
          avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=wang-family',
          activityScore: 82,
          chatMessages: 98,
          interactions: 29,
          weeklyReadingTime: 245,
          readingParticipation: 78
        },
        {
          name: '赵家',
          avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=zhao-family',
          activityScore: 76,
          chatMessages: 87,
          interactions: 25,
          weeklyReadingTime: 210,
          readingParticipation: 72
        },
        {
          name: '刘家',
          avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=liu-family',
          activityScore: 68,
          chatMessages: 65,
          interactions: 18,
          weeklyReadingTime: 185,
          readingParticipation: 65
        }
      ]
      
      // 家庭成员详细信息
      this.familyMembers = [
        {
          name: '爸爸',
          relation: '父亲',
          avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=dad123&backgroundColor=b6e3f4',
          intimacyScore: 95,
          sharedBooks: 125,
          interactions: 36,
          interactionQuality: 89
        },
        {
          name: '妈妈',
          relation: '母亲',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom456',
          intimacyScore: 98,
          sharedBooks: 156,
          interactions: 42,
          interactionQuality: 92
        },
        {
          name: '爷爷',
          relation: '祖父',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=grandpa789',
          intimacyScore: 88,
          sharedBooks: 98,
          interactions: 28,
          interactionQuality: 85
        },
        {
          name: '奶奶',
          relation: '祖母',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=grandma012',
          intimacyScore: 90,
          sharedBooks: 112,
          interactions: 32,
          interactionQuality: 87
        }
      ]
      
      this.intimacyScore = 92
      console.log('[家庭关系分析] 已加载默认家庭数据')
    },
    // 格式化更新时间
    formatUpdateTime() {
      const now = new Date()
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      this.updateTime = `今天 ${hours}:${minutes}`
    },
    // 加载所有数据
    async loadAllData() {
      console.log('[家庭关系分析] 加载所有数据')
      this.formatUpdateTime()
      await this.loadCurrentUser()
      await this.loadFamilyData()
    }
  },
  onLoad() {
    console.log('[家庭关系分析] 页面加载')
    this.loadAllData()
  },
  onShow() {
    console.log('[家庭关系分析] 页面显示')
    this.loadAllData()
  }
}
</script>

<style>
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
}

.header-content {
  display: flex;
  align-items: center;
  color: #ffffff;
}

.back-button {
  padding: 8rpx;
  margin-right: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.back-button .fas {
  color: #ffffff;
  font-size: 36rpx;
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

.section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
  padding: 0 16rpx;
}

.intimacy-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.intimacy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.intimacy-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
}

.intimacy-score {
  font-size: 48rpx;
  font-weight: bold;
  color: #6366f1;
}

.intimacy-progress {
  margin-bottom: 24rpx;
}

.progress-bar {
  height: 16rpx;
  background: #e5e7eb;
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 8rpx;
}

.intimacy-desc {
  font-size: 28rpx;
  color: #4b5563;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.member-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.member-info {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.member-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 24rpx;
}

.member-details {
  flex: 1;
}

.member-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 4rpx;
  display: block;
}

.member-relation {
  font-size: 24rpx;
  color: #6b7280;
  display: block;
}

.member-score {
  text-align: right;
}

.score-number {
  font-size: 36rpx;
  font-weight: bold;
  color: #6366f1;
  display: block;
}

.score-label {
  font-size: 24rpx;
  color: #6b7280;
}

.interaction-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  border-top: 2rpx solid #f3f4f6;
  padding-top: 24rpx;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
  display: block;
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #6b7280;
}

.suggestion-list {
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-icon {
  font-size: 32rpx;
  color: #6366f1;
  margin-right: 16rpx;
  width: 32rpx;
  text-align: center;
}

.suggestion-text {
  font-size: 28rpx;
  color: #4b5563;
  flex: 1;
}

/* 活跃度排行样式 */
.activity-ranking {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.ranking-header {
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.ranking-subtitle {
  font-size: 24rpx;
  color: #6b7280;
  display: block;
  margin-bottom: 8rpx;
}

.update-time {
  font-size: 20rpx;
  color: #9ca3af;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  border-radius: 16rpx;
  background: #f9fafb;
  transition: all 0.3s ease;
}

.ranking-item:hover {
  background: #f3f4f6;
}

.rank-number {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e5e7eb;
  color: #6b7280;
  font-size: 28rpx;
  font-weight: bold;
  margin-right: 16rpx;
}

.rank-number.top-three {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #92400e;
}

.rank-number.top-three .fas {
  font-size: 32rpx;
}

.family-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  border: 4rpx solid #e5e7eb;
}

.family-info {
  flex: 1;
  margin-right: 16rpx;
}

.family-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #1f2937;
  display: block;
  margin-bottom: 4rpx;
}

.activity-detail {
  font-size: 24rpx;
  color: #6b7280;
}

.activity-score {
  text-align: right;
}

.score-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #6366f1;
}

.score-unit {
  font-size: 24rpx;
  color: #6b7280;
}
</style> 
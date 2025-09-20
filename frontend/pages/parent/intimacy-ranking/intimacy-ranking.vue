<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">亲密度排行榜</text>
      </view>
    </view>

    <!-- 排行榜标题区 -->
    <view class="title-section">
      <view class="title-card">
        <text class="title">亲密度排行榜</text>
        <text class="subtitle">提升您与孩子的亲密度，赢在家庭教育的起跑线！</text>
        <view class="stats-row">
          <view class="stat-item">
            <text class="fas fa-crown text-yellow-300"></text>
            <text class="stat-text">本周排名：</text>
            <text class="stat-value">{{myRank}}</text>
          </view>
          <view class="stat-item">
            <text class="fas fa-heart text-red-300"></text>
            <text class="stat-text">我的亲密度：</text>
            <text class="stat-value">{{myIntimacy}}%</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 排行榜类型选项卡 -->
    <view class="tabs">
      <view class="tab" :class="{'active': activeTab === 'global'}" @click="switchTab('global')">
        <text>全网排行</text>
      </view>
      <view class="tab" :class="{'active': activeTab === 'family'}" @click="switchTab('family')">
        <text>家庭内部排行</text>
      </view>
    </view>

    <!-- 排行榜和趋势 -->
    <scroll-view scroll-y="true" class="ranking-content">
      <!-- 全网排行榜 -->
      <view v-if="activeTab === 'global'" class="ranking-section">
        <view class="section-header">
          <text class="section-title">全网排行</text>
          <picker :range="timeRanges" @change="onTimeRangeChange" class="time-picker">
            <text class="picker-text">{{currentTimeRange}}</text>
          </picker>
        </view>
        
        <view class="ranking-list">
          <view v-for="(user, index) in globalRankings" :key="user.id" 
                class="ranking-item" :class="{'highlight': user.id === myUserId}">
            <view class="rank-number" :class="'medal-' + (index + 1)">{{index + 1}}</view>
            <image :src="user.avatar" class="user-avatar"></image>
            <view class="user-info">
              <view class="info-row">
                <text class="user-name">{{user.name}} ({{user.childName}})</text>
                <view class="intimacy-info">
                  <text class="fas" :class="getTrendIcon(user.trend)"></text>
                  <text class="intimacy-value">{{user.intimacy}}%</text>
                </view>
              </view>
              <view class="progress-bar">
                <view class="progress" :style="{width: user.intimacy + '%'}"></view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 家庭内部排行榜 -->
      <view v-if="activeTab === 'family'" class="ranking-section">
        <view class="section-header">
          <text class="section-title">家庭内部排行</text>
          <picker :range="timeRanges" @change="onFamilyTimeRangeChange" class="time-picker">
            <text class="picker-text">{{currentFamilyTimeRange}}</text>
          </picker>
        </view>
        
        <view class="ranking-list">
          <view v-for="(member, index) in familyRankings" :key="member.id" class="ranking-item">
            <view class="rank-number" :class="'medal-' + (index + 1)">{{index + 1}}</view>
            <image :src="member.avatar" class="user-avatar"></image>
            <view class="user-info">
              <view class="info-row">
                <view class="name-role">
                  <text class="user-name">{{member.name}}</text>
                  <text class="role-tag">{{member.role}}</text>
                </view>
                <view class="intimacy-info">
                  <text class="fas" :class="getTrendIcon(member.trend)"></text>
                  <text class="intimacy-value">{{member.intimacy}}%</text>
                </view>
              </view>
              <view class="progress-bar">
                <view class="progress family" :style="{width: member.intimacy + '%'}"></view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 亲密度趋势 -->
      <view class="trend-section">
        <view class="section-header">
          <text class="section-title">亲密度趋势</text>
          <picker :range="chartTimeRanges" @change="onChartTimeRangeChange" class="time-picker">
            <text class="picker-text">{{currentChartTimeRange}}</text>
          </picker>
        </view>
        <view id="intimacyChart" class="trend-chart"></view>
      </view>

      <!-- 亲密度成长提示 -->
      <view class="tips-section">
        <text class="section-title">提升亲密度的方法</text>
        <view class="tips-list">
          <view class="tip-item">
            <view class="tip-icon-box blue">
              <text class="fas fa-book"></text>
            </view>
            <view class="tip-content">
              <text class="tip-title">每日共读</text>
              <text class="tip-desc">每天坚持与孩子共读15分钟，对话交流可提升亲密度。</text>
            </view>
          </view>
          <view class="tip-item">
            <view class="tip-icon-box green">
              <text class="fas fa-comments"></text>
            </view>
            <view class="tip-content">
              <text class="tip-title">互动交流</text>
              <text class="tip-desc">对孩子的问题及时回应，与他们多进行思考型交流。</text>
            </view>
          </view>
          <view class="tip-item">
            <view class="tip-icon-box purple">
              <text class="fas fa-gamepad"></text>
            </view>
            <view class="tip-content">
              <text class="tip-title">积极参与</text>
              <text class="tip-desc">积极参与孩子的学习活动和游戏，提高互动质量。</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

// 响应式状态
const activeTab = ref('global')
const myUserId = ref('user123')
const myRank = ref('--')
const myIntimacy = ref('--')
const timeRanges = ['本周', '本月', '全部']
const chartTimeRanges = ['近7天', '近30天']
const currentTimeRange = ref('本周')
const currentFamilyTimeRange = ref('本周')
const currentChartTimeRange = ref('近7天')
const globalRankings = ref([])
const familyRankings = ref([])
let intimacyChart = null

// 模拟API类
class RankingAPI {
  constructor() {
    this.myUserId = "user123"
    this.myFamilyId = "family001"
  }

  async getGlobalRankingData(timeRange = 'week') {
    const users = [
      { id: "user123", name: "张爸爸", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=zhang123", intimacy: 92, childName: "小张", trend: "up" },
      { id: "user456", name: "王妈妈", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=wang456", intimacy: 96, childName: "小王", trend: "up" },
      { id: "user789", name: "李爸爸", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=li789", intimacy: 90, childName: "小李", trend: "down" },
      { id: "user012", name: "陈妈妈", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=chen012", intimacy: 88, childName: "小陈", trend: "up" },
      { id: "user345", name: "刘爸爸", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=liu345", intimacy: 84, childName: "小刘", trend: "same" }
    ]
    users.sort((a, b) => b.intimacy - a.intimacy)
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          rankings: users,
          myRank: users.findIndex(user => user.id === this.myUserId) + 1,
          myIntimacy: users.find(user => user.id === this.myUserId)?.intimacy || 0
        })
      }, 800)
    })
  }

  async getFamilyRankingData(timeRange = 'week') {
    const familyMembers = [
      { id: "dad123", name: "爸爸", avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=dad123&backgroundColor=b6e3f4", intimacy: 95, role: "父亲", trend: "up" },
      { id: "mom456", name: "妈妈", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=mom456", intimacy: 98, role: "母亲", trend: "up" },
      { id: "grandpa789", name: "爷爷", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=grandpa789", intimacy: 88, role: "祖父", trend: "down" },
      { id: "grandma012", name: "奶奶", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=grandma012", intimacy: 90, role: "祖母", trend: "same" }
    ]
    familyMembers.sort((a, b) => b.intimacy - a.intimacy)
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({ rankings: familyMembers })
      }, 800)
    })
  }

  async getIntimacyTrend(timeRange = 'week') {
    const data = {
      week: {
        labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        data: [87, 88, 86, 90, 91, 92, 92],
        average: [83, 84, 85, 85, 86, 87, 87]
      },
      month: {
        labels: ['第1周', '第2周', '第3周', '第4周'],
        data: [85, 87, 89, 92],
        average: [82, 83, 85, 86]
      }
    }
    return new Promise(resolve => {
      setTimeout(() => {
        resolve(data[timeRange])
      }, 800)
    })
  }
}

const rankingAPI = new RankingAPI()

// 方法
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'global') {
    loadGlobalRankingData()
  } else {
    loadFamilyRankingData()
  }
}

const getTrendIcon = (trend) => {
  switch (trend) {
    case 'up': return 'fa-caret-up text-green-500'
    case 'down': return 'fa-caret-down text-red-500'
    default: return 'fa-minus text-gray-400'
  }
}

const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile',
    success: () => {
      console.log('Successfully switched to profile page')
    },
    fail: (err) => {
      console.error('Switch tab failed:', err)
      uni.showToast({
        title: '返回失败',
        icon: 'none'
      })
    }
  })
}

const onTimeRangeChange = (e) => {
  currentTimeRange.value = timeRanges[e.detail.value]
  loadGlobalRankingData()
}

const onFamilyTimeRangeChange = (e) => {
  currentFamilyTimeRange.value = timeRanges[e.detail.value]
  loadFamilyRankingData()
}

const onChartTimeRangeChange = (e) => {
  currentChartTimeRange.value = chartTimeRanges[e.detail.value]
  loadIntimacyTrend()
}

const loadGlobalRankingData = async () => {
  try {
    const data = await rankingAPI.getGlobalRankingData()
    globalRankings.value = data.rankings
    myRank.value = data.myRank
    myIntimacy.value = data.myIntimacy
  } catch (error) {
    console.error('Failed to load global ranking data:', error)
  }
}

const loadFamilyRankingData = async () => {
  try {
    const data = await rankingAPI.getFamilyRankingData()
    familyRankings.value = data.rankings
  } catch (error) {
    console.error('Failed to load family ranking data:', error)
  }
}

const loadIntimacyTrend = async () => {
  try {
    const data = await rankingAPI.getIntimacyTrend(
      currentChartTimeRange.value === '近7天' ? 'week' : 'month'
    )
    
    if (!intimacyChart) {
      const chartDom = document.getElementById('intimacyChart')
      intimacyChart = echarts.init(chartDom)
    }
    
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['我的亲密度', '平均亲密度'],
        top: 0,
        right: 0
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.labels
      },
      yAxis: {
        type: 'value',
        min: 80,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '我的亲密度',
          type: 'line',
          data: data.data,
          itemStyle: {
            color: '#3B82F6'
          }
        },
        {
          name: '平均亲密度',
          type: 'line',
          data: data.average,
          itemStyle: {
            color: '#9CA3AF'
          }
        }
      ]
    }
    
    intimacyChart.setOption(option)
  } catch (error) {
    console.error('Failed to load intimacy trend:', error)
  }
}

// 生命周期钩子
onMounted(() => {
  loadGlobalRankingData()
  loadIntimacyTrend()
})
</script>

<style>
/* 引入Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding: 0;
  margin: 0;
  width: 100%;
}

/* 顶部导航样式 */
.nav-header {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.nav-content {
  display: flex;
  align-items: center;
}

.back-btn {
  margin-right: 30rpx;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
}

/* 标题区域样式 */
.title-section {
  padding: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.title-card {
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  color: #ffffff;
  width: 100%;
  box-sizing: border-box;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.subtitle {
  opacity: 0.9;
  font-size: 28rpx;
}

.stats-row {
  display: flex;
  gap: 16rpx;
  margin-top: 30rpx;
}

.stat-item {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 9999rpx;
  padding: 8rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
}

/* 选项卡样式 */
.tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  background-color: #ffffff;
  padding: 0 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  color: #6b7280;
  font-weight: 500;
}

.tab.active {
  color: #3b82f6;
  border-bottom: 4rpx solid #3b82f6;
}

/* 排行榜内容样式 */
.ranking-content {
  flex: 1;
  padding: 30rpx;
  box-sizing: border-box;
  width: 100%;
}

.ranking-section {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
}

.time-picker {
  font-size: 28rpx;
  color: #6b7280;
  background-color: #f3f4f6;
  padding: 8rpx 24rpx;
  border-radius: 9999rpx;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  width: 100%;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-radius: 16rpx;
  width: 100%;
  box-sizing: border-box;
}

.ranking-item.highlight {
  background-color: #eff6ff;
}

.rank-number {
  width: 64rpx;
  height: 64rpx;
  border-radius: 9999rpx;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 24rpx;
  color: #ffffff;
  flex-shrink: 0;
}

.medal-1 {
  background: linear-gradient(135deg, #ffd700, #ffa500);
}

.medal-2 {
  background: linear-gradient(135deg, #c0c0c0, #a9a9a9);
}

.medal-3 {
  background: linear-gradient(135deg, #cd7f32, #a0522d);
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 9999rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.user-name {
  font-weight: 500;
  margin-right: 16rpx;
}

.role-tag {
  font-size: 24rpx;
  color: #6b7280;
  margin-left: 8rpx;
}

.intimacy-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  flex-shrink: 0;
}

.progress-bar {
  width: 100%;
  height: 16rpx;
  background-color: #f3f4f6;
  border-radius: 9999rpx;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: #3b82f6;
  border-radius: 9999rpx;
  transition: width 1s ease-in-out;
}

.progress.family {
  background-color: #8b5cf6;
}

/* 趋势图样式 */
.trend-section {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.trend-chart {
  width: 100%;
  height: 480rpx;
}

/* 提示区域样式 */
.tips-section {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-top: 24rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
}

.tip-icon-box {
  width: 64rpx;
  height: 64rpx;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tip-icon-box.blue {
  background-color: #eff6ff;
  color: #3b82f6;
}

.tip-icon-box.green {
  background-color: #ecfdf5;
  color: #10b981;
}

.tip-icon-box.purple {
  background-color: #f5f3ff;
  color: #8b5cf6;
}

.tip-content {
  flex: 1;
  min-width: 0;
}

.tip-title {
  font-weight: 500;
  margin-bottom: 8rpx;
  font-size: 28rpx;
}

.tip-desc {
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.5;
}

.name-role {
  display: flex;
  align-items: center;
}
</style>

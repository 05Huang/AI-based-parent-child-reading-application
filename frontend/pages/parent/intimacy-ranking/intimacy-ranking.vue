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
                class="ranking-item" :class="{'highlight': user.name === currentUser?.nickname}">
            <view class="rank-number" :class="'medal-' + (index + 1)">{{index + 1}}</view>
            <image :src="user.avatar" class="user-avatar"></image>
            <view class="user-info">
              <view class="info-row">
                <text class="user-name">{{user.name}}</text>
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
import { intimacyApi, userApi } from '@/utils/api.js'

// 响应式状态
const activeTab = ref('global')
const currentUser = ref(null)
const myRank = ref('--')
const myIntimacy = ref('--')
const timeRanges = ['本周', '本月', '全部']
const chartTimeRanges = ['近7天', '近30天']
const currentTimeRange = ref('本周')
const currentFamilyTimeRange = ref('本周')
const currentChartTimeRange = ref('近7天')
const globalRankings = ref([])
const familyRankings = ref([])
const loading = ref(false)
let intimacyChart = null

// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    console.log('开始获取当前用户信息')
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      console.log('获取用户信息成功：', response.data)
      currentUser.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    uni.showToast({
      title: '获取用户信息失败',
      icon: 'none'
    })
  }
}

// 方法
const switchTab = async (tab) => {
  console.log('切换标签页到：', tab)
  activeTab.value = tab
  if (tab === 'global') {
    await loadGlobalRankingData()
  } else {
    await loadFamilyRankingData()
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

const onTimeRangeChange = async (e) => {
  currentTimeRange.value = timeRanges[e.detail.value]
  console.log('全网排行榜时间范围改变为：', currentTimeRange.value)
  await loadGlobalRankingData()
}

const onFamilyTimeRangeChange = async (e) => {
  currentFamilyTimeRange.value = timeRanges[e.detail.value]
  console.log('家庭排行榜时间范围改变为：', currentFamilyTimeRange.value)
  await loadFamilyRankingData()
}

const onChartTimeRangeChange = (e) => {
  currentChartTimeRange.value = chartTimeRanges[e.detail.value]
  loadIntimacyTrend()
}

const loadGlobalRankingData = async () => {
  try {
    if (loading.value) return
    loading.value = true
    
    console.log('开始加载全网亲密度排行榜数据')
    const response = await intimacyApi.getGlobalRanking()
    
    if (response && response.data && response.data.ranking) {
      console.log('获取全网排行榜成功：', response.data.ranking)
      
      // 转换数据格式，添加趋势信息，并过滤掉可能的孩子用户
      globalRankings.value = response.data.ranking
        .filter(item => {
          // 简单过滤：排除昵称中包含"儿子"、"女儿"、"孩子"等关键词的用户
          const childKeywords = ['儿子', '女儿', '孩子', '宝宝', '小朋友'];
          const nickname = item.nickname || '';
          return !childKeywords.some(keyword => nickname.includes(keyword));
        })
        .map((item, index) => ({
          id: `user_${item.rank}`,
          name: item.nickname || `用户${item.rank}`,
          avatar: item.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${index}`,
          intimacy: Math.round(item.percentage || 0),
          childName: '', // 后端没有返回孩子名称，暂时为空
          trend: index % 3 === 0 ? 'up' : (index % 3 === 1 ? 'down' : 'same') // 模拟趋势
        }))
      
      // 查找当前用户的排名和亲密度
      if (currentUser.value) {
        const userIndex = globalRankings.value.findIndex(item => item.name === currentUser.value.nickname)
        if (userIndex !== -1) {
          myRank.value = userIndex + 1
          myIntimacy.value = globalRankings.value[userIndex].intimacy
        }
      }
    }
  } catch (error) {
    console.error('获取全网排行榜失败：', error)
    uni.showToast({
      title: '获取排行榜失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const loadFamilyRankingData = async () => {
  try {
    if (!currentUser.value || loading.value) return
    loading.value = true
    
    console.log('开始加载家庭亲密度排行榜数据，用户ID：', currentUser.value.id)
    const response = await intimacyApi.getUserRanking(currentUser.value.id)
    
    if (response && response.data) {
      console.log('获取家庭排行榜成功：', response.data)
      
      // 更新我的排名和亲密度信息
      myRank.value = response.data.globalRank || '--'
      myIntimacy.value = Math.round(response.data.intimacyPercentage || 0)
      
      // 处理家庭排行榜数据，过滤掉可能的孩子用户
      if (response.data.ranking && response.data.ranking.length > 0) {
        familyRankings.value = response.data.ranking
          .filter(item => {
            // 简单过滤：排除昵称中包含"儿子"、"女儿"、"孩子"等关键词的用户
            const childKeywords = ['儿子', '女儿', '孩子', '宝宝', '小朋友'];
            const nickname = item.nickname || '';
            return !childKeywords.some(keyword => nickname.includes(keyword));
          })
          .map((item, index) => ({
            id: `family_${item.rank}`,
            name: item.nickname || `成员${item.rank}`,
            avatar: item.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=family${index}`,
            intimacy: Math.round(item.percentage || 0),
            role: index === 0 ? '父亲' : (index === 1 ? '母亲' : (index === 2 ? '祖父' : '祖母')), // 简单的角色分配
            trend: index % 3 === 0 ? 'up' : (index % 3 === 1 ? 'down' : 'same') // 模拟趋势
          }))
      }
    }
  } catch (error) {
    console.error('获取家庭排行榜失败：', error)
    // 不显示错误提示，因为可能是没有家庭关系导致的
  } finally {
    loading.value = false
  }
}

const loadIntimacyTrend = async () => {
  try {
    console.log('开始加载亲密度趋势图数据')
    
    // 由于后端暂时没有趋势数据接口，这里使用模拟数据
    const timeRange = currentChartTimeRange.value === '近7天' ? 'week' : 'month'
    const data = {
      week: {
        labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        data: [87, 88, 86, 90, 91, 92, myIntimacy.value || 92],
        average: [83, 84, 85, 85, 86, 87, 87]
      },
      month: {
        labels: ['第1周', '第2周', '第3周', '第4周'],
        data: [85, 87, 89, myIntimacy.value || 92],
        average: [82, 83, 85, 86]
      }
    }
    
    const trendData = data[timeRange]
    
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
        data: trendData.labels
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
          data: trendData.data,
          itemStyle: {
            color: '#3B82F6'
          }
        },
        {
          name: '平均亲密度',
          type: 'line',
          data: trendData.average,
          itemStyle: {
            color: '#9CA3AF'
          }
        }
      ]
    }
    
    intimacyChart.setOption(option)
    console.log('亲密度趋势图加载完成')
  } catch (error) {
    console.error('加载亲密度趋势图失败：', error)
  }
}

// 生命周期钩子
onMounted(async () => {
  console.log('亲密度排行榜页面已挂载，开始加载数据')
  await loadCurrentUser()
  if (activeTab.value === 'global') {
    await loadGlobalRankingData()
  } else {
    await loadFamilyRankingData()
  }
  await loadIntimacyTrend()
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

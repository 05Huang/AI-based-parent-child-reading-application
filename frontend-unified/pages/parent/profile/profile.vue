<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <text class="page-title">个人中心</text>
        <view class="nav-actions">
          <view class="icon-btn" @click="showMyQRCode">
            <text class="fas fa-qrcode"></text>
          </view>
          <view class="icon-btn" @click="navigateToSettings">
            <text class="fas fa-cog"></text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      <!-- 用户信息区域 (视觉降噪，突出核心) -->
      <view class="user-section">
        <view class="user-card">
          <view class="user-basic">
            <image class="avatar" :src="currentUser.avatar" mode="aspectFill"></image>
            <view class="user-meta">
              <view class="name-row">
                <text class="nickname">{{ currentUser.nickname }}</text>
                <view class="level-tag">
                  <text class="fas fa-crown"></text>
                  <text>Lv.{{ userStats.readingLevel }} {{ readingTitle }}</text>
                </view>
              </view>
              <text class="username">ID: {{ currentUser.username }}</text>
            </view>
          </view>
          
          <!-- 核心阅读数据 (替代原有的关注/粉丝) -->
          <view class="data-row">
            <view class="data-item">
              <text class="data-num">{{ userStats.totalReadCount || 0 }}</text>
              <text class="data-label">阅读本数</text>
            </view>
            <view class="data-divider"></view>
            <view class="data-item">
              <text class="data-num">{{ Math.floor((userStats.totalReadDuration || 0) / 60) }}</text>
              <text class="data-label">阅读时长(分)</text>
            </view>
            <view class="data-divider"></view>
            <view class="data-item">
              <text class="data-num">{{ userStats.readingLevel }}</text>
              <text class="data-label">成长等级</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 核心功能入口 (提升优先级) -->
      <view class="function-section">
        <view class="function-grid">
          <view class="function-item" @click="navigateToReadingRecord">
            <view class="function-icon-box blue-box">
              <text class="fas fa-book-open"></text>
            </view>
            <text class="function-name">阅读记录</text>
          </view>
          <view class="function-item" @click="navigateToFavorites">
            <view class="function-icon-box pink-box">
              <text class="fas fa-heart"></text>
            </view>
            <text class="function-name">我的收藏</text>
          </view>
          <view class="function-item" @click="navigateToHistory">
            <view class="function-icon-box purple-box">
              <text class="fas fa-clock"></text>
            </view>
            <text class="function-name">浏览历史</text>
          </view>
          <view class="function-item" @click="navigateToReport">
            <view class="function-icon-box green-box">
              <text class="fas fa-chart-pie"></text>
            </view>
            <text class="function-name">阅读报告</text>
          </view>
        </view>
      </view>

      <!-- 家庭互动 (原亲密度排行，优化布局与展示) -->
      <view class="section-card">
        <view class="section-header">
          <view class="header-left">
            <text class="section-title">家庭亲密度</text>
            <text class="section-subtitle">陪伴是最好的爱</text>
          </view>
          <view class="more-btn" @click="navigateToIntimacyRanking">
            <text>查看全部</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        
        <view class="family-list">
          <view v-for="(item, index) in intimacyRankingPreview" :key="index" class="family-item">
            <view class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</view>
            <image class="item-avatar" :src="item.avatar" mode="aspectFill"></image>
            <view class="item-content">
              <view class="item-top">
                <text class="item-name">{{ item.name }}</text>
                <view class="trend-indicator" :class="item.trend">
                  <text class="fas" :class="item.trend === 'up' ? 'fa-caret-up' : 'fa-caret-down'"></text>
                  <text>{{ item.trend === 'up' ? '上升' : '下降' }}</text>
                </view>
              </view>
              <view class="progress-container">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: item.intimacy + '%' }"></view>
                </view>
                <text class="progress-text">{{ item.intimacy }}%</text>
              </view>
            </view>
          </view>
          
          <view v-if="intimacyRankingPreview.length === 0" class="empty-state">
            <text>暂无家庭互动数据</text>
          </view>
        </view>
      </view>

      <!-- 底部留白 -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 二维码弹窗 -->
    <view v-if="showQRModal" class="qr-modal" @click="closeQRModal">
      <view class="qr-modal-content" @click.stop>
        <view class="qr-close-btn" @click="closeQRModal">
          <text class="fas fa-times"></text>
        </view>
        <view class="qr-bg-decoration"></view>
        <view class="qr-modal-body">
          <view class="qr-title-section">
            <text class="qr-modal-title">我的专属二维码</text>
            <text class="qr-subtitle">扫码绑定家庭成员</text>
          </view>
          <view class="qr-user-card">
            <image class="qr-avatar" :src="currentUser.avatar" mode="aspectFill"></image>
            <view class="qr-user-info">
              <text class="qr-nickname">{{ currentUser.nickname }}</text>
              <text class="qr-username">ID: {{ currentUser.username }}</text>
            </view>
          </view>
          <view class="qr-code-container">
            <view class="qr-code-wrapper">
              <canvas canvas-id="qrCanvas" class="qr-canvas" :style="{ width: qrSize + 'px', height: qrSize + 'px' }"></canvas>
            </view>
            <button class="save-btn" @click="saveQRCode">保存二维码</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userApi, intimacyApi, userBehaviorApi } from '@/utils/api.js'
import eventBus, { EVENTS } from '@/utils/eventBus.js'
import request from '@/utils/request.js'

// 状态栏高度处理
const statusBarHeight = ref(20)
const headerHeight = ref(80)

// 响应式状态
const currentUser = ref({
  nickname: '家长',
  username: 'parent',
  id: null,
  avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=parent_default'
})

const userStats = ref({
  readingLevel: 1,
  totalReadDuration: 0,
  totalReadCount: 0,
  followingCount: 0,
  followersCount: 0
})

const getReadingTitle = (level) => {
  const lv = Math.max(1, Number(level) || 1)
  if (lv >= 20) return '阅读宗师'
  if (lv >= 10) return '阅读专家'
  if (lv >= 4) return '阅读大师'
  if (lv === 3) return '阅读达人'
  if (lv === 2) return '阅读爱好者'
  return '阅读新手'
}

const readingTitle = computed(() => getReadingTitle(userStats.value.readingLevel))

const calculateReadingLevel = (totalReadCount, totalReadDurationSeconds) => {
  const safeCount = Math.max(0, Number(totalReadCount) || 0)
  const safeSeconds = Math.max(0, Number(totalReadDurationSeconds) || 0)
  const levelFromBooks = Math.floor(safeCount / 2)
  const levelFromTime = Math.floor(safeSeconds / 1800)
  const lv = 1 + levelFromBooks + levelFromTime
  return Math.min(100, Math.max(1, lv))
}

const lastStatsSnapshot = ref(null)

const intimacyRankingPreview = ref([])
const showQRModal = ref(false)
const qrSize = 200

// 页面加载
onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  headerHeight.value = statusBarHeight.value + 44
  
  refreshAllData()
  registerEventListeners()
})

onShow(() => {
  refreshAllData()
})

// 页面跳转方法
const navigateToIntimacyRanking = () => {
  uni.navigateTo({ url: '/pages/parent/intimacy-ranking/intimacy-ranking' })
}

const navigateToReadingRecord = () => {
  uni.navigateTo({ url: '/pages/parent/profile/details/readTheTranscript' })
}

const navigateToFavorites = () => {
  uni.navigateTo({ url: '/pages/parent/profile/details/favorites' })
}

const navigateToHistory = () => {
  uni.navigateTo({ url: '/pages/parent/profile/details/history' })
}

const navigateToReport = () => {
  uni.navigateTo({ url: '/pages/parent/profile/details/report' })
}

const navigateToSettings = () => {
  uni.navigateTo({ url: '/pages/parent/profile/details/setting' })
}

// 二维码相关
const showMyQRCode = () => {
  if (!currentUser.value.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  showQRModal.value = true
  setTimeout(() => generateQRCode(), 100)
}

const closeQRModal = () => showQRModal.value = false

const generateQRCode = () => {
  const qrContent = `qz-family-bind:${currentUser.value.id}:${currentUser.value.username}`
  const ctx = uni.createCanvasContext('qrCanvas')
  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=${qrSize}x${qrSize}&data=${encodeURIComponent(qrContent)}`
  
  uni.downloadFile({
    url: qrUrl,
    success: (res) => {
      if (res.statusCode === 200) {
        ctx.drawImage(res.tempFilePath, 0, 0, qrSize, qrSize)
        ctx.draw()
      }
    }
  })
}

const saveQRCode = () => {
  uni.canvasToTempFilePath({
    canvasId: 'qrCanvas',
    success: (res) => {
      uni.saveImageToPhotosAlbum({
        filePath: res.tempFilePath,
        success: () => uni.showToast({ title: '已保存到相册', icon: 'success' }),
        fail: () => uni.showToast({ title: '保存失败', icon: 'none' })
      })
    }
  })
}

// 数据加载方法
const loadCurrentUser = async () => {
  try {
    const response = await userApi.getCurrentUser()
    if (response?.data) {
      currentUser.value = {
        ...currentUser.value,
        ...response.data,
        avatar: response.data.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(response.data.nickname || 'Parent')}&background=3b82f6&color=fff`
      }
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

const loadUserStats = async (payload) => {
  try {
    if (!currentUser.value.id) return
    const prev = {
      totalReadDuration: userStats.value.totalReadDuration,
      totalReadCount: userStats.value.totalReadCount,
      readingLevel: userStats.value.readingLevel
    }
    const browsingStats = await userBehaviorApi.getBrowsingStats(currentUser.value.id)
    if (browsingStats?.data) {
      const totalReadDuration =
        browsingStats.data.totalReadDuration ??
        browsingStats.data.totalReadingTime ??
        0
      const totalReadCount = browsingStats.data.totalReadCount ?? 0

      userStats.value.totalReadDuration = totalReadDuration
      userStats.value.totalReadCount = totalReadCount
      userStats.value.readingLevel = calculateReadingLevel(totalReadCount, totalReadDuration)

      if (payload) {
        console.log('[个人中心] 收到统计更新事件：', payload)
      }

      const diffBooks = (userStats.value.totalReadCount || 0) - (prev.totalReadCount || 0)
      if (diffBooks > 0) {
        console.log(`[个人中心] 阅读本数+${diffBooks}，当前共 ${userStats.value.totalReadCount} 本`)
      }

      const diffSeconds = (userStats.value.totalReadDuration || 0) - (prev.totalReadDuration || 0)
      if (diffSeconds > 0) {
        console.log(`[个人中心] 阅读时长+${Math.floor(diffSeconds / 60)} 分钟，当前共 ${Math.floor((userStats.value.totalReadDuration || 0) / 60)} 分钟`)
      }

      if (userStats.value.readingLevel !== prev.readingLevel) {
        console.log(`[个人中心] 成长等级变更：Lv.${prev.readingLevel} → Lv.${userStats.value.readingLevel}（${readingTitle.value}）`)
      }

      lastStatsSnapshot.value = {
        totalReadDuration: userStats.value.totalReadDuration,
        totalReadCount: userStats.value.totalReadCount,
        readingLevel: userStats.value.readingLevel
      }
    }
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

const loadIntimacyRankingPreview = async () => {
  try {
    if (!currentUser.value.id) return
    const response = await intimacyApi.getUserRanking(currentUser.value.id)
    if (response?.data?.ranking) {
      const filtered = response.data.ranking.filter(item => {
        // 简单过滤，实际应根据业务逻辑
        return item.id !== currentUser.value.id
      })
      
      intimacyRankingPreview.value = filtered.slice(0, 3).map((item, index) => ({
        rank: index + 1,
        name: item.nickname || '家庭成员',
        avatar: item.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(item.nickname)}&background=random`,
        intimacy: Math.round(item.percentage || 0),
        trend: Math.random() > 0.5 ? 'up' : 'down' // 模拟趋势
      }))
    }
  } catch (e) {
    console.error('获取亲密度失败', e)
    // 默认数据用于展示效果
    intimacyRankingPreview.value = [
      { rank: 1, name: '孩子', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=child', intimacy: 88, trend: 'up' },
      { rank: 2, name: '伴侣', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=partner', intimacy: 75, trend: 'down' }
    ]
  }
}

const refreshAllData = async () => {
  await loadCurrentUser()
  await loadUserStats()
  await loadIntimacyRankingPreview()
}

const isEventListenerRegistered = ref(false)
const registerEventListeners = () => {
  if (isEventListenerRegistered.value) return
  eventBus.on(EVENTS.USER_INFO_UPDATED, loadCurrentUser)
  eventBus.on(EVENTS.USER_STATS_UPDATED, loadUserStats)
  isEventListenerRegistered.value = true
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f8faff;
  box-sizing: border-box;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: #ffffff;
  padding-bottom: 20rpx;
}

.nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32rpx;
  height: 88rpx;
}

.page-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1f2937;
}

.nav-actions {
  display: flex;
  gap: 24rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #4b5563;
  transition: all 0.2s;
}

.icon-btn:active {
  background: #e5e7eb;
}

/* 主内容区 */
.main-content {
  padding: 32rpx;
  box-sizing: border-box;
}

/* 用户卡片 */
.user-section {
  margin-bottom: 40rpx;
}

.user-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.04);
}

.user-basic {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  border: 4rpx solid #fff;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.user-meta {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
  flex-wrap: wrap;
}

.nickname {
  font-size: 36rpx;
  font-weight: 800;
  color: #1f2937;
  margin-right: 16rpx;
}

.level-tag {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.username {
  font-size: 24rpx;
  color: #9ca3af;
}

.data-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-top: 24rpx;
  border-top: 2rpx solid #f3f4f6;
}

.data-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.data-num {
  font-size: 36rpx;
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 4rpx;
}

.data-label {
  font-size: 22rpx;
  color: #6b7280;
}

.data-divider {
  width: 2rpx;
  height: 40rpx;
  background: #f3f4f6;
}

/* 功能区 */
.function-section {
  margin-bottom: 40rpx;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  background: #ffffff;
  padding: 32rpx 20rpx;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.function-icon-box {
  width: 96rpx;
  height: 96rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  transition: transform 0.2s;
}

.function-item:active .function-icon-box {
  transform: scale(0.95);
}

.blue-box { background: #eff6ff; color: #3b82f6; }
.pink-box { background: #fff1f2; color: #ec4899; }
.purple-box { background: #f5f3ff; color: #8b5cf6; }
.green-box { background: #ecfdf5; color: #10b981; }

.function-name {
  font-size: 24rpx;
  color: #4b5563;
  font-weight: 500;
}

/* 通用区块 */
.section-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
  margin-bottom: 40rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #1f2937;
  margin-right: 12rpx;
}

.section-subtitle {
  font-size: 22rpx;
  color: #9ca3af;
}

.more-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 20rpx;
  background: #f3f4f6;
  border-radius: 24rpx;
}

.more-btn text {
  font-size: 22rpx;
  color: #6b7280;
}

/* 家庭列表 */
.family-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.family-item {
  display: flex;
  align-items: center;
}

.rank-badge {
  width: 40rpx;
  height: 40rpx;
  border-radius: 12rpx;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 24rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.rank-1 { background: #fff7ed; color: #f59e0b; }
.rank-2 { background: #eff6ff; color: #3b82f6; }
.rank-3 { background: #fff1f2; color: #ec4899; }

.item-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  margin-right: 24rpx;
}

.item-content {
  flex: 1;
}

.item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
}

.trend-indicator {
  font-size: 20rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.trend-indicator.up { color: #ef4444; }
.trend-indicator.down { color: #10b981; }

.progress-container {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: #f3f4f6;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  border-radius: 6rpx;
}

.progress-text {
  font-size: 24rpx;
  color: #6b7280;
  width: 60rpx;
  text-align: right;
}

.empty-state {
  text-align: center;
  padding: 40rpx 0;
  color: #9ca3af;
  font-size: 26rpx;
}

.bottom-spacer {
  height: 100rpx;
}

/* 二维码弹窗 */
.qr-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.qr-modal-content {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 40rpx;
  position: relative;
  overflow: hidden;
  padding-bottom: 40rpx;
}

.qr-bg-decoration {
  height: 160rpx;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}

.qr-close-btn {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 64rpx;
  height: 64rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  color: #fff;
}

.qr-modal-body {
  position: relative;
  z-index: 1;
  padding: 60rpx 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-title-section {
  text-align: center;
  margin-bottom: 40rpx;
  color: #fff;
}

.qr-modal-title {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.qr-subtitle {
  font-size: 24rpx;
  opacity: 0.9;
}

.qr-user-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
  margin-bottom: 40rpx;
  width: 100%;
  box-sizing: border-box;
}

.qr-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  margin-right: 24rpx;
}

.qr-user-info {
  display: flex;
  flex-direction: column;
}

.qr-nickname {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
}

.qr-username {
  font-size: 24rpx;
  color: #9ca3af;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.qr-code-wrapper {
  background: #fff;
  padding: 24rpx;
  border-radius: 24rpx;
  border: 2rpx dashed #e5e7eb;
  margin-bottom: 32rpx;
}

.save-btn {
  background: #3b82f6;
  color: #fff;
  font-size: 28rpx;
  padding: 16rpx 60rpx;
  border-radius: 40rpx;
  border: none;
}
</style>

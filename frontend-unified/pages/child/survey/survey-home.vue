<template>
  <view class="container">
    <!-- 自定义顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <view class="back-btn" @tap="goBack">
          <view class="nav-back-icon"></view>
        </view>
        <text class="nav-title">创作者激励计划</text>
        <view class="nav-placeholder"></view> <!-- 占位，保持标题居中 -->
      </view>
    </view>

    <!-- 主要内容滚动区 -->
    <scroll-view 
      scroll-y 
      class="main-scroll"
      :enable-back-to-top="true"
    >
      <!-- 顶部 Banner 区域 -->
      <view class="banner-wrapper">
        <view class="banner-card">
          <image 
            src="/static/images/backgrounds/问卷.png" 
            class="banner-image" 
            mode="widthFix"
          ></image>
          <view class="banner-mask">
            <view class="banner-text-group">
              <text class="banner-title">阅桥亲子阅读APP创作者激励计划</text>
              <text class="banner-sub">分享你的故事，点亮阅读之光</text>
            </view>
          </view>
        </view>

        <!-- 引言文本 -->
        <view class="quote-box">
          <text class="quote-text">我们相信每个家庭都有独特的阅读故事。通过分享你的阅读经历和见解，你将帮助我们为更多家庭提供更好的阅读体验。</text>
        </view>
      </view>

      <!-- 调研列表区域 -->
      <view class="section-container">
        <view class="section-header">
          <view class="header-left">
            <view class="indicator-dot"></view>
            <text class="header-title">参与调研</text>
          </view>
          <view class="progress-tag">
            {{ completedCount }}/{{ totalCount }} 已完成
          </view>
      </view>

        <!-- 列表 -->
        <view class="task-list">
          <view 
            v-for="(survey, index) in surveys" 
            :key="index"
            class="task-item"
            :class="{'item-disabled': survey.locked}"
            @tap="handleSurveyClick(survey)"
          >
            <!-- 左侧：图标 -->
            <view class="task-icon-box">
              <view class="icon-lines">
                <view class="line short"></view>
                <view class="line long"></view>
                <view class="line long"></view>
              </view>
              </view>

            <!-- 中间：信息 -->
            <view class="task-info">
              <text class="task-title">{{ survey.title }}</text>
              <text class="task-desc">{{ survey.description }}</text>
              <view class="task-meta">
                <text class="meta-time">预计 {{ survey.duration }} 分钟</text>
                <text class="meta-points">+{{ survey.points }} 积分</text>
              </view>
            </view>

            <!-- 右侧：按钮 -->
            <view class="task-action">
              <button 
                class="action-btn"
                :class="{
                  'btn-completed': survey.completed,
                  'btn-locked': survey.locked,
                  'btn-active': !survey.completed && !survey.locked
                }"
              >
                <text v-if="survey.completed">已完成</text>
                <text v-else-if="survey.locked">未解锁</text>
                <text v-else>去填写</text>
                <view v-if="!survey.completed && !survey.locked" class="btn-arrow"></view>
              </button>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="surveys.length === 0" class="empty-box">
          <image src="https://cdn-icons-png.flaticon.com/512/7486/7486747.png" class="empty-img" mode="widthFix"></image>
          <text class="empty-txt">暂无调查问卷</text>
      </view>

      </view>
      
      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const surveys = ref([])
const totalCount = ref(0)
const completedCount = ref(0)

onMounted(() => {
  loadSurveys()
})

// 页面显示时刷新完成状态（用户可能从问卷页面返回）
onShow(() => {
  const userId = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
  if (userId && surveys.value && surveys.value.length > 0) {
    checkCompletedStatus(userId)
  }
})

const loadSurveys = async () => {
  try {
    // 获取用户ID
    const userId = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
    
    // 只获取"进行中"状态的问卷（status=1），过滤掉已关闭和未发布的问卷
    const response = await request.get('/api/surveystar/list', {
      current: 1,
      size: 50,
      status: 1  // 1-进行中（已发布），2-已结束（已关闭），3-未发布
    })
    
    if (response && response.code === 200) {
      let records = []
      if (response.data) {
        if (response.data.records && Array.isArray(response.data.records)) {
          records = response.data.records
        } else if (Array.isArray(response.data)) {
          records = response.data
        }
      }
      
      if (records.length > 0) {
        // 先构建问卷列表
        surveys.value = records.map((survey, index) => {
          return {
            id: survey.questId || survey.id,
            title: survey.title || '未命名问卷',
            description: survey.description || '暂无描述',
            duration: 3 + (index % 5),
            points: 50 + (index * 10), 
            completed: false, // 初始化为未完成，稍后检查
            locked: survey.status === 3, 
            surveyUrl: survey.surveyUrl
          }
        })
        
        totalCount.value = response.data.total || records.length
        
        // 如果有用户ID，批量检查完成状态
        if (userId) {
          await checkCompletedStatus(userId)
        } else {
          completedCount.value = 0
        }
      } else {
        surveys.value = []
        totalCount.value = 0
        completedCount.value = 0
      }
    } else {
      surveys.value = []
      totalCount.value = 0
      completedCount.value = 0
    }
  } catch (error) {
    console.error('❌ 加载问卷列表失败', error)
    surveys.value = []
    totalCount.value = 0
    completedCount.value = 0
  }
}

// 批量检查用户完成状态
const checkCompletedStatus = async (userId) => {
  try {
    if (!surveys.value || surveys.value.length === 0) {
      return
    }
    
    // 获取所有问卷ID
    const questIds = surveys.value.map(s => s.id).filter(id => id != null)
    
    if (questIds.length === 0) {
      return
    }
    
    console.log('🔍 批量检查用户完成状态，用户ID:', userId, '问卷数量:', questIds.length)
    
    // 调用批量检查接口（将 userId 作为查询参数）
    const url = `/api/surveystar/check-completed-batch?userId=${encodeURIComponent(userId)}`
    const response = await request.post(url, questIds)
    
    if (response && response.code === 200) {
      const completedIds = response.data || []
      console.log('✅ 已完成问卷ID列表:', completedIds)
      
      // 更新问卷的完成状态
      surveys.value.forEach(survey => {
        survey.completed = completedIds.includes(survey.id)
      })
      
      // 更新完成数量
      completedCount.value = completedIds.length
      
      console.log('✅ 完成状态更新完成，已完成数量:', completedCount.value)
    } else {
      console.warn('⚠️ 检查完成状态失败，使用默认值')
      completedCount.value = 0
    }
  } catch (error) {
    console.error('❌ 检查用户完成状态失败', error)
    // 失败时不影响显示，只是不显示完成状态
    completedCount.value = 0
  }
}

const handleSurveyClick = (survey) => {
  if (survey.locked) {
    uni.showToast({ title: '此问卷还未解锁', icon: 'none' })
        return
      }
      
  if (survey.completed) {
    uni.showModal({
      title: '提示',
      content: '问卷已完成，是否重新查看？',
      success: (res) => {
        if (res.confirm) goToSurvey(survey)
      }
    })
      } else {
    goToSurvey(survey)
  }
}

const goToSurvey = (survey) => {
  const encodedTitle = encodeURIComponent(survey.title)
  const url = `/pages/child/survey/survey-web-view?surveyId=${survey.id}&title=${encodedTitle}`
  uni.navigateTo({ 
    url,
    success: () => {
      console.log('✅ 跳转到问卷页面成功')
    },
    fail: (err) => {
      console.error('❌ 跳转失败', err)
      uni.showToast({
        title: '打开问卷失败',
        icon: 'none'
      })
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
/* 全局容器 */
.container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F6F7F9; /* 更柔和的灰底 */
}

/* 顶部导航栏 */
.nav-bar {
  background: linear-gradient(90deg, #7C3AED 0%, #8B5CF6 100%);
  padding-top: var(--status-bar-height);
  padding-bottom: 20rpx;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.nav-back-icon {
  width: 20rpx;
  height: 20rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg);
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #ffffff;
}

.nav-placeholder {
  width: 60rpx;
}

/* 滚动区域 */
.main-scroll {
  flex: 1;
  height: 0;
}

/* Banner 区域 */
.banner-wrapper {
  padding: 30rpx;
  background: #fff;
  border-radius: 0 0 40rpx 40rpx; /* 底部圆角 */
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
  margin-bottom: 24rpx;
}

.banner-card {
  position: relative;
  width: 100%;
  min-height: 320rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(124, 58, 237, 0.2);
}

.banner-image {
  width: 100%;
  height: auto;
  display: block;
}

.banner-mask {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0) 40%, rgba(0,0,0,0.7) 100%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 30rpx;
}

.banner-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8rpx;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.3);
}

.banner-sub {
  font-size: 26rpx;
  color: rgba(255,255,255,0.9);
}

.quote-box {
  margin-top: 24rpx;
  padding: 0 8rpx;
}

.quote-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  text-align: justify;
}

/* 调研列表容器 */
.section-container {
  padding: 0 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding-top: 20rpx;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.indicator-dot {
  width: 8rpx;
  height: 28rpx;
  background: #7C3AED;
  border-radius: 4rpx;
}

.header-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
}

.progress-tag {
  font-size: 24rpx;
  color: #7C3AED;
  background: rgba(124, 58, 237, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

/* 任务列表项样式 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.task-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
  transition: transform 0.1s ease;
}

.task-item:active {
  transform: scale(0.99);
}

.task-item.item-disabled {
  opacity: 0.7;
  background: #FCFCFC;
}

/* 图标区域 */
.task-icon-box {
  width: 88rpx;
  height: 88rpx;
  background: #F0F2F5;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-lines {
  width: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.line {
  height: 6rpx;
  background: #8B9BB4; /* 灰蓝色 */
  border-radius: 4rpx;
}

.line.short {
  width: 24rpx;
  background: #7C3AED; /* 紫色点缀 */
}

.line.long {
  width: 100%;
}

/* 中间文字 */
.task-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden; /* 防止文字撑开 */
}

.task-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-desc {
  font-size: 24rpx;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-top: 4rpx;
}

.meta-time {
  font-size: 22rpx;
  color: #999;
}

.meta-points {
  font-size: 24rpx;
  color: #FF9500; /* 橙色高亮积分 */
  font-weight: bold;
}

/* 右侧按钮 */
.task-action {
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 0 24rpx;
  height: 64rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  font-weight: 500;
  line-height: 64rpx;
  border: none;
}

.action-btn::after {
  border: none;
}

/* 按钮状态 */
.btn-active {
  background: rgba(59, 130, 246, 0.1); /* 浅蓝背景 */
  color: #3B82F6; /* 蓝色文字 */
}

.btn-completed {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.btn-locked {
  background: #F3F4F6;
  color: #9CA3AF;
}

.btn-arrow {
  width: 10rpx;
  height: 10rpx;
  border-top: 3rpx solid currentColor;
  border-right: 3rpx solid currentColor;
  transform: rotate(45deg);
  margin-left: 4rpx;
  margin-top: 2rpx;
}

/* 空状态 */
.empty-box {
  padding: 100rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
}

.empty-img {
  width: 160rpx;
  opacity: 0.5;
}

.empty-txt {
  font-size: 28rpx;
  color: #999;
}

.safe-bottom {
  height: calc(40rpx + env(safe-area-inset-bottom));
}
</style>
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">阅读报告</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 总览卡片 -->
      <view class="card overview-card">
        <view class="card-header">
          <text class="card-title big-title">本月阅读概览</text>
          <view class="date-picker" @click="showDatePicker">
            <text class="fas fa-calendar picker-icon"></text>
            <text class="date-text">{{selectedDateText}}</text>
            <text class="fas fa-chevron-down picker-arrow"></text>
          </view>
        </view>
        
        <view class="overview-grid">
          <view class="overview-item">
            <view class="overview-icon-bg indigo">
              <text class="fas fa-clock overview-icon"></text>
            </view>
            <view class="overview-data">
              <text class="overview-value">{{reportDisplay.totalDuration}}</text>
              <text class="overview-label">总阅读时长(分钟)</text>
            </view>
          </view>
          
          <view class="overview-item">
            <view class="overview-icon-bg green">
              <text class="fas fa-book-open overview-icon"></text>
            </view>
            <view class="overview-data">
              <text class="overview-value">{{reportDisplay.totalArticles}}</text>
              <text class="overview-label">阅读文章(篇)</text>
            </view>
          </view>
          
          <view class="overview-item">
            <view class="overview-icon-bg orange">
              <text class="fas fa-comments overview-icon"></text>
            </view>
            <view class="overview-data">
              <text class="overview-value">{{reportDisplay.interactionCount}}</text>
              <text class="overview-label">互动次数(次)</text>
            </view>
          </view>
          
          <view class="overview-item">
            <view class="overview-icon-bg purple">
              <text class="fas fa-chart-line overview-icon"></text>
            </view>
            <view class="overview-data">
              <text class="overview-value">{{reportDisplay.activityScore}}%</text>
              <text class="overview-label">活跃度</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 阅读习惯分析 -->
      <view class="card habits-card">
        <view class="card-header">
          <text class="card-title">阅读习惯分析</text>
        </view>
        <view class="habits-container">
          <view class="habit-box">
            <view class="habit-icon-wrapper indigo-light">
              <text class="fas fa-hourglass-half habit-icon"></text>
            </view>
            <view class="habit-content">
              <text class="habit-label">平均阅读时长</text>
              <text class="habit-number">{{reportDisplay.avgReadTime}}<text class="habit-unit">分钟/篇</text></text>
            </view>
          </view>
          
          <view class="habit-divider"></view>
          
          <view class="habit-box">
            <view class="habit-icon-wrapper orange-light">
              <text class="fas fa-heart habit-icon"></text>
            </view>
            <view class="habit-content">
              <text class="habit-label">最爱内容类型</text>
              <text class="habit-number text-truncate">{{reportDisplay.favoriteCategory}}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 本月热门文章 -->
      <view class="card books-card">
        <view class="card-header">
          <text class="card-title">本月热门文章</text>
          <view class="view-all-btn" @click="goToAllArticles">
            <text>查看全部</text>
            <text class="fas fa-chevron-right icon-small"></text>
          </view>
        </view>
        <scroll-view scroll-x="true" class="books-scroll">
          <view 
            class="book-item" 
            v-for="(article, index) in completedArticles" 
            :key="index"
            @click="viewArticle(article)"
          >
            <image :src="article.coverUrl" class="book-cover" mode="aspectFill"></image>
            <view class="book-info">
              <text class="book-title">{{article.title}}</text>
              <view class="article-stats">
                <view class="stat-pill">
                  <text class="fas fa-thumbs-up stat-icon"></text>
                  <text>{{article.likes}}</text>
                </view>
                <view class="stat-pill">
                  <text class="fas fa-comment stat-icon"></text>
                  <text>{{article.comments}}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </scroll-view>
    
    <!-- 时间选择器弹窗 -->
    <view v-if="showDatePickerModal" class="date-picker-modal" @click="hideDatePicker">
      <view class="date-picker-content" @click.stop>
        <view class="date-picker-header">
          <text class="date-picker-title">选择时间</text>
          <view class="close-btn" @click="hideDatePicker">
            <text class="fas fa-times"></text>
          </view>
        </view>
        
        <view class="date-picker-body">
          <!-- 年份选择 -->
          <view class="picker-section">
            <text class="picker-label">年份</text>
            <scroll-view scroll-y="true" class="picker-scroll">
              <view 
                v-for="year in yearOptions" 
                :key="year"
                class="picker-item"
                :class="{ 'active': selectedYear === year }"
                @click="selectYear(year)"
              >
                {{year}}年
              </view>
            </scroll-view>
          </view>
          
          <!-- 月份选择 -->
          <view class="picker-section">
            <text class="picker-label">月份</text>
            <scroll-view scroll-y="true" class="picker-scroll">
              <view 
                v-for="month in monthOptions" 
                :key="month"
                class="picker-item"
                :class="{ 'active': selectedMonth === month }"
                @click="selectMonth(month)"
              >
                {{month}}月
              </view>
            </scroll-view>
          </view>
        </view>
        
        <view class="date-picker-footer">
          <view class="picker-btn cancel" @click="hideDatePicker">取消</view>
          <view class="picker-btn confirm" @click="confirmDateSelection">确定</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userBehaviorApi, userApi, viewHistoryApi } from '@/utils/api.js'

// 响应式状态
const currentUser = ref(null)
const weeklyReport = ref({
  totalReadDuration: 0,
  totalArticleCount: 0,
  interactionCount: 0,
  activityScore: 0,
  dailyAvgReadTime: 0,
  favoriteCategory: '未知'
})
const completedArticles = ref([])
const loading = ref(false)

// 时间选择相关状态
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const showDatePickerModal = ref(false)

// 计算选中的时间显示文本
const selectedDateText = computed(() => {
  return `${selectedYear.value}年${selectedMonth.value}月`
})

// 年份选项（最近3年到当前年）
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  return [currentYear - 2, currentYear - 1, currentYear]
})

// 月份选项（根据选择的年份限制）
const monthOptions = computed(() => {
  const currentDate = new Date()
  const currentYear = currentDate.getFullYear()
  const currentMonth = currentDate.getMonth() + 1
  
  // 如果选择的是当前年，只能选择到当前月
  if (selectedYear.value === currentYear) {
    const months = []
    for (let i = 1; i <= currentMonth; i++) {
      months.push(i)
    }
    return months
  }
  
  // 其他年份可以选择全年
  return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
})

// 计算月报统计数据显示
const reportDisplay = computed(() => {
  // 后端返回的activityScore是0-26分的绝对分数（满分26 = 20*0.3 + 20*0.4 + 40*0.3）
  // 需要转换为百分比：(分数 / 26) * 100
  const rawScore = weeklyReport.value.activityScore || 0
  const activityPercent = Math.min(Math.round((rawScore / 26) * 100), 100) // 转为百分比，最高100%
  
  console.log('[儿童端阅读报告] 活跃度计算 - 原始分数:', rawScore, '转换后百分比:', activityPercent)
  
  // 计算平均阅读时长（分钟/篇）
  const duration = Math.floor((weeklyReport.value.totalReadDuration || 0) / 60)
  const count = weeklyReport.value.totalArticleCount || 0
  const avgTime = count > 0 ? (duration / count).toFixed(1) : 0
  
  return {
    totalDuration: duration, // 转为分钟
    totalArticles: count,
    interactionCount: weeklyReport.value.interactionCount || 0,
    activityScore: activityPercent, // 显示为百分比
    avgReadTime: avgTime, // 平均每篇阅读时长
    favoriteCategory: weeklyReport.value.favoriteCategory || '科普知识'
  }
})
// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    console.log('[儿童端阅读报告] 开始获取当前用户信息')
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      console.log('[儿童端阅读报告] 获取用户信息成功：', response.data)
      currentUser.value = response.data
    }
  } catch (error) {
    console.error('[儿童端阅读报告] 获取用户信息失败：', error)
          uni.showToast({
      title: '获取用户信息失败',
            icon: 'none'
          })
        }
      }
      
// 获取周报数据
const loadWeeklyReport = async () => {
  try {
    if (!currentUser.value?.id) {
      console.log('[儿童端阅读报告] 用户ID不存在，跳过加载周报数据')
        return
      }
      
    console.log('[儿童端阅读报告] 开始获取周报数据，用户ID：', currentUser.value.id, '选择时间：', selectedYear.value, '年', selectedMonth.value, '月')
    const response = await userBehaviorApi.getWeeklyReport(currentUser.value.id, selectedYear.value.toString(), selectedMonth.value.toString())
    
    console.log('[儿童端阅读报告] 周报API完整响应：', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      console.log('[儿童端阅读报告] 获取周报数据成功：', response.data)
      
      // 确保所有字段都有默认值
      weeklyReport.value = {
        totalReadDuration: response.data.totalReadDuration || 0,
        totalArticleCount: response.data.totalArticleCount || 0,
        interactionCount: response.data.interactionCount || 0,
        activityScore: response.data.activityScore || 0,
        dailyAvgReadTime: response.data.dailyAvgReadTime || 0,
        favoriteCategory: response.data.favoriteCategory || '科普知识'
      }
      console.log('[儿童端阅读报告] 更新后的weeklyReport：', weeklyReport.value)
    } else {
      console.warn('[儿童端阅读报告] 周报响应格式异常或无数据，使用默认值')
    }
  } catch (error) {
    console.error('[儿童端阅读报告] 获取周报数据失败：', error)
    // 不显示错误提示，使用默认值
  }
}


// 获取热门文章（从浏览历史中提取）
const loadHotArticles = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    console.log('[儿童端阅读报告] 开始获取热门文章，用户ID：', currentUser.value.id)
    
    // 计算选择的时间范围
    const startTime = `${selectedYear.value}-${selectedMonth.value.toString().padStart(2, '0')}-01 00:00:00`
    const endTime = new Date(selectedYear.value, selectedMonth.value, 0).toISOString().slice(0, 19).replace('T', ' ')
    
    console.log('[儿童端阅读报告] 查询时间范围：', startTime, '到', endTime)
    
    const response = await viewHistoryApi.getUserViewHistory(currentUser.value.id, {
          current: 1,
      size: 10,
      startTime: startTime,
      endTime: endTime
    })
    
    if (response && response.data && response.data.records) {
      console.log('[儿童端阅读报告] 获取浏览历史成功，筛选热门文章')
      
      // 过滤有效数据并转换
      const validRecords = response.data.records.filter(record => {
        return record.contentId && 
               record.contentTitle && 
               record.contentTitle.trim() !== '' &&
               record.contentTitle !== '无标题'
      })
      
      // 转换并筛选热门文章（取前3个）
      const articles = validRecords.slice(0, 3).map(record => ({
        id: record.contentId,
        title: record.contentTitle,
        coverUrl: record.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
        duration: record.duration || 0,
        likes: record.likeCount || 0,
        comments: record.commentCount || 0,
        contentId: record.contentId,
        contentType: record.contentType || 1
      }))
      
      completedArticles.value = articles
      console.log('[儿童端阅读报告] 热门文章加载完成，共', articles.length, '篇')
    }
  } catch (error) {
    console.error('[儿童端阅读报告] 获取热门文章失败：', error)
    // 使用默认文章
    completedArticles.value = [
      {
        title: '神奇的海洋世界',
        coverUrl: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
        duration: 120,
        likes: 256,
        comments: 45
      },
      {
        title: '探索宇宙奥秘',
        coverUrl: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop',
        duration: 90,
        likes: 189,
        comments: 32
      },
      {
        title: '动物王国大冒险',
        coverUrl: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&auto=format&fit=crop',
        duration: 150,
        likes: 324,
        comments: 56
      }
    ]
  } finally {
    loading.value = false
  }
}

// 显示时间选择器
const showDatePicker = () => {
  console.log('[儿童端阅读报告] 显示时间选择器')
  showDatePickerModal.value = true
}

// 隐藏时间选择器
const hideDatePicker = () => {
  showDatePickerModal.value = false
}

// 选择年份
const selectYear = (year) => {
  selectedYear.value = year
  console.log('[儿童端阅读报告] 选择年份：', year)
}

// 选择月份
const selectMonth = (month) => {
  selectedMonth.value = month
  console.log('[儿童端阅读报告] 选择月份：', month)
}

// 确认时间选择
const confirmDateSelection = async () => {
  console.log('[儿童端阅读报告] 确认时间选择：', selectedYear.value, '年', selectedMonth.value, '月')
  showDatePickerModal.value = false
  
  // 显示加载提示
  uni.showLoading({
    title: '加载数据中...'
  })
  
  try {
    // 重新加载数据
    await loadWeeklyReport()
    await loadHotArticles()
    
    uni.hideLoading()
    uni.showToast({
      title: '数据已更新',
      icon: 'success',
      duration: 1500
    })
  } catch (error) {
    console.error('[儿童端阅读报告] 加载数据失败：', error)
    uni.hideLoading()
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 查看文章详情
const viewArticle = (article) => {
  console.log('[儿童端阅读报告] 查看文章：', article.title, '文章ID：', article.id)
  
  if (!article || !article.id) {
    console.error('[儿童端阅读报告] 文章信息不完整，无法跳转')
    uni.showToast({
      title: '文章信息错误',
      icon: 'none'
    })
    return
  }
  
  // 跳转到儿童端文章详情页
  uni.navigateTo({
    url: `/pages/child/article-detail?id=${article.id}`,
    success: () => {
      console.log('[儿童端阅读报告] 跳转到阅读页面成功')
    },
    fail: (err) => {
      console.error('[儿童端阅读报告] 跳转到阅读页面失败:', err)
      uni.showToast({
        title: '跳转失败',
        icon: 'none'
      })
    }
  })
}

// 跳转到全部文章页面
const goToAllArticles = () => {
  console.log('[儿童端阅读报告] 跳转到浏览记录页面')
  uni.navigateTo({
    url: '/pages/child/profile-detail/browse-history',
    success: () => {
      console.log('[儿童端阅读报告] 跳转成功')
    },
    fail: (err) => {
      console.error('[儿童端阅读报告] 跳转失败:', err)
      uni.showToast({
        title: '跳转失败',
        icon: 'none'
      })
    }
  })
}

// 加载所有数据的统一方法
const loadAllData = async () => {
  console.log('[儿童端阅读报告] 加载所有数据')
  await loadCurrentUser()
  await loadWeeklyReport()
  await loadHotArticles()
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('[儿童端阅读报告] 页面已挂载，开始加载数据')
  await loadAllData()
})

// 页面显示时刷新数据（从其他页面返回时会触发）
onShow(async () => {
  console.log('[儿童端阅读报告] 页面显示，刷新数据')
  await loadAllData()
})
</script>

<style>
/* 引入Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  width: 100%;
  box-sizing: border-box;
  padding-bottom: 40rpx;
}

/* 顶部导航样式 */
.nav-header {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
}

.nav-content {
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

.back-btn {
  padding: 20rpx;
  margin: -20rpx;
  margin-right: 10rpx;
  color: #1f2937;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #111827;
}

/* 主要内容区域 */
.main-content {
  margin-top: 88rpx;
  padding: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

/* 卡片通用样式 */
.card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
}

.card-title.big-title {
  font-size: 36rpx;
  font-weight: bold;
}

/* 总览卡片样式 */
.overview-card {
  background: #ffffff;
  border-top: 8rpx solid #6366f1; /* 顶部Indigo条强调 */
}

.date-picker {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 24rpx;
  background-color: #f3f4f6;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #4b5563;
  transition: all 0.3s ease;
}

.picker-icon {
  color: #6366f1; /* Indigo */
}

.picker-arrow {
  font-size: 20rpx;
  color: #9ca3af;
}

/* 概览网格 */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  background-color: #f9fafb;
  border-radius: 20rpx;
  border: 1px solid #f3f4f6;
}

.overview-icon-bg {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.overview-icon {
  font-size: 36rpx;
}

/* 颜色变体 */
.overview-icon-bg.indigo {
  background-color: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.overview-icon-bg.green {
  background-color: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.overview-icon-bg.orange {
  background-color: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.overview-icon-bg.purple {
  background-color: rgba(139, 92, 246, 0.1);
  color: #8b5cf6;
}

.overview-data {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.overview-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #1f2937;
  line-height: 1.2;
  margin-bottom: 8rpx;
}

.overview-label {
  font-size: 24rpx;
  color: #6b7280;
}

/* 习惯分析 */
.habits-card {
  background-color: #eef2ff; /* 浅Indigo背景，区分辅助模块 */
  border: 1px solid #e0e7ff;
}

.habits-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.habit-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  background-color: #ffffff;
  padding: 20rpx;
  border-radius: 16rpx;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.habit-icon-wrapper {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.habit-icon-wrapper.indigo-light {
  background-color: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.habit-icon-wrapper.orange-light {
  background-color: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.habit-icon {
  font-size: 40rpx;
}

.habit-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.habit-label {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 8rpx;
}

.habit-number {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
  max-width: 100%;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.habit-unit {
  font-size: 24rpx;
  font-weight: normal;
  color: #9ca3af;
  margin-left: 4rpx;
}

.habit-divider {
  width: 2rpx;
  height: 80rpx;
  background-color: #e5e7eb;
  margin: 0 30rpx;
}

/* 热门文章 */
.view-all-btn {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #6366f1; /* Indigo */
}

.icon-small {
  font-size: 20rpx;
  margin-left: 6rpx;
}

.books-scroll {
  white-space: nowrap;
  width: 100%;
}

.book-item {
  display: inline-block;
  width: 280rpx;
  margin-right: 24rpx;
  vertical-align: top;
}

.book-item:last-child {
  margin-right: 0;
}

.book-cover {
  width: 100%;
  height: 380rpx;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.book-info {
  width: 100%;
}

.book-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.article-stats {
  display: flex;
  align-items: center;
}

.stat-pill {
  display: flex;
  align-items: center;
  background-color: #f3f4f6;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  color: #6b7280;
  margin-right: 12rpx;
}

.stat-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

/* 建议列表 */
.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  padding: 24rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
}

.suggestion-icon-box {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  background-color: rgba(99, 102, 241, 0.1); /* Indigo Light */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.suggestion-icon {
  font-size: 32rpx;
  color: #6366f1; /* Indigo */
}

.suggestion-text {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.4;
}

/* 弹窗样式 */
.date-picker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.date-picker-content {
  width: 600rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.date-picker-header {
  padding: 30rpx;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-picker-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
}

.close-btn {
  padding: 10rpx;
  color: #9ca3af;
}

.date-picker-body {
  padding: 30rpx;
  display: flex;
  height: 400rpx;
}

.picker-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.picker-section:first-child {
  border-right: 1px solid #e5e7eb;
}

.picker-label {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 20rpx;
  text-align: center;
}

.picker-scroll {
  flex: 1;
  height: 0;
}

.picker-item {
  padding: 20rpx;
  text-align: center;
  font-size: 30rpx;
  color: #374151;
  border-radius: 12rpx;
  margin: 0 20rpx 10rpx;
}

.picker-item.active {
  background-color: #6366f1; /* Indigo */
  color: #ffffff;
}

.date-picker-footer {
  padding: 30rpx;
  display: flex;
  gap: 20rpx;
  border-top: 1px solid #e5e7eb;
}

.picker-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 40rpx;
  font-size: 30rpx;
}

.picker-btn.cancel {
  background-color: #f3f4f6;
  color: #4b5563;
}

.picker-btn.confirm {
  background-color: #6366f1; /* Indigo */
  color: #ffffff;
}
</style> 
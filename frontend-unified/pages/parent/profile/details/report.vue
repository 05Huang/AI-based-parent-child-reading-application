<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">浏览报告</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 总览卡片 -->
      <view class="card overview-card">
        <view class="card-header">
          <text class="card-title big-title">本月浏览概览</text>
          <view class="date-picker" @click="showDatePicker">
            <text class="fas fa-calendar-alt picker-icon"></text>
            <text class="date-text">{{selectedDateText}}</text>
            <text class="fas fa-chevron-down picker-arrow"></text>
          </view>
        </view>
        
        <view class="overview-grid">
          <view class="overview-item">
            <view class="overview-icon-bg blue">
              <text class="fas fa-clock overview-icon"></text>
            </view>
            <view class="overview-data">
              <text class="overview-value">{{reportDisplay.totalDuration}}</text>
              <text class="overview-label">总浏览时长(分钟)</text>
            </view>
          </view>
          
          <view class="overview-item">
            <view class="overview-icon-bg green">
              <text class="fas fa-book-open overview-icon"></text>
            </view>
            <view class="overview-data">
              <text class="overview-value">{{reportDisplay.totalArticles}}</text>
              <text class="overview-label">浏览文章(篇)</text>
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

      <!-- 浏览习惯分析 -->
      <view class="card habits-card">
        <view class="card-header">
          <text class="card-title">浏览习惯分析</text>
        </view>
        <view class="habits-container">
          <view class="habit-box">
            <view class="habit-icon-wrapper blue-light">
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
import { ref, onMounted, computed } from 'vue'
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
  
  console.log('活跃度计算 - 原始分数:', rawScore, '转换后百分比:', activityPercent)
  
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
    favoriteCategory: weeklyReport.value.favoriteCategory || '育儿经验'
  }
})

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

// 获取周报数据
const loadWeeklyReport = async () => {
  try {
    if (!currentUser.value?.id) {
      console.log('用户ID不存在，跳过加载周报数据')
      return
    }
    
    console.log('开始获取周报数据，用户ID：', currentUser.value.id, '选择时间：', selectedYear.value, '年', selectedMonth.value, '月')
    const response = await userBehaviorApi.getWeeklyReport(currentUser.value.id, selectedYear.value.toString(), selectedMonth.value.toString())
    
    console.log('周报API完整响应：', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      console.log('获取周报数据成功：', response.data)
      
      // 确保所有字段都有默认值
      weeklyReport.value = {
        totalReadDuration: response.data.totalReadDuration || 0,
        totalArticleCount: response.data.totalArticleCount || 0,
        interactionCount: response.data.interactionCount || 0,
        activityScore: response.data.activityScore || 0,
        dailyAvgReadTime: response.data.dailyAvgReadTime || 0,
        favoriteCategory: response.data.favoriteCategory || '育儿经验'
      }
      console.log('更新后的weeklyReport：', weeklyReport.value)
    } else {
      console.warn('周报响应格式异常或无数据，使用默认值')
    }
  } catch (error) {
    console.error('获取周报数据失败：', error)
    // 不显示错误提示，使用默认值
  }
}


// 获取热门文章（从浏览历史中提取）
const loadHotArticles = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    console.log('开始获取热门文章，用户ID：', currentUser.value.id)
    
    // 计算选择的时间范围
    const startTime = `${selectedYear.value}-${selectedMonth.value.toString().padStart(2, '0')}-01 00:00:00`
    const endTime = new Date(selectedYear.value, selectedMonth.value, 0).toISOString().slice(0, 19).replace('T', ' ')
    
    console.log('查询时间范围：', startTime, '到', endTime)
    
    const response = await viewHistoryApi.getUserViewHistory(currentUser.value.id, {
      current: 1,
      size: 10,
      startTime: startTime,
      endTime: endTime
    })
    
    if (response && response.data && response.data.records) {
      console.log('获取浏览历史成功，筛选热门文章')
      
      // 过滤有效数据并转换
      const validRecords = response.data.records.filter(record => {
        return record.contentId && 
               record.contentTitle && 
               record.contentTitle.trim() !== '' &&
               record.contentTitle !== '无标题'
      })
      
      // 转换并筛选热门文章（取前3个）
      const articles = validRecords.slice(0, 3).map(record => ({
        id: record.contentId, // 使用contentId作为文章ID
        title: record.contentTitle,
        coverUrl: record.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
        duration: record.duration || 0, // 使用真实的阅读时长
        likes: record.likeCount || 0, // 使用真实的点赞数
        comments: record.commentCount || 0, // 使用真实的评论数
        contentId: record.contentId,
        contentType: record.contentType || 1
      }))
      
      completedArticles.value = articles
      console.log('热门文章加载完成，共', articles.length, '篇')
    }
  } catch (error) {
    console.error('获取热门文章失败：', error)
    // 使用默认文章
    completedArticles.value = [
      {
        title: '如何培养孩子的学习兴趣',
        coverUrl: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
        duration: 120,
        likes: 256,
        comments: 45
      },
      {
        title: '亲子沟通的艺术',
        coverUrl: 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop',
        duration: 90,
        likes: 189,
        comments: 32
      },
      {
        title: '儿童成长关键期指南',
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
  console.log('显示时间选择器')
  showDatePickerModal.value = true
}

// 隐藏时间选择器
const hideDatePicker = () => {
  showDatePickerModal.value = false
}

// 选择年份
const selectYear = (year) => {
  selectedYear.value = year
  console.log('选择年份：', year)
}

// 选择月份
const selectMonth = (month) => {
  selectedMonth.value = month
  console.log('选择月份：', month)
}

// 确认时间选择
const confirmDateSelection = async () => {
  console.log('确认时间选择：', selectedYear.value, '年', selectedMonth.value, '月')
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
    console.error('加载数据失败：', error)
    uni.hideLoading()
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 查看文章详情
const viewArticle = (article) => {
  console.log('查看文章：', article.title, '文章ID：', article.id)
  
  if (!article || !article.id) {
    console.error('文章信息不完整，无法跳转')
    uni.showToast({
      title: '文章信息错误',
      icon: 'none'
    })
    return
  }
  
  // 根据内容类型跳转到不同页面
  const contentType = article.contentType || 1
  
  if (contentType === 1) {
    // 图文内容
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${article.id}`,
      success: () => {
        console.log('跳转到阅读页面成功')
      },
      fail: (err) => {
        console.error('跳转到阅读页面失败:', err)
        uni.showToast({
          title: '跳转失败',
          icon: 'none'
        })
      }
    })
  } else if (contentType === 2) {
    // 视频内容
    uni.navigateTo({
      url: `/pages/parent/video/video-player?id=${article.id}`,
      success: () => {
        console.log('跳转到视频播放页面成功')
      },
      fail: (err) => {
        console.error('跳转到视频播放页面失败:', err)
        uni.showToast({
          title: '跳转失败',
          icon: 'none'
        })
      }
    })
  } else {
    console.warn('未知的内容类型：', contentType, '，默认按图文内容处理')
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${article.id}`
    })
  }
}

// 跳转到全部文章页面
const goToAllArticles = () => {
  console.log('跳转到全部文章页面')
  uni.navigateTo({
    url: '/pages/parent/bookshelf/all-articles',
    success: () => {
      console.log('跳转到全部文章页面成功')
    },
    fail: (err) => {
      console.error('跳转到全部文章页面失败:', err)
      uni.showToast({
        title: '跳转失败',
        icon: 'none'
      })
    }
  })
}

// 加载所有数据的统一方法
const loadAllData = async () => {
  console.log('[浏览报告] 加载所有数据')
  await loadCurrentUser()
  await loadWeeklyReport()
  await loadHotArticles()
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('[浏览报告] 页面已挂载，开始加载数据')
  await loadAllData()
})

// 页面显示时刷新数据（从其他页面返回时会触发）
onShow(async () => {
  console.log('[浏览报告] 页面显示，刷新数据')
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
  border-left: 8rpx solid #3b82f6;
  padding-left: 16rpx;
  line-height: 1.2;
}

.card-title.big-title {
  font-size: 36rpx;
  border-left-width: 10rpx;
}

/* 总览卡片样式 */
.overview-card {
  background: #ffffff; /* 改为白色背景，提高对比度 */
  border-top: 8rpx solid #3b82f6; /* 顶部蓝条强调 */
}

.date-picker {
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 26rpx;
  padding: 10rpx 20rpx;
  background: #f3f4f6; /* 统一浅灰色背景 */
  border-radius: 30rpx; /* 圆角统一 */
  color: #4b5563;
  transition: all 0.3s ease;
}

.date-picker:active {
  background: #e5e7eb;
}

.picker-icon {
  color: #3b82f6;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
  padding: 10rpx 0;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20rpx;
  border-radius: 16rpx;
  background-color: #f9fafb; /* 轻微背景区分 */
}

.overview-icon-bg {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.overview-icon-bg.blue { background-color: #dbeafe; color: #2563eb; }
.overview-icon-bg.green { background-color: #dcfce7; color: #16a34a; }
.overview-icon-bg.orange { background-color: #ffedd5; color: #ea580c; }
.overview-icon-bg.purple { background-color: #f3e8ff; color: #9333ea; }

.overview-icon {
  font-size: 36rpx;
}

.overview-data {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.overview-value {
  font-size: 44rpx;
  font-weight: bold;
  color: #111827;
  line-height: 1.2;
}

.overview-label {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

/* 浏览习惯分析样式 */
.habits-card {
  background-color: #f8fbff; /* 浅蓝色背景，区分辅助模块 */
  border: 1px solid #e0f2fe;
}

.habits-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.habit-box {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.habit-icon-wrapper {
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.habit-icon-wrapper.blue-light { background-color: #eff6ff; color: #3b82f6; }
.habit-icon-wrapper.orange-light { background-color: #fff7ed; color: #f97316; }

.habit-icon {
  font-size: 40rpx;
}

.habit-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.habit-label {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 8rpx;
}

.habit-number {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2937;
}

.habit-unit {
  font-size: 24rpx;
  color: #9ca3af;
  font-weight: normal;
  margin-left: 8rpx;
}

.habit-divider {
  height: 1px;
  background-color: #e5e7eb;
  margin: 0 20rpx;
}

/* 本月热门文章样式 */
.books-card {
  /* 保持白色背景，适合展示图片 */
}

.view-all-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  color: #3b82f6;
  background-color: #eff6ff;
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
}

.books-scroll {
  white-space: nowrap;
  padding-bottom: 10rpx; /* 预留滚动条空间或阴影空间 */
}

.book-item {
  display: inline-block;
  width: 240rpx; /* 增加宽度 */
  margin-right: 24rpx;
  vertical-align: top;
}

.book-cover {
  width: 240rpx;
  height: 320rpx; /* 增加高度 */
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.book-info {
  width: 100%;
}

.book-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 12rpx;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 允许两行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 80rpx; /* 固定高度确保对齐 */
  line-height: 40rpx;
}

.article-stats {
  display: flex;
  gap: 16rpx;
}

.stat-pill {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background-color: #f3f4f6;
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: #6b7280;
}

.stat-icon {
  font-size: 20rpx;
}
.stat-icon.fa-thumbs-up { color: #ef4444; } /* 统一红色点赞 */
.stat-icon.fa-comment { color: #3b82f6; } /* 统一蓝色评论 */

/* 浏览建议样式 */
.suggestions-card {
  background-color: #f8fbff; /* 浅蓝色背景 */
  border: 1px solid #e0f2fe;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.suggestion-item {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  gap: 20rpx;
  padding: 24rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
}

.suggestion-icon-box {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background-color: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.suggestion-icon {
  font-size: 32rpx;
  color: #3b82f6;
}

.suggestion-text {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.5;
  flex: 1;
  white-space: normal; /* 确保换行 */
}

/* 时间选择器弹窗样式 - 保持原有，微调 */
.date-picker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.date-picker-content {
  background: #ffffff;
  border-radius: 24rpx;
  width: 80%;
  max-width: 600rpx;
  overflow: hidden;
}

.date-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f3f4f6;
}

.date-picker-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.date-picker-body {
  display: flex;
  padding: 30rpx;
  gap: 30rpx;
}

.picker-section {
  flex: 1;
}

.picker-label {
  font-size: 28rpx;
  font-weight: 500;
  color: #374151;
  margin-bottom: 20rpx;
  display: block;
  text-align: center;
}

.picker-scroll {
  height: 400rpx;
  border: 1px solid #e5e7eb;
  border-radius: 12rpx;
  background: #f9fafb;
}

.picker-item {
  padding: 24rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #374151;
  border-bottom: 1px solid #f3f4f6;
}

.picker-item.active {
  background: #eff6ff;
  color: #3b82f6;
  font-weight: 600;
}

.date-picker-footer {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  border-top: 1px solid #f3f4f6;
}

.picker-btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 40rpx; /* 圆角按钮 */
  text-align: center;
  font-size: 28rpx;
  font-weight: 500;
}

.picker-btn.cancel {
  background: #f3f4f6;
  color: #6b7280;
}

.picker-btn.confirm {
  background: #3b82f6;
  color: #ffffff;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200rpx;
  display: inline-block;
  vertical-align: bottom;
}

.icon-small {
  font-size: 20rpx;
}
</style>

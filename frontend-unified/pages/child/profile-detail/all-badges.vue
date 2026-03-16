<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-btn" @tap="goBack" @touchstart="handleTouchStart">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">我的徽章</text>
        <view class="header-action"></view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content" :style="{ paddingTop: headerHeight + 'px' }" @tap="handleContentTap">
      <!-- 加载状态 -->
      <view class="loading-container" v-if="loading">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载徽章数据中...</text>
      </view>
      
      <!-- 内容区域 -->
      <template v-else>
      <!-- 用户统计信息 -->
      <view class="stats-section">
        <view class="stats-card">
          <view class="stats-item">
            <text class="stats-number">{{userStats.totalBadges}}</text>
            <text class="stats-label">已获得徽章</text>
          </view>
          <view class="stats-item">
            <text class="stats-number">{{userStats.totalTypes}}</text>
            <text class="stats-label">徽章类型</text>
          </view>
          <view class="stats-item">
            <text class="stats-number">{{userStats.completionRate}}%</text>
            <text class="stats-label">完成度</text>
          </view>
        </view>
      </view>

      <!-- 分类筛选 -->
      <view class="filter-section">
        <scroll-view scroll-x="true" class="filter-scroll" :show-scrollbar="false">
          <view class="filter-list">
            <view 
              class="filter-item" 
              :class="{ active: selectedCategory === 'all' }"
              @tap="selectCategory('all')"
            >
              全部 ({{allBadges.length}})
            </view>
            <view 
              class="filter-item" 
              :class="{ active: selectedCategory === category.key }"
              v-for="category in categories" 
              :key="category.key"
              @tap="selectCategory(category.key)"
            >
              {{category.name}} ({{getBadgeCountByCategory(category.key)}})
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 徽章列表 -->
      <view class="badges-section">
        <view class="section-title">
          {{selectedCategory === 'all' ? '全部徽章' : getCategoryName(selectedCategory)}}
        </view>
        
        <!-- 加载状态 -->
        <view class="loading-container" v-if="loading">
          <text class="loading-text">加载中...</text>
        </view>
        
        <!-- 徽章网格 -->
        <view class="badges-grid" v-else>
          <view 
            class="badge-item" 
            :class="{ unlocked: badge.unlocked, featured: badge.featured }"
            v-for="badge in filteredBadges" 
            :key="badge.id"
            @tap="showBadgeDetail(badge)"
          >
            <view class="badge-icon-container">
              <view class="badge-icon" :class="getBadgeIconClass(badge.category)">
                <text :class="badge.icon"></text>
              </view>
              <view class="unlock-indicator" v-if="badge.unlocked">
                <text class="fas fa-check"></text>
              </view>
              <view class="lock-overlay" v-if="!badge.unlocked">
                <text class="fas fa-lock"></text>
              </view>
            </view>
            <view class="badge-info">
              <text class="badge-name">{{badge.name}}</text>
              <text class="badge-desc">{{badge.description}}</text>
              <view class="badge-progress" v-if="!badge.unlocked && badge.progress">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: badge.progress.percentage + '%' }"></view>
                </view>
                <text class="progress-text">{{badge.progress.current}}/{{badge.progress.total}}</text>
              </view>
              <view class="unlock-date" v-if="badge.unlocked && badge.unlockDate">
                <text class="unlock-text">{{formatDate(badge.unlockDate)}} 获得</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="!loading && filteredBadges.length === 0">
          <text class="fas fa-trophy empty-icon"></text>
          <text class="empty-text">暂无相关徽章</text>
          <text class="empty-desc">完成更多阅读任务来解锁徽章吧！</text>
        </view>
      </view>
      </template>
    </scroll-view>

    <!-- 徽章详情弹窗 -->
    <view class="modal-overlay" v-if="showModal" @tap="closeModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <view class="modal-badge-icon" :class="getBadgeIconClass(selectedBadge?.category)">
            <text :class="selectedBadge?.icon"></text>
          </view>
          <text class="modal-close" @tap="closeModal">
            <text class="fas fa-times"></text>
          </text>
        </view>
        <view class="modal-body">
          <text class="modal-badge-name">{{selectedBadge?.name}}</text>
          <text class="modal-badge-desc">{{selectedBadge?.description}}</text>
          
          <view class="modal-requirements" v-if="selectedBadge?.requirements">
            <text class="requirements-title">获得条件</text>
            <text class="requirements-text">{{selectedBadge.requirements}}</text>
          </view>
          
          <view class="modal-progress" v-if="!selectedBadge?.unlocked && selectedBadge?.progress">
            <text class="progress-title">当前进度</text>
            <view class="progress-container">
              <view class="progress-bar">
                <view class="progress-fill" :style="{ width: selectedBadge.progress.percentage + '%' }"></view>
              </view>
              <text class="progress-text">{{selectedBadge.progress.current}}/{{selectedBadge.progress.total}}</text>
            </view>
          </view>
          
          <view class="modal-unlock-info" v-if="selectedBadge?.unlocked">
            <text class="unlock-title">获得时间</text>
            <text class="unlock-date">{{formatDate(selectedBadge.unlockDate)}}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
// 暂时不导入badgeApi，因为尚未实现
// import { badgeApi, userBehaviorApi } from '@/utils/api.js'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 100,
      loading: false,
      showModal: false,
      selectedBadge: null,
      selectedCategory: 'all',
      userStats: {
        totalBadges: 0,
        totalTypes: 0,
        completionRate: 0
      },
      categories: [
        { key: 'reading', name: '阅读成就' },
        { key: 'time', name: '时间管理' },
        { key: 'knowledge', name: '知识探索' },
        { key: 'social', name: '社交互动' },
        { key: 'special', name: '特殊成就' }
      ],
      allBadges: []
    }
  },
  computed: {
    filteredBadges() {
      if (this.selectedCategory === 'all') {
        return this.allBadges
      }
      return this.allBadges.filter(badge => badge.category === this.selectedCategory)
    }
  },
  onLoad() {
    // 获取状态栏高度
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight || 20
    // 计算总header高度：状态栏 + header-content的padding(20rpx*2) + 文字高度(约40rpx) + 额外间距
    this.headerHeight = this.statusBarHeight + 20 + 40 + 20 + 24 // 状态栏 + 上padding + 文字高度 + 下padding + 额外间距
    console.log('徽章页面状态栏高度：', this.statusBarHeight, '总页眉高度：', this.headerHeight)
    
    // 加载徽章数据
    this.loadBadges()
  },
  methods: {
    // 处理触摸开始事件（用于调试）
    handleTouchStart() {
      console.log('返回按钮被触摸')
    },
    
    // 处理内容区域点击事件（用于重试）
    handleContentTap() {
      if (!this.loading && this.allBadges.length === 0) {
        // 如果数据为空且不在加载中，则重新加载
        console.log('用户点击重试，重新加载徽章数据')
        this.loadBadges()
      }
    },
    
    goBack() {
      console.log('点击返回按钮')
      const pages = getCurrentPages()
      console.log('当前页面栈长度:', pages.length)
      
      if (pages.length > 1) {
        console.log('页面栈中有上一页，执行返回')
        uni.navigateBack({
          success: () => {
            console.log('返回上一页成功')
          },
          fail: (error) => {
            console.error('返回上一页失败:', error)
            this.forceGoToProfile()
          }
        })
      } else {
        console.log('页面栈中无上一页，直接跳转到个人页面')
        this.forceGoToProfile()
      }
    },
    
    // 强制跳转到个人页面
    forceGoToProfile() {
      console.log('强制跳转到个人页面')
      uni.redirectTo({
        url: '/pages/child/profile',
        success: () => {
          console.log('成功跳转到个人页面')
        },
        fail: (error) => {
          console.error('跳转到个人页面失败:', error)
          uni.showToast({
            title: '返回失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 加载徽章数据
    async loadBadges() {
      try {
        this.loading = true
        
        // 获取真实的用户ID
        const userId = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
        if (!userId) {
          console.warn('未找到用户ID，使用模拟数据')
          this.loadMockBadges()
          return
        }
        
        console.log('加载用户徽章数据，用户ID:', userId)
        
        // 由于badgeApi尚未实现，暂时使用模拟数据
        // 但根据用户ID生成个性化的徽章数据
        this.loadMockBadges(userId)
        
      } catch (error) {
        console.error('加载徽章数据失败:', error)
        this.loadMockBadges()
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    // 转换API数据格式
    transformBadgeData(apiData) {
      return apiData.map(badge => ({
        id: badge.id,
        name: badge.name,
        description: badge.description,
        category: badge.category || 'reading',
        icon: this.getBadgeIcon(badge.type),
        unlocked: badge.unlocked || false,
        unlockDate: badge.unlockDate,
        featured: badge.featured || false,
        requirements: badge.requirements,
        progress: badge.progress ? {
          current: badge.progress.current,
          total: badge.progress.total,
          percentage: Math.round((badge.progress.current / badge.progress.total) * 100)
        } : null
      }))
    },
    
    // 加载模拟徽章数据
    loadMockBadges(userId = null) {
      // 根据用户ID生成个性化的徽章数据
      const userSeed = userId ? parseInt(userId) : 1
      const baseUnlockCount = Math.min(5 + (userSeed % 10), 15) // 根据用户ID决定解锁的徽章数量
      
      console.log('生成个性化徽章数据，用户ID:', userId, '基础解锁数量:', baseUnlockCount)
      this.allBadges = [
        // 阅读成就类
        {
          id: 1,
          name: '阅读新手',
          description: '完成第一次阅读',
          category: 'reading',
          icon: 'fas fa-book',
          unlocked: baseUnlockCount >= 1,
          unlockDate: baseUnlockCount >= 1 ? '2024-01-15' : null,
          featured: false,
          requirements: '完成任意一篇文章的阅读'
        },
        {
          id: 2,
          name: '阅读达人',
          description: '累计阅读100篇文章',
          category: 'reading',
          icon: 'fas fa-book-open',
          unlocked: baseUnlockCount >= 3,
          unlockDate: baseUnlockCount >= 3 ? '2024-03-20' : null,
          featured: baseUnlockCount >= 3,
          requirements: '累计完成100篇文章的阅读'
        },
        {
          id: 3,
          name: '阅读专家',
          description: '累计阅读500篇文章',
          category: 'reading',
          icon: 'fas fa-graduation-cap',
          unlocked: baseUnlockCount >= 8,
          unlockDate: baseUnlockCount >= 8 ? '2024-05-10' : null,
          progress: baseUnlockCount < 8 ? {
            current: Math.min(156 + (userSeed % 50), 499),
            total: 500,
            percentage: Math.round((Math.min(156 + (userSeed % 50), 499) / 500) * 100)
          } : null,
          requirements: '累计完成500篇文章的阅读'
        },
        
        // 时间管理类
        {
          id: 4,
          name: '时间管理者',
          description: '连续7天每天阅读30分钟',
          category: 'time',
          icon: 'fas fa-clock',
          unlocked: baseUnlockCount >= 2,
          unlockDate: baseUnlockCount >= 2 ? '2024-02-10' : null,
          requirements: '连续7天每天至少阅读30分钟'
        },
        {
          id: 5,
          name: '坚持之星',
          description: '连续30天保持阅读习惯',
          category: 'time',
          icon: 'fas fa-star',
          unlocked: baseUnlockCount >= 6,
          unlockDate: baseUnlockCount >= 6 ? '2024-04-15' : null,
          progress: baseUnlockCount < 6 ? {
            current: Math.min(12 + (userSeed % 18), 29),
            total: 30,
            percentage: Math.round((Math.min(12 + (userSeed % 18), 29) / 30) * 100)
          } : null,
          requirements: '连续30天每天至少阅读15分钟'
        },
        {
          id: 6,
          name: '早起阅读',
          description: '在早上6-8点完成10次阅读',
          category: 'time',
          icon: 'fas fa-sun',
          unlocked: false,
          progress: {
            current: 3,
            total: 10,
            percentage: 30
          },
          requirements: '在早上6-8点之间完成10次阅读任务'
        },
        
        // 知识探索类
        {
          id: 7,
          name: '知识探索者',
          description: '阅读涵盖5个不同分类的内容',
          category: 'knowledge',
          icon: 'fas fa-search',
          unlocked: true,
          unlockDate: '2024-02-25',
          requirements: '阅读涵盖5个不同分类的文章'
        },
        {
          id: 8,
          name: '百科全书',
          description: '每个分类都至少阅读20篇',
          category: 'knowledge',
          icon: 'fas fa-book-atlas',
          unlocked: false,
          progress: {
            current: 3,
            total: 8,
            percentage: 38
          },
          requirements: '在每个内容分类中都至少阅读20篇文章'
        },
        
        // 社交互动类
        {
          id: 9,
          name: '评论达人',
          description: '发表100条有意义的评论',
          category: 'social',
          icon: 'fas fa-comment',
          unlocked: true,
          unlockDate: '2024-04-01',
          featured: true,
          requirements: '累计发表100条评论'
        },
        {
          id: 10,
          name: '点赞之王',
          description: '累计获得1000个点赞',
          category: 'social',
          icon: 'fas fa-thumbs-up',
          unlocked: false,
          progress: {
            current: 287,
            total: 1000,
            percentage: 29
          },
          requirements: '你的评论累计获得1000个点赞'
        },
        {
          id: 11,
          name: '分享专家',
          description: '分享文章给朋友50次',
          category: 'social',
          icon: 'fas fa-share',
          unlocked: false,
          progress: {
            current: 12,
            total: 50,
            percentage: 24
          },
          requirements: '累计分享50篇文章给朋友'
        },
        
        // 特殊成就类
        {
          id: 12,
          name: '夜猫子',
          description: '在深夜完成10次阅读',
          category: 'special',
          icon: 'fas fa-moon',
          unlocked: false,
          progress: {
            current: 2,
            total: 10,
            percentage: 20
          },
          requirements: '在晚上22点后完成10次阅读任务'
        },
        {
          id: 13,
          name: '周末勇士',
          description: '连续4个周末都有阅读记录',
          category: 'special',
          icon: 'fas fa-trophy',
          unlocked: true,
          unlockDate: '2024-03-15',
          requirements: '连续4个周末都完成阅读任务'
        },
        {
          id: 14,
          name: '速读高手',
          description: '单次阅读速度超过400字/分钟',
          category: 'special',
          icon: 'fas fa-bolt',
          unlocked: false,
          requirements: '在一次阅读中平均速度达到400字/分钟以上'
        }
      ]
      
      this.calculateStats()
    },
    
    // 计算统计数据
    calculateStats() {
      const unlockedBadges = this.allBadges.filter(badge => badge.unlocked)
      const totalTypes = [...new Set(this.allBadges.map(badge => badge.category))].length
      
      this.userStats = {
        totalBadges: unlockedBadges.length,
        totalTypes: totalTypes,
        completionRate: Math.round((unlockedBadges.length / this.allBadges.length) * 100)
      }
    },
    
    // 获取徽章图标
    getBadgeIcon(type) {
      const iconMap = {
        'reading_beginner': 'fas fa-book',
        'reading_expert': 'fas fa-book-open',
        'reading_master': 'fas fa-graduation-cap',
        'time_manager': 'fas fa-clock',
        'persistent': 'fas fa-star',
        'early_bird': 'fas fa-sun',
        'explorer': 'fas fa-search',
        'encyclopedia': 'fas fa-book-atlas',
        'commenter': 'fas fa-comment',
        'liked': 'fas fa-thumbs-up',
        'sharer': 'fas fa-share',
        'night_owl': 'fas fa-moon',
        'weekend_warrior': 'fas fa-trophy',
        'speed_reader': 'fas fa-bolt'
      }
      return iconMap[type] || 'fas fa-medal'
    },
    
    // 选择分类
    selectCategory(category) {
      this.selectedCategory = category
    },
    
    // 获取分类名称
    getCategoryName(key) {
      const category = this.categories.find(cat => cat.key === key)
      return category ? category.name : '全部徽章'
    },
    
    // 获取分类徽章数量
    getBadgeCountByCategory(category) {
      return this.allBadges.filter(badge => badge.category === category).length
    },
    
    // 获取徽章图标样式类
    getBadgeIconClass(category) {
      const classMap = {
        'reading': 'blue',
        'time': 'green', 
        'knowledge': 'purple',
        'social': 'pink',
        'special': 'yellow'
      }
      return classMap[category] || 'blue'
    },
    
    // 显示徽章详情
    showBadgeDetail(badge) {
      this.selectedBadge = badge
      this.showModal = true
    },
    
    // 关闭弹窗
    closeModal() {
      this.showModal = false
      this.selectedBadge = null
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
    }
  }
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f3f4f6;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 6rpx solid #e5e7eb;
  border-top: 6rpx solid #8b5cf6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 28rpx;
  color: #6b7280;
}

/* 顶部导航栏 */
.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 40rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  color: #ffffff;
  font-size: 40rpx;
  padding: 16rpx;
  min-width: 80rpx;
  min-height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.3s ease;
  z-index: 101;
}

.back-btn:active {
  background-color: rgba(255, 255, 255, 0.2);
}

.page-title {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
}

.header-action {
  width: 56rpx;
}

/* 主内容区域 */
.main-content {
  margin-top: 120rpx;
  padding: 32rpx;
  height: calc(100vh - 120rpx);
  box-sizing: border-box;
}

/* 统计信息 */
.stats-section {
  margin-bottom: 32rpx;
  margin-top: 16rpx; /* 添加上边距，防止被header遮挡 */
}

.stats-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx 24rpx;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  margin: 0 8rpx;
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stats-number {
  font-size: 48rpx;
  font-weight: bold;
  color: #6366f1;
}

.stats-label {
  font-size: 24rpx;
  color: #6b7280;
}

/* 分类筛选 */
.filter-section {
  margin-bottom: 32rpx;
  margin: 0 8rpx 32rpx 8rpx;
}

.filter-scroll {
  white-space: nowrap;
}

.filter-list {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 16rpx;
}

.filter-item {
  padding: 16rpx 32rpx;
  background: #ffffff;
  border-radius: 40rpx;
  font-size: 28rpx;
  color: #6b7280;
  white-space: nowrap;
  border: 2rpx solid transparent;
}

.filter-item.active {
  background: #6366f1;
  color: #ffffff;
}

/* 徽章部分 */
.badges-section {
  margin: 0 8rpx 32rpx 8rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 24rpx;
  padding: 0 8rpx;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400rpx;
}

.loading-text {
  color: #6b7280;
  font-size: 28rpx;
}

/* 徽章网格 */
.badges-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20rpx;
}

.badge-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  position: relative;
  transition: all 0.3s ease;
  margin: 0 8rpx;
}

.badge-item.unlocked {
  border: 2rpx solid #10b981;
}

.badge-item.featured {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  border: 2rpx solid #f59e0b;
}

.badge-item:not(.unlocked) {
  opacity: 0.6;
}

.badge-icon-container {
  position: relative;
  flex-shrink: 0;
}

.badge-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
}

.unlock-indicator {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 40rpx;
  height: 40rpx;
  background: #10b981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 20rpx;
}

.lock-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 32rpx;
}

.badge-info {
  flex: 1;
}

.badge-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8rpx;
  display: block;
}

.badge-desc {
  font-size: 24rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
  display: block;
}

.badge-progress {
  margin-top: 16rpx;
}

.progress-bar {
  height: 8rpx;
  background: #e5e7eb;
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-fill {
  height: 100%;
  background: #6366f1;
  border-radius: 8rpx;
}

.progress-text {
  font-size: 24rpx;
  color: #6b7280;
}

.unlock-date {
  margin-top: 16rpx;
}

.unlock-text {
  font-size: 24rpx;
  color: #10b981;
}

/* 颜色主题 */
.blue {
  background: #dbeafe;
  color: #3b82f6;
}

.green {
  background: #d1fae5;
  color: #10b981;
}

.purple {
  background: #f3e8ff;
  color: #8b5cf6;
}

.pink {
  background: #fce7f3;
  color: #ec4899;
}

.yellow {
  background: #fef3c7;
  color: #f59e0b;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.empty-icon {
  font-size: 120rpx;
  color: #d1d5db;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 弹窗样式 */
.modal-overlay {
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
  padding: 40rpx;
}

.modal-content {
  background: #ffffff;
  border-radius: 32rpx;
  width: 100%;
  max-width: 600rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  position: relative;
  padding: 40rpx;
  display: flex;
  justify-content: center;
  border-bottom: 2rpx solid #f3f4f6;
}

.modal-badge-icon {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
}

.modal-close {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  color: #6b7280;
  font-size: 32rpx;
  padding: 8rpx;
}

.modal-body {
  padding: 40rpx;
}

.modal-badge-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f2937;
  text-align: center;
  margin-bottom: 16rpx;
  display: block;
}

.modal-badge-desc {
  font-size: 28rpx;
  color: #6b7280;
  text-align: center;
  margin-bottom: 32rpx;
  display: block;
}

.modal-requirements,
.modal-progress,
.modal-unlock-info {
  margin-bottom: 32rpx;
}

.requirements-title,
.progress-title,
.unlock-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 16rpx;
  display: block;
}

.requirements-text {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.6;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-container .progress-bar {
  flex: 1;
}

.unlock-date {
  font-size: 26rpx;
  color: #10b981;
}
</style>

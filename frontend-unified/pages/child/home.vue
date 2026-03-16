<template>
  <view class="page-container">
    <!-- 顶部导航 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="header-left">
          <image 
            :src="userInfo?.avatar || currentUser?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(userDisplayName)}&background=8b5cf6&color=fff&size=128`" 
            alt="头像" 
            class="avatar" 
            mode="aspectFill"
          ></image>
        </view>
        <view class="user-info-box">
          <view class="username">{{userDisplayName}}</view>
          <view class="reading-time">今日阅读时间: {{todayReadingTime}}分钟</view>
        </view>
        <view class="header-right">
          <view class="icon-wrapper">
            <text class="fas fa-envelope icon" @tap="goToMessage"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      
      <!-- 模块组：内容推荐 -->
      <view class="module-group">
        <view class="group-header">
          <text class="group-title">内容推荐</text>
        </view>

        <!-- 精选推荐 -->
        <view class="sub-section">
          <view class="section-header">
            <text class="section-title">精选推荐</text>
            <view class="more-btn" @tap="navigateToAllArticles">
              <text>查看全部</text>
              <text class="fas fa-chevron-right"></text>
            </view>
          </view>
          <scroll-view scroll-x="true" class="book-scroll">
            <view v-if="loadingRecommendations" class="book-card loading-card">
              <view class="loading-placeholder">
                <text>加载中...</text>
              </view>
            </view>
            <view 
              v-for="item in recommendedContents" 
              :key="item.id"
              class="book-card"
              @tap="navigateToReading(item)"
            >
              <image 
                :src="item.coverUrl || 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&auto=format&fit=crop'" 
                class="book-cover"
                mode="aspectFill"
              ></image>
              <view class="book-info">
                <text class="book-title">{{ item.title }}</text>
                <text class="book-age">{{ formatTags(item.tags) }}</text>
              </view>
            </view>
            <view v-if="!loadingRecommendations && recommendedContents.length === 0" class="book-card no-data-card">
              <view class="no-data-placeholder">
                <text>暂无推荐内容</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- 继续阅读 -->
        <view class="sub-section">
          <view class="section-header">
            <text class="section-title">继续阅读</text>
          </view>
          <view class="current-reading-box">
            <view class="article-progress" @tap="goToArticleDetail" v-if="currentArticle">
              <image :src="fixImageUrl(currentArticle.coverUrl)" alt="文章配图" class="article-cover" mode="aspectFill" @error="handleImageError"></image>
              <view class="article-info">
                <view class="article-title">{{currentArticle.title || '暂无标题'}}</view>
                <view class="article-author">作者：{{currentArticle.author || currentArticle.creatorName || '未知作者'}}</view>
                <view class="progress-container">
                  <view class="progress-bar">
                    <view class="progress-fill" :style="{ width: (currentArticle.progress || 0) + '%' }"></view>
                  </view>
                  <view class="reading-progress">已读{{currentArticle.progress || 0}}%</view>
                </view>
              </view>
            </view>
            <view class="article-progress empty-state" v-else>
              <view class="empty-content">
                <view class="empty-icon">📖</view>
                <view class="empty-text">暂无阅读记录</view>
                <view class="empty-subtitle">开始阅读，记录你的成长故事</view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 创作者激励计划 -->
      <view class="module-section">
        <view class="incentive-card" @tap="goToSurvey">
          <view class="incentive-left">
            <view class="incentive-icon">
              <text class="fas fa-star"></text>
            </view>
            <view class="incentive-text">
              <view class="incentive-title">参与阅读调查</view>
              <view class="incentive-subtitle">分享你的阅读偏好，发现更多好书</view>
            </view>
          </view>
          <view class="incentive-right">
            <view class="cta-btn">
              <text>开始调查</text>
              <text class="fas fa-chevron-right"></text>
            </view>
          </view>
        </view>
      </view>

      <!-- 阅读排行榜 -->
      <view class="module-section">
        <view class="section-header">
          <text class="section-title">阅读时长排行榜</text>
          <view class="more-btn" @tap="goToReadingRank">
            <text>查看全部</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="leaderboard">
          <view 
            v-for="(item, index) in leaderboardList" 
            :key="item.userId || index"
            class="leaderboard-item"
          >
            <view class="rank-info">
              <text class="rank-number">{{ item.rank }}</text>
              <image 
                :src="item.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(item.name || '用户')}&background=6366f1&color=fff&size=128`" 
                class="rank-avatar"
                mode="aspectFill"
              ></image>
              <text class="rank-name">{{ item.name }}</text>
            </view>
            <view class="rank-score">
              <text class="reading-minutes">{{ item.minutes }}</text>
              <text class="minute-label">分钟</text>
            </view>
          </view>
          <view v-if="leaderboardList.length === 0 && !loadingLeaderboard" class="leaderboard-empty">
            <text class="empty-text">暂无排行榜数据</text>
          </view>
          <view v-if="loadingLeaderboard" class="leaderboard-loading">
            <text class="loading-text">加载中...</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view class="tab-item" :class="{ active: currentTab === 0 }" @tap="switchTab('/pages/child/home')">
        <text class="fas fa-home"></text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 1 }" @tap="switchTab('/pages/child/reading')">
        <text class="fas fa-book"></text>
        <text class="tab-text">阅读</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 2 }" @tap="switchTab('/pages/child/video')">
        <text class="fas fa-video"></text>
        <text class="tab-text">视频</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 3 }" @tap="switchTab('/pages/child/chat')">
        <text class="fas fa-comments"></text>
        <text class="tab-text">对话</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 4 }" @tap="switchTab('/pages/child/profile')">
        <text class="fas fa-user"></text>
        <text class="tab-text">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
import { contentApi, userApi, recommendationApi, viewHistoryApi, userBehaviorApi, likeApi } from '@/utils/api.js'

export default {
  data() {
    return {
      currentUser: null,
      currentArticle: null,
      loading: false,
      participantsCount: 1248, // 参与人数
      userSurveyStatus: 'not_started', // not_started, in_progress, completed
      todayReadingTime: 0,
      username: '小明',
      userInfo: null, // 添加用户信息存储
      statusBarHeight: 20, // 状态栏高度
      headerHeight: 120, // 总页眉高度
      currentTab: 0, // 当前激活的tab索引（0=首页，1=阅读，2=视频，3=对话，4=我的）
      leaderboardList: [], // 阅读排行榜列表（只显示前3名）
      loadingLeaderboard: false, // 排行榜加载状态
      recommendedContents: [], // 精选推荐内容
      loadingRecommendations: false, // 推荐内容加载状态
      recommendedSize: 6 // 推荐内容数量
    }
  },
  computed: {
    // 用户显示名称
    userDisplayName() {
      if (this.userInfo?.nickname) return this.userInfo.nickname
      if (this.userInfo?.username) return this.userInfo.username
      if (this.currentUser?.nickname) return this.currentUser.nickname
      if (this.currentUser?.username) return this.currentUser.username
      return this.username || '小朋友'
    }
  },
  onShow() {
    // 每次显示页面时刷新当前阅读文章和用户信息
    this.loadCurrentArticle()
    this.loadUserInfoFromStorage()
    // 加载排行榜数据
    this.loadLeaderboard()
    
    // 设置当前tab为首页
    this.currentTab = 0
  },
  onLoad() {
    console.log(' [首页] onLoad')
    
    // 获取状态栏高度并计算页眉高度
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight || 20
    // Header内容高度固定为56px
    this.headerHeight = this.statusBarHeight + 56
    console.log('📏 布局参数:', { 
        statusBar: this.statusBarHeight, 
        headerContent: 56, 
        totalHeader: this.headerHeight 
    })
    
    // 计算推荐内容数量
    this.calculateRecommendedSize()
    
    // 检查登录状态
    this.checkLoginStatus()
  },
  methods: {
    // 从本地存储加载用户信息
    loadUserInfoFromStorage() {
      console.log('📦 [首页] 从本地存储加载用户信息')
      try {
        const storedUserInfo = uni.getStorageSync('userInfo')
        if (storedUserInfo) {
          this.userInfo = storedUserInfo
          // 尝试设置 currentUser，以便在 API 失败时能有 fallback
          this.currentUser = storedUserInfo
          console.log('✅ [首页] 本地用户信息加载成功:', this.userInfo)
        } else {
          console.log('⚠️ [首页] 本地没有用户信息')
        }
      } catch (error) {
        console.error('❌ [首页] 从本地存储加载用户信息失败:', error)
      }
    },
    
    // 检查登录状态
    async checkLoginStatus() {
      console.log('🔐 [首页] 检查登录状态')
      const token = uni.getStorageSync('token')
      const userRole = uni.getStorageSync('userRole')
      
      console.log('   - Token:', token ? '存在' : '不存在')
      console.log('   - Role:', userRole)
      
      if (!token || userRole !== 'child') {
        console.warn('⚠️ [首页] 未登录或角色不符，跳转登录')
        uni.redirectTo({
          url: '/pages/child/login/login'
        })
        return
      }
      
      try {
        console.log('📡 [首页] 开始获取最新用户信息...')
        // 优先从本地存储获取用户ID
        let userId = uni.getStorageSync('userId') || uni.getStorageSync('currentUserId')
        console.log('   - 本地存储的用户ID:', userId)
        
        if (!userId) {
          // 如果没有存储的用户ID，尝试获取当前用户信息
          console.log('   - 未找到用户ID，调用 getCurrentUser API')
          const response = await userApi.getCurrentUser()
          if (response && response.data) {
            this.currentUser = response.data
            this.updateUserInfo(response.data)
            userId = response.data.id
            uni.setStorageSync('currentUserId', userId)
            console.log('✅ [首页] getCurrentUser 成功')
          } else {
            throw new Error('获取当前用户信息失败')
          }
        } else {
          // 根据用户ID获取详细信息
          console.log(`   - 使用用户ID ${userId} 获取详细信息`)
          const userResponse = await userApi.getUserById(userId)
          if (userResponse?.data) {
            this.currentUser = userResponse.data
            this.updateUserInfo(userResponse.data)
            console.log('✅ [首页] getUserById 成功')
          } else {
            // 如果根据ID获取失败，尝试getCurrentUser
            console.warn('⚠️ [首页] getUserById 失败，尝试 getCurrentUser')
            const currentResponse = await userApi.getCurrentUser()
            if (currentResponse?.data) {
              this.currentUser = currentResponse.data
              this.updateUserInfo(currentResponse.data)
              console.log('✅ [首页] fallback getCurrentUser 成功')
            } else {
               throw new Error('所有获取用户信息尝试均失败')
            }
          }
        }
        
        // 用户信息获取成功后，加载首页数据
        console.log('✅ [首页] 用户信息准备就绪，开始加载首页数据')
        this.loadHomeData()
        
      } catch (error) {
        console.error('❌ [首页] 获取用户信息失败：', error)
        // 不强制跳转到登录页，使用本地存储的信息
        if (this.currentUser) {
            console.log('⚠️ [首页] API失败，使用本地存储的用户信息加载首页数据')
            this.loadHomeData()
        } else {
            console.error('❌ [首页] 无法加载首页数据：无有效用户信息')
            uni.showToast({
                title: '获取用户信息失败',
                icon: 'none'
            })
        }
      }
    },
    
    // 更新用户信息（参考家长端实现）
    updateUserInfo(apiUserData) {
      if (!apiUserData) return
      
      const displayName = apiUserData.nickname || apiUserData.username || '小朋友'
      const updatedInfo = {
        id: apiUserData.id,
        username: apiUserData.username || apiUserData.nickname || this.userInfo?.username || '用户',
        nickname: apiUserData.nickname || apiUserData.username || this.userInfo?.nickname || '用户',
        avatar: apiUserData.avatar || this.userInfo?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=8b5cf6&color=fff&size=128`,
        // 保留其他已有信息
        ...this.userInfo
      }
      
      this.userInfo = updatedInfo
      this.username = updatedInfo.nickname
      
      // 保存到本地存储
      uni.setStorageSync('userInfo', updatedInfo)
      // console.log('更新用户信息:', updatedInfo) // 减少日志噪音
    },
    
    // 加载首页数据
    async loadHomeData() {
      console.log('🚀 [首页] 开始加载首页数据')
      
      if (!this.currentUser) {
        console.error('❌ [首页] 无法加载首页数据：currentUser 为空')
        return
      }
      
      console.log('   - 当前用户ID:', this.currentUser.id)
      
      try {
        this.loading = true
        console.log('   - 开始并行加载: 文章、阅读时间、排行榜、推荐内容')
        
        const results = await Promise.allSettled([
          this.loadCurrentArticle(),
          this.loadTodayReadingTime(),
          this.loadLeaderboard(),
          this.loadRecommendedContents()
        ])
        
        results.forEach((result, index) => {
            const tasks = ['CurrentArticle', 'TodayReadingTime', 'Leaderboard', 'RecommendedContents']
            if (result.status === 'rejected') {
                console.error(`❌ [首页] ${tasks[index]} 加载失败:`, result.reason)
            }
        })
        
        console.log('✅ [首页] 首页数据加载流程完成')
        
      } catch (error) {
        console.error('❌ [首页] 加载首页数据全局错误：', error)
      } finally {
        this.loading = false
      }
    },
    
    // 加载阅读排行榜数据（只显示前3名）
    async loadLeaderboard() {
      try {
        this.loadingLeaderboard = true
        console.log('[首页] 开始加载阅读排行榜数据')
        
        const response = await userBehaviorApi.getWeeklyReadingRanking({
          limit: 3
        })
        
        console.log('[首页] 排行榜API响应:', response)
        
        // 处理不同的响应格式
        let rankingData = null
        if (response && response.code === 200 && response.data) {
          rankingData = response.data
        } else if (response && Array.isArray(response)) {
          rankingData = response
        } else if (response && response.data && Array.isArray(response.data)) {
          rankingData = response.data
        }
        
        if (rankingData && Array.isArray(rankingData) && rankingData.length > 0) {
          // 只取前3名
          this.leaderboardList = rankingData.slice(0, 3).map((item, index) => {
            // 处理阅读时长：转换为分钟
            const readDurationSeconds = item.readDuration || item.totalReadingTime || 0
            const minutes = Math.round(readDurationSeconds / 60)
            
            return {
              rank: item.rank || index + 1,
              userId: item.userId || item.id,
              name: item.nickname || item.name || item.username || `用户${index + 1}`,
              avatar: item.avatar || item.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(item.nickname || item.name || `用户${index + 1}`)}&background=6366f1&color=fff&size=128`,
              minutes: minutes
            }
          })
          
          console.log('[首页] 排行榜数据加载成功，共', this.leaderboardList.length, '条记录')
        } else {
          console.warn('[首页] 排行榜数据为空')
          this.leaderboardList = []
        }
      } catch (error) {
        console.error('[首页] 加载排行榜数据失败:', error)
        this.leaderboardList = []
      } finally {
        this.loadingLeaderboard = false
      }
    },
    
    // 手动刷新当前阅读文章
    async refreshCurrentArticle() {
      console.log('🔄 手动刷新当前阅读文章')
      uni.showLoading({
        title: '刷新中...',
        mask: true
      })
      
      try {
        await this.loadCurrentArticle()
        uni.showToast({
          title: '刷新成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('刷新失败:', error)
        uni.showToast({
          title: '刷新失败',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 加载当前阅读文章
    async loadCurrentArticle() {
      try {
        const userId = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
        if (!userId) {
          console.warn('未找到用户ID，无法加载当前阅读文章')
          this.currentArticle = null
          return
        }
        
        console.log('🔍 开始加载用户最近阅读文章，用户ID:', userId)
        
        // 1. 先尝试从浏览历史获取最近阅读的文章
        try {
          console.log('📚 尝试从浏览历史获取最近阅读文章...')
          const historyResponse = await viewHistoryApi.getUserViewHistory(userId, {
            current: 1,
            size: 5 // 获取最近5条记录，增加成功概率
          })
          
          console.log('📖 浏览历史API响应:', historyResponse)
          
          if (historyResponse?.data) {
            let historyData = []
            if (Array.isArray(historyResponse.data)) {
              historyData = historyResponse.data
            } else if (historyResponse.data.records) {
              historyData = historyResponse.data.records
            }
            
            console.log('📋 解析的历史数据:', historyData)
            
            if (historyData.length > 0) {
              // 遍历历史记录，找到第一个有效的文章
              for (let i = 0; i < historyData.length; i++) {
                const latestRecord = historyData[i]
                console.log(`📄 处理第${i+1}条记录:`, latestRecord)
                
                try {
                  // 获取文章详情
                  const contentResponse = await contentApi.getContentDetail(latestRecord.contentId)
                  console.log('📰 文章详情API响应:', contentResponse)
                  
                  if (contentResponse?.data) {
                    const articleData = contentResponse.data
                    this.currentArticle = {
                      id: articleData.id,
                      title: articleData.title,
                      author: articleData.author,
                      creatorName: articleData.creatorName,
                      coverUrl: articleData.coverUrl,
                      progress: this.calculateReadingProgress(latestRecord)
                    }
                    console.log('✅ 成功加载当前文章:', this.currentArticle)
                    return
                  }
                } catch (contentError) {
                  console.warn(`❌ 获取第${i+1}条记录的文章详情失败:`, contentError)
                  continue // 尝试下一条记录
                }
              }
              
              console.log('⚠️ 所有历史记录都无法获取到有效的文章详情')
            } else {
              console.log('📭 浏览历史为空')
            }
          } else {
            console.log('❌ 浏览历史API返回空数据')
          }
        } catch (historyError) {
          console.warn('❌ 获取浏览历史失败:', historyError)
        }
        
        // 2. 如果浏览历史没有数据，尝试获取推荐内容
        try {
          console.log('🎯 尝试获取推荐内容...')
          const response = await recommendationApi.getRecommendations(userId, 1)
          console.log('🎯 推荐API响应:', response)
          
          if (response && response.data && response.data.length > 0) {
            const recommendedArticle = response.data[0]
            this.currentArticle = {
              id: recommendedArticle.id,
              title: recommendedArticle.title,
              author: recommendedArticle.author || recommendedArticle.creatorName,
              creatorName: recommendedArticle.creatorName,
              coverUrl: recommendedArticle.coverUrl,
              progress: 0 // 新推荐的文章进度为0
            }
            console.log('✅ 使用推荐文章:', this.currentArticle)
            return
          }
        } catch (recommendError) {
          console.warn('❌ 获取推荐内容失败:', recommendError)
        }
        
        // 3. 如果推荐接口也失败，使用内容列表接口
        try {
          console.log('📑 尝试获取内容列表...')
          const contentResponse = await contentApi.getContentPage({
            current: 1,
            size: 1,
            type: 1 // 文章类型
          })
          console.log('📑 内容列表API响应:', contentResponse)
          
          if (contentResponse && contentResponse.data && contentResponse.data.records.length > 0) {
            const articleData = contentResponse.data.records[0]
            this.currentArticle = {
              id: articleData.id,
              title: articleData.title,
              author: articleData.author,
              creatorName: articleData.creatorName,
              coverUrl: articleData.coverUrl,
              progress: 0 // 新文章进度为0
            }
            console.log('✅ 使用内容列表文章:', this.currentArticle)
            return
          }
        } catch (contentError) {
          console.warn('❌ 获取内容列表失败:', contentError)
        }
        
        // 4. 所有API都失败，设置为null显示空状态
        console.log('❌ 所有API都失败，显示空状态')
        this.currentArticle = null
        
      } catch (error) {
        console.error('💥 加载当前文章失败：', error)
        this.currentArticle = null
      }
    },
    
    // 计算阅读进度
    calculateReadingProgress(record) {
      // 这里可以根据实际的记录数据计算进度
      // 暂时使用模拟逻辑
      if (record.progress !== undefined) {
        return Math.min(Math.max(record.progress, 0), 100)
      }
      
      // 根据浏览次数估算进度
      const viewCount = record.viewCount || 1
      const estimatedProgress = Math.min(viewCount * 25, 100) // 每次浏览增加25%
      
      return estimatedProgress
    },
    
    // 修复图片URL
    fixImageUrl(url) {
      if (!url) {
        return 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60'
      }
      
      // 保持原始URL不变，数据库中存储的就是正确的
      return url
    },
    
    // 图片加载错误处理
    handleImageError() {
      console.log('文章封面加载失败，使用默认图片')
      if (this.currentArticle) {
        this.currentArticle.coverUrl = 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60'
      }
    },
    
    // 加载今日阅读时间
    async loadTodayReadingTime() {
      try {
        // 这里可以调用用户行为统计接口
        // const response = await userBehaviorApi.getBrowsingStats(this.currentUser.id)
        // 暂时使用模拟数据
        this.todayReadingTime = 30
      } catch (error) {
        console.error('加载阅读时间失败：', error)
        this.todayReadingTime = 0
      }
    },
    
    // 添加浏览记录
    async addViewHistory(contentId) {
      try {
        // 获取用户ID
        const userId = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
        if (!userId) {
          console.warn('未找到用户ID，无法添加浏览记录')
          return
        }
        
        console.log('添加浏览记录，用户ID:', userId, '内容ID:', contentId)
        
        // 并行调用多个API
        const promises = []
        
        // 1. 添加浏览记录
        promises.push(
          viewHistoryApi.addViewHistory(userId, contentId)
            .catch(error => console.warn('添加浏览记录失败:', error))
        )
        
        // 2. 记录用户行为
        const behaviorData = {
          userId: parseInt(userId),
          contentId: parseInt(contentId),
          behaviorType: 'view',
          duration: 0,
          progress: 0,
          source: 'home_recommendation'
        }
        
        promises.push(
          userBehaviorApi.recordUserBehavior(behaviorData)
            .then(response => console.log('记录用户行为成功:', response))
            .catch(error => console.warn('记录用户行为失败:', error))
        )
        
        // 3. 增加浏览量
        promises.push(
          contentApi.incrementViewCount(contentId)
            .catch(error => console.warn('增加浏览量失败:', error))
        )
        
        // 等待所有API调用完成
        await Promise.allSettled(promises)
        
      } catch (error) {
        console.error('添加浏览记录失败:', error)
      }
    },
    
    goToArticleDetail() {
      if (!this.currentArticle) return
      
      // 添加浏览记录
      this.addViewHistory(this.currentArticle.id)
      
      uni.navigateTo({
        url: `/pages/child/article-detail?id=${this.currentArticle.id}`
      })
    },
    
    goToMessage() {
      uni.navigateTo({
        url: '/pages/common/notification'
      })
    },
    
    // 底部导航栏切换（参考家长端实现）
    switchTab(url) {
      console.log('切换标签页：', url)
      
      try {
        // 获取当前页面路径
        const pages = getCurrentPages()
        
        // 检查pages是否有效
        if (!pages || pages.length === 0) {
          console.warn('getCurrentPages() 返回空数组，直接跳转')
          uni.reLaunch({
            url,
            fail: (err) => {
              console.error('页面跳转失败：', err)
            }
          })
          return
        }
        
        const currentPage = pages[pages.length - 1]
        
        // 检查currentPage是否存在
        if (!currentPage) {
          console.warn('当前页面对象不存在，直接跳转')
          uni.reLaunch({
            url,
            fail: (err) => {
              console.error('页面跳转失败：', err)
            }
          })
          return
        }
        
        // 安全地获取route属性
        const route = currentPage.route || ''
        const currentRoute = '/' + route
        
        console.log('当前路由：', currentRoute, '目标路由：', url)
        
        // 如果点击的是当前页面，不做任何操作
        if (currentRoute === url) {
          console.log('已在当前页面，无需跳转')
          return
        }
        
        // 使用 reLaunch 确保页面栈清理干净
        uni.reLaunch({
          url,
          success: () => {
            console.log('✅ 页面跳转成功')
          },
          fail: (err) => {
            console.error('❌ 页面跳转失败：', err)
            // 如果 reLaunch 失败，尝试使用 redirectTo
            uni.redirectTo({
              url,
              success: () => {
                console.log('✅ 使用 redirectTo 成功')
              },
              fail: (redirectErr) => {
                console.error('❌ redirectTo 也失败：', redirectErr)
                uni.showToast({
                  title: '页面跳转失败，请重试',
                  icon: 'none'
                })
              }
            })
          }
        })
      } catch (error) {
        console.error('❌ switchTab 方法异常：', error)
        // 发生异常时，直接尝试跳转
        uni.reLaunch({
          url,
          fail: (err) => {
            console.error('❌ 异常后的跳转也失败了：', err)
            uni.showToast({
              title: '页面跳转失败，请重试',
              icon: 'none'
            })
          }
        })
      }
    },
    
    goToReadingRank() {
      uni.navigateTo({
        url: '/pages/child/profile-detail/reading-rank'
      })
    },

    // 根据屏幕宽度计算推荐内容数量
    calculateRecommendedSize() {
      try {
        const systemInfo = uni.getSystemInfoSync()
        const screenWidth = systemInfo.screenWidth || 375
        
        if (screenWidth < 768) {
          this.recommendedSize = 6
        } else if (screenWidth < 1024) {
          this.recommendedSize = 12
        } else {
          this.recommendedSize = 18
        }
      } catch (error) {
        this.recommendedSize = 6
      }
    },

    // 加载精选推荐内容
    async loadRecommendedContents() {
      try {
        this.loadingRecommendations = true
        console.log('======================================')
        console.log('🎯 [首页] 开始加载精选推荐内容')
        console.log('======================================')
        console.log('📊 推荐参数：')
        console.log('  - 用户ID:', this.currentUser?.id)
        console.log('  - 推荐数量:', this.recommendedSize)
        
        if (!this.currentUser?.id) {
          console.warn('⚠️ [首页] 用户ID为空，使用降级方案')
          await this.loadContentsFallback('recommended')
          return
        }
        
        console.log('📡 [首页] 调用推荐API...')
        const startTime = Date.now()
        
        const response = await recommendationApi.getArticleRecommendations(
          this.currentUser.id,
          this.recommendedSize
        )
        
        const endTime = Date.now()
        console.log(`⏱️ API调用耗时: ${endTime - startTime}ms`)
        console.log('📦 API响应:', response)
        
        if (!response || !response.data || response.data.length === 0) {
          console.warn('⚠️ [首页] 推荐内容为空，使用降级方案')
          await this.loadContentsFallback('recommended')
          return
        }
        
        console.log(`✅ [首页] 获取到 ${response.data.length} 条推荐内容`)
        
        this.recommendedContents = await Promise.all(
          response.data.map(async (item) => {
            // 这里可以添加点赞状态获取逻辑，如果需要的话
            // 目前保持简单，只转换数据结构
            const contentWithLikeStatus = { ...item, isLiked: false }
            return contentWithLikeStatus
          })
        )
        
        console.log('✨ [首页] 推荐内容处理完成')
        
      } catch (error) {
        console.error('❌ [首页] 加载推荐内容失败：', error)
        console.log('🔄 [首页] 启动降级方案...')
        await this.loadContentsFallback('recommended')
      } finally {
        this.loadingRecommendations = false
      }
    },

    // 降级方案
    async loadContentsFallback(type) {
      try {
        const firstParams = {
          current: 1,
          size: this.recommendedSize,
          status: 1,
          type: 1,
          sortField: 'created_time',
          sortOrder: 'desc'
        }
        
        const firstResponse = await contentApi.getContentPage(firstParams)
        
        if (!firstResponse || !firstResponse.data || !firstResponse.data.records) {
          return
        }
        
        const totalPages = firstResponse.data.pages || 1
        const randomPage = totalPages > 1 ? Math.floor(Math.random() * Math.min(totalPages, 5)) + 1 : 1
        
        const response = randomPage === 1 ? firstResponse : await contentApi.getContentPage({
          ...firstParams,
          current: randomPage
        })
        
        if (response && response.data && response.data.records && response.data.records.length > 0) {
          const shuffled = response.data.records.sort(() => Math.random() - 0.5)
          this.recommendedContents = shuffled
        }
      } catch (error) {
        console.error('降级加载失败：', error)
      }
    },

    // 格式化标签
    formatTags(tags) {
      if (!tags) return '推荐阅读'
      const tagArray = typeof tags === 'string' ? tags.split(',') : tags
      const cleanTags = tagArray.map(tag => tag.trim()).filter(tag => tag.length > 0)
      if (cleanTags.length === 0) return '推荐阅读'
      const displayTags = cleanTags.slice(0, 1)
      return displayTags.join(' · ')
    },

    // 跳转到阅读页面
    navigateToReading(item) {
      if (!item || !item.id) return
      
      // 添加浏览记录
      this.addViewHistory(item.id)
      
      uni.navigateTo({
        url: `/pages/child/article-detail?id=${item.id}`
      })
    },

    // 跳转到全部文章页面
    navigateToAllArticles() {
      uni.navigateTo({
        url: '/pages/child/bookshelf/all-articles'
      })
    },
    
    goToSurvey() {
      console.log('🎯 点击了参与调查卡片')
      
      try {
        // 跳转到问卷首页
        uni.navigateTo({
          url: '/pages/child/survey/survey-home',
          success: () => {
            console.log('✅ 成功打开问卷页面')
          },
          fail: (err) => {
            console.error('❌ 打开问卷页面失败：', err)
            uni.showToast({
              title: '打开问卷页面失败',
              icon: 'none'
            })
          }
        })
      } catch (error) {
        console.error('❌ 操作失败：', error)
        uni.showToast({
          title: '操作失败，请重试',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style>
/* 引入 Font Awesome 图标库 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.page-container {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding-bottom: 140rpx; /* 增加底部padding，避免被tabbar遮挡 */
}

/* 顶部导航样式 */
.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
}

.header-content {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  box-sizing: border-box;
  width: 100%;
  height: 56px;
}

.header-left {
  margin-right: 12px;
}

.user-info-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2rpx solid #ffffff;
  background-color: #ffffff;
}

.username {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reading-time {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 2rpx;
}

.header-right {
  display: flex;
  align-items: center;
  margin-left: 12px;
}

.icon-wrapper {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-size: 20px;
  color: #ffffff;
}

/* 模块通用样式 */
.module-group {
  margin-bottom: 20rpx; /* 统一模块间距 */
}

.module-section {
  padding: 0 32rpx;
  margin-bottom: 20rpx; /* 统一模块间距 */
}

.group-header {
  padding: 20rpx 32rpx 10rpx;
}

.group-title {
  font-size: 40rpx; /* 加大标题权重 */
  font-weight: 800;
  color: #1f2937;
}

.sub-section {
  margin-bottom: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx; /* 减小标题与内容的间距，更紧凑 */
  padding: 0 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #374151;
}

.more-btn {
  display: flex;
  align-items: center;
  gap: 4rpx; /* 减小图标与文字间距 */
  font-size: 24rpx; /* 微调字号 */
  color: #8b5cf6;
  font-weight: 500;
  padding: 8rpx 0; /* 增加点击区域但保持视觉紧凑 */
}

/* 当前阅读样式 */
.current-reading-box {
  padding: 0 32rpx;
}

.article-progress {
  background: #ffffff;
  border-radius: 24rpx; /* 统一圆角 */
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.article-progress:active {
  transform: scale(0.98);
}

.article-progress.empty-state {
  flex-direction: column;
  text-align: center;
  padding: 60rpx 32rpx;
  justify-content: center;
  min-height: 240rpx;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.02) 0%, rgba(139, 92, 246, 0.02) 100%);
  border: 1rpx dashed #e5e7eb;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.empty-icon {
  font-size: 80rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 4rpx;
}

.empty-subtitle {
  font-size: 24rpx;
  color: #9ca3af;
  line-height: 1.5;
}

.article-cover {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  object-fit: cover;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.article-info {
  flex: 1;
  margin-left: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 140rpx;
}

.article-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.article-author {
  font-size: 24rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 16rpx;
  height: 32rpx; /* 固定高度确保垂直居中 */
}

.progress-bar {
  flex: 1;
  height: 8rpx;
  background: #f3f4f6;
  border-radius: 999rpx;
  margin-bottom: 0;
  overflow: hidden;
  display: flex; /* 确保内部填充对齐 */
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #8b5cf6, #6366f1);
  border-radius: 999rpx;
  transition: width 0.3s ease;
}

.reading-progress {
  font-size: 24rpx; /* 稍微调大字号增强可读性 */
  color: #8b5cf6;
  text-align: right;
  font-weight: 600;
  white-space: nowrap;
  line-height: 1; /* 确保垂直居中 */
}

/* 创作者激励计划卡片样式 (调整为轻量化风格) */
.incentive-card {
  background: #f5f3ff;
  border: 2rpx solid #ddd6fe;
  border-radius: 24rpx; /* 统一圆角 */
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.incentive-card:active {
  transform: translateY(-2rpx);
  background: #ede9fe;
}

.incentive-left {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 20rpx;
}

.incentive-icon {
  width: 72rpx;
  height: 72rpx;
  background: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(139, 92, 246, 0.15);
}

.incentive-icon .fas {
  color: #8b5cf6;
  font-size: 36rpx;
}

.incentive-text {
  flex: 1;
}

.incentive-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #5b21b6;
  margin-bottom: 6rpx;
}

.incentive-subtitle {
  font-size: 24rpx;
  color: #6d28d9;
  line-height: 1.4;
}

.incentive-right {
  display: flex;
  align-items: center;
  margin-left: 20rpx;
  flex-shrink: 0;
}

.cta-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
  background: #8b5cf6;
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(139, 92, 246, 0.3);
}

.cta-btn:active {
  background: #7c3aed;
  transform: scale(0.95);
}

.cta-btn .fas {
  font-size: 20rpx;
}

/* 精选推荐样式 */
.book-scroll {
  padding: 0 32rpx;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
  scroll-behavior: smooth;
  font-size: 0; 
}

.book-card {
  display: inline-block;
  width: 220rpx;
  margin-right: 20rpx;
  background-color: #fff;
  border-radius: 24rpx; /* 统一圆角 */
  overflow: hidden;
  height: 420rpx; /* 增加高度防止内容截断 */
  animation: slideInFromRight 0.4s ease-out;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  vertical-align: top;
}

.book-card:hover {
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12);
  transform: translateY(-4rpx);
}

@keyframes slideInFromRight {
  from {
    opacity: 0;
    transform: translateX(30rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.book-cover {
  width: 100%;
  height: 240rpx;
  object-fit: cover;
}

.book-info {
  padding: 16rpx;
  height: 180rpx; /* 增加高度以容纳标题和标签 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box; /* 确保padding不影响计算 */
}

.book-title {
  font-size: 26rpx;
  font-weight: 600;
  line-height: 1.4;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: normal;
  height: 72rpx;
  margin-bottom: 8rpx;
}

.book-age {
  font-size: 20rpx;
  color: #3b82f6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
  margin-top: auto;
  background: #eff6ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  display: block; /* 改为block确保宽度控制 */
  width: fit-content;
  max-width: 100%; /* 防止超出父容器 */
  font-weight: 500;
  line-height: 1.2;
}

/* 排行榜样式 */
.leaderboard {
  background-color: #ffffff;
  border-radius: 24rpx; /* 统一圆角 */
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.leaderboard-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.leaderboard-item:last-child {
  border-bottom: none;
}

.rank-info {
  display: flex;
  align-items: center;
}

.rank-number {
  font-size: 28rpx;
  font-weight: bold;
  color: #1f2937;
  margin-right: 24rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f3f4f6;
}

.leaderboard-item:nth-child(1) .rank-number {
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: white;
  box-shadow: 0 2rpx 8rpx rgba(255, 165, 0, 0.3);
}

.leaderboard-item:nth-child(2) .rank-number {
  background: linear-gradient(135deg, #c0c0c0, #a9a9a9);
  color: white;
}

.leaderboard-item:nth-child(3) .rank-number {
  background: linear-gradient(135deg, #cd7f32, #b87333);
  color: white;
}

.rank-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  border: 2rpx solid #f3f4f6;
}

.rank-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2937;
}

.rank-score {
  display: flex;
  align-items: center;
  background: #f5f3ff;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
}

.reading-minutes {
  font-size: 28rpx;
  font-weight: bold;
  color: #8b5cf6;
  margin-right: 4rpx;
}

.minute-label {
  font-size: 22rpx;
  color: #6b7280;
}

.leaderboard-empty,
.leaderboard-loading {
  padding: 60rpx 24rpx;
  text-align: center;
}

.loading-card, .no-data-card {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 200rpx;
  width: 240rpx;
  margin-right: 20rpx;
}

.loading-placeholder, .no-data-placeholder {
  text-align: center;
  color: #9ca3af;
  font-size: 28rpx;
}

/* 底部导航栏样式 */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: #ffffff;
  border-top: 2rpx solid #e5e7eb;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  padding: 4rpx 0;
  transition: all 0.3s ease;
}

.tab-item.active {
  color: #8b5cf6;
  transform: translateY(-2rpx);
}

.tab-item .fas {
  font-size: 40rpx;
  line-height: 1;
  margin-bottom: 8rpx;
}

.tab-text {
  font-size: 20rpx;
  line-height: 1;
  font-weight: 500;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}
</style>

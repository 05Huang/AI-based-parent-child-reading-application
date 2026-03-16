<template>
  <view class="container">
    <!-- 顶部搜索栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-top">
        <text class="header-title">我的书架</text>
        <view class="header-actions">
          <view class="action-btn" @tap="goToSearch">
            <text class="fas fa-search"></text>
          </view>
        </view>
      </view>
      <!-- 分类标签 -->
      <scroll-view scroll-x="true" class="category-scroll" :show-scrollbar="false">
        <view class="category-list">
          <view class="category-item" :class="{ active: currentCategory === '全部' }" @tap="switchCategory('全部')">全部</view>
          <view class="category-item" :class="{ active: currentCategory === '故事解读' }" @tap="switchCategory('故事解读')">故事解读</view>
          <view class="category-item" :class="{ active: currentCategory === '科普知识' }" @tap="switchCategory('科普知识')">科普知识</view>
          <view class="category-item" :class="{ active: currentCategory === '名著赏析' }" @tap="switchCategory('名著赏析')">名著赏析</view>
        </view>
      </scroll-view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content" :style="{ paddingTop: headerHeight + 'px' }">
      <!-- 文章列表 -->
      <view class="article-list">
        <view class="article-item" v-for="(article, index) in articles" :key="index" @tap="goToArticleDetail(article)">
          <!-- 作者信息 -->
          <view class="author-bar">
            <image :src="article.authorAvatar" class="author-avatar"></image>
            <text class="author-name">{{article.author}}</text>
            <text class="bullet">·</text>
            <text class="follow-btn">关注</text>
          </view>
          
          <!-- 文章内容 -->
          <view class="article-content">
            <view class="content-main">
              <view class="text-content">
                <text class="title">{{article.title}}</text>
                <text class="summary">{{article.summary}}</text>
              </view>
              <image v-if="article.cover" :src="article.cover" mode="aspectFill" class="cover-image"></image>
            </view>
            
            <!-- 文章数据 -->
            <view class="article-data">
              <view class="data-item">
                <text class="category-tag">{{article.category}}</text>
              </view>
              <view class="data-item">
                <text class="views">{{article.views}}阅读</text>
              </view>
              <view class="data-item" v-if="article.likes > 0">
                <text class="fas fa-thumbs-up stats-icon"></text>
                <text class="stats-text">{{article.likes}}</text>
              </view>
              <view class="data-item" v-if="article.comments > 0">
                <text class="fas fa-comment stats-icon"></text>
                <text class="stats-text">{{article.comments}}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view class="tab-item" @tap="switchTab('/pages/child/home')">
        <text class="fas fa-home"></text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item active" @tap="switchTab('/pages/child/reading')">
        <text class="fas fa-book"></text>
        <text class="tab-text">阅读</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/video')">
        <text class="fas fa-video"></text>
        <text class="tab-text">视频</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/chat')">
        <text class="fas fa-comments"></text>
        <text class="tab-text">对话</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/profile')">
        <text class="fas fa-user"></text>
        <text class="tab-text">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
import { contentApi, categoryApi, userApi, viewHistoryApi, userBehaviorApi } from '@/utils/api.js'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 100,
      currentCategory: '全部',
      articles: [],
      categories: ['全部', '故事解读', '科普知识', '名著赏析'],
      categoryList: [], // 存储从后端获取的分类完整信息
      loading: false,
      currentUser: null
    }
  },
  onLoad() {
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight
    this.headerHeight = this.statusBarHeight + 56 + 44 // 状态栏高度 + 头部内容高度 + 分类标签高度
    
    this.checkLoginAndLoadData()
  },
  onShow() {
    this.loadArticles()
  },
  methods: {
    // 检查登录状态并加载数据
    async checkLoginAndLoadData() {
      try {
        const userResponse = await userApi.getCurrentUser()
        if (userResponse && userResponse.data) {
          this.currentUser = userResponse.data
          await this.loadCategories()
          await this.loadArticles()
        }
      } catch (error) {
        console.error('获取用户信息失败：', error)
        // 即使没有登录也可以浏览内容
        await this.loadCategories()
        await this.loadArticles()
      }
    },
    
    // 加载分类
    async loadCategories() {
      try {
        const response = await categoryApi.getAllActiveCategories()
        if (response && response.data) {
          this.categoryList = response.data
          // 注意：这里不再覆盖 this.categories，因为前端显示是固定的
        }
      } catch (error) {
        console.error('加载分类失败：', error)
      }
    },
    
    // 加载文章列表
    async loadArticles() {
      try {
        this.loading = true
        
        const params = {
          current: 1,
          size: 10,
          type: 1 // 文章类型
        }
        
        // 如果选择了特定分类，添加分类过滤
        if (this.currentCategory !== '全部') {
          // 根据分类名称在 categoryList 中查找对应的 ID
          const category = this.categoryList.find(c => c.name === this.currentCategory)
          if (category) {
            params.categoryId = category.id
          } else {
            console.warn(`未找到分类 "${this.currentCategory}" 的ID，尝试使用名称作为标签搜索`)
            // 如果没找到ID（可能是后端没有完全匹配的分类名），可以尝试传 tags 或者不做过滤
            // 这里为了演示，假设后端支持 tags 参数，或者就暂时不过滤
             params.tags = this.currentCategory
          }
        }
        
        const response = await contentApi.getContentPage(params)
        
        if (response && response.data && response.data.records) {
          this.articles = response.data.records.map(item => ({
            id: item.id,
            author: item.creatorName || '作者',
            authorAvatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.creatorName || 'author'}`,
            title: item.title,
            summary: this.extractTextFromHtml(item.description || item.content || '', 100),
            cover: this.fixImageUrl(item.coverUrl),
            category: item.tags || '推荐阅读',
            views: this.formatViewCount(item.viewCount || 0),
            likes: item.likeCount || 0,
            comments: item.commentCount || 0
          }))
        }
      } catch (error) {
        console.error('加载文章失败：', error)
        // 使用默认模拟数据
        this.loadDefaultArticles()
      } finally {
        this.loading = false
      }
    },
    
    // 加载默认文章数据（API失败时的备用方案）
    loadDefaultArticles() {
      this.articles = [
        {
          id: 1,
          author: '儿童发展专家',
          authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=expert123',
          title: '培养孩子专注力的科学方法：从神经科学角度解析',
          summary: '在当今信息爆炸的时代，孩子的注意力越来越容易被分散。本文从神经科学角度，详细解析培养孩子专注力的方法...',
          cover: 'https://images.unsplash.com/photo-1544776193-352d25ca82cd?w=500&auto=format&fit=crop&q=60',
          category: '教育研究',
          views: '3.2万',
          likes: 328,
          comments: 89
        },
        {
          id: 2,
          author: '艺术教育专家',
          authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=art456',
          title: '儿童创造力开发：激发想象力的艺术教育方法',
          summary: '创造力是未来社会最重要的能力之一。本文从艺术教育的角度，探讨如何通过多元化的艺术活动...',
          cover: 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=500&auto=format&fit=crop&q=60',
          category: '艺术教育',
          views: '2.8万',
          likes: 256,
          comments: 48
        }
      ]
    },
    
    // 格式化浏览量
    formatViewCount(count) {
      if (count < 1000) return count.toString()
      if (count < 10000) return (count / 1000).toFixed(1) + 'k'
      return (count / 10000).toFixed(1) + '万'
    },
    
    // 从HTML中提取纯文本
    extractTextFromHtml(html, maxLength = 100) {
      if (!html) return ''
      
      // 移除HTML标签
      let text = html.replace(/<[^>]+>/g, '')
      
      // 解码HTML实体
      text = text.replace(/&nbsp;/g, ' ')
               .replace(/&lt;/g, '<')
               .replace(/&gt;/g, '>')
               .replace(/&amp;/g, '&')
               .replace(/&quot;/g, '"')
               .replace(/&#39;/g, "'")
      
      // 移除多余的空白字符
      text = text.replace(/\s+/g, ' ').trim()
      
      // 截断到指定长度
      if (text.length > maxLength) {
        text = text.substring(0, maxLength) + '...'
      }
      
      return text || '暂无简介'
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
          source: 'reading_list'
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
    
    goToSearch() {
      uni.navigateTo({
        url: '/pages/child/search/search'
      })
    },
    
    goToArticleDetail(article) {
      // 添加浏览记录
      this.addViewHistory(article.id)
      
      // 跳转到文章详情页
      uni.navigateTo({
        url: `/pages/child/article-detail?id=${article.id}&title=${encodeURIComponent(article.title)}`
      })
    },
    
    switchCategory(category) {
      this.currentCategory = category
      this.loadArticles()
    },
    
    switchTab(url) {
      if (url === '/pages/child/reading') {
        return
      }
      const path = url.replace('/pages/child/', '')
      this.switchChildTab(path)
    },
    
    // 孩子端tabbar切换
    switchChildTab(tabName) {
      const pages = ['home', 'reading', 'video', 'chat', 'profile']
      if (pages.includes(tabName)) {
        uni.redirectTo({
          url: `/pages/child/${tabName}`
        })
      }
    },
    
    // 修复图片URL
    fixImageUrl(url) {
      if (!url) {
        // 返回默认封面图片
        return 'https://via.placeholder.com/300x200/f0f0f0/666666?text=暂无封面'
      }
      
      // 保持MinIO链接不变，数据库中存储的URL就是正确的
      return url
    }
  }
}
</script>

<style>
/* 引入 Font Awesome 图标库 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
}

/* 顶部导航栏样式 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  z-index: 100;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
}

.header-top {
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  box-sizing: border-box;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.action-btn:active {
  transform: scale(0.88);
  opacity: 0.7;
}

.action-btn .fas {
  font-size: 20px;
  color: #ffffff;
}

/* 分类标签样式 */
.category-scroll {
  padding: 8px 0;
  white-space: nowrap;
  overflow-x: auto;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.15);
}

.category-list {
  display: inline-flex;
  padding: 0 16px;
  gap: 12px;
  align-items: center;
}

.category-item {
  padding: 6px 14px;
  border-radius: 14px;
  background-color: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  white-space: nowrap;
  line-height: 1;
  position: relative;
}

.category-item:active {
  opacity: 0.8;
}

.category-item.active {
  background-color: #ffffff;
  color: #8b5cf6;
  font-weight: 600;
}

.category-item.active::after {
  content: '';
  display: none;
}

/* 主要内容区域样式 */
.main-content {
  padding-bottom: 120rpx;
}

/* 文章列表样式 */
.article-list {
  background: #f3f4f6; /* 背景改为灰色，使卡片分明 */
  padding: 20rpx;
}

/* 修改文章列表样式 */
.article-item {
  padding: 40rpx 32rpx; /* 增加内边距 */
  border-bottom: none;
  background: #ffffff;
  transition: all 0.2s;
  margin-bottom: 24rpx; /* 增加卡片间距 */
  border-radius: 24rpx; /* 圆角卡片 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.article-item:active {
  transform: scale(0.98);
}

.author-bar {
  display: flex;
  align-items: center;
  margin-bottom: 28rpx; /* 增加间距 */
}

.author-avatar {
  width: 64rpx; /* 稍微放大头像 */
  height: 64rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  border: 2rpx solid #f0f0f0;
}

.author-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 600; /* 加粗作者名 */
  margin-right: 12rpx;
}

.bullet {
  color: #ccc;
  margin: 0 12rpx;
}

.follow-btn {
  color: #8b5cf6; /* 改为紫色系 */
  font-size: 26rpx;
  padding: 8rpx 24rpx;
  border-radius: 30rpx; /* 改为圆角 */
  background: rgba(139, 92, 246, 0.1);
  font-weight: 500;
}

.content-main {
  display: flex;
  gap: 32rpx; /* 增加图文间距 */
  margin-bottom: 28rpx;
}

.text-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20rpx; /* 增加标题和摘要间距 */
}

.title {
  font-size: 36rpx; /* 增大字号 */
  font-weight: bold; /* 加粗 */
  color: #1a1a1a;
  line-height: 1.5;
  margin-bottom: 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.summary {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2; /* 限制为2行 */
  overflow: hidden;
  margin-top: 4rpx;
}

.cover-image {
  width: 220rpx;
  height: 160rpx;
  border-radius: 16rpx;
  object-fit: cover;
  flex-shrink: 0;
}

.article-data {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f5f5f5;
}

.data-item {
  font-size: 24rpx;
  color: #999;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.category-tag {
  background: #f5f3ff; /* 紫色背景 */
  color: #8b5cf6; /* 紫色文字 */
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  font-weight: 500;
  font-size: 24rpx;
  max-width: 240rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats-icon {
  font-size: 26rpx;
  color: #9ca3af;
  margin-right: 6rpx;
}

.stats-text {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 底部导航栏样式 */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 110rpx; /* 稍微调高 */
  background: #ffffff;
  border-top: 1rpx solid rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.02);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  padding: 8rpx 0;
  flex: 1;
}

.tab-item.active {
  color: #8b5cf6;
}

.tab-item.active .fas {
  transform: translateY(-2rpx); /* 选中时微动 */
}

.tab-item.active .tab-text {
  font-weight: 600;
}

/* 选中项底部指示条 */
.tab-item.active::after {
  /* 可以选择加一个小圆点或者下划线，这里暂时不用，以免过于复杂，靠颜色和加粗区分 */
}

.tab-item .fas {
  font-size: 44rpx; /* 增大图标 */
  line-height: 1;
  margin-bottom: 10rpx;
  transition: all 0.2s;
}

.tab-text {
  font-size: 22rpx;
  line-height: 1;
}
</style>

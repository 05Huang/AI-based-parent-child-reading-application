<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <view class="back-btn" @click="goBack">
            <text class="fas fa-arrow-left"></text>
          </view>
          <text class="header-title">评论 {{ comments.length }}</text>
        </view>
        <view class="header-right">
          <view class="sort-btn" @click="toggleSortMenu">
            <text class="fas fa-sort"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 评论列表区域 -->
    <scroll-view 
      scroll-y="true" 
      class="comments-container"
      @scrolltolower="loadMoreComments"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 文章信息卡片 -->
      <view class="article-card">
        <text class="article-title">{{ article.title }}</text>
        <view class="article-meta">
          <text class="article-author">{{ article.author }}</text>
          <text class="article-date">{{ article.date }}</text>
        </view>
      </view>

      <!-- 评论列表 -->
      <view class="comments-list">
        <view v-for="comment in comments" :key="comment.id" class="comment-item">
          <view class="comment-header">
            <view class="user-info">
              <image class="avatar" :src="comment.avatar" mode="aspectFill"></image>
              <view class="user-meta">
                <text class="username">{{ comment.username }}</text>
                <text class="comment-time">{{ comment.time }}</text>
              </view>
            </view>
            <view class="comment-actions">
              <view class="action-btn" @click="toggleLike(comment)">
                <text class="fas fa-thumbs-up" :class="{ 'active': comment.isLiked }"></text>
                <text class="action-count">{{ comment.likes }}</text>
              </view>
            </view>
          </view>
          <view class="comment-content">
            <text>{{ comment.content }}</text>
          </view>
          <!-- 回复列表 -->
          <view v-if="comment.replies && comment.replies.length > 0" class="replies-container">
            <view v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <view class="reply-header">
                <image class="avatar small" :src="reply.avatar" mode="aspectFill"></image>
                <text class="username">{{ reply.username }}</text>
                <text v-if="reply.replyTo" class="reply-to">回复</text>
                <text v-if="reply.replyTo" class="username">{{ reply.replyTo }}</text>
              </view>
              <view class="reply-content">
                <text>{{ reply.content }}</text>
              </view>
              <view class="reply-footer">
                <text class="reply-time">{{ reply.time }}</text>
                <view class="reply-actions">
                  <view class="action-btn small" @click="toggleLike(reply)">
                    <text class="fas fa-thumbs-up" :class="{ 'active': reply.isLiked }"></text>
                    <text class="action-count">{{ reply.likes }}</text>
                  </view>
                  <view class="action-btn small" @click="showReplyInput(reply)">
                    <text class="fas fa-reply"></text>
                  </view>
                </view>
              </view>
            </view>
            <view v-if="comment.replies.length >= 3" class="view-more" @click="viewAllReplies(comment)">
              查看全部{{ comment.totalReplies }}条回复
              <text class="fas fa-chevron-right"></text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="loading" class="loading">
        <text class="fas fa-spinner fa-spin"></text>
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>

    <!-- 评论输入区域 -->
    <view class="comment-input-container" :style="{ bottom: keyboardHeight + 'px' }">
      <view class="input-wrapper">
        <input 
          class="comment-input"
          v-model="newComment"
          :placeholder="inputPlaceholder"
          @focus="onInputFocus"
          @blur="onInputBlur"
          confirm-type="send"
          @confirm="submitComment"
        />
        <view class="submit-btn" :class="{ 'active': newComment.trim().length > 0 }" @click="submitComment">
          <text class="fas fa-paper-plane"></text>
        </view>
      </view>
    </view>

    <!-- 排序菜单 -->
    <view v-if="showSortMenu" class="sort-menu">
      <view class="sort-option" :class="{ 'active': sortBy === 'latest' }" @click="setSortBy('latest')">
        <text class="fas fa-clock"></text>
        <text>最新</text>
      </view>
      <view class="sort-option" :class="{ 'active': sortBy === 'hot' }" @click="setSortBy('hot')">
        <text class="fas fa-fire"></text>
        <text>最热</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { contentApi, commentApi, likeApi, userApi } from '@/utils/api.js'

// 文章信息
const article = ref({
  id: null,
  title: '加载中...',
  author: '',
  date: ''
})

// 页面参数
const contentId = ref(null)

// 当前用户信息
const currentUser = ref(null)

// 页面加载时获取参数
onLoad(async (option) => {
  console.log('评论页面参数：', option)
  if (option.contentId) {
    contentId.value = parseInt(option.contentId)
    article.value.id = contentId.value
    await loadCurrentUser() // 先加载用户信息
    await loadArticleInfo()
    await loadComments()
  } else {
    console.error('缺少文章ID参数')
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
  }
})

// 加载当前用户信息
const loadCurrentUser = async () => {
  try {
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      currentUser.value = response.data
      console.log('当前用户信息加载成功：', currentUser.value)
    }
  } catch (error) {
    console.error('加载当前用户信息失败：', error)
  }
}

// 加载文章信息
const loadArticleInfo = async () => {
  try {
    const response = await contentApi.getContentDetail(contentId.value)
    if (response && response.data) {
      const data = response.data
      article.value = {
        id: data.id,
        title: data.title || '无标题',
        author: data.creatorName || '匿名作者',
        date: formatDate(data.createdTime)
      }
      console.log('文章信息加载成功：', article.value)
    }
  } catch (error) {
    console.error('加载文章信息失败：', error)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch (error) {
    console.error('日期格式化失败：', error)
    return ''
  }
}

// 评论数据
const comments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

// 加载评论列表
const loadComments = async (refresh = false) => {
  if (loading.value && !refresh) return
  
  try {
    loading.value = true
    console.log('开始加载评论列表，页码：', refresh ? 1 : currentPage.value)
    
    const response = await commentApi.getContentComments(
      contentId.value, 
      refresh ? 1 : currentPage.value, 
      pageSize.value
    )
    
    if (response && response.data) {
      const { records, current, total, pages } = response.data
      
      // 转换数据格式
      const formattedComments = await Promise.all(records.map(async comment => {
        const commentData = {
          id: comment.id,
          username: comment.userNickname || '匿名用户',
          avatar: comment.userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(comment.userNickname || '匿名')}&background=3b82f6&color=fff&size=100`,
          content: comment.content,
          time: formatCommentTime(comment.createdTime),
          likes: comment.likeCount || 0,
          isLiked: false,
          replies: [], // TODO: 加载回复
          totalReplies: comment.replyCount || 0
        }
        
        // 获取点赞状态
        if (currentUser.value && currentUser.value.id) {
          try {
            const likeResponse = await likeApi.getLikeStatus(currentUser.value.id, comment.id, 2)
            if (likeResponse && likeResponse.data !== undefined) {
              commentData.isLiked = likeResponse.data
            }
          } catch (error) {
            console.error('获取评论点赞状态失败：', error)
          }
        }
        
        return commentData
      }))
      
      if (refresh) {
        comments.value = formattedComments
        currentPage.value = 1
      } else {
        comments.value.push(...formattedComments)
      }
      
      currentPage.value = current
      hasMore.value = current < pages
      
      console.log('评论加载成功：', formattedComments.length, '条')
    }
  } catch (error) {
    console.error('加载评论失败：', error)
    uni.showToast({
      title: '加载评论失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

// 格式化评论时间
const formatCommentTime = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    const now = new Date()
    const diff = now - date
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)
    
    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    return date.toLocaleDateString('zh-CN')
  } catch (error) {
    console.error('时间格式化失败：', error)
    return ''
  }
}

// 交互状态
const loading = ref(false)
const isRefreshing = ref(false)
const showSortMenu = ref(false)
const sortBy = ref('latest')
const keyboardHeight = ref(0)
const newComment = ref('')
const inputPlaceholder = ref('写下你的评论...')
const replyTo = ref(null)

// 返回上一页
const goBack = () => {
  // 获取当前页面栈
  const pages = getCurrentPages()
  
  if (pages.length > 1) {
    // 如果有上一页，直接返回
    uni.navigateBack({
      delta: 1,
      fail: (err) => {
        console.error('返回失败:', err)
        // 如果返回失败，尝试重定向到阅读页面
        uni.redirectTo({
          url: '/pages/parent/reading/reading'
        })
      }
    })
  } else {
    // 如果没有上一页，重定向到阅读页面
    uni.redirectTo({
      url: '/pages/parent/reading/reading'
    })
  }
}

// 加载更多评论
const loadMoreComments = () => {
  if (loading.value || !hasMore.value) return
  currentPage.value += 1
  loadComments(false)
}

// 下拉刷新
const onRefresh = () => {
  isRefreshing.value = true
  loadComments(true)
}

// 切换点赞状态
const toggleLike = async (item) => {
  if (!currentUser.value || !currentUser.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    console.log('开始切换评论点赞状态，评论ID：', item.id, '当前状态：', item.isLiked)
    
    const response = await likeApi.toggleLike(currentUser.value.id, item.id, 2) // 2表示评论点赞
    
    if (response && response.data) {
      // 更新点赞状态和数量
      item.isLiked = response.data.isLiked
      item.likes = response.data.likeCount || 0
      
      console.log('评论点赞状态更新成功：', response.data)
      
      uni.showToast({
        title: item.isLiked ? '已点赞' : '已取消点赞',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('评论点赞失败：', error)
    uni.showToast({
      title: '点赞失败，请重试',
      icon: 'none'
    })
  }
}

// 显示回复输入框
const showReplyInput = (reply) => {
  replyTo.value = reply
  inputPlaceholder.value = `回复 ${reply.username}：`
  // 聚焦输入框
  const input = document.querySelector('.comment-input')
  if (input) input.focus()
}

// 切换排序方式
const toggleSortMenu = () => {
  showSortMenu.value = !showSortMenu.value
}

const setSortBy = (type) => {
  sortBy.value = type
  showSortMenu.value = false
  // 这里应该根据排序方式重新获取评论数据
}

// 查看所有回复
const viewAllReplies = (comment) => {
  // 这里应该跳转到完整的回复列表页面
  uni.navigateTo({
    url: `/pages/parent/reading/comment-replies?commentId=${comment.id}`
  })
}

// 输入框相关
const onInputFocus = (e) => {
  keyboardHeight.value = e.detail.height || 0
}

const onInputBlur = () => {
  keyboardHeight.value = 0
  if (replyTo.value) {
    replyTo.value = null
    inputPlaceholder.value = '写下你的评论...'
  }
}

// 提交评论
const submitComment = async () => {
  if (!newComment.value.trim()) return
  
  if (!currentUser.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    const commentData = {
      contentId: contentId.value,
      content: newComment.value.trim(),
      commentType: 1, // 1-普通评论
      parentId: replyTo.value ? replyTo.value.id : 0,
      rootId: replyTo.value ? (replyTo.value.rootId || replyTo.value.id) : 0
    }
    
    console.log('提交评论：', commentData)
    
    const response = await commentApi.addComment(commentData)
    
    if (response && response.data) {
      // 评论提交成功，刷新评论列表
      uni.showToast({
        title: '评论成功',
        icon: 'success'
      })
      
      newComment.value = ''
      onInputBlur()
      
      // 刷新评论列表
      await loadComments(true)
    }
  } catch (error) {
    console.error('提交评论失败：', error)
    uni.showToast({
      title: '评论失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style>
/* 引入 Font Awesome */
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
  background-color: #3b82f6;
  z-index: 50;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn .fas {
  color: #ffffff;
  font-size: 18px;
}

.header-title {
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
}

.sort-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sort-btn .fas {
  color: #ffffff;
  font-size: 18px;
}

/* 评论容器样式 */
.comments-container {
  margin-top: 56px;
  margin-bottom: 64px;
  height: calc(100vh - 120px);
  background-color: #f3f4f6;
}

/* 文章卡片样式 */
.article-card {
  margin: 16px;
  padding: 16px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.article-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 8px;
  display: block;
}

.article-meta {
  display: flex;
  gap: 12px;
}

.article-author, .article-date {
  font-size: 14px;
  color: #6b7280;
}

/* 评论列表样式 */
.comments-list {
  padding: 0 16px;
}

.comment-item {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e5e7eb;
}

.avatar.small {
  width: 24px;
  height: 24px;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.comment-time {
  font-size: 12px;
  color: #6b7280;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.action-btn.small {
  padding: 2px 4px;
}

.action-btn .fas {
  font-size: 14px;
  color: #6b7280;
}

.action-btn .fas.active {
  color: #3b82f6;
}

.action-count {
  font-size: 12px;
  color: #6b7280;
}

.comment-content {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  margin-bottom: 12px;
}

/* 回复列表样式 */
.replies-container {
  margin-left: 52px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.reply-item {
  margin-bottom: 12px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.reply-to {
  font-size: 12px;
  color: #6b7280;
}

.reply-content {
  font-size: 14px;
  color: #374151;
  margin-left: 32px;
}

.reply-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 32px;
  margin-top: 4px;
}

.reply-time {
  font-size: 12px;
  color: #6b7280;
}

.reply-actions {
  display: flex;
  gap: 12px;
}

.view-more {
  font-size: 14px;
  color: #3b82f6;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

/* 加载更多样式 */
.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: #6b7280;
}

.loading .fas {
  font-size: 16px;
}

.loading-text {
  font-size: 14px;
}

/* 评论输入区域样式 */
.comment-input-container {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ffffff;
  padding: 12px 16px;
  border-top: 1px solid #e5e7eb;
  z-index: 40;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-input {
  flex: 1;
  height: 40px;
  padding: 8px 16px;
  border-radius: 20px;
  background-color: #f3f4f6;
  font-size: 14px;
  color: #374151;
}

.submit-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #e5e7eb;
  transition: all 0.2s ease;
}

.submit-btn.active {
  background-color: #3b82f6;
}

.submit-btn .fas {
  font-size: 16px;
  color: #6b7280;
}

.submit-btn.active .fas {
  color: #ffffff;
}

/* 排序菜单样式 */
.sort-menu {
  position: fixed;
  top: 56px;
  right: 16px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 45;
}

.sort-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  font-size: 14px;
  color: #374151;
  transition: all 0.2s ease;
}

.sort-option.active {
  color: #3b82f6;
}

.sort-option .fas {
  font-size: 14px;
}

/* 响应式设计 */
@media screen and (min-width: 768px) {
  .comments-container {
    max-width: 768px;
    margin-left: auto;
    margin-right: auto;
  }

  .comment-input-container {
    max-width: 768px;
    margin-left: auto;
    margin-right: auto;
  }
}

@media screen and (min-width: 1024px) {
  .comments-container {
    max-width: 1024px;
  }

  .comment-input-container {
    max-width: 1024px;
  }

  .comment-item {
    transition: transform 0.2s ease;
  }

  .comment-item:hover {
    transform: translateY(-2px);
  }
}
</style> 
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

// 文章信息
const article = ref({
  title: '如何在3分钟内安抚孩子的小情绪',
  author: '育儿专家 王老师',
  date: '2024-03-20'
})

// 评论数据
const comments = ref([
  {
    id: 1,
    username: '快乐妈妈',
    avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop',
    content: '这篇文章写得非常实用，特别是关于情绪安全区的建议，我已经在家里实践了，效果很好！',
    time: '2小时前',
    likes: 28,
    isLiked: false,
    replies: [
      {
        id: 11,
        username: '阳光爸爸',
        avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop',
        content: '同感，我家也设置了一个小角落，放了些玩具和画画工具',
        time: '1小时前',
        likes: 12,
        isLiked: false
      }
    ],
    totalReplies: 5
  },
  {
    id: 2,
    username: '育儿达人',
    avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&h=100&fit=crop',
    content: '文中提到的共情倾听确实很重要，但有时候执行起来还是有难度的，特别是当自己也很疲惫的时候。',
    time: '3小时前',
    likes: 15,
    isLiked: false,
    replies: []
  }
])

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
  if (loading.value) return
  loading.value = true
  
  // 模拟加载更多数据
  setTimeout(() => {
    // 这里应该调用实际的API
    loading.value = false
  }, 1000)
}

// 下拉刷新
const onRefresh = () => {
  isRefreshing.value = true
  
  // 模拟刷新数据
  setTimeout(() => {
    isRefreshing.value = false
  }, 1000)
}

// 切换点赞状态
const toggleLike = (item) => {
  item.isLiked = !item.isLiked
  item.likes += item.isLiked ? 1 : -1
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
const submitComment = () => {
  if (!newComment.value.trim()) return
  
  const comment = {
    id: Date.now(),
    username: '当前用户',
    avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
    content: newComment.value,
    time: '刚刚',
    likes: 0,
    isLiked: false,
    replies: []
  }
  
  if (replyTo.value) {
    // 添加回复
    const parentComment = comments.value.find(c => c.replies.some(r => r.id === replyTo.value.id))
    if (parentComment) {
      parentComment.replies.push({
        ...comment,
        replyTo: replyTo.value.username
      })
    }
  } else {
    // 添加新评论
    comments.value.unshift(comment)
  }
  
  newComment.value = ''
  onInputBlur()
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
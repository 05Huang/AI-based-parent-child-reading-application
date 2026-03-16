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
          <view class="comment-footer">
            <view class="comment-footer-actions">
              <view class="action-btn small" @click="showReplyInput(comment)">
                <text class="fas fa-reply"></text>
                <text class="action-text">回复</text>
              </view>
            </view>
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
    <view class="comment-input-container" :class="{ 'with-emoji': showEmojiPicker }" :style="{ bottom: keyboardHeight + 'px' }">
      <!-- 回复提示 -->
      <view v-if="replyTo" class="reply-hint">
        <text class="reply-hint-text">回复 {{ replyTo.username }}</text>
        <view class="cancel-reply" @click="cancelReply">
          <text class="fas fa-times"></text>
        </view>
      </view>
      <view class="input-wrapper">
        <view class="emoji-btn" @click="toggleEmojiPicker">
          <text class="fas fa-smile"></text>
        </view>
        <input 
          class="comment-input"
          v-model="newComment"
          :placeholder="replyTo ? `回复 ${replyTo.username}：` : '写下你的评论...'"
          @focus="onInputFocus"
          @blur="onInputBlur"
          confirm-type="send"
          @confirm="submitComment"
        />
        <view class="submit-btn" :class="{ 'active': newComment.trim().length > 0 }" @click="submitComment">
          <text class="fas fa-paper-plane"></text>
        </view>
      </view>
      
      <!-- 表情包选择器 -->
      <view v-if="showEmojiPicker" class="emoji-picker">
        <view class="emoji-header">
          <text class="emoji-title">选择表情</text>
          <view class="close-emoji" @click="toggleEmojiPicker">
            <text class="fas fa-times"></text>
          </view>
        </view>
        <scroll-view scroll-y="true" class="emoji-list">
          <view class="emoji-grid">
            <view 
              v-for="emoji in emojiList" 
              :key="emoji" 
              class="emoji-item" 
              @click="insertEmoji(emoji)"
            >
              <text class="emoji-text">{{ emoji }}</text>
            </view>
          </view>
        </scroll-view>
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
import { onLoad, onShow } from '@dcloudio/uni-app'
import { contentApi, commentApi, likeApi, userApi } from '@/utils/api.js'
import readingModeManager from '@/utils/readingModeManager.js'

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
  
  // 应用阅读模式设置
  applyReadingModeSettings()
})

// 页面显示时重新应用阅读模式设置
onShow(() => {
  console.log('[文章评论页面] 页面显示，重新应用阅读模式设置')
  applyReadingModeSettings()
})

// 应用阅读模式设置
const applyReadingModeSettings = () => {
  try {
    const settings = readingModeManager.getSettings()
    console.log('[文章评论页面] 应用阅读模式设置：', settings)
    readingModeManager.applySettings(settings)
  } catch (error) {
    console.error('[文章评论页面] 应用阅读模式设置失败：', error)
  }
}

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
          replies: [], // 初始化回复列表
          totalReplies: comment.replyCount || 0,
          rootId: comment.rootId || 0,
          parentId: comment.parentId || 0
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
      
      // 组织评论层级结构
      const rootComments = []
      const commentMap = new Map()
      
      // 首先将所有评论放入Map
      formattedComments.forEach(comment => {
        commentMap.set(comment.id, comment)
        comment.replies = [] // 确保每个评论都有replies数组
      })
      
      // 然后组织层级结构
      formattedComments.forEach(comment => {
        if (comment.parentId === 0) {
          // 根评论
          rootComments.push(comment)
        } else {
          // 回复评论 - 所有非根评论都放到根评论下
          let targetComment = null
          
          // 如果有rootId，找到根评论
          if (comment.rootId && comment.rootId !== 0) {
            targetComment = commentMap.get(comment.rootId)
          }
          
          // 如果没找到根评论，找直接父评论
          if (!targetComment) {
            targetComment = commentMap.get(comment.parentId)
          }
          
          if (targetComment) {
            // 添加回复目标用户名（如果不是直接回复根评论）
            if (comment.parentId !== comment.rootId && comment.parentId !== 0) {
              const directParent = commentMap.get(comment.parentId)
              if (directParent) {
                comment.replyToUsername = directParent.username
              }
            }
            targetComment.replies.push(comment)
          } else {
            // 如果找不到父评论，作为根评论处理
            rootComments.push(comment)
          }
        }
      })
      
      if (refresh) {
        comments.value = rootComments
        currentPage.value = 1
      } else {
        // 对于加载更多的情况，需要合并到现有评论中
        comments.value.push(...rootComments)
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
const replyTo = ref(null)

// 表情包相关
const showEmojiPicker = ref(false)
const emojiList = ref([
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
  '😘', '😗', '😚', '😙', '😋', '😛', '😜', '🤪',
  '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨',
  '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
  '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕',
  '🤢', '🤮', '🤧', '🥵', '🥶', '😵', '🤯', '🤠',
  '🥳', '😎', '🤓', '🧐', '😕', '😟', '🙁', '😮',
  '😯', '😲', '😳', '🥺', '😦', '😧', '😨', '😰',
  '😥', '😢', '😭', '😱', '😖', '😣', '😞', '😓',
  '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈',
  '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻',
  '👽', '👾', '🤖', '😺', '😸', '😹', '😻', '😼',
  '😽', '🙀', '😿', '😾', '👏', '🙌', '👍', '👎',
  '👊', '✊', '🤛', '🤜', '🤞', '✌️', '🤟', '🤘',
  '👌', '🤏', '👈', '👉', '👆', '👇', '☝️', '✋',
  '🤚', '🖐️', '🖖', '👋', '🤙', '💪', '🦾', '🙏',
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍',
  '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖',
  '💘', '💝', '💟', '⭐', '🌟', '✨', '⚡', '🔥',
  '💯', '💢', '💥', '💫', '💦', '💨', '🕊️', '🦋',
  '🌸', '💮', '🏵️', '🌹', '🥀', '🌺', '🌻', '🌼'
])

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
        if (contentId.value) {
          uni.redirectTo({
            url: `/pages/parent/reading/reading?id=${contentId.value}`
          })
        } else {
          uni.switchTab({
            url: '/pages/parent/bookshelf/bookshelf'
          })
        }
      }
    })
  } else {
    // 如果没有上一页，重定向到阅读页面，带上文章ID
    if (contentId.value) {
      uni.redirectTo({
        url: `/pages/parent/reading/reading?id=${contentId.value}`
      })
    } else {
      // 如果没有contentId，返回到书架页面
      uni.switchTab({
        url: '/pages/parent/bookshelf/bookshelf'
      })
    }
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
  newComment.value = ''
  // 聚焦输入框
  uni.showToast({
    title: `回复 ${reply.username}`,
    icon: 'none',
    duration: 1000
  })
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
  newComment.value = ''
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
  // 不自动取消回复，让用户手动取消
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
      replyTo.value = null
      showEmojiPicker.value = false // 关闭表情选择器
      
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

// 切换表情包选择器
const toggleEmojiPicker = () => {
  console.log('切换表情包选择器，当前状态：', showEmojiPicker.value)
  showEmojiPicker.value = !showEmojiPicker.value
}

// 插入表情
const insertEmoji = (emoji) => {
  console.log('插入表情：', emoji)
  newComment.value += emoji
  // 不自动关闭表情选择器，方便连续选择
}
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: var(--reading-bg-color, #f3f4f6);
  position: relative;
  transition: background-color 0.3s ease;
}

/* 顶部导航栏样式 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: var(--reading-header-bg, #3b82f6);
  z-index: 50;
  transition: background-color 0.3s ease;
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
  background-color: var(--reading-bg-color, #f3f4f6);
  transition: background-color 0.3s ease;
}

/* 文章卡片样式 */
.article-card {
  margin: 12px;
  padding: 12px;
  background-color: var(--reading-comment-bg, rgba(0, 0, 0, 0.03));
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: background-color 0.3s ease;
}

.article-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--reading-title-color, #1f2937);
  margin-bottom: 8px;
  display: block;
  transition: color 0.3s ease;
}

.article-meta {
  display: flex;
  gap: 12px;
}

.article-author, .article-date {
  font-size: 14px;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
}

/* 评论列表样式 */
.comments-list {
  padding: 0 12px;
}

.comment-item {
  margin-bottom: 12px;
  padding: 12px;
  background-color: var(--reading-card-bg, rgba(255, 255, 255, 0.8));
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: background-color 0.3s ease;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
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
  color: var(--reading-title-color, #1f2937);
  transition: color 0.3s ease;
}

.comment-time {
  font-size: 12px;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
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
  color: var(--reading-toolbar-icon, #6b7280);
  transition: color 0.3s ease;
}

.action-btn .fas.active {
  color: #3b82f6;
}

.action-count {
  font-size: 12px;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
}

.comment-content {
  font-size: 14px;
  color: var(--reading-text-color, #374151);
  line-height: 1.6;
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.comment-footer {
  margin-bottom: 8px;
}

.comment-footer-actions {
  display: flex;
  justify-content: flex-end;
}

/* 回复列表样式 */
.replies-container {
  margin-left: 36px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--reading-border-color, #e5e7eb);
  transition: border-color 0.3s ease;
}

.reply-item {
  margin-bottom: 10px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.reply-to {
  font-size: 12px;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
}

.reply-content {
  font-size: 13px;
  color: var(--reading-text-color, #374151);
  margin-left: 32px;
  line-height: 1.5;
  margin-top: 4px;
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.reply-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 32px;
  margin-top: 2px;
}

.reply-time {
  font-size: 12px;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
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
  background-color: var(--reading-toolbar-bg, #ffffff);
  padding: 12px 16px;
  border-top: 1px solid var(--reading-toolbar-border, #e5e7eb);
  z-index: 40;
  transition: bottom 0.3s ease, background-color 0.3s ease, border-color 0.3s ease;
}

.comment-input-container.with-emoji {
  bottom: 300px; /* 表情选择器高度 */
}

.reply-hint {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  border-radius: 6px;
  margin-bottom: 6px;
  transition: background-color 0.3s ease;
}

.reply-hint-text {
  font-size: 14px;
  color: #3b82f6;
}

.cancel-reply {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e7eb;
  border-radius: 50%;
}

.cancel-reply .fas {
  font-size: 12px;
  color: #6b7280;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.emoji-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  border-radius: 50%;
  transition: all 0.2s ease;
}

.emoji-btn:active {
  background-color: var(--reading-btn-hover-bg, rgba(0, 0, 0, 0.08));
}

.emoji-btn .fas {
  color: var(--reading-toolbar-icon, #6b7280);
  font-size: 20px;
  transition: color 0.3s ease;
}

.comment-input {
  flex: 1;
  height: 40px;
  padding: 8px 16px;
  border-radius: 20px;
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  font-size: 14px;
  color: var(--reading-text-color, #374151);
  transition: background-color 0.3s ease, color 0.3s ease;
}

.submit-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: var(--reading-btn-hover-bg, rgba(0, 0, 0, 0.08));
  transition: all 0.2s ease;
}

.submit-btn.active {
  background-color: #3b82f6;
}

.submit-btn .fas {
  font-size: 16px;
  color: var(--reading-toolbar-icon, #6b7280);
  transition: color 0.3s ease;
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

/* 表情包选择器样式 */
.emoji-picker {
  position: absolute;
  bottom: 100%; /* 在输入框正上方 */
  left: 0;
  right: 0;
  width: 100%; /* 与父容器（输入框区域）同宽 */
  height: 300px;
  background-color: var(--reading-card-bg, rgba(248, 249, 250, 0.95));
  border-top: 1px solid var(--reading-border-color, #e5e7eb);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px 12px 0 0; /* 顶部圆角 */
  animation: slideUp 0.3s ease;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.emoji-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background-color: var(--reading-toolbar-bg, #ffffff);
  border-bottom: 1px solid var(--reading-border-color, #e5e7eb);
  border-radius: 12px 12px 0 0; /* 顶部圆角 */
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.emoji-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--reading-title-color, #1f2937);
  transition: color 0.3s ease;
}

.close-emoji {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.close-emoji:active {
  background-color: var(--reading-btn-hover-bg, rgba(0, 0, 0, 0.08));
}

.close-emoji .fas {
  font-size: 14px;
  color: var(--reading-toolbar-icon, #6b7280);
  transition: color 0.3s ease;
}

.emoji-list {
  height: calc(100% - 48px);
  background-color: var(--reading-card-bg, rgba(255, 255, 255, 0.95));
  transition: background-color 0.3s ease;
}

.emoji-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 8px;
}

.emoji-item {
  width: 12.5%; /* 每行8个 */
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
  border-radius: 8px;
}

.emoji-item:active {
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  transform: scale(1.3);
}

.emoji-text {
  font-size: 32px;
  line-height: 1;
}
</style> 
<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <view class="back-btn" @click="goBack">
            <text class="fas fa-arrow-left"></text>
          </view>
          <text class="header-title">段落评论</text>
        </view>
      </view>
    </view>

    <!-- 评论内容区域 -->
    <view class="comment-container">
      <!-- 原段落内容 -->
      <view class="original-paragraph">
        <text class="paragraph-text">{{ paragraphContent }}</text>
      </view>

      <!-- 评论列表 -->
      <scroll-view scroll-y="true" class="comments-list">
        <view v-for="comment in comments" :key="comment.id" class="comment-item">
          <view class="comment-header">
            <image :src="comment.avatar" class="avatar"></image>
            <view class="comment-info">
              <text class="username">{{ comment.username }}</text>
              <text class="time">{{ comment.time }}</text>
            </view>
          </view>
          <text class="comment-content">{{ comment.content }}</text>
          <view class="comment-actions">
            <view class="action-btn" @click="likeComment(comment)">
              <text class="fas fa-thumbs-up" :class="{ 'active': comment.isLiked }"></text>
              <text class="count">{{ comment.likes }}</text>
            </view>
            <view class="action-btn" @click="replyComment(comment)">
              <text class="fas fa-reply"></text>
              <text class="action-text">回复</text>
            </view>
          </view>
          
          <!-- 回复列表 -->
          <view v-if="comment.replies && comment.replies.length > 0" class="replies-container">
            <view v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <view class="reply-header">
                <image :src="reply.avatar" class="avatar small"></image>
                <view class="reply-info">
                  <text class="username">{{ reply.username }}</text>
                  <text v-if="reply.replyToUsername" class="reply-to">回复 {{ reply.replyToUsername }}</text>
                  <text class="time">{{ reply.time }}</text>
                </view>
              </view>
              <text class="reply-content">{{ reply.content }}</text>
              <view class="reply-actions">
                <view class="action-btn small" @click="likeComment(reply)">
                  <text class="fas fa-thumbs-up" :class="{ 'active': reply.isLiked }"></text>
                  <text class="count">{{ reply.likes }}</text>
                </view>
                <view class="action-btn small" @click="replyComment(reply)">
                  <text class="fas fa-reply"></text>
                  <text class="action-text">回复</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- 评论输入框 -->
      <view class="comment-input" :class="{ 'with-emoji': showEmojiPicker }">
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
            type="text" 
            v-model="newComment" 
            :placeholder="replyTo ? `回复 ${replyTo.username}：` : '写下你的评论...'" 
            class="input-field"
          />
          <view class="send-btn" :class="{ 'active': newComment.trim().length > 0 }" @click="submitComment">
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
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { commentApi, likeApi, userApi } from '@/utils/api.js'

// 获取页面参数
const contentId = ref(null)
const paragraphId = ref('')
const paragraphContent = ref('')

// 当前用户信息
const currentUser = ref(null)

// 在页面加载时获取参数
onLoad(async (option) => {
  console.log('段落评论页面参数：', option)
  if (option.contentId && option.paragraphId && option.content) {
    contentId.value = parseInt(option.contentId)
    paragraphId.value = option.paragraphId
    paragraphContent.value = decodeURIComponent(option.content)
    await loadCurrentUser() // 先加载用户信息
    await loadComments()
  } else {
    console.error('缺少必要参数')
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

// 评论数据
const comments = ref([])
const loading = ref(false)

// 新评论内容
const newComment = ref('')
const replyTo = ref(null) // 回复的目标评论

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
      delta: 1
    })
  } else {
    // 如果没有上一页，重定向到阅读页面，带上文章ID
    if (contentId.value) {
      uni.redirectTo({
        url: `/pages/parent/reading/reading?id=${contentId.value}`,
        fail: (err) => {
          console.error('重定向失败:', err)
          // 提示用户
          uni.showToast({
            title: '返回失败，请重试',
            icon: 'none'
          })
        }
      })
    } else {
      // 如果没有contentId，返回到书架页面
      uni.switchTab({
        url: '/pages/parent/bookshelf/bookshelf',
        fail: (err) => {
          console.error('返回书架失败:', err)
          uni.showToast({
            title: '返回失败，请重试',
            icon: 'none'
          })
        }
      })
    }
  }
}

// 加载评论
const loadComments = async () => {
  if (!contentId.value || !paragraphId.value) return
  
  try {
    loading.value = true
    console.log('开始加载段落评论，内容ID：', contentId.value, '段落ID：', paragraphId.value)
    
    const response = await commentApi.getParagraphComments(contentId.value, paragraphId.value)
    
    if (response && response.data && response.data.records) {
      const formattedComments = await Promise.all(response.data.records.map(async comment => {
        const commentData = {
          id: comment.id,
          username: comment.userNickname || '匿名用户',
          avatar: comment.userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(comment.userNickname || '匿名')}&background=3b82f6&color=fff&size=100`,
          content: comment.content,
          time: formatCommentTime(comment.createdTime),
          likes: comment.likeCount || 0,
          isLiked: false,
          replies: [], // 初始化回复列表
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
            console.error('获取段落评论点赞状态失败：', error)
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
      
      comments.value = rootComments
      console.log('段落评论加载成功：', rootComments.length, '条根评论，总计', formattedComments.length, '条评论')
    }
  } catch (error) {
    console.error('加载段落评论失败：', error)
    uni.showToast({
      title: '加载评论失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
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

// 提交评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    uni.showToast({
      title: '请输入评论内容',
      icon: 'none'
    })
    return
  }

  if (!currentUser.value || !currentUser.value.id) {
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
      commentType: 2, // 2-段落评论
      paragraphId: paragraphId.value,
      parentId: replyTo.value ? replyTo.value.id : 0,
      rootId: replyTo.value ? (replyTo.value.rootId || replyTo.value.id) : 0
    }
    
    console.log('提交段落评论：', commentData)
    
    const response = await commentApi.addComment(commentData)
    
    if (response && response.data) {
      uni.showToast({
        title: '评论成功',
        icon: 'success'
      })
      
      // 清空输入框和回复状态
      newComment.value = ''
      replyTo.value = null
      
      // 重新加载评论
      await loadComments()
    }
  } catch (error) {
    console.error('提交段落评论失败：', error)
    uni.showToast({
      title: '评论失败，请重试',
      icon: 'none'
    })
  }
}

// 点赞评论
const likeComment = async (comment) => {
  if (!currentUser.value || !currentUser.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    console.log('开始切换段落评论点赞状态，评论ID：', comment.id, '当前状态：', comment.isLiked)
    
    const response = await likeApi.toggleLike(currentUser.value.id, comment.id, 2) // 2表示评论点赞
    
    if (response && response.data) {
      // 更新点赞状态和数量
      comment.isLiked = response.data.isLiked
      comment.likes = response.data.likeCount || 0
      
      console.log('段落评论点赞状态更新成功：', response.data)
      
      uni.showToast({
        title: comment.isLiked ? '已点赞' : '已取消点赞',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('段落评论点赞失败：', error)
    uni.showToast({
      title: '点赞失败，请重试',
      icon: 'none'
    })
  }
}

// 回复评论
const replyComment = (comment) => {
  replyTo.value = comment
  newComment.value = ''
  // 聚焦输入框
  uni.showToast({
    title: `回复 ${comment.username}`,
    icon: 'none',
    duration: 1000
  })
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
  newComment.value = ''
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
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #3b82f6;
  padding: 8px 16px;
  height: 56px;
  /* 移除过大的padding-top */
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
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

.comment-container {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 56px); /* 调整高度计算，只减去header的56px */
  padding-top: 12px;
}

.original-paragraph {
  background-color: #f3f4f6;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.paragraph-text {
  font-size: 16px;
  line-height: 1.6;
  color: #374151;
}

.comments-list {
  flex: 1;
  margin-bottom: 80px;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #e5e7eb;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
}

.comment-info {
  flex: 1;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
  display: block;
}

.time {
  font-size: 12px;
  color: #6b7280;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #374151;
  margin: 8px 0;
  display: block;
}

.comment-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.action-btn:hover {
  background-color: #f3f4f6;
}

.action-btn .fas {
  font-size: 14px;
  color: #4b5563; /* 更深的灰色，确保在白色背景上可见 */
}

.action-btn .fas.active {
  color: #3b82f6;
}

.count, .action-text {
  font-size: 12px;
  color: #6b7280;
}

.comment-input {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background-color: #ffffff;
  border-top: 1px solid #e5e7eb;
  transition: bottom 0.3s ease;
}

.comment-input.with-emoji {
  /* 移除 bottom: 300px，让输入框保持在底部 */
  /* 表情选择器通过 position: absolute; bottom: 100% 显示在输入框上方 */
}

.reply-hint {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  background-color: #f3f4f6;
  border-radius: 6px;
  margin-bottom: 6px;
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
  gap: 8px;
  align-items: center;
}

.emoji-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.emoji-btn:active {
  background-color: #e5e7eb;
}

.emoji-btn .fas {
  color: #6b7280;
  font-size: 18px;
}

.input-field {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  background-color: #f3f4f6;
  border-radius: 18px;
  font-size: 14px;
}

.send-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e7eb;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.send-btn.active {
  background-color: #3b82f6;
}

.send-btn .fas {
  color: #6b7280;
  font-size: 14px;
  transition: color 0.2s ease;
}

.send-btn.active .fas {
  color: #ffffff;
}

/* 回复样式 */
.replies-container {
  margin-left: 36px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
}

.reply-item {
  margin-bottom: 12px;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.avatar.small {
  width: 24px;
  height: 24px;
  margin-right: 8px;
}

.reply-info {
  flex: 1;
}

.reply-info .username {
  font-size: 12px;
  font-weight: 500;
  color: #1f2937;
  margin-right: 8px;
}

.reply-to {
  font-size: 12px;
  color: #6b7280;
  margin-right: 8px;
}

.reply-info .time {
  font-size: 11px;
  color: #9ca3af;
}

.reply-content {
  font-size: 13px;
  line-height: 1.5;
  color: #374151;
  margin: 4px 0;
  margin-left: 32px;
  display: block;
}

.reply-actions {
  display: flex;
  gap: 10px;
  margin-left: 32px;
}

.action-btn.small {
  padding: 2px 6px;
}

.action-btn.small .fas {
  font-size: 12px;
}

.action-btn.small .count,
.action-btn.small .action-text {
  font-size: 11px;
}

/* 表情包选择器样式 */
.emoji-picker {
  position: absolute;
  bottom: 100%; /* 在输入框正上方 */
  left: 0;
  right: 0;
  width: 100%; /* 与父容器（输入框区域）同宽 */
  height: 300px;
  background-color: #f8f9fa;
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px 12px 0 0; /* 顶部圆角 */
  animation: slideUp 0.3s ease;
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
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  border-radius: 12px 12px 0 0; /* 顶部圆角 */
}

.emoji-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2937;
}

.close-emoji {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.close-emoji:active {
  background-color: #e5e7eb;
}

.close-emoji .fas {
  font-size: 14px;
  color: #6b7280;
}

.emoji-list {
  height: calc(100% - 48px);
  background-color: #ffffff;
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
  background-color: #f3f4f6;
  transform: scale(1.3);
}

.emoji-text {
  font-size: 32px;
  line-height: 1;
}
</style> 
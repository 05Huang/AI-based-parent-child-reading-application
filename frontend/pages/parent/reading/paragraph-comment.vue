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
        </view>
      </scroll-view>

      <!-- 评论输入框 -->
      <view class="comment-input">
        <input 
          type="text" 
          v-model="newComment" 
          placeholder="写下你的评论..." 
          class="input-field"
        />
        <view class="send-btn" @click="submitComment">
          <text class="fas fa-paper-plane"></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// 获取页面参数
const paragraphId = ref('')
const paragraphContent = ref('')

// 在页面加载时获取参数
onLoad((option) => {
  if (option.id && option.content) {
    paragraphId.value = option.id
    paragraphContent.value = decodeURIComponent(option.content)
    loadComments()
  }
})

// 评论数据
const comments = ref([
  {
    id: 1,
    username: '阅读爱好者',
    avatar: 'https://ui-avatars.com/api/?name=阅读爱好者&background=3b82f6&color=fff&size=100',
    content: '这段写得很有见地，对我教育孩子很有帮助！',
    time: '10分钟前',
    likes: 12,
    isLiked: false
  },
  {
    id: 2,
    username: '育儿专家',
    avatar: 'https://ui-avatars.com/api/?name=育儿专家&background=22c55e&color=fff&size=100',
    content: '补充一点，在实践中要注意因材施教。',
    time: '15分钟前',
    likes: 8,
    isLiked: false
  }
])

// 新评论内容
const newComment = ref('')

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
    // 如果没有上一页，重定向到阅读页面
    uni.redirectTo({
      url: '/pages/parent/reading/reading',
      fail: (err) => {
        console.error('重定向失败:', err)
        // 提示用户
        uni.showToast({
          title: '返回失败，请重试',
          icon: 'none'
        })
      }
    })
  }
}

// 加载评论
const loadComments = () => {
  // TODO: 从服务器加载评论数据
}

// 提交评论
const submitComment = () => {
  if (!newComment.value.trim()) {
    uni.showToast({
      title: '请输入评论内容',
      icon: 'none'
    })
    return
  }

  // 添加新评论
  comments.value.unshift({
    id: Date.now(),
    username: '我',
    avatar: 'https://ui-avatars.com/api/?name=我&background=f97316&color=fff&size=100',
    content: newComment.value,
    time: '刚刚',
    likes: 0,
    isLiked: false
  })

  // 清空输入框
  newComment.value = ''

  // TODO: 向服务器提交评论
}

// 点赞评论
const likeComment = (comment) => {
  comment.isLiked = !comment.isLiked
  comment.likes += comment.isLiked ? 1 : -1
}

// 回复评论
const replyComment = (comment) => {
  newComment.value = `@${comment.username} `
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
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 56px);
}

.original-paragraph {
  background-color: #f3f4f6;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.paragraph-text {
  font-size: 16px;
  line-height: 1.6;
  color: #374151;
}

.comments-list {
  flex: 1;
  margin-bottom: 60px;
}

.comment-item {
  padding: 16px 0;
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
  display: flex;
  gap: 12px;
  align-items: center;
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
  background-color: #3b82f6;
  border-radius: 50%;
}

.send-btn .fas {
  color: #ffffff;
  font-size: 14px;
}
</style> 
<template>
  <view class="like-button" @click="handleLike" :class="{ 'liked': isLiked }">
    <text class="fas fa-thumbs-up like-icon" :class="{ 'liked': isLiked }"></text>
    <text class="like-count">{{ formatCount(likeCount) }}</text>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { likeApi, userApi } from '@/utils/api.js'

// 定义props
const props = defineProps({
  targetId: {
    type: [Number, String],
    required: true
  },
  type: {
    type: Number,
    default: 1 // 1: 内容，2: 评论
  },
  initialLikeCount: {
    type: Number,
    default: 0
  },
  initialIsLiked: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'normal' // normal, small, large
  }
})

// 定义emits
const emit = defineEmits(['likeChanged'])

// 响应式数据
const likeCount = ref(props.initialLikeCount)
const isLiked = ref(props.initialIsLiked)
const loading = ref(false)

// 监听props变化
watch(() => props.initialLikeCount, (newValue) => {
  likeCount.value = newValue
})

watch(() => props.initialIsLiked, (newValue) => {
  isLiked.value = newValue
})

// 处理点赞事件
const handleLike = async () => {
  if (loading.value) return
  
  try {
    loading.value = true
    
    // 获取当前用户信息
    const userResponse = await userApi.getCurrentUser()
    if (!userResponse || !userResponse.data) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    const userId = userResponse.data.id
    console.log('切换点赞状态，目标ID：', props.targetId, '类型：', props.type, '当前状态：', isLiked.value)
    
    const response = await likeApi.toggleLike(userId, props.targetId, props.type)
    
    if (response && response.data) {
      // 更新本地状态
      isLiked.value = response.data.isLiked
      likeCount.value = response.data.likeCount
      
      // 触发事件，通知父组件
      emit('likeChanged', {
        targetId: props.targetId,
        type: props.type,
        isLiked: isLiked.value,
        likeCount: likeCount.value
      })
      
      // 给用户反馈
      uni.showToast({
        title: isLiked.value ? '点赞成功' : '取消点赞',
        icon: 'success',
        duration: 1000
      })
      
      console.log('点赞状态更新成功，新状态：', isLiked.value, '新点赞数：', likeCount.value)
    }
  } catch (error) {
    console.error('点赞操作失败：', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 格式化数量显示
const formatCount = (count) => {
  if (!count || count === 0) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + '万'
}
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.like-button {
  display: flex;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
  padding: 12rpx;
  margin: -12rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;
  user-select: none;
}

.like-button:hover {
  background-color: rgba(244, 63, 94, 0.1);
  transform: scale(1.05);
}

.like-button:active {
  transform: scale(0.95);
}

.like-icon {
  color: #9ca3af;
  font-size: 32rpx;
  transition: color 0.3s ease;
}

.like-icon.liked {
  color: #f43f5e;
  animation: likeAnimation 0.3s ease;
}

.like-count {
  font-size: 28rpx;
  color: #6b7280;
  transition: color 0.3s ease;
}

.like-button.liked .like-count {
  color: #f43f5e;
}

/* 点赞动画 */
@keyframes likeAnimation {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

/* 尺寸变体 */
.like-button.small .like-icon {
  font-size: 24rpx;
}

.like-button.small .like-count {
  font-size: 22rpx;
}

.like-button.large .like-icon {
  font-size: 40rpx;
}

.like-button.large .like-count {
  font-size: 32rpx;
}
</style> 
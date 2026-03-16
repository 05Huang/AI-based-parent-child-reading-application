<template>
  <view class="container">
    <!-- 返回按钮 -->
    <view class="back-button" @click="goBack" :style="{ top: (statusBarHeight || 20) + 10 + 'px' }">
      <text class="fas fa-arrow-left"></text>
    </view>

    <!-- 视频播放区域 -->
    <view class="video-player">
      <video 
        :src="videoInfo.mediaUrl || videoInfo.videoUrl" 
        :poster="videoInfo.coverUrl || videoInfo.coverImage"
        class="video"
        :controls="true"
        :autoplay="true"
        @play="onVideoPlay"
        @pause="onVideoPause"
        @ended="onVideoEnded"
      ></video>
    </view>

    <!-- 视频信息 -->
    <view class="video-info">
      <view class="video-title">{{ videoInfo.title }}</view>
      <view class="video-stats">
        <text class="views">{{ videoInfo.views }}次观看</text>
        <view class="actions">
          <view class="action-item" @click="toggleLike">
            <text class="fas fa-thumbs-up" :class="{ 'active': isLiked }"></text>
            <text>{{ videoInfo.likes }}</text>
          </view>
          <view class="action-item" @click="goToComments">
            <text class="fas fa-comment"></text>
            <text>{{ videoComments.length || videoInfo.comments }}</text>
          </view>
          <view class="action-item" @click="toggleBookmark">
            <text class="fas fa-bookmark" :class="{ 'active': isBookmarked }"></text>
            <text>收藏</text>
          </view>
          <view class="action-item" @click="shareVideo">
            <text class="fas fa-share"></text>
            <text>分享</text>
          </view>
        </view>
      </view>
      
      <!-- 创作者信息 -->
      <view class="creator-info">
        <image :src="videoInfo.creatorAvatar" mode="aspectFill" class="creator-avatar"></image>
        <view class="creator-detail">
          <text class="creator-name">{{ videoInfo.creatorName }}</text>
          <text class="creator-followers">{{ videoInfo.followers }}位关注者</text>
        </view>
        <button class="follow-btn" :class="{ followed: isFollowed }" @tap="toggleFollow">
          {{ isFollowed ? '已关注' : '关注' }}
        </button>
      </view>

      <!-- 视频描述 -->
      <view class="video-description">
        <text>{{ videoDescriptionText }}</text>
      </view>
    </view>

    <!-- 相关推荐 -->
    <view class="related-videos">
      <view class="section-header">
        <view class="section-title">相关推荐</view>
        <view class="more-btn" @click="navigateToAllVideos">
          <text>更多</text>
          <text class="fas fa-chevron-right"></text>
        </view>
      </view>
      <view class="video-list">
        <view class="video-item" v-for="(item, index) in relatedVideos" :key="index" @tap="playVideo(item)">
          <view class="video-thumb">
            <image :src="item.coverUrl" mode="aspectFill"></image>
            <text class="video-duration">{{ item.duration }}</text>
          </view>
          <view class="video-content">
            <text class="video-title">{{ item.title }}</text>
            <text class="video-category">{{ item.category }} · {{ item.views }}次观看</text>
            <view class="creator">
              <text class="creator-name">{{ item.creatorName }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 评论弹窗 -->
    <view v-if="showCommentPopup" class="comment-popup-mask" @click="closeCommentPopup">
      <view class="comment-popup" @click.stop>
        <!-- 弹窗标题栏 -->
        <view class="popup-header">
          <text class="popup-title">视频评论</text>
          <view class="close-btn" @click="closeCommentPopup">
            <text class="fas fa-times"></text>
          </view>
        </view>

        <!-- 评论列表 -->
        <scroll-view scroll-y="true" class="comments-list">
          <!-- 加载状态 -->
          <view v-if="loadingComments" class="loading-comments">
            <text class="fas fa-spinner fa-spin"></text>
            <text class="loading-text">加载中...</text>
          </view>

          <!-- 评论内容 -->
          <view v-else-if="videoComments.length > 0">
            <view v-for="comment in videoComments" :key="comment.id" class="comment-item">
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
          </view>

          <!-- 无评论提示 -->
          <view v-else class="no-comments">
            <text class="no-comments-text">还没有评论，快来抢沙发吧~</text>
          </view>
        </scroll-view>

        <!-- 评论输入框 -->
        <view class="comment-input">
          <!-- 回复提示 -->
          <view v-if="replyTo" class="reply-hint">
            <text class="reply-hint-text">回复 {{ replyTo.username }}</text>
            <view class="cancel-reply" @click="cancelReply">
              <text class="fas fa-times"></text>
            </view>
          </view>

          <!-- 输入框 -->
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
            <view class="send-btn" :class="{ 'active': newComment.trim().length > 0 }" @click="submitVideoComment">
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
  </view>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { viewHistoryApi, contentApi, userBehaviorApi, userApi, likeApi, favoriteApi, commentApi, videoApi, friendApi, recommendationApi } from '@/utils/api.js'

// 状态变量
const videoInfo = ref({})

const isFollowed = ref(false)
const isLiked = ref(false)
const isBookmarked = ref(false)
const relatedVideos = ref([])
const statusBarHeight = ref(20)
const watchStartTime = ref(null)
const currentUser = ref(null)
const showCommentPopup = ref(false)
const loadingComments = ref(false)
const videoComments = ref([])
const newComment = ref('')
const replyTo = ref(null)
const showEmojiPicker = ref(false)
const loading = ref(false)
const error = ref('')

const htmlToPlainText = (html) => {
  if (!html) return ''

  let text = String(html)
    .replace(/<\s*br\s*\/?\s*>/gi, '\n')
    .replace(/<\/\s*p\s*>/gi, '\n')
    .replace(/<\s*p[^>]*>/gi, '')
    .replace(/<\/\s*div\s*>/gi, '\n')
    .replace(/<\s*div[^>]*>/gi, '')
    .replace(/<\/\s*li\s*>/gi, '\n')
    .replace(/<\s*li[^>]*>/gi, '• ')
    .replace(/<[^>]+>/g, '')

  text = text
    .replace(/&nbsp;/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")

  text = text
    .replace(/\r\n/g, '\n')
    .replace(/[ \t]+\n/g, '\n')
    .replace(/\n[ \t]+/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()

  return text
}

const formatDuration = (seconds) => {
  const value = Number(seconds);
  if (!Number.isFinite(value) || value <= 0) return '00:00';
  const mins = Math.floor(value / 60);
  const secs = Math.floor(value % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

const formatViewCount = (count) => {
  const value = Number(count);
  if (!Number.isFinite(value) || value <= 0) return '0';
  if (value < 1000) return String(value);
  if (value < 10000) return (value / 1000).toFixed(1) + 'K';
  return (value / 10000).toFixed(1) + 'W';
}

const videoDescriptionText = computed(() => {
  const raw = videoInfo.value?.description || videoInfo.value?.content || ''
  const text = htmlToPlainText(raw)
  return text || '暂无简介'
})

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
  '👌', '🤏', '👈', '👉', '👆', '👇', '☝️', '✋'
])

// 生命周期
onLoad((options) => {
  // 获取状态栏高度
  try {
    const systemInfo = uni.getSystemInfoSync()
    statusBarHeight.value = systemInfo.statusBarHeight || 20
    console.log('[视频详情] 状态栏高度:', statusBarHeight.value)
  } catch (error) {
    console.error('[视频详情] 获取状态栏高度失败:', error)
  }

  // 记录观看开始时间
  watchStartTime.value = Date.now()
  console.log('开始观看视频，记录时间戳：', watchStartTime.value)

  // 处理页面参数
  let id = options.id
  if (!id && options.videoInfo) {
    try {
      const videoData = JSON.parse(decodeURIComponent(options.videoInfo))
      if (videoData.id) {
        id = videoData.id
        // 先设置已有信息，避免加载等待
        videoInfo.value = { ...videoInfo.value, ...videoData }
      }
    } catch (e) {
      console.error('解析视频数据失败:', e)
    }
  }

  if (id) {
    videoInfo.value.id = id
    loadData()
  } else {
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
  }
})

onUnload(() => {
  console.log('视频详情页面卸载')
  
  // 记录观看时长
  if (watchStartTime.value && currentUser.value?.id && videoInfo.value?.id) {
    const watchEndTime = Date.now()
    const duration = Math.floor((watchEndTime - watchStartTime.value) / 1000) // 转换为秒
    
    console.log('结束观看，时长：', duration, '秒')
    
    // 只有观看时长超过3秒才记录
    if (duration >= 3) {
      try {
        userBehaviorApi.recordReadingBehavior(
          currentUser.value.id,
          videoInfo.value.id,
          duration,
          0 // 视频没有阅读进度，设为0
        )
        console.log('视频观看行为记录成功')
      } catch (error) {
        console.error('记录视频观看行为失败：', error)
      }
    } else {
      console.log('观看时长过短，不记录')
    }
  }
})

// 方法
const loadCurrentUser = async () => {
  try {
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      currentUser.value = response.data
    }
  } catch (err) {
    console.error('加载用户信息失败：', err)
  }
}

const loadData = async () => {
  loading.value = true
  error.value = ''
  
  try {
    // 并行加载用户信息和视频数据
    await loadCurrentUser()
    await loadVideoData()
  } catch (err) {
    console.error('加载数据失败：', err)
    error.value = '加载失败，请重试'
  } finally {
    loading.value = false
  }
}

const loadVideoData = async () => {
  try {
    let response = null
    let data = null
    const id = videoInfo.value.id
    
    // 优先使用 videoApi
    try {
      response = await videoApi.getVideoDetail(id)
      if (response && response.code === 200 && response.data) {
        data = response.data
        console.log('[视频详情] 使用 videoApi 加载视频数据成功')
      } else if (response && response.data) {
        // 兼容没有 code 字段的情况
        data = response.data
        console.log('[视频详情] 使用 videoApi 加载视频数据成功（兼容格式）')
      }
    } catch (videoApiError) {
      console.warn('[视频详情] videoApi 加载失败，尝试使用 contentApi：', videoApiError)
    }
    
    // 如果 videoApi 失败，使用 contentApi
    if (!data) {
      try {
        response = await contentApi.getContentDetail(id)
        if (response && response.data) {
          data = response.data
          console.log('[视频详情] 使用 contentApi 加载视频数据成功')
        }
      } catch (contentApiError) {
        console.error('[视频详情] contentApi 加载失败：', contentApiError)
      }
    }
    
    if (data) {
      // 关键修正：确保 mediaUrl 是有效的 URL
      // 检查 content 是否是 URL (http 开头)
      const contentUrl = (data.content && typeof data.content === 'string' && data.content.startsWith('http')) ? data.content : '';
      
      const mediaUrl = data.mediaUrl || data.videoUrl || contentUrl;

      videoInfo.value = {
        id: data.id,
        title: data.title || '无标题',
        views: data.viewCount || data.views || 0,
        likes: data.likeCount || data.likes || 0,
        comments: data.commentCount || data.comments || 0,
        mediaUrl: mediaUrl,
        videoUrl: mediaUrl,
        coverUrl: data.coverUrl || data.coverImage,
        coverImage: data.coverUrl || data.coverImage,
        creatorId: data.creatorId || data.creator_id || data.userId || data.user_id || data.authorId || data.author_id,
        creatorName: data.creatorName || '匿名作者',
        creatorAvatar: data.creatorAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(data.creatorName || '匿名')}&background=8b5cf6&color=fff&size=100`,
        followers: data.followers || 0,
        description: data.description || (contentUrl ? '' : data.content) || '', // 如果 content 是 URL，则不作为描述显示
        category: data.category || '视频'
      }
      
      console.log('[视频详情] 视频数据加载成功：', videoInfo.value)
      console.log('[视频详情] 视频URL：', videoInfo.value.mediaUrl)
      
      // 验证视频URL是否存在
      if (!videoInfo.value.mediaUrl) {
        console.warn('[视频详情] 警告：视频URL为空')
        uni.showToast({
          title: '视频地址不存在',
          icon: 'none'
        })
      }

      await loadRelatedVideos()
      
      // 加载点赞状态和收藏状态
      if (currentUser.value && currentUser.value.id) {
        await loadLikeStatus()
        await loadBookmarkStatus()
        await loadFollowStatus()
      }
      
      // 加载初始评论数量
      await loadCommentCount()
    } else {
      throw new Error('视频数据为空')
    }
  } catch (err) {
    console.error('加载视频失败：', err)
    uni.showToast({
      title: '加载视频失败',
      icon: 'none'
    })
    throw err
  }
}

const mapRelatedVideo = (item) => {
  const coverUrl = item?.coverUrl || item?.coverImage || 'https://images.unsplash.com/photo-1551029506-0807df4e2031?w=500&auto=format&fit=crop&q=60';
  const views = item?.viewCount ?? item?.views ?? 0;
  const durationSeconds = item?.contentLength ?? item?.duration ?? 0;
  return {
    id: item?.id,
    title: item?.title || '推荐视频',
    coverUrl,
    duration: formatDuration(durationSeconds),
    category: item?.category || item?.categoryName || videoInfo.value?.category || '视频',
    views: formatViewCount(views),
    creatorName: item?.creatorName || '内容创作者'
  };
};

const loadRelatedVideosFallback = async () => {
  try {
    const firstResponse = await videoApi.getVideoPage({
      current: 1,
      size: 10,
      sortField: 'created_time',
      sortOrder: 'desc'
    });

    if (!(firstResponse && firstResponse.code === 200 && firstResponse.data)) {
      relatedVideos.value = [];
      return;
    }

    const totalPages = firstResponse.data.pages || 1;
    const randomPage = totalPages > 1 ? Math.floor(Math.random() * Math.min(totalPages, 5)) + 1 : 1;

    const response = randomPage === 1 ? firstResponse : await videoApi.getVideoPage({
      current: randomPage,
      size: 10,
      sortField: 'created_time',
      sortOrder: 'desc'
    });

    const records = response?.data?.records || [];
    const currentId = String(videoInfo.value?.id || '');
    const candidates = records
      .filter(item => item && item.type === 2 && item.id && String(item.id) !== currentId)
      .sort(() => Math.random() - 0.5)
      .slice(0, 6);

    relatedVideos.value = candidates.map(mapRelatedVideo).filter(v => v && v.id);
  } catch (error) {
    console.error('[视频详情] 加载相关推荐失败（随机降级）：', error);
    relatedVideos.value = [];
  }
};

const loadRelatedVideos = async (shuffle = false) => {
  const currentId = String(videoInfo.value?.id || '');
  const userId = currentUser.value?.id || uni.getStorageSync('currentUserId') || uni.getStorageSync('userId') || null;

  try {
    if (userId) {
      const response = await recommendationApi.getVideoRecommendations(userId, 10, shuffle);
      const candidates = (Array.isArray(response?.data) ? response.data : [])
        .filter(item => item && item.type === 2 && item.id && String(item.id) !== currentId)
        .slice(0, 6);

      if (candidates.length > 0) {
        relatedVideos.value = candidates.map(mapRelatedVideo).filter(v => v && v.id);
        return;
      }
    }
  } catch (error) {
    console.error('[视频详情] 加载相关推荐失败（Python 推荐），使用随机降级：', error);
  }

  await loadRelatedVideosFallback();
}

const loadLikeStatus = async () => {
  try {
    const response = await likeApi.getLikeStatus(currentUser.value.id, videoInfo.value.id, 1)
    if (response && response.data !== undefined) {
      isLiked.value = response.data
      console.log('[视频详情] 点赞状态加载成功：', isLiked.value)
    }
  } catch (err) {
    console.error('加载点赞状态失败：', err)
  }
}

const loadBookmarkStatus = async () => {
  try {
    const response = await favoriteApi.getFavoriteStatus(currentUser.value.id, videoInfo.value.id)
    if (response && response.data !== undefined) {
      isBookmarked.value = response.data
      console.log('[视频详情] 收藏状态加载成功：', isBookmarked.value)
    }
  } catch (error) {
    console.error('加载收藏状态失败：', error)
  }
}

const loadFollowStatus = async () => {
  const userId = currentUser.value?.id
  const creatorId = videoInfo.value?.creatorId
  if (!userId || !creatorId || String(userId) === String(creatorId)) {
    isFollowed.value = false
    return
  }

  try {
    const res = await friendApi.getFollowStatus(creatorId)
    if (res && res.data && res.data.followed !== undefined) {
      isFollowed.value = !!res.data.followed
    }
  } catch (e) {
    isFollowed.value = false
  }
}

const toggleFollow = async () => {
  if (!currentUser.value?.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  const creatorId = videoInfo.value?.creatorId
  if (!creatorId) {
    uni.showToast({
      title: '作者信息缺失',
      icon: 'none'
    })
    return
  }
  if (String(currentUser.value.id) === String(creatorId)) {
    uni.showToast({
      title: '不能关注自己',
      icon: 'none'
    })
    return
  }

  try {
    if (!isFollowed.value) {
      await friendApi.addFriend({ friendId: creatorId })
      isFollowed.value = true
      if (typeof videoInfo.value.followers === 'number') {
        videoInfo.value.followers += 1
      }
      uni.showToast({ title: '已关注', icon: 'none' })
    } else {
      await friendApi.unfollow(creatorId)
      isFollowed.value = false
      if (typeof videoInfo.value.followers === 'number') {
        videoInfo.value.followers = Math.max(0, videoInfo.value.followers - 1)
      }
      uni.showToast({ title: '已取消关注', icon: 'none' })
    }
  } catch (err) {
    const msg = err?.message || err?.msg || err?.data?.message || ''
    if (String(msg).includes('已是好友')) {
      isFollowed.value = true
      return
    }
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  } finally {
    await loadFollowStatus()
  }
}

const toggleLike = async () => {
  if (!currentUser.value || !currentUser.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    const response = await likeApi.toggleLike(currentUser.value.id, videoInfo.value.id, 1)
    if (response && response.data) {
      isLiked.value = response.data.isLiked
      videoInfo.value.likes = response.data.likeCount || 0
      uni.showToast({
        title: isLiked.value ? '已点赞' : '已取消点赞',
        icon: 'success'
      })
    }
  } catch (err) {
    console.error('点赞失败：', err)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
}

const toggleBookmark = async () => {
  if (!currentUser.value || !currentUser.value.id || !videoInfo.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('[视频详情] 开始切换收藏状态，当前状态：', isBookmarked.value)
    
    const response = await favoriteApi.toggleFavorite(currentUser.value.id, videoInfo.value.id)
    
    if (response && response.data !== undefined) {
      isBookmarked.value = response.data
      
      console.log('[视频详情] 收藏状态更新成功：', isBookmarked.value)
      
      uni.showToast({
        title: isBookmarked.value ? '已收藏' : '已取消收藏',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('[视频详情] 更新收藏状态失败：', error)
    uni.showToast({
      title: '收藏失败，请重试',
      icon: 'none'
    })
  }
}

const playVideo = (video) => {
  // 播放新的视频时，将完整的视频信息传递过去
  const videoInfoStr = encodeURIComponent(JSON.stringify(video))
  uni.navigateTo({
    url: `/pages/child/video-detail?videoInfo=${videoInfoStr}&id=${video.id}`
  })
}

const navigateToAllVideos = () => {
  console.log('[视频详情] 跳转到全部视频页面')
  uni.navigateTo({
    url: '/pages/child/video/all-videos'
  })
}

const goBack = () => {
  console.log('[视频详情] 点击返回按钮')
  
  try {
    // 获取当前页面栈
    const pages = getCurrentPages()
    console.log('[视频详情] 当前页面栈长度:', pages.length)
    
    if (pages.length > 1) {
      // 如果有上一页，直接返回
      console.log('[视频详情] 执行返回操作')
      uni.navigateBack({
        delta: 1,
        success: () => {
          console.log('[视频详情] 返回上一页成功')
        },
        fail: (err) => {
          console.error('[视频详情] 返回失败:', err)
          fallbackNavigation()
        }
      })
    } else {
      // 如果没有上一页，使用备用导航
      console.log('[视频详情] 页面栈只有一层，使用备用导航')
      fallbackNavigation()
    }
  } catch (error) {
    console.error('[视频详情] 返回操作异常:', error)
    fallbackNavigation()
  }
}

const fallbackNavigation = () => {
  console.log('[视频详情] 执行备用导航')
  
  // 优先尝试返回到视频列表页
  uni.switchTab({
    url: '/pages/child/video',
    success: () => {
      console.log('[视频详情] 跳转到视频列表成功')
    },
    fail: (err) => {
      console.warn('[视频详情] 跳转到视频列表失败，尝试跳转首页:', err)
      // 如果视频列表页跳转失败，尝试返回到首页
      uni.switchTab({
        url: '/pages/child/home',
        success: () => {
          console.log('[视频详情] 跳转到首页成功')
        },
        fail: (err2) => {
          console.error('[视频详情] 跳转首页也失败:', err2)
          uni.showToast({
            title: '返回失败，请重试',
            icon: 'none',
            duration: 2000
          })
        }
      })
    }
  })
}

const shareVideo = async () => {
  console.log('[儿童端] 开始分享视频：', videoInfo.value.title)
  
  if (!videoInfo.value.id) {
    uni.showToast({
      title: '视频数据未加载',
      icon: 'none'
    })
    return
  }
  
  // 记录分享行为到数据库
  const userId = uni.getStorageSync('currentUserId') || uni.getStorageSync('userId')
  if (userId) {
    try {
      await userBehaviorApi.recordShareBehavior(userId, videoInfo.value.id)
      console.log('[儿童端] 视频分享行为记录成功')
    } catch (error) {
      console.error('[儿童端] 记录视频分享行为失败：', error)
    }
  }
  
  // 构建分享内容
  const shareTitle = videoInfo.value.title || '精彩视频'
  const shareContent = `推荐观看：《${shareTitle}》`
  
  // 检测平台
  const systemInfo = uni.getSystemInfoSync()
  console.log('[儿童端] 当前平台：', systemInfo.platform)
  
  // #ifdef H5
  if (navigator.share) {
    console.log('[儿童端] 使用Web Share API')
    navigator.share({
      title: shareTitle,
      text: shareContent,
      url: window.location.href
    }).then(() => {
      console.log('[儿童端] 视频分享成功')
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    }).catch((error) => {
      console.error('[儿童端] 视频分享失败：', error)
      if (error.name !== 'AbortError') {
        showShareActionSheet()
      }
    })
  } else {
    console.log('[儿童端] 不支持Web Share API，显示分享选项')
    showShareActionSheet()
  }
  // #endif
  
  // #ifdef MP-WEIXIN
  console.log('[儿童端] 微信小程序环境，使用小程序分享')
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline'],
    success: () => {
      uni.showToast({
        title: '请点击右上角分享',
        icon: 'none'
      })
    },
    fail: () => {
      uni.showToast({
        title: '分享失败',
        icon: 'none'
      })
    }
  })
  // #endif
  
  // #ifdef APP-PLUS
  console.log('[儿童端] App环境，尝试使用系统分享')
  uni.share({
    provider: 'system',
    type: 1,
    title: shareTitle,
    summary: `来自阅桥亲子阅读APP：${shareTitle}`,
    href: `https://parentreading.com/video/${videoInfo.value.id}`,
    success: (res) => {
      console.log('[儿童端] 视频系统分享成功：', res)
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('[儿童端] 视频系统分享失败，降级使用操作菜单：', err)
      showShareActionSheet()
    }
  })
  // #endif
}

const showShareActionSheet = () => {
  console.log('[儿童端] 显示视频分享操作菜单')
  
  // #ifdef APP-PLUS
  uni.showActionSheet({
    itemList: ['复制链接', '使用系统分享', '生成分享海报'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0: copyVideoLink(); break;
        case 1: useSystemShare(); break;
        case 2: generateSharePoster(); break;
      }
    },
    fail: (err) => console.error('[儿童端] 显示视频分享菜单失败：', err)
  })
  // #endif
  
  // #ifndef APP-PLUS
  uni.showActionSheet({
    itemList: ['复制链接', '生成分享海报'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0: copyVideoLink(); break;
        case 1: generateSharePoster(); break;
      }
    },
    fail: (err) => console.error('[儿童端] 显示视频分享菜单失败：', err)
  })
  // #endif
}

const useSystemShare = async () => {
  // ... (keeping implementation brief for now, similar to original)
  console.log('[儿童端] 使用系统分享视频')
  // #ifdef APP-PLUS
  const shareTitle = videoInfo.value.title || '精彩视频'
  uni.share({
    provider: 'system',
    type: 1,
    title: shareTitle,
    summary: `来自阅桥亲子阅读APP：${shareTitle}`,
    href: `https://parentreading.com/video/${videoInfo.value.id}`,
    success: () => uni.showToast({ title: '分享成功', icon: 'success' }),
    fail: () => uni.showToast({ title: '分享失败', icon: 'none' })
  })
  // #endif
}

const copyVideoLink = () => {
  let videoLink = ''
  // #ifdef H5
  videoLink = window.location.href
  // #endif
  // #ifndef H5
  videoLink = `https://parentreading.com/video/${videoInfo.value.id}`
  // #endif
  const shareText = `${videoInfo.value.title}\n\n${videoLink}`
  uni.setClipboardData({
    data: shareText,
    success: () => uni.showToast({ title: '链接已复制', icon: 'success' })
  })
}

const generateSharePoster = () => {
  uni.showToast({ title: '海报生成功能开发中', icon: 'none' })
}

const goToComments = () => {
  console.log('[视频详情] 跳转到评论页面')
  openCommentPopup()
}

const openCommentPopup = () => {
  showCommentPopup.value = true
  loadComments()
}

const closeCommentPopup = () => {
  showCommentPopup.value = false
  newComment.value = ''
  replyTo.value = null
}

const loadCommentCount = async () => {
  try {
    const response = await commentApi.getContentComments(videoInfo.value.id, 1, 10)
    if (response && response.data && response.data.records) {
       // Just update the count or simple list
       // For now, we update videoComments to show count in button
       // But wait, the button shows videoComments.length
       // We should perhaps just store the count if we don't want to load all
       // But the original code loaded 10 comments and set videoComments.
       // Let's do the same logic but mapped.
       const records = response.data.records
       videoComments.value = records.map(c => ({
         id: c.id,
         // ... simplified mapping for count
         username: c.userNickname,
         // ...
       }))
       // Actually, we should fully map it if we use it in the list immediately
       // But loadComments does full mapping. 
       // Let's call loadComments directly if we want comments loaded, 
       // or just get count. The original code did a map.
       // For simplicity and correctness, let's just let loadData call loadCommentCount which does a simple fetch
       // The original code in loadCommentCount did map and set videoComments.
       // We will do the same but correctly using our formatCommentTime
       const formatted = records.map(comment => ({
          id: comment.id,
          username: comment.userNickname || '匿名用户',
          avatar: comment.userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(comment.userNickname || '匿名')}&background=8b5cf6&color=fff&size=100`,
          content: comment.content,
          time: formatCommentTime(comment.createdTime),
          likes: comment.likeCount || 0,
          isLiked: false,
          replies: [],
          rootId: comment.rootId || 0,
          parentId: comment.parentId || 0
       }))
       videoComments.value = formatted
    }
  } catch (error) {
    console.error('加载初始评论失败：', error)
  }
}

const loadComments = async () => {
  loadingComments.value = true
  try {
    const response = await commentApi.getContentComments(videoInfo.value.id, 1, 50)
    if (response && response.data && response.data.records) {
      const formattedComments = await Promise.all(response.data.records.map(async comment => {
        const commentData = {
          id: comment.id,
          username: comment.userNickname || '匿名用户',
          avatar: comment.userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(comment.userNickname || '匿名')}&background=8b5cf6&color=fff&size=100`,
          content: comment.content,
          time: formatCommentTime(comment.createdTime),
          likes: comment.likeCount || 0,
          isLiked: false,
          replies: [],
          rootId: comment.rootId || 0,
          parentId: comment.parentId || 0
        }
        
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
      
      // Organize hierarchy
      const rootComments = []
      const commentMap = new Map()
      formattedComments.forEach(c => {
        commentMap.set(c.id, c)
        c.replies = []
      })
      
      formattedComments.forEach(comment => {
        if (comment.parentId === 0) {
          rootComments.push(comment)
        } else {
          let targetComment = null
          if (comment.rootId && comment.rootId !== 0) {
            targetComment = commentMap.get(comment.rootId)
          }
          if (!targetComment) {
            targetComment = commentMap.get(comment.parentId)
          }
          if (targetComment) {
             if (comment.parentId !== comment.rootId && comment.parentId !== 0) {
                const directParent = commentMap.get(comment.parentId)
                if (directParent) {
                  comment.replyToUsername = directParent.username
                }
             }
             targetComment.replies.push(comment)
          } else {
            rootComments.push(comment)
          }
        }
      })
      videoComments.value = rootComments
    }
  } catch (error) {
    console.error('加载评论失败：', error)
    uni.showToast({ title: '加载评论失败', icon: 'none' })
  } finally {
    loadingComments.value = false
  }
}

const submitVideoComment = async () => {
  if (!newComment.value.trim()) {
    uni.showToast({ title: '请输入评论内容', icon: 'none' })
    return
  }
  if (!currentUser.value || !currentUser.value.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  
  try {
    const commentData = {
      contentId: videoInfo.value.id,
      content: newComment.value.trim(),
      commentType: 1,
      parentId: replyTo.value ? replyTo.value.id : 0,
      rootId: replyTo.value ? (replyTo.value.rootId || replyTo.value.id) : 0
    }
    
    const response = await commentApi.addComment(commentData)
    if (response && response.data) {
      uni.showToast({ title: '评论成功', icon: 'success' })
      newComment.value = ''
      replyTo.value = null
      showEmojiPicker.value = false
      await loadComments()
    }
  } catch (error) {
    console.error('提交评论失败：', error)
    uni.showToast({ title: '评论失败，请重试', icon: 'none' })
  }
}

const replyComment = (comment) => {
  replyTo.value = comment
  newComment.value = `@${comment.username}：`
  showEmojiPicker.value = false
}

const cancelReply = () => {
  replyTo.value = null
  newComment.value = ''
}

const likeComment = async (comment) => {
  if (!currentUser.value || !currentUser.value.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    const response = await likeApi.toggleLike(currentUser.value.id, comment.id, 2)
    if (response && response.data) {
      comment.isLiked = response.data.isLiked
      comment.likes = response.data.likeCount || 0
      uni.showToast({
        title: comment.isLiked ? '已点赞' : '已取消点赞',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('点赞失败：', error)
    uni.showToast({ title: '点赞失败，请重试', icon: 'none' })
  }
}

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const insertEmoji = (emoji) => {
  newComment.value += emoji
}

const formatCommentTime = (timestamp) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);

  if (diffInSeconds < 60) {
    return `${diffInSeconds}秒前`;
  } else if (diffInSeconds < 3600) {
    return `${Math.floor(diffInSeconds / 60)}分钟前`;
  } else if (diffInSeconds < 86400) {
    return `${Math.floor(diffInSeconds / 3600)}小时前`;
  } else {
    return `${Math.floor(diffInSeconds / 86400)}天前`;
  }
}

const onVideoPlay = () => {
  console.log('[视频详情] 视频开始播放')
  watchStartTime.value = Date.now()
}

const onVideoPause = () => {
  console.log('[视频详情] 视频暂停播放')
}

const onVideoEnded = () => {
  console.log('[视频详情] 视频播放结束')
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
}

/* 返回按钮样式 */
.back-button {
  position: fixed;
  left: 20rpx;
  z-index: 999;
  width: 72rpx;
  height: 72rpx;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
}

.back-button:active {
  transform: scale(0.9);
  background-color: rgba(0, 0, 0, 0.7);
}

.back-button .fas {
  color: #ffffff;
  font-size: 36rpx;
  pointer-events: none;
}

.video-player {
  width: 100%;
  height: 422rpx;
  background-color: #000;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.video {
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 1;
  object-fit: contain;
}

/* 隐藏视频播放器控件中的下载和投屏按钮（左上角） */
/* Webkit浏览器（Chrome, Safari等） */
.video::-webkit-media-controls {
  display: none !important;
}

.video::-webkit-media-controls-download-button {
  display: none !important;
}

.video::-webkit-media-controls-cast-button {
  display: none !important;
}

/* 隐藏视频播放器左上角的控制按钮 */
.video::-webkit-media-controls-panel {
  display: none !important;
}

/* 如果视频控件仍然显示，使用更通用的方式隐藏 */
video::-webkit-media-controls-enclosure {
  display: none !important;
}

.video-info {
  background-color: #fff;
  padding: 32rpx;
  margin-bottom: 20rpx;
}

.video-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 16rpx;
}

.video-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.views {
  font-size: 26rpx;
  color: #6b7280;
}

.actions {
  display: flex;
  gap: 32rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #6b7280;
  font-size: 26rpx;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 4rpx 8rpx;
  border-radius: 8rpx;
}

.action-item:active {
  transform: scale(0.95);
  background-color: rgba(0, 0, 0, 0.05);
}

.action-item .fas {
  font-size: 26rpx;
  color: #6b7280;
  transition: color 0.3s ease;
}

.action-item .fas.active {
  color: #6366f1;
}

.action-item.active {
  color: #6366f1;
}

.creator-info {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-top: 2rpx solid #e5e7eb;
  border-bottom: 2rpx solid #e5e7eb;
}

.creator-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.creator-detail {
  flex: 1;
}

.creator-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2937;
}

.creator-followers {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

.follow-btn {
  padding: 12rpx 32rpx;
  background-color: #6366f1;
  color: #ffffff;
  border-radius: 30rpx;
  font-size: 26rpx;
  margin: 0;
}

.follow-btn.followed {
  background-color: #e5e7eb;
  color: #6b7280;
}

.video-description {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.6;
  white-space: pre-line;
  word-break: break-word;
}

.related-videos {
  background-color: #fff;
  padding: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 0;
}

.more-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #6b7280;
  padding: 8rpx 0;
}

.more-btn .fas {
  font-size: 24rpx;
}

.video-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.video-item {
  display: flex;
  gap: 20rpx;
}

.video-thumb {
  position: relative;
  width: 240rpx;
  height: 135rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.video-thumb image {
  width: 100%;
  height: 100%;
}

.video-duration {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  background-color: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  padding: 4rpx 8rpx;
  border-radius: 4rpx;
  font-size: 20rpx;
}

.video-content {
  flex: 1;
}

.video-content .video-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-category {
  font-size: 24rpx;
  color: #6b7280;
}

.video-content .creator-name {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 8rpx;
}

/* 评论弹窗样式 */
.comment-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: flex-end;
  z-index: 1000;
}

.comment-popup {
  width: 100%;
  background-color: #fff;
  border-top-left-radius: 30rpx;
  border-top-right-radius: 30rpx;
  padding: 32rpx;
  box-sizing: border-box;
  max-height: 80vh; /* 弹窗最大高度 */
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.popup-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f2937;
}

.close-btn {
  padding: 12rpx;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn .fas {
  color: #6b7280;
  font-size: 40rpx;
}

.comments-list {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-right: 16rpx;
}

.loading-comments {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 0;
}

.loading-comments .fas {
  color: #6366f1;
  font-size: 40rpx;
  margin-right: 16rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #6b7280;
}

.comment-item {
  background-color: #f9fafb;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  position: relative;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.small.avatar {
  width: 40rpx;
  height: 40rpx;
}

.comment-info {
  flex: 1;
}

.username {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2937;
}

.time {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

.comment-content {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.comment-actions {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #6b7280;
  font-size: 26rpx;
}

.action-btn.active {
  color: #6366f1;
}

.action-text {
  font-size: 26rpx;
  color: #6b7280;
}

.replies-container {
  margin-top: 24rpx;
  padding-left: 60rpx; /* 与父评论的缩进对齐 */
}

.reply-item {
  background-color: #f0f2f5;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  position: relative;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.reply-header .avatar {
  width: 40rpx;
  height: 40rpx;
  margin-right: 12rpx;
}

.reply-info {
  flex: 1;
}

.reply-info .username {
  font-size: 26rpx;
  font-weight: 500;
  color: #1f2937;
}

.reply-to {
  font-size: 24rpx;
  color: #6b7280;
  margin-left: 8rpx;
}

.reply-info .time {
  font-size: 22rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

.reply-content {
  font-size: 26rpx;
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 12rpx;
}

.reply-actions {
  display: flex;
  gap: 20rpx;
}

.reply-actions .action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #6b7280;
  font-size: 24rpx;
}

.reply-actions .action-btn.active {
  color: #6366f1;
}

.no-comments {
  text-align: center;
  padding: 40rpx 0;
}

.no-comments-text {
  font-size: 28rpx;
  color: #6b7280;
}

.comment-input {
  position: relative;
  bottom: auto;
  left: auto;
  right: auto;
  background-color: #fff;
  padding: 24rpx 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  box-shadow: none;
  border-top: 1rpx solid #e5e7eb;
  z-index: 999;
}

.input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #f0f0f0;
  border-radius: 40rpx;
  padding: 12rpx 20rpx;
  box-sizing: border-box;
  margin: 0 32rpx;
}

.emoji-btn {
  padding: 12rpx;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12rpx;
  flex-shrink: 0;
}

.emoji-btn .fas {
  color: #6b7280;
  font-size: 36rpx;
}

.input-field {
  flex: 1;
  font-size: 28rpx;
  color: #1f2937;
  padding: 0;
  border: none;
  outline: none;
  background-color: transparent;
}

.send-btn {
  padding: 12rpx 24rpx;
  background-color: #6366f1;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.send-btn .fas {
  color: #ffffff;
  font-size: 36rpx;
}

.send-btn.active {
  background-color: #5254d0;
}

.emoji-picker {
  position: absolute;
  bottom: 100%; /* 在输入框上方 */
  left: 0;
  width: 100%;
  background-color: #fff;
  border-top-left-radius: 20rpx;
  border-top-right-radius: 20rpx;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.1);
  z-index: 1001;
  padding: 16rpx 0;
  box-sizing: border-box;
}

.emoji-picker .emoji-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20rpx 16rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.emoji-picker .emoji-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
}

.emoji-picker .close-emoji {
  padding: 12rpx;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emoji-picker .close-emoji .fas {
  color: #6b7280;
  font-size: 40rpx;
}

.emoji-picker .emoji-list {
  padding: 16rpx 0;
  box-sizing: border-box;
}

.emoji-picker .emoji-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12rpx;
  padding: 0 20rpx;
  box-sizing: border-box;
}

.emoji-picker .emoji-item {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  cursor: pointer;
}

.emoji-picker .emoji-text {
  line-height: 1;
}

.reply-hint {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 32rpx;
  background-color: rgba(99, 102, 241, 0.1);
  border-radius: 12rpx;
  margin: 0 32rpx;
  transition: background-color 0.3s ease;
}

.reply-hint-text {
  font-size: 26rpx;
  color: #6366f1;
  font-weight: 500;
}

.cancel-reply {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.cancel-reply:active {
  background-color: rgba(0, 0, 0, 0.15);
}

.cancel-reply .fas {
  font-size: 24rpx;
  color: #6b7280;
}
</style>

<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar"></view>
    
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="fas fa-arrow-left back-icon"></text>
      </view>
      <view class="header-center">
        <text class="header-title">视频详情</text>
      </view>
      <view class="header-right">
        <text class="fas fa-share-alt share-icon" @click="shareVideo"></text>
      </view>
    </view>

    <!-- 视频播放器 -->
    <view class="video-container">
      <video 
        :src="videoInfo.mediaUrl" 
        :poster="videoInfo.coverUrl"
        controls
        autoplay
        class="video-player"
        @play="onVideoPlay"
        @pause="onVideoPause"
        @ended="onVideoEnded"
        @timeupdate="onVideoTimeUpdate"
      ></video>
    </view>

    <!-- 视频信息 -->
    <scroll-view scroll-y="true" class="content-area">
      <view class="video-info">
        <text class="video-title">{{ videoInfo.title || '加载中...' }}</text>
        <view class="video-stats">
          <view class="stat-item">
            <text class="fas fa-play-circle"></text>
            <text class="stat-text">{{ formatViewCount(videoInfo.viewCount) }}次播放</text>
          </view>
          <view class="stat-item like-btn" @click="toggleVideoLike">
            <text class="fas fa-heart" :class="{ 'liked': isVideoLiked }"></text>
            <text class="stat-text">{{ videoInfo.likeCount || 0 }}</text>
          </view>
          <view class="stat-item">
            <text class="fas fa-comment"></text>
            <text class="stat-text">{{ videoInfo.commentCount || 0 }}</text>
          </view>
          <view class="stat-item">
            <text class="fas fa-share"></text>
            <text class="stat-text">{{ videoInfo.shareCount || 0 }}</text>
          </view>
        </view>
        <view class="video-meta">
          <text class="video-time">{{ formatTime(videoInfo.createdTime) }}</text>
          <view class="video-tags" v-if="videoInfo.tags">
            <text 
              v-for="tag in getTagList(videoInfo.tags)" 
              :key="tag" 
              class="tag"
            >#{{ tag }}</text>
          </view>
        </view>
      </view>

      <!-- 视频描述 -->
      <view class="video-description" v-if="videoInfo.content">
        <view class="section-title">
          <text class="title-text">视频介绍</text>
        </view>
        <text class="description-text">{{ videoInfo.content }}</text>
      </view>

      <!-- 创作者信息 -->
      <view class="creator-info" v-if="videoInfo.creatorName">
        <view class="creator-avatar">
          <image 
            :src="videoInfo.creatorAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + videoInfo.creatorName" 
            mode="aspectFill"
          ></image>
        </view>
        <view class="creator-details">
          <text class="creator-name">{{ videoInfo.creatorName }}</text>
          <text class="creator-desc">内容创作者</text>
        </view>
        <view class="follow-btn">
          <text class="follow-text">关注</text>
        </view>
      </view>

      <!-- 相关推荐 -->
      <view class="related-videos">
        <view class="section-title">
          <text class="title-text">相关推荐</text>
          <text class="refresh-btn" @click="loadRelatedVideos">
            <text class="fas fa-sync-alt"></text>
            换一批
          </text>
        </view>
        <view class="related-list">
          <view 
            v-for="video in relatedVideos" 
            :key="video.id"
            class="related-item"
            @click="playRelatedVideo(video)"
          >
            <view class="related-cover">
              <image :src="video.coverUrl" mode="aspectFill"></image>
              <text class="video-duration">{{ formatDuration(video.contentLength) }}</text>
            </view>
            <view class="related-info">
              <text class="related-title">{{ video.title }}</text>
              <view class="related-meta">
                <text class="related-views">{{ formatViewCount(video.viewCount) }}次播放</text>
                <text class="related-time">{{ formatTime(video.createdTime) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

              <!-- 评论区 -->
        <view class="comments-section">
          <view class="section-title">
            <text class="title-text">评论 ({{ videoInfo.commentCount || 0 }})</text>
          </view>
          
          <!-- 评论输入区域 -->
          <view class="comment-input-area">
            <!-- 回复提示 -->
            <view v-if="replyTo" class="reply-hint">
              <text class="reply-hint-text">回复 {{ replyTo.userName }}</text>
              <view class="cancel-reply" @click="cancelReply">
                <text class="fas fa-times"></text>
              </view>
            </view>
            <view class="comment-input">
              <image 
                :src="currentUser?.avatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(currentUser?.nickname || '用户') + '&background=3b82f6&color=fff&size=64'" 
                class="user-avatar"
                mode="aspectFill"
              ></image>
              <input 
                type="text" 
                :placeholder="replyTo ? `回复 ${replyTo.userName}：` : '说点什么...'" 
                class="comment-text-input"
                v-model="commentText"
                @confirm="submitComment"
              />
              <view class="submit-btn" :class="{ 'active': commentText.trim().length > 0 }" @click="submitComment">
                <text class="fas fa-paper-plane"></text>
              </view>
            </view>
          </view>
          
          <!-- 评论列表 -->
          <view class="comments-list">
            <view 
              v-for="comment in comments" 
              :key="comment.id"
              class="comment-item"
            >
              <view class="comment-header">
                <image 
                  :src="comment.userAvatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(comment.userName || '匿名') + '&background=3b82f6&color=fff&size=100'" 
                  class="comment-avatar"
                  mode="aspectFill"
                ></image>
                <view class="comment-info">
                  <text class="comment-user">{{ comment.userName }}</text>
                  <text class="comment-time">{{ formatTime(comment.createdTime) }}</text>
                </view>
              </view>
              <text class="comment-text">{{ comment.content }}</text>
              <view class="comment-actions">
                <view class="action-btn" @click="toggleCommentLike(comment)">
                  <text class="fas fa-thumbs-up" :class="{ 'active': comment.isLiked }"></text>
                  <text class="count">{{ comment.likeCount || 0 }}</text>
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
                    <image :src="reply.userAvatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(reply.userName || '匿名') + '&background=3b82f6&color=fff&size=100'" class="avatar small"></image>
                    <view class="reply-info">
                      <text class="username">{{ reply.userName }}</text>
                      <text v-if="reply.replyToUsername" class="reply-to">回复 {{ reply.replyToUsername }}</text>
                      <text class="time">{{ formatTime(reply.createdTime) }}</text>
                    </view>
                  </view>
                  <text class="reply-content">{{ reply.content }}</text>
                  <view class="reply-actions">
                    <view class="action-btn small" @click="toggleCommentLike(reply)">
                      <text class="fas fa-thumbs-up" :class="{ 'active': reply.isLiked }"></text>
                      <text class="count">{{ reply.likeCount || 0 }}</text>
                    </view>
                    <view class="action-btn small" @click="replyComment(reply)">
                      <text class="fas fa-reply"></text>
                      <text class="action-text">回复</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
            
            <!-- 加载更多 -->
            <view class="load-more" @click="loadMoreComments" v-if="hasMoreComments">
              <text class="load-more-text">加载更多评论</text>
            </view>
          </view>
        </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { videoApi, commentApi, likeApi, userApi } from '../../../utils/api.js';

// 获取页面参数
const videoId = ref(null);

// 视频信息
const videoInfo = ref({});
const relatedVideos = ref([]);
const comments = ref([]);
const commentText = ref('');
const hasMoreComments = ref(true);
const commentPage = ref(1);

// 视频播放状态
const playStartTime = ref(null);
const totalWatchTime = ref(0);

// 当前用户信息
const currentUser = ref(null);

// 点赞状态
const isVideoLiked = ref(false);

// 回复相关
const replyTo = ref(null);

onMounted(async () => {
  console.log('视频播放页面初始化');
  
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  videoId.value = currentPage.options.id;
  
  if (videoId.value) {
    await loadCurrentUser(); // 先加载用户信息
    await loadVideoInfo();
    await loadRelatedVideos();
    await loadComments();
  } else {
    console.error('未获取到视频ID');
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    });
  }
});

// 加载当前用户信息
const loadCurrentUser = async () => {
  try {
    const response = await userApi.getCurrentUser();
    if (response && response.data) {
      currentUser.value = response.data;
      console.log('当前用户信息加载成功：', currentUser.value);
    }
  } catch (error) {
    console.error('加载当前用户信息失败：', error);
  }
};

onUnmounted(() => {
  // 页面销毁时记录观看时长
  if (playStartTime.value) {
    totalWatchTime.value += Date.now() - playStartTime.value;
  }
  console.log('总观看时长：', totalWatchTime.value / 1000, '秒');
});

// 加载视频信息
const loadVideoInfo = async () => {
  try {
    console.log('开始加载视频信息，视频ID：', videoId.value);
    const response = await videoApi.getVideoDetail(videoId.value);
    
    if (response.code === 200) {
      videoInfo.value = response.data;
      console.log('视频信息加载成功：', videoInfo.value);
      
      // 加载视频点赞状态
      await loadVideoLikeStatus();
      
      // 增加浏览量
      await videoApi.incrementVideoViewCount(videoId.value);
    } else {
      console.error('加载视频信息失败：', response.message);
      uni.showToast({
        title: '加载失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('加载视频信息异常：', error);
    uni.showToast({
      title: '网络错误',
      icon: 'none'
    });
  }
};

// 加载相关视频
const loadRelatedVideos = async () => {
  try {
    console.log('开始加载相关视频');
    const response = await videoApi.getRecommendedVideos(10);
    
    if (response.code === 200 && response.data) {
      // 过滤出视频内容并排除当前播放的视频
      const videos = response.data.filter(item => item.type === 2 && item.id != videoId.value);
      relatedVideos.value = videos.slice(0, 6);
      console.log('相关视频加载成功，共', relatedVideos.value.length, '个');
    }
  } catch (error) {
    console.error('加载相关视频异常：', error);
  }
};

// 加载视频点赞状态
const loadVideoLikeStatus = async () => {
  if (!currentUser.value || !currentUser.value.id || !videoId.value) return;
  
  try {
    console.log('开始加载视频点赞状态');
    const response = await likeApi.getLikeStatus(currentUser.value.id, videoId.value, 1);
    if (response && response.data !== undefined) {
      isVideoLiked.value = response.data;
      console.log('视频点赞状态加载成功：', isVideoLiked.value);
    }
  } catch (error) {
    console.error('加载视频点赞状态失败：', error);
  }
};

// 切换视频点赞状态
const toggleVideoLike = async () => {
  if (!currentUser.value || !currentUser.value.id || !videoId.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    return;
  }
  
  try {
    console.log('开始切换视频点赞状态，当前状态：', isVideoLiked.value);
    
    const response = await likeApi.toggleLike(currentUser.value.id, videoId.value, 1);
    
    if (response && response.data) {
      // 更新点赞状态和数量
      isVideoLiked.value = response.data.isLiked;
      videoInfo.value.likeCount = response.data.likeCount;
      
      console.log('视频点赞状态更新成功：', response.data);
      
      uni.showToast({
        title: isVideoLiked.value ? '已点赞' : '已取消点赞',
        icon: 'success'
      });
    }
  } catch (error) {
    console.error('更新视频点赞状态失败：', error);
    uni.showToast({
      title: '点赞失败，请重试',
      icon: 'none'
    });
  }
};

// 加载评论
const loadComments = async (page = 1) => {
  try {
    console.log('开始加载评论，页码：', page);
    const response = await commentApi.getContentComments(videoId.value, page, 10);
    
    if (response && response.data && response.data.records) {
      let commentsList = response.data.records || [];
      console.log('原始评论数据：', commentsList);
      
      // 为每个评论加载点赞状态并组织层级结构
      const commentMap = new Map();
      
      // 格式化评论数据并加载点赞状态
      if (currentUser.value && currentUser.value.id) {
        commentsList = await Promise.all(commentsList.map(async comment => {
          // 格式化评论数据
          const formattedComment = {
            id: comment.id,
            userName: comment.userNickname || '匿名用户',
            userAvatar: comment.userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(comment.userNickname || '匿名')}&background=3b82f6&color=fff&size=100`,
            content: comment.content,
            createdTime: comment.createdTime,
            likeCount: comment.likeCount || 0,
            parentId: comment.parentId || 0,
            rootId: comment.rootId || 0,
            replies: [] // 初始化回复数组
          };
          
          try {
            const likeResponse = await likeApi.getLikeStatus(currentUser.value.id, comment.id, 2);
            if (likeResponse && likeResponse.data !== undefined) {
              formattedComment.isLiked = likeResponse.data;
            } else {
              formattedComment.isLiked = false;
            }
          } catch (error) {
            console.error('获取评论点赞状态失败：', error);
            formattedComment.isLiked = false;
          }
          
          commentMap.set(formattedComment.id, formattedComment);
          return formattedComment;
        }));
      } else {
        commentsList = commentsList.map(comment => {
          const formattedComment = {
            id: comment.id,
            userName: comment.userNickname || '匿名用户',
            userAvatar: comment.userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(comment.userNickname || '匿名')}&background=3b82f6&color=fff&size=100`,
            content: comment.content,
            createdTime: comment.createdTime,
            likeCount: comment.likeCount || 0,
            parentId: comment.parentId || 0,
            rootId: comment.rootId || 0,
            isLiked: false,
            replies: [] // 初始化回复数组
          };
          
          commentMap.set(formattedComment.id, formattedComment);
          return formattedComment;
        });
      }
      
      // 组织评论层级结构
      const rootComments = [];
      commentsList.forEach(comment => {
        if (comment.parentId === 0) {
          // 根评论
          rootComments.push(comment);
        } else {
          // 回复评论 - 所有非根评论都放到根评论下
          let targetComment = null;
          
          // 如果有rootId，找到根评论
          if (comment.rootId && comment.rootId !== 0) {
            targetComment = commentMap.get(comment.rootId);
          }
          
          // 如果没找到根评论，找直接父评论
          if (!targetComment) {
            targetComment = commentMap.get(comment.parentId);
          }
          
          if (targetComment) {
            // 添加回复目标用户名（如果不是直接回复根评论）
            if (comment.parentId !== comment.rootId && comment.parentId !== 0) {
              const directParent = commentMap.get(comment.parentId);
              if (directParent) {
                comment.replyToUsername = directParent.userName;
              }
            }
            targetComment.replies.push(comment);
          } else {
            // 如果找不到父评论，作为根评论处理
            rootComments.push(comment);
          }
        }
      });
      
      if (page === 1) {
        comments.value = rootComments;
      } else {
        comments.value = [...comments.value, ...rootComments];
      }
      
      console.log('格式化后的评论数据：', rootComments);
      hasMoreComments.value = comments.value.length < (response.data.total || 0);
      console.log('评论加载成功，当前评论数：', comments.value.length);
    }
  } catch (error) {
    console.error('加载评论异常：', error);
  }
};

// 加载更多评论
const loadMoreComments = () => {
  commentPage.value++;
  loadComments(commentPage.value);
};

// 切换评论点赞状态
const toggleCommentLike = async (comment) => {
  if (!currentUser.value || !currentUser.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    return;
  }

  try {
    console.log('开始切换视频评论点赞状态，评论ID：', comment.id, '当前状态：', comment.isLiked);
    
    const response = await likeApi.toggleLike(currentUser.value.id, comment.id, 2); // 2表示评论点赞
    
    if (response && response.data) {
      // 更新点赞状态和数量
      comment.isLiked = response.data.isLiked;
      comment.likeCount = response.data.likeCount || 0;
      
      console.log('视频评论点赞状态更新成功：', response.data);
      
      uni.showToast({
        title: comment.isLiked ? '已点赞' : '已取消点赞',
        icon: 'success'
      });
    }
  } catch (error) {
    console.error('视频评论点赞失败：', error);
    uni.showToast({
      title: '点赞失败，请重试',
      icon: 'none'
    });
  }
};

// 回复评论
const replyComment = (comment) => {
  replyTo.value = comment;
  commentText.value = '';
  // 聚焦输入框
  uni.showToast({
    title: `回复 ${comment.userName}`,
    icon: 'none',
    duration: 1000
  });
};

// 取消回复
const cancelReply = () => {
  replyTo.value = null;
  commentText.value = '';
};

// 提交评论
const submitComment = async () => {
  if (!commentText.value.trim()) {
    uni.showToast({
      title: '请输入评论内容',
      icon: 'none'
    });
    return;
  }
  
  if (!currentUser.value || !currentUser.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    return;
  }
  
  try {
    console.log('提交评论：', commentText.value);
    const response = await commentApi.addComment({
      contentId: videoId.value,
      content: commentText.value.trim(),
      commentType: 1, // 普通评论
      parentId: replyTo.value ? replyTo.value.id : 0,
      rootId: replyTo.value ? (replyTo.value.rootId || replyTo.value.id) : 0
    });
    
    if (response && response.data) {
      uni.showToast({
        title: '评论成功',
        icon: 'success'
      });
      commentText.value = '';
      replyTo.value = null;
      // 重新加载评论
      commentPage.value = 1;
      loadComments(1);
      // 更新视频信息中的评论数
      if (videoInfo.value.commentCount !== undefined) {
        videoInfo.value.commentCount = (videoInfo.value.commentCount || 0) + 1;
      }
    } else {
      uni.showToast({
        title: '评论失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('提交评论异常：', error);
    uni.showToast({
      title: '网络错误',
      icon: 'none'
    });
  }
};

// 播放相关视频
const playRelatedVideo = (video) => {
  console.log('播放相关视频：', video.title);
  uni.redirectTo({
    url: `/pages/parent/video/video-player?id=${video.id}`
  });
};

// 视频播放事件
const onVideoPlay = () => {
  console.log('视频开始播放');
  playStartTime.value = Date.now();
};

const onVideoPause = () => {
  console.log('视频暂停播放');
  if (playStartTime.value) {
    totalWatchTime.value += Date.now() - playStartTime.value;
    playStartTime.value = null;
  }
};

const onVideoEnded = () => {
  console.log('视频播放结束');
  if (playStartTime.value) {
    totalWatchTime.value += Date.now() - playStartTime.value;
    playStartTime.value = null;
  }
};

const onVideoTimeUpdate = (e) => {
  // 可以在这里记录播放进度
};

// 返回按钮
const goBack = () => {
  // 获取当前页面栈
  const pages = getCurrentPages();
  
  if (pages.length > 1) {
    // 如果有上一页，直接返回
    uni.navigateBack({
      delta: 1,
      fail: (err) => {
        console.error('navigateBack失败:', err);
        // 如果navigateBack失败，尝试返回到视频页面
        uni.switchTab({
          url: '/pages/parent/video/video'
        });
      }
    });
  } else {
    // 如果没有上一页，返回到视频页面
    uni.switchTab({
      url: '/pages/parent/video/video',
      fail: (err) => {
        console.error('返回视频页面失败:', err);
        // 尝试切换到首页tab
        uni.switchTab({
          url: '/pages/parent/home/home',
          fail: (switchErr) => {
            console.error('切换到首页失败:', switchErr);
            // 提示用户
            uni.showToast({
              title: '返回失败，请重试',
              icon: 'none'
            });
          }
        });
      }
    });
  }
};

// 分享视频
const shareVideo = () => {
  uni.showActionSheet({
    itemList: ['分享到微信', '分享到朋友圈', '复制链接'],
    success: (res) => {
      console.log('选择分享方式：', res.tapIndex);
      uni.showToast({
        title: '分享功能开发中',
        icon: 'none'
      });
    }
  });
};

// 工具函数
const formatViewCount = (count) => {
  if (!count) return '0';
  if (count < 1000) return count.toString();
  if (count < 10000) return (count / 1000).toFixed(1) + 'K';
  return (count / 10000).toFixed(1) + 'W';
};

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  if (diff < 2592000000) return Math.floor(diff / 86400000) + '天前';
  
  return date.toLocaleDateString();
};

const formatDuration = (seconds) => {
  if (!seconds) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

const getTagList = (tags) => {
  if (!tags) return [];
  return tags.split(',').filter(tag => tag.trim());
};
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
  display: flex;
  flex-direction: column;
}

/* 状态栏 */
.status-bar {
  height: var(--status-bar-height);
  background-color: #3b82f6;
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #3b82f6;
  position: relative;
  z-index: 10;
}

.header-left, .header-right {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon, .share-icon {
  color: #ffffff;
  font-size: 18px;
}

.header-center {
  flex: 1;
  text-align: center;
}

.header-title {
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
}

/* 视频播放器 */
.video-container {
  width: 100%;
  height: 220px;
  background-color: #000000;
  flex-shrink: 0;
}

.video-player {
  width: 100%;
  height: 100%;
}

/* 内容区域 */
.content-area {
  background-color: #ffffff;
  border-radius: 12px 12px 0 0;
  margin-top: -12px;
  position: relative;
  z-index: 5;
  flex: 1;
  height: calc(100vh - 280px);
}

/* 视频信息 */
.video-info {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.video-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 12px;
}

.video-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-item .fas {
  color: #6b7280;
  font-size: 14px;
  transition: color 0.3s ease;
}

.stat-item.like-btn {
  cursor: pointer;
}

.stat-item .fas.liked {
  color: #ef4444;
}

.stat-text {
  color: #6b7280;
  font-size: 14px;
}

.video-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.video-time {
  color: #9ca3af;
  font-size: 12px;
}

.video-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  background-color: #3b82f6;
  color: #ffffff;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

/* 视频描述 */
.video-description {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.description-text {
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

/* 创作者信息 */
.creator-info {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.creator-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
}

.creator-avatar image {
  width: 100%;
  height: 100%;
}

.creator-details {
  flex: 1;
}

.creator-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  display: block;
}

.creator-desc {
  font-size: 12px;
  color: #6b7280;
  display: block;
  margin-top: 2px;
}

.follow-btn {
  background-color: #3b82f6;
  padding: 6px 16px;
  border-radius: 16px;
}

.follow-text {
  color: #ffffff;
  font-size: 12px;
}

/* 相关推荐 */
.related-videos {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #3b82f6;
  font-size: 14px;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  display: flex;
  gap: 12px;
}

.related-cover {
  position: relative;
  width: 140px;
  height: 84px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.related-cover image {
  width: 100%;
  height: 100%;
}

.video-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  padding: 1px 4px;
  border-radius: 2px;
  font-size: 10px;
}

.related-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.related-title {
  font-size: 14px;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-meta {
  display: flex;
  gap: 8px;
}

.related-views, .related-time {
  color: #6b7280;
  font-size: 12px;
}

/* 评论区 */
.comments-section {
  padding: 12px;
}

.comment-input-area {
  margin-bottom: 16px;
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

.comment-input {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.comment-text-input {
  flex: 1;
  padding: 8px 12px;
  background-color: #ffffff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  font-size: 14px;
}

.submit-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e7eb;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.submit-btn.active {
  background-color: #3b82f6;
}

.submit-btn .fas {
  color: #6b7280;
  font-size: 14px;
  transition: color 0.2s ease;
}

.submit-btn.active .fas {
  color: #ffffff;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
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

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
}

.comment-info {
  flex: 1;
}

.comment-user {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
  display: block;
}

.comment-time {
  font-size: 12px;
  color: #6b7280;
}

.comment-text {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
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
  color: #4b5563;
  transition: color 0.3s ease;
}

.action-btn .fas.active {
  color: #3b82f6;
}

.count, .action-text {
  font-size: 12px;
  color: #6b7280;
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

.load-more {
  text-align: center;
  padding: 16px;
}

.load-more-text {
  color: #3b82f6;
  font-size: 14px;
}
</style> 
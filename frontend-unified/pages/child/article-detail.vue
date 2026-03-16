<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
      <view class="header-left">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
          <text class="header-title">{{ article.title }}</text>
      </view>
      <view class="header-right">
        <view class="action-btn reading-settings-btn" @click="goToReadingSettings">
          <text class="settings-text">A</text>
        </view>
          <view class="action-btn" @click="shareArticle">
        <text class="fas fa-share-alt"></text>
      </view>
          <view class="action-btn" @click="toggleBookmark">
            <text class="fas fa-bookmark" :class="{ 'active': isBookmarked }"></text>
    </view>
        </view>
      </view>
      </view>

    <!-- 阅读内容区域 -->
    <view class="reading-container">
      <!-- 滚动阅读模式 -->
      <scroll-view 
        v-if="!pageAnimationEnabled"
        scroll-y="true" 
        class="content-scroll"
        :show-scrollbar="false"
        @scroll="onScroll"
      >
        <view class="article-content">
          <!-- 文章标题 -->
          <view class="article-header">
            <text class="article-title">{{ article.title }}</text>
            <view class="article-meta">
              <text class="article-author">{{ article.author }}</text>
              <text class="article-date">{{ article.date }}</text>
            </view>
            <view class="article-stats">
              <view class="stat-item">
                <text class="fas fa-eye"></text>
                <text>{{ article.views }}</text>
              </view>
              <view class="stat-item">
                <text class="fas fa-thumbs-up"></text>
                <text>{{ article.likes }}</text>
              </view>
              <view class="stat-item">
                <text class="fas fa-comment"></text>
                <text>{{ article.comments }}</text>
              </view>
            </view>
          </view>

          <!-- 文章内容 -->
          <view class="article-body">
            <!-- 加载状态 -->
            <view v-if="loading" class="loading-content">
              <text class="fas fa-spinner fa-spin"></text>
              <text class="loading-text">加载中...</text>
            </view>

            <!-- 错误状态 -->
            <view v-else-if="error" class="error-content">
              <text class="error-text">{{ error }}</text>
              <view class="retry-btn" @click="loadArticleData">
                <text>重试</text>
              </view>
            </view>

            <!-- 文章内容 -->
            <view v-else-if="article.content" class="content-wrapper">
              <!-- 封面图 -->
              <image v-if="article.cover" :src="article.cover" mode="widthFix" class="cover-image"></image>
              
              <!-- 渲染解析后的段落内容 -->
              <view v-for="(paragraph, index) in parsedParagraphs" :key="index" 
                    :class="['paragraph-container', { 'is-image': paragraph.isImage, 'has-comments': paragraph.commentCount > 0 }]">
                <view class="paragraph-content" 
                      @click="paragraph.isImage ? onImageClick(paragraph) : null">
                  <rich-text 
                    :nodes="paragraph.content" 
                    class="paragraph-rich-text"
                  ></rich-text>
                </view>
                
                <!-- 段落评论按钮（只显示给非图片段落） -->
                <view v-if="paragraph.id && !paragraph.isImage" class="paragraph-actions">
                  <view class="comment-btn" @click="openParagraphComment(paragraph)">
                    <text class="fas fa-comment"></text>
                    <text class="comment-count">{{ paragraph.commentCount || 0 }}</text>
                    <text v-if="paragraph.commentCount === 0" class="comment-hint">评论</text>
                  </view>
                </view>
              </view>
            </view>
              
            <!-- 无内容提示 -->
            <view v-else class="no-content">
              <text class="no-content-text">暂无内容</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- 翻页阅读模式 -->
      <view v-else class="page-reading-container" 
            @touchstart="onTouchStart" 
            @touchmove="onTouchMove" 
            @touchend="onTouchEnd">
        <!-- 当前页 -->
        <view class="page-wrapper current-page" 
              :class="{ 'turning-next': isTurningNext, 'turning-prev': isTurningPrev }">
          <view class="page-content">
            <view class="article-content page-article">
              <view class="article-header" v-if="currentPage === 0">
                <text class="article-title">{{ article.title }}</text>
                <view class="article-meta">
                  <text class="article-author">{{ article.author }}</text>
                  <text class="article-date">{{ article.date }}</text>
                </view>
                <view class="article-stats">
                  <view class="stat-item">
                    <text class="fas fa-eye"></text>
                    <text>{{ article.views }}</text>
                  </view>
                  <view class="stat-item">
                    <text class="fas fa-thumbs-up"></text>
                    <text>{{ article.likes }}</text>
                  </view>
                  <view class="stat-item">
                    <text class="fas fa-comment"></text>
                    <text>{{ article.comments }}</text>
                  </view>
                </view>
              </view>

              <view class="article-body">
                <view v-if="loading" class="loading-content">
                  <text class="fas fa-spinner fa-spin"></text>
                  <text class="loading-text">加载中...</text>
                </view>

                <view v-else-if="error" class="error-content">
                  <text class="error-text">{{ error }}</text>
                  <view class="retry-btn" @click="loadArticleData">
                    <text>重试</text>
                  </view>
                </view>

                <view v-else-if="article.content" class="content-wrapper">
                  <image v-if="article.cover && currentPage === 0" :src="article.cover" mode="widthFix" class="cover-image"></image>
                  
                  <view v-for="(paragraph, index) in currentPageParagraphs" :key="index" 
                        :class="['paragraph-container', { 'is-image': paragraph.isImage, 'has-comments': paragraph.commentCount > 0 }]">
                    <view class="paragraph-content" 
                          @click="paragraph.isImage ? onImageClick(paragraph) : null">
                      <rich-text 
                        :nodes="paragraph.content" 
                        class="paragraph-rich-text"
                      ></rich-text>
                    </view>
                    
                    <view v-if="paragraph.id && !paragraph.isImage" class="paragraph-actions">
                      <view class="comment-btn" @click="openParagraphComment(paragraph)">
                        <text class="fas fa-comment"></text>
                        <text class="comment-count">{{ paragraph.commentCount || 0 }}</text>
                        <text v-if="paragraph.commentCount === 0" class="comment-hint">评论</text>
                      </view>
                    </view>
                  </view>
                </view>
                
                <view v-else class="no-content">
                  <text class="no-content-text">暂无内容</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 下一页预览 -->
        <view class="page-wrapper next-page-preview" 
              v-if="isTurningNext && currentPage < totalPages - 1">
          <view class="page-content">
            <view class="article-content page-article">
              <view class="article-body">
                <view class="content-wrapper">
                  <view v-for="(paragraph, index) in nextPageParagraphs" :key="index" 
                        :class="['paragraph-container', { 'is-image': paragraph.isImage, 'has-comments': paragraph.commentCount > 0 }]">
                    <view class="paragraph-content">
                      <rich-text :nodes="paragraph.content" class="paragraph-rich-text"></rich-text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 上一页预览 -->
        <view class="page-wrapper prev-page-preview" 
              v-if="isTurningPrev && currentPage > 0">
          <view class="page-content">
            <view class="article-content page-article">
              <view class="article-header" v-if="currentPage - 1 === 0">
                 <text class="article-title">{{ article.title }}</text>
                 <view class="article-meta">
                   <text class="article-author">{{ article.author }}</text>
                   <text class="article-date">{{ article.date }}</text>
                 </view>
              </view>
              <view class="article-body">
                <view class="content-wrapper">
                  <image v-if="article.cover && currentPage - 1 === 0" :src="article.cover" mode="widthFix" class="cover-image"></image>
                  <view v-for="(paragraph, index) in prevPageParagraphs" :key="index" 
                        :class="['paragraph-container', { 'is-image': paragraph.isImage, 'has-comments': paragraph.commentCount > 0 }]">
                    <view class="paragraph-content">
                      <rich-text :nodes="paragraph.content" class="paragraph-rich-text"></rich-text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 页码指示器 -->
        <view class="page-indicator">
          <text class="page-number">{{ currentPage + 1 }} / {{ totalPages }}</text>
        </view>
      </view>
    </view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bar-left">
        <text class="progress-text">阅读进度: {{ readProgress }}%</text>
        </view>
      <view class="bar-right">
        <view class="bar-item" @tap="toggleLike">
          <text class="fas fa-thumbs-up" :class="{ active: isLiked }"></text>
          <text class="bar-text">{{ article.likes }}</text>
        </view>
        <view class="bar-item" @tap="goToComments">
          <text class="fas fa-comment"></text>
          <text class="bar-text">{{ article.comments }}</text>
        </view>
      </view>
    </view>

    <!-- 评论弹窗 -->
    <view v-if="showCommentPopup" class="comment-popup-mask" @click="closeCommentPopup">
      <view class="comment-popup" @click.stop>
        <!-- 弹窗标题栏 -->
        <view class="popup-header">
          <text class="popup-title">{{ currentParagraph ? '段落评论' : '文章评论' }}</text>
          <view class="close-btn" @click="closeCommentPopup">
            <text class="fas fa-times"></text>
          </view>
        </view>

        <!-- 原段落内容（仅段落评论时显示） -->
        <view class="original-paragraph" v-if="currentParagraph">
          <text class="paragraph-text">{{ currentParagraph.text }}</text>
        </view>

        <!-- 评论列表 -->
        <scroll-view scroll-y="true" class="comments-list">
          <!-- 加载状态 -->
          <view v-if="loadingComments" class="loading-comments">
            <text class="fas fa-spinner fa-spin"></text>
            <text class="loading-text">加载中...</text>
          </view>

          <!-- 评论内容 -->
          <view v-else-if="paragraphComments.length > 0">
            <view v-for="comment in paragraphComments" :key="comment.id" class="comment-item">
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
              :placeholder="replyTo ? `回复 ${replyTo.username}：` : (currentParagraph ? '评论这段内容...' : '写下你的评论...')" 
              class="input-field"
            />
            <view class="send-btn" :class="{ 'active': newComment.trim().length > 0 }" @click="submitParagraphComment">
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
import { ref, computed, nextTick } from 'vue'
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { contentApi, likeApi, userApi, commentApi, favoriteApi, userBehaviorApi } from '@/utils/api.js'
import readingModeManager from '@/utils/readingModeManager.js'
import eventBus, { EVENTS } from '@/utils/eventBus.js'

// 文章信息
const article = ref({
  id: null,
  title: '加载中...',
  author: '',
  date: '',
  views: 0,
  likes: 0,
  comments: 0,
  cover: '',
  content: '' // 改为content以统一处理
})

// 页面参数
const contentId = ref(null)
const loading = ref(false)
const error = ref('')

// 阅读计时相关
const readingTimer = ref(null)
const currentReadingDuration = ref(0) // 本次阅读时长（秒）

// 解析后的段落数据
const parsedParagraphs = ref([])
const paragraphCommentCounts = ref({})

// 用户信息
const currentUser = ref(null)
const isLiked = ref(false)
const isBookmarked = ref(false)
const readProgress = ref(0)
const scrollTop = ref(0)

// 翻页模式相关
const pageAnimationEnabled = ref(false) // 是否启用翻页动画
const currentPage = ref(0) // 当前页码
const totalPages = ref(1) // 总页数
const isTurningNext = ref(false) // 是否正在向下翻页
const isTurningPrev = ref(false) // 是否正在向上翻页
const paragraphsPerPage = 5 // 每页显示的段落数

// 触摸滑动相关
const touchStartX = ref(0)
const touchStartY = ref(0)
const touchTranslateX = ref(0)
const isSwiping = ref(false)

// 当前页的段落
const currentPageParagraphs = computed(() => {
  if (!pageAnimationEnabled.value) {
    return parsedParagraphs.value
  }
  
  if (parsedParagraphs.value.length === 0) {
    return []
  }
  
  const start = currentPage.value * paragraphsPerPage
  const end = start + paragraphsPerPage
  return parsedParagraphs.value.slice(start, end)
})

// 下一页的段落（用于预览）
const nextPageParagraphs = computed(() => {
  if (!pageAnimationEnabled.value || parsedParagraphs.value.length === 0) {
    return []
  }
  
  const start = (currentPage.value + 1) * paragraphsPerPage
  const end = start + paragraphsPerPage
  return parsedParagraphs.value.slice(start, end)
})

// 上一页的段落（用于预览）
const prevPageParagraphs = computed(() => {
  if (!pageAnimationEnabled.value || parsedParagraphs.value.length === 0) {
    return []
  }
  
  const start = (currentPage.value - 1) * paragraphsPerPage
  const end = start + paragraphsPerPage
  return parsedParagraphs.value.slice(start, end)
})

// 段落评论弹窗相关
const showCommentPopup = ref(false)
const currentParagraph = ref(null)
const paragraphComments = ref([])
const loadingComments = ref(false)
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

// 页面加载
onLoad(async (option) => {
  console.log('文章详情页面参数：', option)
  
  // 应用阅读模式设置
  applyReadingModeSettings()
  
  const articleId = option.id || option.contentId
  if (articleId) {
    contentId.value = parseInt(articleId)
    await loadCurrentUser()
    await loadArticleData()
  } else {
    error.value = '参数错误'
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
  }
})

// 页面显示时重新应用阅读模式设置
onShow(() => {
  console.log('[文章详情页面] 页面显示，重新应用阅读模式设置')
  applyReadingModeSettings()
  startReadingTimer()
})

onHide(() => {
  stopReadingTimer()
})

onUnload(() => {
  stopReadingTimer()
})

// 阅读计时器逻辑
const startReadingTimer = () => {
  if (readingTimer.value) return
  
  console.log('开始阅读计时')
  readingTimer.value = setInterval(() => {
    currentReadingDuration.value += 60
    const minutes = Math.floor(currentReadingDuration.value / 60)
    console.log(`用户已阅读 ${minutes} 分钟`)
    
    // 同步记录到后端
    if (currentUser.value && currentUser.value.id && article.value && article.value.id) {
      userBehaviorApi.recordReadingBehavior(
        currentUser.value.id, 
        article.value.id, 
        60, // 每次增加60秒
        readProgress.value
      ).catch(err => {
        console.error('记录阅读行为失败:', err)
      })
    }
  }, 60000) // 每分钟执行一次
}

const stopReadingTimer = () => {
  if (readingTimer.value) {
    console.log('停止阅读计时，本次共阅读:', currentReadingDuration.value, '秒')
    clearInterval(readingTimer.value)
    readingTimer.value = null
  }
}

// 应用阅读模式设置
const applyReadingModeSettings = () => {
  try {
    const settings = readingModeManager.loadSettings()
    console.log('[儿童端文章详情] 应用阅读模式设置：', settings)
    
    // 应用翻页动画设置
    pageAnimationEnabled.value = settings.pageAnimation !== undefined ? settings.pageAnimation : true
    console.log('[儿童端文章详情] 翻页动画状态:', pageAnimationEnabled.value)
    
    // 如果启用了翻页动画，需要重新计算总页数
    if (pageAnimationEnabled.value && parsedParagraphs.value.length > 0) {
      calculateTotalPages()
    }
    
    readingModeManager.applySettings(settings)
  } catch (error) {
    console.error('[儿童端文章详情] 应用阅读模式设置失败：', error)
  }
}

// 加载当前用户信息
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

// 加载文章数据
const loadArticleData = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await contentApi.getContentDetail(contentId.value)
    if (response && response.data) {
      const data = response.data
      article.value = {
        id: data.id,
        title: data.title || '无标题',
        author: data.creatorName || '匿名作者',
        date: formatDate(data.createdTime),
        views: data.viewCount || 0,
        likes: data.likeCount || 0,
        comments: data.commentCount || 0,
        cover: data.coverImage,
        content: data.content || data.htmlContent || ''
      }
      
      console.log('文章数据加载成功：', article.value)
      
      // 加载点赞状态和收藏状态
      if (currentUser.value && currentUser.value.id) {
        await loadLikeStatus()
        await loadBookmarkStatus()
      }
      
      // 解析文章内容
      await parseArticleContent()
      
      // 加载段落评论数量
      await loadParagraphCommentCounts()
    }
  } catch (err) {
    console.error('加载文章失败：', err)
    error.value = '加载失败，请重试'
      } finally {
    loading.value = false
  }
}

// 加载点赞状态
const loadLikeStatus = async () => {
  try {
    const response = await likeApi.getLikeStatus(currentUser.value.id, contentId.value, 1)
    if (response && response.data !== undefined) {
      isLiked.value = response.data
    }
  } catch (err) {
    console.error('加载点赞状态失败：', err)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch (err) {
    return ''
  }
}

// 返回上一页
const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack({
      delta: 1
    })
      } else {
    uni.reLaunch({
      url: '/pages/child/home'
    })
  }
}

// 切换点赞
const toggleLike = async () => {
  if (!currentUser.value || !currentUser.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    const response = await likeApi.toggleLike(currentUser.value.id, contentId.value, 1)
    if (response && response.data) {
      isLiked.value = response.data.isLiked
      article.value.likes = response.data.likeCount || 0
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

// 切换收藏
// 加载收藏状态
const loadBookmarkStatus = async () => {
  if (!currentUser.value || !currentUser.value.id || !article.value.id) return
  
  try {
    console.log('[儿童端文章详情] 开始加载收藏状态')
    const response = await favoriteApi.getFavoriteStatus(currentUser.value.id, article.value.id)
    if (response && response.data !== undefined) {
      isBookmarked.value = response.data
      console.log('[儿童端文章详情] 收藏状态加载成功：', isBookmarked.value)
    }
  } catch (error) {
    console.error('[儿童端文章详情] 加载收藏状态失败：', error)
  }
}

// 切换收藏状态
const toggleBookmark = async () => {
  if (!currentUser.value || !currentUser.value.id || !article.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('[儿童端文章详情] 开始切换收藏状态，当前状态：', isBookmarked.value)
    
    // 调用收藏/取消收藏接口
    const response = await favoriteApi.toggleFavorite(currentUser.value.id, article.value.id)
    
    if (response && response.data !== undefined) {
      // 更新收藏状态
      isBookmarked.value = response.data
      
      console.log('[儿童端文章详情] 收藏状态更新成功：', isBookmarked.value)
      
      uni.showToast({
    title: isBookmarked.value ? '已收藏' : '已取消收藏',
        icon: 'success'
      })
      
      // 触发事件通知收藏页面更新
      eventBus.emit(EVENTS.COLLECTION_UPDATED, {
        contentId: article.value.id,
        isBookmarked: isBookmarked.value,
        articleInfo: {
          id: article.value.id,
          title: article.value.title,
          author: article.value.author,
          cover: article.value.cover
        }
      })
      console.log('[儿童端文章详情] 已触发收藏更新事件')
    }
  } catch (error) {
    console.error('[儿童端文章详情] 更新收藏状态失败：', error)
    uni.showToast({
      title: '收藏失败，请重试',
      icon: 'none'
    })
  }
}

// 分享文章
const shareArticle = async () => {
  console.log('[儿童端] 开始分享文章：', article.value.title)
  
  if (!article.value.id) {
  uni.showToast({
      title: '文章数据未加载',
    icon: 'none'
  })
    return
  }
  
  // 记录分享行为到数据库
  if (currentUser.value?.id) {
    try {
      await userBehaviorApi.recordShareBehavior(currentUser.value.id, article.value.id)
      console.log('[儿童端] 分享行为记录成功')
    } catch (error) {
      console.error('[儿童端] 记录分享行为失败：', error)
      // 不影响分享功能，只是统计可能不准确
    }
  }
  
  // 构建分享内容
  const shareTitle = article.value.title || '精彩文章'
  const shareContent = `推荐阅读：《${shareTitle}》`
  
  // 检测平台
  const systemInfo = uni.getSystemInfoSync()
  console.log('[儿童端] 当前平台：', systemInfo.platform)
  
  // H5环境下使用系统分享或显示分享选项
  // #ifdef H5
  if (navigator.share) {
    console.log('[儿童端] 使用Web Share API')
    navigator.share({
      title: shareTitle,
      text: shareContent,
      url: window.location.href
    }).then(() => {
      console.log('[儿童端] 分享成功')
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    }).catch((error) => {
      console.error('[儿童端] 分享失败：', error)
      // 如果用户取消分享，不显示错误提示
      if (error.name !== 'AbortError') {
        showShareActionSheet()
      }
    })
  } else {
    console.log('[儿童端] 不支持Web Share API，显示分享选项')
    showShareActionSheet()
  }
  // #endif
  
  // 小程序环境下使用小程序分享
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
  
  // App环境下优先使用系统分享
  // #ifdef APP-PLUS
  console.log('[儿童端] App环境，尝试使用系统分享')
  
  // 优先使用uni.share()原生分享
  uni.share({
    provider: 'system', // 使用系统分享
    type: 1, // 图文分享
    title: shareTitle,
    summary: `来自阅桥亲子阅读APP：${shareTitle}`,
    href: `https://parentreading.com/article/${article.value.id}`,
    success: (res) => {
      console.log('[儿童端] 系统分享成功：', res)
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('[儿童端] 系统分享失败，降级使用操作菜单：', err)
      // 如果系统分享不可用，降级使用操作菜单
      showShareActionSheet()
    }
  })
  // #endif
}

// 显示分享操作菜单
const showShareActionSheet = () => {
  console.log('[儿童端] 显示分享操作菜单')
  
  // #ifdef APP-PLUS
  // App环境下提供更多分享选项
  uni.showActionSheet({
    itemList: ['复制链接', '使用系统分享', '生成分享海报'],
    success: (res) => {
      console.log('[儿童端] 选择了分享方式，索引：', res.tapIndex)
      
      switch (res.tapIndex) {
        case 0:
          // 复制链接
          copyArticleLink()
          break
        case 1:
          // 使用系统分享
          useSystemShare()
          break
        case 2:
          // 生成分享海报
          generateSharePoster()
          break
      }
    },
    fail: (err) => {
      console.error('[儿童端] 显示分享菜单失败：', err)
    }
  })
  // #endif
  
  // #ifndef APP-PLUS
  // 非App环境下的分享选项
  uni.showActionSheet({
    itemList: ['复制链接', '生成分享海报'],
    success: (res) => {
      console.log('[儿童端] 选择了分享方式，索引：', res.tapIndex)
      
      switch (res.tapIndex) {
        case 0:
          copyArticleLink()
          break
        case 1:
          generateSharePoster()
          break
      }
    },
    fail: (err) => {
      console.error('[儿童端] 显示分享菜单失败：', err)
    }
  })
  // #endif
}

// 使用系统分享（App环境）
const useSystemShare = async () => {
  console.log('[儿童端] 使用系统分享')
  
  // 记录分享行为到数据库
  if (currentUser.value?.id) {
    try {
      await userBehaviorApi.recordShareBehavior(currentUser.value.id, article.value.id)
      console.log('[儿童端] 分享行为记录成功')
    } catch (error) {
      console.error('[儿童端] 记录分享行为失败：', error)
    }
  }
  
  // #ifdef APP-PLUS
  const shareTitle = article.value.title || '精彩文章'
  
  uni.share({
    provider: 'system',
    type: 1,
    title: shareTitle,
    summary: `来自阅桥亲子阅读APP：${shareTitle}`,
    href: `https://parentreading.com/article/${article.value.id}`,
    success: (res) => {
      console.log('[儿童端] 系统分享成功：', res)
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('[儿童端] 系统分享失败：', err)
      uni.showToast({
        title: '分享失败，请稍后重试',
        icon: 'none'
      })
    }
  })
  // #endif
}

// 复制文章链接
const copyArticleLink = async () => {
  console.log('[儿童端] 复制文章链接')
  
  // 记录分享行为到数据库
  if (currentUser.value?.id) {
    try {
      await userBehaviorApi.recordShareBehavior(currentUser.value.id, article.value.id)
      console.log('[儿童端] 分享行为记录成功')
    } catch (error) {
      console.error('[儿童端] 记录分享行为失败：', error)
    }
  }
  
  // 构建文章链接
  let articleLink = ''
  
  // #ifdef H5
  articleLink = window.location.href
  // #endif
  
  // #ifndef H5
  // 在非H5环境下，构建一个模拟链接
  articleLink = `https://parentreading.com/article/${article.value.id}`
  // #endif
  
  const shareText = `${article.value.title}\n\n${articleLink}`
  
  uni.setClipboardData({
    data: shareText,
    success: () => {
      console.log('[儿童端] 链接复制成功')
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('[儿童端] 复制失败：', err)
      uni.showToast({
        title: '复制失败',
        icon: 'none'
      })
    }
  })
}

// 生成分享海报
const generateSharePoster = () => {
  console.log('[儿童端] 生成分享海报')
  uni.showToast({
    title: '海报生成功能开发中',
    icon: 'none'
  })
  // TODO: 实现海报生成功能
  // 可以使用canvas生成包含文章标题、封面、二维码等信息的海报
}

// 打开文章评论弹窗
const goToComments = async () => {
  currentParagraph.value = null // 设置为 null 表示是文章评论
  showCommentPopup.value = true
  
  // 加载文章评论
  await loadComments()
}

// 跳转到阅读设置页面
const goToReadingSettings = () => {
  console.log('跳转到儿童端阅读模式设置页面')
  // 传递来源和文章ID，以便返回时能正确跳转
  const url = `/pages/child/profile-detail/settings-reading-mode?from=article&id=${contentId.value}`
  uni.navigateTo({
    url: url,
    success: () => {
      console.log('跳转到阅读模式设置成功')
    },
    fail: (error) => {
      console.error('跳转到阅读模式设置失败:', error)
      uni.showToast({
        title: '跳转失败，请重试',
        icon: 'none'
      })
    }
  })
}

// 解析文章内容为段落
const parseArticleContent = async () => {
  try {
    console.log('开始解析文章内容')
    
    if (!article.value.content) {
      parsedParagraphs.value = []
      return
    }
    
    const paragraphs = []
    let paragraphIndex = 0
    const htmlContent = article.value.content
    
    // 匹配段落标签和图片标签
    const elementRegex = /<(p|h1|h2|h3|h4|h5|h6|blockquote|pre|div|section|article|img)[^>]*>(?:(.*?)<\/\1>)?/gi
    
    let match
    while ((match = elementRegex.exec(htmlContent)) !== null) {
      const [fullMatch, tagName, innerContent = ''] = match
      
      // 如果是图片标签，单独处理
      if (tagName.toLowerCase() === 'img') {
        // 为图片统一样式
        const processedImgTag = fullMatch.replace(
          /(<img[^>]*)(style="[^"]*")?([^>]*>)/i, 
          '$1 style="width: 100%; height: auto; max-width: 600px; border-radius: 8px; margin: 16px 0; display: block;"$3'
        )
        
        paragraphs.push({
          id: null, // 图片不需要评论功能
          content: processedImgTag,
          text: '',
          tagName: 'img',
          commentCount: 0,
          isImage: true
        })
      } else {
        // 处理文本段落
        // 提取id属性
        const idMatch = fullMatch.match(/id=["']([^"']+)["']/i)
        const paragraphId = idMatch ? idMatch[1] : `para-temp-${paragraphIndex++}`
        
        // 提取纯文本内容
        let textContent = innerContent.replace(/<[^>]*>/g, '').trim()
        textContent = textContent.replace(/&nbsp;/gi, ' ').replace(/&\w+;/g, ' ').trim()
        
        // 检查是否只包含空白字符
        const isOnlyWhitespace = !textContent || /^[\s\u00A0\u3000]+$/.test(textContent)
        
        // 如果段落有实际文本内容才添加评论功能
        if (textContent && textContent.length > 5 && !isOnlyWhitespace) {
          paragraphs.push({
            id: paragraphId,
            content: fullMatch,
            text: textContent,
            tagName: tagName.toLowerCase(),
            commentCount: 0,
            isImage: false
          })
        } else if (fullMatch.includes('<img')) {
          // 如果段落包含图片但没有文本，也要保留但不添加评论功能
          paragraphs.push({
            id: null,
            content: fullMatch,
            text: '',
            tagName: tagName.toLowerCase(),
            commentCount: 0,
            isImage: true
          })
        }
      }
    }
    
    // 如果没有解析到任何内容，使用整个内容
    if (paragraphs.length === 0) {
      // 处理整体内容中的图片样式
      const processedContent = htmlContent.replace(
        /<img([^>]*?)(?:style="[^"]*")?([^>]*?)>/gi,
        '<img$1 style="width: 100%; height: auto; max-width: 600px; border-radius: 8px; margin: 16px 0; display: block;"$2>'
      )
      
      // 提取文本内容
      let textContent = htmlContent.replace(/<[^>]*>/g, '').trim()
      textContent = textContent.replace(/&nbsp;/gi, ' ').replace(/&\w+;/g, ' ').trim()
      
      const isOnlyWhitespace = !textContent || /^[\s\u00A0\u3000]+$/.test(textContent)
      const hasValidText = textContent && textContent.length > 5 && !isOnlyWhitespace
      
      paragraphs.push({
        id: hasValidText ? 'para-fallback-0' : null,
        content: processedContent,
        text: textContent,
        tagName: 'div',
        commentCount: 0,
        isImage: !hasValidText
      })
    }
    
    parsedParagraphs.value = paragraphs
    console.log('文章内容解析完成，段落数量：', paragraphs.length)
    
    // 计算总页数（如果开启了翻页动画）
    if (pageAnimationEnabled.value) {
      calculateTotalPages()
    }
    
    await nextTick()
    uni.createSelectorQuery().select('.content-scroll').boundingClientRect((scrollData) => {
      if (scrollData) {
        uni.createSelectorQuery().select('.article-content').boundingClientRect((contentData) => {
          if (contentData) {
            const totalHeight = contentData.height
            const visibleHeight = scrollData.height
            calculateProgress(scrollTop.value || 0, totalHeight, visibleHeight)
          }
        }).exec()
      }
    }).exec()
    
  } catch (error) {
    console.error('解析文章内容失败：', error)
    // 降级处理：使用原始内容
    const processedContent = article.value.content.replace(
      /<img([^>]*?)(?:style="[^"]*")?([^>]*?)>/gi,
      '<img$1 style="width: 100%; height: auto; max-width: 600px; border-radius: 8px; margin: 16px 0; display: block;"$2>'
    )
    
    parsedParagraphs.value = [{
      id: 'para-fallback-0',
      content: processedContent,
      text: '文章内容',
      tagName: 'div',
      commentCount: 0,
      isImage: false
    }]
  }
}

// 加载段落评论数量
const loadParagraphCommentCounts = async () => {
  try {
    console.log('开始加载段落评论数量')
    
    if (!article.value.id) return
    
    const response = await commentApi.getParagraphCommentCounts(article.value.id)
    
    if (response && response.data) {
      paragraphCommentCounts.value = response.data
      
      // 更新段落的评论数量
      parsedParagraphs.value.forEach(paragraph => {
        if (paragraph.id && paragraphCommentCounts.value[paragraph.id]) {
          paragraph.commentCount = paragraphCommentCounts.value[paragraph.id]
        }
      })
      
      console.log('段落评论数量加载完成：', paragraphCommentCounts.value)
    }
  } catch (error) {
    console.error('加载段落评论数量失败：', error)
  }
}

// 打开段落评论弹窗
const openParagraphComment = async (paragraph) => {
  if (!paragraph.id) return
  
  currentParagraph.value = paragraph
  showCommentPopup.value = true
  
  // 加载该段落的评论
  await loadComments(paragraph.id)
}

// 关闭评论弹窗
const closeCommentPopup = () => {
  showCommentPopup.value = false
  showEmojiPicker.value = false
  newComment.value = ''
  replyTo.value = null
  currentParagraph.value = null
  paragraphComments.value = []
}

// 加载评论（段落评论或文章评论）
const loadComments = async (paragraphId = null) => {
  if (!contentId.value) return
  
  try {
    loadingComments.value = true
    
    let response
    if (paragraphId) {
      // 加载段落评论
      console.log('加载段落评论，内容ID：', contentId.value, '段落ID：', paragraphId)
      response = await commentApi.getParagraphComments(contentId.value, paragraphId)
    } else {
      // 加载文章评论
      console.log('加载文章评论，内容ID：', contentId.value)
      response = await commentApi.getContentComments(contentId.value, 1, 50)
    }
    
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
      
      formattedComments.forEach(comment => {
        commentMap.set(comment.id, comment)
        comment.replies = []
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
      
      paragraphComments.value = rootComments
      console.log('评论加载成功：', rootComments.length, '条', paragraphId ? '(段落评论)' : '(文章评论)')
    }
  } catch (error) {
    console.error('加载评论失败：', error)
    uni.showToast({
      title: '加载评论失败',
      icon: 'none'
    })
  } finally {
    loadingComments.value = false
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
    return ''
  }
}

// 提交评论（段落评论或文章评论）
const submitParagraphComment = async () => {
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
    const isParagraphComment = !!currentParagraph.value
    
    const commentData = {
      contentId: contentId.value,
      content: newComment.value.trim(),
      commentType: isParagraphComment ? 2 : 1, // 1-文章评论, 2-段落评论
      paragraphId: isParagraphComment ? currentParagraph.value.id : null,
      parentId: replyTo.value ? replyTo.value.id : 0,
      rootId: replyTo.value ? (replyTo.value.rootId || replyTo.value.id) : 0
    }
    
    const response = await commentApi.addComment(commentData)
    
    if (response && response.data) {
      uni.showToast({
        title: '评论成功',
        icon: 'success'
      })
      
      newComment.value = ''
      replyTo.value = null
      showEmojiPicker.value = false
      
      // 重新加载评论
      if (isParagraphComment) {
        await loadComments(currentParagraph.value.id)
        // 更新段落评论数量
        currentParagraph.value.commentCount = (currentParagraph.value.commentCount || 0) + 1
        // 重新加载所有段落的评论数量
        await loadParagraphCommentCounts()
      } else {
        await loadComments()
        // 更新文章评论数量
        article.value.comments = (article.value.comments || 0) + 1
      }
    }
  } catch (error) {
    console.error('提交评论失败：', error)
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
  showEmojiPicker.value = false
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
  newComment.value = ''
}

// 切换表情包选择器
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

// 插入表情
const insertEmoji = (emoji) => {
  newComment.value += emoji
}

// 图片点击事件
const onImageClick = (paragraph) => {
  console.log('点击图片段落：', paragraph)
  
  // 提取图片URL
  const imgMatch = paragraph.content.match(/src=["']([^"']+)["']/i)
  if (imgMatch && imgMatch[1]) {
    const imageUrl = imgMatch[1]
    
    // 预览图片
    uni.previewImage({
      urls: [imageUrl],
      current: imageUrl,
      success: () => {
        console.log('图片预览成功')
      },
      fail: (err) => {
        console.error('图片预览失败：', err)
        uni.showToast({
          title: '图片预览失败',
          icon: 'none'
        })
      }
    })
  }
}

// 计算总页数
const calculateTotalPages = () => {
  if (parsedParagraphs.value.length === 0) {
    totalPages.value = 1
    return
  }
  
  totalPages.value = Math.ceil(parsedParagraphs.value.length / paragraphsPerPage)
  console.log('计算总页数:', totalPages.value, '总段落数:', parsedParagraphs.value.length)
  
  // 确保当前页不超出范围
  if (currentPage.value >= totalPages.value) {
    currentPage.value = totalPages.value - 1
  }
}

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value - 1 && !isTurningNext.value && !isTurningPrev.value) {
    console.log('翻到下一页')
    isTurningNext.value = true
    
    // 在动画进行到1/3时切换页面，让下一页开始显示
    setTimeout(() => {
      currentPage.value++
      // 更新阅读进度
      readProgress.value = Math.round(((currentPage.value + 1) / totalPages.value) * 100)
    }, 200)
    
    // 动画结束后重置状态
    setTimeout(() => {
      isTurningNext.value = false
    }, 600)
  } else if (currentPage.value >= totalPages.value - 1) {
    uni.showToast({
      title: '已经是最后一页了',
      icon: 'none'
    })
  }
}

// 上一页
const previousPage = () => {
  if (currentPage.value > 0 && !isTurningNext.value && !isTurningPrev.value) {
    console.log('翻到上一页')
    isTurningPrev.value = true
    
    // 在动画进行到1/3时切换页面
    setTimeout(() => {
      currentPage.value--
      // 更新阅读进度
      readProgress.value = Math.round(((currentPage.value + 1) / totalPages.value) * 100)
    }, 200)
    
    // 动画结束后重置状态
    setTimeout(() => {
      isTurningPrev.value = false
    }, 600)
  } else if (currentPage.value <= 0) {
    uni.showToast({
      title: '已经是第一页了',
      icon: 'none'
    })
  }
}

// 触摸开始
const onTouchStart = (e) => {
  if (!pageAnimationEnabled.value) return
  
  touchStartX.value = e.touches[0].clientX
  touchStartY.value = e.touches[0].clientY
  isSwiping.value = true
}

// 触摸移动
const onTouchMove = (e) => {
  if (!pageAnimationEnabled.value || !isSwiping.value) return
  
  const currentX = e.touches[0].clientX
  const currentY = e.touches[0].clientY
  const deltaX = currentX - touchStartX.value
  const deltaY = currentY - touchStartY.value
  
  // 如果垂直滑动距离大于水平滑动，则认为是垂直滚动，不处理翻页
  if (Math.abs(deltaY) > Math.abs(deltaX)) {
    return
  }
  
  touchTranslateX.value = deltaX
}

// 触摸结束
const onTouchEnd = (e) => {
  if (!pageAnimationEnabled.value || !isSwiping.value) return
  
  isSwiping.value = false
  const deltaX = touchTranslateX.value
  touchTranslateX.value = 0
  
  const minSwipeDistance = 50 // 最小滑动距离
  
  if (Math.abs(deltaX) > minSwipeDistance) {
    if (deltaX < 0) {
      // 向左滑动，下一页
      nextPage()
    } else {
      // 向右滑动，上一页
      previousPage()
    }
  } else {
    // 点击翻页逻辑（点击屏幕左右边缘）
    // 获取屏幕宽度
    const systemInfo = uni.getSystemInfoSync()
    const screenWidth = systemInfo.windowWidth
    const touchX = touchStartX.value // 使用开始触摸的位置
    
    // 点击左侧1/3区域
    if (touchX < screenWidth / 3) {
      previousPage()
    } 
    // 点击右侧1/3区域
    else if (touchX > screenWidth * 2 / 3) {
      nextPage()
    }
  }
}

const onScroll = (e) => {
  const detail = e.detail || {}
  const newScrollTop = detail.scrollTop || 0
  const scrollHeight = detail.scrollHeight || 0
  const clientHeight = detail.scrollHeight ? (detail.height || detail.clientHeight || 0) : 0
  
  if (scrollHeight === 0 || clientHeight === 0) {
    uni.createSelectorQuery().select('.content-scroll').boundingClientRect((scrollData) => {
      if (scrollData) {
        uni.createSelectorQuery().select('.article-content').boundingClientRect((contentData) => {
          if (contentData) {
            calculateProgress(newScrollTop, contentData.height, scrollData.height)
          }
        }).exec()
      }
    }).exec()
  } else {
    calculateProgress(newScrollTop, scrollHeight, clientHeight)
  }
  
  scrollTop.value = newScrollTop
}

const calculateProgress = (currentScrollTop, totalHeight, visibleHeight) => {
  if (totalHeight > 0 && visibleHeight > 0) {
    const maxScrollTop = totalHeight - visibleHeight
    if (maxScrollTop > 0) {
      const progress = Math.round((currentScrollTop / maxScrollTop) * 100)
      readProgress.value = Math.max(0, Math.min(100, progress))
    } else {
      readProgress.value = 100
    }
  } else {
    readProgress.value = 0
  }
}

</script>

<style>
/* 引入 Font Awesome 图标库 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: var(--reading-bg-color, #ffffff);
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s ease;
}

/* 顶部导航栏 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 40rpx 32rpx;
  z-index: 50;
  transition: background-color 0.3s ease;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.back-btn .fas {
  color: #ffffff;
  font-size: 18px;
}

.header-title {
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-right {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  transition: background-color 0.2s ease;
}

.action-btn:active {
  background-color: rgba(255, 255, 255, 0.3);
}

.action-btn .fas {
  color: #ffffff;
  font-size: 16px;
  transition: color 0.3s ease;
}

.action-btn .fas.active {
  color: #fbbf24;
}

/* 阅读设置按钮 */
.reading-settings-btn {
  background-color: rgba(255, 255, 255, 0.25);
}

.reading-settings-btn:active {
  background-color: rgba(255, 255, 255, 0.35);
}

.settings-text {
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  font-family: Georgia, serif;
}

/* 阅读内容区域 */
.reading-container {
  flex: 1;
  margin-top: 56px;
  margin-bottom: 120rpx;
}

.content-scroll {
  height: calc(100vh - 56px - 120rpx);
}

.article-content {
  padding: 48rpx;
  max-width: 1600rpx;
  margin: 0 auto;
}

.article-header {
  margin-bottom: 64rpx;
}

.article-title {
  font-size: 56rpx;
  font-weight: 600;
  color: var(--reading-title-color, #1f2937);
  margin-bottom: 32rpx;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.article-meta {
  display: flex;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.article-author,
.article-date {
  font-size: 28rpx;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
}

.article-stats {
  display: flex;
  gap: 40rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  color: #6b7280;
}

.stat-item .fas {
  font-size: 28rpx;
}

.article-body {
  padding-bottom: 80rpx;
  font-family: var(--reading-font-family, system-ui, -apple-system, sans-serif);
  font-size: var(--reading-font-size, 16px);
  line-height: var(--reading-line-height, 1.8);
  color: var(--reading-text-color, #374151);
  transition: font-size 0.3s ease, color 0.3s ease, font-family 0.3s ease;
}

/* 加载和错误状态 */
.loading-content,
.error-content,
.no-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 48rpx;
  text-align: center;
}

.loading-content .fas {
  font-size: 64rpx;
  color: #6366f1;
  margin-bottom: 32rpx;
}

.loading-text,
.error-text,
.no-content-text {
  font-size: 28rpx;
  color: #9ca3af;
}

.retry-btn {
  margin-top: 32rpx;
  padding: 20rpx 48rpx;
  background-color: #6366f1;
  color: #ffffff;
  border-radius: 40rpx;
  font-size: 28rpx;
}

/* 文章内容 */
.content-wrapper {
  min-height: 200rpx;
}

.cover-image {
  width: 100%;
  border-radius: 16rpx;
  margin-bottom: 48rpx;
}

/* 段落容器样式 */
.paragraph-container {
  position: relative;
  margin-bottom: 16rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  transition: background-color 0.2s ease;
}

.paragraph-content {
  position: relative;
}

.paragraph-rich-text {
  line-height: var(--reading-line-height, 1.8);
  font-size: var(--reading-font-size, 16px);
  color: var(--reading-text-color, #374151);
  transition: font-size 0.3s ease, color 0.3s ease;
  word-wrap: break-word;
}

/* 统一图片样式 */
.paragraph-rich-text img {
  width: 100% !important;
  height: auto !important;
  max-width: 600px !important;
  border-radius: 8px !important;
  margin: 16rpx auto !important;
  display: block !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

/* 图片段落的特殊样式 */
.paragraph-container.is-image {
  padding: 4rpx 8rpx;
  margin-bottom: 8rpx;
  background-color: transparent;
  cursor: pointer;
}

.paragraph-container.is-image .paragraph-rich-text {
  text-align: center;
  position: relative;
}

/* 段落操作按钮 */
.paragraph-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8rpx;
  opacity: 0.6;
  transition: opacity 0.2s ease;
}

.paragraph-container:hover .paragraph-actions {
  opacity: 1;
}

.comment-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx 12rpx;
  background-color: var(--reading-btn-bg, rgba(99, 102, 241, 0.1));
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.comment-btn:hover {
  background-color: var(--reading-btn-hover-bg, rgba(99, 102, 241, 0.2));
  border-color: #6366f1;
}

.comment-btn:active {
  transform: scale(0.95);
}

.comment-btn .fas {
  font-size: 20rpx;
  color: #6366f1;
  transition: color 0.2s ease;
}

.comment-count {
  font-size: 20rpx;
  color: #6366f1;
  font-weight: 500;
  min-width: 16rpx;
  text-align: center;
}

.comment-hint {
  font-size: 18rpx;
  color: #6366f1;
  font-weight: 400;
}

/* 当段落有评论时的样式 */
.paragraph-container.has-comments {
  border-left: 3px solid #6366f1;
  background-color: rgba(99, 102, 241, 0.05);
}

.paragraph-container.has-comments .comment-btn {
  background-color: rgba(99, 102, 241, 0.2);
  border-color: #6366f1;
}

/* 评论弹窗遮罩 */
.comment-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 评论弹窗 */
.comment-popup {
  width: 100%;
  max-height: 80vh;
  background-color: var(--reading-bg-color, #ffffff);
  border-radius: 24rpx 24rpx 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
  transition: background-color 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

/* 弹窗标题栏 */
.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid var(--reading-border-color, #e5e7eb);
  transition: border-color 0.3s ease;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--reading-title-color, #1f2937);
  transition: color 0.3s ease;
}

.close-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.close-btn:active {
  background-color: var(--reading-btn-hover-bg, rgba(0, 0, 0, 0.1));
}

.close-btn .fas {
  font-size: 28rpx;
  color: var(--reading-text-color, #6b7280);
  transition: color 0.3s ease;
}

/* 原段落内容 */
.original-paragraph {
  background-color: var(--reading-comment-bg, rgba(99, 102, 241, 0.08));
  padding: 24rpx;
  margin: 0 32rpx 24rpx;
  border-radius: 12rpx;
  border-left: 4rpx solid #6366f1;
  transition: background-color 0.3s ease;
}

.paragraph-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: var(--reading-text-color, #374151);
  transition: color 0.3s ease;
}

/* 评论列表 */
.comments-list {
  flex: 1;
  padding: 0 32rpx;
  max-height: 50vh;
}

.loading-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx;
}

.loading-comments .fas {
  font-size: 48rpx;
  color: #6366f1;
  margin-bottom: 16rpx;
}

.loading-text {
  font-size: 24rpx;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
}

.no-comments {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
}

.no-comments-text {
  font-size: 28rpx;
  color: var(--reading-meta-color, #9ca3af);
  transition: color 0.3s ease;
}

.comment-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid var(--reading-border-color, #e5e7eb);
  transition: border-color 0.3s ease;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 24rpx;
}

.comment-info {
  flex: 1;
}

.username {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--reading-title-color, #1f2937);
  margin-bottom: 4rpx;
  display: block;
  transition: color 0.3s ease;
}

.time {
  font-size: 24rpx;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
}

.comment-content {
  font-size: 28rpx;
  line-height: 1.6;
  color: var(--reading-text-color, #374151);
  margin: 16rpx 0;
  display: block;
  transition: color 0.3s ease;
}

.comment-actions {
  display: flex;
  gap: 32rpx;
  margin-top: 16rpx;
  flex-wrap: nowrap;
}

.comment-actions .action-btn,
.reply-actions .action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  transition: background-color 0.2s ease;
  flex-shrink: 0;
  white-space: nowrap;
}

.comment-actions .action-btn:active,
.reply-actions .action-btn:active {
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
}

.comment-actions .action-btn .fas,
.reply-actions .action-btn .fas {
  font-size: 24rpx;
  color: var(--reading-toolbar-icon, #6b7280);
  transition: color 0.3s ease;
}

.comment-actions .action-btn .fas.active,
.reply-actions .action-btn .fas.active {
  color: #6366f1;
}

.count,
.action-text {
  font-size: 24rpx;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
  white-space: nowrap;
  word-break: keep-all;
}

/* 回复样式 */
.replies-container {
  margin-left: 64rpx;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid var(--reading-border-color, rgba(0, 0, 0, 0.05));
  transition: border-color 0.3s ease;
}

.reply-item {
  margin-bottom: 24rpx;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.avatar.small {
  width: 48rpx;
  height: 48rpx;
  margin-right: 16rpx;
}

.reply-info {
  flex: 1;
}

.reply-info .username {
  font-size: 24rpx;
  margin-right: 16rpx;
}

.reply-to {
  font-size: 24rpx;
  color: var(--reading-meta-color, #6b7280);
  margin-right: 16rpx;
  transition: color 0.3s ease;
}

.reply-info .time {
  font-size: 22rpx;
}

.reply-content {
  font-size: 26rpx;
  line-height: 1.5;
  color: var(--reading-text-color, #374151);
  margin: 8rpx 0;
  margin-left: 64rpx;
  display: block;
  transition: color 0.3s ease;
}

.reply-actions {
  display: flex;
  gap: 20rpx;
  margin-left: 64rpx;
  flex-wrap: nowrap;
}

.reply-actions .action-btn.small {
  padding: 4rpx 12rpx;
  flex-shrink: 0;
  white-space: nowrap;
}

.reply-actions .action-btn.small .fas {
  font-size: 20rpx;
}

.reply-actions .action-btn.small .count,
.reply-actions .action-btn.small .action-text {
  font-size: 22rpx;
  white-space: nowrap;
  word-break: keep-all;
}

/* 评论输入框 */
.comment-input {
  padding: 24rpx 32rpx;
  background-color: var(--reading-toolbar-bg, #ffffff);
  border-top: 1rpx solid var(--reading-toolbar-border, #e5e7eb);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.reply-hint {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 20rpx;
  background-color: rgba(99, 102, 241, 0.1);
  border-radius: 12rpx;
  margin-bottom: 12rpx;
}

.reply-hint-text {
  font-size: 24rpx;
  color: #6366f1;
}

.cancel-reply {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
}

.cancel-reply .fas {
  font-size: 20rpx;
  color: #6b7280;
}

.input-wrapper {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.emoji-btn {
  width: 64rpx;
  height: 64rpx;
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
  font-size: 32rpx;
  transition: color 0.3s ease;
}

.input-field {
  flex: 1;
  height: 64rpx;
  padding: 0 24rpx;
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  border-radius: 32rpx;
  font-size: 28rpx;
  color: var(--reading-text-color, #374151);
  transition: background-color 0.3s ease, color 0.3s ease;
}

.send-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--reading-btn-hover-bg, rgba(0, 0, 0, 0.08));
  border-radius: 50%;
  transition: all 0.2s ease;
}

.send-btn.active {
  background-color: #6366f1;
}

.send-btn .fas {
  color: var(--reading-toolbar-icon, #6b7280);
  font-size: 28rpx;
  transition: color 0.2s ease;
}

.send-btn.active .fas {
  color: #ffffff;
}

/* 表情包选择器 */
.emoji-picker {
  margin-top: 24rpx;
  background-color: var(--reading-card-bg, rgba(248, 249, 250, 0.95));
  border-radius: 12rpx;
  max-height: 400rpx;
  transition: background-color 0.3s ease;
}

.emoji-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: var(--reading-toolbar-bg, #ffffff);
  border-bottom: 1rpx solid var(--reading-border-color, #e5e7eb);
  border-radius: 12rpx 12rpx 0 0;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.emoji-title {
  font-size: 26rpx;
  font-weight: 500;
  color: var(--reading-title-color, #1f2937);
  transition: color 0.3s ease;
}

.close-emoji {
  width: 48rpx;
  height: 48rpx;
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
  font-size: 24rpx;
  color: var(--reading-toolbar-icon, #6b7280);
  transition: color 0.3s ease;
}

.emoji-list {
  height: 320rpx;
  background-color: var(--reading-card-bg, rgba(255, 255, 255, 0.95));
  transition: background-color 0.3s ease;
}

.emoji-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx;
}

.emoji-item {
  width: 12.5%;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
  border-radius: 8rpx;
}

.emoji-item:active {
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  transform: scale(1.2);
}

.emoji-text {
  font-size: 56rpx;
  line-height: 1;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--reading-toolbar-bg, #ffffff);
  border-top: 1rpx solid var(--reading-toolbar-border, #e5e7eb);
  padding: 16rpx 48rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 40;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.bar-left {
  flex: 1;
}

.progress-text {
  font-size: 24rpx;
  color: var(--reading-toolbar-text, #6b7280);
  transition: color 0.3s ease;
}

.bar-right {
  display: flex;
  gap: 32rpx;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  cursor: pointer;
}

.bar-item .fas {
  font-size: 40rpx;
  color: var(--reading-toolbar-icon, #6b7280);
  transition: color 0.3s ease;
}

.bar-item .fas.active {
  color: var(--reading-active-icon, #6366f1);
}

.bar-text {
  font-size: 28rpx;
  color: var(--reading-toolbar-text, #6b7280);
  transition: color 0.3s ease;
}
/* 翻页阅读模式样式 */
.page-reading-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 112px); /* header 56px + toolbar 56px */
  overflow: hidden;
  perspective: 2000px; /* 3D透视效果 */
  background-color: var(--reading-bg-color, #ffffff);
}

/* 页面包装器 */
.page-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  backface-visibility: hidden;
  overflow: hidden;
}

.page-content {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  position: relative;
  background-color: var(--reading-bg-color, #ffffff);
  box-shadow: 0 0 20rpx rgba(0, 0, 0, 0.1);
}

/* 当前页 */
.current-page {
  z-index: 10;
}

/* 下一页预览 */
.next-page-preview {
  z-index: 5;
  opacity: 0;
}

/* 上一页预览 */
.prev-page-preview {
  z-index: 5;
  opacity: 0;
}

/* 向下翻页动画（翻到下一页） - 真实纸张效果 */
.current-page.turning-next {
  animation: paperFlipNext 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  z-index: 20;
}

.current-page.turning-next ~ .next-page-preview {
  animation: nextPageReveal 0.4s cubic-bezier(0.4, 0, 0.2, 1) 0.2s forwards;
  /* 0.2s 延迟，让下一页在翻页进行到1/3时开始显示 */
}

@keyframes paperFlipNext {
  0% {
    transform: rotateY(0deg) translateZ(0);
    transform-origin: left center;
    box-shadow: 0 0 20rpx rgba(0, 0, 0, 0.1);
  }
  50% {
    transform: rotateY(-90deg) translateZ(100rpx);
    transform-origin: left center;
    box-shadow: -20rpx 0 40rpx rgba(0, 0, 0, 0.3);
  }
  100% {
    transform: rotateY(-180deg) translateZ(0);
    transform-origin: left center;
    box-shadow: 0 0 20rpx rgba(0, 0, 0, 0.1);
  }
}

@keyframes nextPageReveal {
  0% {
    opacity: 0;
    transform: scale(0.98);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 向上翻页动画（翻到上一页） - 真实纸张效果 */
.current-page.turning-prev {
  animation: paperFlipPrev 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  z-index: 20;
}

.current-page.turning-prev ~ .prev-page-preview {
  animation: prevPageReveal 0.4s cubic-bezier(0.4, 0, 0.2, 1) 0.2s forwards;
  /* 0.2s 延迟，让上一页在翻页进行到1/3时开始显示 */
}

@keyframes paperFlipPrev {
  0% {
    transform: rotateY(0deg) translateZ(0);
    transform-origin: right center;
    box-shadow: 0 0 20rpx rgba(0, 0, 0, 0.1);
  }
  50% {
    transform: rotateY(90deg) translateZ(100rpx);
    transform-origin: right center;
    box-shadow: 20rpx 0 40rpx rgba(0, 0, 0, 0.3);
  }
  100% {
    transform: rotateY(180deg) translateZ(0);
    transform-origin: right center;
    box-shadow: 0 0 20rpx rgba(0, 0, 0, 0.1);
  }
}

@keyframes prevPageReveal {
  0% {
    opacity: 0;
    transform: scale(0.98);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.page-article {
  min-height: calc(100vh - 112px);
  padding-bottom: 100rpx;
  padding-top: 20rpx;
  position: relative;
}

/* 确保段落内容和评论按钮可以点击 */
.page-article .paragraph-container {
  position: relative;
}

.page-article .comment-btn {
  position: relative;
  pointer-events: auto;
}

/* 页码指示器 */
.page-indicator {
  position: absolute;
  bottom: 20rpx;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 30;
  pointer-events: none;
}

.page-number {
  display: inline-block;
  padding: 8rpx 24rpx;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 30rpx;
  font-size: 24rpx;
  backdrop-filter: blur(4px);
}
</style> 

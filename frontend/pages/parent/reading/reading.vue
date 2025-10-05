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
          <view class="action-btn" @click="goToReadingMode">
            <text class="fas fa-font"></text>
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
      <!-- 文章内容 -->
      <scroll-view 
        scroll-y="true" 
        class="content-scroll"
        :scroll-top="scrollTop"
        @scroll="onScroll"
        :show-scrollbar="false"
      >
        <view class="article-content">
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

          <view class="article-body">
            <!-- 加载状态 -->
            <view v-if="loading" class="loading-content">
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
              <!-- 功能提示 -->
              <view v-if="!hasShownTip" class="function-tip">
                <view class="tip-content">
                  <text class="fas fa-lightbulb"></text>
                  <text class="tip-text">点击段落右侧的评论按钮可以对该段落进行评论，长按段落可以复制内容</text>
                  <view class="tip-close" @click="closeTip">
                    <text class="fas fa-times"></text>
                  </view>
                </view>
              </view>
              
              <!-- 渲染解析后的段落内容 -->
              <view v-for="(paragraph, index) in parsedParagraphs" :key="index" 
                    :class="['paragraph-container', { 'has-comments': paragraph.commentCount > 0, 'is-image': paragraph.isImage }]">
                <!-- 段落内容 -->
                <view class="paragraph-content" 
                      @longpress="!paragraph.isImage ? onParagraphLongPress(paragraph) : null"
                      @click="paragraph.isImage ? onImageClick(paragraph) : null">
                  <rich-text 
                    :nodes="paragraph.content" 
                    class="paragraph-rich-text"
                  ></rich-text>
                </view>
                
                <!-- 段落评论按钮和计数（只显示给非图片段落） -->
                <view v-if="paragraph.id && !paragraph.isImage" class="paragraph-actions">
                  <view class="comment-btn" @click="navigateToComment(paragraph.id, paragraph.text)">
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

      <!-- 回到顶部按钮 -->
      <view class="back-to-top" @click="scrollToTop" v-if="showTopBtn">
        <text class="fas fa-arrow-up"></text>
      </view>
    </view>

    <!-- 底部工具栏 -->
    <view class="bottom-toolbar">
      <view class="toolbar-left">
        <text class="progress-text">阅读进度: {{ readingProgress }}%</text>
      </view>
      <view class="toolbar-right">
        <view class="tool-btn" @click="toggleLike">
          <text class="fas fa-thumbs-up" :class="{ 'active': isLiked }"></text>
          <text class="tool-text">{{ article.likes }}</text>
        </view>
        <view class="tool-btn" @click="navigateToComments">
          <text class="fas fa-comment"></text>
          <text class="tool-text">{{ article.comments }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onUnload, onShow } from '@dcloudio/uni-app'
import { contentApi, commentApi, viewHistoryApi, userApi, likeApi, favoriteApi, userBehaviorApi } from '@/utils/api.js'
import readingModeManager from '@/utils/readingModeManager.js'

// 文章信息
const article = ref({
  id: null,
  title: '加载中...',
  author: '',
  date: '',
  views: '0',
  likes: '0',
  comments: '0',
  content: '',
  coverUrl: '',
  creatorName: '',
  createdTime: null,
  viewCount: 0,
  likeCount: 0,
  commentCount: 0
})

// 加载状态
const loading = ref(true)
const error = ref('')

// 解析后的段落数据
const parsedParagraphs = ref([])
const paragraphCommentCounts = ref({})

// 功能提示
const hasShownTip = ref(false)

// 当前用户信息
const currentUser = ref(null)

// 交互状态
const scrollTop = ref(0)
const showTopBtn = ref(false)
const isBookmarked = ref(false)
const isLiked = ref(false)
const readingProgress = ref(0)

// 阅读时长追踪
const readingStartTime = ref(null)

// 页面加载时获取文章ID并加载数据
onLoad(async (option) => {
  console.log('阅读页面参数：', option)
  
  // 检查是否已经显示过提示
  const tipShown = uni.getStorageSync('reading_tip_shown')
  hasShownTip.value = !!tipShown
  
  // 应用阅读模式设置
  applyReadingModeSettings()
  
  if (option.id) {
    article.value.id = parseInt(option.id)
    await loadCurrentUser() // 先加载用户信息
    await loadArticleData()
    
    // 记录阅读开始时间
    readingStartTime.value = Date.now()
    console.log('开始阅读，记录时间戳：', readingStartTime.value)
  } else {
    error.value = '文章ID不存在'
    console.error('缺少文章ID参数')
  }
})

// 页面显示时重新应用阅读模式设置
onShow(() => {
  console.log('[阅读页面] 页面显示，重新应用阅读模式设置')
  applyReadingModeSettings()
})

// 页面卸载时清理并记录阅读时长
onUnload(async () => {
  console.log('阅读页面卸载')
  
  // 记录阅读时长
  if (readingStartTime.value && currentUser.value?.id && article.value?.id) {
    const readingEndTime = Date.now()
    const duration = Math.floor((readingEndTime - readingStartTime.value) / 1000) // 转换为秒
    
    console.log('结束阅读，时长：', duration, '秒，进度：', readingProgress.value, '%')
    
    // 只有阅读时长超过3秒才记录
    if (duration >= 3) {
      try {
        await userBehaviorApi.recordReadingBehavior(
          currentUser.value.id, 
          article.value.id, 
          duration, 
          readingProgress.value
        )
        console.log('阅读行为记录成功')
      } catch (error) {
        console.error('记录阅读行为失败：', error)
        // 不影响页面卸载
      }
    } else {
      console.log('阅读时长过短，不记录')
    }
  }
})

// 应用阅读模式设置
const applyReadingModeSettings = () => {
  console.log('开始应用阅读模式设置到阅读页面')
  try {
    const settings = readingModeManager.loadSettings()
    console.log('读取到的阅读模式设置:', settings)
    
    // 应用所有设置
    readingModeManager.applySettings(settings)
    
    console.log('阅读模式设置应用完成')
  } catch (error) {
    console.error('应用阅读模式设置失败:', error)
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

// 加载文章数据
const loadArticleData = async () => {
  try {
    loading.value = true
    console.log('开始加载文章数据，ID：', article.value.id)
    
    const response = await contentApi.getContentDetail(article.value.id)
    
    if (response && response.data) {
      const data = response.data
      article.value = {
        id: data.id,
        title: data.title || '无标题',
        author: data.creatorName || '匿名作者',
        date: formatDate(data.createdTime),
        views: formatViewCount(data.viewCount),
        likes: formatViewCount(data.likeCount),
        comments: formatViewCount(data.commentCount),
        content: data.content || '',
        coverUrl: data.coverUrl || '',
        creatorName: data.creatorName || '匿名作者',
        createdTime: data.createdTime,
        viewCount: data.viewCount || 0,
        likeCount: data.likeCount || 0,
        commentCount: data.commentCount || 0
      }
      
      console.log('文章数据加载成功：', article.value)
      
      // 加载点赞状态和收藏状态
      await loadLikeStatus()
      await loadBookmarkStatus()
      
      // 记录浏览行为，增加浏览量并添加浏览记录
      try {
        // 1. 增加浏览量
        await contentApi.incrementViewCount(article.value.id)
        console.log('浏览量增加成功')
        
        // 2. 获取用户ID并添加浏览记录
        if (currentUser.value && currentUser.value.id) {
          const userId = currentUser.value.id
          console.log('当前用户ID：', userId, '准备添加浏览记录')
          
          await viewHistoryApi.addViewHistory(userId, article.value.id)
          console.log('浏览记录添加成功')
        } else {
          console.warn('获取用户ID失败，无法添加浏览记录')
        }
      } catch (error) {
        console.error('添加浏览记录失败：', error)
        // 浏览记录失败不影响正常阅读
      }
      
      // 解析文章内容为段落
      await parseArticleContent()
      
      // 加载段落评论数量
      await loadParagraphCommentCounts()
    } else {
      throw new Error('文章数据格式异常')
    }
  } catch (error) {
    console.error('加载文章数据失败：', error)
    error.value = '加载文章数据失败'
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 加载点赞状态
const loadLikeStatus = async () => {
  if (!currentUser.value || !currentUser.value.id || !article.value.id) return
  
  try {
    console.log('开始加载点赞状态')
    const response = await likeApi.getLikeStatus(currentUser.value.id, article.value.id, 1)
    if (response && response.data !== undefined) {
      isLiked.value = response.data
      console.log('点赞状态加载成功：', isLiked.value)
    }
  } catch (error) {
    console.error('加载点赞状态失败：', error)
    // 不影响主要功能，只是点赞状态可能不准确
  }
}

// 加载收藏状态
const loadBookmarkStatus = async () => {
  if (!currentUser.value || !currentUser.value.id || !article.value.id) return
  
  try {
    console.log('开始加载收藏状态')
    const response = await favoriteApi.getFavoriteStatus(currentUser.value.id, article.value.id)
    if (response && response.data !== undefined) {
      isBookmarked.value = response.data
      console.log('收藏状态加载成功：', isBookmarked.value)
    }
  } catch (error) {
    console.error('加载收藏状态失败：', error)
    // 不影响主要功能，只是收藏状态可能不准确
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

// 格式化浏览量显示
const formatViewCount = (count) => {
  if (!count || count === 0) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + '万'
}

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
    // 如果没有上一页，返回到书架页面
    uni.redirectTo({
      url: '/pages/parent/bookshelf/bookshelf',
      fail: (err) => {
        console.error('返回书架失败:', err)
        // 尝试切换到首页tab
        uni.switchTab({
          url: '/pages/parent/home/home',
          fail: (switchErr) => {
            console.error('切换到首页失败:', switchErr)
            // 提示用户
            uni.showToast({
              title: '返回失败，请重试',
              icon: 'none'
            })
          }
        })
      }
    })
  }
}

// 跳转到阅读模式设置页面
const goToReadingMode = () => {
  console.log('跳转到阅读模式设置')
  uni.navigateTo({
    url: '/pages/parent/profile/details/reading-mode',
    success: () => {
      console.log('跳转成功')
    },
    fail: (error) => {
      console.error('跳转失败:', error)
      uni.showToast({
        title: '跳转失败',
        icon: 'none'
      })
    }
  })
}

// 滚动处理
const onScroll = (e) => {
  console.log('滚动事件原始数据：', e)
  console.log('e.detail:', e.detail)
  
  // 兼容不同平台的参数获取方式
  const detail = e.detail || {}
  const newScrollTop = detail.scrollTop || 0
  const scrollHeight = detail.scrollHeight || 0
  const clientHeight = detail.scrollHeight ? (detail.height || detail.clientHeight || 0) : 0
  
  // 如果scrollHeight为0，尝试通过查询DOM获取
  if (scrollHeight === 0 || clientHeight === 0) {
    // 使用uni.createSelectorQuery查询实际高度
    uni.createSelectorQuery().in(this).select('.content-scroll').boundingClientRect((scrollData) => {
      if (scrollData) {
        uni.createSelectorQuery().in(this).select('.article-content').boundingClientRect((contentData) => {
          if (contentData) {
            const actualScrollHeight = contentData.height
            const actualClientHeight = scrollData.height
            
            console.log('通过查询获取的高度：', {
              scrollHeight: actualScrollHeight,
              clientHeight: actualClientHeight,
              scrollTop: newScrollTop
            })
            
            // 使用查询到的高度计算进度
            calculateProgress(newScrollTop, actualScrollHeight, actualClientHeight)
          }
        }).exec()
      }
    }).exec()
  } else {
    // 使用事件参数计算进度
    console.log('使用事件参数计算，数据：', {
      scrollTop: newScrollTop,
      scrollHeight: scrollHeight,
      clientHeight: clientHeight
    })
    calculateProgress(newScrollTop, scrollHeight, clientHeight)
  }
  
  scrollTop.value = newScrollTop
  
  // 显示/隐藏回到顶部按钮（滚动超过200px时显示）
  showTopBtn.value = newScrollTop > 200
}

// 计算阅读进度的独立函数
const calculateProgress = (currentScrollTop, totalHeight, visibleHeight) => {
  console.log('开始计算阅读进度，参数：', {
    currentScrollTop,
    totalHeight,
    visibleHeight
  })
  
  if (totalHeight > 0 && visibleHeight > 0) {
    // 可滚动的最大距离 = 总高度 - 可见高度
    const maxScrollTop = totalHeight - visibleHeight
    
    console.log('最大可滚动距离：', maxScrollTop)
    
    if (maxScrollTop > 0) {
      // 当前滚动进度 = (已滚动距离 / 可滚动的最大距离) * 100
      const progress = Math.round((currentScrollTop / maxScrollTop) * 100)
      readingProgress.value = Math.max(0, Math.min(100, progress))
      
      console.log('阅读进度计算结果：', {
        maxScrollTop,
        currentScroll: currentScrollTop,
        progress: readingProgress.value
      })
    } else {
      // 如果内容高度小于或等于可见高度，说明不需要滚动，进度为100%
      console.log('内容无需滚动，进度设为100%')
      readingProgress.value = 100
    }
  } else {
    console.log('高度参数无效，进度设为0')
    readingProgress.value = 0
  }
}

// 滚动到顶部
const scrollToTop = () => {
  console.log('点击回到顶部')
  scrollTop.value = 0
  
  // 显示提示
  uni.showToast({
    title: '已回到顶部',
    icon: 'none',
    duration: 1000
  })
}

// 交互功能 - 收藏/取消收藏
const toggleBookmark = async () => {
  if (!currentUser.value || !currentUser.value.id || !article.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('开始切换收藏状态，当前状态：', isBookmarked.value)
    
    // 调用收藏/取消收藏接口
    const response = await favoriteApi.toggleFavorite(currentUser.value.id, article.value.id)
    
    if (response && response.data !== undefined) {
      // 更新收藏状态
      isBookmarked.value = response.data
      
      console.log('收藏状态更新成功：', isBookmarked.value)
      
      uni.showToast({
        title: isBookmarked.value ? '已收藏' : '已取消收藏',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('更新收藏状态失败：', error)
    uni.showToast({
      title: '收藏失败，请重试',
      icon: 'none'
    })
  }
}

const toggleLike = async () => {
  if (!currentUser.value || !currentUser.value.id || !article.value.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('开始切换点赞状态，当前状态：', isLiked.value)
    
    const response = await likeApi.toggleLike(currentUser.value.id, article.value.id, 1)
    
    if (response && response.data) {
      // 更新点赞状态和数量
      isLiked.value = response.data.isLiked
      article.value.likeCount = response.data.likeCount
      article.value.likes = formatViewCount(response.data.likeCount)
      
      console.log('点赞状态更新成功：', response.data)
      
      uni.showToast({
        title: isLiked.value ? '已点赞' : '已取消点赞',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('更新点赞状态失败：', error)
    uni.showToast({
      title: '点赞失败，请重试',
      icon: 'none'
    })
  }
}

// 跳转到评论页面
const navigateToComment = (paragraphId, content) => {
  if (!article.value.id) {
    uni.showToast({
      title: '文章数据未加载',
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages/parent/reading/paragraph-comment?contentId=${article.value.id}&paragraphId=${paragraphId}&content=${encodeURIComponent(content)}`
  })
}

// 跳转到文章评论页面
const navigateToComments = () => {
  if (!article.value.id) {
    uni.showToast({
      title: '文章数据未加载',
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages/parent/reading/article-comments?contentId=${article.value.id}`
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
    let lastIndex = 0
    
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
          isImage: true // 标记为图片
        })
      } else {
        // 处理文本段落
        // 提取id属性
        const idMatch = fullMatch.match(/id=["']([^"']+)["']/i)
        const paragraphId = idMatch ? idMatch[1] : `para-temp-${paragraphIndex++}`
        
        // 提取纯文本内容，并将HTML实体转换为纯文本
        let textContent = innerContent.replace(/<[^>]*>/g, '').trim()
        // 将 &nbsp; 等HTML实体替换为空格，然后再次trim
        textContent = textContent.replace(/&nbsp;/gi, ' ').replace(/&\w+;/g, ' ').trim()
        
        // 检查是否只包含空白字符（包括空格、&nbsp;、全角空格等）
        const isOnlyWhitespace = !textContent || /^[\s\u00A0\u3000]+$/.test(textContent)
        
        // 如果段落有实际文本内容才添加评论功能
        if (textContent && textContent.length > 5 && !isOnlyWhitespace) { // 至少要有5个字符且不是纯空白字符
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
        // 否则，忽略这个只包含空白字符的段落
      }
    }
    
    // 如果没有解析到任何内容，使用整个内容
    if (paragraphs.length === 0) {
      // 处理整体内容中的图片样式
      const processedContent = htmlContent.replace(
        /<img([^>]*?)(?:style="[^"]*")?([^>]*?)>/gi,
        '<img$1 style="width: 100%; height: auto; max-width: 600px; border-radius: 8px; margin: 16px 0; display: block;"$2>'
      )
      
      // 提取文本内容并清理HTML实体
      let textContent = htmlContent.replace(/<[^>]*>/g, '').trim()
      textContent = textContent.replace(/&nbsp;/gi, ' ').replace(/&\w+;/g, ' ').trim()
      
      // 检查是否只包含空白字符
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
    console.log('解析结果：', paragraphs.map(p => ({ tagName: p.tagName, hasText: !!p.text, isImage: p.isImage })))
    
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
    // 不影响主要功能，只是评论数量显示可能不准确
  }
}

// 段落长按事件
const onParagraphLongPress = (paragraph) => {
  if (paragraph.isImage) return // 图片不响应长按
  
  console.log('长按段落：', paragraph.id)
  uni.showActionSheet({
    itemList: ['评论这段内容', '复制段落内容'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 评论段落
        navigateToComment(paragraph.id, paragraph.text)
      } else if (res.tapIndex === 1) {
        // 复制内容
        uni.setClipboardData({
          data: paragraph.text,
          success: () => {
            uni.showToast({
              title: '已复制到剪贴板',
              icon: 'success'
            })
          }
        })
      }
    }
  })
}

// 关闭功能提示
const closeTip = () => {
  hasShownTip.value = true
  uni.setStorageSync('reading_tip_shown', true)
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

// 处理富文本点击事件
const onRichTextClick = (event) => {
  console.log('富文本点击事件：', event)
  // 这里可以处理图片点击等事件
}

// 分享文章
const shareArticle = () => {
  console.log('开始分享文章：', article.value.title)
  
  if (!article.value.id) {
    uni.showToast({
      title: '文章数据未加载',
      icon: 'none'
    })
    return
  }
  
  // 构建分享内容
  const shareTitle = article.value.title || '精彩文章'
  const shareContent = `推荐阅读：《${shareTitle}》`
  
  // 检测平台
  const systemInfo = uni.getSystemInfoSync()
  console.log('当前平台：', systemInfo.platform)
  
  // H5环境下使用系统分享或显示分享选项
  // #ifdef H5
  if (navigator.share) {
    console.log('使用Web Share API')
    navigator.share({
      title: shareTitle,
      text: shareContent,
      url: window.location.href
    }).then(() => {
      console.log('分享成功')
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    }).catch((error) => {
      console.error('分享失败：', error)
      // 如果用户取消分享，不显示错误提示
      if (error.name !== 'AbortError') {
        showShareActionSheet()
      }
    })
  } else {
    console.log('不支持Web Share API，显示分享选项')
    showShareActionSheet()
  }
  // #endif
  
  // 小程序环境下使用小程序分享
  // #ifdef MP-WEIXIN
  console.log('微信小程序环境，使用小程序分享')
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
  console.log('App环境，尝试使用系统分享')
  
  // 优先使用uni.share()原生分享
  uni.share({
    provider: 'system', // 使用系统分享
    type: 1, // 图文分享
    title: shareTitle,
    summary: `来自亲子阅读：${shareTitle}`,
    href: `https://parentreading.com/article/${article.value.id}`,
    success: (res) => {
      console.log('系统分享成功：', res)
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('系统分享失败，降级使用操作菜单：', err)
      // 如果系统分享不可用，降级使用操作菜单
      showShareActionSheet()
    }
  })
  // #endif
}

// 显示分享操作菜单
const showShareActionSheet = () => {
  console.log('显示分享操作菜单')
  
  // #ifdef APP-PLUS
  // App环境下提供更多分享选项
  uni.showActionSheet({
    itemList: ['复制链接', '使用系统分享', '生成分享海报'],
    success: (res) => {
      console.log('选择了分享方式，索引：', res.tapIndex)
      
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
      console.error('显示分享菜单失败：', err)
    }
  })
  // #endif
  
  // #ifndef APP-PLUS
  // 非App环境下的分享选项
  uni.showActionSheet({
    itemList: ['复制链接', '生成分享海报'],
    success: (res) => {
      console.log('选择了分享方式，索引：', res.tapIndex)
      
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
      console.error('显示分享菜单失败：', err)
    }
  })
  // #endif
}

// 使用系统分享（App环境）
const useSystemShare = () => {
  console.log('使用系统分享')
  
  // #ifdef APP-PLUS
  const shareTitle = article.value.title || '精彩文章'
  
  uni.share({
    provider: 'system',
    type: 1,
    title: shareTitle,
    summary: `来自亲子阅读：${shareTitle}`,
    href: `https://parentreading.com/article/${article.value.id}`,
    success: (res) => {
      console.log('系统分享成功：', res)
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('系统分享失败：', err)
      uni.showToast({
        title: '分享失败，请稍后重试',
        icon: 'none'
      })
    }
  })
  // #endif
}

// 复制文章链接
const copyArticleLink = () => {
  console.log('复制文章链接')
  
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
      console.log('链接复制成功')
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('复制失败：', err)
      uni.showToast({
        title: '复制失败',
        icon: 'none'
      })
    }
  })
}

// 生成分享海报
const generateSharePoster = () => {
  console.log('生成分享海报')
  uni.showToast({
    title: '海报生成功能开发中',
    icon: 'none'
  })
  // TODO: 实现海报生成功能
  // 可以使用canvas生成包含文章标题、封面、二维码等信息的海报
}

// 注意：这些方法已不再使用，保留用于后续集成微信SDK时使用
// 如需集成微信分享，需要：
// 1. 在manifest.json中配置微信appid
// 2. 使用uni.share({ provider: 'weixin' })
// 3. 处理分享回调
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: var(--reading-bg-color, #ffffff);
  position: relative;
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s ease;
  animation: pageSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes pageSlideIn {
  0% {
    opacity: 0;
    transform: translateY(30rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
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
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-right {
  display: flex;
  gap: 16px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
}

.action-btn .fas {
  color: #ffffff;
  font-size: 16px;
  transition: color 0.3s ease;
}

.action-btn .fas.active {
  color: var(--reading-active-icon, #fbbf24);
}

/* 阅读内容区域样式 */
.reading-container {
  flex: 1;
  margin-top: 56px;
  margin-bottom: 56px;
  position: relative;
  display: flex;
  background-color: var(--reading-bg-color, #ffffff);
  transition: background-color 0.3s ease;
}

.content-scroll {
  flex: 1;
  height: calc(100vh - 112px); /* 减去header和bottom-toolbar的高度 */
}

.article-content {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.article-header {
  margin-bottom: 32px;
}

.article-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--reading-title-color, #1f2937);
  margin-bottom: 16px;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.article-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.article-author, .article-date {
  font-size: 14px;
  color: var(--reading-meta-color, #6b7280);
  transition: color 0.3s ease;
}

.article-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--reading-meta-color, #6b7280);
  font-size: 14px;
  transition: color 0.3s ease;
}

.stat-item .fas {
  color: var(--reading-active-icon, #3b82f6);
  transition: color 0.3s ease;
}

.article-body {
  font-family: var(--reading-font-family, system-ui, -apple-system, sans-serif);
  font-size: var(--reading-font-size, 16px);
  line-height: var(--reading-line-height, 1.8);
  color: var(--reading-text-color, #374151);
  transition: font-size 0.3s ease, color 0.3s ease, font-family 0.3s ease;
}

.lead-paragraph {
  font-size: 18px;
  color: #4b5563;
  line-height: 1.8;
  margin-bottom: 32px;
  display: block;
}

.section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
  display: block;
}

.section-image {
  width: 100%;
  border-radius: 12px;
  margin: 20px 0;
}

.image-caption {
  display: block;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 20px;
}

.paragraph {
  margin-bottom: 20px;
  display: block;
}

.quote {
  margin: 32px 0;
  padding: 20px;
  background-color: #f3f4f6;
  border-left: 4px solid #3b82f6;
  font-style: italic;
  color: #4b5563;
  font-size: 18px;
  display: block;
}

.tips-box {
  background-color: #f3f4f6;
  border-radius: 12px;
  padding: 20px;
  margin: 20px 0;
}

.tip-item {
  display: block;
  margin-bottom: 12px;
  color: #4b5563;
  font-size: 16px;
}

/* 回到顶部按钮样式 */
.back-to-top {
  position: fixed;
  right: 32rpx;
  bottom: 120rpx;
  width: 88rpx;
  height: 88rpx;
  background: var(--reading-back-to-top-bg, linear-gradient(135deg, #3b82f6, #2563eb));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 16rpx var(--reading-back-to-top-shadow, rgba(59, 130, 246, 0.3));
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 40;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.back-to-top:hover {
  transform: scale(1.1);
  box-shadow: 0 12rpx 24rpx var(--reading-back-to-top-shadow-hover, rgba(59, 130, 246, 0.4));
}

.back-to-top:active {
  transform: scale(0.95);
}

.back-to-top .fas {
  font-size: 36rpx;
  color: #ffffff;
}

/* 底部工具栏样式 */
.bottom-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background-color: var(--reading-toolbar-bg, #ffffff);
  border-top: 1px solid var(--reading-toolbar-border, #e5e7eb);
  padding: 0 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 40;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.progress-text {
  font-size: 14px;
  color: var(--reading-toolbar-text, #6b7280);
  transition: color 0.3s ease;
}

.toolbar-right {
  display: flex;
  gap: 24px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px;
}

.tool-btn .fas {
  font-size: 18px;
  color: var(--reading-toolbar-icon, #6b7280);
  transition: color 0.3s ease;
}

.tool-btn .fas.active {
  color: var(--reading-active-icon, #3b82f6);
}

.tool-text {
  font-size: 14px;
  color: var(--reading-toolbar-text, #6b7280);
  transition: color 0.3s ease;
}

/* 响应式设计 */
@media screen and (min-width: 768px) {
  .article-content {
    padding: 32px 48px;
  }

  .article-title {
    font-size: 32px;
  }

  .lead-paragraph {
    font-size: 20px;
  }

  .section-title {
    font-size: 28px;
  }

  .paragraph {
    font-size: 18px;
  }
}

@media screen and (min-width: 1024px) {
  .article-content {
    padding: 48px 64px;
  }

  .section-image {
    margin: 32px 0;
  }

  .quote {
    margin: 40px 0;
    padding: 24px;
    font-size: 20px;
  }
}

@media screen and (min-width: 1280px) {
  .article-content {
    padding: 64px 80px;
  }
}

/* 段落包装器样式 */
.paragraph-wrapper {
  position: relative;
  margin-bottom: 20px;
}

.comment-icon {
  display: inline-block;
  padding: 0 4px;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.2s ease;
  margin-left: 4px;
}

.comment-icon:hover {
  color: #3b82f6;
}

.comment-icon .fas {
  font-size: 14px;
}

/* 调整段落样式 */
.paragraph, .lead-paragraph {
  display: inline;
}

/* 新增样式 */
.loading-content, .error-content, .no-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx;
  color: #6b7280;
}

.loading-text, .error-text, .no-content-text {
  font-size: 32rpx;
  margin-bottom: 20rpx;
}

.retry-btn {
  padding: 20rpx 40rpx;
  background-color: #3b82f6;
  color: white;
  border-radius: 10rpx;
  font-size: 28rpx;
}

.content-wrapper {
  min-height: 200rpx;
}

.rich-content {
  line-height: var(--reading-line-height, 1.8);
  font-size: var(--reading-font-size, 32rpx);
  color: var(--reading-text-color, #374151);
  transition: font-size 0.3s ease, color 0.3s ease;
}

/* 富文本内容样式 */
.rich-content p {
  margin-bottom: 20rpx;
  line-height: 1.8;
}

.rich-content img {
  max-width: 100%;
  height: auto;
  margin: 20rpx 0;
  border-radius: 12rpx;
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

.paragraph-rich-text img:hover {
  transform: scale(1.02);
}

/* 段落容器样式 */
.paragraph-container {
  position: relative;
  margin-bottom: 16rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  transition: background-color 0.2s ease;
}

.paragraph-container:hover {
  background-color: var(--reading-hover-bg, rgba(0, 0, 0, 0.03));
}

.paragraph-content {
  position: relative;
}

.paragraph-rich-text {
  line-height: var(--reading-line-height, 1.8);
  font-size: var(--reading-font-size, 32rpx);
  color: var(--reading-text-color, #374151);
  transition: font-size 0.3s ease, color 0.3s ease;
}

.paragraph-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8rpx;
  opacity: 0.4;
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
  background-color: var(--reading-btn-bg, rgba(0, 0, 0, 0.05));
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  transform: scale(0.9);
}

.comment-btn:hover {
  background-color: var(--reading-btn-hover-bg, rgba(0, 0, 0, 0.08));
  border-color: var(--reading-active-icon, #3b82f6);
  transform: scale(1);
}

.comment-btn:active {
  transform: scale(0.85);
}

.comment-btn .fas {
  font-size: 20rpx;
  color: #6b7280;
  transition: color 0.2s ease;
}

.comment-btn:hover .fas {
  color: var(--reading-active-icon, #3b82f6);
}

.comment-count {
  font-size: 20rpx;
  color: #6b7280;
  font-weight: 500;
  min-width: 16rpx;
  text-align: center;
}

.comment-hint {
  font-size: 18rpx;
  color: #9ca3af;
  font-weight: 400;
}

.comment-btn:hover .comment-count,
.comment-btn:hover .comment-hint {
  color: var(--reading-active-icon, #3b82f6);
}

/* 当段落有评论时的样式 */
.paragraph-container.has-comments {
  border-left: 3px solid var(--reading-active-icon, #3b82f6);
  background-color: var(--reading-comment-bg, rgba(59, 130, 246, 0.05));
  transition: border-color 0.3s ease, background-color 0.3s ease;
}

.paragraph-container.has-comments .comment-btn {
  background-color: var(--reading-comment-btn-bg, rgba(59, 130, 246, 0.15));
  border-color: var(--reading-active-icon, #3b82f6);
  transform: scale(0.95);
}

.paragraph-container.has-comments .comment-btn .fas,
.paragraph-container.has-comments .comment-btn .comment-count {
  color: var(--reading-active-icon, #3b82f6);
}

/* 图片段落的特殊样式 */
.paragraph-container.is-image {
  padding: 4rpx 8rpx;
  margin-bottom: 8rpx;
  background-color: transparent;
  cursor: pointer;
}

.paragraph-container.is-image:hover {
  background-color: transparent;
}

.paragraph-container.is-image .paragraph-rich-text {
  text-align: center;
  position: relative;
}

.paragraph-container.is-image .paragraph-content::after {
  content: '点击查看大图';
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4rpx 8rpx;
  border-radius: 4rpx;
  font-size: 20rpx;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
}

.paragraph-container.is-image:hover .paragraph-content::after {
  opacity: 1;
}

/* 功能提示样式 */
.function-tip {
  margin-bottom: 32rpx;
  padding: 0 24rpx;
}

.tip-content {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background: var(--reading-tip-bg, linear-gradient(135deg, #3b82f6, #1d4ed8));
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx var(--reading-tip-shadow, rgba(59, 130, 246, 0.2));
  position: relative;
  transition: background 0.3s ease, box-shadow 0.3s ease;
}

.tip-content .fas {
  color: #fbbf24;
  font-size: 28rpx;
}

.tip-text {
  flex: 1;
  color: #ffffff;
  font-size: 26rpx;
  line-height: 1.5;
}

.tip-close {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  cursor: pointer;
}

.tip-close .fas {
  color: #ffffff;
  font-size: 24rpx;
}

.tip-close:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .paragraph-container {
    margin-bottom: 12rpx;
    padding: 6rpx 12rpx;
  }
  
  .comment-btn {
    padding: 4rpx 8rpx;
    gap: 2rpx;
    transform: scale(0.85);
  }
  
  .comment-btn .fas {
    font-size: 18rpx;
  }
  
  .comment-count {
    font-size: 18rpx;
  }
  
  .comment-hint {
    font-size: 16rpx;
  }
  
  .tip-content {
    padding: 16rpx 20rpx;
  }
  
  .tip-text {
    font-size: 24rpx;
  }
}
</style> 
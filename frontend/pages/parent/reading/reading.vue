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
          <view class="action-btn">
            <text class="fas fa-font"></text>
          </view>
          <view class="action-btn">
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
                  <text class="tip-text">💡 点击段落右侧的评论按钮可以对该段落进行评论，长按段落可以复制内容</text>
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

      <!-- 左右翻页指示器 -->
      <view class="page-indicators">
        <view class="page-btn prev" @click="scrollToTop" v-if="showTopBtn">
          <text class="fas fa-arrow-up"></text>
        </view>
        <view class="page-btn next" @click="scrollToBottom" v-if="showBottomBtn">
          <text class="fas fa-arrow-down"></text>
        </view>
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
import { onLoad } from '@dcloudio/uni-app'
import { contentApi, commentApi } from '@/utils/api.js'

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

// 页面加载时获取文章ID并加载数据
onLoad(async (option) => {
  console.log('阅读页面参数：', option)
  
  // 检查是否已经显示过提示
  const tipShown = uni.getStorageSync('reading_tip_shown')
  hasShownTip.value = !!tipShown
  
  if (option.id) {
    article.value.id = parseInt(option.id)
    await loadArticleData()
  } else {
    error.value = '文章ID不存在'
    console.error('缺少文章ID参数')
  }
})

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

// 交互状态
const scrollTop = ref(0)
const showTopBtn = ref(false)
const showBottomBtn = ref(true)
const isBookmarked = ref(false)
const isLiked = ref(false)
const readingProgress = ref(0)

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

// 滚动处理
const onScroll = (e) => {
  const { scrollTop: newScrollTop, scrollHeight } = e.detail
  scrollTop.value = newScrollTop
  
  // 计算阅读进度
  const clientHeight = e.detail.height
  readingProgress.value = Math.round((newScrollTop / (scrollHeight - clientHeight)) * 100)
  
  // 显示/隐藏上下按钮
  showTopBtn.value = newScrollTop > 100
  showBottomBtn.value = readingProgress.value < 95
}

// 滚动控制
const scrollToTop = () => {
  scrollTop.value = 0
}

const scrollToBottom = () => {
  const query = uni.createSelectorQuery().in(this)
  query.select('.content-scroll').boundingClientRect((data) => {
    if (data) {
      scrollTop.value = data.height
    }
  }).exec()
}

// 交互功能
const toggleBookmark = () => {
  isBookmarked.value = !isBookmarked.value
  uni.showToast({
    title: isBookmarked.value ? '已收藏' : '已取消收藏',
    icon: 'none'
  })
}

const toggleLike = () => {
  isLiked.value = !isLiked.value
  article.value.likes = isLiked.value ? '2.4k' : '2.3k'
  uni.showToast({
    title: isLiked.value ? '已点赞' : '已取消点赞',
    icon: 'none'
  })
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
        
        // 提取纯文本内容
        const textContent = innerContent.replace(/<[^>]*>/g, '').trim()
        
        // 如果段落有文本内容才添加评论功能
        if (textContent) {
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
      
      const textContent = htmlContent.replace(/<[^>]*>/g, '').trim()
      paragraphs.push({
        id: textContent ? 'para-fallback-0' : null,
        content: processedContent,
        text: textContent,
        tagName: 'div',
        commentCount: 0,
        isImage: !textContent
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
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #ffffff;
  position: relative;
  display: flex;
  flex-direction: column;
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
  color: #fbbf24;
}

/* 阅读内容区域样式 */
.reading-container {
  flex: 1;
  margin-top: 56px;
  margin-bottom: 56px;
  position: relative;
  display: flex;
  background-color: #ffffff;
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
  color: #1f2937;
  margin-bottom: 16px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.article-author, .article-date {
  font-size: 14px;
  color: #6b7280;
}

.article-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 14px;
}

.stat-item .fas {
  color: #3b82f6;
}

.article-body {
  font-size: 16px;
  line-height: 1.8;
  color: #374151;
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

/* 翻页指示器样式 */
.page-indicators {
  position: fixed;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 40;
}

.page-btn {
  width: 40px;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover {
  background-color: #3b82f6;
}

.page-btn:hover .fas {
  color: #ffffff;
}

.page-btn .fas {
  font-size: 16px;
  color: #6b7280;
}

/* 底部工具栏样式 */
.bottom-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background-color: #ffffff;
  border-top: 1px solid #e5e7eb;
  padding: 0 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 40;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.progress-text {
  font-size: 14px;
  color: #6b7280;
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
  color: #6b7280;
  transition: color 0.3s ease;
}

.tool-btn .fas.active {
  color: #3b82f6;
}

.tool-text {
  font-size: 14px;
  color: #6b7280;
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
  line-height: 1.8;
  font-size: 32rpx;
  color: #374151;
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
  background-color: #f9fafb;
}

.paragraph-content {
  position: relative;
}

.paragraph-rich-text {
  line-height: 1.8;
  font-size: 32rpx;
  color: #374151;
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
  background-color: #f3f4f6;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  transform: scale(0.9);
}

.comment-btn:hover {
  background-color: #e5e7eb;
  border-color: #3b82f6;
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
  color: #3b82f6;
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
  color: #3b82f6;
}

/* 当段落有评论时的样式 */
.paragraph-container.has-comments {
  border-left: 3px solid #3b82f6;
  background-color: #f8fafc;
}

.paragraph-container.has-comments .comment-btn {
  background-color: #dbeafe;
  border-color: #3b82f6;
  transform: scale(0.95);
}

.paragraph-container.has-comments .comment-btn .fas,
.paragraph-container.has-comments .comment-btn .comment-count {
  color: #3b82f6;
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
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.2);
  position: relative;
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
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
            <!-- 导语 -->
            <view class="paragraph-wrapper">
              <text class="lead-paragraph">在育儿的过程中，我们经常会遇到孩子闹情绪的情况。作为家长，如何在短时间内有效地安抚孩子的情绪，是一个值得深入探讨的话题。本文将从心理学的角度，为大家介绍几个实用的方法。
                <text class="comment-icon" @click.stop="navigateToComment('p0', '在育儿的过程中，我们经常会遇到孩子闹情绪的情况。作为家长，如何在短时间内有效地安抚孩子的情绪，是一个值得深入探讨的话题。本文将从心理学的角度，为大家介绍几个实用的方法。')">
                  <text class="fas fa-comments"></text>
                </text>
              </text>
            </view>

            <!-- 第一部分 -->
            <view class="section">
              <text class="section-title">理解孩子的情绪根源</text>
              <image 
                class="section-image"
                src="https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=800&auto=format&fit=crop"
                mode="widthFix"
              ></image>
              <text class="image-caption">孩子的情绪表达往往直接而真实</text>
              <view class="paragraph-wrapper">
                <text class="paragraph">首先，我们要理解孩子产生情绪的原因。通常来说，孩子的情绪波动可能源于以下几个方面：生理需求未得到满足、想要得到关注和认可、感到不安全或害怕、对环境变化的不适应等。
                  <text class="comment-icon" @click.stop="navigateToComment('p1', '首先，我们要理解孩子产生情绪的原因。通常来说，孩子的情绪波动可能源于以下几个方面：生理需求未得到满足、想要得到关注和认可、感到不安全或害怕、对环境变化的不适应等。')">
                    <text class="fas fa-comments"></text>
                  </text>
                </text>
              </view>
            </view>

            <!-- 第二部分 -->
            <view class="section">
              <text class="section-title">共情倾听的重要性</text>
              <view class="paragraph-wrapper">
                <text class="paragraph">当孩子情绪激动时，首要任务是让孩子感受到我们的理解和支持。研究表明，当孩子感受到被理解时，他们的情绪会很快得到缓解。这种方法不仅能够快速安抚孩子，还能增进亲子关系。
                  <text class="comment-icon" @click.stop="navigateToComment('p2', '当孩子情绪激动时，首要任务是让孩子感受到我们的理解和支持。研究表明，当孩子感受到被理解时，他们的情绪会很快得到缓解。这种方法不仅能够快速安抚孩子，还能增进亲子关系。')">
                    <text class="fas fa-comments"></text>
                  </text>
                </text>
              </view>
              <image 
                class="section-image"
                src="https://images.unsplash.com/photo-1516627145497-ae6968895b74?w=800&auto=format&fit=crop"
                mode="widthFix"
              ></image>
              <text class="image-caption">通过倾听和陪伴建立情感连接</text>
            </view>

            <!-- 第三部分 -->
            <view class="section">
              <text class="section-title">有效的转移注意力技巧</text>
              <text class="quote">"转移注意力不是逃避问题，而是给孩子一个情绪缓冲的机会。"</text>
              <text class="paragraph">在孩子情绪稍微平静后，我们可以通过以下方式转移他们的注意力：讲一个有趣的小故事、提出一个简单的游戏建议、展示一个新奇的物品等。这些方法都能有效地帮助孩子走出负面情绪。</text>
              <image 
                class="section-image"
                src="https://images.unsplash.com/photo-1596464716127-f2a82984de30?w=800&auto=format&fit=crop"
                mode="widthFix"
              ></image>
              <text class="image-caption">通过游戏和互动转移注意力</text>
            </view>

            <!-- 第四部分 -->
            <view class="section">
              <text class="section-title">情绪表达引导</text>
              <image 
                class="section-image"
                src="https://images.unsplash.com/photo-1536640712-4d4c36ff0e4e?w=800&auto=format&fit=crop"
                mode="widthFix"
              ></image>
              <text class="image-caption">引导孩子学会表达自己的感受</text>
              <text class="paragraph">教会孩子正确表达情绪是很重要的。我们可以通过以下方式帮助孩子：</text>
              <view class="tips-box">
                <text class="tip-item">• 帮助孩子说出感受："你是不是觉得很生气？"</text>
                <text class="tip-item">• 教导表达方式："生气的时候可以告诉妈妈"</text>
                <text class="tip-item">• 示范情绪管理："妈妈有时也会生气，但是..."</text>
              </view>
            </view>

            <!-- 第五部分 -->
            <view class="section">
              <text class="section-title">建立情绪安全区</text>
              <text class="paragraph">在家中设置一个专门的区域，让孩子在情绪波动时可以安静地待一会，做一些深呼吸，抱着心爱的玩具，或者画画发泄情绪。这个安全区域能帮助孩子学会自我调节。</text>
              <image 
                class="section-image"
                src="https://images.unsplash.com/photo-1577896851231-70ef18881754?w=800&auto=format&fit=crop"
                mode="widthFix"
              ></image>
              <text class="image-caption">为孩子创造安全舒适的情绪释放空间</text>
            </view>

            <!-- 总结部分 -->
            <view class="section">
              <text class="section-title">总结与建议</text>
              <text class="paragraph">安抚孩子情绪是一个需要耐心和技巧的过程。记住以下几点：保持冷静和耐心、给予足够的理解和支持、采用适合孩子年龄的方法、保持一致性和持续性。最后，每个孩子都是独特的个体，需要我们用心观察，找到最适合的方法。</text>
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

// 文章信息
const article = ref({
  title: '如何在3分钟内安抚孩子的小情绪',
  author: '育儿专家 王老师',
  date: '2024-03-20',
  views: '2.1万',
  likes: '2.3k',
  comments: '128'
})

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
  uni.navigateTo({
    url: `/pages/parent/reading/paragraph-comment?id=${paragraphId}&content=${encodeURIComponent(content)}`
  })
}

// 跳转到文章评论页面
const navigateToComments = () => {
  uni.navigateTo({
    url: '/pages/parent/reading/article-comments'
  })
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
</style> 
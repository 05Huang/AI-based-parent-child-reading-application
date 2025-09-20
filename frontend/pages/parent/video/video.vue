<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <image src="https://ui-avatars.com/api/?name=爸爸&background=3b82f6&color=fff&size=128" class="avatar" @click="navigateToProfile"></image>
        </view>
        <view class="search-box">
          <view class="search-input-wrapper" @click="navigateToSearch">
            <text class="fas fa-search search-icon"></text>
            <input 
              type="text" 
              placeholder="搜索感兴趣的视频" 
              placeholder-class="search-placeholder" 
              class="input"
              disabled
            />
          </view>
        </view>
        <view class="header-right">
          <view class="icon-wrapper" @click="navigateToNotification">
            <text class="fas fa-envelope icon"></text>
          </view>
        </view>
      </view>
      <!-- 分类标签 -->
      <scroll-view 
        scroll-x="true" 
        class="category-container" 
        :show-scrollbar="false"
        :scroll-into-view="activeTab"
        @scroll="handleScroll"
        :scroll-left="scrollLeft"
        :scroll-with-animation="true"
        :scroll-animation-duration="300"
        ref="scrollView"
      >
        <view class="tabs-wrapper">
          <view 
            v-for="(tab, index) in tabs" 
            :key="index"
            :id="'tab-' + index"
            class="tab"
            :class="{ active: currentTab === index }"
            @click="switchTab(index)"
          >
            {{ tab.name }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 推荐视频 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">推荐视频</text>
          <view class="more-link">
            <text class="more-text">更多</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="video-card featured">
          <view class="video-cover">
            <image src="https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60" 
                   mode="aspectFill"></image>
            <text class="video-duration">05:24</text>
            <view class="video-play-button">
              <text class="fas fa-play"></text>
            </view>
            <view class="video-info-overlay">
              <text class="video-title">《哈利·波特》系列的魔法世界解析</text>
              <view class="video-stats">
                <text class="views">3.2万观看</text>
                <text class="dot">·</text>
                <text class="time">2小时前</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 热门视频 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">热门视频</text>
          <view class="refresh-link">
            <text class="refresh-text">换一换</text>
            <text class="fas fa-sync-alt"></text>
          </view>
        </view>
        <view class="video-list">
          <!-- 视频项 1 -->
          <view class="video-item">
            <view class="video-thumbnail">
              <image src="https://images.unsplash.com/photo-1551029506-0807df4e2031?w=500&auto=format&fit=crop&q=60" 
                     mode="aspectFill"></image>
              <text class="video-duration">03:45</text>
            </view>
            <view class="video-content">
              <text class="video-title">如何提高阅读理解能力 - 实用技巧分享</text>
              <view class="video-metrics">
                <view class="metric">
                  <text class="fas fa-play-circle"></text>
                  <text class="metric-value">2.1万</text>
                </view>
                <view class="metric">
                  <text class="fas fa-comment"></text>
                  <text class="metric-value">328</text>
                </view>
              </view>
              <view class="creator-info">
                <image class="creator-avatar" 
                       src="https://api.dicebear.com/7.x/avataaars/svg?seed=creator1"
                       mode="aspectFill"></image>
                <text class="creator-name">阅读达人</text>
              </view>
            </view>
          </view>

          <!-- 视频项 2 -->
          <view class="video-item">
            <view class="video-thumbnail">
              <image src="https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=500&auto=format&fit=crop&q=60" 
                     mode="aspectFill"></image>
              <text class="video-duration">04:12</text>
            </view>
            <view class="video-content">
              <text class="video-title">《小王子》中的哲学思考 - 不只是童话</text>
              <view class="video-metrics">
                <view class="metric">
                  <text class="fas fa-play-circle"></text>
                  <text class="metric-value">1.8万</text>
                </view>
                <view class="metric">
                  <text class="fas fa-comment"></text>
                  <text class="metric-value">256</text>
                </view>
              </view>
              <view class="creator-info">
                <image class="creator-avatar" 
                       src="https://api.dicebear.com/7.x/avataaars/svg?seed=creator2"
                       mode="aspectFill"></image>
                <text class="creator-name">文学研究社</text>
              </view>
            </view>
          </view>

          <!-- 视频项 3 -->
          <view class="video-item">
            <view class="video-thumbnail">
              <image src="https://images.unsplash.com/photo-1589998059171-988d887df646?w=500&auto=format&fit=crop&q=60" 
                     mode="aspectFill"></image>
              <text class="video-duration">06:30</text>
            </view>
            <view class="video-content">
              <text class="video-title">深海探秘：海洋生物的奇妙世界</text>
              <view class="video-metrics">
                <view class="metric">
                  <text class="fas fa-play-circle"></text>
                  <text class="metric-value">3.5万</text>
                </view>
                <view class="metric">
                  <text class="fas fa-comment"></text>
                  <text class="metric-value">512</text>
                </view>
              </view>
              <view class="creator-info">
                <image class="creator-avatar" 
                       src="https://api.dicebear.com/7.x/avataaars/svg?seed=creator3"
                       mode="aspectFill"></image>
                <text class="creator-name">科学探索</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 为你推荐 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">为你推荐</text>
          <text class="refresh-text">换一批</text>
        </view>
        <view class="recommended-grid">
          <!-- 推荐视频 1 -->
          <view class="recommended-card">
            <view class="video-cover">
              <image src="https://images.unsplash.com/photo-1633477189729-9290b3261d0a?w=500&auto=format&fit=crop&q=60" 
                     mode="aspectFill"></image>
              <text class="video-duration">03:18</text>
              <view class="video-info-overlay">
                <text class="video-title">《夏洛的网》：友谊的力量</text>
                <view class="video-stats">
                  <text class="fas fa-play-circle"></text>
                  <text class="views">1.2万</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 推荐视频 2 -->
          <view class="recommended-card">
            <view class="video-cover">
              <image src="https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=500&auto=format&fit=crop&q=60" 
                     mode="aspectFill"></image>
              <text class="video-duration">04:05</text>
              <view class="video-info-overlay">
                <text class="video-title">太阳系行星探索：神秘的土星</text>
                <view class="video-stats">
                  <text class="fas fa-play-circle"></text>
                  <text class="views">2.3万</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部导航栏 -->
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';

// 页面加载时检查登录状态
onMounted(() => {
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.redirectTo({
      url: '/pages/parent/login/login'
    });
  }
});

// 跳转到个人中心页面
const navigateToProfile = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  });
};

// 跳转到消息通知页面
const navigateToNotification = () => {
  uni.navigateTo({
    url: '/pages/parent/notification/notification'
  });
};

// 跳转到搜索页面
const navigateToSearch = () => {
  // 添加触觉反馈
  uni.vibrateShort({
    success: function () {
      // 跳转到搜索页面
      uni.navigateTo({
        url: '/pages/parent/search/search',
        animationType: 'fade-in',
        animationDuration: 200,
        success: () => {
          // 可以在这里添加跳转成功后的回调
        }
      });
    }
  });
};

// 导航标签数据
const tabs = ref([
  { name: '全部', id: 'all' },
  { name: '故事解读', id: 'story' },
  { name: '科普知识', id: 'science' },
  { name: '名著赏析', id: 'classics' },
  { name: '阅读技巧', id: 'skills' },
  { name: '亲子共读', id: 'reading' },
  { name: '英语启蒙', id: 'english' }
]);

// 当前选中的标签索引
const currentTab = ref(0);
// 滚动位置
const scrollLeft = ref(0);
// scroll-view 引用
const scrollView = ref(null);

// 切换标签
const switchTab = async (index) => {
  if (currentTab.value === index) return;
  
  currentTab.value = index;
  
  // 等待DOM更新
  await nextTick();
  
  // 获取被点击的标签元素
  const query = uni.createSelectorQuery().in(this);
  query.select(`#tab-${index}`).boundingClientRect();
  query.select('.category-container').boundingClientRect();
  
  query.exec((res) => {
    if (!res[0] || !res[1]) return;
    
    const tabElement = res[0];
    const scrollView = res[1];
    
    // 计算需要滚动的距离
    // 目标：将选中的标签滚动到最左侧
    const newScrollLeft = tabElement.left - scrollView.left;
    
    // 设置滚动位置（会触发动画）
    scrollLeft.value = newScrollLeft;
  });
};

// 监听滚动事件
const handleScroll = (e) => {
  scrollLeft.value = e.detail.scrollLeft;
};
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
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
  align-items: center;
  padding: 8px 16px;
  box-sizing: border-box;
  width: 100%;
  height: 56px;
}

.header-left {
  margin-right: 12px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.search-box {
  flex: 1;
  margin: 0 12px;
  height: 36px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 0 12px;
  height: 100%;
  cursor: pointer;
}

.search-icon {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  width: 14px;
  text-align: center;
}

.input {
  flex: 1;
  margin: 0 8px;
  color: white;
  font-size: 14px;
  height: 100%;
  line-height: 36px;
  pointer-events: none;
}

.search-placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.header-right {
  display: flex;
  align-items: center;
}

.icon-wrapper {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-size: 20px;
  color: #ffffff;
}

/* 分类标签样式 */
.category-container {
  width: 100%;
  background-color: #f3f4f6;
  padding: 8px 0;
  white-space: nowrap;
  border-bottom: 1px solid #e5e7eb;
  overflow: hidden;
}

.category-container ::-webkit-scrollbar {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}

.tabs-wrapper {
  display: inline-flex;
  padding: 0 16px;
  gap: 12px;
  transition: transform 0.3s ease;
}

.tab {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 14px;
  font-size: 13px;
  color: #6b7280;
  background-color: #ffffff;
  transition: all 0.3s ease;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
}

.tab::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background-color: #3b82f6;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.tab.active {
  background-color: #3b82f6;
  color: white;
  transform: scale(1.05);
}

.tab.active::after {
  width: 50%;
}

/* 主要内容区域样式 */
.main-content {
  margin-top: 96px; /* 56px + 40px */
  padding: 12px;
  box-sizing: border-box;
  width: 100%;
}

.section {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.more-link, .refresh-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 14px;
}

.more-link .fas, .refresh-link .fas {
  font-size: 12px;
}

/* 视频卡片样式 */
.video-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background-color: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.video-cover {
  position: relative;
  width: 100%;
  height: 180px;
}

.video-cover image {
  width: 100%;
  height: 100%;
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.video-play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 50px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-play-button .fas {
  color: #ffffff;
  font-size: 20px;
}

.video-info-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
  padding: 20px 10px 10px;
}

.video-info-overlay .video-title {
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.video-stats {
  display: flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
}

.dot {
  margin: 0 4px;
}

/* 视频列表样式 */
.video-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.video-item {
  display: flex;
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  padding: 8px;
}

.video-thumbnail {
  position: relative;
  width: 140px;
  height: 84px;
  flex-shrink: 0;
}

.video-thumbnail image {
  width: 100%;
  height: 100%;
}

.video-content {
  flex: 1;
  padding: 8px;
  display: flex;
  flex-direction: column;
}

.video-content .video-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-metrics {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 12px;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.creator-avatar {
  width: 16px;
  height: 16px;
  border-radius: 50%;
}

.creator-name {
  font-size: 12px;
  color: #6b7280;
}

/* 推荐网格样式 */
.recommended-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.recommended-card {
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.recommended-card .video-cover {
  height: 100px;
}
</style> 
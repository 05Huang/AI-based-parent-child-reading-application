<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <view class="header-left">
          <image 
            :src="currentUser?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser?.nickname || '用户')}&background=3b82f6&color=fff&size=128`" 
            class="avatar" 
            @click="navigateToProfile"
          ></image>
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
    <scroll-view 
      scroll-y="true" 
      class="main-content"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 推荐视频 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">推荐视频</text>
          <view class="more-link" @click="navigateToAllVideos">
            <text class="more-text">更多</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="video-card featured" @click="navigateToVideoPlayer(featuredVideo.id)" v-if="featuredVideo.id">
          <view class="video-cover">
            <image :src="featuredVideo.coverUrl || 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60'" 
                   mode="aspectFill"></image>
            <text class="video-duration">{{ formatDuration(featuredVideo.contentLength) }}</text>
            <view class="video-play-button">
              <text class="fas fa-play"></text>
            </view>
            <view class="video-info-overlay">
              <text class="video-title">{{ featuredVideo.title || '推荐视频' }}</text>
              <view class="video-stats">
                <text class="views">{{ formatViewCount(featuredVideo.viewCount) }}观看</text>
                <text class="dot">·</text>
                <text class="time">{{ formatTime(featuredVideo.createdTime) }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="video-card featured" v-else>
          <view class="video-cover">
            <view class="loading-placeholder">
              <text class="loading-text">加载中...</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 热门视频 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">热门视频</text>
          <view class="refresh-link" @click="refreshHotVideos">
            <text class="refresh-text">换一换</text>
            <text class="fas fa-sync-alt"></text>
          </view>
        </view>
        <view class="video-list">
          <!-- 动态渲染热门视频 -->
          <view 
            class="video-item" 
            v-for="video in hotVideos" 
            :key="video.id"
            @click="navigateToVideoPlayer(video.id)"
          >
            <view class="video-thumbnail">
              <image :src="video.coverUrl || 'https://images.unsplash.com/photo-1551029506-0807df4e2031?w=500&auto=format&fit=crop&q=60'" 
                     mode="aspectFill"></image>
              <text class="video-duration">{{ formatDuration(video.contentLength) }}</text>
            </view>
            <view class="video-content">
              <text class="video-title">{{ video.title || '视频标题' }}</text>
              <view class="video-metrics">
                <view class="metric">
                  <text class="fas fa-play-circle"></text>
                  <text class="metric-value">{{ formatViewCount(video.viewCount) }}</text>
                </view>
                <view class="metric">
                  <text class="fas fa-comment"></text>
                  <text class="metric-value">{{ video.commentCount || 0 }}</text>
                </view>
              </view>
              <view class="creator-info">
                <image class="creator-avatar" 
                       :src="video.creatorAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + (video.creatorName || 'creator')"
                       mode="aspectFill"></image>
                <text class="creator-name">{{ video.creatorName || '内容创作者' }}</text>
              </view>
            </view>
          </view>
          
          <!-- 加载状态 -->
          <view class="video-item" v-if="hotVideos.length === 0 && !loading">
            <view class="video-thumbnail">
              <view class="loading-placeholder">
                <text class="loading-text">暂无热门视频</text>
              </view>
            </view>
            <view class="video-content">
              <text class="video-title">正在加载热门视频...</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 为你推荐 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">为你推荐</text>
          <text class="refresh-text" @click="refreshRecommendedVideos">换一批</text>
        </view>
        <view class="recommended-grid">
          <!-- 动态渲染推荐视频 -->
          <view 
            class="recommended-card" 
            v-for="video in recommendedVideos" 
            :key="video.id"
            @click="navigateToVideoPlayer(video.id)"
          >
            <view class="video-cover">
              <image :src="video.coverUrl || 'https://images.unsplash.com/photo-1633477189729-9290b3261d0a?w=500&auto=format&fit=crop&q=60'" 
                     mode="aspectFill"></image>
              <text class="video-duration">{{ formatDuration(video.contentLength) }}</text>
              <view class="video-info-overlay">
                <text class="video-title">{{ video.title || '推荐视频' }}</text>
                <view class="video-stats">
                  <text class="fas fa-play-circle"></text>
                  <text class="views">{{ formatViewCount(video.viewCount) }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 占位卡片 -->
          <view class="recommended-card" v-if="recommendedVideos.length < 2">
            <view class="video-cover">
              <view class="loading-placeholder">
                <text class="loading-text">更多精彩视频即将上线</text>
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
import { videoApi, categoryApi, userApi } from '../../../utils/api.js';

// 加载所有视频数据
const loadAllVideoData = () => {
  console.log('[视频页面] 加载所有视频数据');
  loadCategories();
  loadFeaturedVideo();
  loadHotVideos();
  loadRecommendedVideos();
};

// 初始化页面数据
const initPageData = async () => {
  console.log('[视频页面] 初始化页面数据');
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.redirectTo({
      url: '/pages/parent/login/login'
    });
    return;
  }
  
  // 获取当前用户信息
  try {
    const userResponse = await userApi.getCurrentUser();
    if (userResponse && userResponse.data) {
      currentUser.value = userResponse.data;
      console.log('获取到当前用户信息：', currentUser.value);
    }
  } catch (error) {
    console.error('获取用户信息失败：', error);
  }
  
  // 加载页面数据
  loadAllVideoData();
};

// 页面加载时检查登录状态
onMounted(async () => {
  console.log('[视频页面] 页面已挂载');
  await initPageData();
});

// 下拉刷新
const onRefresh = async () => {
  console.log('[视频页面] 下拉刷新');
  isRefreshing.value = true;
  try {
    loadAllVideoData();
    uni.showToast({
      title: '刷新成功',
      icon: 'success',
      duration: 1000
    });
  } catch (error) {
    console.error('刷新失败：', error);
  } finally {
    // 延迟关闭刷新状态，确保动画完整
    setTimeout(() => {
      isRefreshing.value = false;
    }, 500);
  }
};

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

// 数据状态
const tabs = ref([{ name: '全部', id: null }]); // 默认包含"全部"选项
const currentTab = ref(0);
const scrollLeft = ref(0);
const scrollView = ref(null);

// 视频数据
const featuredVideo = ref({});
const hotVideos = ref([]);
const recommendedVideos = ref([]);
const loading = ref(false);
const isRefreshing = ref(false); // 下拉刷新状态

// 当前用户信息
const currentUser = ref(null);

// 加载分类数据
const loadCategories = async () => {
  try {
    console.log('开始加载视频分类');
    const response = await categoryApi.getAllActiveCategories();
    
    if (response.code === 200 && response.data) {
      // 保留"全部"选项，然后添加其他分类
      const allCategories = [
        { name: '全部', id: null },
        ...response.data.map(category => ({
          name: category.name,
          id: category.id
        }))
      ];
      tabs.value = allCategories;
      console.log('分类加载成功，共', tabs.value.length, '个分类');
    }
  } catch (error) {
    console.error('加载分类失败：', error);
  }
};

// 加载推荐视频（随机）
const loadFeaturedVideo = async () => {
  try {
    console.log('开始加载推荐视频（随机）');
    const firstResponse = await videoApi.getVideoPage({
      current: 1,
      size: 10,
      sortField: 'created_time',
      sortOrder: 'desc'
    });
    
    if (firstResponse.code === 200 && firstResponse.data) {
      const totalPages = firstResponse.data.pages || 1;
      const randomPage = totalPages > 1 ? Math.floor(Math.random() * Math.min(totalPages, 3)) + 1 : 1;
      
      const response = randomPage === 1 ? firstResponse : await videoApi.getVideoPage({
        current: randomPage,
        size: 10,
        sortField: 'created_time',
        sortOrder: 'desc'
      });
      
      if (response.data && response.data.records && response.data.records.length > 0) {
        const videos = response.data.records.filter(item => item.type === 2);
        const shuffled = videos.sort(() => Math.random() - 0.5);
        if (shuffled.length > 0) {
          featuredVideo.value = shuffled[0];
          console.log('推荐视频加载成功（随机）：', featuredVideo.value.title);
        }
      }
    }
  } catch (error) {
    console.error('加载推荐视频失败：', error);
  }
};

// 加载热门视频（随机）
const loadHotVideos = async () => {
  try {
    console.log('开始加载热门视频（随机）');
    const firstResponse = await videoApi.getVideoPage({
      current: 1,
      size: 10,
      sortField: 'view_count',
      sortOrder: 'desc'
    });
    
    if (firstResponse.code === 200 && firstResponse.data) {
      const totalPages = firstResponse.data.pages || 1;
      const randomPage = totalPages > 1 ? Math.floor(Math.random() * Math.min(totalPages, 5)) + 1 : 1;
      
      const response = randomPage === 1 ? firstResponse : await videoApi.getVideoPage({
        current: randomPage,
        size: 10,
        sortField: 'view_count',
        sortOrder: 'desc'
      });
      
      if (response.data && response.data.records && response.data.records.length > 0) {
        const videos = response.data.records.filter(item => item.type === 2);
        const shuffled = videos.sort(() => Math.random() - 0.5);
        hotVideos.value = shuffled.slice(0, 3);
        console.log('热门视频加载成功（随机），共', hotVideos.value.length, '个');
      }
    }
  } catch (error) {
    console.error('加载热门视频失败：', error);
  }
};

// 加载推荐视频列表（随机）
const loadRecommendedVideos = async () => {
  try {
    console.log('开始加载推荐视频列表（随机）');
    const firstResponse = await videoApi.getVideoPage({
      current: 1,
      size: 8,
      sortField: 'created_time',
      sortOrder: 'desc'
    });
    
    if (firstResponse.code === 200 && firstResponse.data) {
      const totalPages = firstResponse.data.pages || 1;
      const randomPage = totalPages > 1 ? Math.floor(Math.random() * Math.min(totalPages, 5)) + 1 : 1;
      
      const response = randomPage === 1 ? firstResponse : await videoApi.getVideoPage({
        current: randomPage,
        size: 8,
        sortField: 'created_time',
        sortOrder: 'desc'
      });
      
      if (response.data && response.data.records && response.data.records.length > 0) {
        const videos = response.data.records.filter(item => item.type === 2);
        const shuffled = videos.sort(() => Math.random() - 0.5);
        recommendedVideos.value = shuffled.slice(0, 2);
        console.log('推荐视频列表加载成功（随机），共', recommendedVideos.value.length, '个');
      }
    }
  } catch (error) {
    console.error('加载推荐视频列表失败：', error);
  }
};

// 切换标签
const switchTab = async (index) => {
  if (currentTab.value === index) return;
  
  console.log('切换到分类标签：', tabs.value[index].name);
  currentTab.value = index;
  
  // 根据分类加载视频
  await loadVideosByCategory(tabs.value[index].id);
  
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

// 根据分类加载视频
const loadVideosByCategory = async (categoryId) => {
  try {
    loading.value = true;
    console.log('根据分类加载视频，分类ID：', categoryId);
    
    let response;
    if (categoryId) {
      // 特定分类的视频
      response = await videoApi.getVideosByCategory(categoryId, 20);
    } else {
      // 全部视频
      response = await videoApi.getVideoPage({
        current: 1,
        size: 20,
        sortField: 'created_time',
        sortOrder: 'desc'
      });
    }
    
    if (response.code === 200 && response.data) {
      // 更新热门视频和推荐视频
      const videos = (response.data.records || response.data).filter(item => item.type === 2);
      if (videos.length > 0) {
        // 更新推荐视频
        if (videos.length > 0) {
          featuredVideo.value = videos[0];
        }
        // 更新热门视频
        hotVideos.value = videos.slice(1, 4);
        // 更新推荐视频列表
        recommendedVideos.value = videos.slice(4, 6);
        console.log('分类视频加载成功');
      }
    }
  } catch (error) {
    console.error('加载分类视频失败：', error);
  } finally {
    loading.value = false;
  }
};

// 刷新热门视频
const refreshHotVideos = async () => {
  console.log('刷新热门视频');
  await loadHotVideos();
  uni.showToast({
    title: '已刷新',
    icon: 'success',
    duration: 1000
  });
};

// 刷新推荐视频
const refreshRecommendedVideos = async () => {
  console.log('刷新推荐视频');
  await loadRecommendedVideos();
  uni.showToast({
    title: '已刷新',
    icon: 'success',
    duration: 1000
  });
};

// 跳转到视频播放页面
const navigateToVideoPlayer = (videoId) => {
  console.log('跳转到视频播放页面，视频ID：', videoId);
  uni.navigateTo({
    url: `/pages/parent/video/video-player?id=${videoId}`
  });
};

// 跳转到全部视频页面
const navigateToAllVideos = () => {
  uni.navigateTo({
    url: '/pages/parent/video/all-videos'
  });
};

// 格式化播放数量
const formatViewCount = (count) => {
  if (!count) return '0';
  if (count < 1000) return count.toString();
  if (count < 10000) return (count / 1000).toFixed(1) + 'K';
  return (count / 10000).toFixed(1) + 'W';
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return Math.floor(diff / 86400000) + '天前';
};

// 格式化视频时长
const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
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
  height: calc(100vh - 96px);
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
  color: #3b82f6;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.more-link:hover, .refresh-link:hover {
  color: #1d4ed8;
}

.more-link:active, .refresh-link:active {
  opacity: 0.7;
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

/* 加载占位符样式 */
.loading-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
}

.loading-text {
  color: #9ca3af;
  font-size: 12px;
  text-align: center;
}
</style> 
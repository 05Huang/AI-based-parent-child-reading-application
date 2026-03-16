<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="header-left">
          <image 
            :src="currentUser?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser?.nickname || '用户')}&background=8b5cf6&color=fff&size=128`" 
            class="avatar" 
            @tap="navigateToProfile"
            mode="aspectFill"
          ></image>
        </view>
        <view class="search-box">
          <view class="search-input-wrapper" @tap="navigateToSearch">
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
          <view class="icon-wrapper" @tap="navigateToMessage">
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
      >
        <view class="tabs-wrapper">
          <view 
            v-for="(tab, index) in tabs" 
            :key="index"
            :id="'tab-' + index"
            class="tab"
            :class="{ active: currentTab === index }"
            @tap="switchCategory(index)"
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
      :style="{ marginTop: headerHeight + 'px', height: 'calc(100vh - ' + (headerHeight + 60) + 'px)' }"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 推荐视频 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">推荐视频</text>
          <view class="more-link" @tap="navigateToAllVideos">
            <text class="more-text">更多</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="video-card featured" @tap="navigateToVideoPlayer(featuredVideo.id)" v-if="featuredVideo.id">
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
          <view class="refresh-link" @tap="refreshHotVideos">
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
            @tap="navigateToVideoPlayer(video.id)"
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
                       :src="officialAvatar"
                       mode="aspectFill"></image>
                <text class="creator-name">{{ officialDisplayName }}</text>
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
          <text class="refresh-text" @tap="refreshRecommendedVideos">换一批</text>
        </view>
        <view class="recommended-grid">
          <!-- 动态渲染推荐视频 -->
          <view 
            class="recommended-card" 
            v-for="video in recommendedVideos" 
            :key="video.id"
            @tap="navigateToVideoPlayer(video.id)"
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
    <view class="tab-bar">
      <view class="tab-item" @tap="switchTab('/pages/child/home')">
        <text class="fas fa-home"></text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/reading')">
        <text class="fas fa-book"></text>
        <text class="tab-text">阅读</text>
      </view>
      <view class="tab-item active" @tap="switchTab('/pages/child/video')">
        <text class="fas fa-video"></text>
        <text class="tab-text">视频</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/chat')">
        <text class="fas fa-comments"></text>
        <text class="tab-text">对话</text>
      </view>
      <view class="tab-item" @tap="switchTab('/pages/child/profile')">
        <text class="fas fa-user"></text>
        <text class="tab-text">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue';
import { videoApi, categoryApi, userApi, searchApi, recommendationApi } from '@/utils/api.js';

// 状态变量
const statusBarHeight = ref(20);
const headerHeight = ref(100);
const currentUser = ref(null);
const OFFICIAL_USER_ID = -999;
const officialUser = ref(null);
const tabs = ref([{ name: '全部', id: null }]);
const currentTab = ref(0);
const scrollLeft = ref(0);
const featuredVideo = ref({});
const hotVideos = ref([]);
const recommendedVideos = ref([]);
const loading = ref(false);
const isRefreshing = ref(false);
const categoryVideoPool = ref([]);

const officialDisplayName = computed(() => {
  return officialUser.value?.nickname || officialUser.value?.username || '阅读小助手';
});

const officialAvatar = computed(() => {
  return officialUser.value?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(officialDisplayName.value)}&background=8b5cf6&color=fff&size=128`;
});

const getCurrentUserId = () => {
  return currentUser.value?.id || uni.getStorageSync('currentUserId') || uni.getStorageSync('userId') || null;
};

const getCurrentCategoryId = () => {
  return tabs.value[currentTab.value]?.id ?? null;
};

const getCurrentTab = () => {
  return tabs.value[currentTab.value] || { name: '全部', id: null };
};

const getCurrentKeyword = () => {
  const tab = getCurrentTab();
  return tab.name && tab.name !== '全部' ? tab.name : '';
};

const shuffleVideos = (videos) => {
  return videos.slice().sort(() => Math.random() - 0.5);
};

const applyPoolToSections = (pool) => {
  if (!pool || pool.length === 0) {
    featuredVideo.value = {};
    hotVideos.value = [];
    recommendedVideos.value = [];
    return;
  }

  const shuffled = shuffleVideos(pool);
  featuredVideo.value = shuffled[0] || {};
  hotVideos.value = shuffled.slice(1, 4);
  recommendedVideos.value = shuffled.slice(4, 6);
};

const getExistingVideoIds = () => {
  const ids = new Set();
  if (featuredVideo.value && featuredVideo.value.id) {
    ids.add(featuredVideo.value.id);
  }
  hotVideos.value.forEach((video) => {
    if (video && video.id) {
      ids.add(video.id);
    }
  });
  recommendedVideos.value.forEach((video) => {
    if (video && video.id) {
      ids.add(video.id);
    }
  });
  return ids;
};

// 初始化页面
onMounted(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 20;
  // 计算页眉高度：状态栏 + 头部内容(56px) + 分类标签(44px)
  headerHeight.value = statusBarHeight.value + 56 + 44;
  
  initPageData();
});

const checkLoginStatus = () => {
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.redirectTo({ url: '/pages/child/login/login' });
    return false;
  }
  loadUserInfo();
  return true;
};

const loadUserInfo = async () => {
  try {
    const storedUserInfo = uni.getStorageSync('userInfo');
    if (storedUserInfo) {
      currentUser.value = storedUserInfo;
    }
    
    const response = await userApi.getCurrentUser();
    if (response && response.data) {
      currentUser.value = response.data;
      uni.setStorageSync('userInfo', response.data);
    }
  } catch (error) {
    console.error('获取用户信息失败：', error);
  }
};

const loadOfficialUser = async () => {
  try {
    const response = await userApi.getUserById(OFFICIAL_USER_ID);
    if (response && response.data) {
      officialUser.value = response.data;
      return;
    }
  } catch (error) {
    console.error('获取官方用户信息失败：', error);
  }
  officialUser.value = { id: OFFICIAL_USER_ID, nickname: '阅读小助手' };
};

const initPageData = async () => {
  if (!checkLoginStatus()) return;
  await loadOfficialUser();
  await loadCategories();
  await loadVideosForCurrentTab();
};

const loadAllVideoData = async (shuffle = false) => {
  await loadVideosForCurrentTab(shuffle);
};

const onRefresh = async () => {
  isRefreshing.value = true;
  try {
    await loadAllVideoData(true);
    uni.showToast({ title: '刷新成功', icon: 'success' });
  } catch (error) {
    console.error('刷新失败：', error);
  } finally {
    setTimeout(() => {
      isRefreshing.value = false;
    }, 500);
  }
};

const loadCategories = async () => {
  try {
    const response = await categoryApi.getAllActiveCategories();
    if (response.code === 200 && response.data) {
      tabs.value = [
        { name: '全部', id: null },
        ...response.data.map(category => ({
          name: category.name,
          id: category.id
        }))
      ];
    }
  } catch (error) {
    console.error('加载分类失败：', error);
  }
};

const switchCategory = async (index) => {
  if (currentTab.value === index) return;
  
  currentTab.value = index;
  await loadVideosForCurrentTab();
  
  await nextTick();
  
  // 简单模拟滚动计算
  // 在 script setup 中获取 query 需要用 uni.createSelectorQuery
  const query = uni.createSelectorQuery();
  query.select(`#tab-${index}`).boundingClientRect();
  query.select('.category-container').boundingClientRect();
  query.exec((res) => {
    if (res[0] && res[1]) {
      scrollLeft.value = res[0].left - res[1].left;
    }
  });
};

const loadVideosByCategory = async (categoryId) => {
  loading.value = true;
  try {
    const response = await videoApi.getVideosByCategory(categoryId, 50);
    if (response.code === 200 && response.data) {
      const pool = (Array.isArray(response.data) ? response.data : (response.data.records || [])).filter(item => item.type === 2);
      categoryVideoPool.value = pool;
      applyPoolToSections(pool);
      return;
    }

    categoryVideoPool.value = [];
    applyPoolToSections([]);
  } catch (error) {
    console.error('加载分类视频失败：', error);
    categoryVideoPool.value = [];
    applyPoolToSections([]);
  } finally {
    loading.value = false;
  }
};

const loadVideosByKeyword = async (keyword) => {
  loading.value = true;
  try {
    const response = await searchApi.searchContent(keyword, 1, 50, 2);
    if (response.code === 200 && response.data) {
      const pool = (response.data.records || []).filter(item => item.type === 2);
      categoryVideoPool.value = pool;
      applyPoolToSections(pool);
      return pool.length;
    }

    categoryVideoPool.value = [];
    applyPoolToSections([]);
    return 0;
  } catch (error) {
    console.error('关键词加载视频失败：', error);
    categoryVideoPool.value = [];
    applyPoolToSections([]);
    return 0;
  } finally {
    loading.value = false;
  }
};

const loadVideosForCurrentTab = async (shuffle = false) => {
  const keyword = getCurrentKeyword();
  if (keyword) {
    const matched = await loadVideosByKeyword(keyword);
    if (matched > 0) return;

    const categoryId = getCurrentCategoryId();
    if (categoryId) {
      await loadVideosByCategory(categoryId);
      return;
    }
  }

  categoryVideoPool.value = [];
  await Promise.all([loadFeaturedVideo(shuffle), loadHotVideos(shuffle), loadRecommendedVideos(shuffle)]);
};

const loadFeaturedVideo = async (shuffle = false) => {
  const userId = getCurrentUserId();
  try {
    if (userId) {
      const response = await recommendationApi.getVideoRecommendations(userId, 1, shuffle);
      if (response && Array.isArray(response.data) && response.data.length > 0) {
        const video = response.data.find(item => item && item.type === 2) || response.data[0];
        if (video && video.id) {
          featuredVideo.value = video;
          return;
        }
      }
    }
  } catch (error) {
    console.error('加载推荐视频失败（Python 推荐），使用随机方案：', error);
  }

  try {
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
      
      if (response.data && response.data.records) {
        const existingIds = getExistingVideoIds();
        const videos = response.data.records.filter(item => item.type === 2 && item.id && !existingIds.has(item.id));
        const shuffled = videos.sort(() => Math.random() - 0.5);
        if (shuffled.length > 0) {
          featuredVideo.value = shuffled[0];
        }
      }
    }
  } catch (error) {
    console.error('加载推荐视频失败：', error);
  }
};

const loadHotVideos = async (shuffle = false) => {
  const userId = getCurrentUserId();
  try {
    if (userId) {
      const response = await recommendationApi.getHotVideos(userId, 10, shuffle);
      if (response && Array.isArray(response.data) && response.data.length > 0) {
        const existingIds = getExistingVideoIds();
        const candidates = response.data.filter(item => item && item.type === 2 && item.id && !existingIds.has(item.id));
        const shuffled = candidates.sort(() => Math.random() - 0.5);
        hotVideos.value = shuffled.slice(0, 3);
        if (hotVideos.value.length > 0) return;
      }
    }
  } catch (error) {
    console.error('加载热门视频失败（Python 推荐），使用随机方案：', error);
  }

  try {
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
      
      if (response.data && response.data.records) {
        const existingIds = getExistingVideoIds();
        const videos = response.data.records.filter(item => item.type === 2 && item.id && !existingIds.has(item.id));
        const shuffled = videos.sort(() => Math.random() - 0.5);
        hotVideos.value = shuffled.slice(0, 3);
      }
    }
  } catch (error) {
    console.error('加载热门视频失败：', error);
  }
};

const loadRecommendedVideos = async (shuffle = false) => {
  const userId = getCurrentUserId();
  try {
    if (userId) {
      const response = await recommendationApi.getVideoRecommendations(userId, 10, shuffle);
      if (response && Array.isArray(response.data) && response.data.length > 0) {
        const existingIds = getExistingVideoIds();
        const candidates = response.data.filter(item => item && item.type === 2 && item.id && !existingIds.has(item.id));
        const shuffled = candidates.sort(() => Math.random() - 0.5);
        recommendedVideos.value = shuffled.slice(0, 2);
        if (recommendedVideos.value.length > 0) return;
      }
    }
  } catch (error) {
    console.error('加载推荐列表失败（Python 推荐），使用随机方案：', error);
  }

  try {
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
      
      if (response.data && response.data.records) {
        const existingIds = getExistingVideoIds();
        const videos = response.data.records.filter(item => item.type === 2 && item.id && !existingIds.has(item.id));
        const shuffled = videos.sort(() => Math.random() - 0.5);
        recommendedVideos.value = shuffled.slice(0, 2);
      }
    }
  } catch (error) {
    console.error('加载推荐列表失败：', error);
  }
};

const refreshHotVideos = async () => {
  const keyword = getCurrentKeyword();
  if (!keyword) {
    await loadHotVideos(true);
  } else if (categoryVideoPool.value.length > 0) {
    hotVideos.value = shuffleVideos(categoryVideoPool.value).slice(0, 3);
  } else {
    await loadVideosForCurrentTab(true);
  }
  uni.showToast({ title: '已刷新', icon: 'success' });
};

const refreshRecommendedVideos = async () => {
  const keyword = getCurrentKeyword();
  if (!keyword) {
    await loadRecommendedVideos(true);
  } else if (categoryVideoPool.value.length > 0) {
    recommendedVideos.value = shuffleVideos(categoryVideoPool.value).slice(0, 2);
  } else {
    await loadVideosForCurrentTab(true);
  }
  uni.showToast({ title: '已刷新', icon: 'success' });
};

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
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return Math.floor(diff / 86400000) + '天前';
};

const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

const handleScroll = (e) => {
  scrollLeft.value = e.detail.scrollLeft;
};

const navigateToProfile = () => {
  uni.switchTab({ url: '/pages/child/profile' });
};

const navigateToSearch = () => {
  uni.navigateTo({ url: '/pages/child/search/search' });
};

const navigateToMessage = () => {
  uni.navigateTo({ url: '/pages/common/notification' });
};

const navigateToAllVideos = () => {
  uni.navigateTo({
    url: '/pages/child/video/all-videos'
  });
};

const navigateToVideoPlayer = (videoId) => {
  if (!videoId) return;
  uni.navigateTo({ url: `/pages/child/video-detail?id=${videoId}` });
};

const switchTab = (url) => {
  uni.switchTab({
    url,
    fail: () => {
      uni.reLaunch({ url });
    }
  });
};
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
  animation: pageSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  padding-bottom: 120rpx; /* 为底部Tabbar留出空间 */
}

@keyframes pageSlideIn {
  0% { opacity: 0; transform: translateY(30rpx); }
  100% { opacity: 1; transform: translateY(0); }
}

/* 顶部导航栏样式 - 调整为紫色系 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #6366f1, #8b5cf6); /* 统一使用紫色渐变 */
  z-index: 50;
  box-shadow: 0 2rpx 12rpx rgba(139, 92, 246, 0.2);
}

.header-content {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  box-sizing: border-box;
  width: 100%;
  height: 45px;
}

.header-left {
  margin-right: 12px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2rpx solid #ffffff;
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
}

.search-icon {
  color: rgba(255, 255, 255, 0.8);
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
  color: rgba(255, 255, 255, 0.8);
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
  background-color: #f3f4f6; /* 保持浅灰背景 */
  padding: 8px 0;
  white-space: nowrap;
  border-bottom: 1px solid #e5e7eb;
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
  background-color: #8b5cf6; /* 紫色下划线 */
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.tab.active {
  background-color: #8b5cf6; /* 紫色激活背景 */
  color: white;
  transform: scale(1.05);
}

.tab.active::after {
  width: 50%;
}

/* 主要内容区域样式 */
.main-content {
  /* margin-top 由 style 动态控制 */
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
  color: #8b5cf6; /* 紫色链接 */
  font-size: 14px;
  transition: color 0.2s ease;
}

.more-link:active, .refresh-link:active {
  opacity: 0.7;
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
  backdrop-filter: blur(4px);
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
  border-radius: 6px;
}

.video-content {
  flex: 1;
  padding: 0 8px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.video-content .video-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.video-metrics {
  display: flex;
  gap: 12px;
  margin-top: 4px;
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
  gap: 6px;
  margin-top: 4px;
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

/* 底部导航栏样式 (复用自home.vue) */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: #ffffff;
  border-top: 2rpx solid #e5e7eb;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  padding: 4rpx 0;
  transition: all 0.3s ease;
}

.tab-item.active {
  color: #8b5cf6; /* 紫色激活 */
  transform: translateY(-2rpx);
}

.tab-item .fas {
  font-size: 40rpx;
  line-height: 1;
  margin-bottom: 8rpx;
}

.tab-text {
  font-size: 20rpx;
  line-height: 1;
  font-weight: 500;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}

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
}
</style>

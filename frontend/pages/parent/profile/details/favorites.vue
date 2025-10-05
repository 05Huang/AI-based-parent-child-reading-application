<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">我的收藏</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 收藏统计 -->
      <view class="stats-card">
        <view class="stats-header">
          <text class="stats-title">收藏统计</text>
          <view class="refresh-btn" @click="refreshCache">
            <text class="fas fa-sync-alt" :class="{ 'rotating': refreshing }"></text>
            <text class="refresh-text">刷新</text>
          </view>
        </view>
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.totalCollections}}</text>
            <text class="stat-label">收藏总数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.monthlyCollections}}</text>
            <text class="stat-label">本月收藏</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.collectionShares}}</text>
            <text class="stat-label">已分享</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{statsDisplay.interactionCount}}</text>
            <text class="stat-label">互动数</text>
          </view>
        </view>
      </view>

      <!-- 收藏列表 -->
      <view v-if="articles.length > 0" class="favorites-list">
        <view v-for="(article, index) in articles" :key="index" class="book-card">
          <view class="book-cover-container" @click="toggleCardActions(index)">
            <image :src="article.coverUrl" class="book-cover"></image>
            <view class="book-actions" :class="{ 'show': activeCardIndex === index }">
              <view class="action-btn play" @click.stop="viewArticle(article)">
                <text class="fas fa-eye btn-icon"></text>
                <text class="btn-text">查看</text>
              </view>
              <view class="action-btn share" @click.stop="shareArticle(article)">
                <text class="fas fa-share-alt btn-icon"></text>
                <text class="btn-text">分享</text>
              </view>
              <view class="action-btn delete" @click.stop="deleteArticle(article)">
                <text class="fas fa-trash-alt btn-icon"></text>
                <text class="btn-text">删除</text>
              </view>
            </view>
          </view>
          <view class="book-info">
            <text class="book-title">{{ article.title }}</text>
          </view>
        </view>
      </view>
      
      <!-- 无收藏内容时显示 -->
      <view v-else class="empty-state">
        <view class="empty-icon">
          <text class="fas fa-heart"></text>
        </view>
        <text class="empty-title">暂无收藏内容</text>
        <text class="empty-desc">去发现一些精彩内容吧</text>
        <view class="empty-action" @click="goToDiscovery">
          <text class="action-text">去发现</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { favoriteApi, userBehaviorApi, userApi, categoryApi } from '@/utils/api.js'

// 响应式状态
const currentUser = ref(null)
const collectionStats = ref({
  totalCollections: 0,
  monthlyCollections: 0,
  collectionShares: 0,
  interactionCount: 0
})
const articles = ref([])
const loading = ref(false)
const refreshing = ref(false)
const activeCardIndex = ref(-1) // 当前激活的卡片索引

// 计算统计数据显示
const statsDisplay = computed(() => {
  return {
    totalCollections: collectionStats.value.totalCollections || 0,
    monthlyCollections: collectionStats.value.monthlyCollections || 0,
    collectionShares: collectionStats.value.collectionShares || 0,
    interactionCount: collectionStats.value.interactionCount || 0
  }
})

// 删除分类筛选相关代码

// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    console.log('开始获取当前用户信息')
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      console.log('获取用户信息成功：', response.data)
      currentUser.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    uni.showToast({
      title: '获取用户信息失败',
      icon: 'none'
    })
  }
}

// 获取收藏统计数据
const loadCollectionStats = async () => {
  try {
    if (!currentUser.value?.id) {
      console.log('用户ID不存在，跳过加载收藏统计')
      return
    }
    
    console.log('开始获取收藏统计数据，用户ID：', currentUser.value.id)
    const response = await userBehaviorApi.getCollectionStats(currentUser.value.id)
    
    console.log('收藏统计API完整响应：', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      console.log('获取收藏统计成功：', response.data)
      // 确保所有字段都有默认值
      collectionStats.value = {
        totalCollections: response.data.totalCollections || 0,
        monthlyCollections: response.data.monthlyCollections || 0,
        collectionShares: response.data.collectionShares || 0,
        interactionCount: response.data.interactionCount || 0
      }
      console.log('更新后的collectionStats：', collectionStats.value)
    } else {
      console.warn('收藏统计响应格式异常或无数据，使用默认值')
    }
  } catch (error) {
    console.error('获取收藏统计失败：', error)
    // 不显示错误提示，使用默认值
  }
}

// 删除分类加载相关代码

// 获取收藏列表
const loadFavorites = async () => {
  try {
    if (!currentUser.value?.id || loading.value) return
    
    loading.value = true
    console.log('开始获取收藏列表，用户ID：', currentUser.value.id)
    
    const queryParams = {
      userId: currentUser.value.id,
      current: 1,
      size: 50
    }
    
    const response = await favoriteApi.getUserFavorites(queryParams)
    
    if (response && response.data && response.data.records) {
      console.log('获取收藏列表成功，共', response.data.records.length, '条')
      
      // 转换数据格式并过滤无效数据
      const validArticles = response.data.records.filter(item => {
        // 过滤条件：必须有内容ID和标题
        const isValid = item.contentId && 
                       item.contentTitle && 
                       item.contentTitle.trim() !== '' &&
                       item.contentTitle !== '无标题'
        
        if (!isValid) {
          console.log('过滤掉无效收藏项：', item)
        }
        
        return isValid
      })
      
      console.log('有效收藏项数量：', validArticles.length, '，原始数量：', response.data.records.length)
      
      articles.value = await Promise.all(
        validArticles.map(async (item) => {
          console.log('处理有效收藏项：', item)
          return {
            id: item.id,
            title: item.contentTitle,
            coverUrl: item.coverUrl || 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&auto=format&fit=crop',
            contentId: item.contentId,
            contentType: item.contentType || 1, // 默认为图文内容
            favoriteId: item.id, // 收藏记录ID，用于删除
            addTime: formatTime(item.createdTime)
          }
        })
      )
    }
  } catch (error) {
    console.error('获取收藏列表失败：', error)
    uni.showToast({
      title: '获取收藏失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 删除不再需要的分类函数

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 切换卡片按钮显示状态
const toggleCardActions = (index) => {
  console.log('切换卡片按钮显示状态，索引：', index)
  if (activeCardIndex.value === index) {
    // 如果点击的是当前激活的卡片，则隐藏按钮
    activeCardIndex.value = -1
  } else {
    // 否则显示新卡片的按钮
    activeCardIndex.value = index
  }
}

// 返回上一页
const goBack = () => {
  uni.switchTab({
    url: '/pages/parent/profile/profile'
  })
}

// 去发现页面
const goToDiscovery = () => {
  console.log('跳转到发现页面')
  uni.switchTab({
    url: '/pages/parent/home/home',
    success: () => {
      console.log('跳转到发现页面成功')
    },
    fail: (err) => {
      console.error('跳转到发现页面失败:', err)
      uni.showToast({
        title: '跳转失败',
        icon: 'none'
      })
    }
  })
}

// 查看文章
const viewArticle = (article) => {
  console.log('查看内容：', article.title, '内容ID：', article.contentId, '内容类型：', article.contentType)
  
  if (!article.contentId) {
    console.error('内容ID不存在')
    uni.showToast({
      title: '内容ID不存在',
      icon: 'none'
    })
    return
  }
  
  // 如果内容类型为null或undefined，默认为图文内容
  const contentType = article.contentType || 1
  console.log('处理后的内容类型：', contentType)
  
  if (contentType === 1) {
    // 图文内容
    console.log('跳转到图文阅读页面')
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${article.contentId}`,
      success: () => {
        console.log('跳转到阅读页面成功')
      },
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({
          title: '暂时无法查看',
          icon: 'none'
        })
      }
    })
  } else if (contentType === 2) {
    // 视频内容
    console.log('跳转到视频播放页面')
    uni.navigateTo({
      url: `/pages/parent/video/video-player?id=${article.contentId}`,
      success: () => {
        console.log('跳转到视频页面成功')
      },
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({
          title: '暂时无法查看',
          icon: 'none'
        })
      }
    })
  } else {
    console.warn('未知的内容类型：', contentType, '，默认按图文内容处理')
    // 对于未知类型，默认按图文内容处理
    uni.navigateTo({
      url: `/pages/parent/reading/reading?id=${article.contentId}`,
      success: () => {
        console.log('跳转到阅读页面成功（默认处理）')
      },
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({
          title: '暂时无法查看',
          icon: 'none'
        })
      }
    })
  }
}

// 分享文章
const shareArticle = (article) => {
  console.log('开始分享文章：', article.title)
  
  if (!article.contentId) {
    uni.showToast({
      title: '文章数据未加载',
      icon: 'none'
    })
    return
  }
  
  // 构建分享内容
  const shareTitle = article.title || '精彩文章'
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
        showShareActionSheet(article)
      }
    })
  } else {
    console.log('不支持Web Share API，显示分享选项')
    showShareActionSheet(article)
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
    href: `https://parentreading.com/article/${article.contentId}`,
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
      showShareActionSheet(article)
    }
  })
  // #endif
}

// 显示分享操作菜单
const showShareActionSheet = (article) => {
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
          copyArticleLink(article)
          break
        case 1:
          // 使用系统分享
          useSystemShare(article)
          break
        case 2:
          // 生成分享海报
          generateSharePoster(article)
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
          copyArticleLink(article)
          break
        case 1:
          generateSharePoster(article)
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
const useSystemShare = (article) => {
  console.log('使用系统分享')
  
  // #ifdef APP-PLUS
  const shareTitle = article.title || '精彩文章'
  
  uni.share({
    provider: 'system',
    type: 1,
    title: shareTitle,
    summary: `来自亲子阅读：${shareTitle}`,
    href: `https://parentreading.com/article/${article.contentId}`,
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
const copyArticleLink = (article) => {
  console.log('复制文章链接')
  
  // 构建文章链接
  let articleLink = ''
  
  // #ifdef H5
  articleLink = window.location.href
  // #endif
  
  // #ifndef H5
  // 在非H5环境下，构建一个模拟链接
  articleLink = `https://parentreading.com/article/${article.contentId}`
  // #endif
  
  const shareText = `${article.title}\n\n${articleLink}`
  
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
const generateSharePoster = (article) => {
  console.log('生成分享海报')
  uni.showToast({
    title: '海报生成功能开发中',
    icon: 'none'
  })
  // TODO: 实现海报生成功能
  // 可以使用canvas生成包含文章标题、封面、二维码等信息的海报
}

// 取消收藏
const deleteArticle = async (article) => {
  if (!currentUser.value?.id) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }
  
  uni.showModal({
    title: '确认删除',
    content: `确定要取消收藏《${article.title}》吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          console.log('删除收藏：', article.title, '用户ID：', currentUser.value.id, '内容ID：', article.contentId)
          
          const response = await favoriteApi.deleteFavorite(currentUser.value.id, article.contentId)
          
          console.log('删除收藏API响应：', response)
          
          if (response && response.code === 200) {
            // 从本地列表中移除
            articles.value = articles.value.filter(item => item.id !== article.id)
            
            console.log('收藏删除成功，从本地列表移除')
            
            uni.showToast({
              title: '已取消收藏',
              icon: 'success'
            })
            
            // 重新加载统计数据
            await loadCollectionStats()
          } else {
            console.error('删除收藏失败，响应：', response)
            uni.showToast({
              title: response?.message || '取消收藏失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('删除收藏失败：', error)
          uni.showToast({
            title: '取消收藏失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 刷新缓存
const refreshCache = async () => {
  if (!currentUser.value?.id || refreshing.value) return
  
  refreshing.value = true
  console.log('开始刷新统计缓存')
  
  try {
    const response = await userBehaviorApi.refreshStatsCache(currentUser.value.id)
    
    if (response && response.code === 200) {
      console.log('缓存刷新成功，重新加载统计数据')
      
      // 重新加载统计数据
      await loadCollectionStats()
      
      uni.showToast({
        title: '数据已刷新',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('刷新缓存失败：', error)
    uni.showToast({
      title: '刷新失败',
      icon: 'none'
    })
  } finally {
    refreshing.value = false
  }
}

// 加载所有数据的统一方法
const loadAllData = async () => {
  console.log('[收藏页面] 加载所有数据')
  await loadCurrentUser()
  await loadCollectionStats()
  await loadFavorites()
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('[收藏页面] 页面已挂载，开始加载数据')
  await loadAllData()
})

// 页面显示时刷新数据
onShow(async () => {
  console.log('[收藏页面] 页面显示，刷新数据')
  await loadAllData()
})
</script>

<style>
/* 引入Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  width: 100%;
  box-sizing: border-box;
}

/* 顶部导航样式 */
.nav-header {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
}

.nav-content {
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

.back-btn {
  padding: 20rpx;
  margin: -20rpx;
  margin-right: 10rpx;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
}

/* 主要内容区域 */
.main-content {
  margin-top: 88rpx;
  padding: 30rpx;
  height: calc(100vh - 88rpx);
  width: 100%;
  box-sizing: border-box;
}

/* 删除分类标签样式 */

/* 统计卡片样式 */
.stats-card {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.15);
  width: 100%;
  box-sizing: border-box;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.stats-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  transition: all 0.3s ease;
}

.refresh-btn:active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.95);
}

.refresh-btn .fas {
  font-size: 24rpx;
  color: #ffffff;
}

.refresh-btn .rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.refresh-text {
  font-size: 24rpx;
  color: #ffffff;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
  width: 100%;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #ffffff;
}

.stat-value {
  font-size: 48rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 24rpx;
  opacity: 0.9;
}

/* 收藏列表样式 */
.favorites-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  width: 100%;
}

.book-card {
  background-color: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.book-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.12);
}

.book-cover-container {
  position: relative;
  width: 100%;
  padding-top: 120%;
}

.book-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20rpx;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.book-actions.show {
  opacity: 1;
  pointer-events: auto;
}

.action-btn {
  min-width: 120rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background-color: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 0 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.action-btn:active {
  background-color: rgba(255, 255, 255, 0.95);
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.4);
}

.action-btn text {
  font-size: 24rpx;
}

.action-btn text.btn-icon {
  font-size: 22rpx;
}

.action-btn text.btn-text {
  font-size: 22rpx;
  font-weight: normal;
}

/* 为不同按钮添加特定颜色 */
.action-btn.play {
  background-color: #10b981;
}

.action-btn.share {
  background-color: #6366f1;
}

.action-btn.delete {
  background-color: #ef4444;
}

.action-btn text {
  color: #ffffff;
}

.book-info {
  padding: 24rpx 20rpx;
}

.book-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-align: center;
}

/* 删除不再需要的元数据样式 */

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
  text-align: center;
}

.empty-icon {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
}

.empty-icon .fas {
  font-size: 48rpx;
  color: #9ca3af;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 40rpx;
  line-height: 1.5;
}

.empty-action {
  padding: 20rpx 40rpx;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 24rpx;
  transition: all 0.3s ease;
}

.empty-action:active {
  transform: scale(0.95);
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}

.action-text {
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 500;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style>

<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-content">
        <text class="header-title">个人中心</text>
        <view class="header-actions">
          <text class="fas fa-cog" @click="navigateToSettings"></text>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 用户信息卡片 -->
      <view class="user-card">
        <view class="qr-icon-btn" @click="showMyQRCode">
          <text class="fas fa-qrcode"></text>
        </view>
        <view class="user-info">
          <image class="avatar" :src="currentUser.avatar" mode="aspectFill"></image>
          <view class="user-details">
            <text class="nickname">{{currentUser.nickname}}</text>
            <text class="username">@{{currentUser.username}}</text>
          </view>
        </view>
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-number">{{userStats.followingCount}}</text>
            <text class="stat-label">关注</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{userStats.followersCount}}</text>
            <text class="stat-label">粉丝</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{userStats.readingLevel}}</text>
            <text class="stat-label">阅读等级</text>
          </view>
        </view>
      </view>

      <!-- 功能区 -->
      <view class="feature-grid">
        <view class="feature-item" @click="navigateToReadingRecord">
          <view class="feature-icon blue">
            <text class="fas fa-book"></text>
          </view>
          <text class="feature-text">阅读记录</text>
        </view>
        <view class="feature-item" @click="navigateToFavorites">
          <view class="feature-icon pink">
            <text class="fas fa-star"></text>
          </view>
          <text class="feature-text">我的收藏</text>
        </view>
        <view class="feature-item" @click="navigateToHistory">
          <view class="feature-icon purple">
            <text class="fas fa-history"></text>
          </view>
          <text class="feature-text">阅读历史</text>
        </view>
        <view class="feature-item" @click="navigateToReport">
          <view class="feature-icon green">
            <text class="fas fa-chart-line"></text>
          </view>
          <text class="feature-text">阅读报告</text>
        </view>
      </view>

    

      <!-- 亲密度排行 -->
      <view class="intimacy-ranking">
        <view class="section-header">
          <text class="section-title">亲密度排行</text>
          <view class="view-all" @click="navigateToIntimacyRanking">
            <text class="view-all-text">查看全部</text>
            <text class="fas fa-chevron-right"></text>
          </view>
        </view>
        <view class="ranking-list">
          <view v-for="(item, index) in intimacyRankingPreview" :key="index" class="ranking-item">
            <view class="ranking-left">
              <view class="rank-number" :class="{'rank-first': item.rank === 1}">{{item.rank}}</view>
              <image class="rank-avatar" :src="item.avatar" mode="aspectFill"></image>
              <view class="rank-info">
                <text class="rank-name">{{item.name}}</text>
                <text class="rank-intimacy">亲密度: {{item.intimacy}}%</text>
              </view>
            </view>
            <view class="trend-tag" :class="item.trend">
              <text class="fas" :class="item.trend === 'up' ? 'fa-arrow-up' : 'fa-arrow-down'"></text>
              <text>{{item.trend === 'up' ? '上升' : '下降'}}</text>
            </view>
          </view>
        </view>
      </view>

      
    </view>

    <!-- 底部导航栏 -->
    
    <!-- 二维码弹窗 -->
    <view v-if="showQRModal" class="qr-modal" @click="closeQRModal">
      <view class="qr-modal-content" @click.stop>
        <!-- 关闭按钮 -->
        <view class="qr-close-btn" @click="closeQRModal">
          <text class="fas fa-times"></text>
        </view>
        
        <!-- 渐变背景装饰 -->
        <view class="qr-bg-decoration"></view>
        
        <view class="qr-modal-body">
          <!-- 标题区域 -->
          <view class="qr-title-section">
            <text class="qr-icon">✨</text>
            <text class="qr-modal-title">我的专属二维码</text>
            <text class="qr-subtitle">扫码即可快速绑定家庭成员</text>
          </view>
          
          <!-- 用户信息卡片 -->
          <view class="qr-user-card">
            <image class="qr-avatar" :src="currentUser.avatar" mode="aspectFill"></image>
            <view class="qr-user-info">
              <text class="qr-nickname">{{ currentUser.nickname }}</text>
              <text class="qr-username">@{{ currentUser.username }}</text>
            </view>
          </view>
          
          <!-- 二维码容器 -->
          <view class="qr-code-container">
            <view class="qr-code-wrapper" @longpress="saveQRCode">
              <canvas canvas-id="qrCanvas" class="qr-canvas" :style="{ width: qrSize + 'px', height: qrSize + 'px' }"></canvas>
              <!-- 四角装饰 -->
              <view class="qr-corner qr-corner-tl"></view>
              <view class="qr-corner qr-corner-tr"></view>
              <view class="qr-corner qr-corner-bl"></view>
              <view class="qr-corner qr-corner-br"></view>
            </view>
            <view class="save-qr-btn" @click="saveQRCode">
              <text class="fas fa-download"></text>
              <text class="save-qr-text">保存二维码</text>
            </view>
          </view>
          
          <!-- 提示信息 -->
          <view class="qr-tips">
            <view class="qr-tip-item">
              <text class="fas fa-mobile-alt qr-tip-icon"></text>
              <text class="qr-tip-text">打开亲子阅读APP扫一扫</text>
            </view>
            <view class="qr-tip-item">
              <text class="fas fa-users qr-tip-icon"></text>
              <text class="qr-tip-text">选择关系类型完成绑定</text>
            </view>
          </view>
          
          <!-- 底部装饰 -->
          <view class="qr-footer">
            <text class="qr-footer-text">长按保存二维码 · 分享给家人</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi, intimacyApi, userBehaviorApi } from '@/utils/api.js'

// 响应式状态
const currentUser = ref({
  nickname: '张爸爸',
  username: 'zhangbaba',
  id: 888888,
  avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=dad123&backgroundColor=b6e3f4'
})

const userStats = ref({
  followingCount: 0,
  followersCount: 0,
  readingLevel: 0
})

const intimacyRankingPreview = ref([])
const showQRModal = ref(false)
const qrSize = 260

// 页面跳转方法
const navigateToIntimacyRanking = () => {
  console.log('即将跳转到亲密度排行榜页面')
  uni.navigateTo({
    url: '/pages/parent/intimacy-ranking/intimacy-ranking',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加阅读记录跳转方法
const navigateToReadingRecord = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/readTheTranscript',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加收藏页面跳转方法
const navigateToFavorites = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/favorites',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加阅读历史跳转方法
const navigateToHistory = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/history',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加阅读报告跳转方法
const navigateToReport = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/report',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 添加设置页面跳转方法
const navigateToSettings = () => {
  uni.navigateTo({
    url: '/pages/parent/profile/details/setting',
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '页面跳转失败',
        icon: 'none'
      })
    }
  })
}

// 显示我的二维码
const showMyQRCode = () => {
  console.log('显示我的二维码，用户信息：', currentUser.value)
  showQRModal.value = true
  
  // 延迟生成二维码，确保canvas已渲染
  setTimeout(() => {
    generateQRCode()
  }, 100)
}

// 关闭二维码弹窗
const closeQRModal = () => {
  showQRModal.value = false
}

// 生成二维码
const generateQRCode = () => {
  const qrContent = `qz-family-bind:${currentUser.value.id}:${currentUser.value.username}`
  console.log('生成二维码，内容：', qrContent)
  
  const ctx = uni.createCanvasContext('qrCanvas')
  
  // 使用在线API生成二维码
  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=${qrSize}x${qrSize}&data=${encodeURIComponent(qrContent)}`
  
  console.log('二维码URL：', qrUrl)
  
  // 下载二维码图片并绘制到canvas
  uni.downloadFile({
    url: qrUrl,
    header: {
      'Accept': 'image/png'
    },
    success: (res) => {
      if (res.statusCode === 200) {
        console.log('二维码图片下载成功')
        ctx.drawImage(res.tempFilePath, 0, 0, qrSize, qrSize)
        ctx.draw(false, () => {
          console.log('二维码绘制完成')
        })
      } else {
        console.error('二维码下载失败，状态码：', res.statusCode)
        drawErrorMessage(ctx)
      }
    },
    fail: (err) => {
      console.error('二维码图片下载失败：', err)
      drawErrorMessage(ctx)
    }
  })
}

// 绘制错误提示
const drawErrorMessage = (ctx) => {
  ctx.setFillStyle('#f3f4f6')
  ctx.fillRect(0, 0, qrSize, qrSize)
  ctx.setFillStyle('#000000')
  ctx.setFontSize(14)
  ctx.setTextAlign('center')
  ctx.fillText('二维码生成失败', qrSize / 2, qrSize / 2)
  ctx.draw()
}

// 保存二维码
const saveQRCode = () => {
  console.log('开始保存二维码')
  
  // #ifdef H5
  // H5环境：将canvas转为图片并下载
  uni.canvasToTempFilePath({
    canvasId: 'qrCanvas',
    success: (res) => {
      console.log('Canvas转图片成功：', res.tempFilePath)
      
      // 创建a标签下载
      const link = document.createElement('a')
      link.href = res.tempFilePath
      link.download = `${currentUser.value.username}_二维码.png`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      uni.showToast({
        title: '二维码已保存',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('Canvas转图片失败：', err)
      uni.showToast({
        title: '保存失败，请重试',
        icon: 'none'
      })
    }
  })
  // #endif
  
  // #ifdef APP-PLUS
  // APP环境：保存到相册
  uni.canvasToTempFilePath({
    canvasId: 'qrCanvas',
    success: (res) => {
      uni.saveImageToPhotosAlbum({
        filePath: res.tempFilePath,
        success: () => {
          uni.showToast({
            title: '已保存到相册',
            icon: 'success'
          })
        },
        fail: (err) => {
          console.error('保存到相册失败：', err)
          uni.showToast({
            title: '保存失败，请检查相册权限',
            icon: 'none'
          })
        }
      })
    }
  })
  // #endif
  
  // #ifdef MP-WEIXIN
  // 小程序环境：保存到相册
  uni.canvasToTempFilePath({
    canvasId: 'qrCanvas',
    success: (res) => {
      uni.saveImageToPhotosAlbum({
        filePath: res.tempFilePath,
        success: () => {
          uni.showToast({
            title: '已保存到相册',
            icon: 'success'
          })
        },
        fail: (err) => {
          console.error('保存到相册失败：', err)
          uni.showToast({
            title: '保存失败，请授权相册权限',
            icon: 'none'
          })
        }
      })
    }
  })
  // #endif
}

// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    console.log('开始获取当前用户信息')
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      console.log('获取用户信息成功：', response.data)
      currentUser.value = {
        nickname: response.data.nickname || '张爸爸',
        username: response.data.username || 'zhangbaba',
        id: response.data.id || 888888,
        avatar: response.data.avatar || 'https://api.dicebear.com/7.x/bottts/svg?seed=dad123&backgroundColor=b6e3f4'
      }
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    uni.showToast({
      title: '获取用户信息失败',
      icon: 'none'
    })
  }
}

// 获取用户统计数据
const loadUserStats = async () => {
  try {
    if (!currentUser.value.id) return
    
    console.log('开始获取用户统计数据，用户ID：', currentUser.value.id)
    
    // 获取浏览统计 - 这里可以作为阅读等级的参考
    const browsingStats = await userBehaviorApi.getBrowsingStats(currentUser.value.id)
    console.log('获取浏览统计成功：', browsingStats)
    
    if (browsingStats && browsingStats.data) {
      // 根据阅读时长和阅读数量计算阅读等级
      const readingLevel = Math.min(100, Math.floor((browsingStats.data.totalReadingTime || 0) / 60) + (browsingStats.data.totalReadCount || 0))
      userStats.value.readingLevel = readingLevel
    }
    
    // 这里关注和粉丝数量暂时设为固定值，后续可以根据实际业务需求调整
    userStats.value.followingCount = 128
    userStats.value.followersCount = 256
    
  } catch (error) {
    console.error('获取用户统计数据失败：', error)
    // 不显示错误提示，使用默认值
  }
}

// 获取亲密度排行榜预览数据
const loadIntimacyRankingPreview = async () => {
  try {
    if (!currentUser.value.id) {
      console.log('用户ID不存在，跳过获取亲密度排行榜预览')
      return
    }
    
    console.log('开始获取亲密度排行榜预览数据，用户ID：', currentUser.value.id)
    const response = await intimacyApi.getUserRanking(currentUser.value.id)
    
    console.log('亲密度接口响应：', response)
    
    if (response && response.data && response.data.ranking) {
      console.log('获取亲密度排行榜成功，数据：', response.data.ranking)
      
      // 过滤掉可能的孩子用户，然后只取前2名用于预览
      const filteredRanking = response.data.ranking.filter(item => {
        // 简单过滤：排除昵称中包含"儿子"、"女儿"、"孩子"等关键词的用户
        const childKeywords = ['儿子', '女儿', '孩子', '宝宝', '小朋友'];
        const nickname = item.nickname || '';
        return !childKeywords.some(keyword => nickname.includes(keyword));
      });
      
      intimacyRankingPreview.value = filteredRanking.slice(0, 2).map((item, index) => ({
        rank: index + 1,
        name: item.nickname || `用户${item.rank}`,
        avatar: item.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${index}`,
        intimacy: Math.round(item.percentage || 0),
        trend: index === 0 ? 'up' : 'down' // 简单的趋势显示
      }))
      
      console.log('亲密度排行榜预览数据处理完成：', intimacyRankingPreview.value)
    } else {
      console.log('响应数据格式不正确或为空，使用默认数据')
      intimacyRankingPreview.value = [
        {
          rank: 1,
          name: '小明',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming345',
          intimacy: 98,
          trend: 'up'
        },
        {
          rank: 2,
          name: '妈妈',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom456',
          intimacy: 95,
          trend: 'down'
        }
      ]
    }
  } catch (error) {
    console.error('获取亲密度排行榜预览失败，错误详情：', error)
    
    // 使用默认数据，不显示错误提示
    intimacyRankingPreview.value = [
      {
        rank: 1,
        name: '小明',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming345',
        intimacy: 98,
        trend: 'up'
      },
      {
        rank: 2,
        name: '妈妈',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom456',
        intimacy: 95,
        trend: 'down'
      }
    ]
  }
}

// 页面加载时获取数据
onMounted(async () => {
  console.log('个人中心页面已挂载，开始加载数据')
  await loadCurrentUser()
  await loadUserStats()
  await loadIntimacyRankingPreview()
})
</script>

<style>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* 顶部导航栏样式 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: rgba(59, 130, 246, 0.98);  /* 略微透明效果 */
  backdrop-filter: blur(10px);  /* 毛玻璃效果 */
  -webkit-backdrop-filter: blur(10px);
  z-index: 50;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  max-width: 1200rpx;  /* 内容最大宽度限制 */
  margin: 0 auto;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
}

.header-actions {
  font-size: 40rpx;
  color: #ffffff;
}

/* 主要内容区域 */
.main-content {
  margin-top: 88rpx;
}

/* 用户信息卡片 */
.user-card {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  padding: 40rpx 32rpx 32rpx 32rpx;
  margin: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.15);
  position: relative;
}

.qr-icon-btn {
  position: absolute;
  top: 75rpx;
  right: 75rpx;
  width: 72rpx;
  height: 72rpx;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: #ffffff;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.qr-icon-btn:active {
  transform: scale(0.95);
  background-color: rgba(255, 255, 255, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);  /* 头像阴影 */
}

.user-details {
  margin-left: 32rpx;
  flex: 1;
}

.nickname {
  font-size: 40rpx;
  font-weight: 600;
  letter-spacing: -0.5rpx;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.username {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 400;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  backdrop-filter: blur(10px);
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-number {
  font-size: 32rpx;
  font-weight: 700;
  display: block;
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: 22rpx;
  opacity: 0.9;
  font-weight: 400;
}



/* 功能区 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background-color: #ffffff;
  margin: 24rpx;
  padding: 40rpx 20rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  gap: 20rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 8rpx;
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.feature-item:active {
  transform: scale(0.95);
  background-color: #f8fafc;
}

.feature-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  font-size: 40rpx;
  transition: all 0.3s ease;
}

.feature-icon.blue {
  background-color: #eff6ff;
  color: #3b82f6;
}

.feature-icon.pink {
  background-color: #fce7f3;
  color: #ec4899;
}

.feature-icon.purple {
  background-color: #f3e8ff;
  color: #9333ea;
}

.feature-icon.green {
  background-color: #ecfdf5;
  color: #10b981;
}

.feature-text {
  font-size: 26rpx;
  color: #4b5563;
  font-weight: 500;
  text-align: center;
  line-height: 1.2;
}

/* 阅读成就 */
.achievements {
  background-color: #ffffff;
  margin: 24rpx;  /* 添加外边距 */
  border-radius: 24rpx;  /* 统一的圆角 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);  /* 柔和的阴影 */
  overflow: hidden;  /* 确保圆角生效 */
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid rgba(229, 231, 235, 0.6);
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  letter-spacing: -0.5rpx;
}

.achievement-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32rpx;
  padding: 32rpx;
}

.achievement-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.achievement-icon {
  width: 144rpx;  /* 略微增大图标 */
  height: 144rpx;
  border-radius: 32rpx;  /* 方形圆角 */
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);  /* 添加阴影 */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  font-size: 48rpx;
}

.achievement-icon.yellow {
  background-color: #fef3c7;
  color: #f59e0b;
}

.achievement-icon.blue {
  background-color: #eff6ff;
  color: #3b82f6;
}

.achievement-icon.green {
  background-color: #ecfdf5;
  color: #10b981;
}

.achievement-text {
  font-size: 28rpx;
}

/* 亲密度排行 */
.intimacy-ranking {
  background-color: #ffffff;
  margin: 24rpx;  /* 添加外边距 */
  border-radius: 24rpx;  /* 统一的圆角 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);  /* 柔和的阴影 */
  overflow: hidden;
}

.view-all {
  display: flex;
  align-items: center;
  color: #9ca3af;
}

.view-all-text {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.view-all .fas {
  font-size: 24rpx;
}

.ranking-list {
  padding: 32rpx;
}

.ranking-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.ranking-left {
  display: flex;
  align-items: center;
}

.rank-number {
  width: 56rpx;  /* 稍微缩小数字圆圈 */
  height: 56rpx;
  border-radius: 16rpx;  /* 方形圆角 */
  background-color: #e5e7eb;
  color: #4b5563;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 28rpx;
  margin-right: 24rpx;
}

.rank-number.rank-first {
  background-color: #fbbf24;
  color: #ffffff;
}

.rank-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 22rpx;  /* 方形圆角 */
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);  /* 添加阴影 */
  margin-right: 24rpx;
}

.rank-info {
  display: flex;
  flex-direction: column;
}

.rank-name {
  font-size: 30rpx;
  font-weight: 500;
  letter-spacing: -0.3rpx;
}

.rank-intimacy {
  font-size: 24rpx;
  color: #6b7280;
}

.trend-tag {
  padding: 8rpx 20rpx;
  border-radius: 12rpx;  /* 更小的圆角 */
  font-size: 24rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.trend-tag.up {
  background-color: #ecfdf5;
  color: #10b981;
}

.trend-tag.down {
  background-color: #fef2f2;
  color: #ef4444;
}

.trend-tag .fas {
  margin-right: 8rpx;
}

/* 设置选项 */
.settings {
  background-color: #ffffff;
  margin: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  padding: 8rpx 32rpx;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid rgba(229, 231, 235, 0.6);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-left {
  display: flex;
  align-items: center;
}

.setting-icon {
  width: 48rpx;
  font-size: 36rpx;
  color: #9ca3af;
  opacity: 0.7;
  text-align: center;
}

.setting-text {
  margin-left: 24rpx;
  font-size: 32rpx;
  color: #1f2937;
}

.arrow-icon {
  color: #9ca3af;
  font-size: 28rpx;
  opacity: 0.7;
  width: 40rpx;
  text-align: right;
  margin-left: auto;
}

/* 添加一些交互效果 */
.feature-icon:active,
.achievement-icon:active {
  transform: scale(0.96);
}

/* 优化字体层级 */
.section-title {
  font-size: 34rpx;
  font-weight: 600;
  letter-spacing: -0.5rpx;
}

.rank-name {
  font-size: 30rpx;
  font-weight: 500;
  letter-spacing: -0.3rpx;
}

/* 统一图标颜色透明度 */
.setting-icon,
.arrow-icon {
  opacity: 0.7;
}

/* 二维码弹窗 */
.qr-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.9), rgba(147, 51, 234, 0.9));
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
  padding: 40rpx;
}

.qr-modal-content {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 32rpx;
  width: 640rpx;
  max-width: 100%;
  position: relative;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  animation: slideUp 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 渐变背景装饰 */
.qr-bg-decoration {
  position: absolute;
  top: -100rpx;
  right: -100rpx;
  width: 400rpx;
  height: 400rpx;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border-radius: 50%;
  opacity: 0.1;
  filter: blur(60rpx);
}

/* 关闭按钮 */
.qr-close-btn {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  z-index: 10;
  transition: all 0.3s ease;
}

.qr-close-btn:active {
  transform: scale(0.9);
  background-color: rgba(255, 255, 255, 1);
}

.qr-close-btn .fas {
  font-size: 32rpx;
  color: #6b7280;
}

.qr-modal-body {
  padding: 60rpx 40rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 标题区域 */
.qr-title-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;
}

.qr-icon {
  font-size: 48rpx;
  margin-bottom: 16rpx;
}

.qr-modal-title {
  font-size: 40rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12rpx;
  letter-spacing: 1rpx;
}

.qr-subtitle {
  font-size: 24rpx;
  color: #9ca3af;
  text-align: center;
}

/* 用户信息卡片 */
.qr-user-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #eff6ff, #f3e8ff);
  padding: 24rpx 32rpx;
  border-radius: 20rpx;
  margin-bottom: 40rpx;
  width: 100%;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.1);
}

.qr-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  margin-right: 24rpx;
}

.qr-user-info {
  display: flex;
  flex-direction: column;
}

.qr-nickname {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6rpx;
}

.qr-username {
  font-size: 26rpx;
  color: #6b7280;
}

/* 二维码容器 */
.qr-code-container {
  margin-bottom: 40rpx;
  width: 100%;
}

.qr-code-wrapper {
  position: relative;
  padding: 32rpx;
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.qr-canvas {
  display: block;
  background-color: #ffffff;
  border-radius: 12rpx;
}

/* 四角装饰 */
.qr-corner {
  position: absolute;
  width: 32rpx;
  height: 32rpx;
  border-style: solid;
  border-color: #3b82f6;
}

.qr-corner-tl {
  top: 16rpx;
  left: 16rpx;
  border-width: 4rpx 0 0 4rpx;
  border-radius: 8rpx 0 0 0;
}

.qr-corner-tr {
  top: 16rpx;
  right: 16rpx;
  border-width: 4rpx 4rpx 0 0;
  border-radius: 0 8rpx 0 0;
}

.qr-corner-bl {
  bottom: 16rpx;
  left: 16rpx;
  border-width: 0 0 4rpx 4rpx;
  border-radius: 0 0 0 8rpx;
}

.qr-corner-br {
  bottom: 16rpx;
  right: 16rpx;
  border-width: 0 4rpx 4rpx 0;
  border-radius: 0 0 8rpx 0;
}

/* 保存二维码按钮 */
.save-qr-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  margin-top: 24rpx;
  padding: 20rpx 32rpx;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border-radius: 16rpx;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
}

.save-qr-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.3);
}

.save-qr-btn .fas {
  font-size: 28rpx;
  color: #ffffff;
}

.save-qr-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 600;
}

/* 提示信息 */
.qr-tips {
  width: 100%;
  margin-bottom: 32rpx;
}

.qr-tip-item {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: rgba(59, 130, 246, 0.05);
  border-radius: 16rpx;
  margin-bottom: 16rpx;
}

.qr-tip-item:last-child {
  margin-bottom: 0;
}

.qr-tip-icon {
  font-size: 28rpx;
  color: #3b82f6;
  margin-right: 20rpx;
  width: 40rpx;
  text-align: center;
}

.qr-tip-text {
  font-size: 26rpx;
  color: #4b5563;
  flex: 1;
}

/* 底部装饰 */
.qr-footer {
  padding-top: 24rpx;
  border-top: 1rpx solid #e5e7eb;
  width: 100%;
}

.qr-footer-text {
  font-size: 24rpx;
  color: #9ca3af;
  text-align: center;
  display: block;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(60rpx) scale(0.95);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}
</style> 
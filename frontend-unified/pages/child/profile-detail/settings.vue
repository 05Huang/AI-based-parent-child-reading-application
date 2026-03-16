<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-button" @tap="goBack" @touchstart="onTouchStart">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">设置</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      <!-- 账号设置 -->
      <view class="section">
        <view class="section-title">账号设置</view>
        <view class="settings-list">
          <view class="settings-item" @tap="goToProfile">
            <view class="item-left">
              <text class="fas fa-user item-icon"></text>
              <text class="item-title">个人资料</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="goToBindFamily">
            <view class="item-left">
              <text class="fas fa-users item-icon"></text>
              <text class="item-title">绑定家人</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="goToSecurity">
            <view class="item-left">
              <text class="fas fa-shield-alt item-icon"></text>
              <text class="item-title">账号安全</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 阅读设置 -->
      <view class="section">
        <view class="section-title">阅读设置</view>
        <view class="settings-list">
          <view class="settings-item" @tap="goToFontSetting">
            <view class="item-left">
              <text class="fas fa-font item-icon"></text>
              <text class="item-title">字体设置</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item" @tap="goToReadingMode">
            <view class="item-left">
              <text class="fas fa-book-reader item-icon"></text>
              <text class="item-title">阅读模式</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="section">
        <view class="section-title">通知设置</view>
        <view class="settings-list">
          <view class="settings-item">
            <view class="item-left">
              <text class="fas fa-bell item-icon"></text>
              <text class="item-title">推送通知</text>
            </view>
            <switch checked color="#6366f1" @change="handleNotificationChange" />
          </view>
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="section">
        <view class="section-title">其他</view>
        <view class="settings-list">
          <view class="settings-item" @tap="goToAbout">
            <view class="item-left">
              <text class="fas fa-info-circle item-icon"></text>
              <text class="item-title">关于我们</text>
            </view>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
          <view class="settings-item danger" @tap="handleLogout">
            <view class="item-left">
              <text class="fas fa-sign-out-alt item-icon"></text>
              <text class="item-title">退出登录</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 100
    }
  },
  onLoad() {
    // 获取状态栏高度
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight || 20
    this.headerHeight = this.statusBarHeight + 64 + 16 // 状态栏 + 头部内容 + 额外间距
  },
  methods: {
    onTouchStart() {
      console.log('触摸开始 - 返回按钮')
    },
    goBack() {
      console.log('点击返回按钮')
      try {
        // 检查页面栈
        const pages = getCurrentPages()
        console.log('当前页面栈长度:', pages.length)
        console.log('当前页面栈:', pages.map(page => page.route))
        
        if (pages.length > 1) {
          // 有上一页，使用navigateBack
          console.log('使用navigateBack返回')
          uni.navigateBack({
            success: () => {
              console.log('navigateBack返回成功')
            },
            fail: (error) => {
              console.error('navigateBack返回失败:', error)
              // 如果navigateBack失败，强制跳转到个人中心
              this.forceGoToProfile()
            }
          })
        } else {
          // 没有上一页，直接跳转到个人中心
          console.log('页面栈只有1页，直接跳转到个人中心')
          this.forceGoToProfile()
        }
      } catch (e) {
        console.error('goBack方法执行出错:', e)
        this.forceGoToProfile()
      }
    },
    
    forceGoToProfile() {
      console.log('强制跳转到个人中心')
      uni.redirectTo({
        url: '/pages/child/profile',
        success: () => {
          console.log('强制跳转到个人中心成功')
        },
        fail: (err) => {
          console.error('强制跳转到个人中心失败:', err)
          uni.showToast({
            title: '返回失败，请手动返回',
            icon: 'none'
          })
        }
      })
    },
    goToProfile() {
      uni.navigateTo({
        url: '/pages/child/profile-detail/settings-profile'
      })
    },
    goToBindFamily() {
      uni.navigateTo({
        url: '/pages/child/profile-detail/bind-family'
      })
    },
    goToSecurity() {
      uni.navigateTo({
        url: '/pages/child/profile-detail/settings-security'
      })
    },
    goToFontSetting() {
      uni.navigateTo({
        url: '/pages/child/profile-detail/settings-font'
      })
    },
    goToReadingMode() {
      uni.navigateTo({
        url: '/pages/child/profile-detail/settings-reading-mode'
      })
    },
    goToAbout() {
      uni.navigateTo({
        url: '/pages/child/profile-detail/settings-about'
      })
    },
    handleNotificationChange(e) {
      console.log('通知设置改变：', e.detail.value)
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            try {
              // 清除用户认证数据
              uni.removeStorageSync('token')
              uni.removeStorageSync('userInfo')
              uni.removeStorageSync('userId')
              uni.removeStorageSync('userName')
              uni.removeStorageSync('userRole')
              
              // 重定向到登录页面
              uni.reLaunch({
                url: '/pages/child/login/login',
                success: () => {
                  console.log('退出登录成功，已跳转到登录页面')
                  uni.showToast({
                    title: '已退出登录',
                    icon: 'success',
                    duration: 1500
                  })
                },
                fail: (err) => {
                  console.error('跳转到登录页面失败：', err)
                  uni.showToast({
                    title: '跳转失败，请重试',
                    icon: 'none'
                  })
                }
              })
            } catch (e) {
              console.error('退出登录错误：', e)
              uni.showToast({
                title: '退出失败，请重试',
                icon: 'none'
              })
            }
          }
        }
      })
    }
  }
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 20rpx 32rpx;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 2rpx 12rpx rgba(99, 102, 241, 0.2);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  width: 100%;
  position: relative;
}

.back-button {
  padding: 16rpx;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  left: 0;
  z-index: 101;
}

.back-button text {
  font-size: 36rpx;
  color: #ffffff;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
}

.main-content {
  padding: 32rpx;
  height: calc(100vh - 160rpx);
  box-sizing: border-box;
}

.section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
  padding: 0 16rpx;
}

.settings-list {
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.settings-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
}

.item-icon {
  font-size: 40rpx;
  color: #6366f1;
  margin-right: 24rpx;
  width: 40rpx;
  text-align: center;
}

.item-title {
  font-size: 28rpx;
  color: #1f2937;
}

.arrow-icon {
  color: #d1d5db;
  font-size: 32rpx;
}

.danger {
  color: #ef4444;
}

.danger .item-icon,
.danger .item-title {
  color: #ef4444;
}
</style> 
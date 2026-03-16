# 创建新的账号安全页面
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">账号安全</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 登录密码 -->
      <view class="security-section">
        <view class="security-item" @click="handlePasswordChange">
          <view class="item-left">
            <text class="fas fa-lock item-icon"></text>
            <view class="item-info">
              <text class="item-title">登录密码</text>
              <text class="item-desc">建议定期更换密码，确保账号安全</text>
            </view>
          </view>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>

        <!-- 手机号码 -->
        <view class="security-item" @click="handlePhoneChange">
          <view class="item-left">
            <text class="fas fa-mobile-alt item-icon"></text>
            <view class="item-info">
              <text class="item-title">手机号码</text>
              <text class="item-desc">已绑定：{{phoneNumber}}</text>
            </view>
          </view>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>

        <!-- 邮箱绑定 -->
        <view class="security-item" @click="handleEmailChange">
          <view class="item-left">
            <text class="fas fa-envelope item-icon"></text>
            <view class="item-info">
              <text class="item-title">邮箱绑定</text>
              <text class="item-desc">{{email || '未绑定'}}</text>
            </view>
          </view>
          <text class="fas fa-chevron-right arrow-icon"></text>
        </view>
      </view>


      <!-- 账号注销 -->
      <view class="delete-account" @click="handleDeleteAccount">
        <text class="delete-text">注销账号</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/utils/api.js'

// 用户安全信息
const phoneNumber = ref('')
const email = ref('')
const userInfo = ref(null)
const loading = ref(false)

// 页面加载时获取用户信息
onMounted(() => {
  console.log('安全设置页面加载，开始获取用户信息')
  loadUserInfo()
})

// 获取用户信息
const loadUserInfo = async () => {
  try {
    loading.value = true
    console.log('调用获取当前用户信息接口')
    
    // 先获取当前用户的基本信息（包含ID）
    const currentUserResponse = await userApi.getCurrentUser()
    console.log('获取当前用户基本信息成功：', currentUserResponse)
    
    if (currentUserResponse.code === 200 && currentUserResponse.data && currentUserResponse.data.id) {
      const userId = currentUserResponse.data.id
      console.log('当前用户ID：', userId)
      
      // 使用用户ID获取完整的用户信息（包含phone和email）
      const fullUserResponse = await userApi.getUserById(userId)
      console.log('获取完整用户信息成功：', fullUserResponse)
      
      if (fullUserResponse.code === 200 && fullUserResponse.data) {
        userInfo.value = fullUserResponse.data
        console.log('用户手机号：', fullUserResponse.data.phone)
        console.log('用户邮箱：', fullUserResponse.data.email)
        
        // 格式化手机号显示（隐藏中间4位）
        if (fullUserResponse.data.phone) {
          phoneNumber.value = fullUserResponse.data.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
          console.log('格式化后的手机号：', phoneNumber.value)
        } else {
          phoneNumber.value = '未绑定手机号'
          console.log('用户未绑定手机号')
        }
        
        // 格式化邮箱显示（隐藏用户名部分）
        if (fullUserResponse.data.email) {
          // 检查是否是临时邮箱（@temp.com格式）
          if (fullUserResponse.data.email.endsWith('@temp.com')) {
            email.value = '未绑定邮箱'
            console.log('用户使用临时邮箱，显示为未绑定')
          } else {
            const emailParts = fullUserResponse.data.email.split('@')
            if (emailParts.length === 2) {
              const username = emailParts[0]
              const domain = emailParts[1]
              const hiddenUsername = username.length > 3 
                ? username.substring(0, 3) + '***' 
                : username.substring(0, 1) + '***'
              email.value = hiddenUsername + '@' + domain
              console.log('格式化后的邮箱：', email.value)
            } else {
              email.value = fullUserResponse.data.email
            }
          }
        } else {
          email.value = '未绑定邮箱'
          console.log('用户未绑定邮箱')
        }
      } else {
        console.error('获取完整用户信息失败：', fullUserResponse.message)
        uni.showToast({
          title: fullUserResponse.message || '获取用户详细信息失败',
          icon: 'none'
        })
      }
    } else {
      console.error('获取当前用户基本信息失败：', currentUserResponse.message)
      uni.showToast({
        title: currentUserResponse.message || '获取用户信息失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('获取用户信息异常：', error)
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 修改密码
const handlePasswordChange = () => {
  console.log('用户点击修改密码')
  uni.navigateTo({
    url: '/pages/parent/profile/details/change-password'
  })
}

// 修改手机号
const handlePhoneChange = () => {
  console.log('用户点击修改手机号')
  uni.navigateTo({
    url: '/pages/parent/profile/details/change-phone'
  })
}

// 修改邮箱
const handleEmailChange = () => {
  console.log('用户点击修改邮箱')
  uni.navigateTo({
    url: '/pages/parent/profile/details/change-email'
  })
}


// 处理账号注销
const handleDeleteAccount = async () => {
  console.log('用户点击注销账号')
  
  uni.showModal({
    title: '注销账号',
    content: '注销后，账号将无法恢复，确定要注销吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          console.log('用户确认注销账号，开始注销流程')
          
          // 显示二次确认
          uni.showModal({
            title: '最后确认',
            content: '您确定要永久注销账号吗？此操作不可撤销！',
            confirmColor: '#ef4444',
            success: async (confirmRes) => {
              if (confirmRes.confirm) {
                console.log('用户最终确认注销账号')
                
                if (!userInfo.value || !userInfo.value.id) {
                  uni.showToast({
                    title: '用户信息获取失败，请重试',
                    icon: 'none'
                  })
                  return
                }
                
                // 调用注销接口（使用删除用户接口）
                try {
                  console.log('调用删除用户接口，用户ID：', userInfo.value.id)
                  const response = await userApi.deleteUser(userInfo.value.id)
                  console.log('删除用户响应：', response)
                  
                  if (response.code === 200) {
                    uni.showToast({
                      title: '账号注销成功',
                      icon: 'success'
                    })
                    
                    // 清除本地存储的用户信息和token
                    uni.removeStorageSync('userInfo')
                    uni.removeStorageSync('token')
                    uni.removeStorageSync('accessToken')
                    
                    // 延迟跳转到登录页
                    setTimeout(() => {
                      uni.reLaunch({
                        url: '/pages/role-select/role-select'
                      })
                    }, 1500)
                  } else {
                    console.error('注销账号失败：', response.message)
                    uni.showToast({
                      title: response.message || '注销失败，请稍后重试',
                      icon: 'none'
                    })
                  }
                } catch (error) {
                  console.error('注销账号异常：', error)
                  uni.showToast({
                    title: '网络错误，请稍后重试',
                    icon: 'none'
                  })
                }
              }
            }
          })
        } catch (error) {
          console.error('注销账号流程异常：', error)
          uni.showToast({
            title: '操作失败，请稍后重试',
            icon: 'none'
          })
        }
      }
    }
  })
}
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
  cursor: pointer;
}

.back-btn text {
  color: #6b7280;
  font-size: 32rpx;
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

/* 安全设置区块样式 */
.security-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
  overflow: hidden;
}

.section-title {
  font-size: 28rpx;
  color: #6b7280;
  padding: 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.security-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.item-icon {
  width: 40rpx;
  font-size: 36rpx;
  color: #3b82f6;
  text-align: center;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.item-title {
  font-size: 30rpx;
  color: #111827;
}

.item-desc {
  font-size: 24rpx;
  color: #6b7280;
}

.arrow-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 注销账号按钮 */
.delete-account {
  background-color: #ffffff;
  padding: 30rpx;
  border-radius: 16rpx;
  text-align: center;
  margin-top: 60rpx;
}

.delete-text {
  color: #ef4444;
  font-size: 30rpx;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
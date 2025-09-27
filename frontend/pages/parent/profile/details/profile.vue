<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">个人资料</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 加载中状态 -->
      <view v-if="loading" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 个人资料表单 -->
      <view v-else class="profile-form">
        <!-- 头像设置 -->
        <view class="profile-item" @click="handleAvatarClick">
          <text class="item-label">头像</text>
          <view class="item-right">
            <image 
              class="avatar" 
              :src="userInfo.avatar || defaultAvatar" 
              mode="aspectFill"
            ></image>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>

        <!-- 昵称设置 -->
        <view class="profile-item" @click="handleNicknameClick">
          <text class="item-label">昵称</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.nickname || '未设置' }}</text>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>

        <!-- 性别设置 -->
        <view class="profile-item" @click="handleGenderClick">
          <text class="item-label">性别</text>
          <view class="item-right">
            <text class="item-value">{{ genderText }}</text>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>

        <!-- 保存按钮 -->
        <view class="save-section">
          <button 
            class="save-btn" 
            @click="saveProfile"
            :disabled="saving"
          >
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi, fileApi } from '../../../../utils/api.js'

// 响应式数据
const userInfo = ref({
  id: null,
  avatar: '',
  nickname: '',
  sex: 0 // 0:男 1:女
})

const loading = ref(true)
const saving = ref(false)
const defaultAvatar = 'https://ui-avatars.com/api/?name=用户&background=3b82f6&color=fff&size=128'

// 性别显示文本
const genderText = computed(() => {
  const genderMap = {
    0: '男',
    1: '女'
  }
  return genderMap[userInfo.value.sex] || '未设置'
})

// 页面加载时获取用户信息
onMounted(() => {
  console.log('profile页面加载，开始获取用户信息')
  loadUserProfile()
})

// 获取用户资料
const loadUserProfile = async () => {
  try {
    console.log('开始调用获取用户信息API')
    loading.value = true
    
    const response = await userApi.getCurrentUser()
    console.log('获取用户信息API响应：', response)
    
    if (response.code === 200 && response.data) {
      // 映射后端数据到前端数据结构
      userInfo.value = {
        id: response.data.id,
        avatar: response.data.avatar || '',
        nickname: response.data.nickname || '',
        sex: response.data.sex || 0
      }
      console.log('用户信息加载成功：', userInfo.value)
    } else {
      console.error('获取用户信息失败：', response.message || '未知错误')
      uni.showToast({
        title: response.message || '获取用户信息失败',
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

// 保存用户资料
const saveProfile = async () => {
  try {
    console.log('开始保存用户资料：', userInfo.value)
    saving.value = true

    // 构建更新数据
    const updateData = {
      id: userInfo.value.id,
      nickname: userInfo.value.nickname,
      avatar: userInfo.value.avatar,
      sex: userInfo.value.sex
    }

    const response = await userApi.updateUser(updateData)
    console.log('更新用户信息API响应：', response)

    if (response.code === 200) {
      console.log('用户信息更新成功')
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
    } else {
      console.error('更新用户信息失败：', response.message || '未知错误')
      uni.showToast({
        title: response.message || '保存失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('保存用户资料异常：', error)
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    })
  } finally {
    saving.value = false
  }
}

// 返回上一页
const goBack = () => {
  console.log('返回上一页')
  
  try {
    // 获取当前页面栈
    const pages = getCurrentPages()
    console.log('当前页面栈长度：', pages.length)
    
    if (pages.length > 1) {
      // 如果页面栈中有上一页，直接返回
      console.log('使用navigateBack返回上一页')
      uni.navigateBack({
        delta: 1,
        success: () => {
          console.log('成功返回上一页')
        },
        fail: (error) => {
          console.error('返回失败：', error)
          // 如果返回失败，跳转到设置页面
          fallbackToSetting()
        }
      })
    } else {
      // 如果没有上一页，跳转到设置页面
      console.log('没有上一页，跳转到设置页面')
      fallbackToSetting()
    }
  } catch (error) {
    console.error('返回处理异常：', error)
    fallbackToSetting()
  }
}

// 备用方案：跳转到设置页面
const fallbackToSetting = () => {
  console.log('使用备用方案跳转到设置页面')
  uni.navigateTo({
    url: '/pages/parent/profile/details/setting',
    success: () => {
      console.log('成功跳转到设置页面')
    },
    fail: (error) => {
      console.error('跳转到设置页面失败：', error)
      // 最后备用方案：跳转到个人中心
      uni.switchTab({
        url: '/pages/parent/profile/profile'
      })
    }
  })
}

// 处理头像点击
const handleAvatarClick = () => {
  console.log('点击头像，准备选择图片')
  
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      console.log('选择图片成功：', res.tempFilePaths[0])
      
      // 显示上传进度
      uni.showLoading({
        title: '上传头像中...'
      })
      
      try {
        // 先上传图片到服务器
        await uploadAvatar(res.tempFilePaths[0])
      } catch (error) {
        console.error('上传头像失败：', error)
        uni.hideLoading()
        uni.showToast({
          title: '上传头像失败',
          icon: 'none'
        })
      }
    },
    fail: (error) => {
      console.error('选择图片失败：', error)
      uni.showToast({
        title: '选择图片失败',
        icon: 'none'
      })
    }
  })
}

// 上传头像到服务器
const uploadAvatar = async (filePath) => {
  try {
    console.log('开始上传头像到服务器：', filePath)
    
    // 使用uni.uploadFile上传文件
    const uploadResult = await new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8888/api/file/upload',
        filePath: filePath,
        name: 'file',
        formData: {
          type: 'image',
          path: 'register/avatar'
        },
        header: {
          'accessToken': uni.getStorageSync('token')
        },
        success: (uploadRes) => {
          console.log('文件上传成功：', uploadRes)
          try {
            const data = JSON.parse(uploadRes.data)
            resolve(data)
          } catch (parseError) {
            console.error('解析上传结果失败：', parseError)
            reject(parseError)
          }
        },
        fail: (error) => {
          console.error('文件上传失败：', error)
          reject(error)
        }
      })
    })
    
    console.log('上传结果：', uploadResult)
    
    if (uploadResult.code === 200 && uploadResult.data && uploadResult.data.originUrl) {
      // 上传成功，更新头像URL
      userInfo.value.avatar = uploadResult.data.originUrl
      console.log('头像URL更新为：', userInfo.value.avatar)
      
      uni.hideLoading()
      uni.showToast({
        title: '头像上传成功，请点击保存',
        icon: 'success'
      })
    } else {
      throw new Error(uploadResult.message || '上传失败')
    }
  } catch (error) {
    console.error('上传头像异常：', error)
    uni.hideLoading()
    throw error
  }
}

// 处理昵称点击
const handleNicknameClick = () => {
  console.log('点击昵称，准备修改')
  
  uni.showModal({
    title: '修改昵称',
    editable: true,
    placeholderText: '请输入昵称',
    content: userInfo.value.nickname,
    success: (res) => {
      if (res.confirm && res.content && res.content.trim()) {
        console.log('昵称修改为：', res.content.trim())
        userInfo.value.nickname = res.content.trim()
        
        uni.showToast({
          title: '昵称已修改，请点击保存',
          icon: 'none'
        })
      } else if (res.confirm && !res.content.trim()) {
        uni.showToast({
          title: '昵称不能为空',
          icon: 'none'
        })
      }
    }
  })
}

// 处理性别点击
const handleGenderClick = () => {
  console.log('点击性别，准备选择')
  
  uni.showActionSheet({
    itemList: ['男', '女'],
    success: (res) => {
      console.log('选择性别：', res.tapIndex === 0 ? '男' : '女')
      userInfo.value.sex = res.tapIndex // 0:男 1:女
      
      uni.showToast({
        title: '性别已修改，请点击保存',
        icon: 'none'
      })
    },
    fail: (error) => {
      console.error('选择性别失败：', error)
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

/* 加载状态样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
}

.loading-text {
  color: #9ca3af;
  font-size: 28rpx;
}

/* 个人资料表单样式 */
.profile-form {
  width: 100%;
}

/* 个人资料项样式 */
.profile-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 20rpx;
  background-color: #ffffff;
  margin-bottom: 2rpx;
}

.profile-item:first-child {
  border-top-left-radius: 16rpx;
  border-top-right-radius: 16rpx;
}

.profile-item:last-child {
  border-bottom-left-radius: 16rpx;
  border-bottom-right-radius: 16rpx;
  margin-bottom: 0;
}

.item-label {
  font-size: 30rpx;
  color: #111827;
  font-weight: 500;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 2rpx solid #e5e7eb;
}

.item-value {
  font-size: 28rpx;
  color: #6b7280;
  max-width: 400rpx;
  text-align: right;
}

.arrow-icon {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 保存按钮区域 */
.save-section {
  margin-top: 60rpx;
  padding: 0 20rpx;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  background-color: #3b82f6;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 16rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.save-btn:disabled {
  background-color: #9ca3af;
  opacity: 0.6;
}

.save-btn:not(:disabled):active {
  background-color: #2563eb;
  transform: scale(0.98);
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
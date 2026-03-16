<template>
  <view class="container">
    <view class="header">
      <view class="header-content">
        <view class="back-button" @tap="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">个人资料</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-content">
      <!-- 加载状态 -->
      <view v-if="loading" class="loading-container">
        <text class="loading-text">加载中...</text>
            </view>

      <!-- 个人资料表单 -->
      <view v-else class="profile-form">
        <!-- 头像设置 -->
        <view class="profile-item" @tap="handleAvatarClick">
          <text class="item-label">头像</text>
          <view class="item-right">
            <image 
              class="avatar" 
              :src="displayAvatar" 
              mode="aspectFill"
              @error="handleAvatarError"
            ></image>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>

        <!-- 用户名（只读） -->
        <view class="profile-item">
          <text class="item-label">用户名</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.username || '未设置' }}</text>
          </view>
        </view>

        <!-- 昵称设置 -->
        <view class="profile-item" @tap="handleNicknameClick">
          <text class="item-label">昵称</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.nickname || '未设置' }}</text>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>

        <!-- 性别设置 -->
        <view class="profile-item" @tap="handleGenderClick">
          <text class="item-label">性别</text>
          <view class="item-right">
            <text class="item-value">{{ genderText }}</text>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>

        <!-- 个性签名设置 -->
        <view class="profile-item" @tap="handleSignatureClick">
          <text class="item-label">个性签名</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.signature || '未设置' }}</text>
            <text class="fas fa-chevron-right arrow-icon"></text>
          </view>
        </view>

        <!-- 保存按钮 -->
        <view class="save-section">
          <button 
            class="save-btn" 
            @tap="handleSubmit"
            :disabled="saving"
          >
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { userApi, fileApi } from '@/utils/api.js'

export default {
  data() {
    return {
      userInfo: {
        id: null,
        username: '',
        nickname: '',
        avatar: '',
        gender: 0,
        signature: ''
      },
      defaultAvatar: 'https://ui-avatars.com/api/?name=用户&background=6366f1&color=fff&size=128',
      loading: true,
      saving: false,
      avatarError: false
    }
  },
  computed: {
    // 显示的头像（带错误处理）
    displayAvatar() {
      if (this.avatarError || !this.userInfo.avatar) {
        return this.defaultAvatar
      }
      return this.userInfo.avatar
    },
    // 性别文本
    genderText() {
      const genderMap = {
        0: '男',
        1: '女',
        2: '其他'
      }
      return genderMap[this.userInfo.gender] || '未设置'
    }
  },
  onLoad() {
    this.loadUserInfo()
  },
  methods: {
    // 加载用户信息
    async loadUserInfo() {
      try {
        this.loading = true
        console.log('[儿童端个人资料] 开始加载用户信息')
        
        // 从本地存储获取用户信息（作为备用）
        const storedUserInfo = uni.getStorageSync('userInfo')
        if (storedUserInfo) {
          let parsedUserInfo = storedUserInfo
          if (typeof storedUserInfo === 'string') {
            try {
              parsedUserInfo = JSON.parse(storedUserInfo)
            } catch (e) {
              console.warn('[儿童端个人资料] 解析本地用户信息失败:', e)
            }
          }
          this.userInfo = { ...this.userInfo, ...parsedUserInfo }
          console.log('[儿童端个人资料] 从本地存储加载用户信息:', this.userInfo)
        }
        
        // 优先从API获取最新信息
        try {
          const response = await userApi.getCurrentUser()
          console.log('[儿童端个人资料] API返回数据:', response)
          
          if (response?.code === 200 && response?.data) {
            this.updateUserInfo(response.data)
            // 同步更新本地存储
            uni.setStorageSync('userInfo', this.userInfo)
          } else {
            console.warn('[儿童端个人资料] API返回异常:', response)
          }
        } catch (apiError) {
          console.warn('[儿童端个人资料] API获取用户信息失败，使用本地数据:', apiError)
        }
        
      } catch (error) {
        console.error('[儿童端个人资料] 加载用户信息失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    // 更新用户信息
    updateUserInfo(apiData) {
      this.userInfo = {
        id: apiData.id,
        username: apiData.username || apiData.nickname || '',
        nickname: apiData.nickname || apiData.username || '',
        avatar: apiData.avatar || '',
        gender: apiData.gender !== undefined ? apiData.gender : 0,
        signature: apiData.signature || ''
      }
      // 重置头像错误状态
      this.avatarError = false
      
      console.log('[儿童端个人资料] 用户信息已更新:', this.userInfo)
    },
    
    // 头像加载错误处理
    handleAvatarError() {
      console.log('[儿童端个人资料] 头像加载失败，使用默认头像')
      this.avatarError = true
    },
    
    // 处理头像点击
    handleAvatarClick() {
      console.log('[儿童端个人资料] 点击头像，准备选择图片')
      
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          console.log('[儿童端个人资料] 选择图片成功:', res.tempFilePaths[0])
          
          uni.showLoading({
            title: '上传头像中...'
          })
          
          try {
            await this.uploadAvatar(res.tempFilePaths[0])
          } catch (error) {
            console.error('[儿童端个人资料] 上传头像失败:', error)
            uni.hideLoading()
            uni.showToast({
              title: '上传头像失败',
              icon: 'none'
            })
          }
        },
        fail: (error) => {
          console.error('[儿童端个人资料] 选择图片失败:', error)
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 上传头像到服务器
    async uploadAvatar(file) {
      try {
        console.log('[儿童端个人资料] 开始上传头像到服务器:', file)
        
        // 使用统一的fileApi上传
        const uploadResult = await fileApi.uploadImage(file, 'register/avatar')
        
        console.log('[儿童端个人资料] 上传结果:', uploadResult)
        
        if (uploadResult.code === 200 && uploadResult.data && uploadResult.data.originUrl) {
          this.userInfo.avatar = uploadResult.data.originUrl
          this.avatarError = false
          console.log('[儿童端个人资料] 头像URL更新为:', this.userInfo.avatar)
        
        uni.hideLoading()
        uni.showToast({
            title: '头像上传成功，请点击保存',
          icon: 'success'
        })
        } else {
          throw new Error(uploadResult.message || '上传失败')
        }
      } catch (error) {
        console.error('[儿童端个人资料] 上传头像异常:', error)
        uni.hideLoading()
        throw error
      }
    },
    
    // 处理昵称点击
    handleNicknameClick() {
      console.log('[儿童端个人资料] 点击昵称，准备修改')
      
      uni.showModal({
        title: '修改昵称',
        editable: true,
        placeholderText: '请输入昵称',
        content: this.userInfo.nickname,
        success: (res) => {
          if (res.confirm && res.content && res.content.trim()) {
            console.log('[儿童端个人资料] 昵称修改为:', res.content.trim())
            this.userInfo.nickname = res.content.trim()
            
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
    },
    
    // 处理性别点击
    handleGenderClick() {
      console.log('[儿童端个人资料] 点击性别，准备选择')
      
      uni.showActionSheet({
        itemList: ['男', '女', '其他'],
        success: (res) => {
          console.log('[儿童端个人资料] 选择性别索引:', res.tapIndex)
          this.userInfo.gender = res.tapIndex
          
          uni.showToast({
            title: '性别已修改，请点击保存',
            icon: 'none'
          })
        },
        fail: (error) => {
          console.error('[儿童端个人资料] 选择性别失败:', error)
        }
      })
    },
    
    // 处理个性签名点击
    handleSignatureClick() {
      console.log('[儿童端个人资料] 点击个性签名，准备修改')
      
      uni.showModal({
        title: '修改个性签名',
        editable: true,
        placeholderText: '写一句话介绍自己',
        content: this.userInfo.signature,
        success: (res) => {
          if (res.confirm) {
            console.log('[儿童端个人资料] 个性签名修改为:', res.content)
            this.userInfo.signature = res.content ? res.content.trim() : ''
            
            uni.showToast({
              title: '个性签名已修改，请点击保存',
              icon: 'none'
            })
          }
        }
      })
    },
    
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack({ delta: 1 })
      } else {
        uni.navigateTo({
          url: '/pages/child/profile-detail/settings'
        })
      }
    },
    async handleSubmit() {
      try {
        this.saving = true
        uni.showLoading({ title: '保存中...' })
        
        console.log('[儿童端个人资料] 保存用户信息:', this.userInfo)
        
        // 构建更新数据
        const updateData = {
          id: this.userInfo.id,
          username: this.userInfo.username,
          nickname: this.userInfo.nickname,
          avatar: this.userInfo.avatar,
          gender: this.userInfo.gender,
          signature: this.userInfo.signature
        }
        
          const response = await userApi.updateUser(updateData)
        console.log('[儿童端个人资料] 更新用户信息API响应:', response)
        
        if (response?.code === 200) {
            // 更新本地存储
            uni.setStorageSync('userInfo', this.userInfo)
            
          console.log('[儿童端个人资料] 用户信息更新成功')
            uni.hideLoading()
            uni.showToast({
              title: '保存成功',
              icon: 'success'
            })
            
            setTimeout(() => {
            this.goBack()
            }, 1500)
          } else {
          console.error('[儿童端个人资料] 更新用户信息失败:', response?.message || '未知错误')
          uni.hideLoading()
          uni.showToast({
            title: response?.message || '保存失败',
            icon: 'none'
          })
        }
        
      } catch (error) {
        console.error('[儿童端个人资料] 保存用户资料异常:', error)
        uni.hideLoading()
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.saving = false
      }
    }
  }
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

.header {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 40rpx 32rpx;
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
  position: relative;
}

.back-button {
  padding: 16rpx;
  position: absolute;
  left: 0;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
}

.main-content {
  margin-top: 120rpx;
  padding: 32rpx;
  height: calc(100vh - 120rpx);
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
  transition: background-color 0.2s ease;
}

.profile-item:active {
  background-color: #f9fafb;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  font-size: 24rpx;
  color: #9ca3af;
  flex-shrink: 0;
}

/* 保存按钮区域 */
.save-section {
  margin-top: 60rpx;
  padding: 0 20rpx;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 44rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.2);
  transition: all 0.3s ease;
}

.save-btn:disabled {
  background: #9ca3af;
  opacity: 0.6;
  box-shadow: none;
}

.save-btn:not(:disabled):active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.3);
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
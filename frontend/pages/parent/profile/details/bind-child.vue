<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">绑定家人</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- 搜索区域 -->
      <view class="search-section">
        <text class="section-title">添加家庭成员</text>
        <view class="form-item">
          <text class="form-label">用户名</text>
          <input 
            v-model="searchUsername"
            class="form-input" 
            placeholder="请输入家庭成员的用户名"
            @input="onSearchInput"
          />
        </view>
        <view class="form-item">
          <text class="form-label">关系类型</text>
          <picker 
            :value="relationTypeIndex" 
            :range="relationTypes"
            range-key="label"
            @change="onRelationTypeChange"
            class="relation-picker"
          >
            <view class="picker-display">
              {{ selectedRelationType ? selectedRelationType.label : '请选择关系类型' }}
              <text class="fas fa-chevron-down picker-arrow"></text>
            </view>
          </picker>
        </view>
        <view class="button-container">
          <button 
            class="bind-btn" 
            :disabled="!searchUsername.trim() || !selectedRelationType || loading"
            @click="bindChild"
          >
            {{ loading ? '绑定中...' : '绑定家庭成员' }}
          </button>
        </view>
        <text class="search-tip">提示：请确保输入的是准确的用户名，并选择正确的关系类型</text>
      </view>

      <!-- 已绑定的家庭成员列表 -->
      <view class="children-section">
        <text class="section-title">已绑定的家庭成员 ({{ childrenList.length }})</text>
        <view v-if="childrenList.length === 0" class="empty-state">
          <text class="fas fa-users empty-icon"></text>
          <text class="empty-text">还没有绑定任何家庭成员</text>
          <text class="empty-tip">使用上方表单添加家庭成员</text>
        </view>
        <view v-else class="children-list">
          <view 
            v-for="child in childrenList" 
            :key="child.id" 
            class="child-item"
          >
            <view class="child-avatar">
              <image 
                :src="child.avatarThumb || child.avatar || '/static/images/avatars/default-child.png'" 
                class="avatar-img"
                mode="aspectFill"
              />
            </view>
            <view class="child-info">
              <text class="child-nickname">{{ child.nickname }}</text>
              <text class="child-username">@{{ child.username }}</text>
              <text class="relation-type" v-if="child.relationType">关系：{{ child.relationType }}</text>
              <text class="bind-time">绑定时间：{{ formatTime(child.bindTime) }}</text>
            </view>
            <view class="child-actions">
              <button 
                class="unbind-btn" 
                @click="showUnbindConfirm(child)"
                :disabled="unbindingId === child.id"
              >
                {{ unbindingId === child.id ? '解绑中...' : '解绑' }}
              </button>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 不使用uni-popup组件，改用uni.showModal -->
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

// 响应式数据
const searchUsername = ref('')
const loading = ref(false)
const childrenList = ref([])
const unbindingId = ref(null)
const relationTypeIndex = ref(-1)
const selectedRelationType = ref(null)

// 关系类型选项
const relationTypes = ref([
  // 父母子女关系
  { value: '父子', label: '父子（我是父亲，对方是儿子）' },
  { value: '父女', label: '父女（我是父亲，对方是女儿）' },
  { value: '母子', label: '母子（我是母亲，对方是儿子）' },
  { value: '母女', label: '母女（我是母亲，对方是女儿）' },
  { value: '子父', label: '子父（我是儿子，对方是父亲）' },
  { value: '子母', label: '子母（我是儿子，对方是母亲）' },
  { value: '女父', label: '女父（我是女儿，对方是父亲）' },
  { value: '女母', label: '女母（我是女儿，对方是母亲）' },
  
  // 夫妻关系
  { value: '夫妻', label: '夫妻' },
  
  // 兄弟姐妹关系
  { value: '兄弟', label: '兄弟' },
  { value: '兄妹', label: '兄妹（我是哥哥，对方是妹妹）' },
  { value: '姐弟', label: '姐弟（我是姐姐，对方是弟弟）' },
  { value: '姐妹', label: '姐妹' },
  { value: '妹兄', label: '妹兄（我是妹妹，对方是哥哥）' },
  { value: '弟兄', label: '弟兄（我是弟弟，对方是哥哥）' },
  
  // 祖孙关系
  { value: '祖孙', label: '祖孙（我是爷爷，对方是孙子）' },
  { value: '祖女', label: '祖女（我是爷爷，对方是孙女）' },
  { value: '奶孙', label: '奶孙（我是奶奶，对方是孙子）' },
  { value: '奶女', label: '奶女（我是奶奶，对方是孙女）' },
  { value: '孙祖', label: '孙祖（我是孙子，对方是爷爷）' },
  { value: '孙奶', label: '孙奶（我是孙子，对方是奶奶）' },
  { value: '女祖', label: '女祖（我是孙女，对方是爷爷）' },
  { value: '女奶', label: '女奶（我是孙女，对方是奶奶）' },
  
  // 外祖孙关系
  { value: '外祖孙', label: '外祖孙（我是外公，对方是外孙）' },
  { value: '外祖女', label: '外祖女（我是外公，对方是外孙女）' },
  { value: '外奶孙', label: '外奶孙（我是外婆，对方是外孙）' },
  { value: '外奶女', label: '外奶女（我是外婆，对方是外孙女）' },
  { value: '孙外祖', label: '孙外祖（我是外孙，对方是外公）' },
  { value: '孙外奶', label: '孙外奶（我是外孙，对方是外婆）' },
  { value: '女外祖', label: '女外祖（我是外孙女，对方是外公）' },
  { value: '女外奶', label: '女外奶（我是外孙女，对方是外婆）' },
  
  // 叔侄关系
  { value: '叔侄', label: '叔侄（我是叔叔，对方是侄子）' },
  { value: '叔女', label: '叔女（我是叔叔，对方是侄女）' },
  { value: '婶侄', label: '婶侄（我是婶婶，对方是侄子）' },
  { value: '婶女', label: '婶女（我是婶婶，对方是侄女）' },
  { value: '侄叔', label: '侄叔（我是侄子，对方是叔叔）' },
  { value: '侄婶', label: '侄婶（我是侄子，对方是婶婶）' },
  { value: '女叔', label: '女叔（我是侄女，对方是叔叔）' },
  { value: '女婶', label: '女婶（我是侄女，对方是婶婶）' },
  
  // 舅甥关系
  { value: '舅甥', label: '舅甥（我是舅舅，对方是外甥）' },
  { value: '舅女', label: '舅女（我是舅舅，对方是外甥女）' },
  { value: '舅妈甥', label: '舅妈甥（我是舅妈，对方是外甥）' },
  { value: '舅妈女', label: '舅妈女（我是舅妈，对方是外甥女）' },
  { value: '甥舅', label: '甥舅（我是外甥，对方是舅舅）' },
  { value: '甥舅妈', label: '甥舅妈（我是外甥，对方是舅妈）' },
  { value: '女舅', label: '女舅（我是外甥女，对方是舅舅）' },
  { value: '女舅妈', label: '女舅妈（我是外甥女，对方是舅妈）' },
  
  // 其他关系
  { value: '其他亲属', label: '其他亲属' }
])

// 页面加载时获取已绑定的孩子列表
onMounted(() => {
  console.log('绑定家人页面加载，开始获取家庭成员列表')
  getFamilyMembers()
})

// 返回上一页
const goBack = () => {
  // 获取页面栈信息
  const pages = getCurrentPages()
  console.log('当前页面栈长度：', pages.length)
  
  if (pages.length > 1) {
    // 有上一页，使用navigateBack
    console.log('使用navigateBack返回上一页')
    uni.navigateBack()
  } else {
    // 没有上一页（比如直接访问该页面），跳转到设置页面
    console.log('没有上一页，直接跳转到设置页面')
    uni.navigateTo({
      url: '/pages/parent/profile/details/setting'
    })
  }
}

// 获取家庭成员列表
const getFamilyMembers = async () => {
  console.log('开始调用获取家庭成员接口')
  try {
    const response = await request.get('/api/family/members')
    
    console.log('获取家庭成员响应：', response)
    
    if (response.code === 200) {
      childrenList.value = response.data.children || []
      console.log('已绑定的孩子列表：', childrenList.value)
    }
  } catch (error) {
    console.error('获取家庭成员异常：', error)
    // 如果error包含业务错误信息，request.js已经处理；否则显示网络错误
    if (error && error.message) {
      console.log('业务错误已由request.js处理，错误信息：', error.message)
    } else {
      uni.showToast({
        title: '网络错误，请重试',
        icon: 'none'
      })
    }
  }
}

// 搜索输入处理
const onSearchInput = (e) => {
  searchUsername.value = e.detail.value
}

// 关系类型选择处理
const onRelationTypeChange = (e) => {
  const index = e.detail.value
  relationTypeIndex.value = index
  selectedRelationType.value = relationTypes.value[index]
  console.log('选择关系类型：', selectedRelationType.value)
}

// 绑定家庭成员
const bindChild = async () => {
  const username = searchUsername.value.trim()
  if (!username) {
    uni.showToast({
      title: '请输入用户名',
      icon: 'none'
    })
    return
  }

  if (!selectedRelationType.value) {
    uni.showToast({
      title: '请选择关系类型',
      icon: 'none'
    })
    return
  }

  console.log('开始绑定家庭成员，用户名：', username, '关系类型：', selectedRelationType.value.value)
  loading.value = true

  try {
    // 使用URL参数方式传递用户名和关系类型
    const response = await request.post('/api/family/bind-child', null, {
      childUsername: username,
      relationType: selectedRelationType.value.value
    })

      console.log('绑定家人响应：', response)

      if (response.code === 200) {
        uni.showToast({
          title: '绑定成功',
          icon: 'success'
        })
        searchUsername.value = ''
        relationTypeIndex.value = -1
        selectedRelationType.value = null
        // 重新获取列表
        await getFamilyMembers()
      }
    } catch (error) {
      console.error('绑定家人异常：', error)
      // 如果error包含业务错误信息，显示具体错误；否则显示网络错误
      if (error && error.message) {
        // 业务错误，request.js已经显示了toast，这里不需要重复显示
        console.log('业务错误已由request.js处理，错误信息：', error.message)
      } else {
        // 真正的网络错误或其他异常
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      }
    } finally {
      loading.value = false
    }
}

// 显示解绑确认对话框
const showUnbindConfirm = (child) => {
  console.log('显示解绑确认对话框，孩子：', child)
  uni.showModal({
    title: '解绑确认',
    content: `确定要解除与 ${child.nickname} 的绑定关系吗？解绑后将无法查看该孩子的阅读记录。`,
    confirmText: '确认解绑',
    cancelText: '取消',
    confirmColor: '#ef4444',
    success: (res) => {
      if (res.confirm) {
        confirmUnbind(child)
      }
    }
  })
}

// 确认解绑
const confirmUnbind = async (child) => {
  console.log('确认解绑孩子，孩子ID：', child.id)
  unbindingId.value = child.id

  try {
    const response = await request.delete(`/api/family/unbind/${child.id}`)

    console.log('解绑孩子响应：', response)

    if (response.code === 200) {
      uni.showToast({
        title: '解绑成功',
        icon: 'success'
      })
      // 重新获取列表
      await getFamilyMembers()
    }
  } catch (error) {
    console.error('解绑孩子异常：', error)
    // 如果error包含业务错误信息，request.js已经处理；否则显示网络错误
    if (error && error.message) {
      console.log('业务错误已由request.js处理，错误信息：', error.message)
    } else {
      uni.showToast({
        title: '网络错误，请重试',
        icon: 'none'
      })
    }
  } finally {
    unbindingId.value = null
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  
  try {
    const date = new Date(timeStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch (error) {
    console.error('时间格式化错误：', error)
    return timeStr
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

/* 区块标题 */
.section-title {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
  padding: 0 20rpx;
  font-weight: 600;
}

/* 表单区域 */
.search-section {
  margin-bottom: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
  padding: 0 20rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #374151;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #ffffff;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #3b82f6;
  outline: none;
}

.relation-picker {
  width: 100%;
}

.picker-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #ffffff;
  color: #374151;
}

.picker-arrow {
  font-size: 24rpx;
  color: #9ca3af;
}

.button-container {
  padding: 0 20rpx;
  margin-bottom: 16rpx;
}

.bind-btn {
  width: 100%;
  height: 88rpx;
  background-color: #3b82f6;
  color: #ffffff;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
}

.bind-btn:disabled {
  background-color: #9ca3af;
  opacity: 0.6;
}

.search-tip {
  font-size: 24rpx;
  color: #9ca3af;
  padding: 0 20rpx;
  line-height: 1.4;
}

/* 孩子列表区域 */
.children-section {
  margin-bottom: 30rpx;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  margin: 0 20rpx;
}

.empty-icon {
  font-size: 80rpx;
  color: #d1d5db;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #6b7280;
  margin-bottom: 12rpx;
  font-weight: 500;
}

.empty-tip {
  font-size: 24rpx;
  color: #9ca3af;
  text-align: center;
  line-height: 1.4;
}

/* 孩子列表 */
.children-list {
  background-color: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  margin: 0 20rpx;
}

.child-item {
  display: flex;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.child-item:last-child {
  border-bottom: none;
}

.child-avatar {
  margin-right: 20rpx;
}

.avatar-img {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 2rpx solid #e5e7eb;
}

.child-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.child-nickname {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
}

.child-username {
  font-size: 26rpx;
  color: #6b7280;
}

.relation-type {
  font-size: 24rpx;
  color: #3b82f6;
  font-weight: 500;
}

.bind-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.child-actions {
  margin-left: 20rpx;
}

.unbind-btn {
  height: 60rpx;
  padding: 0 24rpx;
  background-color: #ef4444;
  color: #ffffff;
  border: none;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.unbind-btn:disabled {
  background-color: #fca5a5;
  opacity: 0.6;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 
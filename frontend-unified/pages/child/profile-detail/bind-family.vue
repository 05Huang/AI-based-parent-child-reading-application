<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="page-title">家庭成员</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content" :style="{ marginTop: headerHeight + 'px' }">
      <!-- 统计信息卡片 -->
      <view class="section">
        <view class="stats-card">
          <view class="stats-icon">
            <text class="fas fa-users"></text>
          </view>
          <view class="stats-content">
            <text class="stat-label">已绑定成员</text>
            <view class="stat-value-wrapper">
              <text class="stat-value">{{ familyMembers.length }}</text>
              <text class="stat-unit">人</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 快速操作按钮 -->
      <view class="section">
        <view class="quick-actions">
          <view class="action-btn primary" @click="focusSearchInput">
            <view class="action-icon">
              <text class="fas fa-user-plus"></text>
            </view>
            <text class="action-text">添加成员</text>
          </view>
          <view class="action-btn secondary" @click="scanQRCode">
            <view class="action-icon">
              <text class="fas fa-qrcode"></text>
            </view>
            <text class="action-text">扫码绑定</text>
          </view>
        </view>
      </view>

      <!-- 添加家庭成员表单 -->
      <view class="section">
        <view class="form-card">
          <view class="section-header">
            <text class="section-title">添加家庭成员</text>
          </view>
          
          <view class="form-item">
            <view class="form-label-wrapper">
              <text class="form-icon fas fa-user"></text>
              <text class="form-label">成员用户名</text>
            </view>
            <input 
              ref="searchInput"
              v-model="searchUsername"
              class="form-input" 
              placeholder="输入要绑定的用户名"
              @input="onSearchInput"
            />
          </view>

          <view class="form-item">
            <view class="form-label-wrapper">
              <text class="form-icon fas fa-heart"></text>
              <text class="form-label">家庭关系</text>
            </view>
            <picker 
              :value="relationTypeIndex" 
              :range="relationTypes"
              range-key="label"
              @change="onRelationTypeChange"
              class="relation-picker"
            >
              <view class="picker-display" :class="{ 'has-value': selectedRelationType }">
                <text class="picker-text">{{ selectedRelationType ? selectedRelationType.label : '点击选择关系' }}</text>
                <text class="fas fa-chevron-down picker-arrow"></text>
              </view>
            </picker>
          </view>

          <view class="button-container">
            <view 
              class="bind-btn" 
              :class="{ 'disabled': !searchUsername.trim() || !selectedRelationType || loading }"
              @click="bindFamily"
            >
              <text class="fas fa-check bind-icon"></text>
              <text class="bind-text">{{ loading ? '绑定中...' : '确认绑定' }}</text>
            </view>
          </view>

          <view class="form-tip">
            <view class="tip-icon">
              <text class="fas fa-info-circle"></text>
            </view>
            <text class="tip-text">提示：请输入准确的用户名，选择正确的家庭关系</text>
          </view>
        </view>
      </view>

      <!-- 已绑定的家庭成员列表 -->
      <view class="section" v-if="familyMembers.length > 0">
        <view class="section-header">
          <text class="section-title">已绑定成员</text>
          <text class="section-count">({{ familyMembers.length }})</text>
        </view>
        <view class="members-list">
          <view 
            v-for="member in familyMembers" 
            :key="member.id" 
            class="member-card"
          >
            <view class="member-avatar-wrapper">
              <image 
                :src="member.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(member.nickname || member.username || '用户')}&background=8b5cf6&color=fff&size=128`" 
                class="member-avatar"
                mode="aspectFill"
              />
            </view>

            <view class="member-info">
              <view class="member-name-row">
                <text class="member-name">{{ member.nickname || member.username }}</text>
                <view class="relation-tag" v-if="member.relationType">
                  <text class="relation-text">{{ formatRelationType(member.relationType) }}</text>
                </view>
              </view>
              <text class="member-username">@{{ member.username }}</text>
              <view class="bind-time" v-if="member.bindTime">
                <text class="fas fa-calendar-alt time-icon"></text>
                <text class="time-text">{{ formatTime(member.bindTime) }}</text>
              </view>
            </view>

            <view class="member-actions">
              <view 
                class="action-unbind" 
                @click="showUnbindConfirm(member)"
                :class="{ 'disabled': unbindingId === member.id }"
              >
                <text class="fas fa-trash-alt"></text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="section">
        <view class="empty-state">
          <view class="empty-icon-wrapper">
            <text class="fas fa-users empty-icon"></text>
          </view>
          <text class="empty-title">还没有绑定家庭成员</text>
          <text class="empty-desc">添加家庭成员后，可以互相查看阅读进度和家庭分析</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { familyApi } from '@/utils/api.js'
import eventBus, { EVENTS } from '@/utils/eventBus.js'

// 响应式数据
const searchUsername = ref('')
const loading = ref(false)
const familyMembers = ref([])
const unbindingId = ref(null)
const relationTypeIndex = ref(-1)
const selectedRelationType = ref(null)
const searchInput = ref(null)
const statusBarHeight = ref(20)
const headerHeight = ref(100)

// 关系类型选项 - 分类组织
const relationTypes = ref([
  // 父母子女关系
  { value: '父子', label: '父子（我是儿子，对方是父亲）', category: '亲子' },
  { value: '父女', label: '父女（我是女儿，对方是父亲）', category: '亲子' },
  { value: '母子', label: '母子（我是儿子，对方是母亲）', category: '亲子' },
  { value: '母女', label: '母女（我是女儿，对方是母亲）', category: '亲子' },
  { value: '子父', label: '子父（我是父亲，对方是儿子）', category: '亲子' },
  { value: '子母', label: '子母（我是母亲，对方是儿子）', category: '亲子' },
  { value: '女父', label: '女父（我是父亲，对方是女儿）', category: '亲子' },
  { value: '女母', label: '女母（我是母亲，对方是女儿）', category: '亲子' },
  
  // 兄弟姐妹关系
  { value: '兄弟', label: '兄弟', category: '兄妹' },
  { value: '兄妹', label: '兄妹（我是妹妹，对方是哥哥）', category: '兄妹' },
  { value: '姐弟', label: '姐弟（我是弟弟，对方是姐姐）', category: '兄妹' },
  { value: '姐妹', label: '姐妹', category: '兄妹' },
  
  // 祖孙关系
  { value: '祖孙', label: '祖孙（我是孙子，对方是爷爷）', category: '祖孙' },
  { value: '奶孙', label: '奶孙（我是孙子，对方是奶奶）', category: '祖孙' },
  
  // 其他关系
  { value: '其他亲属', label: '其他亲属', category: '其他' }
])

// 页面加载时获取已绑定的家庭成员列表
onMounted(() => {
  console.log('[儿童端绑定家人] 页面加载，开始获取家庭成员列表')
  // 获取系统状态栏高度
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  headerHeight.value = statusBarHeight.value + 64 + 32
  getFamilyMembers()
})

// 返回上一页
const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.navigateTo({
      url: '/pages/child/profile-detail/settings'
    })
  }
}

// 获取家庭成员列表
const getFamilyMembers = async () => {
  console.log('[儿童端绑定家人] 开始调用获取家庭成员接口')
  try {
    const response = await familyApi.getFamilyMembers()
    console.log('[儿童端绑定家人] 获取家庭成员响应：', response)
    
    if (response?.code === 200 && response?.data) {
      // 合并所有家庭成员
      const allMembers = []
      
      const memberTypes = ['children', 'parents', 'spouse', 'siblings', 'grandparents', 'grandchildren', 'others']
      
      memberTypes.forEach(type => {
        const members = response.data[type]
        if (type === 'spouse' && members) {
          allMembers.push(members)
        } else if (Array.isArray(members) && members.length > 0) {
          allMembers.push(...members)
        }
      })
      
      familyMembers.value = allMembers
      console.log('[儿童端绑定家人] 已绑定的家庭成员列表：', familyMembers.value)
    }
  } catch (error) {
    console.error('[儿童端绑定家人] 获取家庭成员异常：', error)
    uni.showToast({
      title: '获取成员列表失败',
      icon: 'none'
    })
  }
}

// 焦点输入框
const focusSearchInput = () => {
  if (searchInput.value) {
    searchInput.value.focus()
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
  console.log('[儿童端绑定家人] 选择关系类型：', selectedRelationType.value)
}

// 绑定家庭成员
const bindFamily = async () => {
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

  console.log('[儿童端绑定家人] 开始绑定家庭成员，用户名：', username, '关系类型：', selectedRelationType.value.value)
  loading.value = true

  try {
    const response = await familyApi.bindFamilyMember(username, selectedRelationType.value.value)
    console.log('[儿童端绑定家人] 绑定家人响应：', response)

    if (response?.code === 200) {
      uni.showToast({
        title: '绑定成功！',
        icon: 'success',
        duration: 1500
      })
      
      searchUsername.value = ''
      relationTypeIndex.value = -1
      selectedRelationType.value = null
      
      // 触发家人绑定事件
      eventBus.emit(EVENTS.CHILD_BOUND, {
        username: username,
        relationType: selectedRelationType.value?.value
      })
      
      // 重新获取列表
      await getFamilyMembers()
    }
  } catch (error) {
    console.error('[儿童端绑定家人] 绑定家人异常：', error)
    uni.showToast({
      title: error?.message || '绑定失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 扫码绑定
const scanQRCode = () => {
  console.log('[儿童端绑定家人] 开始扫描二维码')
  
  // #ifdef H5
  uni.showModal({
    title: '扫码功能提示',
    content: 'Web浏览器暂不支持扫码，请用以下方式绑定：\n• 手动输入用户名\n• 使用移动端APP扫码',
    confirmText: '我知道了',
    showCancel: false
  })
  // #endif
  
  // #ifdef APP-PLUS
  uni.scanCode({
    success: (res) => {
      console.log('[儿童端绑定家人] 扫码成功，结果：', res)
      handleScanResult(res.result)
    },
    fail: (err) => {
      console.error('[儿童端绑定家人] 扫码失败：', err)
      uni.showToast({
        title: '扫码失败，请重试',
        icon: 'none'
      })
    }
  })
  // #endif
  
  // #ifdef MP-WEIXIN
  uni.scanCode({
    onlyFromCamera: true,
    scanType: ['qrCode'],
    success: (res) => {
      console.log('[儿童端绑定家人] 扫码成功，结果：', res)
      handleScanResult(res.result)
    },
    fail: (err) => {
      console.error('[儿童端绑定家人] 扫码失败：', err)
      uni.showToast({
        title: '扫码失败，请重试',
        icon: 'none'
      })
    }
  })
  // #endif
}

// 处理扫码结果
const handleScanResult = async (scanResult) => {
  console.log('[儿童端绑定家人] 处理扫码结果：', scanResult)
  
  try {
    if (!scanResult.startsWith('qz-family-bind:')) {
      uni.showToast({
        title: '无效的二维码',
        icon: 'none'
      })
      return
    }
    
    const parts = scanResult.split(':')
    if (parts.length < 3) {
      uni.showToast({
        title: '二维码格式错误',
        icon: 'none'
      })
      return
    }
    
    const targetUsername = parts[2]
    console.log('[儿童端绑定家人] 扫描到的用户：', targetUsername)
    
    // 弹出关系选择对话框
    const relationTypeItems = relationTypes.value.map(item => item.label)
    
    uni.showActionSheet({
      title: `即将绑定 ${targetUsername}`,
      itemList: relationTypeItems,
      success: async (res) => {
        const selectedRelation = relationTypes.value[res.tapIndex]
        await bindByQRCode(targetUsername, selectedRelation.value)
      },
      fail: () => {
        console.log('[儿童端绑定家人] 用户取消选择关系类型')
      }
    })
  } catch (error) {
    console.error('[儿童端绑定家人] 解析二维码失败：', error)
    uni.showToast({
      title: '二维码解析失败',
      icon: 'none'
    })
  }
}

// 通过二维码绑定
const bindByQRCode = async (username, relationType) => {
  console.log('[儿童端绑定家人] 通过二维码绑定，用户名：', username)
  loading.value = true
  
  try {
    const response = await familyApi.bindFamilyMember(username, relationType)
    console.log('[儿童端绑定家人] 扫码绑定响应：', response)
    
    if (response?.code === 200) {
      uni.showToast({
        title: '绑定成功！',
        icon: 'success',
        duration: 1500
      })
      
      eventBus.emit(EVENTS.CHILD_BOUND, {
        username: username,
        relationType: relationType
      })
      
      await getFamilyMembers()
    }
  } catch (error) {
    console.error('[儿童端绑定家人] 扫码绑定异常：', error)
    uni.showToast({
      title: error?.message || '绑定失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 显示解绑确认对话框
const showUnbindConfirm = (member) => {
  console.log('[儿童端绑定家人] 显示解绑确认对话框')
  uni.showModal({
    title: '确认解除绑定',
    content: `确定要解除与 ${member.nickname || member.username} 的关系吗？`,
    confirmText: '确认解绑',
    cancelText: '取消',
    confirmColor: '#ef4444',
    success: (res) => {
      if (res.confirm) {
        confirmUnbind(member)
      }
    }
  })
}

// 确认解绑
const confirmUnbind = async (member) => {
  console.log('[儿童端绑定家人] 确认解绑成员ID：', member.id)
  unbindingId.value = member.id

  try {
    const response = await familyApi.unbindFamilyMember(member.id)
    console.log('[儿童端绑定家人] 解绑响应：', response)

    if (response?.code === 200) {
      uni.showToast({
        title: '已解除绑定',
        icon: 'success',
        duration: 1500
      })
      
      eventBus.emit(EVENTS.CHILD_UNBOUND, {
        memberId: member.id,
        memberName: member.nickname
      })
      
      await getFamilyMembers()
    }
  } catch (error) {
    console.error('[儿童端绑定家人] 解绑异常：', error)
    uni.showToast({
      title: error?.message || '解绑失败，请重试',
      icon: 'none'
    })
  } finally {
    unbindingId.value = null
  }
}

// 格式化关系类型（从孩子视角显示对方的关系）
const formatRelationType = (relationType) => {
  const typeMap = {
    // 父母子女关系（从孩子视角）
    '子父': '父亲',  // 我是儿子，对方是父亲
    '子母': '母亲',  // 我是儿子，对方是母亲
    '女父': '父亲',  // 我是女儿，对方是父亲
    '女母': '母亲',  // 我是女儿，对方是母亲
    '父子': '儿子',  // 我是父亲，对方是儿子
    '父女': '女儿',  // 我是父亲，对方是女儿
    '母子': '儿子',  // 我是母亲，对方是儿子
    '母女': '女儿',  // 我是母亲，对方是女儿
    // 兄弟姐妹关系
    '兄弟': '兄弟',
    '兄妹': '哥哥',  // 我是妹妹，对方是哥哥
    '姐弟': '姐姐',  // 我是弟弟，对方是姐姐
    '姐妹': '姐妹',
    // 祖孙关系
    '祖孙': '爷爷',  // 我是孙子，对方是爷爷
    '奶孙': '奶奶',  // 我是孙子，对方是奶奶
    // 其他关系
    '其他亲属': '亲属'
  }
  return typeMap[relationType] || relationType
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}年${month}月${day}日`
  } catch (error) {
    return timeStr
  }
}
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
}

/* 顶部导航栏样式 */
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
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 36rpx;
  transition: all 0.3s ease;
}

.back-btn:active {
  transform: scale(0.9);
  opacity: 0.8;
}

.page-title {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
  flex: 1;
  text-align: center;
}

.header-placeholder {
  width: 64rpx;
}

/* 主内容区域样式 */
.main-content {
  padding: 16rpx 24rpx 0;
  padding-bottom: 40rpx;
  width: 100%;
  box-sizing: border-box;
}

.section {
  margin-bottom: 32rpx;
  width: 100%;
  box-sizing: border-box;
}

.section:first-child {
  margin-top: 8rpx;
}

/* 统计卡片样式 */
.stats-card {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.25);
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.stats-icon {
  width: 120rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  color: #ffffff;
  flex-shrink: 0;
}

.stats-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.stat-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.stat-value-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.stat-value {
  font-size: 64rpx;
  font-weight: 700;
  color: #ffffff;
  line-height: 1;
}

.stat-unit {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 快速操作按钮 */
.quick-actions {
  display: flex;
  gap: 20rpx;
  width: 100%;
  box-sizing: border-box;
}

.action-btn {
  flex: 1;
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  transition: all 0.3s ease;
}

.action-btn:active {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.action-btn.primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.action-btn.secondary {
  background: linear-gradient(135deg, #6ee7b7, #34d399);
}

.action-icon {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  color: #ffffff;
}

.action-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
}

/* 区块标题样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2937;
}

.section-count {
  font-size: 28rpx;
  color: #6b7280;
  font-weight: normal;
}

/* 表单卡片 */
.form-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-item:last-of-type {
  margin-bottom: 0;
}

.form-label-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.form-icon {
  font-size: 28rpx;
  color: #6366f1;
}

.form-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  font-size: 28rpx;
  background-color: #f9fafb;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.form-input:focus {
  border-color: #6366f1;
  background-color: #ffffff;
  box-shadow: 0 0 0 4rpx rgba(99, 102, 241, 0.1);
}

.picker-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  font-size: 28rpx;
  background-color: #f9fafb;
  color: #9ca3af;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.picker-display.has-value {
  color: #374151;
  border-color: #6366f1;
  background-color: #ffffff;
}

.picker-text {
  flex: 1;
}

.picker-arrow {
  font-size: 24rpx;
  color: #d1d5db;
  margin-left: 12rpx;
}

.button-container {
  margin-top: 32rpx;
}

.bind-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
  transition: all 0.3s ease;
}

.bind-btn.disabled {
  background: #d1d5db;
  box-shadow: none;
  opacity: 0.6;
}

.bind-btn:active:not(.disabled) {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.3);
}

.bind-icon {
  font-size: 32rpx;
  color: #ffffff;
}

.bind-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.form-tip {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-top: 24rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border-radius: 12rpx;
  border: 1rpx solid #bae6fd;
}

.tip-icon {
  font-size: 28rpx;
  color: #0369a1;
  flex-shrink: 0;
  margin-top: 2rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #0369a1;
  line-height: 1.6;
  flex: 1;
}

/* 家庭成员列表 */
.members-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  width: 100%;
  box-sizing: border-box;
}

.member-card {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  width: 100%;
  box-sizing: border-box;
}

.member-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.member-avatar-wrapper {
  position: relative;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.member-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  border: 4rpx solid #f0f0f0;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.member-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.relation-tag {
  background: linear-gradient(135deg, #f3e8ff, #e9d5ff);
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  border: 1rpx solid #ddd6fe;
}

.relation-text {
  font-size: 22rpx;
  color: #8b5cf6;
  font-weight: 500;
}

.member-username {
  font-size: 26rpx;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bind-time {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 4rpx;
}

.time-icon {
  font-size: 22rpx;
  color: #9ca3af;
}

.time-text {
  font-size: 24rpx;
  color: #9ca3af;
}

.member-actions {
  margin-left: 16rpx;
  flex-shrink: 0;
}

.action-unbind {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #dc2626;
  transition: all 0.3s ease;
  box-shadow: 0 2rpx 8rpx rgba(220, 38, 38, 0.15);
}

.action-unbind:active:not(.disabled) {
  transform: scale(0.95);
  box-shadow: 0 1rpx 4rpx rgba(220, 38, 38, 0.2);
}

.action-unbind.disabled {
  opacity: 0.5;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx 80rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.empty-icon-wrapper {
  width: 160rpx;
  height: 160rpx;
  background: linear-gradient(135deg, #f3e8ff, #e9d5ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.empty-icon {
  font-size: 80rpx;
  color: #8b5cf6;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #6b7280;
  text-align: center;
  line-height: 1.8;
  max-width: 500rpx;
}

/* 滚动条隐藏 */
::-webkit-scrollbar {
  display: none;
}
</style>


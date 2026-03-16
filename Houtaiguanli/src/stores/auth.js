import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getCurrentUser } from '@/api/auth'

/**
 * 认证状态管理
 * 包括用户登录状态、token存储、用户信息管理
 */
export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 执行登录
   * @param {Object} credentials - 登录凭证 { username, password }
   * @returns {Promise}
   */
  async function doLogin(credentials) {
    try {
      console.log('开始登录，凭证:', credentials)
      const response = await login(credentials)
      console.log('登录API响应:', response)
      
      // 后端返回格式：LoginVO { accessToken, refreshToken, role, ... }
      if (response && response.accessToken) {
        // 保存tokens
        token.value = response.accessToken
        refreshToken.value = response.refreshToken || ''
        
        // 保存到本地存储
        localStorage.setItem('token', response.accessToken)
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken)
        }
        
        console.log('Token已保存，准备获取用户信息')
        
        // 可选：获取完整用户信息
        try {
          await fetchUserInfo()
        } catch (fetchError) {
          console.warn('获取用户信息失败，但登录成功:', fetchError)
          // 即使获取用户信息失败，也认为登录成功
        }
        
        return {
          success: true,
          message: '登录成功'
        }
      } else {
        console.error('登录响应数据格式错误:', response)
        throw new Error('登录失败：返回数据格式错误，缺少accessToken')
      }
    } catch (error) {
      console.error('登录错误详情:', error)
      console.error('错误响应:', error.response)
      throw error
    }
  }

  /**
   * 获取当前用户信息
   */
  async function fetchUserInfo() {
    try {
      const response = await getCurrentUser()
      if (response) {
        userInfo.value = response
        localStorage.setItem('userInfo', JSON.stringify(response))
        return response
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取失败，不影响已登录状态
      return null
    }
  }

  /**
   * 检查用户是否为管理员
   */
  const isAdmin = computed(() => {
    return userInfo.value?.userType === 2 || userInfo.value?.role === 'admin'
  })

  /**
   * 检查用户权限
   */
  const hasPermission = (requiredRole) => {
    if (!userInfo.value) return false
    return userInfo.value.userType === 2 // 管理员拥有所有权限
  }

  /**
   * 设置 Token（用于注册后自动登录）
   */
  function setToken(accessToken, refreshTokenValue) {
    token.value = accessToken
    refreshToken.value = refreshTokenValue || ''
    
    localStorage.setItem('token', accessToken)
    if (refreshTokenValue) {
      localStorage.setItem('refreshToken', refreshTokenValue)
    }
    
    console.log('Token已设置并保存')
  }

  /**
   * 重置状态（用于退出登录）
   */
  function resetState() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  /**
   * 获取用户昵称或用户名
   */
  const displayName = computed(() => {
    return userInfo.value?.nickname || userInfo.value?.username || '用户'
  })

  /**
   * 获取用户角色中文
   */
  const roleText = computed(() => {
    const roleMap = {
      1: '家长',
      2: '孩子',
      'parent': '家长',
      'child': '孩子'
    }
    return roleMap[userInfo.value?.role] || '未知'
  })

  return {
    // 状态
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    displayName,
    roleText,
    
    // 方法
    doLogin,
    fetchUserInfo,
    setToken,
    resetState,
    hasPermission
  }
})





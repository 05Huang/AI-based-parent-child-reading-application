import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 登录结果，包含token信息
 */
export function login(data) {
  // 后端LoginDTO使用userName字段，需要转换
  return request({
    url: '/api/user/login',
    method: 'post',
    data: {
      userName: data.username,  // 注意：后端使用userName（驼峰）
      password: data.password
    }
  })
}

/**
 * 获取当前登录用户信息
 * @returns {Promise} 当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: '/api/user/current',
    method: 'get'
  })
}

/**
 * 用户退出登录
 * @returns {Promise} 退出结果
 */
export function logout() {
  // 清除本地token即可，后端如果需要可以添加logout端点
  return Promise.resolve({
    code: 200,
    message: '退出登录成功'
  })
}

/**
 * 刷新Token
 * @param {string} refreshToken - 刷新令牌
 * @returns {Promise} 新的token信息
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/api/user/refresh-token',
    method: 'post',
    data: { refreshToken }
  })
}

/**
 * 检查邮箱是否已注册
 * @param {string} email - 邮箱地址
 * @returns {Promise} 检查结果
 */
export function checkEmail(email) {
  return request({
    url: `/api/user/check-email/${email}`,
    method: 'get'
  })
}

/**
 * 手机号验证码登录
 * @param {Object} data - 登录数据
 * @param {string} data.phone - 手机号
 * @param {string} data.verificationCode - 验证码
 * @returns {Promise} 登录结果
 */
export function loginByPhone(data) {
  return request({
    url: '/api/user/login-by-phone',
    method: 'post',
    data
  })
}

/**
 * 邮箱验证码登录
 * @param {Object} data - 登录数据
 * @param {string} data.email - 邮箱
 * @param {string} data.verificationCode - 验证码
 * @returns {Promise} 登录结果
 */
export function loginByEmail(data) {
  return request({
    url: '/api/user/login-by-email',
    method: 'post',
    data
  })
}

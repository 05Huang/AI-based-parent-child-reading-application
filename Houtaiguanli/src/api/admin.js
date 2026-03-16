import request from '@/utils/request'

/**
 * 管理员注册（不需要验证码）
 * 参考家长端的注册逻辑，简化版
 */
export function registerAdmin(data) {
  return request({
    url: '/api/admin/register',
    method: 'post',
    data
  })
}

/**
 * 检查用户名是否可用
 */
export function checkUsername(username) {
  return request({
    url: '/api/admin/check-username',
    method: 'get',
    params: { username }
  })
}

/**
 * 检查手机号是否可用
 */
export function checkPhone(phone) {
  return request({
    url: '/api/admin/check-phone',
    method: 'get',
    params: { phone }
  })
}

// ==================== 扩展管理员管理接口（可选） ====================

/**
 * 获取管理员列表（分页）
 */
export function getAdminList(params) {
  return request({
    url: '/api/admin/page',
    method: 'get',
    params
  })
}

/**
 * 获取管理员详情
 */
export function getAdminDetail(id) {
  return request({
    url: `/api/admin/${id}`,
    method: 'get'
  })
}

/**
 * 更新管理员信息
 */
export function updateAdmin(data) {
  return request({
    url: '/api/admin',
    method: 'put',
    data
  })
}

/**
 * 删除管理员
 */
export function deleteAdmin(id) {
  return request({
    url: `/api/admin/${id}`,
    method: 'delete'
  })
}

/**
 * 更新管理员状态
 */
export function updateAdminStatus(id, status) {
  return request({
    url: `/api/admin/${id}/status`,
    method: 'patch',
    params: { status }
  })
}




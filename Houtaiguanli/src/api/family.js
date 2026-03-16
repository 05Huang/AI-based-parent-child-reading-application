import request from '@/utils/request'

// 获取所有家长列表（假设后端会添加此接口，或通过用户API过滤）
export function getFamilyList(params) {
  return request({
    url: '/api/user/page',
    method: 'get',
    params: {
      ...params,
      excludeAdminAndOfficial: true,
      role: 1 // 家长角色
    }
  })
}

// 获取家庭详情（包括该家长的孩子）
export function getFamilyDetail(userId) {
  return request({
    url: `/api/family/admin/members/${userId}`,
    method: 'get'
  })
}

// 获取家庭统计信息
export function getFamilyStats() {
  return request({
    url: '/api/family/stats',
    method: 'get'
  })
}

// 获取用户信息
export function getUserInfo(userId) {
  return request({
    url: `/api/user/${userId}`,
    method: 'get'
  })
}

// 绑定孩子
export function bindChild(childUsername, relationType) {
  return request({
    url: '/api/family/bind-child',
    method: 'post',
    params: {
      childUsername,
      relationType
    }
  })
}

// 解除绑定
export function unbindChild(relativeId) {
  return request({
    url: `/api/family/unbind/${relativeId}`,
    method: 'delete'
  })
}

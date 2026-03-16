import request from '@/utils/request'

// 获取用户列表（分页）
export function getUserList(params) {
  return request({
    url: '/api/user/page',
    method: 'get',
    params
  })
}

// 获取单个用户详情
export function getUserDetail(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'get'
  })
}

// 获取当前登录用户的信息
export function getCurrentUser() {
  return request({
    url: '/api/user/current',
    method: 'get'
  })
}

// 添加用户（新增）
export function addUser(data) {
  return request({
    url: '/api/user',
    method: 'post',
    data
  })
}

// 更新用户信息（用户管理页面使用）
export function updateUser(data) {
  return request({
    url: '/api/user',
    method: 'put',
    data
  })
}

// 更新用户信息（个人信息页面使用 - 与updateUser相同）
export function updateUserInfo(id, data) {
  return request({
    url: '/api/user',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/api/user/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

// 检查用户名是否可用
export function checkUsernameAvailable(username) {
  return request({
    url: '/api/user/check/username',
    method: 'get',
    params: { username }
  })
}

// 上传用户头像
export function uploadUserAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', 'avatar')
  formData.append('path', 'user/avatar')
  
  return request({
    url: '/api/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 更新用户头像
export function updateUserAvatar(id, avatarUrl) {
  return request({
    url: `/api/user/${id}/avatar`,
    method: 'put',
    data: { avatar: avatarUrl }
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/api/user/change-password',
    method: 'post',
    data
  })
}





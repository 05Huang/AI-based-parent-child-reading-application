import request from '@/utils/request'

// 获取用户的群聊列表
export function getGroupList(params) {
  return request({
    url: '/api/groupMessage/groups',
    method: 'get',
    params
  })
}

// 获取所有群聊列表（管理后台使用）
export function getAllGroupList(params) {
  return request({
    url: '/api/groupMessage/admin/all-groups',
    method: 'get',
    params
  })
}

// 获取群组详情
export function getGroupDetail(groupId) {
  return request({
    url: `/api/groupMessage/${groupId}`,
    method: 'get'
  })
}

// 获取群组消息列表
export function getGroupMessages(groupId, params) {
  return request({
    url: `/api/groupMessage/${groupId}`,
    method: 'get',
    params
  })
}

// 获取群组成员列表
export function getGroupMembers(groupId, params) {
  return request({
    url: `/api/groupMessage/${groupId}/members`,
    method: 'get',
    params
  })
}

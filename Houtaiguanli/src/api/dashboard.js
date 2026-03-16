import request from '@/utils/request'

// 获取用户统计
export function getUserStats() {
  return request({
    url: '/api/user/page',
    method: 'get',
    params: { current: 1, size: 1, excludeAdminAndOfficial: true }
  })
}

// 获取文章统计
export function getArticleStats() {
  return request({
    url: '/api/content/page',
    method: 'get',
    params: { current: 1, size: 1, type: 1 }
  })
}

// 获取视频统计
export function getVideoStats() {
  return request({
    url: '/api/content/page',
    method: 'get',
    params: { current: 1, size: 1, type: 2 }
  })
}

// 获取评论统计
export function getCommentStats() {
  return request({
    url: '/api/comment/page',
    method: 'get',
    params: { current: 1, size: 1 }
  })
}

// 获取最新用户
export function getLatestUsers(limit = 10) {
  return request({
    url: '/api/user/page',
    method: 'get',
    params: { current: 1, size: limit, excludeAdminAndOfficial: true }
  })
}

// 获取最新内容（文章和视频）
export function getLatestContent(limit = 10) {
  return request({
    url: '/api/content/page',
    method: 'get',
    params: { current: 1, size: limit }
  })
}

// 获取内容分类统计
export function getCategoryStats() {
  return request({
    url: '/api/category/all',
    method: 'get'
  })
}





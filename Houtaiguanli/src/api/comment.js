import request from '@/utils/request'

// 获取评论列表（分页）
export function getCommentList(params) {
  return request({
    url: '/api/comment/page',
    method: 'get',
    params
  })
}

// 获取单个评论详情
export function getCommentDetail(id) {
  return request({
    url: `/api/comment/${id}`,
    method: 'get'
  })
}

// 更新评论状态（审核/通过/拒绝）
export function updateCommentStatus(id, status) {
  return request({
    url: `/api/comment/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

// 删除评论
export function deleteComment(id, deleteReason) {
  return request({
    url: `/api/comment/${id}`,
    method: 'delete',
    params: { deleteReason }
  })
}

// 批量删除评论
export function batchDeleteComments(ids, deleteReason) {
  return request({
    url: '/api/comment/batch',
    method: 'delete',
    data: ids,
    params: { deleteReason }
  })
}

// 批量更新评论状态
export function batchUpdateCommentStatus(ids, status) {
  return request({
    url: '/api/comment/batch/status',
    method: 'patch',
    data: ids,
    params: { status }
  })
}





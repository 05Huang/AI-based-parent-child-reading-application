import request from '@/utils/request'

// 获取文章列表（分页）
export function getArticleList(params) {
  return request({
    url: '/api/content/page',
    method: 'get',
    params: {
      ...params,
      type: params?.type !== undefined ? params.type : 1  // 如果调用者指定了 type，使用调用者的值；否则默认为 1（文章）
    }
  })
}

// 创建新文章
export function addArticle(data) {
  return request({
    url: '/api/content',
    method: 'post',
    data
  })
}

// 获取文章详情
export function getArticleDetail(id) {
  return request({
    url: `/api/content/${id}`,
    method: 'get'
  })
}

// 更新文章
export function updateArticle(id, data) {
  return request({
    url: `/api/content/${id}`,
    method: 'put',
    data
  })
}

// 删除文章
export function deleteArticle(id) {
  return request({
    url: `/api/content/${id}`,
    method: 'delete'
  })
}

// 批量删除文章
export function batchDeleteArticles(ids) {
  return request({
    url: '/api/content/batch',
    method: 'delete',
    data: ids
  })
}

// 获取分类列表
export function getCategoryList() {
  return request({
    url: '/api/category/all',
    method: 'get'
  })
}

// 上传文件
export function uploadFile(file, formData = {}) {
  const data = new FormData()
  data.append('file', file)
  Object.keys(formData).forEach(key => {
    data.append(key, formData[key])
  })
  
  return request({
    url: '/api/file/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

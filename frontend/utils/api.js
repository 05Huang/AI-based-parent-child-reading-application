import request from './request.js'

// 内容相关API
export const contentApi = {
  // 分页查询内容列表
  getContentPage(params) {
    console.log('调用分页查询内容接口，参数：', params)
    return request.get('/api/content/page', params)
  },
  
  // 获取内容详情
  getContentDetail(id) {
    console.log('获取内容详情，ID：', id)
    return request.get(`/api/content/${id}`)
  },
  
  // 增加浏览量
  incrementViewCount(id) {
    console.log('增加浏览量，ID：', id)
    return request.post(`/api/content/${id}/view`)
  }
}

// 推荐相关API
export const recommendationApi = {
  // 获取个性化推荐内容
  getRecommendations(userId, size = 10) {
    console.log('获取推荐内容，用户ID：', userId, '数量：', size)
    return request.post('/api/recommendation/get', {
      userId: userId,
      size: size
    })
  },
  
  // 获取热门内容
  getHotContents(size = 10) {
    console.log('获取热门内容，数量：', size)
    return request.get('/api/recommendation/hot', { size })
  },
  
  // 更新用户行为
  updateUserBehavior(userId) {
    console.log('更新用户行为，用户ID：', userId)
    return request.post(`/api/recommendation/updateBehavior?userId=${userId}`)
  }
}

// 分类相关API  
export const categoryApi = {
  // 获取分类详情
  getCategoryById(id) {
    console.log('获取分类详情，ID：', id)
    return request.get(`/api/category/${id}`)
  }
}

// 评论相关API
export const commentApi = {
  // 获取文章评论列表（普通评论）
  getContentComments(contentId, page = 1, size = 10) {
    console.log('获取文章评论列表，内容ID：', contentId, '页码：', page)
    return request.get(`/api/comment/contentby/${contentId}`, {
      current: page,
      size: size,
      commentType: 1 // 1-普通评论
    })
  },
  
  // 获取段落评论列表
  getParagraphComments(contentId, paragraphId) {
    console.log('获取段落评论列表，内容ID：', contentId, '段落ID：', paragraphId)
    return request.get(`/api/comment/contentby/${contentId}`, {
      current: 1,
      size: 50,
      commentType: 2, // 2-段落评论
      paragraphId: paragraphId
    })
  },
  
  // 获取段落评论计数
  getParagraphCommentCounts(contentId) {
    console.log('获取段落评论计数，内容ID：', contentId)
    return request.get('/api/comment/paragraph/counts', { contentId })
  },
  
  // 发表评论
  addComment(commentData) {
    console.log('发表评论：', commentData)
    return request.post('/api/comment', commentData)
  },
  
  // 获取评论回复
  getCommentReplies(commentId, page = 1, size = 10) {
    console.log('获取评论回复，评论ID：', commentId)
    return request.get(`/api/comment/${commentId}/replies`, {
      page: page,
      size: size
    })
  }
} 
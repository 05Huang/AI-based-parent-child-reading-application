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
  
  // 增加浏览量（同时会自动添加浏览记录）
  incrementViewCount(id) {
    console.log('增加浏览量并添加浏览记录，ID：', id)
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
  // 获取所有活跃分类
  getAllActiveCategories() {
    console.log('调用获取所有活跃分类接口')
    return request.get('/api/category/all')
  },
  
  // 获取分类详情
  getCategoryById(id) {
    console.log('获取分类详情，ID：', id)
    return request.get(`/api/category/${id}`)
  },
  
  // 分页查询分类
  getCategoryPage(params) {
    console.log('调用分类分页查询接口，参数：', params)
    return request.get('/api/category/page', params)
  }
}

// 视频相关API
export const videoApi = {
  // 获取视频列表（分页）
  getVideoPage(params) {
    console.log('调用视频分页查询接口，参数：', params)
    // 设置type=2表示只查询视频内容
    const videoParams = { ...params, type: 2 }
    return request.get('/api/content/page', videoParams)
  },
  
  // 获取热门视频
  getHotVideos(limit = 10) {
    console.log('获取热门视频，数量：', limit)
    return request.get('/api/content/hot', { limit })
  },
  
  // 获取推荐视频
  getRecommendedVideos(size = 10) {
    console.log('获取推荐视频，数量：', size)
    // 使用热门内容接口，因为推荐接口需要用户ID
    return request.get('/api/content/hot', { limit: size })
  },
  
  // 根据分类获取视频
  getVideosByCategory(categoryId, limit = 10) {
    console.log('根据分类获取视频，分类ID：', categoryId, '数量：', limit)
    return request.get(`/api/content/category/${categoryId}`, { limit })
  },
  
  // 获取视频详情
  getVideoDetail(id) {
    console.log('获取视频详情，ID：', id)
    return request.get(`/api/content/${id}`)
  },
  
  // 增加视频浏览量
  incrementVideoViewCount(id) {
    console.log('增加视频浏览量，ID：', id)
    return request.post(`/api/content/${id}/view`)
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

// 收藏相关API
export const favoriteApi = {
  // 获取用户收藏列表
  getUserFavorites(params) {
    console.log('调用获取用户收藏列表接口，参数：', params)
    return request.get('/api/user/favorite', params)
  },

  // 添加收藏
  addFavorite(userId, contentId, note = '') {
    console.log('调用添加收藏接口，用户ID：', userId, '内容ID：', contentId, '备注：', note)
    // 后端使用 @RequestParam，所以用查询参数方式传递
    return request.post('/api/user/favorite', null, {
      userId,
      contentId,
      note
    })
  },

  // 删除收藏
  deleteFavorite(userId, contentId) {
    console.log('调用删除收藏接口，用户ID：', userId, '内容ID：', contentId)
    return request.delete('/api/user/favorite', {
      userId,
      contentId
    })
  },

  // 批量删除收藏
  batchDeleteFavorites(userId, ids) {
    console.log('调用批量删除收藏接口，用户ID：', userId, 'IDs：', ids)
    return request.delete('/api/user/favorite/batch', {
      userId,
      ids
    })
  },

  // 获取收藏状态
  getFavoriteStatus(userId, contentId) {
    console.log('调用获取收藏状态接口，用户ID：', userId, '内容ID：', contentId)
    return request.get('/api/user/favorite/status', {
      userId,
      contentId
    })
  },

  // 切换收藏状态（收藏/取消收藏）
  toggleFavorite(userId, contentId) {
    console.log('调用切换收藏状态接口，用户ID：', userId, '内容ID：', contentId)
    return request.post('/api/user/favorite/toggle', null, {
      userId,
      contentId
    })
  }
}

// 搜索相关API
export const searchApi = {
  // 搜索内容
  searchContent(keyword, current = 1, size = 10) {
    console.log('调用搜索内容接口，关键词：', keyword, '页码：', current, '每页大小：', size)
    return request.get('/api/content/search', {
      keyword,
      current,
      size
    })
  },

  // 获取热门内容
  getHotContent(limit = 10) {
    console.log('调用获取热门内容接口，限制数量：', limit)
    return request.get('/api/content/hot', {
      limit
    })
  },

  // 获取热门搜索关键词（如果后端有的话，暂时用热门内容的标题作为关键词）
  getHotSearchKeywords(limit = 5) {
    console.log('调用获取热门搜索关键词接口，限制数量：', limit)
    return request.get('/api/content/hot', {
      limit
    })
  }
}

// 用户行为相关API
export const userBehaviorApi = {
  // 记录用户行为
  recordUserBehavior(behaviorData) {
    console.log('调用记录用户行为接口，数据：', behaviorData)
    return request.post('/api/user/behavior/record', behaviorData)
  },
  
  // 获取浏览统计
  getBrowsingStats(userId) {
    console.log('调用获取浏览统计接口，用户ID：', userId)
    return request.get(`/api/user/behavior/browsing/${userId}`)
  },
  
  // 获取收藏统计
  getCollectionStats(userId) {
    console.log('调用获取收藏统计接口，用户ID：', userId)
    return request.get(`/api/user/behavior/collection/${userId}`)
  },
  
  // 获取历史统计
  getHistoryStats(userId) {
    console.log('调用获取历史统计接口，用户ID：', userId)
    return request.get(`/api/user/behavior/history/${userId}`)
  },
  
  // 获取周报
  getWeeklyReport(userId) {
    console.log('调用获取周报接口，用户ID：', userId)
    return request.get(`/api/user/behavior/weekly-report/${userId}`)
  }
}

// 用户信息相关API
export const userApi = {
  // 获取当前登录用户信息
  getCurrentUser() {
    console.log('调用获取当前用户信息接口')
    return request.get('/api/user/current')
  },
  
  // 根据ID获取用户详情
  getUserById(id) {
    console.log('调用获取用户详情接口，用户ID：', id)
    return request.get(`/api/user/${id}`)
  }
}

// 浏览历史相关API
export const viewHistoryApi = {
  // 添加浏览记录
  addViewHistory(userId, contentId) {
    console.log('调用添加浏览记录接口，用户ID：', userId, '内容ID：', contentId)
    // 使用与addFavorite相同的方式传递@RequestParam参数
    return request.post('/api/user/view-history/add', null, {
      userId,
      contentId
    })
  },
  
  // 获取用户浏览历史
  getUserViewHistory(userId, params = {}) {
    console.log('调用获取用户浏览历史接口，用户ID：', userId, '参数：', params)
    console.log('请求URL：', `/api/user/view-history/${userId}`)
    return request.get(`/api/user/view-history/${userId}`, params)
  },
  
  // 批量删除浏览历史
  batchDeleteViewHistory(userId, ids) {
    console.log('调用批量删除浏览历史接口，用户ID：', userId, 'IDs：', ids)
    return request.delete(`/api/user/view-history/${userId}/batch`, ids)
  },
  
  // 清空用户所有浏览历史
  clearAllViewHistory(userId) {
    console.log('调用清空用户浏览历史接口，用户ID：', userId)
    return request.delete(`/api/user/view-history/${userId}/all`)
  }
}

// 亲密度相关API
export const intimacyApi = {
  // 获取全网亲密度排行
  getGlobalRanking() {
    console.log('调用获取全网亲密度排行接口')
    return request.get('/api/intimacy/global-ranking')
  },
  
  // 获取用户家庭亲密度排行和个人信息
  getUserRanking(userId) {
    console.log('调用获取用户亲密度排行接口，用户ID：', userId)
    return request.get(`/api/intimacy/user-ranking/${userId}`)
  },
  
  // 手动触发亲密度计算（用于测试）
  calculateIntimacy() {
    console.log('调用手动触发亲密度计算接口')
    return request.post('/api/intimacy/calculate')
  }
}

// 点赞相关API
export const likeApi = {
  // 点赞/取消点赞
  toggleLike(userId, targetId, type) {
    console.log('调用点赞/取消点赞接口，用户ID：', userId, '目标ID：', targetId, '类型：', type)
    return request.post('/api/like', {
      userId,
      targetId,
      type // 1: 内容，2: 评论
    })
  },
  
  // 获取点赞状态
  getLikeStatus(userId, targetId, type) {
    console.log('调用获取点赞状态接口，用户ID：', userId, '目标ID：', targetId, '类型：', type)
    return request.get('/api/like/status', {
      userId,
      targetId,
      type
    })
  }
} 
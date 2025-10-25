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
  
  // 获取文章推荐（精选推荐）- 使用Python推荐系统
  getArticleRecommendations(userId, size = 6) {
    console.log('===== [前端API] 开始调用文章推荐接口 =====')
    console.log('用户ID：', userId, '请求数量：', size)
    console.log('API端点：/api/recommendation/articles')
    return request.post('/api/recommendation/articles', {
      userId: userId,
      size: size
    }).then(response => {
      console.log('===== [前端API] 文章推荐接口响应 =====')
      console.log('响应状态：', response ? '成功' : '失败')
      console.log('响应数据：', response)
      if (response && response.data) {
        console.log('推荐文章数量：', response.data.length)
        response.data.forEach((article, index) => {
          console.log(`推荐文章 ${index + 1}:`, {
            id: article.id,
            title: article.title,
            viewCount: article.viewCount
          })
        })
      }
      return response
    }).catch(error => {
      console.error('===== [前端API] 文章推荐接口失败 =====')
      console.error('错误信息：', error)
      throw error
    })
  },
  
  // 获取热门内容
  getHotContents(size = 10) {
    console.log('获取热门内容，数量：', size)
    return request.get('/api/recommendation/hot', { size })
  },
  
  // 获取热门文章 - 使用Python推荐系统
  getHotArticles(userId, size = 4) {
    console.log('===== [前端API] 开始调用热门文章接口 =====')
    console.log('用户ID：', userId, '请求数量：', size)
    console.log('API端点：/api/recommendation/hot/articles')
    
    const params = { size }
    if (userId) {
      params.userId = userId
    }
    
    return request.get('/api/recommendation/hot/articles', params).then(response => {
      console.log('===== [前端API] 热门文章接口响应 =====')
      console.log('响应状态：', response ? '成功' : '失败')
      console.log('响应数据：', response)
      if (response && response.data) {
        console.log('热门文章数量：', response.data.length)
        response.data.forEach((article, index) => {
          console.log(`热门文章 ${index + 1}:`, {
            id: article.id,
            title: article.title,
            viewCount: article.viewCount,
            likeCount: article.likeCount
          })
        })
      }
      return response
    }).catch(error => {
      console.error('===== [前端API] 热门文章接口失败 =====')
      console.error('错误信息：', error)
      throw error
    })
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
    // 将参数作为查询参数添加到URL中
    const url = `/api/user/favorite?userId=${userId}&contentId=${contentId}`
    return request.delete(url)
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
  
  // 记录阅读行为（带时长）
  recordReadingBehavior(userId, contentId, duration, progress) {
    console.log('调用记录阅读行为接口，用户ID：', userId, '内容ID：', contentId, '时长（秒）：', duration, '进度：', progress)
    return request.post('/api/user/behavior/record', {
      userId,
      contentId,
      behaviorType: 'view',
      duration,
      progress
    })
  },
  
  // 记录分享行为
  recordShareBehavior(userId, contentId) {
    console.log('调用记录分享行为接口，用户ID：', userId, '内容ID：', contentId)
    return request.post('/api/user/behavior/record', {
      userId,
      contentId,
      behaviorType: 'share',
      source: 'reading'
    })
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
  getWeeklyReport(userId, year = null, month = null) {
    console.log('调用获取周报接口，用户ID：', userId, '年份：', year, '月份：', month)
    let url = `/api/user/behavior/weekly-report/${userId}`
    if (year && month) {
      url += `?year=${year}&month=${month}`
    }
    return request.get(url)
  },
  
  // 刷新用户统计缓存
  refreshStatsCache(userId) {
    console.log('调用刷新统计缓存接口，用户ID：', userId)
    return request.post(`/api/user/behavior/refresh-cache/${userId}`)
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
  },
  
  // 更新用户信息
  updateUser(userInfo) {
    console.log('调用更新用户信息接口，用户信息：', userInfo)
    return request.put('/api/user', userInfo)
  },
  
  // 删除用户（注销账号）
  deleteUser(id) {
    console.log('调用删除用户接口，用户ID：', id)
    return request.delete(`/api/user/${id}`)
  },
  
  // 重置密码
  resetPassword(resetData) {
    console.log('调用重置密码接口，数据：', resetData)
    return request.post('/api/user/reset-password', resetData)
  },
  
  // 发送邮箱验证码（带人机验证）
  sendEmailCodeWithCaptcha(email, ticket, randstr) {
    console.log('调用发送邮箱验证码接口（带人机验证），邮箱：', email, 'ticket：', ticket)
    return request.post('/api/user/send-email-code', { 
      email,
      ticket,
      randstr
    })
  },
  
  // 发送邮箱验证码（不带人机验证，用于兼容旧代码）
  sendEmailCode(email) {
    console.log('调用发送邮箱验证码接口，邮箱：', email)
    return request.post('/api/user/send-email-code', { email })
  },
  
  // 发送手机验证码
  sendSmsCode(phone) {
    console.log('调用发送手机验证码接口，手机号：', phone)
    return request.get('/api/user/smscode', { phone })
  },
  
  // 验证密码（用于修改密码时验证当前密码）
  verifyPassword(loginData) {
    console.log('调用验证密码接口，数据：', loginData)
    return request.post('/api/user/login', loginData)
  }
}

// 文件上传相关API
export const fileApi = {
  // 上传图片文件
  uploadImage(file, path = 'register/avatar') {
    console.log('调用上传图片接口，路径：', path)
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'image')
    formData.append('path', path)
    
    return request.post('/api/file/upload', formData, {
      'Content-Type': 'multipart/form-data'
    })
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

// 聊天相关API
export const chatApi = {
  // 获取聊天列表（包括群聊、私聊、AI助手）
  getChatList() {
    console.log('调用获取聊天列表接口')
    return request.get('/api/chat/list')
  },
  
  // 获取群聊列表
  getGroupChats() {
    console.log('调用获取群聊列表接口')
    return request.get('/api/groupMessage/groups')
  },
  
  // 获取私聊联系人列表
  getPrivateContacts() {
    console.log('调用获取私聊联系人列表接口')
    return request.get('/api/privateMessage/contacts')
  },
  
  // 获取群消息列表
  getGroupMessages(groupId, page = 1, size = 20) {
    console.log('调用获取群消息列表接口，群组ID：', groupId, '页码：', page, '每页大小：', size)
    return request.get(`/api/groupMessage/${groupId}`, {
      page,
      size
    })
  },
  
  // 获取私聊消息列表
  getPrivateMessages(contactId, page = 1, size = 20) {
    console.log('调用获取私聊消息列表接口，联系人ID：', contactId, '页码：', page, '每页大小：', size)
    return request.get(`/api/privateMessage/${contactId}`, {
      page,
      size
    })
  },
  
  // 发送群消息
  sendGroupMessage(groupId, content, type = 0) {
    console.log('调用发送群消息接口，群组ID：', groupId, '内容：', content, '类型：', type)
    return request.post('/api/groupMessage/send', {
      groupId,
      content,
      type
    })
  },
  
  // 发送私聊消息
  sendPrivateMessage(contactId, content, type = 0) {
    console.log('调用发送私聊消息接口，联系人ID：', contactId, '内容：', content, '类型：', type)
    return request.post('/api/privateMessage/send', {
      contactId,
      content,
      type
    })
  }
} 
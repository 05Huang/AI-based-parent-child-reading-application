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
  getArticleRecommendations(userId, size = 10, shuffle = false) {
    console.log('获取文章推荐内容，用户ID：', userId, '数量：', size, 'shuffle：', shuffle)
    return request.post('/api/recommendation/get', {
      userId,
      size,
      type: 1,
      shuffle
    })
  },

  getVideoRecommendations(userId, size = 10, shuffle = false) {
    console.log('获取视频推荐内容，用户ID：', userId, '数量：', size, 'shuffle：', shuffle)
    return request.post('/api/recommendation/get', {
      userId,
      size,
      type: 2,
      shuffle
    })
  },

  getHotArticles(userId, size = 10, shuffle = false) {
    console.log('获取热门文章，用户ID：', userId, '数量：', size, 'shuffle：', shuffle)
    return request.get('/api/recommendation/hot', { userId, size, type: 1, shuffle })
  },

  getHotVideos(userId, size = 10, shuffle = false) {
    console.log('获取热门视频（推荐系统），用户ID：', userId, '数量：', size, 'shuffle：', shuffle)
    return request.get('/api/recommendation/hot', { userId, size, type: 2, shuffle })
  },

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
    // 添加 type=2 参数确保只返回视频类型内容
    return request.get('/api/content/hot', { limit, type: 2 })
  },
  
  // 获取推荐视频
  getRecommendedVideos(size = 10) {
    console.log('获取推荐视频，数量：', size)
    // 使用热门内容接口，添加 type=2 参数确保只返回视频
    return request.get('/api/content/hot', { limit: size, type: 2 })
  },
  
  // 根据分类获取视频
  getVideosByCategory(categoryId, limit = 10) {
    console.log('根据分类获取视频，分类ID：', categoryId, '数量：', limit)
    return request.get(`/api/content/category/${categoryId}`, { limit, type: 2 })
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

// 群组相关API
export const groupApi = {
  // 获取群聊列表
  getGroupList(params) {
    console.log('调用获取群聊列表接口，参数：', params)
    return request.get('/api/groupMessage/groups', params)
  },

  // 获取群组详情
  getGroupDetail(groupId) {
    console.log('获取群组详情，群组ID：', groupId)
    return request.get(`/api/groupMessage/${groupId}`)
  },

  // 获取群组消息列表
  getGroupMessages(groupId, params) {
    console.log('获取群组消息列表，群组ID：', groupId, '参数：', params)
    return request.get(`/api/groupMessage/${groupId}`, params)
  },

  // 获取群组成员列表
  getGroupMembers(groupId, params) {
    console.log('获取群组成员列表，群组ID：', groupId, '参数：', params)
    return request.get(`/api/groupMessage/${groupId}/members`, params)
  },
  
  // 获取我在该群中的成员信息
  getMyMemberInfo(groupId) {
    console.log('获取我在该群的成员信息，群组ID：', groupId)
    return request.get(`/api/groupMessage/${groupId}/member/me`)
  },

  // 创建群组
  createGroup(data) {
    console.log('创建群组，数据：', data)
    return request.post('/api/groupMessage/groups', data)
  },

  // 加入群组
  joinGroup(groupId, userId) {
    console.log('加入群组，群组ID：', groupId, '用户ID：', userId)
    return request.post(`/api/groupMessage/${groupId}/join`, { userId })
  },
  
  // 邀请加入群组
  inviteMembers(groupId, inviteeIds) {
    console.log('邀请加入群组，群组ID：', groupId, '成员：', inviteeIds)
    return request.post(`/api/groupMessage/${groupId}/invite`, { inviteeIds })
  },
  
  // 获取群聊邀请
  getGroupInvites() {
    console.log('获取群聊邀请列表')
    return request.get('/api/groupMessage/invites')
  },
  
  // 处理群聊邀请
  handleGroupInvite(inviteId, status) {
    console.log('处理群聊邀请，邀请ID：', inviteId, '状态：', status)
    return request.post(`/api/groupMessage/invites/${inviteId}/handle`, { status })
  },
  
  // 发送群消息
  sendGroupMessage(data) {
    console.log('发送群消息，数据：', data)
    return request.post('/api/groupMessage/send', data)
  },
  
  // 置顶/取消置顶群聊（按用户维度）
  pinGroup(groupId, pinned) {
    console.log('置顶群聊，群组ID：', groupId, '置顶：', pinned)
    return request.post(`/api/groupMessage/${groupId}/pin`, { pinned })
  },
  
  // 清除本人群消息
  clearGroupMessages(groupId) {
    console.log('清除群消息，群组ID：', groupId)
    return request.post(`/api/groupMessage/${groupId}/clear`)
  },
  
  // 更新群名（群主）
  updateGroupName(groupId, name) {
    console.log('更新群名，群组ID：', groupId, '群名：', name)
    return request.put(`/api/groupMessage/${groupId}/name`, { name })
  },
  
  // 更新群头像（群主）
  updateGroupAvatar(groupId, avatarUrl) {
    console.log('更新群头像，群组ID：', groupId)
    return request.put(`/api/groupMessage/${groupId}/avatar`, { avatarUrl })
  },
  
  // 更新我在群中的昵称（成员）
  updateMyNickname(groupId, nickname) {
    console.log('更新群内昵称，群组ID：', groupId, '昵称：', nickname)
    return request.put(`/api/groupMessage/${groupId}/nickname`, { nickname })
  },
  
  // 更新我看到的群名备注（成员）
  updateMyGroupRemarkName(groupId, remark) {
    console.log('更新显示群名，群组ID：', groupId, '备注：', remark)
    return request.put(`/api/groupMessage/${groupId}/remark-group`, { remark })
  },
  
  // 退出群聊（成员）
  leaveGroup(groupId) {
    console.log('退出群聊，群组ID：', groupId)
    return request.post(`/api/groupMessage/${groupId}/quit`)
  },
  
  // 解散群聊（群主）
  dissolveGroup(groupId) {
    console.log('解散群聊，群组ID：', groupId)
    return request.post(`/api/groupMessage/${groupId}/dissolve`)
  },
  
  // 提交投诉
  submitComplaint(data) {
    console.log('提交投诉：', data)
    return request.post('/api/complaint', data)
  }
}

// AI助手相关API
export const aiApi = {
  chat(messages) {
    const API_KEY = 'sk-3d690e22a1184178aef733b5c0f2bece'
    const API_URL = 'https://api.deepseek.com/chat/completions'
    return fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${API_KEY}`
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages,
        stream: false
      })
    }).then(async (response) => {
      if (!response.ok) {
        const text = await response.text().catch(() => '')
        throw new Error(text || 'AI请求失败')
      }
      return response.json()
    })
  },
  groupChat(data) {
    console.log('调用群聊AI助手接口', data)
    return request.post('/api/ai/group-chat', data)
  },
  savePersonalRecord(data) {
    console.log('保存个人AI助手对话记录', data)
    return request.post('/api/ai/personal/record', data)
  },
  getPersonalSessions() {
    console.log('获取个人AI助手历史会话列表')
    return request.get('/api/ai/personal/sessions')
  },
  getPersonalSessionDetail(sessionId) {
    console.log('获取个人AI助手会话详情', sessionId)
    return request.get(`/api/ai/personal/session/${sessionId}`)
  },
  clearPersonalHistory() {
    console.log('清空个人AI助手历史记录')
    return request.delete('/api/ai/personal/clear')
  }
}

// 好友相关API
export const friendApi = {
  // 获取好友列表
  getFriendList(userId) {
    console.log('获取好友列表，用户ID：', userId)
    return request.get('/api/friend/list', { userId })
  },

  // 搜索用户（用于添加好友）
  searchUser(keyword, type = null) {
    console.log('搜索用户，关键字：', keyword, '类型：', type)
    const q = String(keyword).trim()
    if (type === 'phone') {
      return request.get(`/api/user/phone/${q}`)
    }
    if (type === 'id') {
      return request.get(`/api/user/username/${q}`)
    }
    const isPhone = /^1\d{10}$/.test(q)
    if (isPhone) {
      return request.get(`/api/user/phone/${q}`)
    } else {
      return request.get(`/api/user/username/${q}`)
    }
  },

  // 发送好友申请
  addFriend(data) {
    console.log('发送好友申请，数据：', data)
    return request.post('/api/friend/apply', data)
  },

  // 获取好友申请列表
  getFriendRequests() {
    console.log('获取好友申请列表')
    return request.get('/api/friend/requests')
  },

  // 处理好友申请
  handleFriendRequest(requestId, status) {
    console.log('处理好友申请，申请ID：', requestId, '状态：', status)
    return request.post('/api/friend/handle', { requestId, status })
  },
  
  // 获取我与某好友的关系信息
  getMyFriendInfo(friendId) {
    console.log('获取好友关系信息，好友ID：', friendId)
    return request.get(`/api/friend/${friendId}/me`)
  },

  // 获取关注状态（是否已关注）
  getFollowStatus(friendId) {
    console.log('获取关注状态，好友ID：', friendId)
    return request.get(`/api/friend/${friendId}/status`)
  },
  
  // 置顶/取消置顶好友
  pinFriend(friendId, pinned) {
    console.log('置顶好友，好友ID：', friendId, '置顶：', pinned)
    return request.post(`/api/friend/${friendId}/pin`, { pinned })
  },
  
  // 更新好友备注
  updateFriendRemark(friendId, remark) {
    console.log('更新好友备注，好友ID：', friendId, '备注：', remark)
    return request.put(`/api/friend/${friendId}/remark`, { remark })
  },
  
  // 清除本人可见的与该好友的历史消息
  clearPrivateMessages(friendId) {
    console.log('清除私聊消息，好友ID：', friendId)
    return request.post(`/api/friend/${friendId}/clear`)
  },
  
  // 删除好友（双向解除关系）
  deleteFriend(friendId) {
    console.log('删除好友，好友ID：', friendId)
    return request.delete(`/api/friend/${friendId}`)
  },

  // 取消关注（单向解除关系）
  unfollow(friendId) {
    console.log('取消关注，好友ID：', friendId)
    return request.delete(`/api/friend/${friendId}/unfollow`)
  }
}

// 私聊相关API
export const privateChatApi = {
  // 获取私聊消息列表
  getPrivateMessages(friendId, params) {
    console.log('获取私聊消息列表，好友ID：', friendId, '参数：', params)
    return request.get(`/api/privateMessage/${friendId}`, params)
  },

  // 发送私聊消息
  sendPrivateMessage(data) {
    console.log('发送私聊消息，数据：', data)
    return request.post('/api/privateMessage/send', data)
  },
  
  // 标记消息为已读
  markAsRead(friendId) {
     console.log('标记消息为已读，好友ID：', friendId)
     return request.put(`/api/privateMessage/${friendId}/read`)
  }
}

// 搜索相关API
export const searchApi = {
  // 搜索内容
  searchContent(keyword, current = 1, size = 10, type = null) {
    console.log('调用搜索内容接口，关键词：', keyword, '页码：', current, '每页大小：', size, '类型：', type)
    const params = {
      keyword,
      current,
      size
    }
    if (type) {
      params.type = type
    }
    return request.get('/api/content/search', params)
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
  },
  
  // 获取连续阅读天数
  getConsecutiveReadingDays(userId) {
    console.log('调用获取连续阅读天数接口，用户ID：', userId)
    return request.get(`/api/user/behavior/consecutive-days/${userId}`)
  },
  
  // 获取周阅读排行榜
  getWeeklyReadingRanking(params = {}) {
    console.log('调用获取周阅读排行榜接口，参数：', params)
    return request.get('/api/user/behavior/weekly-ranking', params)
  },

  // 获取每周目标阅读时间（分钟）
  getWeeklyReadingTargetMinutes(userId) {
    console.log('获取每周目标阅读时间，用户ID：', userId)
    return request.get(`/api/user/behavior/reading-target/${userId}`)
  },

  // 设置每周目标阅读时间（分钟）
  setWeeklyReadingTargetMinutes(userId, targetMinutes) {
    console.log('设置每周目标阅读时间，用户ID：', userId, '目标分钟：', targetMinutes)
    return request.post('/api/user/behavior/reading-target', null, {
      userId,
      targetMinutes
    })
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
  
  // 发送邮箱验证码
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
    
    return new Promise((resolve, reject) => {
      // 构建请求URL
      const url = `${request.baseUrl}/api/file/upload`
      
      // 准备上传参数
      let uploadOptions = {
        url: url,
        name: 'file', // 后端接收的文件参数名
        formData: {
          'type': 'image',
          'path': path
        },
        header: {
          // 如果有token，添加到请求头
          ...(uni.getStorageSync('token') ? {
            'accessToken': uni.getStorageSync('token')
          } : {})
        },
        success: (uploadFileRes) => {
          console.log('上传接口返回原始数据：', uploadFileRes.data)
          try {
            // uni.uploadFile返回的data是字符串，需要解析
            const data = typeof uploadFileRes.data === 'string' ? JSON.parse(uploadFileRes.data) : uploadFileRes.data
            
            if (uploadFileRes.statusCode === 200 && data.code === 200) {
              resolve(data)
            } else {
              uni.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              })
              reject(data)
            }
          } catch (e) {
            console.error('解析上传响应失败：', e)
            reject(e)
          }
        },
        fail: (err) => {
          console.error('上传网络请求失败：', err)
          uni.showToast({
            title: '网络请求失败',
            icon: 'none'
          })
          reject(err)
        }
      }

      // 处理文件对象
      // H5端 file 是 File 对象
      // App/小程序端 file 可能是路径字符串或包含 path 的对象
      if (file.path) {
        uploadOptions.filePath = file.path
      } else if (typeof file === 'string') {
        uploadOptions.filePath = file
      } else {
        // H5 File对象
        uploadOptions.file = file
      }

      // 发起上传
      uni.uploadFile(uploadOptions)
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
  },
  
  // 获取家庭关系亲密度分析（儿童端使用）
  getIntimacyAnalysis() {
    console.log('调用获取家庭关系亲密度分析接口')
    return request.get('/api/family/intimacy-analysis')
  },

  // 获取亲密度趋势
  getIntimacyTrend(userId, days = 7) {
    console.log('调用获取亲密度趋势接口，用户ID：', userId, '天数：', days)
    return request.get('/api/intimacy/trend', { userId, days })
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

// 家庭相关API
export const familyApi = {
  // 获取当前用户的家庭成员列表
  getFamilyMembers() {
    console.log('调用获取家庭成员列表接口')
    return request.get('/api/family/members')
  },
  
  // 绑定家庭成员
  bindFamilyMember(username, relationType) {
    console.log('调用绑定家庭成员接口，用户名：', username, '关系类型：', relationType)
    return request.post('/api/family/bind-child', null, {
      childUsername: username,
      relationType: relationType
    })
  },
  
  // 解绑家庭成员
  unbindFamilyMember(memberId) {
    console.log('调用解绑家庭成员接口，成员ID：', memberId)
    return request.delete(`/api/family/unbind/${memberId}`)
  }
}

// 问卷星相关API
export const surveyApi = {
  // 生成问卷链接
  generateSurveyLink(surveyId, userId, source = 'child-app') {
    console.log('调用生成问卷链接接口，问卷ID：', surveyId, '用户ID：', userId, '来源：', source)
    return request.get('/api/surveystar/generate-link', {
      surveyId: surveyId,
      userId: userId,
      source: source
    })
  },
  
  // 获取所有可用的问卷列表
  getSurveyList(params = {}) {
    console.log('调用获取问卷列表接口，参数：', params)
    return request.get('/api/surveystar/list', params)
  },
  
  // 获取问卷详情
  getSurveyDetail(surveyId) {
    console.log('调用获取问卷详情接口，问卷ID：', surveyId)
    return request.get(`/api/surveystar/${surveyId}`)
  },
  
  // 获取问卷答卷列表
  getSurveyAnswers(questId, params = {}) {
    console.log('调用获取问卷答卷列表接口，问卷ID：', questId, '参数：', params)
    return request.get(`/api/surveystar/${questId}/answers`, params)
  },
  
  // 获取用户问卷参与统计
  getUserSurveyStatistics(userId) {
    console.log('调用获取用户问卷统计接口，用户ID：', userId)
    return request.get(`/api/surveystar/user/${userId}/statistics`)
  },
  
  // 创建问卷记录（后台管理使用）
  createSurvey(surveyData) {
    console.log('调用创建问卷记录接口，数据：', surveyData)
    return request.post('/api/surveystar/create', surveyData)
  },
  
  // 更新问卷信息（后台管理使用）
  updateSurvey(id, surveyData) {
    console.log('调用更新问卷信息接口，ID：', id, '数据：', surveyData)
    return request.put(`/api/surveystar/${id}`, surveyData)
  }
} 

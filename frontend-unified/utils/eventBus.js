/**
 * 全局事件总线 - 用于页面间数据同步
 * 
 * 使用场景：
 * 1. 修改数据后通知其他页面刷新
 * 2. 避免每次返回都重新请求接口
 * 3. 精准更新需要刷新的页面
 */

// 事件名称常量
export const EVENTS = {
  // 用户相关
  USER_INFO_UPDATED: 'userInfoUpdated',           // 用户信息更新
  USER_AVATAR_UPDATED: 'userAvatarUpdated',       // 头像更新
  USER_STATS_UPDATED: 'userStatsUpdated',         // 用户统计数据更新
  
  // 内容相关
  CONTENT_LIKED: 'contentLiked',                  // 内容点赞
  CONTENT_COLLECTED: 'contentCollected',          // 内容收藏
  CONTENT_COMMENTED: 'contentCommented',          // 内容评论
  CONTENT_DELETED: 'contentDeleted',              // 内容删除
  
  // 关系相关
  CHILD_BOUND: 'childBound',                      // 绑定孩子
  CHILD_UNBOUND: 'childUnbound',                  // 解绑孩子
  FOLLOW_CHANGED: 'followChanged',                // 关注状态变化
  
  // 阅读相关
  READING_RECORD_UPDATED: 'readingRecordUpdated', // 阅读记录更新
  COLLECTION_UPDATED: 'collectionUpdated',        // 收藏更新
  HISTORY_UPDATED: 'historyUpdated',              // 历史记录更新
  
  // 亲密度相关
  INTIMACY_UPDATED: 'intimacyUpdated'             // 亲密度更新
}

class EventBus {
  constructor() {
    // 使用 Map 存储事件监听器
    this.listeners = new Map()
    console.log('EventBus 初始化完成')
  }
  
  /**
   * 监听事件
   * @param {string} eventName - 事件名称
   * @param {function} callback - 回调函数
   * @param {object} context - 上下文（用于日志）
   */
  on(eventName, callback, context = {}) {
    if (!this.listeners.has(eventName)) {
      this.listeners.set(eventName, [])
    }
    
    this.listeners.get(eventName).push({
      callback,
      context: context.pageName || '未知页面'
    })
    
    console.log(`[EventBus] ${context.pageName || '未知页面'} 监听事件: ${eventName}`)
  }
  
  /**
   * 取消监听事件
   * @param {string} eventName - 事件名称
   * @param {function} callback - 回调函数（可选，不传则移除所有）
   */
  off(eventName, callback) {
    if (!this.listeners.has(eventName)) return
    
    if (callback) {
      const listeners = this.listeners.get(eventName)
      const index = listeners.findIndex(item => item.callback === callback)
      if (index > -1) {
        listeners.splice(index, 1)
        console.log(`[EventBus] 取消监听事件: ${eventName}`)
      }
    } else {
      this.listeners.delete(eventName)
      console.log(`[EventBus] 移除所有监听: ${eventName}`)
    }
  }
  
  /**
   * 触发事件
   * @param {string} eventName - 事件名称
   * @param {any} data - 传递的数据
   */
  emit(eventName, data) {
    console.log(`[EventBus] 触发事件: ${eventName}`, data)
    
    if (!this.listeners.has(eventName)) {
      console.log(`[EventBus] 无监听器: ${eventName}`)
      return
    }
    
    const listeners = this.listeners.get(eventName)
    console.log(`[EventBus] 通知 ${listeners.length} 个监听器`)
    
    listeners.forEach(({ callback, context }) => {
      try {
        console.log(`[EventBus] 执行回调 - 页面: ${context}`)
        callback(data)
      } catch (error) {
        console.error(`[EventBus] 回调执行失败 - 页面: ${context}`, error)
      }
    })
  }
  
  /**
   * 只监听一次
   * @param {string} eventName - 事件名称
   * @param {function} callback - 回调函数
   */
  once(eventName, callback) {
    const onceCallback = (data) => {
      callback(data)
      this.off(eventName, onceCallback)
    }
    this.on(eventName, onceCallback)
  }
  
  /**
   * 清空所有监听器
   */
  clear() {
    this.listeners.clear()
    console.log('[EventBus] 清空所有监听器')
  }
  
  /**
   * 获取当前监听器数量（用于调试）
   */
  getListenerCount(eventName) {
    if (!eventName) {
      return this.listeners.size
    }
    return this.listeners.has(eventName) ? this.listeners.get(eventName).length : 0
  }
}

// 导出单例
export default new EventBus()

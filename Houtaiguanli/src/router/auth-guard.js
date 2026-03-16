import { useAuthStore } from '@/stores/auth'

/**
 * 设置路由守卫
 * 保护需要认证的路由
 */
export function setupAuthGuard(router) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()
    
    // 不需要认证的公开页面列表
    const publicPages = ['/login', '/register']
    const authRequired = !publicPages.includes(to.path)
    
    // 获取当前token
    const isLoggedIn = authStore.isLoggedIn

    if (authRequired && !isLoggedIn) {
      // 需要认证但未登录，重定向到登录页
      next('/login')
    } else if (to.path === '/register' && isLoggedIn) {
      // 已登录用户访问注册页，允许访问（管理员可以创建更多管理员）
      next()
    } else {
      // 检查权限
      if (to.meta?.requiresAdmin) {
        // 需要管理员权限
        // 如果没有userInfo，先尝试获取用户信息
        if (!authStore.userInfo && authStore.isLoggedIn) {
          try {
            await authStore.fetchUserInfo()
          } catch (error) {
            console.warn('获取用户信息失败:', error)
          }
        }
        
        // 重新检查权限
        if (!authStore.isAdmin) {
          console.warn('用户没有管理员权限，当前userInfo:', authStore.userInfo)
          next('/403')
          return
        }
      }
      
      next()
    }
  })
}

import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/register/index.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/dashboard-layout',
    name: 'Layout',
    component: () => import('../layout/index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '控制台', icon: 'Odometer', requiresAuth: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'articles',
        name: 'Articles',
        component: () => import('../views/articles/index.vue'),
        meta: { title: '文章管理', icon: 'Document', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'videos',
        name: 'Videos',
        component: () => import('../views/videos/index.vue'),
        meta: { title: '视频管理', icon: 'VideoCamera', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('../views/comments/index.vue'),
        meta: { title: '评论管理', icon: 'ChatDotRound', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'chat-groups',
        name: 'ChatGroups', 
        component: () => import('../views/chat-groups/index.vue'),
        meta: { title: '群聊管理', icon: 'ChatLineRound', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'family',
        name: 'Family',
        component: () => import('../views/family/index.vue'),
        meta: { title: '家庭管理', icon: 'House', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'questionnaire',
        name: 'Questionnaire',
        component: () => import('../views/questionnaire/index.vue'),
        meta: { title: '问卷管理', icon: 'Document', requiresAuth: true, requiresAdmin: true }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/403.vue'),
    meta: { title: '权限不足' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/404.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router 
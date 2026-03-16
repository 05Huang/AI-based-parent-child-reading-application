import './assets/main.css'
import './assets/styles/global.scss'  // 引入全局样式
import './assets/styles/dialog.scss' // 引入对话框样式

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import { setupAuthGuard } from './router/auth-guard'

const app = createApp(App)
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 安装Pinia状态管理
app.use(pinia)

// 设置路由守卫（需要在pinia之后）
setupAuthGuard(router)

// 安装路由
app.use(router)

// 安装Element Plus UI库
app.use(ElementPlus, {
  locale: zhCn,
})

// 挂载应用
app.mount('#app')

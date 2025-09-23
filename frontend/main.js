import { createSSRApp } from 'vue'
import App from './App'
import request from './utils/request'

export function createApp() {
  const app = createSSRApp(App)
  
  // 全局注册$http
  app.config.globalProperties.$http = request
  
  return {
    app
  }
} 
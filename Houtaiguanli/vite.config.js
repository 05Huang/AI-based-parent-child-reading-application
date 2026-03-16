import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    // 开发服务器主机和端口
    host: '0.0.0.0',
    port: 5173,
    
    // API代理配置，用于开发环境中解决CORS问题
    proxy: {
      // 代理所有以 /api 开头的请求到后端服务
      '/api': {
        target: process.env.VITE_API_URL || 'http://localhost:8888',
        changeOrigin: true,
        rewrite: (path) => path, // 保持路径不变
      }
    },
    
    // 热模块替换配置
    hmr: {
      host: 'localhost',
      port: 5173,
      protocol: 'ws'
    }
  },
  
  // 构建配置
  build: {
    // 输出目录
    outDir: 'dist',
    
    // 小文件内联大小限制（字节）
    assetsInlineLimit: 4096,
    
    // 启用源映射用于调试
    sourcemap: false,
    
    // 最小化配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // 移除console
        drop_debugger: true // 移除debugger
      }
    },
    
    // 分块大小警告阈值
    chunkSizeWarningLimit: 500
  }
})

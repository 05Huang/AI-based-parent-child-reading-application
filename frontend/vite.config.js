import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni(),
  ],
  server: {
    port: 5173,
    host: '0.0.0.0'
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, './src')
    }
  },
  // 添加静态资源配置
  publicDir: 'public',
  build: {
    assetsDir: 'static',
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html')
      }
    }
  }
}) 
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // 患者端 API（8081）
      '/patient': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // 医生端 API（8082）
      '/doctor': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      // 管理端 API（8083）
      '/admin': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      // WebSocket 连接（8084）
      '/ws': {
        target: 'ws://localhost:8084',
        ws: true,
        changeOrigin: true,
      },
      // 咨询 HTTP 接口（8084）
      '/consultation': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
    },
  },
})
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 配置：开发服务器 + 后端代理
// 后端默认 8082（避开本机 8080 教程服务、8081 演示工程）；改端口时只需改这里与后端 yml
const target = process.env.VITE_PROXY_TARGET || 'http://localhost:8082'

export default defineConfig({
  plugins: [vue()],

  server: {
    port: Number(process.env.FRONTEND_PORT || 5174),
    open: false,                 // 不自动弹浏览器，避免每次重启都跳窗口
    proxy: {
      // 所有 /api 开头的请求转发到后端，浏览器看到的仍是同源地址，天然绕开 CORS
      '/api': {
        target,
        changeOrigin: true
        // 后端路径本身就是 /api/users，所以这里不做 rewrite
      }
    }
  }
})

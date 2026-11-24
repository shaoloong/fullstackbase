import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'

// Vue3 入口：createApp 返回应用实例，挂载到 index.html 里的 #app
// 路由由 src/config/features.js 的注册表自动生成，新增功能无需改这里
createApp(App).use(router).mount('#app')

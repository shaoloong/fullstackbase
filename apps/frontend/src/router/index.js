import { createRouter, createWebHistory } from 'vue-router'
import { features } from '../config/features'

// 路由完全由注册表生成：新增功能不用改本文件
// ready=false 的功能也注册路由，由 App.vue 渲染占位卡片（先占位、后实现）
const routes = [
  { path: '/', redirect: features[0]?.path || '/users' },
  ...features.map((f) => ({
    path: f.path,
    name: f.key,
    component: f.component,
    meta: { key: f.key, title: f.title, icon: f.icon, ready: f.ready }
  })),
  // 兜底：未知路径回首页，避免白屏
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

export default createRouter({
  history: createWebHistory(),
  routes
})

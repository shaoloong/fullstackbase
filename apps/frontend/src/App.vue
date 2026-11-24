<script setup>
/**
 * 应用外壳：左侧菜单 + 顶部功能标题 + 主内容区。
 *
 * 这一层**不写任何业务逻辑**，只消费 config/features.js 注册表：
 * 新增功能时菜单、路由、标题会自动出现，本文件完全不用改。
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { features } from './config/features'
import http from './api/http'

const route = useRoute()

// 当前功能定义：路由 meta 由 router/index.js 从注册表注入
const current = computed(() => features.find((f) => f.key === route.meta.key) || features[0])

// 后端功能目录（GET /api/features）：用来校验前后端注册表是否一致
// 这是扩展纪律的自证手段 —— 两边 key 对不上时页面顶部直接给红字提示
const remoteKeys = ref(null)
const catalogError = ref('')
const mismatch = computed(() => {
  if (!remoteKeys.value) return ''
  const local = features.map((f) => f.key).sort().join(',')
  const remote = [...remoteKeys.value].sort().join(',')
  return local === remote ? '' : `前后端功能注册表不一致：前端 [${local}] / 后端 [${remote}]`
})

onMounted(async () => {
  try {
    // 拦截器已拆掉 R 外壳，data 直接是 Feature 数组
    const list = await http.get('/features')
    remoteKeys.value = list.map((f) => f.key)
  } catch (e) {
    catalogError.value = e.message || '功能目录接口不可用'
  }
})
</script>

<template>
  <div class="shell">
    <!-- ===== 侧边栏：菜单 100% 由注册表生成 ===== -->
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-name">fullstack-base</div>
        <div class="brand-sub">可扩展全栈底座 · v0.1</div>
      </div>

      <div class="nav-label">功能模块</div>
      <nav>
        <router-link
          v-for="f in features"
          :key="f.key"
          :to="f.path"
          class="nav-item"
          active-class="active"
        >
          <span class="nav-icon">{{ f.icon }}</span>
          <span>{{ f.title }}</span>
          <span v-if="!f.ready" class="nav-todo">待实现</span>
        </router-link>
      </nav>

      <div class="side-foot">
        <div>JDK 21 · Boot 3.5.16</div>
        <div>Vue3 · Vite · H2</div>
      </div>
    </aside>

    <!-- ===== 主内容区 ===== -->
    <main class="main">
      <header class="topbar">
        <div>
          <span class="topbar-icon">{{ current.icon }}</span>
          <b>{{ current.title }}</b>
          <span class="crumb">/ {{ current.path }}</span>
        </div>
        <a class="link" href="/swagger-ui.html" target="_blank">API 文档 ↗</a>
      </header>

      <!-- 注册表不一致 / 接口不可用：给显式提示，避免静默失联 -->
      <div v-if="mismatch" class="banner error">⚠️ {{ mismatch }}</div>
      <div v-else-if="catalogError" class="banner warn">⚠️ {{ catalogError }}（后端是否已启动？）</div>

      <div class="content">
        <!-- ready=false：占位卡片。功能可以先登记、后实现，菜单不会因此报错 -->
        <section v-if="!current.ready" class="card placeholder">
          <div class="ph-icon">🚧</div>
          <h2>{{ current.title }} · 已注册，待实现</h2>
          <p>功能已在前后端注册表占位，路由与菜单都已就绪。</p>
          <p>接下来：<code>node scripts/new-module.js --name {{ current.key }} --title {{ current.title }}</code></p>
        </section>
        <router-view v-else />
      </div>
    </main>
  </div>
</template>

<style scoped>
.shell { display: flex; min-height: 100vh; }
.sidebar { width: 232px; flex: 0 0 232px; background: #fff; border-right: 1px solid #e5e9f2;
           display: flex; flex-direction: column; padding: 22px 14px; }
.brand { padding: 0 10px 22px; border-bottom: 1px solid #f0f2f7; }
.brand-name { font-size: 16px; font-weight: 700; color: #23304a; letter-spacing: .2px; }
.brand-sub { font-size: 11px; color: #8b93a7; margin-top: 4px; }
.nav-label { font-size: 11px; color: #a0a7b8; padding: 22px 10px 8px; letter-spacing: 1px; }
.nav-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 9px;
            color: #4b5566; text-decoration: none; font-size: 13px; margin-bottom: 4px; }
.nav-item:hover { background: #f6f8fc; color: #4f6ef7; }
.nav-item.active { background: #eef2ff; color: #4f6ef7; font-weight: 600; }
.nav-icon { font-size: 15px; }
.nav-todo { margin-left: auto; font-size: 10px; color: #b47b16; background: #fff5df;
            padding: 1px 6px; border-radius: 999px; }
.side-foot { margin-top: auto; padding: 14px 10px 0; border-top: 1px solid #f0f2f7;
             font-size: 11px; color: #a0a7b8; line-height: 1.8; }
.main { flex: 1; min-width: 0; }
.topbar { height: 60px; background: #fff; border-bottom: 1px solid #e5e9f2;
          display: flex; align-items: center; justify-content: space-between; padding: 0 28px; }
.topbar-icon { margin-right: 8px; }
.crumb { margin-left: 8px; color: #a0a7b8; font-size: 12px; font-family: ui-monospace, Consolas, monospace; }
.link { color: #4f6ef7; text-decoration: none; font-size: 12px; }
.banner { margin: 14px 28px 0; padding: 10px 14px; border-radius: 8px; font-size: 12px; }
.banner.error { background: #fff1f1; color: #c03a3a; }
.banner.warn { background: #fff8e8; color: #9a6a10; }
.content { padding: 20px 28px 48px; }
.placeholder { text-align: center; padding: 60px 24px; }
.ph-icon { font-size: 40px; }
.placeholder h2 { font-size: 18px; margin: 12px 0; }
.placeholder p { color: #6b7280; font-size: 13px; margin: 6px 0; }
.placeholder code { background: #f6f8fc; padding: 2px 6px; border-radius: 4px; }
</style>

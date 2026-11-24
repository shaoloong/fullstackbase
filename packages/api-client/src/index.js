/**
 * 契约层统一出口（扩展入口 · 契约侧）
 * ============================================================
 * 当前阶段（零依赖可跑）：各功能域在 apps/frontend/src/features/<域>/api.js 手写接口，
 * 本文件只作为「未来自动生成物的唯一出口」占位，不反向依赖前端代码。
 *
 * 接入 orval 后（bash scripts/gen-api.sh）：生成 src/gen/<域>/<域>.ts，
 * 在此逐域 `export * from './gen/<域>/<域>'`，前端改成
 * `import { listOrders } from '@fullstack-base/api-client'`。
 *
 * 铁律：前端禁止自造后端已有的类型，也禁止绕过契约层手写请求；
 * 一切接口与类型都从本包出，字段改名才能在构建期报错而不是上线白屏。
 *
 * scripts/new-module.js 会在 @NEW_EXPORT 锚点前插入新域的导出说明，勿删锚点。
 */

// export * from './gen/user/user'
// @NEW_EXPORT

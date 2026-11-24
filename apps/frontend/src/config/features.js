/**
 * 前端功能注册表（扩展入口 · 前端侧，唯一真相）
 * ============================================================
 * 新增一个功能只需要：
 *   ① 在 features/ 下建一个同名目录，放 Page.vue（域出口、composable、api 可选）
 *   ② 在下面数组里加一行（scripts/new-module.js 自动完成）
 * 路由、菜单、面包屑全部由本表生成，不用碰 router / App.vue。
 *
 * ⚠️ key 必须与后端 core/FeatureRegistry.java 的 key 一致；
 *    `node scripts/check-features.js` 会校验，不一致即报错。
 *    scripts/new-module.js 会在 @NEW_FEATURE 锚点前自动插入新行，勿删锚点。
 *
 * 字段说明：
 *   key       全局唯一小写标识，同时是后端模块名 / 前端目录名 / 路由 name
 *   title     菜单与页头显示名
 *   path      路由路径
 *   icon      菜单图标（emoji，零依赖）
 *   ready     false 时展示「已注册，待实现」占位页，方便先把坑占上再填功能
 *   component 懒加载该域的 Page.vue；路径必须是字面量，Vite 才能静态分析打包
 */
const registry = [
  {
    key: 'user',
    title: '用户管理',
    path: '/users',
    icon: '👤',
    ready: true,
    component: () => import('../features/user/Page.vue')
  }
]

// 用 push 登记：生成器只需在锚点前插一行，不会出现尾随逗号的语法坑
// @NEW_FEATURE

export const features = registry

/** 按 key 查功能定义；页面里用来显示当前功能标题 */
export function findFeature(key) {
  return features.find((f) => f.key === key)
}

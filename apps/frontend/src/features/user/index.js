/**
 * user 功能域出口：域外（路由 / 其它域）只允许从这里导入。
 * 禁止深路径引用域内文件 —— 否则以后重构目录会牵一发动全身。
 */
export { default as UserPage } from './Page.vue'
export { useUserList } from './useUserList'
export { fetchUsers } from './api'

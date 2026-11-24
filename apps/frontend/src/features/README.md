# 前端功能域目录（扩展点）

每个子目录是一个功能域，**域外只允许从 `index.js` 出口导入**，禁止深路径引用域内文件。

```
features/
├── user/     # 已实现：Page.vue + useUserList.js + api.js + index.js
└── <新域>/   # 由 node scripts/new-module.js 生成
```

## 结构约定

| 文件 | 职责 |
|---|---|
| `Page.vue` | 页面薄壳，只做渲染与交互接线 |
| `use<Domain>List.js` | composable：状态 + 请求 + 加载/错误/空态 + 竞态保护 |
| `api.js` | 接口层，统一走 `src/api/http.js`（拦截器已拆 R 外壳） |
| `index.js` | 域出口 |

## 加功能要改哪里

**只用改 `src/config/features.js` 一行**（生成器自动插入）：

```js
registry.push({
  key: 'order', title: '订单', path: '/orders', icon: '🧩', ready: true,
  component: () => import('../features/order/Page.vue')
})
```

路由、菜单、面包屑由 `src/router/index.js` 与 `App.vue` 从注册表生成，
**这两个文件不用改**。

## 铁律

- **显式 import**：features/ 下所有组件/组合式函数必须 `import { ref } from 'vue'`，
  不依赖构建工具自动导入（换构建工具 / 单测时才不会崩）。
- **页面不管取数**：数据获取放 composable，组件 prop 驱动、纯展示。
- **不手写重复类型**：接契约后统一从 `@fullstack-base/api-client` 导入。
- **注册表两边一致**：`node scripts/check-features.js` 是唯一裁判。

详见 [`docs/extend-guide.md`](../../../../docs/extend-guide.md)。

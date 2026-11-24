# 前端功能域模板

> 由 `scripts/new-module.js` 自动实例化，勿手工复制。
> 生成目标：`apps/frontend/src/features/<域>/`

| 模板文件 | 生成后 |
|---|---|
| `Page.vue.tpl` | `Page.vue`（页面薄壳，只做渲染） |
| `use__Domain__List.js.tpl` | `use<Domain>List.js`（状态 + 请求 + 交互） |
| `api.js.tpl` | `api.js`（接口层，走 `src/api/http.js`） |
| `index.js.tpl` | `index.js`（域出口） |

## 结构铁律（对齐 tcmp 的 features 约定）

- **域出口**：域外只允许 `import from '../features/<域>'`，禁止深路径引用域内文件。
- **显式 import**：features/ 下所有组件与组合式函数必须显式 `import { ref } from 'vue'`，
  不依赖构建工具的自动导入（单测与换构建工具时才不会崩）。
- **页面是薄壳**：数据获取放 composable，Page.vue 只接线与渲染。
- **路由不用改**：`src/config/features.js` 注册表自动生成路由与菜单。
- **注册表一致性**：`node scripts/check-features.js` 校验前后端 key 对齐。

## 生成后必改

1. 表格列、筛选项对齐后端 `__Domain__VO` / `__Domain__Query`。
2. 需要更丰富的交互时，在 `use__Domain__List.js` 里加方法，别把逻辑塞进模板。
3. 接了契约生成后把 `api.js` 换成 `@fullstack-base/api-client` 的生成函数，
   这样字段改名会在 `pnpm build` 阶段直接报错，而不是上线后白屏。

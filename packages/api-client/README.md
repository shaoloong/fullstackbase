# 契约层 `packages/api-client`

对齐 tcmp 的契约驱动思路，但按本工程的依赖现状做了降级与升级路径。

## 链路

```
后端 Controller（@Tag / @Operation）
  → springdoc 暴露 /v3/api-docs
  → scripts/gen-api.sh 导出 openapi.json（随代码入库，PR 可 diff）
  →（可选）orval 生成 src/gen/<域>/<域>.ts 类型化客户端
  → 前端 import { listOrders } from '@fullstack-base/api-client'
```

## 当前状态

- `openapi.json`：可运行的**契约快照**（手写最小版，启动后端后由 `gen-api.sh` 覆盖）。
- `src/index.js`：零依赖的手写导出，前端现在就能用，不需要装 orval。
- `src/gen/`：orval 生成物目录，运行生成后出现。

## 升级到自动生成

```bash
# 1) 安装前端依赖（工程级 .npmrc 已指向淘宝源）
pnpm install
# 2) 启动后端后生成契约
bash scripts/gen-api.sh
# 3) 前端把 features/<域>/api.js 里的手写函数换成 @fullstack-base/api-client 的导出
```

## 契约纪律

- Controller 的 `@Tag` 用**英文**（会成为 orval 目录名）；每个方法显式写 `operationId`（会成为前端函数名）。
- 根 `package.json` 的 `contractVersion` 与 `OpenApiConfig.CONTRACT_VERSION` **必须同步**；
  `gen-api.sh` 会断言，不同步就拒绝生成。破坏性变更 bump MAJOR，兼容新增 bump MINOR。
- 改了接口却不重新生成契约 = 前端类型撒谎；提交时把 `openapi.json` 与生成物一起入库。

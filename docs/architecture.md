# 架构说明

## 分层依赖（严格单向）

```
main（可执行：启动类 + 配置 + Flyway + 静态预览页）
  └─ modules/user（示例业务模块，未来 modules/order、modules/report…）
       └─ core（全局异常 / 分页插件 / CORS / OpenAPI / 功能注册表）
            └─ common（R / PageResult / BizException，不依赖 Spring）
```

- 启动类 `com.example.base.BaseApplication` 必须在**最外层包**，组件扫描才能覆盖 core 与所有 modules。
- 业务模块之间**禁止互相依赖**；需要复用先下沉到 core / common。
- `core` 承担所有跨模块技术能力，`main` 只做聚合，不写业务。

## 一次列表请求的完整链路

```
浏览器 /preview.html 或 Vite :5174
  │  GET /api/users?page=1&size=10&keyword=张&status=1
  ▼
（Vite 场景）vite.config.js 代理 → http://localhost:8082
  ▼
UserController @RestController @RequestMapping("/api/users")
  │  @Valid UserQuery（jakarta.validation，失败由全局异常转 R.fail(400)）
  ▼
UserService / UserServiceImpl extends ServiceImpl<UserMapper, User>
  │  LambdaQueryWrapper：and(...) 括号包住多个 OR + eq 开关 + orderByDesc
  │  MP 分页插件 → 先 COUNT 再 LIMIT；@TableLogic 自动追加 deleted = 0
  ▼
H2（MODE=MySQL）或 MySQL 8.4
  │  实体 User（@TableName("t_user")）
  ▼
UserVO（id → String / statusLabel / createdAt 格式化）
  ▼
R.ok(PageResult{items,total,page,size}) → JSON
  ▼
axios 响应拦截器拆掉 R 外壳 → 页面只拿到 data
```

## 功能注册表（扩展的地基）

```
core/FeatureRegistry.java  ──GET /api/features──┬──> 免构建预览页菜单
                                                └──> Vite App.vue 一致性提示

apps/frontend/src/config/features.js ──> router/index.js 自动生成路由 ──> 菜单
                                    └──> App.vue 渲染 ready=false 的占位卡片
```

两处 key 必须一致，由 `scripts/check-features.js` 校验。
**新增功能时两边都由 `scripts/new-module.js` 自动插入**，人不参与就不会漏。

## 契约链路

```
Controller 的 @Tag / @Operation
  → springdoc 2.8.13 暴露 /v3/api-docs 与 /swagger-ui.html
  → scripts/gen-api.sh（版本闸门 + 导出）
  → packages/api-client/openapi.json（入库，PR 可 diff）
  →（可选）orval 生成 src/gen/<域>/<域>.ts
  → 前端从 @fullstack-base/api-client 导入
```

当前阶段为了「零依赖也能跑」，前端各域 `api.js` 仍是手写函数；
`packages/api-client/src/index.js` 是未来生成物的唯一出口，不反向依赖前端代码。

## 数据与环境

| 项 | 默认 | 切换方式 |
|---|---|---|
| 数据库 | H2 内存（MODE=MySQL，Flyway 自动建表灌 24 条） | `-Dspring-boot.run.profiles=mysql` |
| 迁移 | `main/src/main/resources/db/migration/V1__init.sql`、`V2__seed_data.sql` | 新增 `V<n>__xxx.sql`，版本号只增不改 |
| 端口 | 8082（pom 插件用命令行参数钉死） | 改 `application.yml` + `vite.config.js` + smoke 脚本 |
| 虚拟线程 | `spring.threads.virtual.enabled=true`（JDK 21） | 注意 21 里 `synchronized` 会 pin 载体线程 |

## 与 tcmp 的对应关系

| tcmp | 本工程 | 说明 |
|---|---|---|
| Nuxt 4 + Tailwind + vue-query | Vue3 + Vite + vue-router | 降依赖，保证离线可跑 |
| Spring Boot 4.1 / springdoc 3.1 | Boot 3.5.16 / springdoc 2.8.13 | 按当前项目版本基准 |
| Oracle/PG/MySQL/信创六方言 | H2 + MySQL 两档 | 先跑通，后续按 profile 扩 |
| `schema/postgres/schema.sql` 权威文件 | Flyway 版本脚本 | 演示工程更适合增量迁移 |
| `templates/` + `scripts/` + `AGENTS.md` | 完全对齐 | **这是本次复用的核心** |
| 验证金字塔 L0–L3 | L0–L2 | 暂不引入分钟级 E2E |

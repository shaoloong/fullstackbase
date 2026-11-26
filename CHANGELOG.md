# Changelog

本项目采用 [Semantic Versioning](https://semver.org/)。**当前版本段落置顶**，每次版本相关变更
（版本号 bump、发版、用户可感知行为变更、契约版本变更）都在此追加条目。

## 0.2.0 — 2026-11-26

新增产品管理功能域，契约版本同步 bump 至 0.2.0。

### 新增

- **产品管理模块**（`modules/product`）：`GET/POST/PUT/DELETE /api/products` 五个接口，
  支持分页、名称/编码关键词、状态筛选、详情、新增、修改与逻辑删除；编码唯一冲突返回 409。
- **`t_product` 表与 `V3__product.sql`**：含 `uk_product_code` 唯一约束与
  `idx_product_status_created` 联合索引；未修改 `V1`/`V2`。
- **前端 `/products` 页面**：列表/筛选/分页与新增/编辑/删除表单闭环，路由与菜单由注册表生成。
- **CRUD 集成测试**：`ProductServiceTest` 覆盖分页、筛选、新增、修改、逻辑删除与 400/404/409 业务码。

### 变更

- 契约版本 0.1.0 → 0.2.0（根 `package.json`、`OpenApiConfig`、`openapi.json` 三处同步）。
- 修复基线 `UserService.java` 中阻断编译的孤立字符 `i`（零行为变更）。

## 0.1.0 — 2026-11-13

首个可用底座，结构参考 `D:\Workspace\tcmp`，内置一个已跑通的用户列表查询功能。

### 新增

- **Maven 多模块后端**：`common → core → modules/user → main`，JDK 21 + Spring Boot 3.5.16 +
  MyBatis-Plus 3.5.9 + Flyway + springdoc 2.8.13，默认 H2 内存库零配置启动。
- **Vue3 + Vite 前端**：`features/<域>` 功能域、注册表自动路由、axios 拦截器拆统一响应外壳。
- **功能注册表**：后端 `FeatureRegistry` 暴露 `GET /api/features`，前端 `config/features.js`
  生成路由与菜单；`scripts/check-features.js` 校验两边 key 一致并守护 6 处扩展锚点。
- **扩展入口**：`scripts/new-module.js` 一条命令生成后端模块 + 前端功能域 +
  `V<n>` 建表脚本 + 6 处登记；`templates/` 提供三套可复制模板。
- **契约层**：`packages/api-client`（openapi.json 快照 + 生成物出口 + 版本闸门脚本）。
- **零依赖预览页**：`static/preview.html`（Vue3 运行时本地 vendor），npm 装不上时仍可看全链路。
- **规则与文档**：根 `AGENTS.md` / `CLAUDE.md`、后端 `AGENTS.md`、
  `docs/extend-guide.md`（扩展手册）、`architecture.md`、`troubleshooting.md`。

### 已固化的实测结论

- MP 3.5.9 的 `PaginationInnerInterceptor` 在独立模块 `mybatis-plus-jsqlparser`，必须显式引入。
- 环境注入的 `SERVER__PORT` 会被 Spring Boot 松散绑定识别成 `server.port`，
  已用 spring-boot-maven-plugin 的命令行参数钉死 8082。
- 用户级 `.npmrc` 企业镜像对 scoped 包返回 403，工程级 `.npmrc` 已指向淘宝源。
- 端口规划避开本机已有服务：后端 8082 / 前端 5174 / MySQL 13307。

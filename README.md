# fullstack-base — 可扩展全栈底座

结构参考 `D:\Workspace\tcmp`，技术栈按当前项目基准收敛为 **JDK 21 + Spring Boot 3.5.16 + Vue3 + Vite**。
不预设业务，内置一个已跑通的「用户列表查询」作为范例，重点是把**后面继续加功能的入口**全部铺好。

> AI 代理请先读 [AGENTS.md](AGENTS.md)；要加功能直接看 [docs/extend-guide.md](docs/extend-guide.md)。

## 技术栈

| 层 | 选型 |
|---|---|
| 编排 | pnpm workspaces + Turborepo（JS 侧）；Maven 多模块（Java 侧，common→core→modules→main） |
| 后端 | JDK 21 + Spring Boot 3.5.16 + MyBatis-Plus 3.5.9 + Flyway + springdoc 2.8.13 |
| 前端 | Vue 3 + Vite 6 + vue-router（`features/<域>` 功能域 + 注册表自动路由） |
| 契约 | springdoc `/v3/api-docs` → `packages/api-client/openapi.json` →（可选）orval 生成 TS 客户端 |
| 数据库 | 默认 H2 内存（零配置，启动自动建表灌数）；`mysql` profile 切 MySQL 8.4 |

## 快速开始

前置：JDK 21、Maven 3.9+、Node 20+；真库联调才需要 Docker。

```bash
# 后端（默认 H2，零配置）
pnpm backend:dev
# 或纯 Maven
mvn -f apps/backend/pom.xml -q -DskipTests install -T 1C
mvn -f apps/backend/pom.xml -pl main spring-boot:run
```

打开 **http://localhost:8082/preview.html** —— 后端同源提供的零依赖 Vue3 预览页，
菜单来自 `GET /api/features`，列表来自 `GET /api/users`，不需要装任何前端依赖。

正式前端工程（需要 node_modules）：

```bash
pnpm install
pnpm dev            # Vite http://localhost:5174
```

> 本机若 `pnpm install` 失败：工程级 `.npmrc` 已指向淘宝源，覆盖用户级企业内网镜像；
> 若仍被网络策略拦住，用上面的免构建预览页，后端与接口链路完全不受影响。

## 内置功能验证

```bash
bash scripts/backend-smoke.sh
# ✅ 功能目录 / 分页 total=23 / id 为 String / keyword=张 / status=0 / page=0 → 400 / api-docs
```

Swagger UI：http://localhost:8082/swagger-ui.html ｜ H2 控制台：http://localhost:8082/h2-console

## 目录结构

```
apps/backend/
├── common/     R / PageResult / BizException（不依赖 Spring）
├── core/       全局异常 / 分页插件 / CORS / OpenAPI / FeatureRegistry（功能目录）
├── modules/    user/（示例业务模块）· 新增模块放这里
└── main/       启动类 + application*.yml + Flyway 迁移 + static/preview.html
apps/frontend/
├── src/config/features.js     ★ 前端功能注册表（路由与菜单由此生成）
├── src/router/index.js        自动路由，不用手改
├── src/features/<域>/         功能域：Page.vue + useList + api + index（域出口）
└── src/api/http.js            axios 拦截器统一拆 R 外壳
packages/api-client/           契约层（openapi.json + 生成物出口）
scripts/                       new-module（扩展入口）/ check-features / smoke / gen-api
templates/                     后端模块 / 前端功能域 / 迁移脚本三套模板
docs/                          extend-guide / architecture / troubleshooting
deploy/nginx/                  反向代理示例
```

## 怎么加第二个功能（核心）

```bash
node scripts/new-module.js --name order --title 订单 --dry-run   # 预览计划
node scripts/new-module.js --name order --title 订单             # 生成
```

一条命令会同时产出：后端模块（entity/mapper/service/controller/VO/DTO/测试）、前端功能域、
Flyway 建表脚本，并自动改好 6 处登记（两处 POM、两处注册表、契约出口）。
生成后按 [docs/extend-guide.md](docs/extend-guide.md) 改字段、补索引、跑自证。

## 多数据库

| profile | 库 | 用途 |
|---|---|---|
| 默认（dev） | H2 内存（MODE=MySQL） | L0–L2 自证，零配置，重启清空 |
| `mysql` | MySQL 8.4（compose，宿主 13307） | 真库联调 |

```bash
docker compose up -d --wait
mvn -f apps/backend/pom.xml -pl main spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 端口规划（避开本机已有服务）

| 服务 | 端口 | 说明 |
|---|---|---|
| 本工程后端 | 8082 | 8080 是教程预览、8081 是 demo-fullstack |
| 本工程前端 | 5174 | 避开常用 5173 |
| 可选 MySQL | 13307 | 避开本机 3306、demo 的 3307、tcmp 的 13306 |

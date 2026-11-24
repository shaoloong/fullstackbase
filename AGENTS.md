# fullstack-base — AI 代理工作指南（根）

> 本文件只放**必须全局知道的事**。后端细节看 `apps/backend/AGENTS.md`，扩展操作看 `docs/extend-guide.md`。
> 改规则文档前先想清楚：规则是给未来的自己/AI 看的，写进去的每条都要能被脚本或测试验证。

## 项目是什么

**不预设业务的可扩展全栈底座**（结构参考 `D:\Workspace\tcmp`）：
前端 Vue3 + Vite，后端 JDK 21 + Spring Boot 3.5.16 Maven 多模块，默认 H2 内存库零配置启动。
前端绝不直连数据库，数据访问全部收敛在 Java 后端。

```
fullstack-base/
├── apps/backend/       # Maven 多模块：common → core → modules/* → main
├── apps/frontend/      # Vue3 + Vite：features/<域> + 注册表自动路由
├── packages/api-client # 契约层：openapi.json +（可选）orval 生成物
├── scripts/            # new-module / check-features / smoke / gen-api  ← 扩展入口在这里
├── templates/          # backend-module / frontend-feature / migration   ← 可复制模板
├── docs/               # extend-guide（操作手册）/ architecture / troubleshooting
├── deploy/             # nginx 反向代理示例
└── docker-compose.yml  # 可选 MySQL 8.4（宿主 13307）
```

## 任务命令表（按需执行）

| 命令 | 作用 | 自证层级 |
|---|---|---|
| `pnpm backend:dev` | 后端 install + 启动（:8082，默认 H2） | — |
| `pnpm dev` | turbo 并行起前后端（需先 pnpm install） | — |
| `mvn -f apps/backend/pom.xml -q -DskipTests package` | L0 全量构建 | **L0** |
| `mvn -f apps/backend/pom.xml -q test` | L1 单测/集成（H2） | **L1** |
| `bash scripts/backend-smoke.sh` | L2 后端冒烟：curl 断言关键接口 | **L2** |
| `bash scripts/gen-api.sh` | 导出 openapi.json（改接口后必跑） | — |
| `node scripts/check-features.js` | 前后端注册表一致性 + 锚点守护 | **L0** |
| `node scripts/new-module.js --name order --title 订单` | **一条命令新增功能** | — |
| `pnpm build` | 前端 vite build + 后端 package | **L0** |

端口：后端 **8082**、Vite **5174**、可选 MySQL **13307**。避开本机已占用的 8080 / 8081 / 3306 / 3307。

## 验证金字塔（改完必须在对应层级自证，报告写明层级）

```
L2 后端冒烟   scripts/backend-smoke.sh   秒级   —— 起服务 curl 断言，改 Controller/新增接口必跑
L1 单测集成   mvn test                   秒级   —— H2 替身库，确定性、可离线
L0 编译/校验  mvn package / check:features —— 每次必过
```

| 改动范围 | 最低自证层级 |
|---|---|
| 注释 / 文档 / 配置 | L0 |
| Service 逻辑 / 新增字段 | L1（对应单测） |
| Controller / 新增接口 / 新增功能 | L2（起后端 curl）+ 跑 `gen-api` |
| 前后端注册表改动 | `check-features.js` 必过 |

**硬规则**：不允许「仅编译通过」就交付；默认 H2 零配置即可完成 L0–L2，不需要 Docker。

## 新增功能的标准流程（唯一推荐路径）

```bash
# 1) 先看计划（不写盘）
node scripts/new-module.js --name order --title 订单 --dry-run
# 2) 真生成：后端模块 + 前端功能域 + 6 处登记 + V<n> 建表脚本
node scripts/new-module.js --name order --title 订单
# 3) 按 docs/extend-guide.md 改字段、补索引
# 4) 自证
mvn -f apps/backend/pom.xml -q test
bash scripts/backend-smoke.sh
node scripts/check-features.js
bash scripts/gen-api.sh             # 改了接口才需要
```

只想**先占位、后实现**：加 `--placeholder`，注册表 `ready=false`，菜单显示「待实现」卡片，
接口不会被冒烟脚本当 ready 功能断言，实现完把两处 `ready` 改 true。

生成器会自动改的 6 处（漏一处就编译不过或接口 404）：
父 POM `<modules>`、父 POM `dependencyManagement`、main POM 依赖、后端 `FeatureRegistry`、
前端 `config/features.js`、`packages/api-client/src/index.js`。

## 规则索引（按任务跳转，不预读）

- 新增业务模块 / 页面 / 建表 → [`docs/extend-guide.md`](docs/extend-guide.md)
- 后端分层依赖、profile 切库、SQL 规范 → [`apps/backend/AGENTS.md`](apps/backend/AGENTS.md)
- 前后端契约、版本同步 → [`packages/api-client/README.md`](packages/api-client/README.md)
- 已实测的坑（环境 / 依赖 / 端口） → [`docs/troubleshooting.md`](docs/troubleshooting.md)

## 完成定义（DoD）

- [ ] 对应层级（L0–L2）已通过，报告写明自证层级
- [ ] `node scripts/check-features.js` 通过（前后端注册表 key 一致，6 处锚点完好）
- [ ] 新增功能：字段 / 建表脚本 / 索引已按业务补齐，不是留着模板默认列交付
- [ ] 改了接口：`gen-api.sh` 已跑，`contractVersion` 与 `OpenApiConfig.CONTRACT_VERSION` 同步
- [ ] 版本相关变更已写进根 `CHANGELOG.md`
- [ ] 涉及 DB：迁移脚本版本号递增，且**没有修改已执行过的旧脚本**

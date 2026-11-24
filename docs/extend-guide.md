# 扩展指南：怎么继续加功能

> 这是本工程最重要的文档。目标：**加一个功能不需要改任何框架代码**，
> 所有「不改就 404 / 不报错但静默失效」的登记点都由脚本代劳。

## 三种扩展姿势

| 场景 | 做法 |
|---|---|
| 加一个标准列表功能（90% 情况） | `node scripts/new-module.js --name <域> --title <中文名>` |
| 只要菜单占位，功能以后再写 | 同上加 `--placeholder`，`ready=false` |
| 现有功能里加接口（POST/PUT/DELETE） | 直接改对应模块，见文末「在已有模块里加接口」 |

## 路径 A：用生成器（推荐）

### 1. 先看计划

```bash
node scripts/new-module.js --name order --title 订单 --dry-run
```

输出会列出将新建的文件与将修改的 6 处登记，不写盘。

### 2. 真生成

```bash
node scripts/new-module.js --name order --title 订单
```

`--name` 必须是小写字母/数字（会作为 Java 包名、目录名、注册表 key）。
推导出的约定：

| 派生值 | order 的例子 |
|---|---|
| 后端模块目录 | `apps/backend/modules/order` |
| Java 包 | `com.example.base.modules.order` |
| artifactId | `base-module-order` |
| 类名前缀 | `Order` / `OrderController` / `OrderVO` |
| 接口 | `GET /api/orders` |
| 前端路由 | `/orders`，功能域 `src/features/order` |
| 表名 | `t_order`，迁移脚本 `V3__order.sql`（版本号自动取下一个） |

### 3. 生成器替你改好的 6 处登记

| # | 文件 | 插入内容 | 漏了会怎样 |
|---|---|---|---|
| 1 | `apps/backend/pom.xml` `<modules>` | `modules/order` | Maven 根本不编译，接口 404 |
| 2 | `apps/backend/pom.xml` `dependencyManagement` | `base-module-order` | 子 POM 报「版本缺失」 |
| 3 | `apps/backend/main/pom.xml` | 模块依赖 | 代码编译了但没进 jar，接口 404 且无报错 |
| 4 | `core/FeatureRegistry.java` | `FEATURES.add(...)` | 后端功能目录里没有，前端校验报不一致 |
| 5 | `apps/frontend/src/config/features.js` | `registry.push({...})` | 菜单与路由都没有 |
| 6 | `packages/api-client/src/index.js` | 导出说明 | 契约生成物没出口 |

生成器还会在**锚点前**插入（`// @NEW_FEATURE`、`<!-- @NEW_DEPENDENCY -->` 等），
`node scripts/check-features.js` 会守护这些锚点，删了就报错。

### 4. 生成后必改（模板只是骨架）

1. **字段**：模板只保留 `id / name / status / deleted / created_at / updated_at` 六个通用列。
   改 `entity`、`VO`、`Query`、`ServiceImpl#toVO()` 与条件构造器；
   特别把 `ServiceImpl` 里两个相同的 `like(name, ...)` 换成真实要匹配的列。
2. **建表**：`apps/backend/main/src/main/resources/db/migration/V<n>__order.sql` 补列与索引。
   表名 `t_` 前缀、类型保守、不用 MySQL 专有语法（H2 也要能跑）。
3. **状态字典**：`statusLabel` 的「启用/停用」按业务换成自己的枚举文案。
4. **前端列**：`src/features/order/Page.vue` 表格列、筛选项对齐 `OrderVO` / `OrderQuery`。

### 5. 自证

```bash
mvn -f apps/backend/pom.xml -q test          # L1（含模板生成的集成测试）
pnpm backend:dev                             # 重启后端让 Flyway 执行新脚本
bash scripts/backend-smoke.sh                # L2：会自动把注册表里 ready 的接口都测一遍
node scripts/check-features.js               # 前后端 key 一致 + 锚点完好
bash scripts/gen-api.sh                      # 改了接口必跑，同步 openapi.json
```

> 冒烟脚本会读 `/api/features`，对所有 `ready=true` 的功能自动发 GET 断言 200，
> 所以**新增功能不需要改冒烟脚本**。

## 路径 B：纯占位（`--placeholder`）

```bash
node scripts/new-module.js --name report --title 报表 --placeholder
```

生成物一样齐全，但两处注册表 `ready=false`：
菜单出现「🚧 已注册，待实现」卡片，冒烟脚本跳过它的接口断言。
实现完把 `FeatureRegistry.java` 与 `features.js` 里对应行的 `false` 改成 `true` 即可上线。

## 路径 C：在已有模块里加接口

以「用户新增」为例，全部改动都在 `modules/user/` 内，**不用碰注册表**：

1. `dto/UserCreateReq.java`：字段加 `jakarta.validation` 注解（`@NotBlank`、`@Email`…）。
2. `service/UserService.java` + `impl`：新增方法，写方法加
   `@Transactional(rollbackFor = Exception.class)`（默认只回滚 RuntimeException，受检异常不写就静默不回滚）。
3. `controller/UserController.java`：`@PostMapping`，显式写 `@Operation(operationId = "createUser")`。
4. 失败时 `throw new BizException(409, "手机号已存在")`，由全局异常统一转 `R.fail`。
5. 前端 `features/user/api.js` 加 `createUser`，`useUserList.js` 暴露动作，`Page.vue` 加按钮与表单。
6. 跑 `bash scripts/gen-api.sh` 重新导出契约。

## 登记点总览（手工加模块时的完整清单）

如果不用生成器，必须手工完成：

- [ ] 复制 `templates/backend-module` → `apps/backend/modules/<域>`，替换 7 个占位符
- [ ] 复制 `templates/frontend-feature` → `apps/frontend/src/features/<域>`
- [ ] 新建 `db/migration/V<n>__<域>.sql`（版本号递增，绝不改旧脚本）
- [ ] 6 处登记（见上表）
- [ ] `node scripts/check-features.js` 通过
- [ ] L1 + L2 自证

## 纪律（踩过的坑写在这里）

- **分层单向**：`main → modules/<域> → core → common`，业务模块之间禁止互相依赖；
  要复用就下沉到 `core`（技术能力）或 `common`（数据结构）。
- **实体不出 Controller**：一律转 VO；`Long id` 必须转 `String`（JS 超过 2^53 静默丢精度）。
- **迁移脚本只增不改**：已执行过的脚本内容变了，Flyway 校验和不匹配，启动直接失败。
- **改了接口就跑 gen-api**：否则前端类型在撒谎，`contractVersion` 与 `OpenApiConfig` 要同步 bump。
- **注册表 key 两边一致**：`check-features.js` 是唯一裁判，不要靠肉眼。
- **条件构造器的 OR 要用 `and(...)` 包括号**：否则会和 status 条件串成 `(A OR B AND C)`。

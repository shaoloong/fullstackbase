# 后端工作指南（apps/backend）

JDK 21 + Spring Boot 3.5.16 + MyBatis-Plus 3.5.9 + Flyway + springdoc 2.8.13，Maven 多模块。

## 分层依赖（严格单向）

`main` → `modules/<域名>` → `core` → `common`。
启动类 `com.example.base.BaseApplication` 必须在最外层包，否则组件扫描不到子模块。

| 模块 | 放什么 | 别放什么 |
|---|---|---|
| `common` | `R` / `PageResult` / `BizException` | 任何 Spring / Web 依赖 |
| `core` | 全局异常、MP 分页、CORS、OpenAPI、FeatureRegistry | 业务表、业务接口 |
| `modules/*` | 单域的 entity / mapper / dto / vo / service / controller | 跨域调用、跨模块依赖 |
| `main` | 启动类、`application*.yml`、Flyway 脚本、`static/` | 业务逻辑 |

需要跨模块复用，先下沉到 `core`（技术能力）或 `common`（数据结构）。

## 常用命令（cwd = 工程根）

| 命令 | 说明 |
|---|---|
| `mvn -f apps/backend/pom.xml -q -DskipTests install -T 1C` | 安装全部模块到本地仓库（**改了任何模块后必做**） |
| `mvn -f apps/backend/pom.xml -pl main spring-boot:run` | 起服务（:8082，默认 H2） |
| `mvn -f apps/backend/pom.xml -q test` | L1 测试（H2） |
| `mvn -f apps/backend/pom.xml -q -DskipTests package` | L0 构建，产物 `main/target/base-main-0.1.0-SNAPSHOT.jar` |

⚠️ `spring-boot:run` 对兄弟模块走**本地仓库**解析：改了 common/core/modules 后不先 `mvn install`，
跑的还是上一次的 jar，症状是「我明明改了怎么没生效」。根 `pnpm backend:dev` 已把两步串好。

## 数据库

- 默认 H2 内存（`MODE=MySQL`），Flyway 启动时执行 `db/migration/V*.sql`，重启清空重灌。
- `mysql` profile 连 compose 的 MySQL 8.4（宿主 13307）：
  `mvn -f apps/backend/pom.xml -pl main spring-boot:run -Dspring-boot.run.profiles=mysql`
- 同一份迁移脚本要能同时跑在 H2 与 MySQL：**不用**列内联 `COMMENT`、`ENGINE=InnoDB`、
  `ON DUPLICATE`、`NOW()` 等方言写法。

### SQL / 实体规范

- 表名 `t_` 前缀；主键 `BIGINT`；金额一律 `DECIMAL(p,s)`，绝不用 `FLOAT/DOUBLE`。
- 时间用 `DATETIME` + 应用侧 `LocalDateTime`；`application.yml` 已设 `Asia/Shanghai`，不设会差 8 小时。
- **零手写 SQL**：CRUD/分页用 `BaseMapper` + `LambdaQueryWrapper`；分页交给 MP 插件，
  忘了配 `PaginationInnerInterceptor` 会返回全表且 `total=0`（不报错，最坑）。
- 多个 OR 条件必须用 `and(w -> ...)` 包成括号整体，否则与后续 AND 条件串成错误逻辑。
- `wrapper.eq(cond, col, val)` 的 cond 开关别用 `eq(col, null)`——生成 `= NULL` 永不命中。
- `@TableLogic` 会自动追加 `deleted = 0`；删除是 UPDATE，不是物理删除。
- **DTO 的 id 一律 String**（雪花超 JS 2^53），但调 `removeById` 等 MP 方法要传 Long。

## 接口与契约

- 所有接口返回 `R<T>`；失败用 `throw new BizException(code, msg)`，由 `GlobalExceptionHandler` 统一转换。
- 写方法加 `@Transactional(rollbackFor = Exception.class)`（默认只回滚 RuntimeException）。
- `@Tag(name = "英文")` + 每个方法显式 `operationId`；它们会变成前端生成的目录名与函数名。
- 改接口后跑 `bash scripts/gen-api.sh`，并同步 `contractVersion` 与 `OpenApiConfig.CONTRACT_VERSION`。

## 测试

- surefire 只跑 `*Test`，写成 `*IT` 会被**静默跳过**。
- 模板生成的集成测试放在 `main/src/test/java/com/example/base/modules/<域>/`，
  因为业务模块 classpath 里没有启动类（`@SpringBootTest` 找不到 `@SpringBootConfiguration`）。
- Service 纯单测可用 Mockito，不起 Spring 容器，更快。

## 新增模块

用 `node scripts/new-module.js --name <域> --title <中文名>`，见 `docs/extend-guide.md`。
手工方式必须完成 3 处 POM 登记 + 2 处注册表登记，否则编译过了接口也 404。

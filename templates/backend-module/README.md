# 后端业务模块模板

> 由 `scripts/new-module.js` 自动实例化，**不要手工复制本目录**。
> 手工方式见 [`docs/extend-guide.md`](../../docs/extend-guide.md)。

## 占位符

| 占位符 | 含义 | 示例（order） |
|---|---|---|
| `__domain__` | 小写域名 / 包名 / 目录名 | `order` |
| `__Domain__` | 大驼峰类名前缀 | `Order` |
| `__title__` | 中文功能名 | `订单` |
| `__table__` | 表名 | `t_order` |
| `__api__` | 后端接口前缀 | `/api/orders` |
| `__path__` | 前端路由 | `/orders` |

## 生成后会得到

```
apps/backend/modules/<域>/
├── pom.xml
└── src/main/java/com/example/base/modules/<域>/
    ├── entity/<Domain>.java          实体（@TableName / @TableLogic）
    ├── mapper/<Domain>Mapper.java    BaseMapper
    ├── dto/<Domain>Query.java        查询入参（jakarta.validation）
    ├── vo/<Domain>VO.java            对外视图（id 转 String）
    ├── service/<Domain>Service.java
    ├── service/impl/<Domain>ServiceImpl.java   分页 + 条件构造
    └── controller/<Domain>Controller.java      @Tag + operationId
└── src/test/java/.../<Domain>ServiceTest.java
```

## 生成后必改清单

1. **字段**：模板只保留了 `id / name / status / deleted / created_at / updated_at` 六个通用列，
   按业务替换，并同步改 VO、Query、ServiceImpl 的 `toVO()` 与条件构造器。
2. **建表**：`db/migration/V<n>__<域>.sql` 已生成骨架，补齐列与索引。
   表名 `t_` 前缀，列类型保守（BIGINT / VARCHAR / DECIMAL / DATETIME），
   不用数据库专有语法，保证 H2 与 MySQL 都能跑。
3. **契约**：Controller 的 `@Tag` 保持英文，每个新方法都要有显式 `operationId`；
   改完接口跑 `pnpm gen:api`。
4. **业务动作**：新增 POST / PUT / DELETE 时沿用 `R` 外壳；
   写方法加 `@Transactional(rollbackFor = Exception.class)`，
   失败 `throw new BizException(code, msg)`，由全局异常统一转响应。
5. **自证**：`mvn -f apps/backend/pom.xml -q test`（L1）→ `bash scripts/backend-smoke.sh`（L2）。

## 分层铁律

`main → modules/<域> → core → common`，严格单向。业务模块之间**禁止互相依赖**；
需要复用就下沉到 `core`（技术能力）或 `common`（数据结构）。

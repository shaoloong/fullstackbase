# Flyway 迁移脚本

后端启动时按**文件名版本号**顺序执行，只跑一次；`flyway_schema_history` 记录校验和。

```
V1__init.sql          建 t_user 表 + 索引
V2__seed_data.sql     灌 24 条演示数据（含 1 条 deleted=1，用来验证逻辑删除）
V<n>__<域>.sql        新增功能时由 scripts/new-module.js 自动生成
```

## 铁律

1. **版本号只增不改**：新需求一律新建 `V<n+1>__xxx.sql`。
   修改已执行过的脚本 → 校验和不匹配 → 启动直接失败（H2 重启是空库反而能过，真库一定炸）。
2. **文件名双下划线**：`V3__order.sql`（`V` + 版本号 + `__` + 描述 + `.sql`）。
3. **一份脚本兼容两个库**：默认 H2 `MODE=MySQL`，`mysql` profile 是 MySQL 8.4。
   不用列内联 `COMMENT`、`ENGINE=InnoDB`、`ON DUPLICATE`、`NOW()` 等方言写法。
4. **类型保守**：`BIGINT` / `VARCHAR(n)` / `DECIMAL(p,s)` / `DATETIME`；
   金额绝不用 `FLOAT/DOUBLE`；表名 `t_` 前缀。
5. **逻辑删除列** `deleted TINYINT DEFAULT 0` 与实体 `@TableLogic` 配套。
6. **索引按真实查询建**：`LIKE '%xx%'` 前缀带 `%` 用不上普通索引，数据量大要换方案。

## 切真库

```bash
docker compose up -d --wait
mvn -f apps/backend/pom.xml -pl main spring-boot:run -Dspring-boot.run.profiles=mysql
```

`baseline-on-migrate: true` 只在「库里已有表但没有迁移记录」时兜底；
干净的新库会正常从 V1 开始跑。

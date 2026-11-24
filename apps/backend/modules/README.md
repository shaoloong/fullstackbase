# 业务模块目录（扩展点）

每个子目录是一个独立业务域，**只依赖 `core`，模块之间禁止互相依赖**。

```
modules/
├── user/     # 已实现：分页 / 关键词 / 状态筛选（范例，照它写新模块）
└── <新域>/   # 由 node scripts/new-module.js 生成
```

## 新增模块的三种登记（生成器自动完成）

1. 根 `apps/backend/pom.xml` 的 `<modules>` 加一行 → 否则 Maven 不编译
2. 根 `apps/backend/pom.xml` 的 `dependencyManagement` 加坐标 → 否则子 POM 报版本缺失
3. `main/pom.xml` 加依赖 → 否则代码没进 jar，接口 404 且无报错

外加后端功能注册表 `core/FeatureRegistry.java` 加 `FEATURES.add(...)`。

## 单个模块内部约定

```
<域>/
├── entity/      数据库实体（@TableName / @TableLogic），不出 Controller
├── mapper/      BaseMapper 接口
├── dto/         入参（jakarta.validation 校验）
├── vo/          出参（id 转 String / 状态字典 / 时间格式化）
├── service/     接口 + impl（ServiceImpl<Mapper, Entity>）
└── controller/  @RestController，@Tag 英文 + 显式 operationId
```

集成测试不放在这里，放 `main/src/test/java/com/example/base/modules/<域>/`
（业务模块 classpath 没有启动类）。生成器已经替你放好位置。

详见 [`docs/extend-guide.md`](../../../docs/extend-guide.md)。

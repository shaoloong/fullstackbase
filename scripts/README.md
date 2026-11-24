# scripts/ 目录说明

| 脚本 | 作用 | 依赖 |
|---|---|---|
| `new-module.js` | **扩展入口**：一条命令生成后端模块 + 前端功能域 + 6 处登记 + 建表脚本 | 仅 Node |
| `check-features.js` | 前后端功能注册表 key 一致性 + 扩展锚点守护 | 仅 Node |
| `backend-smoke.sh` | L2 冒烟：接口断言（自动覆盖注册表里所有 ready 功能） | curl + Node |
| `gen-api.sh` | 契约导出：版本闸门 → openapi.json →（可选）orval | curl + Node |

设计原则：**扩展入口与校验脚本零外部依赖**。`pnpm install` 装不上（企业内网源 / 离线）
也能生成新功能、做一致性检查；只有前端构建与 orval 才需要 node_modules。

## 常用命令

```bash
node scripts/new-module.js --name order --title 订单 --dry-run  # 先看计划
node scripts/new-module.js --name order --title 订单            # 真生成
node scripts/check-features.js                                 # 校验登记
bash scripts/backend-smoke.sh                                  # 后端已在 8082 时跑
bash scripts/gen-api.sh                                        # 改接口后同步契约
```

## 扩展锚点（勿删）

生成器用字符串锚点定位插入点，`check-features.js` 会守护它们：

```
apps/backend/pom.xml                                  <!-- @NEW_MODULE -->
apps/backend/pom.xml                                  <!-- @NEW_DEPENDENCY -->
apps/backend/main/pom.xml                             <!-- @NEW_DEPENDENCY -->
apps/backend/core/.../FeatureRegistry.java            // @NEW_FEATURE
apps/frontend/src/config/features.js                  // @NEW_FEATURE
packages/api-client/src/index.js                      // @NEW_EXPORT
```

# 排障记录（都是实测踩过的）

## 1. `SERVER__PORT` 环境变量顶掉端口

**症状**：`application.yml` 写 8081/8082，启动却报 `Port 62192 was already in use`。

**根因**：某些 IDE / CI 注入 `SERVER__PORT`，Spring Boot 的 relaxed binding 把它识别成
`server.port`，优先级高于配置文件。

**处理**：`apps/backend/main/pom.xml` 的 spring-boot-maven-plugin 里加
`<arguments><argument>--server.port=8082</argument></arguments>`。命令行参数优先级最高。
用 `java -jar` 启动时同理传 `--server.port=8082`。

## 2. MyBatis-Plus 3.5.9 编译报「找不到 PaginationInnerInterceptor」

**根因**：MP 3.5.9 起把分页插件拆到独立模块 `mybatis-plus-jsqlparser`，
starter 不再传递依赖它。

**处理**：父 POM 的 `dependencyManagement` 与 `core/pom.xml` 都显式引入
`com.baomidou:mybatis-plus-jsqlparser:${mybatis-plus.version}`。

## 3. npm / pnpm 装不上依赖

**症状 A**：`@vitejs/plugin-vue` 返回 403。
**根因 A**：用户级 `~/.npmrc` 把 registry 锁在企业内网镜像。
**处理 A**：工程根放 `.npmrc` 指向 `https://registry.npmmirror.com/`（项目级优先级更高，且不污染全局）。

**症状 B**：换源后仍 180s 超时、`node_modules` 为空。
**根因 B**：当前沙箱的网络策略。
**处理 B**：后端提供 `static/preview.html`（Vue3 运行时已放进 `static/vendor/`），
同源请求 `/api/**`，**不装任何前端依赖也能看完整链路**。正式开发用 Vite 工程。

## 4. 端口冲突

本机已有：8080（教程预览）、8081（demo-fullstack 后端）、3306（本机 MySQL）、3307（demo compose）。
本工程统一用 8082 / 5174 / 13307。改端口要同步四处：
`application.yml`、`vite.config.js`、`main/pom.xml` 的 arguments、`scripts/backend-smoke.sh` 的 BASE。

## 5. 新增模块后接口 404，但编译通过

按概率排查：
1. 父 POM `<modules>` 没加 → Maven 根本没编译这个模块；
2. `main/pom.xml` 没加依赖 → 类没进 jar（最隐蔽，无任何报错）；
3. Controller 的包不在 `com.example.base` 之下 → 组件扫描不到；
4. 忘了重启后端 → 新 Controller 没加载（Flyway 新脚本也没执行）。

用生成器就不会遇到：`node scripts/new-module.js` 会把 1、2 都写好。
再跑 `bash scripts/backend-smoke.sh`，它会遍历功能目录对每个 ready 接口发请求。

## 6. 前后端功能注册表不一致

菜单点了 404 / 后端接口没人调，两边却能各自编译通过。
跑 `node scripts/check-features.js`，它比对 `features.js` 的 key 与
`FeatureRegistry.java` 的 `new Feature(...)`，并检查 6 处扩展锚点是否完好。

## 7. JDK 17 → 21 升级注意

- Lombok 需 **1.18.30+**，否则 `NoSuchFieldError: JCTree$JCImport`；
- Mockito / ByteBuddy 太旧会报 `Unsupported class file major version 65`（65 = Java 21）；
- IDEA 里除了 pom 的 `java.version`，还要改 **Project Structure → SDK** 和 **Maven → Runner → JRE**，
  否则「IDE 里能跑、命令行编译报错」；
- JDK 21 虚拟线程里 `synchronized` 会 **pin 住载体线程**（JDK 24 才修），别在同步块里做 IO，换 `ReentrantLock`；
- String Templates 在 21 只是预览且后来被移除，别写进业务代码。

## 8. curl 访问本机服务返回异常

环境可能注入 HTTP 代理，本机回环请求要加 `--noproxy '*'`：

```bash
curl -s --noproxy '*' http://127.0.0.1:8082/api/users
```

`scripts/backend-smoke.sh` 与 `scripts/gen-api.sh` 内部已处理。

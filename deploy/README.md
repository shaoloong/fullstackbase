# deploy/ 部署说明

本地开发不需要本目录：Vite 已把 `/api` 代理到 8082，浏览器同源。

## 生产形态

```
浏览器 ──> Nginx(:80)
            ├── 静态资源  apps/frontend/dist（vite build 产物）
            └── /api、/swagger-ui.html、/preview.html ──> Spring Boot(:8082)
```

```bash
# 1) 后端可执行 jar
mvn -f apps/backend/pom.xml -q -DskipTests package
java -jar apps/backend/main/target/base-main-0.1.0-SNAPSHOT.jar --spring.profiles.active=mysql

# 2) 前端静态产物
pnpm --filter @fullstack-base/frontend build

# 3) Nginx 引用 deploy/nginx/fullstack-base.conf（按实际路径改 root）
```

## 注意

- SPA 用 history 路由，Nginx 必须 `try_files ... /index.html`，否则刷新子路由 404。
- `index.html` 设 `no-cache`，带 hash 的 js/css 可长缓存。
- 生产务必关掉 `h2-console` 与 `swagger-ui`（上面示例为方便联调放开了，按安全要求收紧）。
- 数据库密码用环境变量注入，别把明文写进 `application-mysql.yml` 提交。
- 后端静态页 `preview.html` 只是零依赖兜底，生产可以保留作健康检查页，也可以不放行。

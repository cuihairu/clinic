# Clinic

门诊系统 monorepo。

## 目录结构

```
clinic/
├── server/    # 服务端（Java 17 / Maven，含 Dockerfile 与部署脚本）
├── app/       # 小程序端（Taro/多端工程，pnpm）
├── web/       # 管理端前端（Ant Design Pro / Umi，pnpm）
└── docs/      # 文档（在 server/docs）
```

- `server/`、`app/` 与 `web/` 的源码直接在本仓维护。
- 后端构建：`mvn -B package --file server/pom.xml`。
- 前端构建：进入 `web/`，`pnpm install && pnpm build`；小程序进入 `app/` 同法。

各组件的详细说明见各自目录内 README。

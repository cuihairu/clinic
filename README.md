# Sinomed

门诊系统 monorepo。

## 目录结构

```
sinomed/
├── server/    # 服务端（Java 21 / Maven，含 Dockerfile 与部署脚本）
├── app/       # 小程序端（Taro/多端工程，Node.js 24，pnpm）
├── web/       # 管理端前端（Ant Design Pro / Umi，Node.js 24，pnpm）
└── docs/      # 文档（在 server/docs）
```

- `server/`、`app/` 与 `web/` 的源码直接在本仓维护。
- 后端构建（需 Java 21）：`mvn -B package --file server/pom.xml`。
- 前端构建（需 Node.js 24）：进入 `web/`，`pnpm install && pnpm build`；小程序进入 `app/` 同法。

各组件的详细说明见各自目录内 README。

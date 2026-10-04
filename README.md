# Clinic

门诊系统 monorepo。

## 目录结构

```
clinic/
├── app/       # 服务端（Java 17 / Maven，含 Dockerfile 与部署脚本）
├── web/       # 管理端前端（Ant Design Pro / Umi，pnpm）
└── docs/      # 文档（在 app/docs）
```

- `app/` 与 `web/` 的源码直接在本仓维护。
- 后端构建：`mvn -B package --file app/pom.xml`。
- 前端构建：进入 `web/`，`pnpm install && pnpm build`。

各组件的详细说明见各自目录内 README。

<h1 align="center">Sinomed</h1>
<p align="center"><img src="docs/public/logo.svg" width="64" alt="Sinomed logo" /></p>
<p align="center"><b>中医医馆管理系统</b>：顾客建档 · 中医诊疗记录 · 卡项 · 复盘回访 · 员工考勤</p>
<p align="center">文档站：<a href="https://cuihairu.github.io/sinomed/">cuihairu.github.io/sinomed</a></p>

## 目录结构

```
sinomed/
├── server/    # 服务端（Java 21 / Maven，含 Dockerfile 与部署脚本）
├── app/       # 小程序端（Taro/多端工程，Node.js 24，pnpm）
├── web/       # 管理端前端（Ant Design Pro / Umi，Node.js 24，pnpm）
└── docs/      # 文档站（VitePress）与调研资料
```

- `server/`、`app/` 与 `web/` 的源码直接在本仓维护。
- 后端构建（需 Java 21）：`mvn -B package --file server/pom.xml`。
- 前端构建（需 Node.js 24）：进入 `web/`，`pnpm install && pnpm build`；小程序进入 `app/` 同法。

各组件的详细说明见各自目录内 README，以及[文档站](https://cuihairu.github.io/sinomed/)。

# 开发指南

## 目录约定

```text
sinomed/
├── server/    # 服务端（Java 21 / Spring Boot 3.5 / Maven）
│   └── src/main/java/com/sinomed/   # controller / service / repository / entity / security / config …
├── web/       # 管理端（Ant Design Pro / Umi，Node.js 24）
├── app/       # 小程序端（Taro 3.6 / React / NutUI，Node.js 24）
├── docs/      # 文档站（VitePress，本站源码）与调研资料
└── .github/workflows/               # CI（Maven 构建、文档站部署）
```

- 三端独立构建、互不依赖；管理端与小程序端通过服务端 `/api/v1` 接口通信。
- 前端包管理统一 pnpm；Java 构建使用仓库自带 `mvnw`（无需本机装 Maven）。
- `.nvmrc` 已在 `web/`、`app/` 声明 Node 24。

## 构建

| 目标 | 命令 | 产物 |
| ---- | ---- | ---- |
| 服务端 | `cd server && ./mvnw -B package -DskipTests` | `server/target/sinomed-server.jar` |
| 服务端（含测试） | `cd server && ./mvnw -B test`（需 MySQL，见下） | - |
| 管理端 | `cd web && pnpm install && pnpm build` | `web/dist/` |
| 管理端开发 | `cd web && pnpm dev` | `http://localhost:8000` |
| 小程序 H5 | `cd app && pnpm build:h5` | `app/dist/` |
| 小程序微信端 | `cd app && pnpm build:weapp` | `app/dist/`（微信开发者工具导入） |

## 测试

- **服务端**：`@SpringBootTest` 上下文测试需要真实 MySQL。先起一个测试库，再带环境变量跑：

```bash
docker run -d --name sinomed-mysql-test -e MYSQL_ROOT_PASSWORD=test123 \
  -e MYSQL_DATABASE=sinomed -p 3308:3306 mysql:8.0

cd server
DB_HOST=127.0.0.1 DB_PORT=3308 DB_NAME=sinomed \
DB_USERNAME=root DB_PASSWORD=test123 SERVER_PORT=2347 \
./mvnw -B test
```

- **管理端**：`pnpm lint`（eslint + prettier + tsc）、`pnpm jest`；
- **小程序端**：暂无测试脚本，以多端构建通过为准。

CI（GitHub Actions）：`Java CI with Maven` 在每次 push / PR 时执行 `mvn -B package --file server/pom.xml -DskipTests`（Java 21）。

## 环境版本要求

| 端 | 要求 |
| ---- | ---- |
| server | Java 21（`pom.xml` `java.version`，CI 同步）、Maven 3.9+ 或 `mvnw` |
| web / app | Node.js 24（`engines` + `.nvmrc` + CI 约束）、pnpm 9/10 |
| 文档站 | Node.js 24，`cd docs && pnpm install && pnpm build` |

## 依赖升级

当前基线（2026-10 升级后）：Spring Boot **3.5.16**、caffeine 3.3.0、jjwt 0.13.0、mysql-connector-j（随 Boot 管理）；@umijs/max **4.7.22**、antd **5.29.3**、pro-components **2.8.10**；Taro **3.6.40**。

以下大版本升级经评估后**暂缓**，破坏点如下，动之前先立项验证：

| 升级项 | 当前 → 目标 | 破坏点 |
| ---- | ---- | ---- |
| Spring Boot 4.x | 3.5.16 → 4.1.x | Spring Framework 7 基线;Knife4j 4.5 / springdoc 尚未跟进 Boot 4;安全与配置属性迁移 |
| React 19 + antd 6 | 18 / 5.x → 19 / 6.x | pro-components 2.x 面向 antd 5;Umi max 对 React 19 的支持需验证;全站组件回归 |
| Taro 4 | 3.6.40 → 4.x | 编译器与插件体系调整;NutUI 需配套升 3.x(其 latest 仍是 `-cpp` 预发布流) |
| mysql-connector-j 26.x | 随 Boot(9.x) → 26.x | MySQL 驱动切换到日历版本号体系,新大版本需观察稳定性 |

范围内的 minor / patch 升级用 `pnpm update`（前端）与直接改版本号（Maven）处理，升级后跑通上文构建与测试再提交。

## 文档站开发

```bash
cd docs
pnpm install
pnpm dev      # 本地预览
pnpm build    # 产物 docs/.vitepress/dist
```

写作约定：中文正文、接口与字段以源码为准（不编造）、功能状态如实标注「占位 / 未完成」；推送 `docs/**` 会自动触发部署工作流。

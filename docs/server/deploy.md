# 配置与部署

服务端的全部配置集中在 `server/src/main/resources/application.yml`，运行时环境变量可覆盖；部署形态有三种：Docker Compose 编排、裸机 systemd、以及开发调试编排。

## 配置项

### 环境变量（部署必读）

| 变量 | 说明 | 示例 |
| ---- | ---- | ---- |
| `DB_HOST` / `DB_PORT` | MySQL 地址与端口 | `db` / `3306` |
| `DB_NAME` | 库名 | `sinomed` |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 | `sinomed` |
| `DB_URL` / `DB_DRIVER` / `DB_DIALECT` | 整串覆盖数据源（演示环境换 SQLite 用） | `jdbc:sqlite:/app/data/sinomed.db?journal_mode=WAL&busy_timeout=5000` / `org.sqlite.JDBC` / `org.hibernate.community.dialect.SQLiteDialect` |
| `DEMO_SEED` | 演示种子数据开关（幂等，重复启动跳过） | `true` |
| `SERVER_PORT` | 服务端口 | `2347` |
| `JAVA_OPTS` | JVM 参数（Dockerfile 注入） | `-Xms256m -Xmx2048m` |

### application.yml 关键配置

| 配置键 | 值 | 说明 |
| ---- | ---- | ---- |
| `spring.jpa.hibernate.ddl-auto` | `update` | 自动建表 / 加列，无需手工 DDL |
| `spring.datasource.dbcp2.*` | 心跳检测 | 连接校验（`SELECT 1`） |
| `application.security.jwt.secret-key` | 内置默认值 | **生产必须替换**，Base64 HMAC 密钥 |
| `application.security.jwt.expiration` | `86400000` | access token 1 天 |
| `application.security.jwt.refresh-token.expiration` | `604800000` | refresh token 7 天 |
| `server.servlet.session.timeout` | `24h` | 会话超时 |
| `springdoc` / `knife4j` | 开启 | 接口文档 `/doc.html`，中文界面 |
| `logging.level.*` | debug/trace | Hibernate SQL 与 Security 日志，生产建议调低 |

### 端口约定

| 端口 | 用途 |
| ---- | ---- |
| 2347 | 服务端（对外由 Nginx 反代 `/api`） |
| 3306 | MySQL |
| 80 / 443 | Nginx（管理端静态站） |
| 5008 | 远程调试（仅 dev 编排） |

## 构建产物

```bash
cd server
./mvnw -B package -DskipTests   # 产物 target/sinomed-server.jar
```

Dockerfile 基于 `eclipse-temurin:21-jre-alpine`，把 jar 复制为 `/app/server.jar`，`JAVA_OPTS` 可注入 JVM 参数，日志目录 `/app/logs`。

## 演示编排（根目录 `compose.yml`，SQLite 一键起）

在线演示与本地体验用根目录 `compose.yml`：两个服务（server + web，web 的 nginx 把 `/api` 反代到 server）、SQLite 数据库文件落 `sinomed-data` 持久卷、`DEMO_SEED=true` 幂等播种虚构演示数据：

```bash
docker compose up -d
# 管理端 http://localhost:8080 （admin / 123）
# 接口文档 http://localhost:2347/doc.html
```

空卷首启自动建表并创建 `admin/123`；重复启动按账号/手机号/卡项名等自然键判重，不会产生重复数据。接回 MySQL 时替换 compose 里 `DB_*` 五个环境变量即可（构建层另有 `MVN_DIST_MIRROR` / `MAVEN_MIRROR_URL` 可选国内源参数，出境受限也能构建）。

## 方式一：Docker Compose（MySQL 整套）

`server/docker-compose.yml` 编排三个服务：MySQL（自动执行 `init/init.sql` 建库建账号）+ 服务端镜像 + Nginx（挂载 `web/dist` 静态站与 `init/nginx_http.conf`）：

```bash
cd server
docker compose up -d
```

## 方式二：开发调试编排

`server/dev.yml` 与上者的区别：服务端从本地 Dockerfile 构建、注入 `DB_*` 环境变量、开放 5008 远程调试端口、MySQL 数据目录与日志落在仓库相对路径：

```bash
cd server
docker compose -f dev.yml up -d
```

## 方式三：裸机 + systemd

1. 安装 MySQL 8，执行 `server/init/init.sql` 建库；
2. 安装 JDK 21，放置 `sinomed-server.jar`（如 `/data/sinomed/`）；
3. Nginx 配置静态站并把 `/api` 反代到 `127.0.0.1:2347`（`server/init/` 内含 http / https 示例配置，SPA 路由 fallback 到 `index.html`）；
4. 参照 `server/init/sinomed.service` 安装 systemd 服务并自启：

```bash
sudo cp server/init/sinomed.service /lib/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now sinomed
```

## 在线演示预案

- **CI 出镜像**：`Docker Images` 工作流（`.github/workflows/docker.yml`）在 push main 且 `server/`、`web/` 变更时构建并推送 ghcr.io，tag 为 `latest`（main 滚动）与 `sha-<短提交>`（可回溯、可回滚）；首次发布后在 GitHub Package 设置把 `sinomed-server` / `sinomed-web` 改为 **Public**，演示机才能匿名拉取。
- **演示机**：runner-Docker（内网 `192.168.5.5`），部署目录 `/data/deploy/sinomed`；放一份仓库的 `compose.yml`，`IMAGE_TAG=sha-<短提交>` 固定版本拉起，回滚即把 tag 改回上一短提交号后 `up -d`。
- **域名与隧道**：`*.cuihairu.site` 走 Cloudflare Tunnel（域名待定，先占位 `sinomed.cuihairu.site`），隧道指向演示机 web 服务的 `8080` 端口，无需公网 IP 与备案端口暴露。
- **数据重置**：演示数据全部为种子脚本虚构内容，`docker compose down -v && docker compose up -d` 即回到全新演示态。

## Nginx 转发关系

```text
浏览器 → Nginx(80/443)
  ├── /            → web/dist 静态资源（SPA fallback index.html）
  └── /api/**      → http://<server>:2347（应用接口 /api/v1/**）
```

## 日志

应用日志输出到 `logs/`（容器内 `/app/logs`）：`info.log`、`error.log` 按天与 100MB 滚动；另有登录、订单、签到等业务日志文件，级别由 `log4j2-spring.xml` 定义。

## 安全待加固清单（如实记录源码现状）

- JWT 签名密钥为源码内置默认值，生产必须更换；
- 首次启动自动创建 `admin / 123` 账号，上线前先改密；
- Compose 文件内含示例数据库口令，需替换；
- 接口层尚未启用方法级权限（权限字符串已定义未强制），任何登录用户可访问全部接口；
- Caffeine 缓存约 50 分钟过期且更新不失效，存在短窗口脏读。

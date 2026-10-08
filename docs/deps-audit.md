# Dependabot 依赖告警清理（分批升级轮）

对照基准：GitHub dependabot open alerts **188 条**（快照 2026-10-08：critical 29 / high 74 / medium 74 / low 11）。
分布：npm 187（`app/pnpm-lock.yaml` 131、`web/pnpm-lock.yaml` 52、`docs/pnpm-lock.yaml` 4）+ rust 1（`desktop/src-tauri/Cargo.lock` glib）。

口径约定：

- 告警全部落在 **lockfile 传递依赖**上（manifest 均为 lockfile，非直接依赖声明）。修复三档：
  1. **范围内升级**：`pnpm up --depth=Infinity --lockfile-only <pkg>@<fix>` 改 lockfile resolution，不动 node_modules；
  2. **overrides 钉版**：修复版超出父依赖 semver 范围、但属于 dev-only 或未使用路径时，package.json `pnpm.overrides` 强制（构建+测试全绿兜底）；
  3. **upstream-blocked 登记**：无补丁版本（NO-PATCH）、或修复版被上游依赖树钉死（如 Taro 3.6.x 钉 swiper 6）→ 如实登记不动。
- **每批全绿才提交推送下一批**：server `./mvnw test` + web/kiosk/tablet/app 各端构建 + desktop `cargo check`。
- 告警计数随升级自动关闭，最终以 dependabot 面板归零（或剩余=blocked 登记）为完成标准。

## 批次进度

- [x] 批1 critical（29 条）→ commit 见 git log：lockfile up vm2@3.12.2/proxy-addr@2.0.8/websocket-driver@0.7.5/form-data@2.5.6+4.0.6（app+web）、underscore→1.13.7（web 范围内）；overrides 补钉 form-data@2.3.3→2.5.6（app+web，request@2.88.2 精确钉死）+ immer@8→9.0.21（web，dva-immer ^8 范围外）；swiper critical 1 条 blocked（见登记表）。全端绿：server 4/4、web/kiosk/tablet/app build 过、cargo check 过
- [x] 批2 high（74 条）：app up source-map-js/fast-uri/brace-expansion×2/lodash-es/joi/js-yaml/browserslist/svgo/immutable/minimatch/qs/ws/glob + web up react-router/node-fetch/path-to-regexp×2/@babel/core；overrides 新增 app{serialize-javascript@6→7.0.5, tough-cookie@2→4.1.4, tmp→0.2.7, adm-zip@0.4.16→0.6.1, glob@10.2.6→10.5.0, compression@1.7.4→1.8.2, http-cache-semantics@3.8.1→4.1.1, minimatch@3.0.8/9.0.3→修复版, node-fetch@1→2.7.0} web{axios@0.27.2→0.34.0, underscore@1.7.0→1.13.8, debug@0.7.4→2.6.9, postcss@7→8.5.28, path-to-regexp@8.2.0→8.4.0}；app 的 postcss@7 override 实测破坏 Taro 老 autoprefixer（unprefixed undefined 构建崩）已撤销→blocked 登记。全端绿（app 重建 0，仅框架自带 2 warning）。audit 余量：app crit3/high8/med29/low0、web crit0/high4/med15/low1——剩余=blocked 清单（见下表）
- [ ] 批3 medium（74 条）
- [ ] 批4 low（11 条）
- [ ] 批5 rust（glib，desktop/src-tauri）
- [ ] 终验：dependabot open 计数复核，blocked 清单与面板剩余一致

## upstream-blocked 登记

| 包 | manifest | 告警 | blocked 原因 |
|---|---|---|---|
| swiper | app/pnpm-lock.yaml | critical 1 | 依赖树钉死：@tarojs/components 3.6.40 → swiper 6.8.0，修复版 12.1.2 为 major 跳版，升级需 Taro 框架升级（3.6.40 生态未适配 swiper 12） |
| decompress 4.2.1 | app/pnpm-lock.yaml | critical 2 | NO-PATCH（上游弃坑无修复版，@tarojs 工具链传递） |
| braces 3.0.3 | app+web/pnpm-lock.yaml | high 各1 | NO-PATCH（3.0.3 已是发布线最新，advisory 无补丁版本） |
| git-clone 0.1.0 | app/pnpm-lock.yaml | high 1 | NO-PATCH |
| html-minifier 4.0.0 | app/pnpm-lock.yaml | high 1 | NO-PATCH（上游弃坑，替代品 html-minifier-terser 需换父链） |
| node-forge 1.4.0 | app/pnpm-lock.yaml | high 1 | NO-PATCH（1.4.0 已最新，advisory 无补丁） |
| http-cache-semantics 4.1.1 | app/pnpm-lock.yaml | high 1 | NO-PATCH（4.1.1 已最新线，advisory 标无补丁） |
| webpack-dev-middleware 5.3.4 | app/pnpm-lock.yaml | high 1 | Taro webpack5-runner 钉 5.x，修复版 7.4.6 需跨两版 major |
| postcss 7.0.39 | app/pnpm-lock.yaml | high/medium | postcss 7 线无修复版；实测跨 major override→8.5.28 令 Taro 老 autoprefixer 构建崩（Cannot read 'unprefixed'），修复需 Taro 工具链整体升级 |
| mockjs 1.1.0 | web/pnpm-lock.yaml | high 1 | NO-PATCH（dev 模拟数据库，上游停更） |
| extract-zip 1.7.0 | web/pnpm-lock.yaml | high 2 | NO-PATCH（pro-cli 老工具链传递） |
| elliptic 6.6.1 | web/pnpm-lock.yaml | low 1 | NO-PATCH（6.6.1 已最新线） |
| request 2.88.2 | app+web/pnpm-lock.yaml | medium | NO-PATCH（上游弃坑；其漏洞主链 form-data 已 override 2.5.6 缓解） |
| sprintf-js | app+web/pnpm-lock.yaml | medium | NO-PATCH（1.1.2 修复未发布） |

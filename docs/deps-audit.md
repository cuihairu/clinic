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

- [ ] 批1 critical（29 条）：app vm2→3.12.x、proxy-addr→2.0.8、websocket-driver→0.7.5、form-data 2.3.3→2.5.6 & 4.0.5→4.0.6；web form-data 同、immer 8.0.4→9.0.21（overrides）、underscore 1.7.0→1.13.x（overrides）；swiper critical 1 条 blocked（Taro 钉死）
- [ ] 批2 high（74 条）
- [ ] 批3 medium（74 条）
- [ ] 批4 low（11 条）
- [ ] 批5 rust（glib，desktop/src-tauri）
- [ ] 终验：dependabot open 计数复核，blocked 清单与面板剩余一致

## upstream-blocked 登记

| 包 | manifest | 告警 | blocked 原因 |
|---|---|---|---|
| swiper | app/pnpm-lock.yaml | critical 1 | 依赖树钉死：@tarojs/components 3.6.40 → swiper 6.8.0，修复版 12.1.2 为 major 跳版，升级需 Taro 框架升级（3.6.40 生态未适配 swiper 12） |

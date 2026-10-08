# 界面原型 → 现状差距清单与对齐计划

对照基准：[docs/design/mockups](./mockups/)（tokens.css 为五端唯一样式源：默认「石墨×古金」主题取自 logo #666666/#C7A674，`data-theme="pine|cinnabar|indigo"` 三套可切换主色；宣纸底 `--paper`、金棕点缀 `--amber`、宋体标题 `--serif`）。

口径约定：

- **P0** = 高频主路径屏（顾客档案、接诊开单、全局壳主题），先行像素级对齐；**P1** = 其余已实现屏逐屏对齐；**仅原型** = 规划功能，不排实现。
- 差距按 **布局 / 配色 / 组件 / 文案** 四类归纳。
- **数据支撑** 列标明原型里的信息块有无真实接口：`✅` 直接可用；`⚠️ 需前端聚合`；`❌ 无接口`。`❌` 的块实现时一律渲染「规划功能」占位或暂不渲染，**不造假数据**（同 app.md 既有 warning 口径）。
- 每屏完成后独立提交（`feat(web): 顾客档案对齐原型` 式标题），本地起 dev server 截图与 `docs/public/screenshots/` 原型图对照后再提交。

## tokens 同源方案

| 端 | 现状 | 做法 |
|---|---|---|
| web 管理后台 | antd 默认蓝 `#1890ff`（config/defaultSettings.ts:12），系统无衬线 | 拷贝 tokens.css → `web/src/tokens.css` 并在 global.less 引入；defaultSettings `colorPrimary` 与 ProLayout token 对齐 `--brand`；标题/数字类切 `--serif` |
| desktop 壳 | 引导页 `desktop/ui/index.html` 自带样式 | 同步 tokens 变量与宋体标题 |
| app 小程序 | 单页 index.css，已有散变量 `--brand-color #c7a674` | 引 tokens.css，删除散变量改引用 |
| kiosk 自助机 | `#2f6d4f` 墨绿 / `#f6f5f1` 底 | 数值恰等于 pine 主题 → 整页挂 `data-theme="pine"` 引 tokens.css，替换散落 hex |
| tablet 展示屏 | 深色 `#101613` + `#ffce54` | 引 tokens.css 沿用石墨基调 + `--amber` 点缀，替换散落 hex |

## 一、管理后台 web（逐屏）

| # | 屏（原型） | 现状 | 主要差距 | 数据支撑 | 级别 |
|---|---|---|---|---|---|
| 1 | 全局壳 + 主题（appearance 原型口径） | ProLayout `mix` 布局、antd 蓝、白底 | **配色**：主色/选中态/链接全换 `--brand` 系；**布局**：`mix`→纯侧栏（`layout: 'side'`），侧栏白底圆角选中块；**组件**：logo 位换「养」字方标+宋体馆名；**文案**：`Youngs.fun`→馆名副标题 | ✅ | **P0** |
| 2 | 顾客档案（admin-customer） | `Customer/Update` 裸 ProForm 7 字段纵排 | **布局**：整页重排 = 头卡(头像/姓名/等级chip/元信息+操作) + 4 统计卡 + 左「诊疗记录」时间线 + 右「持卡卡项」栏；**组件**：宋体姓名/衬线数字、等级金棕 chip、时间线日期块、余次进度条、回访提醒注记块；**文案**：「年纪」→「年龄」等 | 姓名/性别/年龄/手机/等级/生日 ✅；诊疗记录 ✅ `queryTreatByPage({customerId})`；累计消费 ⚠️ 订单聚合无现成接口→暂缓；持卡卡项/余次 ❌ 无卡实体→「规划」占位；下次回访 ❌ 无按顾客查询→「规划」占位 | **P0** |
| 3 | 接诊开单 = 桌面工作站（desktop-workstation） | `Treat/Create` 单列长 ProForm（四诊全宽 textarea、五行 ProCard 嵌套） | **布局**：患者头卡 + 四诊 2×2 + 脉象左右手寸关尺格 + 取穴左右 + 诊断(证型+疗程) + 五行图示 + 调理方案条目 + 右栏（历史调阅/打印处方笺入口）；**配色/组件**：panel 卡片化、标签 chip、宋体节标题；**文案**：标签对齐原型（主诉/问诊/望诊/切诊/脉象/取穴/诊断/调理方案） | 全部 Treat 字段 ✅；顾客头卡 ✅ `fetchCustomerById`；历史调阅 ✅ `queryTreatByPage`；打印处方笺 ⚠️ 有 print 模板链路（D7）可挂；开单区/卡项抵扣 ❌ 无订单创建接口于接诊流→「规划」占位；扫码进单 ✅ 既有 ScannerInput/scan 流程 | **P0** |
| 4 | 每日报表（admin-dash-day） | `Dashboard/Day` 三张 ProCard 表格 | **布局**：顶部 4 统计卡 + 左员工总结表 + 右近 7 日柱状 + 今日回访/今日总结；**配色/组件**：状态 pill（已提交/未填写）、提醒按钮、宋体数字；**文案**：标题带日期 + ‹前一天› 导航 | 员工总结/今日回访/今日总结 ✅ 三接口已在用；在岗/接诊/新客统计 ❌ 无聚合接口→暂缓或「规划」；近 7 日 ⚠️ 按 treat 按日查询聚合（接口参数支持 startTime/endTime） | P1 |
| 5 | 卡项管理（admin-items） | `Item/Query` ProTable（名称/价格/描述/时间） | **布局**：提示条 + 封面列 + 上架开关列 + 排序列 + 分页；**组件**：首字封面 chip；**文案**：tip 同原型 | 名称/价格/描述 ✅；封面/上架/排序 ✅ 实体已有（ItemEntity cover/enabled/sort，ItemView 落库默认 enabled=1/sort=0；仅 web typings.d.ts 未同步）→ P1 可真实做开关/排序/封面，无需占位 | P1 |
| 6 | 订单管理（admin-orders） | `Order/Query` ProTable，状态 Tag 蓝/橙/绿/灰 | **布局**：tab 状态过滤（现有 valueType select→改 tab 或保留）；**配色**：Tag 换主题色 pill；**文案**：「已下单」→「待接待」对齐原型与自助机口径 | 单号/顾客/项目/金额/状态/接单-完成-取消 ✅ 全真实 | P1 |
| 7 | 广告屏（admin-ads） | `Ads/Materials|Schedules|Screens|Calls` 四个 ProTable 页 | **布局**：原型为单页 4 tab + 右栏屏幕状态/快捷叫号；逐页对齐卡片化与 pill；**文案**：脱敏 `王*` 已在展示屏口径 | 素材/排期/屏幕/叫号四组接口 ✅ | P1 |
| 8 | 复盘回访 / 员工 / 登录 | 无专属原型 | 仅随全局壳主题（#1）被动对齐，不单独排屏 | ✅ | P1（被动） |
| 9 | 外观设置（admin-appearance） | 无此页 | 新建「系统/外观设置」页：4 主题卡选择 → 写 localStorage → `data-theme` + antd token 运行时切换 + 实时预览区 | 纯前端 ✅ | P1 |
| 10 | 预约排班 / 中药处方 / 收费结算 | 无 | **仅原型**（已带「规划功能·设计原型」标），不排实现；预约/处方/收费域无表无接口 | ❌ | 仅原型 |

## 二、桌面工作站（desktop 壳）

| # | 屏 | 现状 | 主要差距 | 级别 |
|---|---|---|---|---|
| 1 | 接诊开单主屏 | 加载 web `/treat/create` | 即 web 屏 3，随 P0 落地；`ScannerInput` 扫码定位条随页面对齐 | **P0**（同 web） |
| 2 | 壳引导页 `ui/index.html` | 自带浅色样式、外设自检按钮 | 配色/宋体标题对齐 tokens；外设状态（小票机/扫码枪）展示样式统一 | P1 |

## 三、小程序 app

| # | 屏 | 现状 | 主要差距 | 级别 |
|---|---|---|---|---|
| 1 | 首页（mobile-home） | 单页 45 行极简首页（docs/app.md 已标「界面原型，尚未实现」） | 布局重排：预约横幅/服务宫格/卡项余次卡片区按原型结构搭；**预约横幅、卡项余次 ❌ 无接口 → 明确标「规划」**；配色换 tokens（现有散 `--brand-color #c7a674` 收编） | P1 |

## 四、自助机 kiosk

| # | 屏 | 现状 | 主要差距 | 级别 |
|---|---|---|---|---|
| 1 | 选服务（kiosk-menu） | menu 页已有服务列表+购物篮结构 | 配色已是墨绿（=pine）→ 换 tokens 变量收编散 hex；宋体标题、宣纸底、金棕价签/点缀；购物篮/结算按钮细节对齐原型 | P1 |
| 2 | 下单回执（order） | 已有 | 同上随全局对齐 | P1 |

## 五、展示屏 tablet

| # | 屏 | 现状 | 主要差距 | 级别 |
|---|---|---|---|---|
| 1 | 轮播+叫号（tablet-screen） | player 轮播 + call 叫号条已实现 | 深色底换 tokens 石墨基调、`h1 em` 金棕、宋体标题、叫号条 `王*` 脱敏（已实现，核对）；页码/进度细节 | P1 |

## 执行顺序

1. **P0** web 全局壳主题（tokens.css 落 web + colorPrimary/ProLayout 对齐）→ 2. **P0** 顾客档案 → 3. **P0** 接诊开单（含桌面工作站主屏）→ 4. P1 订单 → 5. P1 卡项 → 6. P1 每日报表 → 7. P1 广告屏 → 8. P1 外观设置（主题切换落地）→ 9. P1 kiosk → 10. P1 tablet → 11. P1 app → 12. P1 desktop 引导页。

每屏：改实现 → `pnpm build`/lint 过 → 本地截图对照原型 PNG → 独立 commit（标题 `feat(web): 顾客档案对齐原型` 式，正文列布局/配色/组件/文案四类改动与实证）→ fetch+rebase → push。

## 进度

- [x] P0-1 web 全局壳主题对齐（bd72bae）
- [x] P0-2 顾客档案对齐（本批提交）
- [x] P0-3 接诊开单（桌面工作站主屏）对齐（本批提交，desktop 壳随页面同享）
- [x] P1-4 订单管理对齐（本批提交）
- [ ] P1-5 卡项管理对齐
- [ ] P1-6 每日报表对齐
- [ ] P1-7 广告屏四页对齐
- [ ] P1-8 外观设置（运行时主题切换）
- [ ] P1-9 kiosk 自助机对齐
- [ ] P1-10 tablet 展示屏对齐
- [ ] P1-11 app 小程序首页对齐（未实现块如实标注）
- [ ] P1-12 desktop 壳引导页对齐

# 界面原型 → 现状差距清单与对齐计划

对照基准：[docs/design/mockups](./mockups/README.md)（tokens.css 为五端唯一样式源：默认「石墨×古金」主题取自 logo #666666/#C7A674，`data-theme="pine|cinnabar|indigo"` 三套可切换主色；宣纸底 `--paper`、金棕点缀 `--amber`、宋体标题 `--serif`）。

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
| 2 | 顾客档案（admin-customer） | `Customer/Update` 裸 ProForm 7 字段纵排 | **布局**：整页重排 = 头卡(头像/姓名/等级chip/元信息+操作) + 4 统计卡 + 左「诊疗记录」时间线 + 右「持卡卡项」栏；**组件**：宋体姓名/衬线数字、等级金棕 chip、时间线日期块、余次进度条、回访提醒注记块；**文案**：「年纪」→「年龄」等 | 姓名/性别/年龄/手机/等级/生日 ✅；诊疗记录 ✅ `queryTreatByPage({customerId})`；累计消费 ✅ `GET /api/v1/order/summary`（fetchOrderSummary，已完成订单价格合计）；持卡卡项/余次 ✅ 已实装（`customer_cards` + `/api/v1/card`，顾客页渲染余次进度条）；下次回访 ❌ 无按顾客查询→「规划」占位 | **P0** |
| 3 | 接诊开单 = 桌面工作站（desktop-workstation） | `Treat/Create` 单列长 ProForm（四诊全宽 textarea、五行 ProCard 嵌套） | **布局**：患者头卡 + 四诊 2×2 + 脉象左右手寸关尺格 + 取穴左右 + 诊断(证型+疗程) + 五行图示 + 调理方案条目 + 右栏（历史调阅/打印处方笺入口）；**配色/组件**：panel 卡片化、标签 chip、宋体节标题；**文案**：标签对齐原型（主诉/问诊/望诊/切诊/脉象/取穴/诊断/调理方案） | 全部 Treat 字段 ✅；顾客头卡 ✅ `fetchCustomerById`；历史调阅 ✅ `queryTreatByPage`；打印处方笺 ⚠️ 有 print 模板链路（D7）可挂；开单区/卡项抵扣 ❌ 无订单创建接口于接诊流→「规划」占位；扫码进单 ✅ 既有 ScannerInput/scan 流程 | **P0** |
| 4 | 每日报表（admin-dash-day） | `Dashboard/Day` 三张 ProCard 表格 | **布局**：顶部 4 统计卡 + 左员工总结表 + 右近 7 日柱状 + 今日回访/今日总结；**配色/组件**：状态 pill（已提交/未填写）、提醒按钮、宋体数字；**文案**：标题带日期 + ‹前一天› 导航 | 员工总结/今日回访/今日总结 ✅ 三接口已在用；在岗/接诊/新客统计 ❌ 无聚合接口→暂缓或「规划」；近 7 日 ⚠️ 按 treat 按日查询聚合（接口参数支持 startTime/endTime） | P1 |
| 5 | 卡项管理（admin-items） | `Item/Query` ProTable（名称/价格/描述/时间） | **布局**：提示条 + 封面列 + 上架开关列 + 排序列 + 分页；**组件**：首字封面 chip；**文案**：tip 同原型 | 名称/价格/描述 ✅；封面/上架/排序 ✅ 实体已有（ItemEntity cover/enabled/sort，ItemView 落库默认 enabled=1/sort=0；仅 web typings.d.ts 未同步）→ P1 可真实做开关/排序/封面，无需占位 | P1 |
| 6 | 订单管理（admin-orders） | `Order/Query` ProTable，状态 Tag 蓝/橙/绿/灰 | **布局**：tab 状态过滤（现有 valueType select→改 tab 或保留）；**配色**：Tag 换主题色 pill；**文案**：「已下单」→「待接待」对齐原型与自助机口径 | 单号/顾客/项目/金额/状态/接单-完成-取消 ✅ 全真实 | P1 |
| 7 | 广告屏（admin-ads） | `Ads/Materials|Schedules|Screens|Calls` 四个 ProTable 页 | **布局**：原型为单页 4 tab + 右栏屏幕状态/快捷叫号；逐页对齐卡片化与 pill；**文案**：脱敏 `王*` 已在展示屏口径 | 素材/排期/屏幕/叫号四组接口 ✅ | P1 |
| 8 | 复盘回访 / 员工 / 登录 | 无专属原型 | 仅随全局壳主题（#1）被动对齐，不单独排屏 | ✅ | P1（被动） |
| 9 | 外观设置（admin-appearance） | 无此页 | 新建「系统/外观设置」页：4 主题卡选择 → 写 localStorage → `data-theme` + antd token 运行时切换 + 实时预览区 | 纯前端 ✅ | P1 |
| 10 | 预约排班 / 中药处方 / 收费结算 | 无→预约/结算已建域 | 预约排班已实装（5910aee/f04cf62：`appointments` 表 + `/api/v1/appointment` + `/appointment/*` 列表工作台 + `/appointment/schedule` 医师×时段排班网格）；收费结算已实装（26d6419/9d1e018：`settlements` 表 + 储值流水扣减 + `/api/v1/settlement`、`/api/v1/recharge` + `/billing/settle` 结算台；次卡抵扣 81aaf5e、小票打印 f57f10d 已实装，处方饮片行仍为设计稿）；中药处方已实装（f91add1/0ee1d1d：`prescriptions`/`prescription_items` 表 + `/api/v1/prescription` + `/prescription/*` 开方与查询，药材自由文本；配伍审方 7100355、计价 60e818c、代煎领取 141f295 已实装） | 预约 ✅ / 处方 ✅ / 收费 ✅ | 三者均已实现；处方库存（饮片出入库）为规划 |

## 二、桌面工作站（desktop 壳）

| # | 屏 | 现状 | 主要差距 | 级别 |
|---|---|---|---|---|
| 1 | 接诊开单主屏 | 加载 web `/treat/create` | 即 web 屏 3，随 P0 落地；`ScannerInput` 扫码定位条随页面对齐 | **P0**（同 web） |
| 2 | 壳引导页 `ui/index.html` | 自带浅色样式、外设自检按钮 | 配色/宋体标题对齐 tokens；外设状态（小票机/扫码枪）展示样式统一 | P1 |

## 三、小程序 app

| # | 屏 | 现状 | 主要差距 | 级别 |
|---|---|---|---|---|
| 1 | 首页（mobile-home） | 已按原型实装：品牌头 / 预约横幅（入口 → `pages/booking` 自助约期）/ 今日宜养宫格（实拉 `GET /api/v1/kiosk/items`）/ 我的卡项 / 养生贴士 / 白标自检页底；**卡项余次 ❌ 无接口 → 标「规划」占位**；配色已收编 tokens（`src/tokens.css` 与 mockups 同源） | 自助约期已实装（`POST /api/v1/kiosk/appointments`，口径见 [/design/app-booking](/design/app-booking)）；登录与卡项余次页未做 | P1 |

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
- [x] P0-2 顾客档案对齐（fc84cfa）
- [x] P0-3 接诊开单（桌面工作站主屏）对齐（42e567b，desktop 壳随页面同享）
- [x] P1-4 订单管理对齐（f42e0ee）
- [x] P1-5 卡项管理对齐（8d4b77f）
- [x] P1-6 每日报表对齐（1c72d5b，随批 fix(server)：分页日期过滤改 Date 比较 b8dc2a8）
- [x] P1-7 广告屏四页对齐（bcb6b1c，四页合并单页 4 tab + 右栏，旧路由 redirect）
- [x] P1-8 外观设置（8591447；运行时主题切换：data-theme CSS 变量 + useAntdConfigSetter 注入插件 ConfigProvider + settings.colorPrimary/侧栏 token 三层下发）
- [x] P1-9 kiosk 自助机对齐（eccf091；tokens.css 同源拷入 + index.html 挂 data-theme="pine"；hero/三步胶囊/渐变首字卡/两行购物篮对齐 kiosk-menu 原型，散 hex 全收编变量）
- [x] P1-10 tablet 展示屏对齐（2caa81f；tokens.css 同源拷入；石墨底 --ink、纸色圆角 slide 卡 + 真实素材内嵌、pager 圆点真实进度、叫号全屏覆盖改原型底部横条=金棕 label+号码脱敏+宋体诊室，15s 自动收回）
- [x] P1-11 app 小程序首页对齐（9566fdb；mobile-home 版式：品牌头/预约横幅/今日宜养/我的卡项/养生贴士/白标自检页底；宫格实拉上架项目，预约与卡项余次如实「规划功能」pill+note；tokens.css 同源引入，--brand-color 白标注入保留）
- [x] P1-12 desktop 壳引导页对齐（5e656b7；五端散色残留 grep 复核零命中，--brand-color 白标注入机制保留）
- [x] P1-13 处方配伍审方 + 药材字典/计价（8e3402f/0144ba0；十八反/十九畏静态规则提示不拦截；herbs 字典 + 处方实时试算，未收录药名不计费）
- [x] P1-14 卡实体（次卡）与结算抵扣（81aaf5e；customer_cards + /api/v1/card 发卡/停用，payType 5 扣 1 次实收 0；顾客页持卡栏与结算台次卡选项实装）
- [x] P1-15 结算小票打印（f57f10d/1b33703；`GET /api/v1/print/receipt/{settlementId}` 服务端套打 80mm HTML：单号 S+结算id、次卡抵扣金额列显示「次卡抵扣」、名字 HTML 转义；结算台收款成功出打印入口、最近结算可补打；模板仍可 data/printtemplates 覆盖）
- [x] P1-16 处方代煎领取（141f295/47e4828；prescriptions +decoction_status/decoction_bags，开方 decoction=true 袋数=剂数落「待煎」；`PUT /prescription/{id}/decoction?status=` 待煎→可取→已取单向流转，回退/跳跃/未选代煎 400；服务费 ¥3/袋 实时算仅提示；开方页勾选、查询页状态列+抽屉流转按钮；加急与取药窗口不做——演示口径）
- [ ] P1-17 处方饮片行进结算（**卡点，待域建模拍板**：设计稿把「中药饮片 8 味 × 7 剂 ¥280」与推拿并列进同一张结算单——落现域有两条路：①处方按单建「饮片订单」复用一单一结算，但 orders.price 为整数元而处方计价是分（totalFen），转元必舍入、违背「钱的事要确定」；②结算单改多行（settlement_items）动 orders/settlements 单行口径与 kiosk/tablet 全链。两者均为域级改动，不属增量可自行拍板的范围，待产品定夺后另开批实施）

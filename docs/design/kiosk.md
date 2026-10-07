# 顾客选服务设计：到店 Kiosk（浏览 / 选卡项 / 下单）

> 状态：**设计稿，未实施**。本文描述规划中的 `kiosk/` 端与配套服务端接口；与候诊区展示屏（[平板展示](/design/tablet)）是两台设备两个工程——展示屏只播不录，本机是顾客自助录入入口。

## 定位与场景

`kiosk/` 是摆在**前台/大堂的顾客自助 iPad 网页**：到店顾客在大屏上浏览服务项目，自己勾选想做的卡项，留手机号提交，前台在管理端接单。三步：

1. **浏览**：大字网格展示启用中的卡项（封面 + 名称 + 价格），触摸友好；
2. **选服务**：点选加入购物篮，可多选、可增删；
3. **下单**：填手机号（可填称呼）提交，屏幕回执单号与合计；前台在管理端「订单管理」看到新单并接待。

不做支付（无任何收款代码，下单即到店待接待）；不做会员价/优惠计算（订单金额按下单时刻卡项价格快照）。

```text
顾客（kiosk/ 大屏点选） ──► server kiosk 接口（浏览只读 + 下单）
                                │
                  前台（web dash 订单管理）◄── 新单状态流转（接单 → 完成 / 取消）
```

## 数据模型（复用既有表，新增字段）

### items — 卡项补三个字段

现有卡项表只有 `name / price / description`，Kiosk 浏览需要展示与上架控制，补：

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| enabled | Integer | 1 上架、0 下架（Kiosk 只展示上架项；存量行默认 1） |
| cover | String | 封面图相对 url（可空；复用现有媒体上传与 `/media` 托管） |
| sort | Integer | 展示顺序，小者在前 |

`ddl-auto: update` 自动加列，不影响存量数据。管理端卡项页（已有）补这三个字段的编辑。

### orders — 状态语义定档

表已存在（`user_id / item_id / staff_id / status / price`），此前接口全是空壳。现在把状态定档并写死：

| status | 含义 |
| ------ | ---- |
| 0 | 已下单（Kiosk 提交后的初始态） |
| 1 | 已确认（前台接待中） |
| 2 | 已完成 |
| 9 | 已取消 |

- `user_id` 存顾客 id（`customers` 表，业务外键）；`item_id` 存卡项 id；`staff_id` 接单时可填（Kiosk 单初始为空）；
- `price` 是**下单时刻的卡项价格快照**（Integer，分为单位与卡项表一致——卡项 price 当前即按元存整数，如实沿用）；
- 一个卡项一条订单（多选 = 多条订单，同一次提交共享同一顾客），不加购物车表。

### 顾客关联

`customers.phone` 唯一。Kiosk 下单按手机号查顾客，查无则建档：`name` 取称呼输入（不填默认「到店客人」），`phone` 必填且要求 11 位手机号格式。

## API 面（服务端新增 `/api/v1/kiosk`，免登录）

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| GET | `/api/v1/kiosk/items` | Kiosk 浏览：`enabled=1` 的卡项按 `sort,name` 升序，返回 `id / name / price / cover / description`（不含审计字段） |
| POST | `/api/v1/kiosk/orders` | Kiosk 下单：`{ phone, name?, items: [{ itemId }] }`；校验手机号格式与卡项存在且上架；按手机号找/建顾客；逐项写订单（status=0、price=卡项现价）；返回 `{ customerId, orders: [{ id, itemId, itemName, price }], totalFee }` |

免登录边界（如实）：Kiosk 是内网设备，浏览接口纯只读；下单接口是唯一写入口，写范围限定「一条顾客档案（按手机号幂等）+ N 条 status=0 订单」，不暴露任何查询/删除能力。如暴露公网需另加验证码或屏 token。

管理端订单接口：复用 `OrderService`（save/findById 已有），新增列表查询与状态流转，走**需登录**的 `/api/v1/order` 组：

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| GET | `/api/v1/order/page` | 分页（current、pageSize、status 可选），联出顾客姓名与卡项名 |
| PUT | `/api/v1/order/status` | `{ id, status }`，只允许 0→1→2 正向流转与 0/1→9 取消 |
| DELETE | `/api/v1/order/{id}` | 沿用既有删除（仅 9 已取消可删） |

现有 `POST/PUT /api/v1/order/`、`GET /api/v1/order/{id}` 等空壳接口一并替换为真实实现（OrderView 字段与实体对齐：customerId/itemId/staffId/status/price，去掉实体里不存在的 extOrderId/payType/payment）。

## 交互链路

```text
Kiosk 首页（卡项网格，30s 无操作自动清篮回首页）
  → 点选卡项 → 底部购物篮（数量合并、可删）
  → 「去下单」→ 手机号 + 称呼（可选）→ 提交
  → 成功页（单号列表 + 合计金额）→ 10s 自动回首页

前台 dash「订单管理」：新单列表（状态角标）
  → 接单（0→1）→ 服务完成（1→2）；未接待可取消（→9）
```

## 目录落位

```text
kiosk/                      # monorepo 新增目录（与 tablet/ 平级，同一套 Vite + React + TS 约定）
├── src/
│   ├── net/api.ts          # items 拉取与下单提交
│   ├── menu/               # 卡项网格与购物篮
│   └── App.tsx             # 首页 / 下单 / 成功三态切换、空闲超时回首页
├── index.html              # 触摸大字布局基准（768–1080 宽竖屏为主）
├── package.json            # name: sinomed-kiosk（Node.js 24 / pnpm）
└── .nvmrc

web/src/pages/Order/        # dash：订单管理（替换现存的顾客列表复制品页面）
server/ …/controller/KioskController.java、OrderController.java（真实实现）
```

部署落位：Nginx 增加 `/kiosk/`（`kiosk/dist`）；卡项封面沿用 `/media/` 托管，无需新增静态段。

## todo 原子项

| # | 事项 | 验收 |
| ---- | ---- | ---- |
| K1 | items 补 `enabled / cover / sort` 三字段并透出到 VO；dash 卡项页补编辑 | dash 可上架/下架、传封面、排序 |
| K2 | server KioskController：`GET /kiosk/items` + `POST /kiosk/orders`（免登录、手机号建档幂等） | curl 浏览只读、下单落库、重复手机号不重复建档 |
| K3 | dash 订单管理页：分页列表（联顾客/卡项名）+ 状态流转，替换空壳 OrderController | kiosk 下的单在 dash 可见、可接单/完成/取消 |
| K4 | `kiosk/` 脚手架 + 卡项网格浏览页 | 浏览器打开即见上架卡项大字网格 |
| K5 | kiosk 购物篮 + 下单 + 成功回执 + 空闲回首页 | 全流程触摸走通，重复手机号二单归同一顾客 |
| K6 | 端到端验收 + 文档同步（data-model / api / deploy / index） | 设计稿改已实施，docs build 过 |

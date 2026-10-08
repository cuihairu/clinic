# REST 接口清单

所有业务接口统一前缀 `/api/v1`，下表路径均以完整路径列出。管理端开发代理把 `/api` 转发到服务端（默认 `127.0.0.1:2347`），生产环境由 Nginx 把 `/api` 转发到服务端。

## 鉴权约定

- **放行路径**：
  - 登录与文档：`/api/v1/user/login`、`/doc.html`、`/swagger-ui/**`、`/v3/api-docs/**`、`/swagger-resources/**`、`/webjars/**`
  - **业务免登录端点（内网设备约定，见对应设计文档）**：
    - `/api/v1/kiosk/**` — 顾客自助机浏览与下单（只读 + 单一写入口，见 [kiosk.md](/design/kiosk)）
    - `/api/v1/ads/playlist` — 平板拉取排期（只读 + 屏 code 校验，见 [tablet.md](/design/tablet)）
    - `/api/v1/calls/latest` — 平板轮询叫号（只读 + 屏 code 校验，见 [tablet.md](/design/tablet)）
    - `/api/v1/print/templates` — 打印模板下发（空白版式无业务数据，见 [desktop.md](/design/desktop) D7）
    - `/media/**` — 广告媒体静态资源（Nginx 托管）
  - `/actuator/**` — 运维监控
- 其余接口一律需要 `Authorization: Bearer <JWT>`，未认证返回 401。
- 注意 `POST /api/v1/user/register`（注册）不在放行列表内——首次使用需先有账号。
- 登录响应头 `Authorization` 返回 access token、`Refresh` 返回 refresh token（均自带 `Bearer ` 前缀）；access 有效期 1 天，refresh 7 天。刷新接口尚未实现，续期需重新登录。

## 用户认证 `/api/v1/user`

| 方法 | 路径 | 功能 | 鉴权 |
| ---- | ---- | ---- | ---- |
| POST | `/api/v1/user/login` | 账号密码登录，返回 token 与权限标识（`admin` / `user`） | 公开 |
| POST | `/api/v1/user/register` | 注册员工账号（用户名 / 密码 2-20 位），直接返回 JWT | 需登录 |
| POST | `/api/v1/user/logout` | 登出（当前仅客户端丢弃 token） | 需登录 |
| GET | `/api/v1/user/current` | 获取当前登录员工信息 | 需登录 |

## 顾客 `/api/v1/customer`

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| GET | `/api/v1/customer/page` | 分页查询（`current` / `pageSize` 必填，可选 `name` / `age` / `phone` / `startTime` / `endTime`） |
| GET | `/api/v1/customer/{id}` | 按 id 查询 |
| GET | `/api/v1/customer/phone/{phone}` | 按手机号查询 |
| GET | `/api/v1/customer/name/{name}` | 按姓名查询（可多条） |
| POST | `/api/v1/customer/` | 建档（性别必填；手机号可选、唯一、大陆号段格式） |
| PUT | `/api/v1/customer/` | 更新顾客信息 |
| DELETE | `/api/v1/customer/{id}` | 删除顾客 |

## 诊疗单 `/api/v1/treat`

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST | `/api/v1/treat/` | 创建诊疗单（`customerId` 必须指向存在的顾客） |
| GET | `/api/v1/treat/{id}` | 按 id 查询 |
| GET | `/api/v1/treat/customer/{id}` | 按顾客 id 查询全部诊疗单 |
| GET | `/api/v1/treat/customer/name/{name}` | 按顾客姓名查询（姓名需唯一） |
| GET | `/api/v1/treat/history` | 分页查询某顾客的诊疗历史（`customerId` 必填，按创建时间倒序） |
| GET | `/api/v1/treat/fuzzy` | 模糊检索（传 `id` 单查；否则按 `name` / `age` / `phone` 圈定顾客后分页） |
| PUT | `/api/v1/treat/` | 更新（占位实现，实际使用请以创建新单 + 删除旧单的流程为准） |
| DELETE | `/api/v1/treat/{id}` | 删除诊疗单 |

字段含义见[数据模型 · treats 表](/server/data-model#treats-诊疗单)。

## 反馈与复盘 `/api/v1/review`

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST | `/api/v1/review/` | 保存每日总结（按当天 0 点 upsert，同日覆盖） |
| GET | `/api/v1/review/day?day=` | 按日期查询每日总结（不存在返回空模板） |
| POST | `/api/v1/review/staff` | 新建员工日复盘（只传 `name` 可自动回填员工 id） |
| POST | `/api/v1/review/customer` | 新建顾客回访记录（只传 `name` 可自动回填顾客 id，`day` 为回访日） |
| GET | `/api/v1/review/staff/list/day?day=` | 按日查询全部员工复盘 |
| GET | `/api/v1/review/customer/list/day?day=` | 按日查询全部顾客回访 |
| POST | `/api/v1/review/staff/bulk` | 批量保存员工复盘（逐条写入） |
| POST | `/api/v1/review/customer/bulk` | 批量保存顾客回访 |
| DELETE | `/api/v1/review/staff/{id}` | 删除员工复盘 |
| DELETE | `/api/v1/review/customer/{id}` | 删除顾客回访 |
| GET | `/api/v1/review/{id}` | 按 id 查询（占位） |
| DELETE | `/api/v1/review/{id}` | 删除每日总结（占位，删除请用 id 查询后再操作） |

## 卡项 `/api/v1/item`

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST | `/api/v1/item/` | 创建卡项（名称唯一、价格、描述） |
| PUT | `/api/v1/item/` | 更新卡项 |
| GET | `/api/v1/item/page` | 分页查询（当前实现未按条件过滤，返回全部） |
| GET | `/api/v1/item/{id}` | 按 id 查询 |
| GET | `/api/v1/item/name/{name}` | 按名称查询（值取自查询参数） |
| DELETE | `/api/v1/item/{id}` | 删除，返回删除前的卡项 |

## 员工与考勤（诊所运营） `/api/v1/staff`

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST | `/api/v1/staff/` | 创建员工（账号唯一、密码 2-20 位、手机号查重） |
| PUT | `/api/v1/staff/` | 更新员工（携带 `password` 则同步改密） |
| GET | `/api/v1/staff/page` | 分页查询员工 |
| GET | `/api/v1/staff/{id}` | 按 id 查询 |
| GET | `/api/v1/staff/name/{name}` | 按姓名查询（值取自查询参数） |
| GET | `/api/v1/staff/phone/{phone}` | 按手机号查询（当前实现按姓名匹配，待修） |
| DELETE | `/api/v1/staff/{id}` | 删除员工 |
| POST | `/api/v1/staff/sign` | 签到 / 签退（`signType`：1 上班、0 下班） |
| GET | `/api/v1/staff/timesheet/today` | 本人今日考勤（打卡明细与总时长） |
| GET | `/api/v1/staff/timesheet/month?month=` | 按月全员考勤统计（1-12，仅当年） |
| POST | `/api/v1/staff/leave` | 请假（占位实现，返回空对象） |

::: tip 考勤口径
时长按整小时统计，不足 1 小时不显示；同日多次上班卡取最早、下班卡取最晚；未打下班卡但已上班的按当前时间计算。详见[管理端](/web)页面说明。
:::

## 顾客选服务 Kiosk `/api/v1/kiosk`（kiosk.md K2）

免登录（前台大屏自助入口，见[顾客选服务设计](/design/kiosk)）；浏览只读 + 单一下单写入口。

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| GET | `/api/v1/kiosk/items` | 上架卡项列表（enabled=1，sort+name 升序），只透 id/name/price/cover/description |
| POST | `/api/v1/kiosk/orders` | 下单：body `{ phone, name?, itemIds[] }`。手机号须 `1\d{10}`；卡项须存在且上架（先全量校验再建档）；按手机号幂等建档（缺称呼默认「到店客人」），逐项落 `status=0` 订单并取卡项现价快照。返回 `{ customerId, customerName, orders[{id,itemId,itemName,price}], totalFee }` |

## 打印模板 `/api/v1/print`（desktop.md D7）

免登录（空白版式、不含业务数据；桌面工作站壳未登录也要拉模板，见[桌面版设计](/design/desktop)）。

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| GET | `/api/v1/print/templates` | 全部白名单模板（prescription / receipt）与内容摘要版本 `{ version, templates[{ name, content }] }`；版本为内容 SHA-256 前 16 位，内容不变则不变。模板解析：`data/printtemplates/{name}.html`（`PRINT_TEMPLATES_DIR` 可配）覆盖目录优先，缺失回落 jar 内置版式——模板更新只需改服务端文件，不发壳版本 |

## 订单 `/api/v1/order`（kiosk.md K3，需登录）

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| GET | `/api/v1/order/page` | 分页（current、pageSize、status 可空），联出顾客名/手机号与卡项名 |
| GET | `/api/v1/order/{id}` | 详情（联顾客与卡项名） |
| PUT | `/api/v1/order/status` | 状态流转：`0→1`、`1→2`，`0/1→9` 取消；其余组合报错（body 带 id 与 status） |
| DELETE | `/api/v1/order/{id}` | 删除，仅 `status=9`（已取消）可删 |

## 广告投屏 `/api/v1/ads`（tablet.md T1/T2）

管理接口需登录；下发与媒体静态资源免登录（内网屏设备约定，见[平板展示设计](/design/tablet)）。

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST/PUT | `/api/v1/ads/materials` | 新建 / 更新素材（name、type 1图2视频、url、durationMs、enabled、sort） |
| DELETE | `/api/v1/ads/materials/{id}` | 删除素材 |
| GET | `/api/v1/ads/materials/page` | 素材分页（current、pageSize） |
| POST | `/api/v1/ads/materials/upload` | multipart 上传媒体，落盘 `data/ads/yyyyMM/`，返回 `{ url, originalName, size }` |
| POST/PUT | `/api/v1/ads/schedules` | 新建 / 更新排期（screenId、materialId、weekdays `1=周一…7=周日` 逗号分隔可空、start/end `HH:mm` 可空、enabled） |
| DELETE | `/api/v1/ads/schedules/{id}` | 删除排期 |
| GET | `/api/v1/ads/schedules/page` | 排期分页 |
| POST/PUT | `/api/v1/ads/screens` | 新建 / 更新屏（code 唯一、name、location、enabled） |
| DELETE | `/api/v1/ads/screens/{id}` | 删除屏 |
| GET | `/api/v1/ads/screens/page` | 屏分页 |
| GET | `/api/v1/ads/playlist?screen={code}` | 平板下发：心跳更新 `last_seen_at`，按星期/时段命中排期取素材（无排期兜底全部启用素材），返回 `{ version, items[] }`；`version` 为素材+排期的 `max(update_time)` 内容戳。屏 code 不存在报 400 |

媒体文件经 `/media/**` 静态映射到上传目录（`sinomed.ads.upload-dir`，默认 `data/ads`），免登录直读。

## 叫号 `/api/v1/calls`（tablet.md T8）

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST | `/api/v1/calls` | 前台叫号（需登录）：number、room 必填，patientMasked 可选，screenId 为空 = 全部屏广播；定向屏需存在且启用 |
| GET | `/api/v1/calls/latest?screen={code}&since={id}` | 平板游标拉取（免登录）：`since` 之后、广播 + 定向本屏的记录按 id 升序，返回 `{ since, calls[] }`；`since` 回传下次调用。屏 code 不存在报 400 |
| GET | `/api/v1/calls/recent` | 管理端最近 20 条（需登录），按 id 倒序 |

## 错误追踪 `/error`

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST | `/error` | 接收前端错误上报并写入日志 |

## 通用约定

- **分页请求**：`current`（1 起始）、`pageSize`；**分页响应**：`{ data, total, pages, success }`。
- **路径绑定**：个别接口（按姓名 / 名称查询等）的 `{xxx}` 占位符需出现在 URL 中，实际取值来自同名查询参数。
- **错误响应**：400 参数或业务校验失败、401 未认证、403 账号锁定、500 系统错误，响应体为 `{ message, ... }`。
- **在线调试**：服务端启动后打开 <http://127.0.0.1:2347/doc.html>（Knife4j 中文界面）可直接调试。

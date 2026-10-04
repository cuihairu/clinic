# REST 接口清单

所有业务接口统一前缀 `/api/v1`，下表路径均以完整路径列出。管理端开发代理把 `/api` 转发到服务端（默认 `127.0.0.1:2347`），生产环境由 Nginx 把 `/api` 转发到服务端。

## 鉴权约定

- **放行路径**：`/api/v1/user/login` 以及接口文档相关路径（`/doc.html`、`/swagger-ui/**` 等）。其余接口一律需要 `Authorization: Bearer <JWT>`，未认证返回 401。
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

## 员工与考勤 `/api/v1/staff`

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
时长按整小时统计，不足 1 小时不显示；同日多次上班卡取最早、下班卡取最晚；未打下班卡但已上班的按当前时间计算。详见管理端的[考勤页面](/web#员工与考勤)。
:::

## 卡项 `/api/v1/item`

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST | `/api/v1/item/` | 创建卡项（名称唯一、价格、描述） |
| PUT | `/api/v1/item/` | 更新卡项 |
| GET | `/api/v1/item/page` | 分页查询（当前实现未按条件过滤，返回全部） |
| GET | `/api/v1/item/{id}` | 按 id 查询 |
| GET | `/api/v1/item/name/{name}` | 按名称查询（值取自查询参数） |
| DELETE | `/api/v1/item/{id}` | 删除，返回删除前的卡项 |

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

## 订单 `/api/v1/order`（预留）

以下 5 个接口路径已定义，**实现为占位（返回空对象）**，订单链路尚未接线：

| 方法 | 路径 |
| ---- | ---- |
| POST | `/api/v1/order/` |
| PUT | `/api/v1/order/` |
| GET | `/api/v1/order/{id}` |
| GET | `/api/v1/order/user/{id}` |
| DELETE | `/api/v1/order/{id}` |

## 错误追踪 `/error`

| 方法 | 路径 | 功能 |
| ---- | ---- | ---- |
| POST | `/error` | 接收前端错误上报并写入日志 |

## 通用约定

- **分页请求**：`current`（1 起始）、`pageSize`；**分页响应**：`{ data, total, pages, success }`。
- **路径绑定**：个别接口（按姓名 / 名称查询等）的 `{xxx}` 占位符需出现在 URL 中，实际取值来自同名查询参数。
- **错误响应**：400 参数或业务校验失败、401 未认证、403 账号锁定、500 系统错误，响应体为 `{ message, ... }`。
- **在线调试**：服务端启动后打开 <http://127.0.0.1:2347/doc.html>（Knife4j 中文界面）可直接调试。

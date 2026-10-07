# 数据模型

数据表由 JPA 按实体自动创建（`ddl-auto: update`），主键均为自增 `id`。所有实体带审计字段 `create_time` / `update_time`（JPA Auditing 自动维护）；实体之间不使用外键约束，通过业务 id 字段在服务层关联。

## customers — 顾客

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| name | String | 姓名（非空） |
| age | Integer | 年龄 |
| level | Integer | 会员等级 |
| phone | String | 手机号（唯一，大陆号段） |
| gender | Integer | 性别（0 女、1 男） |
| address | String | 地址 |
| birthday | Date | 生日 |

索引：姓名、手机号、等级、生日。

## treats — 诊疗单（核心业务表）

中医诊疗记录，一个顾客可有多条：

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| cid | Long | 顾客 id（非空，业务外键） |
| cdesc | TEXT | 主诉 |
| inquiry | TEXT | 问诊 |
| observation | TEXT | 望诊 |
| palpation | TEXT | 触诊 |
| pulse | String | 脉象 |
| pulse_left / pulse_right | TEXT | 左 / 右手脉象 |
| pulse_info | TEXT | 脉象解读 |
| five_ben_zi_* / five_zi_ben_* | String | 五行·子母关系（左/右手） |
| five_ben_ke_* / five_ke_ben_* | String | 五行·相克关系（左/右手） |
| five_nan_jin_*（大/小，左/右） | String | 难经取象 |
| five_rate_left / five_rate_right | TEXT | 五行比率 |
| five_aux_left / five_aux_right | TEXT | 五行辅助 |
| acupoint_left / acupoint_right | TEXT | 取穴（左/右手） |
| diagnose | TEXT | 诊断 |
| plan | TEXT | 调理方案 |
| diet | TEXT | 饮食建议 |
| conditioning | TEXT | 调理过程 |
| review | TEXT | 回访记录 |

索引：顾客 id、创建时间。

## items — 卡项

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| name | String | 名称（非空、唯一） |
| price | Integer | 价格 |
| description | TEXT | 描述 |
| enabled | Integer | 1 上架、0 下架（Kiosk 只展示上架项；存量行默认 1） |
| cover | String | 封面图相对 url（可空，`/media` 托管） |
| sort | Integer | 展示顺序，小者在前 |

## reviews / review_staffs / review_customers — 反馈与复盘

| 表 | 字段要点 | 说明 |
| ---- | ---- | ---- |
| `reviews`（每日总结） | `day`（唯一，归一为当日 0 点）、`good`、`improvement` | 全店每日总结，同日保存即覆盖 |
| `review_staffs`（员工日复盘） | `staff_id`、`name`、`cost`（耗费）、`done`、`advice`、`day` | 按员工按日记录 |
| `review_customers`（顾客回访） | `customer_id`、`name`、`last`（上次时间）、`day`（回访日）、`advice` | `day` 到期后出现在每日报表提醒 |

## orders / recharges

| 表 | 字段要点 | 现状 |
| ---- | ---- | ---- |
| `orders`（订单） | `user_id`（顾客）、`item_id`（卡项）、`staff_id`、`status`、`price` | 已接线：Kiosk 下单落 `status=0` 并快照卡项现价；`status` 语义 `0` 已下单 → `1` 已确认 → `2` 已完成，`9` 已取消（`0/1→9`，仅 `9` 可删），管理端分页与流转见 [api](/server/api) 订单组 |
| `recharges`（充值） | `user_id`、`money` | 仅有 Repository，无接口 |

## staffs — 员工（登录主体，诊所运营）

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| name | String | 姓名（非空） |
| account | String | 登录账号（唯一） |
| password | String | 密码（BCrypt） |
| phone | String | 手机号（唯一） |
| avatar | String | 头像 URL |
| age | Integer | 年龄 |
| gender | Integer | 性别（0 女、1 男） |
| address | String | 地址 |
| birthday | Date | 生日 |
| role | Integer | 角色：`< 10` 管理层（`admin`），`>= 10` 普通员工（默认 99） |
| status | Integer | 状态：0 离职、1 在职、2 休假（`<= 0` 无法登录） |

索引：姓名、账号、手机号。

## signs — 考勤打卡（诊所运营）

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| staff_id | Long | 员工 id |
| type | Integer | 1 上班打卡、0 下班打卡 |
| create_time | Date | 打卡时间 |

考勤统计在查询时实时计算（无定时任务）：同日多次上班卡取最早、下班卡取最晚，时长按整小时计。

## ad_materials / ad_screens / ad_schedules — 广告投屏（平板展示）

配套 [tablet/](/design/tablet) Kiosk 端，设计见[平板展示设计](/design/tablet)。文件本体不上库：素材上传落盘 `data/ads/yyyyMM/`（`sinomed.ads.upload-dir` 可改），表里存相对 url。

**ad_materials — 素材**：`name`、`type`（1 图片、2 视频）、`url`、`duration_ms`（轮播停留毫秒）、`enabled`、`sort`（顺序，小者在前）。

**ad_screens — 屏**：`code`（唯一，平板以 `?screen=code` 打开）、`name`、`location`、`enabled`、`last_seen_at`（平板拉 playlist 时顺带心跳，管理端按 2 分钟阈值显示在线角标）。

**ad_schedules — 排期**：`screen_id` / `material_id`（业务外键）、`weekdays`（`1=周一…7=周日` 逗号分隔，空 = 每天）、`start_time` / `end_time`（`HH:mm`，空 = 全天）、`enabled`。

## queue_calls — 叫号记录（平板展示）

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键（同时作平板拉取游标 `since`） |
| screen_id | Long | 定向屏（业务外键；为空 = 全部屏广播） |
| number | String | 号码（如 `08`） |
| room | String | 诊室名 |
| patient_masked | String | 脱敏姓名（如 `张*`，隐私默认） |
| status | Integer | 0 待叫、1 已叫（当前写入即 1） |
| called_at | Date | 叫号时间 |

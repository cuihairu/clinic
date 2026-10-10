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
| `orders`（订单） | `user_id`（顾客）、`item_id`（卡项）、`staff_id`、`status`、`price` | 已接线：Kiosk 下单与前台建单均落 `status=0` 并快照卡项现价（前台建单记录 `staff_id`）；`status` 语义 `0` 已下单 → `1` 已确认 → `2` 已完成，`9` 已取消（`0/1→9`，仅 `9` 可删），管理端分页与流转见 [api](/server/api) 订单组；结算信息在 `settlements` 表（一单一结算），订单分页联出支付方式 |
| `recharges`（储值流水） | `user_id`、`money` | 已接线：充值为正、储值支付扣减为负，余额 = 流水合计；接口见 [api](/server/api) 储值组 |

## settlements — 结算单（收费结算）

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键（结算单号，时间倒序展示） |
| order_id | Long | 订单 id（非空，唯一索引——一单一结算，防重复收款） |
| user_id | Long | 顾客 id（非空，冗余自订单便于按顾客对账） |
| pay_type | Integer | 支付方式：`1` 储值 / `2` 微信 / `3` 支付宝 / `4` 现金 / `5` 次卡抵扣 |
| money | Integer | 实收金额（元，取订单价格快照；次卡抵扣记 0） |
| create_time / update_time | Date | 审计时间 |

索引：`order_id`（唯一）、`user_id`。结算动作把订单 `0/1 → 2` 已完成；储值支付同时在 `recharges` 落一条负数流水；次卡抵扣扣 `customer_cards` 余 1 次。微信/支付宝为演示口径（仅记录方式，无真实收银通道），见 [api](/server/api) 收费组。

## customer_cards — 顾客持卡（次卡）

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| customer_id | Long | 顾客 id（非空，索引，customers.id） |
| item_id | Long | 卡项 id（非空，items.id） |
| total_times / remaining_times | Integer | 总次数 / 剩余次数（发卡=全量，抵扣减余次） |
| status | Integer | `1` 有效 / `0` 停用（列默认 1；停用卡不参与抵扣） |
| source_order_id | Long | 发卡来源订单（可空，前台手工发卡为空） |
| create_time / update_time | Date | 审计时间 |

索引：顾客。抵扣按「同顾客 + 同卡项、有效且有余次」取**最早一张**扣 1 次；暂无有效期（规划）。接口见 [api](/server/api) 次卡组。

## card_usages — 次卡核销流水

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| card_id | Long | 持卡 id（非空，索引，customer_cards.id） |
| order_id | Long | 触发抵扣的订单（可空——老卡补录/历史迁移无单据） |
| staff_id | Long | 服务/操作员工（可空，取订单 staffId） |
| times_used | Integer | 本次是第几次消费（1 起 = 总次数 − 扣后余次） |
| create_time / update_time | Date | 审计时间 |

append-only：每次结算抵扣（`payType=5`）在同一事务落一条，随结算回滚；不做手工划扣。种子数据为演示卡片补了历史核销（无订单号）。接口见 [api](/server/api) 次卡组。

## appointments — 预约（前台/馆长建约、小程序自助约期 → 到店接待 → 转接诊）

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| customer_id | Long | 顾客 id（非空，customers.id） |
| item_id | Long | 预约卡项 id（可空=到店再定） |
| staff_id | Long | 接待员工 id（可空=到店分配） |
| start_time | Date | 预约时段开始时刻（非空） |
| duration | Integer | 时长（分钟；自助约期固定 60） |
| status | Integer | `0` 待到店 → `1` 已接待（转接诊），`0→9` 取消；仅 `9` 可删 |
| remark | String | 备注（症状/需求）；自助约期记「小程序自助」软标记，无 source 列 |
| create_time / update_time | Date | 审计时间 |

索引：顾客、卡项、时段。接口见 [api](/server/api) 预约组；不做同时段冲突校验（演示边界）。

## prescriptions / prescription_items — 中药处方

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键（处方号） |
| treat_id | Long | 接诊单 id（可空，treats.id） |
| customer_id | Long | 顾客 id（非空，customers.id） |
| staff_id | Long | 开方医师 id（可空，staffs.id） |
| doses | Integer | 剂数（几付，非空，默认 7） |
| usage | String | 用法：煎服法/频次/代煎说明 |
| remark | String | 备注 |
| create_time / update_time | Date | 审计时间 |

`prescription_items`（药味）：`prescription_id`（非空，索引）、`herb`（药名，非空，自由文本）、`weight`（单剂克数，Double，非空）、`special`（特殊煎法，可空）、`sort`（顺序，非空）。

`herbs`（药材字典）：`name`（唯一，与处方药名精确同名比价）、`price`（每克分价，int，>0）、审计时间。处方计价按字典**实时试算**（不落库、无快照，未收录药名不计费）。

索引：`prescriptions` 顾客、接诊单；`prescription_items` 处方。库存与代煎领取为规划功能；配伍审方（十八反/十九畏）为静态规则比对（不落表），见 [api](/server/api) 处方组。

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

## staff_leaves — 员工请假（诊所运营）

| 字段 | 类型 | 含义 |
| ---- | ---- | ---- |
| id | Long | 主键 |
| staff_id | Long | 员工 id（staffs.id） |
| leave_type | Integer | 类型：0 病假、1 事假 |
| reason | String | 事由（非空） |
| start_time / end_time | Date | 起止时间（结束不早于起始） |
| create_time / update_time | Date | 审计时间 |

索引：员工 id。口径：请假为记录性质，不改员工登录状态、无审批流（演示口径）。

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

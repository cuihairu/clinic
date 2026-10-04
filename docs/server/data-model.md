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

## reviews / review_staffs / review_customers — 反馈与复盘

| 表 | 字段要点 | 说明 |
| ---- | ---- | ---- |
| `reviews`（每日总结） | `day`（唯一，归一为当日 0 点）、`good`、`improvement` | 全店每日总结，同日保存即覆盖 |
| `review_staffs`（员工日复盘） | `staff_id`、`name`、`cost`（耗费）、`done`、`advice`、`day` | 按员工按日记录 |
| `review_customers`（顾客回访） | `customer_id`、`name`、`last`（上次时间）、`day`（回访日）、`advice` | `day` 到期后出现在每日报表提醒 |

## orders / recharges — 预留表

| 表 | 字段要点 | 现状 |
| ---- | ---- | ---- |
| `orders`（订单） | `user_id`（顾客）、`item_id`（卡项）、`staff_id`、`status`、`price` | 表已定义，接口为占位，业务未接线 |
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

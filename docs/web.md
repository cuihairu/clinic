# 管理端（web）

`web/` 是医馆日常运转的主工作台：Ant Design Pro + Umi（@umijs/max）工程，Node.js 24 / pnpm，中文界面。

## 运行方式

```bash
cd web
pnpm install
pnpm dev     # dev 模式：MOCK=none，代理到本机服务端
```

- 开发地址 `http://localhost:8000`，`/api` 代理到 `http://127.0.0.1:2347`（`config/proxy.ts`）。
- `pnpm build` 产出 `dist/`，交由 Nginx 托管；`pnpm lint` 代码检查、`pnpm jest` 单测。

## 工程结构

| 位置 | 内容 |
| ---- | ---- |
| `config/config.ts` | 站点标题、布局（mix）、国际化（zh-CN 默认）、插件与 openAPI 配置 |
| `config/routes.ts` | 路由表（页面注册） |
| `config/proxy.ts` | dev / test / pre 环境代理 |
| `src/pages/` | 页面组件，按业务域分目录 |
| `src/services/ant-design-pro/` | 接口定义（与服务端 `/api/v1/**` 一一对应） |
| `src/access.ts` | 运行时权限：`canAdmin`（管理层）、`canUser`（已登录） |
| `src/app.tsx` | 启动时拉取当前用户、未登录重定向、水印 |
| `src/requestErrorConfig.ts` | 从 `localStorage.jwt` 注入 `Authorization: Bearer`，统一错误提示 |
| `src/locales/` | 菜单等文案多语言 |

## 页面与任务对照

登录后默认落地 **每日报表**（`/dash/day`）。按任务找页面：

### 报表系统（/dash）

| 路径 | 页面 | 做什么 |
| ---- | ---- | ---- |
| `/dash/day` | 每日报表 | 默认首页：员工总结表、今日回访、今日总结三块拼版 |

### 顾客系统（/customer）

| 路径 | 页面 | 做什么 |
| ---- | ---- | ---- |
| `/customer/create` | 顾客建案 | 建档：姓名/年龄/性别/手机/住址/会员等级/生日 |
| `/customer/query` | 顾客查询 | 分页检索顾客，行操作：创建诊断、更新、诊断历史、回访计划、删除 |
| `/customer/update` | 顾客更新 | 回填顾客信息并保存 |

### 诊断系统（/treat）

| 路径 | 页面 | 做什么 |
| ---- | ---- | ---- |
| `/treat/create` | 创建诊断 | 诊疗单大表单：主诉、问/望/切、脉象（左右手）、五行、取穴、诊断、方案、饮食、调理；支持 `?customerId=` 预载、`?treatId=` 复诊回填 |
| `/treat/query` | 模糊查询 | 按 id / 姓名 / 年龄 / 手机号检索诊断单 |
| `/treat/history` | 诊断历史 | 某顾客的历史诊疗记录（按时间倒序） |

### 员工系统（/staff）

| 路径 | 页面 | 做什么 | 权限 |
| ---- | ---- | ---- | ---- |
| `/staff/create` | 创建员工 | 新建员工账号 | 仅 `canAdmin` |
| `/staff/query` | 员工查询 | 分页检索与删除员工 | 仅 `canAdmin` |
| `/staff/update` | 员工更新 | 回填并保存 | - |
| `/staff/sign` | 员工签到 | 选择上班 / 下班打卡 | 已登录 |
| `/staff/sign/timesheet/today` | 今日考勤 | 本人打卡流水与总时长 | 已登录 |
| `/staff/timesheet/list` | 考勤列表 | 按月的员工 × 日期考勤矩阵 | - |
| `/staff/leave` | 员工请假 | 请假表单（注意：当前提交调用的是创建员工接口，功能未完成） | - |

### 卡项系统（/item）

| 路径 | 页面 | 做什么 |
| ---- | ---- | ---- |
| `/item/create` | 创建卡项 | 名称 / 价格 / 描述，支持 `?itemId=` 回填 |
| `/item/query` | 卡项查询 | 分页检索与删除 |
| `/item/spread`、`/item/update` | 卡项展示 / 更新 | 占位页面（组件与提交逻辑未完成） |

### 订单系统（/order，未完成）

| 路径 | 页面 | 状态 |
| ---- | ---- | ---- |
| `/order/create` | 创建订单 | 占位：表单与提交未接真实订单接口 |
| `/order/query` | 订单查询 | 占位：当前复用顾客查询接口 |

::: warning 与服务端一致的预留状态
订单系统两端（前端页面与服务端接口）都处于占位状态，暂不可作为可用功能对外介绍。
:::

### 反馈系统（/review）

| 路径 | 页面 | 做什么 |
| ---- | ---- | ---- |
| `/review/day` | 每日总结 | 填写 / 查看当天做得好的与待改进 |
| `/review/staff` | 员工总结 | 批量维护员工日复盘 |
| `/review/customer` | 顾客反馈 | 批量维护顾客回访记录 |
| `/review/plan` | 回访计划 | 为顾客设定回访日与建议 |

## 权限与角色

- `canAdmin`：管理端创建 / 查询员工入口（对应服务端 `role < 10` 的管理层）；
- `canUser`：签到与今日考勤入口（任何已登录员工）；
- 服务端接口层暂未强制方法级权限，页面级隐藏是当前主要控制手段。

## 前端遗留项（如实说明）

- 站点标题当前为模板值 `Youngs.fun`（`config/config.ts`），未改为项目名；
- `services` 中保留 OpenAPI 模板生成的 petstore 示例接口与 `/api/rule` 模板接口，未使用；
- `src/pages/Welcome.tsx`、`Admin.tsx`、`TableList/` 为脚手架残留页面，不在路由中；
- `manifest.json` / PWA 名称仍是脚手架默认值。

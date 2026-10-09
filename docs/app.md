# 小程序端（app）

`app/` 是 Taro 多端工程（React + TypeScript + NutUI React Taro），同一套代码编译到微信小程序、H5 等目标。

::: warning 当前状态
小程序端已完成**工程初始化 + 白标构建 + 顾客首页（单页）**：首页按 mobile-home 原型实装（品牌头 / 预约横幅 / 今日宜养 / 我的卡项 / 养生贴士 / 白标自检页底），「今日宜养」宫格实拉上架项目（复用自助机只读接口 `GET /api/v1/kiosk/items`），请求封装层见 `src/services/api.ts`；预约接口已在服务端就绪（`/api/v1/appointment`），小程序自助约期的**免登录写入口设计草案已出**（[小程序自助约期设计](/design/app-booking)，待拍板，未实现），预约/我的卡项自助页面尚未开发，界面如实标「规划功能」占位。登录与其余业务页面尚未开发。换商家出包见[换马甲构建指南](/app-white-label)。业务功能以[管理端](/web)为准。
:::

顾客首页已按设计稿实装（下方为原型稿，数据为虚构演示；实机宫格数据来自真实接口）：

![小程序端 · 首页（界面原型）](/screenshots/mobile-home.png)

## 工程结构

| 位置 | 内容 |
| ---- | ---- |
| `config/index.ts` | Taro 编译配置（designWidth 375、webpack5、白标参数注入、H5 htmlPluginOption） |
| `config/brand.ts` | 白标品牌参数解析（`BRAND_*` 环境变量 → 带校验的品牌配置，默认值即仓库内置） |
| `src/services/brand.ts` | 业务代码侧品牌常量（编译期替换） |
| `src/services/api.ts` | 请求封装层（`Taro.request`；已封装 `GET /api/v1/kiosk/items`，H5 开发态走 devServer 代理，其余端直连白标 apiBase） |
| `src/app.config.ts` | 页面注册（当前仅 `pages/index/index`）、导航栏品牌名/主题色；无 tabBar |
| `src/pages/index/` | 顾客首页：品牌头 / 预约横幅（规划占位）/ 今日宜养（宫格实拉上架项目）/ 我的卡项（规划占位）/ 养生贴士 / 白标自检页底（品牌名/主题色/后端域名/页内图标） |
| `scripts/build-brand.mjs` | 换马甲一键出包脚本（参数校验 → 注入 → 构建 → 恢复 appid） |
| `brand-assets/` | 仓库默认图标资产（商家图标不进这里，见[白标指南](/app-white-label)） |
| `project.config.json` | 微信开发者工具配置（`miniprogramRoot: ./dist`；构建后 appid 自动恢复） |
| `project.tt.json` | 抖音小程序配置 |
| `babel.config.js` | babel-plugin-import 按需引入 NutUI |

## 构建命令

```bash
cd app
pnpm install
pnpm dev:h5        # H5 开发（浏览器预览）
pnpm dev:weapp     # 微信小程序开发（watch 模式）
pnpm build:h5      # H5 产物 → dist/
pnpm build:weapp   # 微信小程序产物 → dist/
```

其余端（swan / alipay / tt / qq / jd / rn / quickapp）的构建脚本同样可用，见 `package.json`。

换商家出包不走上面的裸构建命令，统一走[白标一键脚本](/app-white-label)：

```bash
node scripts/build-brand.mjs --name 商家名 --domain https://... --theme '#RRGGBB' --appid wx...
```

## 关键配置

- **设计稿**：designWidth 375，`pxtransform` 把 px 转 rpx，NutUI 组件（`nut-` 前缀）豁免转换；
- **样式方案**：原生 CSS 文件（无 CSS Modules），`src/tokens.css` 与 `docs/design/mockups/tokens.css` 同源（品牌变量单一出处）；
- **UI 库**：`@nutui/nutui-react-taro` 2.x，经 babel-plugin-import 按需引入；
- **白标注入**：`BRAND_*` 环境变量 → 编译期替换（详见[换马甲构建指南](/app-white-label)）；
- **webpack**：锁定 5.104.1（5.111 起 ProgressPlugin 加了 schema 校验，与 Taro 3.6.40 内置 webpackbar 冲突）；
- **微信 appid**：仓库存占位 `touristappid`；商家 appid 由构建脚本写进产物，仓库文件不改。

## 后续接手建议

- 请求封装层已建（`src/services/api.ts`），后续接口按业务域扩展；带鉴权的接口参照管理端 `src/requestErrorConfig.ts` 的 token 注入方式；
- 页面注册走 `src/app.config.ts`，tabBar 按业务域规划；
- 首屏登录流程可参照服务端 `POST /api/v1/user/login` 的返回结构（token 存本地）。

# 小程序端（app）

`app/` 是 Taro 多端工程（React + TypeScript + NutUI React Taro），同一套代码编译到微信小程序、H5 等目标。

::: warning 当前状态
小程序端目前处于**工程初始化阶段**：仅有模板演示首页，网络请求层尚未搭建，业务页面尚未开发。业务功能以[管理端](/web)为准。本文档如实记录现有结构，便于后续接手开发。
:::

## 工程结构

| 位置 | 内容 |
| ---- | ---- |
| `config/index.ts` | Taro 编译配置（`projectName`、designWidth 375、webpack5、各端输出） |
| `config/dev.ts` / `prod.ts` | 环境差异配置（当前仅 `NODE_ENV`） |
| `src/app.config.ts` | 页面注册（当前仅 `pages/index/index`）、窗口配置；无 tabBar |
| `src/app.ts` | 应用入口（生命周期为空壳，引入全局样式） |
| `src/pages/index/` | 唯一页面：NutUI 欢迎页 + 按钮示例 |
| `project.config.json` | 微信开发者工具配置（`miniprogramRoot: ./dist`） |
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

## 关键配置

- **设计稿**：designWidth 375，`pxtransform` 把 px 转 rpx，NutUI 组件（`nut-` 前缀）豁免转换；
- **样式方案**：原生 CSS 文件（无 CSS Modules）；
- **UI 库**：`@nutui/nutui-react-taro` 2.x，经 babel-plugin-import 按需引入；
- **微信 appid**：`project.config.json` 当前为占位 `touristappid`，正式开发需替换为自己的 appid。

## 后续接手建议

- 先在 `src/` 下建立请求封装层（服务端接口前缀 `/api/v1`，参照管理端 `src/requestErrorConfig.ts` 的 token 注入方式）；
- 页面注册走 `src/app.config.ts`，tabBar 按业务域规划；
- 首屏登录流程可参照服务端 `POST /api/v1/user/login` 的返回结构（token 存本地）。

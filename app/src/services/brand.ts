/**
 * 白标品牌参数（运行时读取）。
 *
 * 值来自构建期注入的 TARO_APP_* 常量（见 config/index.ts 的 env），
 * 业务代码一律从这里取品牌信息，不要散落硬编码。
 */
export const BRAND = {
  /** 品牌名（导航栏 / 页面标题用） */
  name: process.env.TARO_APP_BRAND_NAME,
  /** 主题色（#RRGGBB） */
  themeColor: process.env.TARO_APP_BRAND_THEME,
  /** 后端 API 域名，请求层 base URL */
  apiBase: process.env.TARO_APP_API_BASE,
  /** 应用图标 data URL；商家未提供位图图标时为空串 */
  iconData: process.env.TARO_APP_BRAND_ICON || ''
} as const

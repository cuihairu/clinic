/**
 * 白标品牌参数（构建期解析，只在 config 侧使用，不进业务代码）。
 *
 * 取值优先级：BRAND_* 环境变量 > 仓库内置默认值。
 * 换马甲出包走 app/scripts/build-brand.mjs 传参，业务代码只读
 * `process.env.TARO_APP_*` 常量，任何商家参数都不写进源码。
 */

const DEFAULTS = {
  /** 品牌名（导航栏标题 / H5 <title>） */
  name: 'Sinomed',
  /** 主题色（导航栏背景、页内强调色），须为 #RGB 或 #RRGGBB */
  themeColor: '#C7A674',
  /** 后端 API 域名（含协议，无尾斜杠），未来请求层的 base URL */
  apiBase: 'https://sinomed.cuihairu.site',
  /** 小程序 appid（对应 project.config.json，构建时临时改写后还原） */
  appid: 'touristappid',
  /** 副标题描述（H5 meta description） */
  description: '中医医馆管理系统小程序端'
}

const HEX_COLOR = /^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/

export interface BrandConfig {
  name: string
  themeColor: string
  apiBase: string
  appid: string
  description: string
  /** 图标文件绝对路径（H5 favicon 用）；null = 用仓库内置 logo */
  iconFile: string | null
  /** 图标 data URL（页内 <Image> 用），仅商家图标为位图时注入 */
  iconData: string | null
}

export function getBrand(): BrandConfig {
  const name = process.env.BRAND_NAME || DEFAULTS.name
  const themeColor = process.env.BRAND_THEME || DEFAULTS.themeColor
  if (!HEX_COLOR.test(themeColor)) {
    throw new Error(`[brand] BRAND_THEME 不是合法色值: ${themeColor}（形如 #C7A674）`)
  }
  const apiBase = (process.env.BRAND_API_BASE || DEFAULTS.apiBase).replace(/\/+$/, '')
  const appid = process.env.BRAND_APPID || DEFAULTS.appid
  if (appid !== 'touristappid' && !/^wx[0-9a-f]{16}$/.test(appid)) {
    throw new Error(`[brand] BRAND_APPID 不合法: ${appid}（应为 wx 开头 16 位 hex，或 touristappid）`)
  }
  return {
    name,
    themeColor,
    apiBase,
    appid,
    description: process.env.BRAND_DESCRIPTION || DEFAULTS.description,
    iconFile: process.env.BRAND_ICON_FILE || null,
    iconData: process.env.BRAND_ICON_DATA || null
  }
}

export { DEFAULTS as BRAND_DEFAULTS }

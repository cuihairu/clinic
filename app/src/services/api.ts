import Taro from '@tarojs/taro'
import { BRAND } from './brand'

/** 上架卡项（与服务端 GET /api/v1/kiosk/items 同构，顾客侧展示同一份上架数据） */
export interface HomeItem {
  id: number
  name: string
  price: number
  cover: string | null
  description: string | null
}

function itemsUrl(): string {
  // H5 开发态走 devServer 代理（config/dev.ts），其余端直连白标 apiBase
  if (process.env.NODE_ENV === 'development' && process.env.TARO_ENV === 'h5') {
    return '/api/v1/kiosk/items'
  }
  return `${BRAND.apiBase}/api/v1/kiosk/items`
}

/** 拉上架项目；失败抛错（页面就地提示，不造假数据） */
export async function fetchHomeItems(): Promise<HomeItem[]> {
  const res = await Taro.request({
    url: itemsUrl(),
    method: 'GET',
    header: { Accept: 'application/json' }
  })
  if (res.statusCode < 200 || res.statusCode >= 300) {
    throw new Error(`加载失败（${res.statusCode}）`)
  }
  return res.data as HomeItem[]
}

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

function kioskUrl(path: string): string {
  // H5 开发态走 devServer 代理（config/dev.ts），其余端直连白标 apiBase
  if (process.env.NODE_ENV === 'development' && process.env.TARO_ENV === 'h5') {
    return `/api/v1/kiosk/${path}`
  }
  return `${BRAND.apiBase}/api/v1/kiosk/${path}`
}

/** 拉上架项目；失败抛错（页面就地提示，不造假数据） */
export async function fetchHomeItems(): Promise<HomeItem[]> {
  const res = await Taro.request({
    url: kioskUrl('items'),
    method: 'GET',
    header: { Accept: 'application/json' }
  })
  if (res.statusCode < 200 || res.statusCode >= 300) {
    throw new Error(`加载失败（${res.statusCode}）`)
  }
  return res.data as HomeItem[]
}

/** 自助约期提交体（与服务端 POST /api/v1/kiosk/appointments 同构） */
export interface BookingBody {
  phone: string
  name?: string
  itemId?: number
  startTime: string
}

/** 自助约期回执 */
export interface BookingResult {
  customerId: number
  customerName: string
  appointmentId: number
  startTime: string
  itemName?: string | null
  status: number
}

/**
 * 提交自助约期（免登录）；服务端 400 的 errorMessage 原样透出（如频控文案）。
 */
export async function bookAppointment(body: BookingBody): Promise<BookingResult> {
  const res = await Taro.request({
    url: kioskUrl('appointments'),
    method: 'POST',
    header: { 'Content-Type': 'application/json', Accept: 'application/json' },
    data: body
  })
  if (res.statusCode < 200 || res.statusCode >= 300) {
    const msg =
      res.data && typeof res.data === 'object' && res.data.errorMessage
        ? res.data.errorMessage
        : `提交失败（${res.statusCode}）`
    throw new Error(msg)
  }
  return res.data as BookingResult
}

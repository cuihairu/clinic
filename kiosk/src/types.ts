/** 服务端 GET /api/v1/kiosk/items 单项（见 KioskController） */
export interface KioskItem {
  id: number
  name: string
  /** 元 */
  price: number
  /** 封面相对 url（/media 托管），可空 */
  cover: string | null
  description: string
}

/** 服务端 POST /api/v1/kiosk/orders 回执 */
export interface KioskOrderResult {
  customerId: number
  customerName: string
  orders: { id: number; itemId: number; itemName: string; price: number }[]
  totalFee: number
}

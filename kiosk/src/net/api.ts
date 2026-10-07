import { useEffect, useState } from 'react'
import type { KioskItem, KioskOrderResult } from '../types'

/** 拉上架卡项；失败返回 null（页面显示重试，不打断） */
export async function fetchKioskItems(): Promise<KioskItem[] | null> {
  try {
    const res = await fetch('/api/v1/kiosk/items', { headers: { Accept: 'application/json' } })
    if (!res.ok) {
      console.warn('[kiosk] items 非 2xx：', res.status)
      return null
    }
    return (await res.json()) as KioskItem[]
  } catch (error) {
    console.warn('[kiosk] items 拉取失败（离线？）：', error)
    return null
  }
}

/** 首页拉一次；加载失败给 retry 按钮手动重拉 */
export function useItems() {
  const [items, setItems] = useState<KioskItem[] | null>(null)
  const [retryTick, setRetryTick] = useState(0)
  useEffect(() => {
    let alive = true
    fetchKioskItems().then((next) => {
      if (alive) setItems(next)
    })
    return () => {
      alive = false
    }
  }, [retryTick])
  return { items, retry: () => setRetryTick((t) => t + 1) }
}

/** 提交下单；失败抛错给页面提示 */
export async function submitOrder(body: {
  phone: string
  name?: string
  itemIds: number[]
}): Promise<KioskOrderResult> {
  const res = await fetch('/api/v1/kiosk/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body)
  })
  const data = await res.json()
  if (!res.ok) {
    throw new Error(data?.errorMessage || `下单失败（${res.status}）`)
  }
  return data as KioskOrderResult
}

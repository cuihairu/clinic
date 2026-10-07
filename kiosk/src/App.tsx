import { useEffect, useRef, useState } from 'react'
import { useItems } from './net/api'
import { ItemGrid } from './menu/ItemGrid'
import { Checkout } from './order/Checkout'
import { Success } from './order/Success'
import type { KioskOrderResult } from './types'
import './app.css'

/** 首页（含下单页）无操作超时：清篮回首页，见 kiosk.md 交互链路 30s */
const IDLE_MS = 30_000
/** 成功回执停留时长（倒计时后自动回首页） */
const SUCCESS_MS = 10_000

type View = 'browse' | 'checkout' | 'success'

export default function App() {
  const { items, retry } = useItems()
  const [selected, setSelected] = useState<Set<number>>(new Set())
  const [view, setView] = useState<View>('browse')
  const [receipt, setReceipt] = useState<KioskOrderResult | null>(null)
  const [countdown, setCountdown] = useState(Math.floor(SUCCESS_MS / 1000))

  // 空闲计时器：任何指针/键盘操作都续期；到点整机复位（清篮 + 回首页）
  const idleTimer = useRef<number | null>(null)
  const resetAll = () => {
    setSelected(new Set())
    setReceipt(null)
    setView('browse')
  }
  const bumpIdle = () => {
    if (idleTimer.current !== null) window.clearTimeout(idleTimer.current)
    idleTimer.current = window.setTimeout(resetAll, IDLE_MS)
  }
  useEffect(() => {
    bumpIdle()
    const bump = () => bumpIdle()
    window.addEventListener('pointerdown', bump)
    window.addEventListener('keydown', bump)
    return () => {
      if (idleTimer.current !== null) window.clearTimeout(idleTimer.current)
      window.removeEventListener('pointerdown', bump)
      window.removeEventListener('keydown', bump)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // 成功页倒计时 → 自动回首页
  useEffect(() => {
    if (view !== 'success') return
    setCountdown(Math.floor(SUCCESS_MS / 1000))
    const tick = window.setInterval(() => setCountdown((s) => s - 1), 1000)
    const done = window.setTimeout(resetAll, SUCCESS_MS)
    return () => {
      window.clearInterval(tick)
      window.clearTimeout(done)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [view])

  const toggle = (id: number) => {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  if (items === null) {
    return (
      <div className="kiosk-center">
        <div className="kiosk-hint">菜单加载失败，请检查网络</div>
        <button type="button" className="kiosk-retry" onClick={retry}>
          重试
        </button>
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <div className="kiosk-center">
        <div className="kiosk-hint">暂无上架服务，请咨询前台</div>
      </div>
    )
  }

  if (view === 'checkout') {
    return (
      <Checkout
        items={items}
        selected={selected}
        onBack={() => setView('browse')}
        onSuccess={(next) => {
          setReceipt(next)
          setView('success')
        }}
      />
    )
  }

  if (view === 'success' && receipt !== null) {
    return <Success receipt={receipt} countdown={countdown} onDone={resetAll} />
  }

  const pickedCount = selected.size
  const pickedTotal = items
    .filter((item) => selected.has(item.id))
    .reduce((sum, item) => sum + (item.price || 0), 0)

  return (
    <div className="kiosk-page">
      <header className="kiosk-header">
        <span className="kiosk-title">选服务</span>
        <span className="kiosk-sub">点选想做的项目，找前台确认</span>
      </header>
      <ItemGrid items={items} selected={selected} onToggle={toggle} />
      {pickedCount > 0 && (
        <div className="basket-bar">
          <span className="basket-summary">
            已选 {pickedCount} 项 · ¥{pickedTotal}
          </span>
          <button
            type="button"
            className="basket-go"
            onClick={() => setView('checkout')}
          >
            去下单
          </button>
        </div>
      )}
    </div>
  )
}

import { useState } from 'react'
import { useItems } from './net/api'
import { ItemGrid } from './menu/ItemGrid'
import './app.css'

export default function App() {
  const { items, retry } = useItems()
  const [selected, setSelected] = useState<Set<number>>(new Set())

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
        </div>
      )}
    </div>
  )
}

import type { KioskItem } from '../types'
import './menu.css'

/**
 * 卡项网格：大字触摸友好；点选切换选中（购物篮在底部状态栏，下单见 K5）。
 */
export function ItemGrid({
  items,
  selected,
  onToggle,
}: {
  items: KioskItem[]
  selected: Set<number>
  onToggle: (id: number) => void
}) {
  return (
    <div className="item-grid">
      {items.map((item) => {
        const isPicked = selected.has(item.id)
        return (
          <button
            key={item.id}
            type="button"
            className={isPicked ? 'item-card item-card-picked' : 'item-card'}
            onClick={() => onToggle(item.id)}
          >
            <div className="item-cover">
              {item.cover ? (
                <img src={item.cover} alt={item.name} />
              ) : (
                <span className="item-cover-fallback">{item.name.slice(0, 1)}</span>
              )}
              {isPicked && <span className="item-check">✓</span>}
            </div>
            <div className="item-name">{item.name}</div>
            {item.description && <div className="item-desc">{item.description}</div>}
            <div className="item-price">¥{item.price}</div>
          </button>
        )
      })}
    </div>
  )
}

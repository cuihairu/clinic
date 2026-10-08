import type { KioskItem } from '../types'
import './menu.css'

/**
 * 卡项网格（对齐原型 kiosk-menu .svc）：渐变首字封面 + 底部横排
 * 圆勾 / 名称（+简介）/ 宋体价签；点选切换选中，选中卡墨绿描边+光环。
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
      {items.map((item, index) => {
        const isPicked = selected.has(item.id)
        const tone = index % 2 === 0 ? 'c1' : 'c2'
        return (
          <button
            key={item.id}
            type="button"
            className={isPicked ? 'item-card item-card-picked' : 'item-card'}
            onClick={() => onToggle(item.id)}
          >
            <div className={`item-cover ${tone}`}>
              {item.cover ? (
                <img src={item.cover} alt={item.name} />
              ) : (
                <span className="item-cover-fallback serif">{item.name.slice(0, 1)}</span>
              )}
            </div>
            <div className="item-body">
              <span className="item-check">{isPicked ? '✓' : ''}</span>
              <span className="item-nameblock">
                <span className="item-name">{item.name}</span>
                {item.description && <span className="item-desc">{item.description}</span>}
              </span>
              <span className="item-price serif">¥{item.price}</span>
            </div>
          </button>
        )
      })}
    </div>
  )
}

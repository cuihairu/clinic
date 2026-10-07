import type { KioskOrderResult } from '../types'
import './order.css'

/**
 * 成功回执：单号列表 + 合计，10s 自动回首页（倒计时由 App 统一管）。
 */
export function Success({
  receipt,
  countdown,
  onDone,
}: {
  receipt: KioskOrderResult
  countdown: number
  onDone: () => void
}) {
  return (
    <div className="order-page">
      <div className="order-card order-card-success">
        <div className="order-success-mark">✓</div>
        <div className="order-title">下单成功</div>
        <div className="order-sub">已登记「{receipt.customerName}」，请到前台确认</div>

        <ul className="order-lines">
          {receipt.orders.map((line) => (
            <li key={line.id}>
              <span className="order-line-name">
                {line.itemName}
                <span className="order-line-id">单号 {line.id}</span>
              </span>
              <span className="order-line-price">¥{line.price}</span>
            </li>
          ))}
        </ul>
        <div className="order-total">合计 ¥{receipt.totalFee}</div>

        <div className="order-actions">
          <button type="button" className="order-btn order-btn-primary" onClick={onDone}>
            返回首页
          </button>
        </div>
        <div className="order-countdown">{countdown} 秒后自动返回首页</div>
      </div>
    </div>
  )
}

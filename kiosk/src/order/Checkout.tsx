import { useState } from 'react'
import type { KioskItem, KioskOrderResult } from '../types'
import { submitOrder } from '../net/api'
import './order.css'

/**
 * 下单页：核对已选 + 手机号（11 位校验）+ 可选称呼 → 提交。
 * 成功后回执交给 onSuccess，失败就地提示不离页。
 */
export function Checkout({
  items,
  selected,
  onBack,
  onSuccess,
}: {
  items: KioskItem[]
  selected: Set<number>
  onBack: () => void
  onSuccess: (receipt: KioskOrderResult) => void
}) {
  const [phone, setPhone] = useState('')
  const [name, setName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const picked = items.filter((item) => selected.has(item.id))
  const total = picked.reduce((sum, item) => sum + (item.price || 0), 0)

  const submit = async () => {
    if (submitting) return
    if (!/^1\d{10}$/.test(phone)) {
      setError('请输入 11 位手机号')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const receipt = await submitOrder({
        phone,
        name: name.trim() || undefined,
        itemIds: picked.map((item) => item.id),
      })
      onSuccess(receipt)
    } catch (e) {
      setError(e instanceof Error ? e.message : '下单失败，请重试')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="order-page">
      <div className="order-card">
        <div className="order-title">确认下单</div>
        <ul className="order-lines">
          {picked.map((item) => (
            <li key={item.id}>
              <span className="order-line-name">{item.name}</span>
              <span className="order-line-price">¥{item.price}</span>
            </li>
          ))}
        </ul>
        <div className="order-total">合计 ¥{total}</div>

        <label className="order-field">
          <span className="order-label">手机号（接单通知用）</span>
          <input
            className="order-input"
            type="tel"
            inputMode="numeric"
            maxLength={11}
            placeholder="11 位手机号"
            value={phone}
            onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 11))}
          />
        </label>
        <label className="order-field">
          <span className="order-label">称呼（选填）</span>
          <input
            className="order-input"
            type="text"
            maxLength={20}
            placeholder="如：王女士"
            value={name}
            onChange={(e) => setName(e.target.value.slice(0, 20))}
          />
        </label>

        {error && <div className="order-error">{error}</div>}

        <div className="order-actions">
          <button type="button" className="order-btn order-btn-ghost" onClick={onBack}>
            返回修改
          </button>
          <button
            type="button"
            className="order-btn order-btn-primary"
            disabled={submitting}
            onClick={submit}
          >
            {submitting ? '提交中…' : `确认下单 ¥${total}`}
          </button>
        </div>
      </div>
    </div>
  )
}

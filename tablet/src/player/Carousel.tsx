import { useEffect, useRef, useState } from 'react'
import type { PlaylistItem } from '../types'
import './carousel.css'

/**
 * 轮播引擎：按顺序播 items，图片按 durationMs 停留，视频播完即走
 * （durationMs 作为视频兜底计时，防止个别编码卡住 onended）。
 * 序列变化时从第一张重新开始（mediaVersion 作 key 触发重挂载）。
 */
export function Carousel({ items, mediaVersion }: { items: PlaylistItem[]; mediaVersion: string }) {
  const [index, setIndex] = useState(0)
  const timerRef = useRef<number | null>(null)

  useEffect(() => {
    setIndex(0)
  }, [mediaVersion])

  useEffect(() => {
    if (items.length === 0) return
    const current = items[index % items.length]
    const fallback = window.setTimeout(
      () => setIndex((i) => (i + 1) % items.length),
      Math.max(current.durationMs || 8000, 1500)
    )
    timerRef.current = fallback
    return () => {
      if (timerRef.current !== null) window.clearTimeout(timerRef.current)
    }
  }, [items, index])

  if (items.length === 0) {
    return (
      <div className="player-empty">
        <div className="player-empty-title">等待内容</div>
        <div className="player-empty-sub">素材与排期由管理端配置，配置后约 30 秒内自动开始轮播</div>
      </div>
    )
  }

  const current = items[index % items.length]
  return (
    <div className="player-stage">
      {current.type === 2 ? (
        <video
          key={`${mediaVersion}-${current.materialId}-${index}`}
          className="player-media"
          src={current.url}
          autoPlay
          muted
          playsInline
          onEnded={() => setIndex((i) => (i + 1) % items.length)}
          onError={() => setIndex((i) => (i + 1) % items.length)}
        />
      ) : (
        <img
          key={`${mediaVersion}-${current.materialId}-${index}`}
          className="player-media"
          src={current.url}
          alt={current.name}
          onError={() => setIndex((i) => (i + 1) % items.length)}
        />
      )}
      <div className="player-pager">
        {items.map((_, i) => (
          <i key={i} className={i === index % items.length ? 'on' : ''} />
        ))}
      </div>
    </div>
  )
}

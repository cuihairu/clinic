import { useEffect, useRef, useState } from 'react'
import type { Playlist } from '../types'

/**
 * 拉取播放列表。同源部署（Nginx 反代 /api）；本地 dev 走 vite proxy。
 * 网络失败返回 null——播放端继续播缓存序列，不打断（离线容错见 T7）。
 */
export async function fetchPlaylist(screen: string): Promise<Playlist | null> {
  try {
    const res = await fetch(`/api/v1/ads/playlist?screen=${encodeURIComponent(screen)}`, {
      headers: { Accept: 'application/json' }
    })
    if (!res.ok) {
      console.warn('[tablet] playlist 非 2xx：', res.status)
      return null
    }
    return (await res.json()) as Playlist
  } catch (error) {
    console.warn('[tablet] playlist 拉取失败（离线？）：', error)
    return null
  }
}

/**
 * 轮询播放列表：首次立即拉，之后每 intervalMs 一次。
 * version 变化才更新 items（媒体重载由 Carousel key 保证）；拉取失败保留上一份。
 */
export function usePlaylist(screen: string, intervalMs = 30_000) {
  const [playlist, setPlaylist] = useState<Playlist | null>(null)
  const [online, setOnline] = useState<boolean>(navigator.onLine)
  const versionRef = useRef<string | null>(null)

  useEffect(() => {
    let alive = true
    const pull = async () => {
      const next = await fetchPlaylist(screen)
      if (!alive) return
      setOnline(navigator.onLine)
      if (next === null) return
      if (next.version !== versionRef.current) {
        versionRef.current = next.version
        setPlaylist(next)
      }
    }
    pull()
    const timer = setInterval(pull, intervalMs)
    const onOnline = () => pull()
    window.addEventListener('online', onOnline)
    return () => {
      alive = false
      clearInterval(timer)
      window.removeEventListener('online', onOnline)
    }
  }, [screen, intervalMs])

  return { playlist, online }
}

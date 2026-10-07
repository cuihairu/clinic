import { useEffect, useRef, useState } from 'react'
import type { CallsLatest, Playlist, QueueCallItem } from '../types'

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
    const onOnline = () => {
      setOnline(true)
      pull()
    }
    const onOffline = () => setOnline(false)
    window.addEventListener('online', onOnline)
    window.addEventListener('offline', onOffline)
    return () => {
      alive = false
      clearInterval(timer)
      window.removeEventListener('online', onOnline)
      window.removeEventListener('offline', onOffline)
    }
  }, [screen, intervalMs])

  return { playlist, online }
}

/**
 * 游标拉取新叫号；失败返回 null（与 playlist 同口径，不打断展示）。
 */
export async function fetchCallsLatest(screen: string, since: number): Promise<CallsLatest | null> {
  try {
    const res = await fetch(
      `/api/v1/calls/latest?screen=${encodeURIComponent(screen)}&since=${since}`,
      { headers: { Accept: 'application/json' } }
    )
    if (!res.ok) {
      console.warn('[tablet] calls 非 2xx：', res.status)
      return null
    }
    return (await res.json()) as CallsLatest
  } catch (error) {
    console.warn('[tablet] calls 拉取失败（离线？）：', error)
    return null
  }
}

/**
 * 轮询新叫号（默认 1 秒一次，保证叫号后约 1 秒内上屏）：内部维持游标与待播队列。
 * current 是正在全屏提示的一条；dismiss() 后自动换队列里的下一条。
 * 启动后的第一次成功拉取只对齐游标不播历史，避免重启屏把旧叫号重播一遍。
 */
export function useCallPolling(screen: string, intervalMs = 1_000) {
  const [current, setCurrent] = useState<QueueCallItem | null>(null)
  // current 的镜像：队列出入全走 ref，避开在 setState updater 里做副作用
  //（StrictMode 开发态会双跑 updater，副作用的 updater 会被执行两次）
  const currentRef = useRef<QueueCallItem | null>(null)
  const queueRef = useRef<QueueCallItem[]>([])
  const sinceRef = useRef(0)
  const syncedRef = useRef(false)

  const showNext = () => {
    const nextItem = queueRef.current.shift() ?? null
    currentRef.current = nextItem
    setCurrent(nextItem)
  }

  useEffect(() => {
    let alive = true
    const pull = async () => {
      const next = await fetchCallsLatest(screen, sinceRef.current)
      if (!alive || next === null) return
      sinceRef.current = next.since
      if (!syncedRef.current) {
        syncedRef.current = true
        return
      }
      if (next.calls.length > 0) {
        queueRef.current.push(...next.calls)
        if (currentRef.current === null) showNext()
      }
    }
    pull()
    const timer = setInterval(pull, intervalMs)
    return () => {
      alive = false
      clearInterval(timer)
    }
  }, [screen, intervalMs])

  const dismiss = () => {
    showNext()
  }
  return { current, dismiss }
}

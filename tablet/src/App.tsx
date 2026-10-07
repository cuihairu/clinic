import { useEffect, useState } from 'react'
import { useCallPolling, usePlaylist } from './net/api'
import { precacheMedia } from './net/precache'
import { CallOverlay } from './player/CallOverlay'
import { Carousel } from './player/Carousel'

/** Kiosk 屏标识：URL ?screen=PAD-01；没配就停在配置提示页 */
function readScreenCode(): string | null {
  const params = new URLSearchParams(window.location.search)
  const code = params.get('screen')
  if (code && code.trim()) {
    localStorage.setItem('tablet.screen', code.trim())
    return code.trim()
  }
  return localStorage.getItem('tablet.screen')
}

/** Wake Lock 尽力而为：部分平板浏览器不支持，不支持就不管 */
function useWakeLock() {
  useEffect(() => {
    let lock: { release(): Promise<void> } | null = null
    const acquire = async () => {
      try {
        if ('wakeLock' in navigator && navigator.wakeLock?.request) {
          lock = await navigator.wakeLock.request('screen')
        }
      } catch {
        /* 拿不到锁就让它正常休眠策略生效 */
      }
    }
    acquire()
    const reacquire = () => {
      if (document.visibilityState === 'visible') acquire()
    }
    document.addEventListener('visibilitychange', reacquire)
    return () => {
      document.removeEventListener('visibilitychange', reacquire)
      lock?.release().catch(() => {})
    }
  }, [])
}

/** 首次点击进全屏（浏览器要求手势触发）；再点保持全屏不退出 */
function useFullscreenOnTap() {
  useEffect(() => {
    const onTap = () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen({ navigationUI: 'hide' }).catch(() => {})
      }
    }
    window.addEventListener('pointerdown', onTap)
    return () => window.removeEventListener('pointerdown', onTap)
  }, [])
}

export default function App() {
  const [screen] = useState(readScreenCode)
  useFullscreenOnTap()

  if (!screen) {
    return (
      <div className="player-empty">
        <div className="player-empty-title">缺少屏标识</div>
        <div className="player-empty-sub">
          请以 /tablet/?screen=屏标识 打开（屏标识在管理端「屏管理」里创建）
        </div>
      </div>
    )
  }

  return <PlayerScreen screen={screen} />
}

function PlayerScreen({ screen }: { screen: string }) {
  const { playlist, online } = usePlaylist(screen)
  const { current: call, dismiss } = useCallPolling(screen)
  useWakeLock()

  // 内容戳变化 → 把新序列的全部媒体交给 SW 预缓存，断网可续播
  useEffect(() => {
    if (!playlist || playlist.items.length === 0) return
    precacheMedia(playlist.items.map((item) => item.url))
  }, [playlist?.version])

  if (!playlist) {
    return (
      <div className="player-empty">
        <div className="player-empty-title">{online ? '连接服务中…' : '网络已断开，等待恢复'}</div>
        <div className="player-empty-sub">屏标识：{screen}</div>
      </div>
    )
  }

  return (
    <>
      <Carousel key={playlist.code} items={playlist.items} mediaVersion={playlist.version} />
      {call && <CallOverlay call={call} holdMs={15_000} onDone={dismiss} />}
      {!online && <div className="player-offline-badge">离线</div>}
    </>
  )
}

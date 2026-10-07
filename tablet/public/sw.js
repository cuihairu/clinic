/*
 * Kiosk 媒体预缓存：/media/** 走「缓存优先 + 后台刷新」。
 * 主线程在 playlist version 变化时 postMessage 素材清单，这里逐个补缓存；
 * 断网时播放端从缓存取媒体继续轮播（离线容错见 docs/design/tablet.md T6/T7）。
 */
const CACHE = 'ads-media-v1'

self.addEventListener('install', () => {
  self.skipWaiting()
})

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim())
})

self.addEventListener('message', (event) => {
  if (event.data?.type !== 'precache' || !Array.isArray(event.data.urls)) return
  event.waitUntil(
    (async () => {
      const cache = await caches.open(CACHE)
      await Promise.all(
        event.data.urls.map(async (url) => {
          try {
            if (await cache.match(url)) return
            await cache.add(new Request(url, { cache: 'reload' }))
          } catch (error) {
            console.warn('[sw] 预缓存失败：', url, error)
          }
        })
      )
    })()
  )
})

self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url)
  if (event.request.method !== 'GET' || !url.pathname.startsWith('/media/')) return

  event.respondWith(
    (async () => {
      const cache = await caches.open(CACHE)
      const hit = await cache.match(event.request)
      if (hit) {
        // 有缓存先回，网络空闲时刷新一份
        event.waitUntil(
          (async () => {
            try {
              const fresh = await fetch(event.request)
              if (fresh.ok) await cache.put(event.request, fresh.clone())
            } catch {
              /* 离线时静默，继续用缓存 */
            }
          })()
        )
        return hit
      }
      try {
        const fresh = await fetch(event.request)
        if (fresh.ok) await cache.put(event.request, fresh.clone())
        return fresh
      } catch {
        return new Response('', { status: 504 })
      }
    })()
  )
})

/** 把当前 version 的全部媒体地址交给 Service Worker 预缓存（不支持 SW 时静默跳过） */
export function precacheMedia(urls: string[]) {
  if (!('serviceWorker' in navigator)) return
  navigator.serviceWorker.controller?.postMessage({ type: 'precache', urls })
}

export function registerServiceWorker() {
  if (!('serviceWorker' in navigator)) return
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/tablet/sw.js').catch((error) => {
      console.warn('[tablet] SW 注册失败（预缓存与断网续播不可用）：', error)
    })
  })
}

import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// Kiosk 全屏页：本地开发把 /api 与 /media 代理到服务端
// （默认 2347，可用 SERVER_BASE 覆盖；生产由 Nginx 同源反代，无需代理）。
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.SERVER_BASE || 'http://localhost:2347'
  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': { target, changeOrigin: true },
        '/media': { target, changeOrigin: true }
      }
    },
    base: '/tablet/'
  }
})

module.exports = {
  env: {
    NODE_ENV: '"development"'
  },
  defineConstants: {},
  mini: {},
  h5: {
    // H5 开发态把 /api 与 /media 代理到本地服务端（SERVER_BASE 覆盖），
    // 页面相对路径请求免跨域；生产由白标 apiBase 直连。
    devServer: {
      proxy: {
        '/api': { target: process.env.SERVER_BASE || 'http://localhost:2347', changeOrigin: true },
        '/media': { target: process.env.SERVER_BASE || 'http://localhost:2347', changeOrigin: true }
      }
    }
  }
}

export default defineAppConfig({
  pages: [
    'pages/index/index'
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: process.env.TARO_APP_BRAND_THEME,
    navigationBarTitleText: process.env.TARO_APP_BRAND_NAME,
    navigationBarTextStyle: 'black'
  }
})

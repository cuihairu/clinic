export default defineAppConfig({
  pages: [
    'pages/index/index',
    'pages/booking/index'
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: process.env.TARO_APP_BRAND_THEME,
    navigationBarTitleText: process.env.TARO_APP_BRAND_NAME,
    navigationBarTextStyle: 'black'
  }
})

/// <reference types="@tarojs/taro" />

declare module '*.png';
declare module '*.gif';
declare module '*.jpg';
declare module '*.jpeg';
declare module '*.svg';
declare module '*.css';
declare module '*.less';
declare module '*.scss';
declare module '*.sass';
declare module '*.styl';

declare namespace NodeJS {
  interface ProcessEnv {
    TARO_ENV: 'weapp' | 'swan' | 'alipay' | 'h5' | 'rn' | 'tt' | 'quickapp' | 'qq' | 'jd'
    /** 白标品牌参数，构建期由 config/index.ts 注入（DefinePlugin 替换） */
    TARO_APP_BRAND_NAME: string
    TARO_APP_BRAND_THEME: string
    TARO_APP_API_BASE: string
    TARO_APP_BRAND_ICON: string
  }
}



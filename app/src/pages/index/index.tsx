import React from 'react'
import { View, Image } from '@tarojs/components'
import { BRAND } from '../../services/brand'
import './index.css'

export default function Index() {
  return (
    <View className="brand-page">
      <View className="brand-head">
        {BRAND.iconData ? (
          <Image className="brand-icon" src={BRAND.iconData} mode="aspectFit" />
        ) : null}
        <View className="brand-name">{BRAND.name}</View>
        <View className="brand-sub">白标构建参数自检页</View>
      </View>

      <View className="brand-card">
        <View className="brand-row">
          <View className="brand-label">主题色</View>
          <View className="brand-value">
            <View className="brand-swatch" style={{ background: BRAND.themeColor }} />
            <View className="brand-mono">{BRAND.themeColor}</View>
          </View>
        </View>
        <View className="brand-row">
          <View className="brand-label">后端域名</View>
          <View className="brand-value">
            <View className="brand-mono">{BRAND.apiBase}</View>
          </View>
        </View>
        <View className="brand-row">
          <View className="brand-label">页内图标</View>
          <View className="brand-value">
            <View className="brand-mono">{BRAND.iconData ? '已注入（商家 icon）' : '未注入位图'}</View>
          </View>
        </View>
      </View>

      <View className="brand-tip">
        以上参数由构建脚本注入，改参数不改代码；换商家出包见
        仓库 docs/app-white-label.md。
      </View>
    </View>
  )
}

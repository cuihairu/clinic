import React, { useEffect, useState } from 'react'
import { View, Image, Text } from '@tarojs/components'
import { BRAND } from '../../services/brand'
import { fetchHomeItems } from '../../services/api'
import type { HomeItem } from '../../services/api'
import './index.css'

/** 按真实时段问候（顾客姓名无接口，不带称呼不造假） */
function greeting(): string {
  const h = new Date().getHours()
  if (h < 5) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
}

/** 规划功能标记：无接口的区块如实标注，不放假数据（口径同管理端 plan-note） */
function PlanPill() {
  return <Text className="plan-pill">规划功能</Text>
}

export default function Index() {
  const [items, setItems] = useState<HomeItem[] | null>(null)
  const [failed, setFailed] = useState(false)

  const load = () => {
    setFailed(false)
    fetchHomeItems()
      .then((next) => setItems(next))
      .catch(() => setFailed(true))
  }

  useEffect(load, [])

  return (
    <View className="home-page">
      {/* 品牌头：商家图标优先，无位图以金棕「养」印兜底 */}
      <View className="brand-head">
        <View className="seal">
          {BRAND.iconData ? <Image className="seal-img" src={BRAND.iconData} mode="aspectFit" /> : '养'}
        </View>
        <View className="bt">
          <View className="bn">{BRAND.name}</View>
          <View className="bg">{greeting()}，欢迎光临</View>
        </View>
      </View>

      {/* 预约横幅：接口为规划域，横幅样式先行、功能如实标注 */}
      <View className="appt">
        <View className="ic">预</View>
        <View className="appt-bd">
          <View className="tt">
            在线预约 <PlanPill />
          </View>
          <View className="ss">上线后可在这里查看预约、到店免等待</View>
        </View>
      </View>

      {/* 今日宜养：实拉上架项目（/api/v1/kiosk/items，与自助机同一份真实数据） */}
      <View className="sec">
        <Text className="sec-h3">今日宜养</Text>
      </View>
      {failed ? (
        <View className="plan-note">
          项目加载失败
          <Text className="retry" onClick={load}>重试</Text>
        </View>
      ) : items === null ? (
        <View className="plan-note">项目加载中…</View>
      ) : items.length === 0 ? (
        <View className="plan-note">暂无上架项目，请到店咨询</View>
      ) : (
        <View className="items">
          {items.map((item, i) => (
            <View className="item" key={item.id}>
              <View className={`cover ${i % 2 === 0 ? 'g1' : 'g2'}`}>
                {item.cover ? (
                  <Image className="cover-img" src={item.cover} mode="aspectFill" />
                ) : (
                  <Text className="cover-zi">{item.name.slice(0, 1)}</Text>
                )}
              </View>
              <View className="bd">
                <View className="nm">{item.name}</View>
                {item.description ? <View className="ds">{item.description}</View> : null}
                <View className="pr">
                  ¥{item.price} <Text className="pr-u">/ 次</Text>
                </View>
              </View>
            </View>
          ))}
        </View>
      )}

      {/* 我的卡项：顾客购卡/余次无接口，如实规划占位 */}
      <View className="sec">
        <Text className="sec-h3">我的卡项</Text>
        <PlanPill />
      </View>
      <View className="plan-note">购卡与余次查询为规划功能，上线后将在这里展示您的卡项与剩余次数。</View>

      {/* 养生贴士：静态编辑内容 */}
      <View className="sec">
        <Text className="sec-h3">养生贴士</Text>
      </View>
      <View className="tip">
        <Text className="tip-b">秋燥宜润肺</Text>
        <Text className="tip-t">少辛增酸，早卧早起；艾灸后两小时勿碰冷水。</Text>
      </View>

      {/* 白标自检（保留工程验收用，挪至页底） */}
      <View className="sec">
        <Text className="sec-h3">白标自检</Text>
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
        以上参数由构建脚本注入，改参数不改代码；换商家出包见仓库 docs/app-white-label.md。
      </View>
    </View>
  )
}

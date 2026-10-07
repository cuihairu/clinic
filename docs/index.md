---
layout: home

hero:
  name: Sinomed
  text: 中医医馆管理系统
  tagline: 围绕中医医馆日常接诊设计：顾客建档、中医诊疗记录（四诊 / 脉象 / 五行 / 取穴 / 诊断与调理方案）、卡项、复盘与回访。服务端、管理端与小程序端一套仓库。
  actions:
    - theme: brand
      text: 快速上手
      link: /guide/getting-started
    - theme: alt
      text: 服务端接口
      link: /server/api

features:
  - icon:
      src: /icons/stethoscope.svg
      alt: 诊疗
    title: 顾客与诊疗记录
    details: 顾客建档、查询、更新；诊疗单覆盖主诉、望闻问切、脉象（左右手）、五行生克、取穴、诊断与调理方案，复诊可调阅历史。
  - icon:
      src: /icons/receipt-yuan.svg
      alt: 卡项
    title: 卡项与会员
    details: 门店卡项（服务项目）的创建、查询与维护；顾客档案带会员等级，为会员运营与订单打基础。
  - icon:
      src: /icons/report-analytics.svg
      alt: 复盘
    title: 复盘与回访
    details: 每日总结按天沉淀，员工复盘与顾客回访按日汇总；回访到期待办自动出现在每日报表。
  - icon:
      src: /icons/prescription.svg
      alt: 规划
    title: 医馆板块规划
    details: 挂号、处方、中药房、收费等门诊板块在规划中，源码尚未实现；分层规划与参考来源见「调研 · 功能点清单」。
  - icon:
      src: /icons/device-desktop.svg
      alt: 桌面
    title: 桌面工作站（规划）
    details: 前台 / 医生工作站的 Windows 薄壳方案（Tauri 2）：打印处方笺、扫码定位顾客，业务仍在服务端。源码尚未实现。
  - icon:
      src: /icons/device-tablet.svg
      alt: 平板
    title: 广告展示端
    details: 候诊区平板 Kiosk：广告与科普轮播、前台叫号 1 秒级上屏、断网续播；素材 / 排期 / 屏 / 叫号在管理端配置。见「设计 · 平板展示」。
  - icon:
      src: /icons/api.svg
      alt: 接口
    title: 接口与文档
    details: REST 接口统一前缀 /api/v1，内置 Knife4j 中文接口文档（doc.html），JWT 登录鉴权。
  - icon:
      src: /icons/inventory.svg
      alt: 部署
    title: 容器化部署
    details: 提供 Dockerfile 与 docker-compose 编排（MySQL + 服务端 + Nginx），亦支持 systemd 裸机部署。
---

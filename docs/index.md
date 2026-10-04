---
layout: home

hero:
  name: Sinomed
  text: 中医医馆管理系统
  tagline: 围绕中医门诊日常运转设计：顾客建档、中医诊疗记录（四诊 / 脉象 / 取穴）、卡项、每日报表与回访、员工考勤，服务端、管理端与小程序端一套仓库。
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
    details: 顾客建档、查询、更新；诊疗单覆盖主诉、望闻问切、脉象（左右手）、五行生克、取穴、诊断与调理方案。
  - icon:
      src: /icons/report-analytics.svg
      alt: 报表
    title: 每日报表与回访
    details: 每日总结按天沉淀，员工复盘与顾客回访按日汇总；回访到期待办自动出现在每日报表。
  - icon:
      src: /icons/schedule.svg
      alt: 考勤
    title: 员工与考勤
    details: 员工账号与角色管理、上/下班签到，按天与按月自动汇总工作时长。
  - icon:
      src: /icons/receipt-yuan.svg
      alt: 卡项
    title: 卡项管理
    details: 门店卡项（服务项目）的创建、查询与维护，为订单与耗卡打基础。
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

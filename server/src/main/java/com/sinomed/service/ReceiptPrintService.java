package com.sinomed.service;

/**
 * 结算小票套打：按结算单填充 80mm 小票模板（receipt），返回可直接打印的 HTML。
 * 口径：单号 S+结算单 id；次卡抵扣（实收 0）金额列显示「次卡抵扣」；金额单位元。
 */
public interface ReceiptPrintService {

    /** 渲染结算小票 HTML；结算单不存在时抛 IllegalArgumentException */
    String render(Long settlementId);
}

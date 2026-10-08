package com.sinomed.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 顾客消费汇总：顾客档案「累计消费」统计卡的数据源，只统计已完成（status=2）的订单。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "顾客消费汇总")
public class OrderSummaryView {
    @Schema(title = "已完成订单数", example = "3")
    private Long orders;

    @Schema(title = "累计消费金额（元，已完成订单价格合计）", example = "276")
    private Long amount;
}

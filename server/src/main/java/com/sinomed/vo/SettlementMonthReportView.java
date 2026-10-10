package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 月度收费报表：按结算时间聚合当月结算单——逐日单数/实收、支付方式构成。
 * 次卡核销（payType 5）计入单数、实收为 0，单列 cardCount 如实呈现。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "月度收费报表")
public class SettlementMonthReportView {

    @Schema(title = "报表月份 yyyy-MM", example = "2026-10")
    private String month;

    @Schema(title = "当月结算单总数（含次卡核销）", example = "7")
    private int totalCount;

    @Schema(title = "当月实收合计（元；次卡核销实收 0）", example = "3516")
    private int totalMoney;

    @Schema(title = "次卡核销单数（不产生实收）", example = "1")
    private int cardCount;

    @Schema(title = "有结算的日期，按日升序")
    private List<DayRow> days;

    @Schema(title = "支付方式构成（只列出现过的，按 payType 升序）")
    private List<PayRow> payTypes;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(title = "报表·单日")
    public static class DayRow {

        @Schema(title = "日期 yyyy-MM-dd", example = "2026-10-08")
        private String day;

        @Schema(title = "当日结算单数", example = "2")
        private int count;

        @Schema(title = "当日实收（元）", example = "878")
        private int money;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(title = "报表·支付方式")
    public static class PayRow {

        @Schema(title = "支付方式：1 储值 / 2 微信 / 3 支付宝 / 4 现金 / 5 次卡抵扣", example = "2")
        private int payType;

        @Schema(title = "支付方式文案", example = "微信")
        private String payTypeText;

        @Schema(title = "该方式结算单数", example = "2")
        private int count;

        @Schema(title = "该方式实收（元）", example = "878")
        private int money;
    }
}

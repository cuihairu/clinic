package com.sinomed.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 储值余额：recharges 流水合计（无流水为 0）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "储值余额")
public class RechargeBalanceView {
    @Schema(title = "顾客id", example = "8")
    private Long customerId;

    @Schema(title = "储值余额（元，可为 0）", example = "500")
    private Long balance;
}

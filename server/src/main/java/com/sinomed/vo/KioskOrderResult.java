package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Kiosk 下单回执：屏幕展示单号与合计。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "Kiosk 下单回执")
public class KioskOrderResult {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(title = "单条订单")
    public static class Line {
        @Schema(title = "订单id", example = "12")
        private Long id;

        @Schema(title = "卡项id", example = "3")
        private Long itemId;

        @Schema(title = "卡项名", example = "拔罐体验券")
        private String itemName;

        @Schema(title = "成交价（元，下单时刻快照）", example = "99")
        private Integer price;
    }

    @Schema(title = "顾客id（按手机号幂等）", example = "8")
    private Long customerId;

    @Schema(title = "顾客称呼", example = "到店客人")
    private String customerName;

    @Schema(title = "本次订单明细")
    private List<Line> orders;

    @Schema(title = "合计金额（元）", example = "198")
    private Integer totalFee;
}

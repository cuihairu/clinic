package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kiosk 自助约期回执：只含本次预约与顾客自身字段，不暴露横向数据（口径见 docs/design/app-booking.md §4）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "Kiosk 自助约期回执")
public class KioskAppointmentResult {
    @Schema(title = "顾客id（按手机号幂等）", example = "8")
    private Long customerId;

    @Schema(title = "顾客称呼", example = "到店客人")
    private String customerName;

    @Schema(title = "预约id", example = "15")
    private Long appointmentId;

    @Schema(title = "预约时段（yyyy-MM-dd HH:mm）", example = "2026-10-12 10:00")
    private String startTime;

    @Schema(title = "卡项名（提交时未选则为空）", example = "艾灸")
    private String itemName;

    @Schema(title = "状态（0 待到店）", example = "0")
    private Integer status;
}

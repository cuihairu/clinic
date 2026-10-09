package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kiosk 自助约期请求（免登录）：按手机号找/建顾客，落一条待到店预约。
 * 口径见 docs/design/app-booking.md（频控 1 条/日/手机号、整点 09:00–17:00、时长固定 60 分钟）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "Kiosk 自助约期请求")
public class KioskAppointmentRequest {
    @NotNull
    @Schema(title = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800001111")
    private String phone;

    @Schema(title = "称呼（可空，建档时用；不填默认「到店客人」）", example = "张女士")
    private String name;

    @Schema(title = "卡项id（可空，到店再定）", example = "3")
    private Long itemId;

    @NotNull
    @Schema(title = "预约时段（yyyy-MM-dd HH:mm 整点，须晚于当前时刻）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-12 10:00")
    private String startTime;
}

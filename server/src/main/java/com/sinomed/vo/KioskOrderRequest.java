package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Kiosk 下单请求：按手机号找/建顾客，itemIds 逐项落一条订单（status=0、价格取卡项现价）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "Kiosk 下单请求")
public class KioskOrderRequest {
    @NotNull
    @Schema(title = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800001111")
    private String phone;

    @Schema(title = "称呼（可空，建档时用；不填默认「到店客人」）", example = "张女士")
    private String name;

    @NotEmpty
    @Schema(title = "卡项id列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[3, 5]")
    private List<Long> itemIds;
}

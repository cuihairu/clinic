package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 配伍审方请求：药材名列表（自由文本，与处方药味同口径）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "配伍审方请求")
public class CompatibilityCheckView {
    @Schema(title = "药材名列表", example = "[\"附子\", \"半夏\"]")
    private List<String> herbs;
}

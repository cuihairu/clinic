package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 处方计价：按药材字典实时计算（不落库、不做快照），字典比价用精确同名匹配。
 * 未收录药名如实列出不计费，totalFen 只含已比价药味。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "处方计价")
public class PricingView {
    @Schema(title = "总价（分）= 单剂价 × 剂数，仅含已比价药味", example = "1234")
    private long totalFen;

    @Schema(title = "单剂价（分）", example = "176")
    private long perDoseFen;

    @Schema(title = "剂数", example = "7")
    private int doses;

    @Schema(title = "药味总数（非空白）", example = "7")
    private int herbCount;

    @Schema(title = "已比价药味数", example = "6")
    private int pricedHerbCount;

    @Schema(title = "未收录药名（原文名，不计费）")
    private List<String> unknownHerbs;
}

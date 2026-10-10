package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 过敏审方入参：按顾客的过敏史比对当前药味（提示不拦截）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "过敏审方入参")
public class AllergyCheckView {

    @Schema(title = "顾客id（必填，过敏史来源）", example = "1")
    private Long customerId;

    @Schema(title = "药味名单（必填，自由文本）", example = "[\"熟地黄\", \"阿胶\"]")
    private List<String> herbs;
}

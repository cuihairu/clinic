package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 过敏审方结果：findings 为空即顾客过敏史未提到当前药味。提示不拦截，是否照用由医师判断。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "过敏审方结果")
public class AllergyResultView {

    @Schema(title = "顾客id", example = "1")
    private Long customerId;

    @Schema(title = "参与比对的药材数（去空白后）", example = "8")
    private int checked;

    @Schema(title = "命中清单，空 = 过敏史未提到当前药味")
    private List<Finding> findings;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(title = "过敏提示")
    public static class Finding {
        @Schema(title = "命中的药材原文", example = "阿胶")
        private String herb;

        @Schema(title = "命中的过敏史记录id", example = "4")
        private Long historyId;

        @Schema(title = "过敏史原文（含药名）", example = "阿胶、蜂蜜过敏")
        private String content;
    }
}

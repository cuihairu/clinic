package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 配伍审方结果：findings 为空即未发现十八反/十九畏同用。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "配伍审方结果")
public class CompatibilityResultView {

    @Schema(title = "参与比对的药材数（去空白后）", example = "8")
    private int checked;

    @Schema(title = "命中清单，空 = 未发现配伍禁忌")
    private List<Finding> findings;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(title = "配伍提示")
    public static class Finding {
        @Schema(title = "级别：禁忌（十八反）/ 慎用（十九畏）", example = "禁忌")
        private String level;

        @Schema(title = "规则组名", example = "十八反·乌头组")
        private String rule;

        @Schema(title = "配伍侧 A（命中的药材原文）", example = "附子")
        private String a;

        @Schema(title = "配伍侧 B（命中的药材原文）", example = "法半夏")
        private String b;

        @Schema(title = "歌诀出处/说明", example = "半蒌贝蔹及攻乌")
        private String note;
    }
}

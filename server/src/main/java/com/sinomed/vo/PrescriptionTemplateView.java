package com.sinomed.vo;


import com.sinomed.entity.PrescriptionTemplateEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 病症处方模板视图：字段与 prescription_templates 表对齐；herbs 为详情联出的药味。
 * 模板只存建议值，套用后随开方单自由改（不改回模板）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "病症处方模板")
public class PrescriptionTemplateView {
    @Schema(title = "模板id", example = "2")
    private Long id;

    @Schema(title = "病症名（唯一，如「风寒感冒」）", example = "风寒感冒")
    private String name;

    @Schema(title = "建议剂数（默认 7）", example = "7")
    private Integer doses;

    @Schema(title = "建议代煎：0 无需 / 1 代煎", example = "0")
    private Integer decoction;

    @Schema(title = "建议用法：煎服法/频次/代煎说明等", example = "水煎服，日一剂，早晚温服")
    private String usage;

    @Schema(title = "备注", example = "随证加减")
    private String remark;

    @Schema(title = "上架：0 停用 / 1 启用", example = "1")
    private Integer enabled;

    @Schema(title = "药味列表（详情联出）")
    private List<PrescriptionItemView> herbs;

    @Schema(title = "创建时间", example = "2026-10-09 11:00:00")
    private Date createTime;

    @Schema(title = "更新时间", example = "2026-10-09 11:00:00")
    private Date updateTime;

    public static PrescriptionTemplateView FromTemplateEntity(PrescriptionTemplateEntity entity) {
        return PrescriptionTemplateView.builder()
                .id(entity.getId())
                .name(entity.getName())
                .doses(entity.getDoses())
                .decoction(entity.getDecoction())
                .usage(entity.getUsage())
                .remark(entity.getRemark())
                .enabled(entity.getEnabled())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}

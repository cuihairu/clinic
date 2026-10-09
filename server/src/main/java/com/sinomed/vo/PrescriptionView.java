package com.sinomed.vo;


import com.sinomed.entity.PrescriptionEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 处方笺视图：字段与 prescriptions 表对齐；customerName/staffName/herbs 为列表与详情联出的展示字段。
 * MVP 无配伍审方与计价（药材无字典无价格），均为规划功能。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "中药处方笺")
public class PrescriptionView {
    @Schema(title = "处方id", example = "3")
    private Long id;

    @Schema(title = "接诊单id（可空）", example = "8")
    private Long treatId;

    @Schema(title = "顾客id", example = "5")
    private Long customerId;

    @Schema(title = "顾客名（列表联出）", example = "王女士")
    private String customerName;

    @Schema(title = "开方医师id（可空）", example = "2")
    private Long staffId;

    @Schema(title = "开方医师名（列表联出）", example = "沈中医师")
    private String staffName;

    @Schema(title = "剂数（几付）", example = "7")
    private Integer doses;

    @Schema(title = "用法：煎服法/频次/代煎说明等", example = "水煎服，日一剂，早晚温服")
    private String usage;

    @Schema(title = "备注", example = "代煎 7 袋")
    private String remark;

    @Schema(title = "药味列表（详情联出）")
    private List<PrescriptionItemView> herbs;

    @Schema(title = "开方时间", example = "2026-10-09 11:00:00")
    private Date createTime;

    public static PrescriptionView FromPrescriptionEntity(PrescriptionEntity entity) {
        return PrescriptionView.builder()
                .id(entity.getId())
                .treatId(entity.getTreatId())
                .customerId(entity.getCustomerId())
                .staffId(entity.getStaffId())
                .doses(entity.getDoses())
                .usage(entity.getUsage())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .build();
    }
}

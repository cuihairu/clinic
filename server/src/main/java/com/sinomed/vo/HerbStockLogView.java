package com.sinomed.vo;

import com.sinomed.entity.HerbStockLogEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 出入库流水视图：登记与回读。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "饮片出入库流水")
public class HerbStockLogView {

    @Schema(title = "流水 id")
    private Long id;

    @Schema(title = "药材 id")
    private Long herbId;

    @Schema(title = "药材名（联出）")
    private String herbName;

    @Schema(title = "流水类型：1 入库 / 0 出库")
    private Integer type;

    @Schema(title = "数量（克）")
    private Integer quantity;

    @Schema(title = "批次效期 yyyy-MM-dd（可空）")
    private String expiry;

    @Schema(title = "供货方（可空）")
    private String supplier;

    @Schema(title = "备注（可空）")
    private String note;

    @Schema(title = "登记时间 yyyy-MM-dd HH:mm:ss")
    private String createTime;

    @Schema(title = "更新时间 yyyy-MM-dd HH:mm:ss")
    private String updateTime;

    public static HerbStockLogView fromEntity(HerbStockLogEntity entity, String herbName) {
        return builder()
                .id(entity.getId())
                .herbId(entity.getHerbId())
                .herbName(herbName)
                .type(entity.getType())
                .quantity(entity.getQuantity())
                .expiry(entity.getExpiry() == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").format(entity.getExpiry()))
                .supplier(entity.getSupplier())
                .note(entity.getNote())
                .createTime(entity.getCreateTime() == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entity.getCreateTime()))
                .updateTime(entity.getUpdateTime() == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entity.getUpdateTime()))
                .build();
    }
}

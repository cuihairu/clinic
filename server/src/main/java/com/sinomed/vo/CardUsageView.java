package com.sinomed.vo;

import com.sinomed.entity.CardUsageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 次卡核销记录视图：staffName 为列表联出展示字段。
 */
@Builder
@Data
@Schema(title = "次卡核销记录")
public class CardUsageView {
    @Schema(title = "记录id", example = "1")
    private Long id;

    @Schema(title = "持卡id", example = "3")
    private Long cardId;

    @Schema(title = "订单id（补录口径可空）", example = "12")
    private Long orderId;

    @Schema(title = "服务员工id（可空）", example = "2")
    private Long staffId;

    @Schema(title = "服务员工名（列表联出）", example = "沈师傅")
    private String staffName;

    @Schema(title = "本次是第几次消费（1 起）", example = "3")
    private Integer timesUsed;

    @Schema(title = "核销时间", example = "2026-10-10 10:00:00")
    private Date createTime;

    public static CardUsageView FromEntity(CardUsageEntity entity) {
        return CardUsageView.builder()
                .id(entity.getId())
                .cardId(entity.getCardId())
                .orderId(entity.getOrderId())
                .staffId(entity.getStaffId())
                .timesUsed(entity.getTimesUsed())
                .createTime(entity.getCreateTime())
                .build();
    }
}

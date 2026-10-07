package com.sinomed.vo;


import com.sinomed.entity.OrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 订单视图：字段与 orders 表对齐；customerName/itemName 是管理端列表联出的展示字段，落库不涉及。
 * 状态：0 已下单、1 已确认（接待中）、2 已完成、9 已取消（见 docs/design/kiosk.md）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "订单信息")
public class OrderView {
    @Schema(title = "订单id", example = "12")
    private Long id;

    @Schema(title = "顾客id", example = "8")
    private Long customerId;

    @Schema(title = "顾客名（列表联出）", example = "王女士")
    private String customerName;

    @Schema(title = "顾客手机号（列表联出）", example = "13700003333")
    private String customerPhone;

    @Schema(title = "卡项id", example = "3")
    private Long itemId;

    @Schema(title = "卡项名（列表联出）", example = "拔罐体验券")
    private String itemName;

    @Schema(title = "接待员工id（Kiosk 单为空）", example = "1")
    private Long staffId;

    @Schema(title = "状态：0 已下单、1 已确认、2 已完成、9 已取消", example = "0")
    private Integer status;

    @Schema(title = "成交价（元，下单时刻卡项价格快照）", example = "99")
    private Integer price;

    @Schema(title = "下单时间", example = "2026-10-07 15:00:00")
    private Date createTime;

    @Schema(title = "更新时间", example = "2026-10-07 15:30:00")
    private Date updateTime;

    public static OrderView FromOrderEntity(OrderEntity entity) {
        return OrderView.builder()
                .id(entity.getId())
                .customerId(entity.getUserId())
                .itemId(entity.getItemId())
                .staffId(entity.getStaffId())
                .status(entity.getStatus())
                .price(entity.getPrice())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}

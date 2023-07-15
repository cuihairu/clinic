package com.clinic.vo;


import com.clinic.entity.ItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Builder
@Data
@Schema(title = "商品信息")
public class ItemView {
    @Schema(title = "商品id",description = "商品id",example = "1234567890")
    private Long id;

    @NotNull
    @Schema(title = "商品名字",description = "商品名字，不可以重复",example = "中医诊断",requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull
    @Schema(title = "商品价格",description = "商品价格，必填，没有写0",example = "1000",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer price;

    @NotNull
    @Schema(title = "商品描述",description = "商品描述，一个简介",example = "妙手回春",requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(title = "商品创建时间",description = "商品的创建时间",example = "2021-01-01 12:00:00")
    private Date createTime;

    @Schema(title = "商品更新时间",description = "商品更新时间",example = "2023-01-01 12:00:00")
    private Date updateTime;

    public ItemEntity ToItemEntity(){
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setId(id);
        itemEntity.setName(name);
        itemEntity.setPrice(price);
        itemEntity.setDescription(description);
        return itemEntity;
    }

    public static ItemView FromItemEntity(ItemEntity itemEntity){
        return ItemView.builder()
                .id(itemEntity.getId())
                .name(itemEntity.getName())
                .price(itemEntity.getPrice())
                .description(itemEntity.getDescription())
                .createTime(itemEntity.getCreateTime())
                .updateTime(itemEntity.getUpdateTime())
                .build();
    }
}

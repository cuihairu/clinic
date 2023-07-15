package com.clinic.vo;

import com.clinic.entity.ReviewCustomerEntity;
import com.clinic.entity.ReviewStaffEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
@Schema(title = "顾客反馈信息")
public class ReviewCustomerView {
    @Schema(title = "复盘id",description = "复盘id",example = "1234567890")
    Long id;
    @Schema(title = "顾客id",description = "顾客id",example = "1234567890")
    Long customerId;
    @NotNull
    @Schema(title = "员工名字",description = "员工名字",example = "张三")
    String name;
    @Schema(title = "最近访问",description = "最近访问",example = "2023-1-2")
    Date last;
    @NotNull
    @Schema(title = "复盘时间",description = "复盘时间",example = "2023-01-01 12:00:00")
    Date day;
    @Schema(title = "复盘内容",description = "复盘内容",example = "今天很开心，成单80单")
    String advice;
    @Schema(title = "创建时间",description = "创建时间",example = "2023-1-1 12:00:00")
    Date createTime;
    @Schema(title = "更新时间",description = "最近修改时间",example = "2023-1-1 12:00:00")
    Date updateTime;

    public ReviewCustomerEntity toReviewCustomerEntity(){
        ReviewCustomerEntity entity = new ReviewCustomerEntity();
        entity.setId(id);
        entity.setCustomerId(customerId);
        entity.setName(name);
        entity.setLast(last);
        entity.setDay(day);
        entity.setAdvice(advice);
        entity.setCreateTime(createTime);
        entity.setUpdateTime(updateTime);
        return entity;
    }
    public static  ReviewCustomerView fromReviewCustomerEntity(ReviewCustomerEntity reviewCustomerEntity){
        ReviewCustomerView.ReviewCustomerViewBuilder builder = ReviewCustomerView.builder();
        builder.id(reviewCustomerEntity.getId());
        builder.customerId(reviewCustomerEntity.getCustomerId());
        builder.name(reviewCustomerEntity.getName());
        builder.last(reviewCustomerEntity.getLast());
        builder.day(reviewCustomerEntity.getDay());
        builder.advice(reviewCustomerEntity.getAdvice());
        builder.createTime(reviewCustomerEntity.getCreateTime());
        builder.updateTime(reviewCustomerEntity.getUpdateTime());
        return builder.build();
    }
}

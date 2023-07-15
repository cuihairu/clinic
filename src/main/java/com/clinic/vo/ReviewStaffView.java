package com.clinic.vo;

import com.clinic.entity.ReviewStaffEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
@Schema(title = "员工复盘信息")
public class ReviewStaffView {
    @Schema(title = "复盘id",description = "复盘id",example = "1234567890")
    Long id;
    @Schema(title = "员工id",description = "员工id",example = "1234567890")
    Long staffId;
    @NotNull
    @Schema(title = "员工名字",description = "员工名字",example = "张三")
    String name;
    @Schema(title = "耗卡",description = "耗卡",example = "10000")
    Integer cost;

    @NotNull
    @Schema(title = "复盘时间",description = "复盘时间",example = "2023-01-01 12:00:00")
    Date day;
    @Schema(title = "复盘内容",description = "复盘内容",example = "今天很开心，成单80单")
    String advice;
    @Schema(title = "卡项已做",description = "卡项已做",example = "韩式半永久")
    String done;
    @Schema(title = "创建时间",description = "创建时间",example = "2023-1-1 12:00:00")
    Date createTime;
    @Schema(title = "更新时间",description = "最近修改时间",example = "2023-1-1 12:00:00")
    Date updateTime;

    public ReviewStaffEntity toReviewStaffEntity(){
        ReviewStaffEntity reviewStaffEntity = new ReviewStaffEntity();
        reviewStaffEntity.setId(id);
        reviewStaffEntity.setStaffId(staffId);
        reviewStaffEntity.setName(name);
        reviewStaffEntity.setCost(cost);
        reviewStaffEntity.setDay(day);
        reviewStaffEntity.setAdvice(advice);
        reviewStaffEntity.setDone(done);
        reviewStaffEntity.setCreateTime(createTime);
        reviewStaffEntity.setUpdateTime(updateTime);
        return reviewStaffEntity;
    }
    public static  ReviewStaffView fromReviewStaffEntity(ReviewStaffEntity reviewStaffEntity){
        ReviewStaffViewBuilder builder = ReviewStaffView.builder();
        builder.id(reviewStaffEntity.getId());
        builder.staffId(reviewStaffEntity.getStaffId());
        builder.name(reviewStaffEntity.getName());
        builder.cost(reviewStaffEntity.getCost());
        builder.day(reviewStaffEntity.getDay());
        builder.advice(reviewStaffEntity.getAdvice());
        builder.done(reviewStaffEntity.getDone());
        builder.createTime(reviewStaffEntity.getCreateTime());
        builder.updateTime(reviewStaffEntity.getUpdateTime());
        return builder.build();
    }

}

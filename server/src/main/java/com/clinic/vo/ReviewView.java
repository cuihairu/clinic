package com.clinic.vo;


import com.clinic.entity.ReviewEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Builder
@Data
@Schema(title = "每日报表信息")
public class ReviewView {
    @Schema(title = "报表id",description = "报表id",example = "1234567890")
    private Long id;
    @NotNull
    @Schema(title = "报表时间",description = "日期，不包含具体时间，只精确到具体的天",example = "2023-01-01")
    private Date day;
    @Schema(title = "员工复盘信息",description = "员工复盘信息")
    private List<ReviewStaffView> reviewStaffViews;
    @Schema(title = "客户回访信息",description = "客户回访信息")
    private List<ReviewCustomerView> reviewCustomerView;
    @Schema(title = "做的好的事情",description = "做的好的事情",example = "今天收入过十万")
    private String good;
    @Schema(title = "做的不好的事情",description = "做的不好的事情",example = "门店太少，员工太少")
    private String improvement;

    public ReviewEntity toReviewEntity(){
        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setId(id);
        reviewEntity.setDay(day);
        reviewEntity.setGood(good);
        reviewEntity.setImprovement(improvement);
        return reviewEntity;
    }

    public static ReviewView fromReviewEntity(ReviewEntity entity){
        ReviewView.ReviewViewBuilder builder = ReviewView.builder();
        builder.id(entity.getId());
        builder.day(entity.getDay());
        builder.good(entity.getGood());
        builder.improvement(entity.getImprovement());
        return builder.build();
    }
}

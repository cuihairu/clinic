package com.clinic.vo;

import com.clinic.entity.SignEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
@Schema(title = "签到信息")
public class SignView {
    @Schema(title = "签到id",description = "签到索引id,数据库管理",example = "1234567890")
    Long id;
    @Schema(title = "staffId",description ="员工id" ,example = "1234567890")
    Long staffId;
    @Schema(title = "姓名",description ="姓名" ,example = "崔海涛")
    String name;
    @Schema(title = "签到时间",description = "签到时间",example = "2059-01-31 12:00:00")
    Date signTime;
    @Schema(title = "签到类型",description = "签到类型1.上班 0.下班",example = "1" )
    Integer signType;

    public static SignView FromSignEntity(SignEntity signEntity){
        return SignView.builder()
                .id(signEntity.getId())
                .staffId(signEntity.getStaffId())
                .signTime(signEntity.getUpdateTime())
                .signType(signEntity.getType())
                .build();
    }

    public SignEntity ToSignEntity(){
        SignEntity signEntity = new SignEntity();
        signEntity.setId(id);
        signEntity.setStaffId(staffId);
        signEntity.setUpdateTime(signTime);
        signEntity.setType(signType);
        return signEntity;
    }
}

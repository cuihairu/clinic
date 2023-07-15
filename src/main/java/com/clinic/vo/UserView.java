package com.clinic.vo;

import com.clinic.entity.StaffEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(title = "用户信息")
@Builder
@Data
public class UserView {
    @Schema(title = "name",description = "当前用户显示的名字",example = "崔海涛")
    String name;
    @Schema(title = "avatar",description = "当前用户显示的头像",example = "")
    String avatar;
    @Schema(title = "userid",description = "当前用户的登录名",example = "cui")
    String userid;
    @Schema(title = "signature",description = "当前用户的签名",example = "努力工作")
    String signature;
    @Schema(title = "title",description = "title",example = "努力工作")
    String title;
    @Schema(title = "group",description = "title",example = "努力工作")
    String group;
    @Schema(title = "notifyCount",description = "提示信息数量",example = "10")
    int notifyCount;
    @Schema(title = "unreadCount",description = "未读信息数量",example = "10")
    int unreadCount;
    @Schema(title = "country",description = "国家",example = "China")
    String country;
    @Schema(title = "access",description = "访问",example = "China")
    String access;
    @Schema(title = "address",description = "地址",example = "China")
    String address;
    @Schema(title = "phone",description = "手机号码",example = "13852299511")
    String phone;
    public static UserView FromStaffEntity(StaffEntity staffEntity){
        return UserView.builder()
                .name(staffEntity.getName())
                .avatar(staffEntity.getAvatar())
                .userid(staffEntity.getAccount())
                .signature("do well")
                .title("cui--------")
                .group("dev")
                .notifyCount(0)
                .unreadCount(0)
                .country("China")
                .access(staffEntity.getAccess())
                .address(staffEntity.getAddress())
                .phone(staffEntity.getPhone())
                .build();
    }
}

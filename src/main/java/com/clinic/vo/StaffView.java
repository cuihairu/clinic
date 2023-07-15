package com.clinic.vo;

import com.clinic.entity.StaffEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Schema(title = "员工信息")
@Builder
@Data
public class StaffView implements Serializable {
    @Schema(title = "员工id",description = "员工id",example = "1234567890")
    Long id;
    @NotNull
    @Schema(title = "账号",description = "账号id",example = "cuihaitao")
    String account;
    @Schema(title = "头像",description = "头像展示的链接",example = "https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png")
    String avatar;
    @Schema(title = "密码",description = "密码",example = "123")
    String password;
    @NotNull
    @Schema(title = "员工姓名",description = "员工姓名",example = "崔海涛",requiredMode = Schema.RequiredMode.REQUIRED)
    String name;
    @Schema(title = "手机号码",description = "手机号码",example = "13852299511",requiredMode = Schema.RequiredMode.REQUIRED)
    String phone;
    @Schema(title = "地址",description = "地址",example = "江苏泰州市海宁区")
    String address;
    @Schema(title = "年纪",description = "年纪",example = "30")
    Integer age;
    @Schema(title = "性别",description = "性别",example = "男")
    Integer gender;
    @Schema(title = "职位",description = "职位" ,example = "1")
    Integer role=99;
    @Schema(title = "生日",description = "生日",example = "1990-1-1")
    Date birthday;
    @Schema(title = "创建时间",description = "创建时间",example = "2023-1-1 12:00:00")
    Date createTime;
    @Schema(title = "更新时间",description = "最近修改时间",example = "2023-1-1 12:00:00")
    Date updateTime;

    public StaffEntity toStaffEntity(){
        StaffEntity staffEntity = StaffEntity.getInstanceWithDefault(account);
        staffEntity.setId(id);
        staffEntity.setName(name);
        staffEntity.setPhone(phone);
        staffEntity.setAge(age);
        staffEntity.setAddress(address);
        staffEntity.setBirthday(birthday);
        staffEntity.setRole(role);
        staffEntity.setAvatar(avatar);
        return staffEntity;
    }

    public void updateStaffEntity(StaffEntity staffEntity){
        if (name != null && !name.isEmpty() && !name.equals(staffEntity.getName())){
            staffEntity.setName(name);
        }
        if (phone != null && !phone.isEmpty() && !phone.equals(staffEntity.getPhone())){
            staffEntity.setPhone(phone);
        }
        if (address != null && !address.isEmpty() && !address.equals(staffEntity.getAddress())){
            staffEntity.setAddress(address);
        }
        if (age != null && age > 0 && age != staffEntity.getAge()){
            staffEntity.setAge(age);
        }
        if (gender != null && gender >= 0 && gender <=1 && gender != staffEntity.getGender()){
            staffEntity.setGender(gender);
        }
        if (role != null && role >= 0  && role != staffEntity.getRole()){
            staffEntity.setRole(role);
        }
        if (birthday != null  && !birthday.equals(staffEntity.getBirthday())){
            staffEntity.setBirthday(birthday);
        }
    }

    public static StaffView fromStaffEntity(StaffEntity staffEntity){
        return StaffView.builder()
                .id(staffEntity.getId())
                .account(staffEntity.getAccount())
                .address(staffEntity.getAddress())
                .avatar(staffEntity.getAvatar())
                .age(staffEntity.getAge())
                .gender(staffEntity.getGender())
                .name(staffEntity.getName())
                .phone(staffEntity.getPhone())
                .birthday(staffEntity.getBirthday())
                .createTime(staffEntity.getCreateTime())
                .updateTime(staffEntity.getUpdateTime())
                .role(staffEntity.getRole())
                .build();
    }
}

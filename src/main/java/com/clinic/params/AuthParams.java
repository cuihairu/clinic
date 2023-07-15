package com.clinic.params;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@Schema(title = "登录信息")
@NoArgsConstructor
public class AuthParams implements Serializable {
    @NotNull(message = "用户名不能为空")
    @Length(min = 2,max = 20,message = "用户名长度范围2~20.")
    String username;

    @NotNull(message = "密码不能为空")
    @Length(min = 2,max=20,message = "密码长度2-20")
    String password;
}

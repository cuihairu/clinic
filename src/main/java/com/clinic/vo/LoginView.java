package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(title = "验证信息")
public class LoginView {
    @Schema(title = "status",description = "登录状态",example = "ok")
    private String status;

    @Schema(title = "type",description = "登录的方式",example = "account")
    private String type;

    @Schema(title = "token",description = "token",example = "xjhjasskaskjkisjduwe")
    private String token;

    @Schema(title = "currentAuthority",description = "当前权限",example = "admin")
    private String currentAuthority;
}

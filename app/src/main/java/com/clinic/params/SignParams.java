package com.clinic.params;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Schema(title = "签到信息")
@NoArgsConstructor
public class SignParams implements Serializable {
    @NotNull(message = "请选择签到类型")
    @Max(value = 1,message = "错误的签到类型,请选择正确的类型")
    @Min(value = 0,message = "错误的签到类型,请选择正确的类型")
    Integer signType;
}

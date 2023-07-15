package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(title = "统一响应信息")
public class Resp<T> {
    Boolean success;
    T data;
    int errorCode;
    String errorMessage;
    ErrorShowType showType;

}

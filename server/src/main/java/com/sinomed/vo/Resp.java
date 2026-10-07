package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "统一响应信息")
public class Resp<T> {
    Boolean success;
    T data;
    int errorCode;
    String errorMessage;
    ErrorShowType showType;

}

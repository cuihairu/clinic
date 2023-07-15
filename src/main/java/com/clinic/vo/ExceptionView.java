package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Schema(title = "异常信息")
@Builder
@Data
public class ExceptionView implements Serializable {
    @Schema(title = "业务成功与否",description ="业务是否成功" ,example = "false")
    Boolean success = false;
    @Schema(title = "异常信息",description ="异常信息，返回错误的信" ,example = "未知的错误")
    String errorMessage;
    @Schema(title = "提示类型",description ="提示类型" ,example = "error")
    ErrorShowType showType;

    @Schema(title = "异常信息",description ="详细的异常堆栈" ,example = "Exception : NullException")
    String data;
    public static ExceptionView Exception(String message,String exception) {
        return ExceptionView.builder().errorMessage(message).data(exception).build();
    }
    public static ExceptionView Exception(String message) {
        return ExceptionView.builder().errorMessage(message).build();
    }
}

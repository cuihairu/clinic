package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Schema(title = "提示信息")
@Builder
@Data
public class MessageView implements Serializable {

    @Schema(title = "提示信息",description = "包含了错误的信息返回和正常的提示信息返回",example = "请求的参数错误,请检查参数再重试")
    String message = "error message";
    public static MessageView Error(String message) {
        return MessageView.builder().message(message).build();
    }
}

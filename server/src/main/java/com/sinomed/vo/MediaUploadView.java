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
@Schema(title = "媒体上传返回")
public class MediaUploadView {
    @Schema(title = "媒体相对地址", description = "建素材时填进 url 字段", example = "/media/202610/uuid.png")
    private String url;

    @Schema(title = "原始文件名", example = "poster.png")
    private String originalName;

    @Schema(title = "文件大小(字节)", example = "102400")
    private Long size;
}

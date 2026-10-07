package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 打印模板单项：模板名（prescription / receipt）与完整 HTML 内容。
 * 模板为空白版式，不含任何业务数据（见 docs/design/desktop.md D7）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "打印模板")
public class PrintTemplateView {
    @Schema(title = "模板名", example = "receipt")
    private String name;

    @Schema(title = "模板 HTML 全文")
    private String content;
}

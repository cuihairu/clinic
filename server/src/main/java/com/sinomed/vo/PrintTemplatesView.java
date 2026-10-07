package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 打印模板集合：version 为全部模板内容的摘要，内容不变则版本不变，
 * 桌面壳据此决定是否覆盖本地缓存（模板更新不发壳版本，见 docs/design/desktop.md D7）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "打印模板集合")
public class PrintTemplatesView {
    @Schema(title = "模板版本（内容摘要，内容不变则不变）", example = "3f7a1c9e0b2d4a68")
    private String version;

    @Schema(title = "模板列表")
    private List<PrintTemplateView> templates;
}

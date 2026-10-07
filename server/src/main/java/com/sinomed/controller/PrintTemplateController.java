package com.sinomed.controller;

import com.sinomed.service.PrintTemplateService;
import com.sinomed.vo.PrintTemplatesView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 打印模板（免登录，见 docs/design/desktop.md D7）：
 * 模板是空白版式、不含业务数据；桌面工作站壳未登录也要在启动/打印前拉取最新模板，
 * 按 version 增量覆盖本地缓存——模板更新不发壳版本。
 */
@Tag(name = "打印模板", description = "处方笺 / 小票 HTML 模板下发（免登录，内容摘要版本）")
@RestController
@RequestMapping("/api/v1/print")
public class PrintTemplateController {

    private final PrintTemplateService printTemplateService;

    public PrintTemplateController(PrintTemplateService printTemplateService) {
        this.printTemplateService = printTemplateService;
    }

    @Operation(summary = "拉取打印模板", description = "全部内置白名单模板（可被 printtemplates 目录同名文件覆盖）与内容摘要版本",
            responses = {
                    @ApiResponse(responseCode = "200", description = "模板集合", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrintTemplatesView.class)
                    ))
            })
    @GetMapping("/templates")
    public PrintTemplatesView templates() {
        return printTemplateService.list();
    }
}

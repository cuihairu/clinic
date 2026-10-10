package com.sinomed.controller;

import com.sinomed.service.ReceiptPrintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结算小票套打（需登录，与小票业务数据一致）：
 * 返回按结算单填充后的 80mm HTML 小票，浏览器新窗口/iframe 直接 window.print()。
 * 模板版式沿用打印模板下发链（printtemplates 目录可覆盖），数据在服务端填充。
 */
@Tag(name = "结算小票", description = "按结算单填充 80mm 小票模板，返回可打印 HTML（需登录）")
@RestController
@RequestMapping("/api/v1/print")
@RequiredArgsConstructor
public class ReceiptPrintController {

    private final ReceiptPrintService receiptPrintService;

    @Operation(summary = "结算小票 HTML", description = "单号 S+结算单id；次卡抵扣金额列显示「次卡抵扣」；机构名可用 sinomed.receipt.clinic-name 覆盖")
    @GetMapping("/receipt/{settlementId}")
    public ResponseEntity<String> receipt(@PathVariable Long settlementId) {
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, java.nio.charset.StandardCharsets.UTF_8))
                .body(receiptPrintService.render(settlementId));
    }
}

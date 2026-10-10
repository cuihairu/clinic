package com.sinomed.controller;

import com.sinomed.entity.SettlementEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.service.SettlementService;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import com.sinomed.vo.SettlementMonthReportView;
import com.sinomed.vo.SettlementView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收费结算（需登录）：结算台对待结算订单（状态 0 已下单 / 1 已确认）收款。
 * 储值支付校验余额并扣减流水；微信/支付宝为演示口径，只记录支付方式。
 */
@Tag(name = "收费", description = "结算API")
@RestController
@RequestMapping("/api/v1/settlement")
public class SettlementController {

    private final SettlementService settlementService;
    private final CustomerRepository customerRepository;

    public SettlementController(SettlementService settlementService,
                                CustomerRepository customerRepository) {
        this.settlementService = settlementService;
        this.customerRepository = customerRepository;
    }

    @Operation(summary = "收款结算", description = "orderId+payType（1 储值 / 2 微信 / 3 支付宝 / 4 现金）必填；"
            + "订单状态 0/1 → 2 已完成，一单一结算；储值支付余额不足报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "结算单", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SettlementView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数缺失 / 订单不存在或已完结 / 已结算过 / 储值余额不足", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public SettlementView create(@Validated @RequestBody SettlementView view) {
        SettlementEntity saved = settlementService.settle(view);
        SettlementView result = SettlementView.FromSettlementEntity(saved);
        customerRepository.findById(saved.getUserId())
                .ifPresent(customer -> result.setCustomerName(customer.getName()));
        return result;
    }

    @Operation(summary = "结算单分页", description = "管理端「最近结算」列表：联出顾客名，时间倒序",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页结算单", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SettlementView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<SettlementView> findPage(
            @Parameter(description = "页码，1 起始") @Validated @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @Validated @NotNull @RequestParam int pageSize) {
        PageRequest pageRequest = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10,
                Sort.by(Sort.Direction.DESC, "id"));
        Page<SettlementEntity> result = settlementService.findPage(pageRequest);

        Map<Long, String> names = customerRepository.findAllById(
                        result.map(SettlementEntity::getUserId).toSet()).stream()
                .collect(Collectors.toMap(customer -> customer.getId(), customer -> customer.getName()));

        List<SettlementView> data = result.map(settlement -> {
            SettlementView view = SettlementView.FromSettlementEntity(settlement);
            view.setCustomerName(names.get(settlement.getUserId()));
            return view;
        }).getContent();
        PageResp<SettlementView> resp = new PageResp<>();
        resp.data = data;
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }

    @Operation(summary = "月度收费报表", description = "month=yyyy-MM 必填；按结算时间聚合当月结算单："
            + "逐日单数/实收、支付方式构成、次卡核销单数（实收 0）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "月度报表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SettlementMonthReportView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "月份格式无效", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/report/month")
    public SettlementMonthReportView monthReport(
            @Parameter(description = "报表月份 yyyy-MM") @Validated @NotNull @RequestParam String month) {
        return settlementService.monthReport(month);
    }
}

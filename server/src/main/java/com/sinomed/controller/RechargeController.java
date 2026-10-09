package com.sinomed.controller;

import com.sinomed.entity.RechargeEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.service.RechargeService;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import com.sinomed.vo.RechargeBalanceView;
import com.sinomed.vo.RechargeView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 储值管理（需登录）：充值、余额与流水。余额 = 流水合计（充值为正、储值支付扣减为负）。
 */
@Tag(name = "储值", description = "储值API")
@RestController
@RequestMapping("/api/v1/recharge")
public class RechargeController {

    private final RechargeService rechargeService;
    private final CustomerRepository customerRepository;

    public RechargeController(RechargeService rechargeService,
                              CustomerRepository customerRepository) {
        this.rechargeService = rechargeService;
        this.customerRepository = customerRepository;
    }

    @Operation(summary = "储值充值", description = "落一条正数流水；金额必须大于 0，顾客须已建档",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建后的流水", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RechargeView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数缺失 / 金额非正 / 顾客不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public RechargeView create(@Validated @RequestBody RechargeView view) {
        RechargeEntity saved = rechargeService.recharge(view);
        RechargeView result = RechargeView.FromRechargeEntity(saved);
        customerRepository.findById(saved.getUserId())
                .ifPresent(customer -> result.setCustomerName(customer.getName()));
        return result;
    }

    @Operation(summary = "储值余额", description = "顾客的储值余额 = recharges 流水合计，无流水为 0",
            responses = {
                    @ApiResponse(responseCode = "200", description = "余额", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RechargeBalanceView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/balance")
    public RechargeBalanceView balance(
            @Parameter(description = "顾客id") @Validated @NotNull @RequestParam Long customerId) {
        return rechargeService.balance(customerId);
    }

    @Operation(summary = "储值流水分页", description = "管理端列表：联出顾客名；customerId 可选过滤，金额为负的行是储值支付扣减",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页流水", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RechargeView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<RechargeView> findPage(
            @Parameter(description = "页码，1 起始") @Validated @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @Validated @NotNull @RequestParam int pageSize,
            @Parameter(description = "顾客id过滤，可空") @RequestParam(required = false) Long customerId) {
        PageRequest pageRequest = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10);
        Page<RechargeEntity> result = rechargeService.findPage(customerId, pageRequest);

        Map<Long, String> names = customerRepository.findAllById(
                        result.map(RechargeEntity::getUserId).toSet()).stream()
                .collect(Collectors.toMap(customer -> customer.getId(), customer -> customer.getName()));

        List<RechargeView> data = result.map(recharge -> {
            RechargeView view = RechargeView.FromRechargeEntity(recharge);
            view.setCustomerName(names.get(recharge.getUserId()));
            return view;
        }).getContent();
        PageResp<RechargeView> resp = new PageResp<>();
        resp.data = data;
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }
}

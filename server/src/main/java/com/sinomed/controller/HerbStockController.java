package com.sinomed.controller;

import com.sinomed.service.HerbStockService;
import com.sinomed.vo.HerbStockBalanceView;
import com.sinomed.vo.HerbStockLogView;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 饮片出入库台账（需登录）：只记流水，当前库存 = 入库合计 - 出库合计；
 * 出库按效期先进先出（FEFO）消耗批次，效期用于近期到期预警；不涉采购单据与结算。
 */
@Tag(name = "饮片库存", description = "饮片出入库API")
@RestController
@RequestMapping("/api/v1/herb-stock")
public class HerbStockController {

    private final HerbStockService herbStockService;

    public HerbStockController(HerbStockService herbStockService) {
        this.herbStockService = herbStockService;
    }

    @Operation(summary = "登记出入库", description = "herbId + type（1 入库 / 0 出库）+ quantity（克，正整数）必填；"
            + "入库可带批次效期（yyyy-MM-dd，不得早于今天）与供货方/备注；出库不得超过当前库存，出库流水不带效期",
            responses = {
                    @ApiResponse(responseCode = "200", description = "登记后的流水（联药材名）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HerbStockLogView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数缺失 / 药材不存在 / 类型或数量非法 / 效期非法或已过期 / 出库超库存", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public HerbStockLogView create(@Validated @RequestBody HerbStockLogView view) {
        return herbStockService.addLog(view);
    }

    @Operation(summary = "流水分页", description = "herbId/type 可空过滤，联出药材名，id 倒序",
            responses = {
                    @ApiResponse(responseCode = "200", description = "流水分页", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResp.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<HerbStockLogView> findPage(@Validated @NotNull @RequestParam int current,
                                               @Validated @NotNull @RequestParam int pageSize,
                                               @Validated @Nullable @RequestParam Long herbId,
                                               @Validated @Nullable @RequestParam Integer type) {
        Page<HerbStockLogView> result = herbStockService.findPage(herbId, type,
                PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10,
                        Sort.by(Sort.Direction.DESC, "id")));
        PageResp<HerbStockLogView> resp = new PageResp<>();
        resp.data = result.getContent();
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }

    @Operation(summary = "库存余额", description = "各药材当前库存（克，入库合计 - 出库合计）与最早未消耗批次效期；"
            + "expiryWithinDays 内到期或已过期标 warnExpiry（默认 30 天）；无流水的药材不成行",
            responses = {
                    @ApiResponse(responseCode = "200", description = "按药材名排序的余额列表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HerbStockBalanceView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/balance")
    public List<HerbStockBalanceView> balance(@Validated @Nullable @RequestParam Integer expiryWithinDays) {
        return herbStockService.balance(expiryWithinDays);
    }

    @Operation(summary = "删除流水", description = "演示口径无留痕；删除后余额按剩余流水重算；流水不存在报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除后的流水", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HerbStockLogView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "流水不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @DeleteMapping("/{id}")
    public HerbStockLogView delete(@PathVariable("id") Long id) {
        return herbStockService.deleteById(id);
    }
}

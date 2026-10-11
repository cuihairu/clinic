package com.sinomed.controller;

import com.sinomed.entity.FormulaEntity;
import com.sinomed.repository.FormulaItemRepository;
import com.sinomed.service.FormulaService;
import com.sinomed.vo.FormulaView;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import com.sinomed.vo.PrescriptionItemView;
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

/**
 * 方剂库（需登录）：经典方剂参考（方名/拼音检索码/出处/功效主治 + 药味组成）。
 * 开方页「方剂库」按方名或拼音（xiaoyaosan 式）检索带出全方，带出后随处方自由改。
 */
@Tag(name = "方剂库", description = "方剂API")
@RestController
@RequestMapping("/api/v1/formula")
public class FormulaController {

    private final FormulaService formulaService;
    private final FormulaItemRepository formulaItemRepository;

    public FormulaController(FormulaService formulaService,
                             FormulaItemRepository formulaItemRepository) {
        this.formulaService = formulaService;
        this.formulaItemRepository = formulaItemRepository;
    }

    @Operation(summary = "建方剂", description = "name（1–20 字，唯一）+ pinyin（全拼小写字母）+ herbs（至少 1 味："
            + "herb 药名 + weight 剂量克，special 可选）必填；source/indication 可选",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建后的方剂（含药味）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FormulaView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数缺失 / 药味为空 / 方名重复或越界 / 拼音码非法", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public FormulaView create(@Validated @RequestBody FormulaView view) {
        FormulaEntity saved = formulaService.save(view);
        return formulaService.findById(saved.getId());
    }

    @Operation(summary = "更新方剂", description = "按 id 全量更新：id/name/pinyin/herbs 必填，药味全量替换；"
            + "换名撞其他方剂报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "更新后的方剂（含药味）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FormulaView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "id 缺失 / 方剂不存在 / 方名重复 / 拼音码非法", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PutMapping("/")
    public FormulaView update(@Validated @RequestBody FormulaView view) {
        FormulaEntity saved = formulaService.update(view);
        return formulaService.findById(saved.getId());
    }

    @Operation(summary = "方剂分页", description = "id 倒序；keyword 模糊匹配方名或拼音码（可空，如「逍遥」或「xiaoyao」）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页方剂", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FormulaView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<FormulaView> findPage(
            @Parameter(description = "页码，1 起始") @Validated @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @Validated @NotNull @RequestParam int pageSize,
            @Parameter(description = "方名/拼音码模糊过滤，可空") @RequestParam(required = false) String keyword) {
        PageRequest pageRequest = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10,
                Sort.by(Sort.Direction.DESC, "id"));
        Page<FormulaEntity> result = formulaService.findPage(keyword, pageRequest);
        List<FormulaView> data = result.map(f -> {
            FormulaView view = FormulaView.FromEntity(f);
            view.setHerbs(formulaItemRepository.findByFormulaIdOrderBySortAsc(f.getId()).stream()
                    .map(PrescriptionItemView::FromFormulaItemEntity).toList());
            return view;
        }).getContent();
        PageResp<FormulaView> resp = new PageResp<>();
        resp.data = data;
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }

    @Operation(summary = "方剂详情", description = "含药味（按 sort 排序）；不存在报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "方剂详情", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FormulaView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "方剂不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/{id}")
    public FormulaView findById(@PathVariable Long id) {
        return formulaService.findById(id);
    }

    @Operation(summary = "删除方剂", description = "连同药味删除；不影响已开处方；不存在报 400（演示环境口径，无留痕）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "方剂不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @DeleteMapping("/{id}")
    public MessageView delete(@PathVariable Long id) {
        formulaService.deleteById(id);
        MessageView messageView = new MessageView();
        messageView.setMessage("已删除");
        return messageView;
    }
}

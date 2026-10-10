package com.sinomed.controller;

import com.sinomed.entity.PrescriptionTemplateEntity;
import com.sinomed.repository.PrescriptionTemplateItemRepository;
import com.sinomed.service.PrescriptionTemplateService;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import com.sinomed.vo.PrescriptionItemView;
import com.sinomed.vo.PrescriptionTemplateView;
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
 * 病症处方模板（需登录）：模板库 CRUD，开方页「套用模板」取数入口。
 * 口径：模板名唯一（1–20 字）、至少 1 味药；模板只存建议值，套用后随开方单自由改（不改回模板）；
 * 更新走药味全量替换；停用（enabled=0）不在开方页出现。
 */
@Tag(name = "病症处方模板", description = "病症处方模板API")
@RestController
@RequestMapping("/api/v1/prescription/template")
public class PrescriptionTemplateController {

    private final PrescriptionTemplateService templateService;
    private final PrescriptionTemplateItemRepository templateItemRepository;

    public PrescriptionTemplateController(PrescriptionTemplateService templateService,
                                          PrescriptionTemplateItemRepository templateItemRepository) {
        this.templateService = templateService;
        this.templateItemRepository = templateItemRepository;
    }

    @Operation(summary = "建模板", description = "name（1–20 字，唯一）+ herbs（至少 1 味：herb 药名 + weight 剂量克，special 可选）必填；"
            + "doses（默认 7）/decoction（0 无需 / 1 代煎，默认 0）/usage/remark/enabled（默认 1）可选",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建后的模板（含药味）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrescriptionTemplateView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数缺失 / 药味为空 / 病症名重复或越界", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public PrescriptionTemplateView create(@Validated @RequestBody PrescriptionTemplateView view) {
        PrescriptionTemplateEntity saved = templateService.save(view);
        return templateService.findById(saved.getId());
    }

    @Operation(summary = "更新模板", description = "按 id 全量更新：id/name/herbs 必填，药味全量替换；换名撞其他模板报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "更新后的模板（含药味）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrescriptionTemplateView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "id 缺失 / 模板不存在 / 病症名重复", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PutMapping("/")
    public PrescriptionTemplateView update(@Validated @RequestBody PrescriptionTemplateView view) {
        PrescriptionTemplateEntity saved = templateService.update(view);
        return templateService.findById(saved.getId());
    }

    @Operation(summary = "上架模板列表", description = "开方页「套用模板」取数入口：enabled=1，按 id 升序；附药味",
            responses = {
                    @ApiResponse(responseCode = "200", description = "上架模板", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrescriptionTemplateView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/enabled")
    public List<PrescriptionTemplateView> findEnabled() {
        return templateService.findEnabledWithHerbs();
    }

    @Operation(summary = "模板分页", description = "管理端列表：id 倒序；name 模糊过滤可选",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页模板", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrescriptionTemplateView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<PrescriptionTemplateView> findPage(
            @Parameter(description = "页码，1 起始") @Validated @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @Validated @NotNull @RequestParam int pageSize,
            @Parameter(description = "病症名模糊过滤，可空") @RequestParam(required = false) String name) {
        PageRequest pageRequest = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10,
                Sort.by(Sort.Direction.DESC, "id"));
        Page<PrescriptionTemplateEntity> result = templateService.findPage(name, pageRequest);
        List<PrescriptionTemplateView> data = result.map(t -> {
            PrescriptionTemplateView view = PrescriptionTemplateView.FromTemplateEntity(t);
            view.setHerbs(templateItemRepository.findByTemplateIdOrderBySortAsc(t.getId()).stream()
                    .map(PrescriptionItemView::FromTemplateItemEntity).toList());
            return view;
        }).getContent();
        PageResp<PrescriptionTemplateView> resp = new PageResp<>();
        resp.data = data;
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }

    @Operation(summary = "模板详情", description = "含药味（按 sort 排序）；不存在报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "模板详情", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrescriptionTemplateView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "模板不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/{id}")
    public PrescriptionTemplateView findById(@PathVariable Long id) {
        return templateService.findById(id);
    }

    @Operation(summary = "删除模板", description = "连同药味删除；不存在报 400（演示环境口径，无留痕）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "模板不存在", content = @Content(
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
        templateService.deleteById(id);
        MessageView messageView = new MessageView();
        messageView.setMessage("已删除");
        return messageView;
    }
}

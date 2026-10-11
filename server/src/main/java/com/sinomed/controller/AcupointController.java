package com.sinomed.controller;

import com.sinomed.entity.AcupointEntity;
import com.sinomed.service.AcupointService;
import com.sinomed.vo.AcupointView;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
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
 * 穴位字典（需登录）：经络穴位参考（穴名/拼音检索码/归经/定位/主治）。
 * 接诊页「取穴」按穴名或拼音（zusanli 式）检索选穴，取穴字段仍为自由文本。
 */
@Tag(name = "穴位字典", description = "穴位API")
@RestController
@RequestMapping("/api/v1/acupoint")
public class AcupointController {

    private final AcupointService acupointService;

    public AcupointController(AcupointService acupointService) {
        this.acupointService = acupointService;
    }

    @Operation(summary = "收录穴位", description = "name（1–10 字，唯一）+ pinyin（全拼小写字母）+ meridian（归经）必填；"
            + "location/indication 可选",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建后的穴位", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AcupointView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数缺失 / 穴名重复或越界 / 拼音码非法 / 归经为空", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public AcupointView create(@Validated @RequestBody AcupointView view) {
        AcupointEntity saved = acupointService.save(view);
        return acupointService.findById(saved.getId());
    }

    @Operation(summary = "更新穴位", description = "按 id 全量更新：id/name/pinyin/meridian 必填；换名撞其他穴位报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "更新后的穴位", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AcupointView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "id 缺失 / 穴位不存在 / 穴名重复 / 拼音码非法", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PutMapping("/")
    public AcupointView update(@Validated @RequestBody AcupointView view) {
        AcupointEntity saved = acupointService.update(view);
        return acupointService.findById(saved.getId());
    }

    @Operation(summary = "穴位分页", description = "id 倒序；keyword 模糊匹配穴名或拼音码（可空，如「足三」或「zusanli」）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页穴位", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AcupointView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<AcupointView> findPage(
            @Parameter(description = "页码，1 起始") @Validated @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @Validated @NotNull @RequestParam int pageSize,
            @Parameter(description = "穴名/拼音码模糊过滤，可空") @RequestParam(required = false) String keyword) {
        PageRequest pageRequest = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10,
                Sort.by(Sort.Direction.DESC, "id"));
        Page<AcupointEntity> result = acupointService.findPage(keyword, pageRequest);
        List<AcupointView> data = result.map(AcupointView::FromEntity).getContent();
        PageResp<AcupointView> resp = new PageResp<>();
        resp.data = data;
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }

    @Operation(summary = "穴位详情", description = "不存在报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "穴位详情", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AcupointView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "穴位不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/{id}")
    public AcupointView findById(@PathVariable Long id) {
        return acupointService.findById(id);
    }

    @Operation(summary = "删除穴位", description = "不影响已开接诊单；不存在报 400（演示环境口径，无留痕）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "穴位不存在", content = @Content(
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
        acupointService.deleteById(id);
        MessageView messageView = new MessageView();
        messageView.setMessage("已删除");
        return messageView;
    }
}

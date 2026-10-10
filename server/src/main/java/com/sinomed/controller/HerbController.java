package com.sinomed.controller;

import com.sinomed.entity.HerbEntity;
import com.sinomed.service.HerbService;
import com.sinomed.vo.HerbView;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 药材字典（需登录）：处方计价的比价依据。名唯一、价格为每克分价；
 * 计价实时查字典不做快照，删改价会同步影响历史处方展示的实时计价。
 */
@Tag(name = "药材字典", description = "药材字典API：处方计价比价依据")
@RestController
@RequestMapping("/api/v1/herb")
@Validated
public class HerbController {

    private final HerbService herbService;

    public HerbController(HerbService herbService) {
        this.herbService = herbService;
    }

    @Operation(summary = "收录药材", description = "name 唯一，price 为每克价格（分，>0）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "收录后的药材", content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = HerbView.class))),
                    @ApiResponse(responseCode = "400", description = "名称为空/重复、价格非法", content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = MessageView.class)))
            })
    @PostMapping("/")
    public HerbView create(@Valid @RequestBody HerbView view) {
        return HerbView.FromEntity(herbService.create(view));
    }

    @Operation(summary = "更新药材", description = "改价/改名；名称查重不包含自身",
            responses = {
                    @ApiResponse(responseCode = "200", description = "更新后的药材", content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = HerbView.class))),
                    @ApiResponse(responseCode = "400", description = "药材不存在/名称重复/价格非法", content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = MessageView.class)))
            })
    @PutMapping("/")
    public HerbView update(@Valid @RequestBody HerbView view) {
        return HerbView.FromEntity(herbService.update(view));
    }

    @Operation(summary = "删除药材", description = "演示环境口径：无引用检查，删后相关处方药味转「未比价」",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = MessageView.class))),
                    @ApiResponse(responseCode = "400", description = "药材不存在", content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = MessageView.class)))
            })
    @DeleteMapping("/{id}")
    public MessageView delete(@PathVariable @NotNull Long id) {
        herbService.deleteById(id);
        return MessageView.builder().message("已删除药材 " + id).build();
    }

    @Operation(summary = "字典分页", description = "keyword 非空按名称包含过滤；名称升序",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页药材", content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = HerbView.class)))
            })
    @GetMapping("/page")
    public PageResp<HerbView> findPage(
            @Parameter(description = "页码，1 起始") @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @NotNull @RequestParam int pageSize,
            @Parameter(description = "名称包含过滤，可空") @RequestParam(required = false) String keyword) {
        PageRequest pageRequest = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10,
                Sort.by(Sort.Direction.ASC, "name"));
        var page = herbService.findPage(keyword, pageRequest);
        PageResp<HerbView> resp = new PageResp<>();
        resp.data = page.map(HerbView::FromEntity).getContent();
        resp.pages = page.getTotalPages();
        resp.total = page.getTotalElements();
        resp.success = true;
        return resp;
    }
}

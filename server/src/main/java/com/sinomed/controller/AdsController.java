package com.sinomed.controller;

import com.sinomed.entity.AdMaterialEntity;
import com.sinomed.entity.AdScheduleEntity;
import com.sinomed.entity.AdScreenEntity;
import com.sinomed.service.AdsService;
import com.sinomed.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "广告投屏", description = "素材/排期/屏管理与平板下发 API")
@RestController
@RequestMapping("/api/v1/ads")
public class AdsController {

    private final AdsService adsService;

    public AdsController(AdsService adsService) {
        this.adsService = adsService;
    }

    // ---------- 素材管理 ----------

    @Operation(summary = "创建素材", description = "创建一条素材记录（媒体先经 upload 接口拿到 url）",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdMaterialView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "素材表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdMaterialView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/materials")
    public AdMaterialView createMaterial(@Validated @RequestBody AdMaterialView view) {
        AdMaterialEntity save = adsService.saveMaterial(view.ToAdMaterialEntity());
        return AdMaterialView.FromAdMaterialEntity(save);
    }

    @Operation(summary = "更新素材", description = "按 id 更新素材",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdMaterialView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "素材表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdMaterialView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PutMapping("/materials")
    public AdMaterialView updateMaterial(@Validated @RequestBody AdMaterialView view) {
        if (view.getId() == null) {
            throw new IllegalArgumentException("素材Id不能为空");
        }
        adsService.findMaterialById(view.getId()).orElseThrow(() -> new IllegalArgumentException("素材不存在"));
        AdMaterialEntity save = adsService.saveMaterial(view.ToAdMaterialEntity());
        return AdMaterialView.FromAdMaterialEntity(save);
    }

    @Operation(summary = "素材分页", description = "按条件分页查素材",
            responses = {
                    @ApiResponse(responseCode = "200", description = "素材分页", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResp.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/materials/page")
    public PageResp<AdMaterialView> pageMaterials(
            @RequestParam int current,
            @RequestParam int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer enabled) {
        PageRequest page = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 1);
        AdMaterialEntity example = new AdMaterialEntity();
        if (name != null && !name.isBlank()) example.setName(name);
        if (enabled != null) example.setEnabled(enabled);
        Page<AdMaterialEntity> result = adsService.findMaterials(example, page);
        return toPageResp(result, AdMaterialView::FromAdMaterialEntity);
    }

    @Operation(summary = "删除素材", description = "按 id 删除素材",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @DeleteMapping("/materials/{id}")
    public MessageView deleteMaterial(@PathVariable Long id) {
        adsService.findMaterialById(id).orElseThrow(() -> new IllegalArgumentException("素材不存在"));
        adsService.deleteMaterialById(id);
        return MessageView.builder().message("删除成功").build();
    }

    @Operation(summary = "媒体上传", description = "multipart 上传图片/视频，落盘 data/ads/，返回可填入素材 url 的相对地址",
            responses = {
                    @ApiResponse(responseCode = "200", description = "上传结果", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MediaUploadView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/materials/upload")
    public MediaUploadView uploadMaterial(@RequestParam("file") MultipartFile file) {
        return adsService.uploadMaterial(file);
    }

    // ---------- 屏管理 ----------

    @Operation(summary = "创建屏", description = "注册一块平板屏（code 唯一）",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdScreenView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "屏表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdScreenView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/screens")
    public AdScreenView createScreen(@Validated @RequestBody AdScreenView view) {
        AdScreenEntity save = adsService.saveScreen(view.ToAdScreenEntity());
        return AdScreenView.FromAdScreenEntity(save);
    }

    @Operation(summary = "更新屏", description = "按 id 更新屏",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdScreenView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "屏表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdScreenView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PutMapping("/screens")
    public AdScreenView updateScreen(@Validated @RequestBody AdScreenView view) {
        if (view.getId() == null) {
            throw new IllegalArgumentException("屏Id不能为空");
        }
        adsService.findScreenById(view.getId()).orElseThrow(() -> new IllegalArgumentException("屏不存在"));
        AdScreenEntity save = adsService.saveScreen(view.ToAdScreenEntity());
        return AdScreenView.FromAdScreenEntity(save);
    }

    @Operation(summary = "屏分页", description = "按条件分页查屏（含最近心跳）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "屏分页", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResp.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/screens/page")
    public PageResp<AdScreenView> pageScreens(
            @RequestParam int current,
            @RequestParam int pageSize,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name) {
        PageRequest page = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 1);
        AdScreenEntity example = new AdScreenEntity();
        if (code != null && !code.isBlank()) example.setCode(code);
        if (name != null && !name.isBlank()) example.setName(name);
        Page<AdScreenEntity> result = adsService.findScreens(example, page);
        return toPageResp(result, AdScreenView::FromAdScreenEntity);
    }

    @Operation(summary = "删除屏", description = "按 id 删除屏",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @DeleteMapping("/screens/{id}")
    public MessageView deleteScreen(@PathVariable Long id) {
        adsService.findScreenById(id).orElseThrow(() -> new IllegalArgumentException("屏不存在"));
        adsService.deleteScreenById(id);
        return MessageView.builder().message("删除成功").build();
    }

    // ---------- 排期管理 ----------

    @Operation(summary = "创建排期", description = "把素材挂到屏上，限定星期与时段",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdScheduleView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "排期表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdScheduleView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/schedules")
    public AdScheduleView createSchedule(@Validated @RequestBody AdScheduleView view) {
        AdScheduleEntity save = adsService.saveSchedule(view.ToAdScheduleEntity());
        return AdScheduleView.FromAdScheduleEntity(save);
    }

    @Operation(summary = "更新排期", description = "按 id 更新排期",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdScheduleView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "排期表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdScheduleView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PutMapping("/schedules")
    public AdScheduleView updateSchedule(@Validated @RequestBody AdScheduleView view) {
        if (view.getId() == null) {
            throw new IllegalArgumentException("排期Id不能为空");
        }
        adsService.findScheduleById(view.getId()).orElseThrow(() -> new IllegalArgumentException("排期不存在"));
        AdScheduleEntity save = adsService.saveSchedule(view.ToAdScheduleEntity());
        return AdScheduleView.FromAdScheduleEntity(save);
    }

    @Operation(summary = "排期分页", description = "按条件分页查排期",
            responses = {
                    @ApiResponse(responseCode = "200", description = "排期分页", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResp.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/schedules/page")
    public PageResp<AdScheduleView> pageSchedules(
            @RequestParam int current,
            @RequestParam int pageSize,
            @RequestParam(required = false) Long screenId,
            @RequestParam(required = false) Long materialId) {
        PageRequest page = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 1);
        AdScheduleEntity example = new AdScheduleEntity();
        if (screenId != null) example.setScreenId(screenId);
        if (materialId != null) example.setMaterialId(materialId);
        Page<AdScheduleEntity> result = adsService.findSchedules(example, page);
        return toPageResp(result, AdScheduleView::FromAdScheduleEntity);
    }

    @Operation(summary = "删除排期", description = "按 id 删除排期",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @DeleteMapping("/schedules/{id}")
    public MessageView deleteSchedule(@PathVariable Long id) {
        adsService.findScheduleById(id).orElseThrow(() -> new IllegalArgumentException("排期不存在"));
        adsService.deleteScheduleById(id);
        return MessageView.builder().message("删除成功").build();
    }

    // ---------- 平板下发 ----------

    @Operation(summary = "拉取播放列表", description = "平板按屏 code 拉取播放序列与内容戳，顺带心跳上报（无需登录，内网只读 + 屏 code 校验）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "播放列表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlaylistView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "屏不存在或参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/playlist")
    public PlaylistView playlist(@Parameter(description = "屏标识", example = "PAD-01") @RequestParam String screen) {
        return adsService.playlist(screen);
    }

    @FunctionalInterface
    private interface Mapper<T, R> {
        R apply(T t);
    }

    private <T, R> PageResp<R> toPageResp(Page<T> page, Mapper<T, R> mapper) {
        PageResp<R> resp = new PageResp<>();
        List<R> data = new ArrayList<>();
        page.forEach(item -> data.add(mapper.apply(item)));
        resp.data = data;
        resp.total = page.getTotalElements();
        resp.pages = page.getTotalPages();
        resp.success = true;
        return resp;
    }
}

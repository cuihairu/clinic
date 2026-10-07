package com.sinomed.controller;

import com.sinomed.entity.AdScreenEntity;
import com.sinomed.entity.QueueCallEntity;
import com.sinomed.repository.AdScreenRepository;
import com.sinomed.repository.QueueCallRepository;
import com.sinomed.vo.CallsLatestView;
import com.sinomed.vo.ExceptionView;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.QueueCallView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 叫号：POST 前台触发（需登录），GET latest 平板轮询（免登录，见 SecurityConfig 放行）。
 */
@Tag(name = "叫号", description = "候诊叫号 API（前台触发 / 平板拉取）")
@RestController
@RequestMapping("/api/v1/calls")
public class CallController {

    private final QueueCallRepository callRepository;
    private final AdScreenRepository screenRepository;

    public CallController(QueueCallRepository callRepository, AdScreenRepository screenRepository) {
        this.callRepository = callRepository;
        this.screenRepository = screenRepository;
    }

    @Operation(summary = "发起叫号", description = "前台手动叫号：写一条 queue_calls；screenId 为空时全部屏都提示",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = QueueCallView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "叫号记录", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = QueueCallView.class)
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
    @PostMapping
    public QueueCallView call(@Validated @RequestBody QueueCallView view) {
        if (view.getScreenId() != null) {
            AdScreenEntity screen = screenRepository.findById(view.getScreenId())
                    .orElseThrow(() -> new IllegalArgumentException("屏不存在：" + view.getScreenId()));
            if (screen.getEnabled() != 1) {
                throw new IllegalArgumentException("屏已停用：" + screen.getCode());
            }
        }
        QueueCallEntity entity = view.ToQueueCallEntity();
        entity.setId(null);
        entity.setStatus(1); // 写入即已叫（叫号动作本身完成）
        entity.setCalledAt(new Date());
        return QueueCallView.FromQueueCallEntity(callRepository.save(entity));
    }

    @Operation(summary = "平板拉取新叫号", description = "游标拉取：返回 since 之后、定向本屏（或未定向）的记录，按 id 升序",
            responses = {
                    @ApiResponse(responseCode = "200", description = "新叫号", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CallsLatestView.class)
                    ))
            })
    @GetMapping("/latest")
    public CallsLatestView latest(
            @Parameter(description = "屏标识", example = "PAD-01") @RequestParam String screen,
            @Parameter(description = "游标（上次拿到的最大 id），缺省从 0 起") @RequestParam(required = false, defaultValue = "0") Long since) {
        AdScreenEntity screenEntity = screenRepository.findByCode(screen)
                .orElseThrow(() -> new IllegalArgumentException("屏不存在：" + screen));
        // 全部屏广播 + 定向本屏，合并去游标后升序
        List<QueueCallEntity> calls = callRepository.findByIdGreaterThanAndScreenIdIsNullOrderByIdAsc(since);
        List<QueueCallEntity> own = callRepository
                .findByIdGreaterThanAndScreenIdOrderByIdAsc(since, screenEntity.getId());
        List<QueueCallEntity> merged = new java.util.ArrayList<>(calls);
        merged.addAll(own);
        merged.sort(java.util.Comparator.comparingLong(QueueCallEntity::getId));
        Long cursor = merged.isEmpty() ? since : merged.get(merged.size() - 1).getId();
        return CallsLatestView.builder()
                .since(cursor)
                .calls(merged.stream().map(QueueCallView::FromQueueCallEntity).toList())
                .build();
    }

    @Operation(summary = "最近叫号", description = "管理端展示最近 20 条叫号记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "最近叫号", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = QueueCallView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/recent")
    public List<QueueCallView> recent() {
        return callRepository.findTop20ByOrderByIdDesc().stream()
                .map(QueueCallView::FromQueueCallEntity)
                .toList();
    }
}

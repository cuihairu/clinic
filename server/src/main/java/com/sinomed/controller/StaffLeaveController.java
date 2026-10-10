package com.sinomed.controller;

import com.sinomed.entity.StaffLeaveEntity;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffLeaveService;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import com.sinomed.vo.StaffLeaveView;
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
 * 员工请假（需登录）：提交/分页/删除请假记录。
 * 口径：请假为记录性质（类型 0 病假 / 1 事假、事由、起止时间），不改员工登录状态、无审批流（演示口径）。
 */
@Tag(name = "员工请假", description = "请假记录提交与查询（需登录）")
@RestController
@RequestMapping("/api/v1/staff/leave")
@Validated
public class StaffLeaveController {

    private final StaffLeaveService staffLeaveService;
    private final StaffRepository staffRepository;

    public StaffLeaveController(StaffLeaveService staffLeaveService, StaffRepository staffRepository) {
        this.staffLeaveService = staffLeaveService;
        this.staffRepository = staffRepository;
    }

    @Operation(summary = "提交请假", description = "staffId + leaveType（0 病假 / 1 事假）+ reason + startTime/endTime 必填，"
            + "结束时间不能早于起始时间；不改员工登录状态",
            responses = {
                    @ApiResponse(responseCode = "200", description = "请假记录", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffLeaveView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "员工不存在 / 类型无效 / 事由为空 / 起止时间非法", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public StaffLeaveView create(@RequestBody StaffLeaveView view) {
        StaffLeaveEntity saved = staffLeaveService.create(view);
        StaffLeaveView result = StaffLeaveView.builder()
                .id(saved.getId())
                .staffId(saved.getStaffId())
                .leaveType(saved.getLeaveType())
                .reason(saved.getReason())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .createTime(saved.getCreateTime())
                .build();
        staffRepository.findById(saved.getStaffId())
                .ifPresent(staff -> result.setStaffName(staff.getName()));
        return result;
    }

    @Operation(summary = "请假分页", description = "管理端列表：联出员工名，id 倒序；staffId 可选过滤",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页请假记录", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffLeaveView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<StaffLeaveView> findPage(
            @Parameter(description = "页码，1 起始") @Validated @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @Validated @NotNull @RequestParam int pageSize,
            @Parameter(description = "员工id过滤，可空") @RequestParam(required = false) Long staffId) {
        PageRequest pageRequest = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10,
                Sort.by(Sort.Direction.DESC, "id"));
        Page<StaffLeaveEntity> result = staffLeaveService.findPage(staffId, pageRequest);

        Map<Long, String> staffNames = staffRepository.findAllById(
                        result.map(StaffLeaveEntity::getStaffId).toSet()).stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s.getName()));
        List<StaffLeaveView> data = result.map(leave -> StaffLeaveView.builder()
                .id(leave.getId())
                .staffId(leave.getStaffId())
                .staffName(staffNames.get(leave.getStaffId()))
                .leaveType(leave.getLeaveType())
                .reason(leave.getReason())
                .startTime(leave.getStartTime())
                .endTime(leave.getEndTime())
                .createTime(leave.getCreateTime())
                .build()).getContent();
        PageResp<StaffLeaveView> resp = new PageResp<>();
        resp.data = data;
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }

    @Operation(summary = "删除请假记录", description = "按 id 删除（演示环境口径，无留痕）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "请假记录不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @DeleteMapping("/{id}")
    public MessageView deleteById(@PathVariable Long id) {
        staffLeaveService.deleteById(id);
        return MessageView.builder().message("已删除请假记录 " + id).build();
    }
}

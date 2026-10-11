package com.sinomed.controller;

import com.sinomed.entity.StaffEntity;
import com.sinomed.entity.StaffShiftEntity;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffShiftService;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import com.sinomed.vo.StaffShiftView;
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

/**
 * 员工周期班表（需登录）：员工 × 星期（1-7）× HH:mm 班次，按周循环；同员工同星期唯一。
 * 班表只做排班参考，考勤打卡仍以 signs 为准。
 */
@Tag(name = "员工班表", description = "周期班表API")
@RestController
@RequestMapping("/api/v1/shift")
public class StaffShiftController {

    private final StaffShiftService shiftService;
    private final StaffRepository staffRepository;

    public StaffShiftController(StaffShiftService shiftService, StaffRepository staffRepository) {
        this.shiftService = shiftService;
        this.staffRepository = staffRepository;
    }

    @Operation(summary = "加排班", description = "staffId + weekday（1 周一 … 7 周日）+ start/end（HH:mm，start < end）必填；"
            + "同一员工同一星期仅一条班次",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建后的班次（联员工姓名）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffShiftView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数缺失 / 员工不存在 / 星期或时段非法 / 班次重复", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public StaffShiftView create(@Validated @RequestBody StaffShiftView view) {
        StaffShiftEntity saved = shiftService.save(view);
        return shiftService.findById(saved.getId());
    }

    @Operation(summary = "更新班次", description = "按 id 全量更新；换员工/星期撞已有班次报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "更新后的班次（联员工姓名）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffShiftView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "id 缺失 / 班次不存在 / 星期或时段非法 / 班次重复", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PutMapping("/")
    public StaffShiftView update(@Validated @RequestBody StaffShiftView view) {
        StaffShiftEntity saved = shiftService.update(view);
        return shiftService.findById(saved.getId());
    }

    @Operation(summary = "班次分页", description = "按员工、星期升序；staffId/weekday 可空过滤",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页班次", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffShiftView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<StaffShiftView> findPage(
            @Parameter(description = "页码，1 起始") @Validated @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @Validated @NotNull @RequestParam int pageSize,
            @Parameter(description = "员工 id 过滤，可空") @RequestParam(required = false) Long staffId,
            @Parameter(description = "星期过滤（1-7），可空") @RequestParam(required = false) Integer weekday) {
        Page<StaffShiftEntity> result = shiftService.findPage(staffId, weekday,
                PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10));
        List<StaffShiftView> data = result.map(shift -> StaffShiftView.FromEntity(shift,
                staffRepository.findById(shift.getStaffId()).map(StaffEntity::getName).orElse(null))).getContent();
        PageResp<StaffShiftView> resp = new PageResp<>();
        resp.data = data;
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }

    @Operation(summary = "员工整周班次", description = "weekday 升序，考勤/预约侧取数入口；员工不存在报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "该员工全部班次", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffShiftView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "员工不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/staff/{staffId}")
    public List<StaffShiftView> listByStaff(@PathVariable Long staffId) {
        return shiftService.listByStaff(staffId).stream()
                .map(shift -> StaffShiftView.FromEntity(shift,
                        staffRepository.findById(shift.getStaffId()).map(StaffEntity::getName).orElse(null)))
                .toList();
    }

    @Operation(summary = "班次详情", description = "不存在报 400",
            responses = {
                    @ApiResponse(responseCode = "200", description = "班次详情", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffShiftView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "班次不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/{id}")
    public StaffShiftView findById(@PathVariable Long id) {
        return shiftService.findById(id);
    }

    @Operation(summary = "删除班次", description = "不影响考勤记录；不存在报 400（演示环境口径，无留痕）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "班次不存在", content = @Content(
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
        shiftService.deleteById(id);
        MessageView messageView = new MessageView();
        messageView.setMessage("已删除");
        return messageView;
    }
}

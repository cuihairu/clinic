package com.sinomed.controller;

import com.sinomed.entity.PrescriptionEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.PrescriptionItemRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.PrescriptionService;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import com.sinomed.vo.PrescriptionItemView;
import com.sinomed.vo.PrescriptionView;
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
 * 中药处方（需登录）：处方笺开方、查询与删除。
 * MVP 口径：药材名为自由文本，无药材字典/库存/配伍审方/计价（均为规划功能）。
 */
@Tag(name = "处方", description = "中药处方API")
@RestController
@RequestMapping("/api/v1/prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  PrescriptionItemRepository prescriptionItemRepository,
                                  CustomerRepository customerRepository,
                                  StaffRepository staffRepository) {
        this.prescriptionService = prescriptionService;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
    }

    @Operation(summary = "开方", description = "customerId + herbs（至少 1 味：herb 药名 + weight 剂量克，special 特殊煎法可选）必填；"
            + "treatId/staffId/doses（默认 7）/usage/remark 可选",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建后的处方（含药味）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrescriptionView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数缺失 / 药味为空 / 药名或剂量非法 / 顾客不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/")
    public PrescriptionView create(@Validated @RequestBody PrescriptionView view) {
        if (view.getCustomerId() == null) {
            throw new IllegalArgumentException("需要顾客 id");
        }
        customerRepository.findById(view.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("顾客不存在：" + view.getCustomerId()));
        if (view.getStaffId() != null) {
            staffRepository.findById(view.getStaffId())
                    .orElseThrow(() -> new IllegalArgumentException("员工不存在：" + view.getStaffId()));
        }
        PrescriptionEntity saved = prescriptionService.save(view);
        // 重查一遍带出审计时间与药味，回包完整
        return prescriptionService.findById(saved.getId());
    }

    @Operation(summary = "处方分页", description = "管理端列表：联出顾客名、医师名与药味（herbs）；customerId 可选过滤，id 倒序",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分页处方", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrescriptionView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/page")
    public PageResp<PrescriptionView> findPage(
            @Parameter(description = "页码，1 起始") @Validated @NotNull @RequestParam int current,
            @Parameter(description = "每页条数") @Validated @NotNull @RequestParam int pageSize,
            @Parameter(description = "顾客id过滤，可空") @RequestParam(required = false) Long customerId) {
        PageRequest pageRequest = PageRequest.of(current > 0 ? current - 1 : 0, pageSize > 0 ? pageSize : 10,
                Sort.by(Sort.Direction.DESC, "id"));
        Page<PrescriptionEntity> result = prescriptionService.findPage(customerId, pageRequest);

        Map<Long, String> customerNames = customerRepository.findAllById(
                        result.map(PrescriptionEntity::getCustomerId).toSet()).stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));
        Map<Long, String> staffNames = staffRepository.findAllById(
                        result.map(PrescriptionEntity::getStaffId).filter(s -> s != null).toSet()).stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s.getName()));
        Map<Long, List<PrescriptionItemView>> herbs = prescriptionItemRepository
                .findByPrescriptionIdInOrderBySortAsc(result.map(PrescriptionEntity::getId).getContent())
                .stream().collect(Collectors.groupingBy(item -> item.getPrescriptionId(),
                        Collectors.mapping(PrescriptionItemView::FromItemEntity, Collectors.toList())));

        List<PrescriptionView> data = result.map(prescription -> {
            PrescriptionView view = PrescriptionView.FromPrescriptionEntity(prescription);
            view.setCustomerName(customerNames.get(prescription.getCustomerId()));
            view.setStaffName(staffNames.get(prescription.getStaffId()));
            view.setHerbs(herbs.get(prescription.getId()));
            return view;
        }).getContent();
        PageResp<PrescriptionView> resp = new PageResp<>();
        resp.data = data;
        resp.pages = result.getTotalPages();
        resp.total = result.getTotalElements();
        resp.success = true;
        return resp;
    }

    @Operation(summary = "处方详情", description = "按 id 查处方（含药味）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "处方详情", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrescriptionView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "处方不存在", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @GetMapping("/{id}")
    public PrescriptionView findById(@PathVariable Long id) {
        return prescriptionService.findById(id);
    }

    @Operation(summary = "删除处方", description = "连同药味一并删除（演示环境口径，无留痕）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "处方不存在", content = @Content(
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
        prescriptionService.deleteById(id);
        return MessageView.builder().message("已删除处方 " + id).build();
    }
}

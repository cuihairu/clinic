package com.sinomed.controller;

import com.sinomed.entity.PrescriptionEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.PrescriptionItemRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.CompatibilityService;
import com.sinomed.service.PrescriptionService;
import com.sinomed.service.PricingService;
import com.sinomed.vo.CompatibilityCheckView;
import com.sinomed.vo.CompatibilityResultView;
import com.sinomed.vo.MessageView;
import com.sinomed.vo.PageResp;
import com.sinomed.vo.PrescriptionItemView;
import com.sinomed.vo.PrescriptionView;
import com.sinomed.vo.PricingView;
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
 * 中药处方（需登录）：处方笺开方、查询、删除、配伍审方与实时计价。
 * 口径：药材名为自由文本；配伍审方（十八反/十九畏）提示不拦截；
 * 计价按药材字典（/api/v1/herb）实时算、无快照；库存与代煎领取仍为规划功能。
 */
@Tag(name = "处方", description = "中药处方API")
@RestController
@RequestMapping("/api/v1/prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final CompatibilityService compatibilityService;
    private final PricingService pricingService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  PrescriptionItemRepository prescriptionItemRepository,
                                  CustomerRepository customerRepository,
                                  StaffRepository staffRepository,
                                  CompatibilityService compatibilityService,
                                  PricingService pricingService) {
        this.prescriptionService = prescriptionService;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.compatibilityService = compatibilityService;
        this.pricingService = pricingService;
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
        // 重查一遍带出审计时间与药味，回包完整（含实时计价）
        PrescriptionView result = prescriptionService.findById(saved.getId());
        result.setPricing(pricingService.price(result.getHerbs(), result.getDoses()));
        return result;
    }

    @Operation(summary = "配伍审方", description = "按经典十八反（禁忌）/十九畏（慎用）比对药材名；自由文本药名按别名包含匹配"
            + "（如「法半夏」命中「半夏」）。findings 为空即未发现配伍禁忌；提示不拦截，是否照用由医师判断",
            responses = {
                    @ApiResponse(responseCode = "200", description = "审方结果", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompatibilityResultView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "药材名单为空", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    ))
            })
    @PostMapping("/compatibility")
    public CompatibilityResultView checkCompatibility(@RequestBody CompatibilityCheckView view) {
        List<String> herbs = view == null ? null : view.getHerbs();
        return compatibilityService.check(herbs);
    }

    @Operation(summary = "处方试算", description = "不开方只算钱：body 传 herbs（herb/weight）+ doses，按药材字典实时计价返回；"
            + "未收录药名计入 unknownHerbs 不计费",
            responses = {
                    @ApiResponse(responseCode = "200", description = "计价结果", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PricingView.class)
                    ))
            })
    @PostMapping("/price")
    public PricingView price(@RequestBody PrescriptionView view) {
        return pricingService.price(view == null ? null : view.getHerbs(), view == null ? null : view.getDoses());
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
            view.setPricing(pricingService.price(view.getHerbs(), view.getDoses()));
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
        PrescriptionView view = prescriptionService.findById(id);
        view.setPricing(pricingService.price(view.getHerbs(), view.getDoses()));
        return view;
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

package com.sinomed.controller;

import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.ItemEntity;
import com.sinomed.entity.OrderEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.ItemRepository;
import com.sinomed.service.OrderService;
import com.sinomed.vo.ExceptionView;
import com.sinomed.vo.KioskItemView;
import com.sinomed.vo.KioskOrderRequest;
import com.sinomed.vo.KioskOrderResult;
import com.sinomed.vo.MessageView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 顾客选服务 Kiosk（免登录，内网设备约定，见 docs/design/kiosk.md）：
 * 浏览纯只读；下单是唯一写入口，写范围限定一条顾客档案（按手机号幂等）+ N 条待接待订单。
 */
@Tag(name = "顾客选服务 Kiosk", description = "到店自助浏览卡项与下单（免登录）")
@RestController
@RequestMapping("/api/v1/kiosk")
public class KioskController {

    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;

    public KioskController(ItemRepository itemRepository,
                           CustomerRepository customerRepository,
                           OrderService orderService) {
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
        this.orderService = orderService;
    }

    @Operation(summary = "浏览上架卡项", description = "Kiosk 首页网格：enabled=1 的卡项按 sort、name 升序，只含展示字段",
            responses = {
                    @ApiResponse(responseCode = "200", description = "上架卡项列表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KioskItemView.class)
                    ))
            })
    @GetMapping("/items")
    public List<KioskItemView> items() {
        return itemRepository.findByEnabledOrderBySortAscNameAsc(1).stream()
                .map(KioskItemView::FromItemEntity)
                .toList();
    }

    @Operation(summary = "下单", description = "按手机号找/建顾客（幂等），每个卡项 id 落一条待接待订单（status=0、价格取卡项现价快照）；不做支付",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = KioskOrderRequest.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "下单回执（单号与合计）", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KioskOrderResult.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "手机号格式错误 / 卡项不存在或已下架", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @PostMapping("/orders")
    public KioskOrderResult order(@Validated @RequestBody KioskOrderRequest request) {
        if (!request.getPhone().matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("手机号格式不对：应为 1 开头的 11 位数字");
        }
        // 卡项先全部校验，避免校验到一半建档
        List<ItemEntity> items = new ArrayList<>();
        for (Long itemId : request.getItemIds()) {
            ItemEntity item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("卡项不存在：" + itemId));
            if (item.getEnabled() == null || item.getEnabled() != 1) {
                throw new IllegalArgumentException("卡项已下架：" + item.getName());
            }
            items.add(item);
        }

        CustomerEntity customer = customerRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    CustomerEntity created = new CustomerEntity();
                    created.setPhone(request.getPhone());
                    String name = request.getName();
                    created.setName(name == null || name.isBlank() ? "到店客人" : name.trim());
                    return customerRepository.save(created);
                });

        List<KioskOrderResult.Line> lines = new ArrayList<>();
        int totalFee = 0;
        for (ItemEntity item : items) {
            OrderEntity order = new OrderEntity();
            order.setUserId(customer.getId());
            order.setItemId(item.getId());
            order.setStatus(0); // 已下单，待前台接待
            order.setPrice(item.getPrice()); // 下单时刻价格快照
            OrderEntity saved = orderService.save(order);
            totalFee += item.getPrice() == null ? 0 : item.getPrice();
            lines.add(KioskOrderResult.Line.builder()
                    .id(saved.getId())
                    .itemId(item.getId())
                    .itemName(item.getName())
                    .price(item.getPrice())
                    .build());
        }
        return KioskOrderResult.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .orders(lines)
                .totalFee(totalFee)
                .build();
    }
}

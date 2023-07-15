package com.clinic.controller;

import com.clinic.entity.TreatEntity;
import com.clinic.entity.CustomerEntity;
import com.clinic.service.TreatService;
import com.clinic.service.CustomerService;
import com.clinic.vo.MessageView;
import com.clinic.vo.ExceptionView;
import com.clinic.vo.PageResp;
import com.clinic.vo.TreatView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Tag(name = "疗程", description = "疗程表")
@RestController
@RequestMapping("/api/v1/treat")
public class TreatController {

    private final TreatService treatService;

    private final CustomerService customerService;

    public TreatController(TreatService treatService, CustomerService customerService) {
        this.treatService = treatService;
        this.customerService = customerService;
    }

    @Operation(summary = "查询客人疗程表", description = "根据客人id来查询用表", parameters = {
            @Parameter(name = "id", description = "客人id")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "疗程表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TreatView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @GetMapping("/customer/{id}")
    public List<TreatView> findTreatByUserId(@PathVariable("id") Long id) {
        List<TreatEntity> treats = treatService.findByCustomerId(id);
        return treats.stream().map(TreatView::FromTreatEntity).toList();
    }

    @Operation(summary = "查询客人疗程表", description = "根据客人名字来查询用表", parameters = {
            @Parameter(name = "name", description = "客人名字")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "疗程表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TreatView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @GetMapping("/customer/name/{name}")
    public List<TreatView> findTreatByCustomerName(@PathVariable("name") String name) throws IllegalArgumentException {
        List<CustomerEntity> customerEntityList = customerService.findByName(name);
        if (customerEntityList.size() == 0) {
            throw new IllegalArgumentException("用户不存在");
        } else if (customerEntityList.size() > 1) {
            throw new IllegalArgumentException("用户不唯一,请使用ID查询");
        }
        CustomerEntity customer = customerEntityList.get(0);
        List<TreatEntity> treats = treatService.findByCustomerId(customer.getId());
        return treats.stream().map(TreatView::FromTreatEntity).toList();
    }

    @GetMapping("/history")
    public PageResp<TreatView> findTreatByPage(@Validated @NotNull @RequestParam int current, @Validated @NotNull @RequestParam int pageSize, @Validated @NotNull @RequestParam Long customerId, @Validated @Nullable @RequestParam String startTime, @Validated @Nullable @RequestParam String endTime) throws IllegalArgumentException {
        Optional<CustomerEntity> customerEntity = customerService.findById(customerId);
        if (customerEntity.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<Long> customerIds = new ArrayList<Long>();
        customerIds.add(customerId);
        Sort sortByCreateTime = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest page = PageRequest.of(current>0?current-1:0, pageSize>0?pageSize:1,sortByCreateTime);
        Page<TreatEntity> allByPage = treatService.findAllByPage(customerIds, startTime, endTime, page);
        PageResp<TreatView> resp = new PageResp<TreatView>();
        resp.success = true;
        resp.total = allByPage.getTotalElements();
        resp.pages = allByPage.getTotalPages();
        resp.data = new ArrayList<>();
        allByPage.stream().forEach(e->{
            resp.data.add(TreatView.FromTreatEntity(e));
        });
        return resp;
    }

    @GetMapping("/fuzzy")
    public PageResp<TreatView> findTreatByFuzzy(@Validated @NotNull @RequestParam int current, @Validated @NotNull @RequestParam int pageSize, @Validated @Nullable @RequestParam("id") Long id,@Validated @Nullable @RequestParam("name") String name,@Validated @Nullable @RequestParam("phone") String phone,@Validated @Nullable @RequestParam("age") Integer age, @Validated @Nullable @RequestParam String startTime, @Validated @Nullable @RequestParam String endTime) throws IllegalArgumentException {
        PageResp<TreatView> resp = new PageResp<TreatView>();
        resp.data = new ArrayList<>();
        resp.success = true;
        if (id != null){
            resp.pages=1;
            Optional<TreatEntity> byId = treatService.findById(id);
            if (!byId.isEmpty()){
                resp.data.add(toTreatView(byId.get()));
                resp.total = 1L;
            }
            return resp;
        }
        PageRequest pageRequest = PageRequest.ofSize(Integer.MAX_VALUE);
        Page<CustomerEntity> customerByPage = customerService.findAllByPage(name, age, phone, null, null, pageRequest);
        if (customerByPage.getTotalElements() == 0){
            return resp;
        }
        List<Long> customerIds = new ArrayList<>();
        customerByPage.forEach((e)->{
            customerIds.add(e.getId());
        });
        Sort sortByCreateTime = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest page = PageRequest.of(current>0?current-1:0, pageSize>0?pageSize:1,sortByCreateTime);
        Page<TreatEntity> allByPage = treatService.findAllByPage(customerIds, startTime, endTime, page);
        resp.total = allByPage.getTotalElements();
        resp.pages = allByPage.getTotalPages();
        allByPage.stream().forEach(e->{
            resp.data.add(toTreatView(e));
        });
        return resp;
    }

    private TreatView toTreatView(TreatEntity treatEntity){
        Long customerId = treatEntity.getCustomerId();
        TreatView treatView = TreatView.FromTreatEntity(treatEntity);
        if (customerId!= null){
            Optional<CustomerEntity> customerEntityOptional = customerService.findById(customerId);
            if (customerEntityOptional.isPresent()){
                CustomerEntity customerEntity = customerEntityOptional.get();
                treatView.setAge(customerEntity.getAge());
                treatView.setName(customerEntity.getName());
                treatView.setGender(customerEntity.getGender());
                treatView.setPhone(customerEntity.getPhone());
            }
        }
        return treatView;
    }

    @Operation(summary = "查询疗程表", description = "根据疗程表id来查询用表", parameters = {
            @Parameter(name = "id", description = "表Id")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "疗程表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TreatView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @GetMapping("/{id}")
    public TreatView findTreatById(@PathVariable("id") Long id) throws IllegalArgumentException {
        Optional<TreatEntity> treat = treatService.findById(id);
        if (treat.isEmpty()) {
            throw new IllegalArgumentException("没有找到该ID的疗程表");
        }
        return TreatView.FromTreatEntity(treat.get());
    }

    @Operation(summary = "创建疗程表", description = "创建新的疗程表", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = TreatView.class)
    ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "疗程表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TreatView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @PostMapping("/")
    public TreatView createTreat(@Validated @NotNull @RequestBody TreatView treat) throws IllegalArgumentException {
        Long customerId = treat.getCustomerId();
        Optional<CustomerEntity> user = customerService.findById(customerId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        TreatEntity entity = treat.ToTreatEntity();
        TreatEntity save = treatService.save(entity);
        TreatView treatView = TreatView.FromTreatEntity(save);
        treatView.setName(user.get().getName());
        treatView.setAge(user.get().getAge());
        treatView.setGender(user.get().getGender());
        return treatView;
    }

    @Operation(summary = "修改疗程表", description = "修改已有的疗程表", responses = {
            @ApiResponse(responseCode = "200", description = "疗程表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TreatView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @PutMapping("/")
    public TreatView updateTreat(@Validated @RequestBody TreatView treat) {
        TreatEntity entity = new TreatEntity();
        entity.setId(treat.getId());
        treatService.save(entity);
        return TreatView.builder().build();
    }

    @Operation(summary = "创建疗程表", description = "创建新的疗程表", responses = {
            @ApiResponse(responseCode = "200", description = "疗程表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TreatView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @DeleteMapping("/{id}")
    public MessageView deleteTreat(@Validated @PathVariable Long id) {
        treatService.deleteById(id);
        return MessageView.builder().message("ok").build();
    }
}

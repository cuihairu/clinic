package com.clinic.controller;

import com.clinic.entity.CustomerEntity;
import com.clinic.service.CustomerService;
import com.clinic.util.PhoneValidationUtil;
import com.clinic.vo.*;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "客人", description = "客人API")
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @Operation(summary = "根据ID查询客人", description = "根据客人的id来查询客人信息",
            parameters = {
                    @Parameter(name = "id", description = "客人Id")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "客人表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerView.class)
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
    public CustomerView findUserById(@PathVariable("id") Long id) throws IllegalArgumentException {
        Optional<CustomerEntity> customerEntity = customerService.findById(id);
        if (customerEntity.isEmpty()) {
            throw new IllegalArgumentException("顾客信息不存在,请先建档");
        }
        return CustomerView.FromCustomerEntity(customerEntity.get());
    }

    @Operation(summary = "根据手机号码查询客人", description = "根据手机号码查询客人信息",
            parameters = {
                    @Parameter(name = "phone", description = "客人手机号码")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "客人表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerView.class)
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
    @GetMapping("/phone/{phone}")
    public CustomerView findUserByPhone(@PathVariable("phone") String phone) throws IllegalArgumentException {
        Optional<CustomerEntity> userEntity = customerService.findByPhone(phone);
        if (userEntity.isEmpty()) {
            throw new IllegalArgumentException("当前号码没有注册");
        }
        return CustomerView.FromCustomerEntity(userEntity.get());
    }

    @GetMapping("/page")
    public PageResp<CustomerView> findByPage(@Validated @NotNull @RequestParam int current, @Validated @NotNull @RequestParam int pageSize,@Validated @Nullable @RequestParam String name, @Validated @Nullable @RequestParam Integer age,@Validated @Nullable @RequestParam String phone,@Validated @Nullable @RequestParam String startTime,@Validated @Nullable @RequestParam String endTime){
        PageRequest page = PageRequest.of(current>0?current-1:0, pageSize>0?pageSize:1);
        Page<CustomerEntity> allByPage = customerService.findAllByPage(name,age,phone,startTime,endTime,page);
        List<CustomerView> data = new ArrayList<>();
        for (CustomerEntity customer : allByPage) {
            data.add(CustomerView.FromCustomerEntity(customer));
        }
        PageResp<CustomerView> customerViewPageResp = new PageResp<CustomerView>();
        customerViewPageResp.data = data;
        customerViewPageResp.pages = allByPage.getTotalPages();
        customerViewPageResp.total = allByPage.getTotalElements();
        customerViewPageResp.success = true;
        return customerViewPageResp;
    }

    @Operation(summary = "根据名字号码查询客人",
            description = "根据名字码查询客人信息",
            parameters = {
                    @Parameter(name = "name", description = "客人名字")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "客人表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerView.class)
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
    @GetMapping("/name/{name}")
    public List<CustomerView> findUserByName(@PathVariable("name") String name) throws IllegalArgumentException {
        List<CustomerEntity> userEntities = customerService.findByName(name);
        if (userEntities.isEmpty()) {
            throw new IllegalArgumentException("没有找到对应名字的客人");
        }
        List<CustomerView> ret = new ArrayList<>();
        for (CustomerEntity userEntity : userEntities) {
            ret.add(CustomerView.FromCustomerEntity(userEntity));
        }
        return ret;
    }

    @Operation(summary = "创建客人",
            description = "创建一个新客人",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CustomerView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "客人表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerView.class)
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
    public CustomerView creatCustomer(@Validated @RequestBody CustomerView customerView) throws IllegalArgumentException {
        if (customerView.getGender() == null) {
            throw new IllegalArgumentException("请输入选择客人的性别");
        }
        String phone = customerView.getPhone();
        if (phone == null || phone.isEmpty()) {
            CustomerEntity save = customerService.save(customerView.toCustomerEntity());
            return CustomerView.FromCustomerEntity(save);
        }
        if (customerView.getAge() == null){
            customerView.setAge(20);
        }
        if (customerView.getLevel() == null){
            customerView.setLevel(0);
        }
        if (customerView.getBirthday() == null){
            customerView.setBirthday(new Date());
        }
        phone = phone.trim();
        if (phone.startsWith("+86")){
            phone = phone.substring(3);
        }
        if (!PhoneValidationUtil.isPhone(phone)){
            throw new IllegalArgumentException("请输入正确的手机号码");
        }
        customerService.findByPhone(phone).ifPresent(staffEntity -> {
            throw new IllegalArgumentException("该手机号码已经绑定了顾客");
        });
        customerView.setPhone(phone);

        CustomerEntity save = customerService.save(customerView.toCustomerEntity());
        return CustomerView.FromCustomerEntity(save);
    }

    @Operation(summary = "更新客人",
            description = "根据用户的id来更新客人信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TreatView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "客人表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerView.class)
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
    public CustomerView updateUser(@Validated @RequestBody CustomerView customerView) throws IllegalArgumentException {
        Long id = customerView.getId();
        if (id == null || id < 0){
            throw new IllegalArgumentException("用户id错误，请传入正确的id");
        }
        CustomerEntity customerEntity = customerService.findById(id).orElseThrow(()->{
            throw new IllegalArgumentException("用户不存在");
        });
        customerView.updateCustomerEntity(customerEntity);
        CustomerEntity save = customerService.save(customerEntity);
        return customerView.FromCustomerEntity(save);
    }

    @Operation(summary = "删除客人", description = "根据用户的id来删除客人信息")
    @DeleteMapping("/{id}")
    public MessageView deleteUser(@PathVariable("id") Long id) {
        Optional<CustomerEntity> userEntity = customerService.findById(id);
        if (userEntity.isEmpty()) {
            return MessageView.builder().message("用户不存在").build();
        }
        customerService.deleteById(id);
        return MessageView.builder().message("删除成功").build();
    }

}

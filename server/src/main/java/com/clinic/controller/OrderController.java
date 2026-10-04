package com.clinic.controller;

import com.clinic.vo.ExceptionView;
import com.clinic.vo.MessageView;
import com.clinic.vo.OrderView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;


@Tag(name="订单",description = "订单API")
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {
    @Operation(summary = "创建新订单",description = "创建一个新订单",
            responses = {
                    @ApiResponse(responseCode = "200",description = "订单表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderView.class)
                    )),
                    @ApiResponse(responseCode = "400",description = "参数错误",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500",description = "服务器参数",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @PostMapping("/")
    public OrderView createOrder(){
        return OrderView.builder().build();
    }

    @Operation(summary = "更新订单信息",description = "更新一个订单信息",
            responses = {
                    @ApiResponse(responseCode = "200",description = "订单表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderView.class)
                    )),
                    @ApiResponse(responseCode = "400",description = "参数错误",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500",description = "服务器参数",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @PutMapping("/")
    public OrderView updateOrder(){
        return OrderView.builder().build();
    }

    @Operation(summary = "根据订单id查询订单信息",description = "查询订单信息",
            responses = {
                    @ApiResponse(responseCode = "200",description = "订单表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderView.class)
                    )),
                    @ApiResponse(responseCode = "400",description = "参数错误",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500",description = "服务器参数",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @GetMapping("/{id}")
    public OrderView findOrder(Long id){
        return OrderView.builder().build();
    }

    @Operation(summary = "根据用户id查询订单信息",description = "查询订单信息",
            responses = {
                    @ApiResponse(responseCode = "200",description = "订单表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderView.class)
                    )),
                    @ApiResponse(responseCode = "400",description = "参数错误",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500",description = "服务器参数",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @GetMapping("/user/{id}")
    public OrderView findOrderByUserId(Long id){
        return OrderView.builder().build();
    }

    @Operation(summary = "删除订单信息",description = "删除一个订单信息",
            responses = {
                    @ApiResponse(responseCode = "200",description = "订单表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderView.class)
                    )),
                    @ApiResponse(responseCode = "400",description = "参数错误",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500",description = "服务器参数",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @DeleteMapping("/{id}")
    public OrderView deleteOrder(Long id){
        return OrderView.builder().build();
    }
}

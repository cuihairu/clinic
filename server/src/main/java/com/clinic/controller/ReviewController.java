package com.clinic.controller;

import com.clinic.entity.ReviewCustomerEntity;
import com.clinic.entity.ReviewEntity;
import com.clinic.entity.ReviewStaffEntity;
import com.clinic.service.ReviewService;
import com.clinic.util.DateUtil;
import com.clinic.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Tag(name="复盘",description = "今天报表API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final ReviewService reviewService;
    @Operation(summary = "创建新复盘",description = "创建一个复盘",
            responses = {
                    @ApiResponse(responseCode = "200",description = "复盘表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewView.class)
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
    public ReviewView createView(@Validated @NotNull  @RequestBody ReviewView reviewView){
        ReviewEntity reviewEntity = reviewService.saveView(reviewView.toReviewEntity());
        return ReviewView.fromReviewEntity(reviewEntity);
    }

    @Operation(summary = "查询复盘总结",description = "根据Id查询复盘总结",
            responses = {
                    @ApiResponse(responseCode = "200",description = "复盘表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewView.class)
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
    public ReviewView findOrderById(Long id){
        return ReviewView.builder().build();
    }

    @Operation(summary = "根据日期查询复盘总结",description = "根据日期查询复盘总结",
            responses = {
                    @ApiResponse(responseCode = "200",description = "复盘表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewView.class)
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
    @GetMapping("/day")
    public ReviewView findViewByDay(@RequestParam("day") @DateTimeFormat(pattern="yyyy-MM-dd hh:mm:ss")Date day){
        Optional<ReviewEntity> viewByDate = reviewService.findViewByDate(day);
        if (viewByDate.isEmpty()){
            return ReviewView.builder().day(day).good("").improvement("").build();
        }
        return ReviewView.fromReviewEntity(viewByDate.get());
    }

    @Operation(summary = "删除复盘信息",description = "删除一个复盘信息",
            responses = {
                    @ApiResponse(responseCode = "200",description = "复盘表",content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewView.class)
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
    public ReviewView deleteView(Long id){
        return ReviewView.builder().build();
    }


    @PostMapping("/staff")
    public ReviewStaffView createStaffReview(@Validated @RequestBody ReviewStaffView reviewStaffView){
        ReviewStaffEntity save = reviewService.saveStaffView(reviewStaffView.toReviewStaffEntity());
        return ReviewStaffView.fromReviewStaffEntity(save);
    }

    @PostMapping("/customer")
    public ReviewCustomerView createCustomerReview(@Validated @RequestBody ReviewCustomerView reviewCustomerView){
        ReviewCustomerEntity save = reviewService.saveCustomerView(reviewCustomerView.toReviewCustomerEntity());
        return ReviewCustomerView.fromReviewCustomerEntity(save);
    }
    @DeleteMapping("/staff/{id}")
    public MessageView deleteStaffReview(@PathVariable("id") Long id){
        reviewService.deleteStaffViewById(id);
        return MessageView.builder().message("ok").build();
    }

    @DeleteMapping("/customer/{id}")
    public MessageView deleteCustomerReview(@PathVariable("id") Long id){
        reviewService.deleteCustomerViewById(id);
        return MessageView.builder().message("ok").build();
    }

    /**
     * 批量创建
     * */
    @PostMapping("/staff/bulk")
    public MessageView createStaffReviewBulk(@Validated @RequestBody List<ReviewStaffView> reviewStaffViews){
        if(reviewStaffViews == null || reviewStaffViews.size() ==0){
            return MessageView.builder().message("ok").build();
        }
        for (ReviewStaffView reviewStaffView : reviewStaffViews) {
            if (reviewStaffView.getId() <=0){
                reviewStaffView.setId(null);
            }
            reviewService.saveStaffView(reviewStaffView.toReviewStaffEntity());
        }
        return MessageView.builder().message("ok").build();
    }
    @PostMapping("/customer/bulk")
    public MessageView createCustomerReviewBulk(@Validated @RequestBody List<ReviewCustomerView> reviewCustomerViews){
        if(reviewCustomerViews == null || reviewCustomerViews.size() ==0){
            return MessageView.builder().message("ok").build();
        }
        for (ReviewCustomerView reviewCustomerView: reviewCustomerViews) {
            if (reviewCustomerView.getId() <=0){
                reviewCustomerView.setId(null);
            }
            reviewService.saveCustomerView(reviewCustomerView.toReviewCustomerEntity());
        }
        return MessageView.builder().message("ok").build();
    }

    @GetMapping("/staff/list/day")
    public List<ReviewStaffView> getStaffReviewListByDay(@RequestParam("day") @DateTimeFormat(pattern="yyyy-MM-dd hh:mm:ss")Date day){
        String d = DateUtil.stdFormat(day);
        log.info(d);
        List<ReviewStaffEntity> list = reviewService.findStaffViewByDate(day);
        ArrayList<ReviewStaffView> data = new ArrayList<>();
        list.forEach(reviewStaffEntity -> {
            ReviewStaffView reviewStaffView = ReviewStaffView.fromReviewStaffEntity(reviewStaffEntity);
            data.add(reviewStaffView);
        });
        return data;
    }
    @GetMapping("/customer/list/day")
    public List<ReviewCustomerView> getCustomerReviewListByDay(@RequestParam("day") @DateTimeFormat(pattern="yyyy-MM-dd hh:mm:ss")Date day){
        String d = DateUtil.stdFormat(day);
        List<ReviewCustomerEntity> list = reviewService.findCustomerViewByDate(day);
        ArrayList<ReviewCustomerView> data = new ArrayList<>();
        list.forEach(entity -> {
            ReviewCustomerView view = ReviewCustomerView.fromReviewCustomerEntity(entity);
            data.add(view);
        });
        return data;
    }
}

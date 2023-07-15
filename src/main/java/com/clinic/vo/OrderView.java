package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.Date;

@Builder
@Data
@Schema(title = "订单信息")
public class OrderView implements Serializable {
    @Schema(title = "订单id",description = "订单id",example = "1234567890")
    private Long id;

    @Schema(title = "用户id",description = "用户id",example = "1234567890")
    private Long customerId;

    @Schema(title = "商品id",description = "商品id",example = "1234567890")
    private Integer itemId;

    @Schema(title = "员工id",description = "员工Id",example = "1")
    private Integer staffId;

    @Schema(title = "外部订单id",description = "外部订单支付的id,比如支付宝,微信",example = "ZFB12654176319873971973912")
    private String extOrderId;

    @Schema(title = "支付类型",description = "支付的类型，0 支付宝 1 微信 3 现金 4 刷卡")
    private Integer payType;

    @Schema(title = "订单的状态",description = "支付的状态")
    private Integer status;
    
    @Schema(title = "价格",description = "支付时服务的价格")
    private Double price;

    @Schema(title = "支付款项",description = "实际支付的钱")
    private Double payment;

    @Schema(title = "创建时间",description = "订单的生成时间",example = "2013-01-01 12:00:12")
    private Date createTime;

    @Schema(title = "更新时间",description = "订单的最后修改时间",example = "2013-01-01 12:00:12")
    private Date updateTime;
}

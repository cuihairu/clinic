package com.sinomed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * 次卡核销流水（推拿/艾灸等疗程卡的消费记录）：每次结算抵扣落一行，append-only。
 * times_used 记录本次是第几次（总次数-扣后余次）；老卡补录/历史迁移可无订单（order_id 可空）。
 */
@Data
@Entity
@Table(name = "`card_usages`", indexes = {
        @Index(name = "idx_card_usage_card", columnList = "card_id")
})
@EntityListeners(AuditingEntityListener.class)
public class CardUsageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    /** 触发抵扣的订单（补录口径可空） */
    @Column(name = "order_id")
    private Long orderId;

    /** 服务/操作员工（取订单 staffId，可空） */
    @Column(name = "staff_id")
    private Long staffId;

    /** 本次是第几次消费（1 起） */
    @Column(name = "times_used", nullable = false)
    private Integer timesUsed;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

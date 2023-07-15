package com.clinic.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@Table(name = "`recharge`",indexes = {
        @Index(name = "idx_recharge_user_id",columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
public class RechargeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(name = "money")
    private Integer money;

    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

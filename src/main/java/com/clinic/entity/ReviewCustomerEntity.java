package com.clinic.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@Table(name = "`review_customer`",indexes = {
        @Index(name = "idx_review_customer_customer_id",columnList = "customer_id"),
        @Index(name = "idx_review_customer_day",columnList = "day")
})
@EntityListeners(AuditingEntityListener.class)
public class ReviewCustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "last")
    private Date last;

    @Column(name = "day",nullable = false) //回访日
    private Date day;

    @Column(name = "advice")
    private String advice;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

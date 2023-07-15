package com.clinic.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@Table(name = "`review`",indexes = {
        @Index(name = "idx_review_create_time",columnList = "create_time"),
        @Index(name = "idx_review_day",columnList = "day")
})
@EntityListeners(AuditingEntityListener.class)
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "day",nullable = false,unique = true)
    private Date day;
    @Column(name = "improvement",columnDefinition = "TEXT")
    private String improvement;
    @Column(name = "good",columnDefinition = "TEXT")
    private String good;
    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;
    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

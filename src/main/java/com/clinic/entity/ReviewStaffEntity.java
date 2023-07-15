package com.clinic.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@Table(name = "`review_staff`",indexes = {
        @Index(name = "idx_review_staff_day",columnList = "day")
})
@EntityListeners(AuditingEntityListener.class)
public class ReviewStaffEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "name")
    private String name;

    @Column(name = "cost")
    private Integer cost;

    @Column(name = "done",columnDefinition = "TEXT")
    private String done;

    @Column(name = "advice",columnDefinition = "TEXT")
    private String advice;

    @Column(name = "day", nullable = false)
    private Date day;

    @CreatedDate
    @Column(name = "createTime", nullable = false)
    private Date createTime;

    @LastModifiedDate
    @Column(name = "updateTime", nullable = false)
    private Date updateTime;

}

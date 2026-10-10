package com.sinomed.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@Table(name = "staff_leaves", indexes = {
        @Index(name = "idx_staff_leave_staff_id", columnList = "staff_id")
})
@EntityListeners(AuditingEntityListener.class)
public class StaffLeaveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** 请假员工 id */
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    /** 类型：0 病假 / 1 事假 */
    @Column(name = "leave_type", nullable = false)
    private Integer leaveType;

    /** 事由 */
    @Column(name = "reason", nullable = false)
    private String reason;

    /** 起始时间 */
    @Column(name = "start_time", nullable = false)
    private Date startTime;

    /** 结束时间 */
    @Column(name = "end_time", nullable = false)
    private Date endTime;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

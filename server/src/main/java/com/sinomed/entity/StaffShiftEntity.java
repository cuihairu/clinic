package com.sinomed.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * 员工周期班表：员工 × 星期 × 班次时段（周一=1 … 周日=7），按周循环。
 * 口径：同一员工同一星期仅一条班次；时段为 HH:mm 文本；考勤打卡仍以 signs 为准，班表只做排班参考。
 */
@Data
@Entity
@Table(name = "`staff_shifts`", indexes = {
        @Index(name = "idx_staff_shift_sid", columnList = "staff_id"),
        @Index(name = "idx_staff_shift_weekday", columnList = "weekday")
})
@EntityListeners(AuditingEntityListener.class)
public class StaffShiftEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** 员工 id（非空，staffs.id） */
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    /** 星期：1 周一 … 7 周日 */
    @Column(name = "weekday", nullable = false)
    private Integer weekday;

    /** 上班时间 HH:mm（如 09:00） */
    @Column(name = "start_time", nullable = false)
    private String start;

    /** 下班时间 HH:mm（如 18:00） */
    @Column(name = "end_time", nullable = false)
    private String end;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

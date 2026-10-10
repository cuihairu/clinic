package com.sinomed.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@Table(name = "`prescriptions`", indexes = {
        @Index(name = "idx_prescription_customer_id", columnList = "customer_id"),
        @Index(name = "idx_prescription_treat_id", columnList = "treat_id")
})
@EntityListeners(AuditingEntityListener.class)
public class PrescriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** 关联接诊单（可空：随到随开） */
    @Column(name = "treat_id")
    private Long treatId;

    /** 顾客 id */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** 开方医师 id（可空） */
    @Column(name = "staff_id")
    private Long staffId;

    /** 剂数（几付） */
    @Column(name = "doses", nullable = false)
    private Integer doses;

    /** 用法：煎服法/频次/代煎说明等自由文本 */
    @Column(name = "`usage`")
    private String usage;

    @Column(name = "remark")
    private String remark;

    /** 代煎状态：0 无需代煎 / 1 待煎 / 2 可取 / 3 已取 */
    @Column(name = "decoction_status", nullable = false)
    private Integer decoctionStatus;

    /** 代煎袋数（=剂数；无需代煎为空） */
    @Column(name = "decoction_bags")
    private Integer decoctionBags;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

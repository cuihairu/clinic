package com.sinomed.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * 病症处方模板：按病症归组的常用药味组合，开方页「套用模板」一键带出药味/剂数/用法。
 * 口径：模板名唯一；模板只存建议值，套用后随开方单自由改（不改回模板）。
 */
@Data
@Entity
@Table(name = "`prescription_templates`", indexes = {
        @Index(name = "idx_template_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
public class PrescriptionTemplateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** 病症名（如「风寒感冒」），唯一 */
    @Column(name = "name", nullable = false)
    private String name;

    /** 建议剂数（默认 7） */
    @Column(name = "doses")
    private Integer doses;

    /** 建议代煎：0 无需 / 1 代煎 */
    @Column(name = "decoction", nullable = false)
    private Integer decoction;

    /** 建议用法：煎服法/频次/代煎说明等自由文本，可空 */
    @Column(name = "`usage`")
    private String usage;

    @Column(name = "remark")
    private String remark;

    /** 上架：0 停用 / 1 启用（停用不在开方页出现） */
    @Column(name = "enabled", nullable = false)
    private Integer enabled;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

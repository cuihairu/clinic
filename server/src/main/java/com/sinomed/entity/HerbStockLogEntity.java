package com.sinomed.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * 饮片出入库流水：药材 × 入库/出库 × 数量（克）。
 * 口径：只记流水，当前库存 = 入库合计 - 出库合计；出库按效期先进先出（FEFO）消耗批次，
 * 批次效期用于「近期到期」预警；不涉采购单据与结算，库存为演示口径台账。
 */
@Data
@Entity
@Table(name = "`herb_stock_logs`", indexes = {
        @Index(name = "idx_herb_stock_herb_id", columnList = "herb_id"),
        @Index(name = "idx_herb_stock_type", columnList = "type")
})
@EntityListeners(AuditingEntityListener.class)
public class HerbStockLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** 药材 id（非空，herbs.id） */
    @Column(name = "herb_id", nullable = false)
    private Long herbId;

    /** 1 入库 / 0 出库 */
    @Column(name = "type", nullable = false)
    private Integer type;

    /** 数量：克（正整数） */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** 批次效期（入库登记，出库可为空） */
    @Column(name = "expiry")
    private Date expiry;

    /** 供货方（≤50 字，可空） */
    @Column(name = "supplier")
    private String supplier;

    /** 备注（≤100 字，可空） */
    @Column(name = "note")
    private String note;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

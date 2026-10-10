package com.sinomed.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 模板药味：与 prescription_items 同构（药材名自由文本 + 单剂克数 + 特殊煎法 + 顺序）。
 */
@Data
@Entity
@Table(name = "`prescription_template_items`", indexes = {
        @Index(name = "idx_template_item_tid", columnList = "template_id")
})
public class PrescriptionTemplateItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    /** 药材名（如「黄芪」） */
    @Column(name = "herb", nullable = false)
    private String herb;

    /** 单剂剂量（克，可为小数如 4.5） */
    @Column(name = "weight", nullable = false)
    private Double weight;

    /** 特殊煎法：先煎/后下/包煎/烊化/冲服等，可空 */
    @Column(name = "special")
    private String special;

    /** 展示顺序，小者在前 */
    @Column(name = "sort", nullable = false)
    private Integer sort;
}

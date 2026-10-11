package com.sinomed.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * 方剂库：经典方剂参考（方名 + 出处 + 功效主治），药味组成在 formula_items。
 * 口径：方名唯一；pinyin 存全拼小写码，供「xiaoyaosan」式拼音检索。
 */
@Data
@Entity
@Table(name = "`formulas`", indexes = {
        @Index(name = "idx_formula_name", columnList = "name"),
        @Index(name = "idx_formula_pinyin", columnList = "pinyin")
})
@EntityListeners(AuditingEntityListener.class)
public class FormulaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** 方名（如「逍遥散」），唯一 */
    @Column(name = "name", nullable = false)
    private String name;

    /** 拼音检索码：全拼小写，如 xiaoyaosan */
    @Column(name = "pinyin", nullable = false)
    private String pinyin;

    /** 出处（如「太平惠民和剂局方」），可空 */
    @Column(name = "source")
    private String source;

    /** 功效主治简述，可空 */
    @Column(name = "indication")
    private String indication;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

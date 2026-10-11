package com.sinomed.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * 穴位字典：经络穴位参考（穴名 + 归经 + 定位 + 主治）。
 * 口径：穴名唯一；pinyin 存全拼小写码，供「zusanli」式拼音检索；接诊单取穴字段仍为自由文本。
 */
@Data
@Entity
@Table(name = "`acupoints`", indexes = {
        @Index(name = "idx_acupoint_name", columnList = "name"),
        @Index(name = "idx_acupoint_pinyin", columnList = "pinyin")
})
@EntityListeners(AuditingEntityListener.class)
public class AcupointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** 穴位名（如「足三里」），唯一 */
    @Column(name = "name", nullable = false)
    private String name;

    /** 拼音检索码：全拼小写，如 zusanli */
    @Column(name = "pinyin", nullable = false)
    private String pinyin;

    /** 归经（如「足阳明胃经」；经外奇穴归「经外奇穴」） */
    @Column(name = "meridian", nullable = false)
    private String meridian;

    /** 体表定位，可空 */
    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    /** 主治简述，可空 */
    @Column(name = "indication", columnDefinition = "TEXT")
    private String indication;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

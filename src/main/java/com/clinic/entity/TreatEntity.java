package com.clinic.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Setter
@Getter
@Entity
@Table(name = "`treat`",indexes = {
        @Index(name = "idx_treat_cid",columnList = "cid"),
        @Index(name = "idx_treat_create_time",columnList = "create_time")
})
@EntityListeners(AuditingEntityListener.class)
public class TreatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    // 客人id
    @Column(name = "cid",nullable = false)
    Long customerId;

    // 主诉
    @Column(name = "cdesc",columnDefinition = "TEXT")
    String desc;

    // 问诊
    @Column(name = "inquiry",columnDefinition = "TEXT")
    String inquiry;

    // 望诊
    @Column(name = "observation",columnDefinition = "TEXT")
    String observation;

    // 触诊
    @Column(name = "palpation",columnDefinition = "TEXT")
    String palpation;

    // 脉象
    @Column(name = "pulse")
    String pulse;

    // 脉象 左手
    @Column(name = "pulse_left",columnDefinition = "TEXT")
    String pulseLeft;

    // 脉象 右手
    @Column(name = "pulse_right",columnDefinition = "TEXT")
    String pulseRight;

    // 脉象 信息
    @Column(name = "pulse_info",columnDefinition = "TEXT")
    String pulseInfo;

    // 五行 本子 左手
    @Column(name = "five_ben_zi_left")
    String fiveBenZiLeft;
    @Column(name = "five_zi_ben_left")
    String fiveZiBenLeft;
    @Column(name = "five_ben_zi_right")
    String fiveBenZiRight;
    @Column(name = "five_zi_ben_right")
    String fiveZiBenRight;

    @Column(name = "five_ben_ke_left")
    String fiveBenKeLeft;
    @Column(name = "five_ke_ben_left")
    String fiveKeBenLeft;
    @Column(name = "five_ben_ke_right")
    String fiveBenKeRight;
    @Column(name = "five_ke_ben_right")
    String fiveKeBenRight;

    // 难经
    @Column(name = "five_nan_jin_left")
    String fiveNanJinLeft;
    @Column(name = "five_nan_jin_big_left")
    String fiveNanJinBigLeft;
    @Column(name = "five_nan_jin_small_left")
    String fiveNanJinSmallLeft;
    @Column(name = "five_nan_jin_right")
    String fiveNanJinRight;
    @Column(name = "five_nan_jin_big_right")
    String fiveNanJinBigRight;
    @Column(name = "five_nan_jin_small_right")
    String fiveNanJinSmallRight;

    @Column(name = "five_rate_left",columnDefinition = "TEXT")
    String fiveRateLeft;

    @Column(name = "five_rate_right",columnDefinition = "TEXT")
    String fiveRateRight;

    // 辅助
    @Column(name = "five_aux_left",columnDefinition = "TEXT")
    String fiveAuxLeft;

    @Column(name = "five_aux_right",columnDefinition = "TEXT")
    String fiveAuxRight;

    // 取穴 左手
    @Column(name = "acupoint_left",columnDefinition = "TEXT")
    String acupointLeft;

    // 取穴 右手
    @Column(name = "acupoint_right",columnDefinition = "TEXT")
    String acupointRight;

    // 诊断
    @Column(name = "diagnose",columnDefinition = "TEXT")
    String diagnose;

    // 方案
    @Column(name = "plan",columnDefinition = "TEXT")
    String plan;

    // 饮食
    @Column(name = "diet",columnDefinition = "TEXT")
    String diet;

    // 调理过程
    @Column(name = "conditioning",columnDefinition = "TEXT")
    String Conditioning;

    // 回访
    @Lob
    @Column(name = "review",columnDefinition = "TEXT")
    String review;

    @CreatedDate
    @Column(name = "create_time")
    Date createTime;
    @LastModifiedDate
    @Column(name = "update_time")
    Date updateTime;
}

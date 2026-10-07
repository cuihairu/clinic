package com.sinomed.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * 广告屏（Kiosk 注册）：平板以 code 标识自己，拉排期时顺带上报心跳。
 */
@Data
@Entity
@Table(name = "`ad_screens`", indexes = {
        @Index(name = "idx_ad_screen_code", columnList = "code", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class AdScreenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** 屏标识，唯一；平板配置里填（如 PAD-01） */
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    /** 名称（如「一楼候诊区」） */
    @Column(name = "name", nullable = false)
    private String name;

    /** 定向位置（候诊区 / 诊室） */
    @Column(name = "location")
    private String location;

    /** 1 启用、0 停用 */
    @Column(name = "enabled", nullable = false)
    private Integer enabled;

    /** 最近心跳（平板拉排期时顺带上报） */
    @Column(name = "last_seen_at")
    private Date lastSeenAt;

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

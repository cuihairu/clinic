package com.clinic.entity;

import com.clinic.util.DateUtil;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@Table(name = "`sign`",indexes = {
        @Index(name = "idx_sign_staff_id",columnList = "staff_id")
})
@EntityListeners(AuditingEntityListener.class)
public class SignEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "type")
    private Integer type; // 1 上班 0 下班

    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 转换为当日的0点整的时间
     * */
    public Date getDayZeroTime(){
        return DateUtil.getZeroTime(createTime);
    }
}

package com.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@Table(name = "`customer`",indexes = {
        @Index(name = "idx_customer_name",columnList = "name"),
        @Index(name = "idx_customer_phone",columnList = "phone"),
        @Index(name = "idx_customer_level",columnList = "level"),
        @Index(name = "idx_customer_birthday",columnList = "birthday")
})
@EntityListeners(AuditingEntityListener.class)
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Long id;
    @Column(name = "name",nullable = false)
    private String name;
    @Column(name = "age")
    private Integer age;
    @Column(name = "level")
    private Integer level;
    @Column(name = "phone",unique = true)
    private String phone;
    @Column(name = "gender")
    private Integer gender;//0 女 1 男
    @Column(name = "address")
    private String address;
    @Column(name = "birthday")
    private Date birthday;
    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;
    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}

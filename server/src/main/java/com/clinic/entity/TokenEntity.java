package com.clinic.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

//@Data
//@Entity
//@Table(name = "`token`",indexes = {
//        @Index(name = "idx_item_name",columnList = "name")
//})
//@EntityListeners(AuditingEntityListener.class)
public class TokenEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    public Integer id;
//
//    @Column(unique = true)
//    public String token;
//
//    @Enumerated(EnumType.STRING)
//    public TokenType tokenType = TokenType.BEARER;
//
//    public boolean revoked;
//
//    public boolean expired;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    public User user;
}

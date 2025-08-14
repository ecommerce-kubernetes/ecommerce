package com.example.couponservice.jpa.entity;


import com.example.couponservice.advice.exceptions.AlreadyUsedCouponException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user_coupons")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DependsOn("couponEntity")
public class UserCouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private CouponEntity coupon;

    @Column(nullable = false)
    private boolean used;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime issuedAt;

    @Column
    private LocalDateTime usedAt;

    @Column
    private LocalDateTime expiresAt;

    @Builder

    public UserCouponEntity(Long userId, String phoneNumber, CouponEntity coupon, boolean used, LocalDateTime usedAt, LocalDateTime expiresAt) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.coupon = coupon;
        this.used = used;
        this.usedAt = usedAt;
        this.expiresAt = expiresAt;
    }

    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void markAsUsed(LocalDateTime usedAt) {
        if (this.used) {
            throw new AlreadyUsedCouponException("이미 사용한 쿠폰입니다.");
        }
        this.used = true;
        this.usedAt = usedAt;
    }
}

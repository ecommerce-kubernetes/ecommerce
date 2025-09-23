package com.example.couponservice.vo;

import com.example.common.DiscountType;
import com.example.couponservice.jpa.entity.CouponEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUserCoupon {
    private Long id;
    private ResponseCoupon coupon;
    private boolean used;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expiresAt;

    @Builder
    public ResponseUserCoupon(Long id, ResponseCoupon responseCoupon, boolean used, LocalDateTime issuedAt, LocalDateTime usedAt, LocalDateTime expiresAt) {
        this.id = id;
        this.coupon = responseCoupon;
        this.used = used;
        this.issuedAt = issuedAt;
        this.usedAt = usedAt;
        this.expiresAt = expiresAt;
    }
}

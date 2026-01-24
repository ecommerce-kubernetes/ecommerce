package com.example.order_service.api.order.domain.model.vo;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponInfo {
    private Long couponId;
    private String couponName;
    private Long discountAmount;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponInfo(Long couponId, String couponName, Long discountAmount){
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountAmount = discountAmount;
    }

    public static CouponInfo of(Long couponId, String couponName, Long discountAmount) {
        return CouponInfo.builder()
                .couponId(couponId)
                .couponName(couponName)
                .discountAmount(discountAmount)
                .build();
    }
}

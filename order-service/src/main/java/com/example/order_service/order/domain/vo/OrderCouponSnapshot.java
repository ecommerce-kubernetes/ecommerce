package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCouponSnapshot {
    private Long couponId;
    private String couponName;
    private Money discountAmount;

    @Builder(builderMethodName = "reconstitute")
    private OrderCouponSnapshot(Long couponId, String couponName, Money discountAmount) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountAmount = discountAmount;
    }

    public static OrderCouponSnapshot of(Long couponId, String couponName, Money discountAmount) {
        if (couponId == null) {
            throw new IllegalArgumentException("적용 쿠폰 Id 는 필수 입니다");
        }
        if (couponName == null || couponName.isBlank()) {
            throw new IllegalArgumentException("적용 쿠폰 이름은 필수입니다");
        }
        if (discountAmount == null) {
            throw new IllegalArgumentException("적용 쿠폰 할인금은 0원 이상이여야 합니다");
        }
        return OrderCouponSnapshot.reconstitute()
                .couponId(couponId)
                .couponName(couponName)
                .discountAmount(discountAmount)
                .build();
    }

    public static OrderCouponSnapshot empty() {
        return OrderCouponSnapshot.reconstitute()
                .couponId(null)
                .couponName("쿠폰 미적용")
                .discountAmount(Money.ZERO)
                .build();
    }
}

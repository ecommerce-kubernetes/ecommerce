package com.example.order_service.ordersheet.domain.model.vo;

import com.example.order_service.common.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

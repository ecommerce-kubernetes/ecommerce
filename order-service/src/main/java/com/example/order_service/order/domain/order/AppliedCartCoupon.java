package com.example.order_service.order.domain.order;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppliedCartCoupon {

    private Long cartCouponId;

    private String name;

    private AppliedCartCoupon(Long cartCouponId, String name) {
        this.cartCouponId = cartCouponId;
        this.name = name;
    }

    public static AppliedCartCoupon of(Long cartCouponId, String name) {
        Assert.notNull(cartCouponId, "장바구니 쿠폰 아이디는 필수 입니다.");
        Assert.hasText(name, "장바구니 쿠폰 이름은 필수 입니다.");

        return new AppliedCartCoupon(cartCouponId, name);
    }
}

package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.mysema.commons.lang.Assert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartCouponSnapshot {
    private Long cartCouponId;
    private String name;
    private CouponDiscountPolicy discountPolicy;
    private Money minimumPaymentAmount;

    @Builder(builderMethodName = "reconstitute")
    private CartCouponSnapshot(Long cartCouponId, String name, CouponDiscountPolicy discountPolicy, Money minimumPaymentAmount) {
        Assert.notNull(cartCouponId, "장바구니 쿠폰 아이디는 필수 입니다.");
        Assert.hasText(name, "장바구니 쿠폰 이름은 필수 입니다.");
        Assert.notNull(discountPolicy, "장바구니 쿠폰 할인 정책은 필수 입니다.");
        Assert.notNull(minimumPaymentAmount, "장바구니 최소 결제 금액은 필수 입니다.");

        this.cartCouponId = cartCouponId;
        this.name = name;
        this.discountPolicy = discountPolicy;
        this.minimumPaymentAmount = minimumPaymentAmount;
    }

    public static CartCouponSnapshot of(Long cartCouponId, String name, CouponDiscountPolicy discountPolicy, Money minimumPaymentAmount) {
        return CartCouponSnapshot.reconstitute()
                .cartCouponId(cartCouponId)
                .name(name)
                .discountPolicy(discountPolicy)
                .minimumPaymentAmount(minimumPaymentAmount)
                .build();
    }

    public static CartCouponSnapshot empty() {
        return null;
    }
}

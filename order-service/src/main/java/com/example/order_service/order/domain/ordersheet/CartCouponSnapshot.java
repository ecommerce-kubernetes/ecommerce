package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@EqualsAndHashCode(of = "cartCouponId")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartCouponSnapshot {

    private Long cartCouponId;

    private String name;

    private CouponDiscountPolicy discountPolicy;

    private Money minimumPaymentAmount;

    private CartCouponSnapshot(Long cartCouponId, String name, CouponDiscountPolicy discountPolicy, Money minimumPaymentAmount) {
        this.cartCouponId = cartCouponId;
        this.name = name;
        this.discountPolicy = discountPolicy;
        this.minimumPaymentAmount = minimumPaymentAmount;
    }

    public static CartCouponSnapshot of(Long cartCouponId, String name, CouponDiscountPolicy discountPolicy, Money minimumPaymentAmount) {
        Assert.notNull(cartCouponId, "장바구니 쿠폰 아이디는 필수 입니다.");
        Assert.hasText(name, "장바구니 쿠폰 이름은 필수 입니다.");
        Assert.notNull(discountPolicy, "장바구니 쿠폰 할인 정책은 필수 입니다.");
        Assert.notNull(minimumPaymentAmount, "장바구니 최소 결제 금액은 필수 입니다.");

        return new CartCouponSnapshot(cartCouponId, name, discountPolicy, minimumPaymentAmount);
    }

    public boolean isSatisfiedBy(Money baseAmount) {
        return !baseAmount.isLessThan(minimumPaymentAmount);
    }

    public Money calculateDiscount(Money baseAmount) {
        return discountPolicy.calculateDiscount(baseAmount);
    }
}

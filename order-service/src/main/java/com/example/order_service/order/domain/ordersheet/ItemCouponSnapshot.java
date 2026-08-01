package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemCouponSnapshot {

    private Long itemCouponId;

    private String name;

    private CouponDiscountPolicy discountPolicy;

    private Integer applyQuantityLimit;

    private ItemCouponSnapshot(Long itemCouponId, String name, CouponDiscountPolicy discountPolicy, int applyQuantityLimit) {
        this.itemCouponId = itemCouponId;
        this.name = name;
        this.discountPolicy = discountPolicy;
        this.applyQuantityLimit = applyQuantityLimit;
    }

    public static ItemCouponSnapshot of(Long itemCouponId, String name, CouponDiscountPolicy discountPolicy, Integer applyQuantityLimit) {
        Assert.notNull(itemCouponId, "상품 쿠폰 아이디는 필수 입니다.");
        Assert.hasText(name, "상품 쿠폰 이름은 필수 입니다.");
        Assert.notNull(discountPolicy, "상품 쿠폰 할인 정책은 필수 입니다.");
        Assert.notNull(applyQuantityLimit, "상품 쿠폰 적용 가능 수량은 필수 입니다.");

        return new ItemCouponSnapshot(itemCouponId, name, discountPolicy, applyQuantityLimit);
    }

    public Money calculateTotalDiscount(Money baseAmount, int quantity) {
        Money discount = discountPolicy.calculateDiscount(baseAmount);
        int applicableQuantity = Math.min(quantity, this.applyQuantityLimit);
        return discount.multiple(applicableQuantity);
    }
}

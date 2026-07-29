package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCouponSnapshot {
    private Long itemCouponId;
    private String name;
    private CouponDiscountPolicy discountPolicy;
    private int applyQuantityLimit;

    @Builder(builderMethodName = "reconstitute")
    private ItemCouponSnapshot(Long itemCouponId, String name, CouponDiscountPolicy discountPolicy, int applyQuantityLimit) {
        Assert.notNull(itemCouponId, "상품 쿠폰 아이디는 필수 입니다.");
        Assert.hasText(name, "상품 쿠폰 이름은 필수 입니다.");
        Assert.notNull(discountPolicy, "상품 쿠폰 할인 정책은 필수입니다");
        Assert.notNull(applyQuantityLimit, "상품 쿠폰 적용 가능 수량은 필수 입니다.");

        this.itemCouponId = itemCouponId;
        this.name = name;
        this.discountPolicy = discountPolicy;
        this.applyQuantityLimit = applyQuantityLimit;
    }

    public static ItemCouponSnapshot of(Long itemCouponId, String name, CouponDiscountPolicy discountPolicy, int applyQuantityLimit) {
        return ItemCouponSnapshot.reconstitute()
                .itemCouponId(itemCouponId)
                .name(name)
                .discountPolicy(discountPolicy)
                .applyQuantityLimit(applyQuantityLimit)
                .build();
    }

    public Money calculateTotalDiscount(Money baseAmount, int quantity) {
        Money discount = discountPolicy.calculateDiscount(baseAmount);
        int applicableQuantity = Math.min(quantity, this.applyQuantityLimit);
        return discount.multiple(applicableQuantity);
    }
}

package com.example.order_service.order.domain.order;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppliedItemCoupon {

    private Long itemCouponId;

    private String name;

    private AppliedItemCoupon (Long itemCouponId, String name) {
        Assert.notNull(itemCouponId, "적용 상품 쿠폰 아이디는 필수 입니다.");
        Assert.hasText(name, "적용 상품 쿠폰 이름은 필수 입니다.");

        this.itemCouponId = itemCouponId;
        this.name = name;
    }

    public static AppliedItemCoupon of(Long itemCouponId, String name) {
        return new AppliedItemCoupon(itemCouponId, name);
    }
}

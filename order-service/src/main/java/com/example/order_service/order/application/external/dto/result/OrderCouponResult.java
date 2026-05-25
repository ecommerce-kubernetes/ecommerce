package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

public class OrderCouponResult {

    @Builder
    public record Calculate(
            CartCoupon cartCoupon,
            List<ItemCoupon> itemCoupons
    ) {
        public static Calculate empty(){
            return Calculate.builder()
                    .cartCoupon(null)
                    .itemCoupons(List.of())
                    .build();
        }
    }

    @Builder
    public record CartCoupon(
            Long couponId,
            String couponName,
            Money discountAmount
    ) {}

    @Builder
    public record ItemCoupon(
            Long productVariantId,
            Long couponId,
            String couponName,
            Money discountAmount
    ) {}
}

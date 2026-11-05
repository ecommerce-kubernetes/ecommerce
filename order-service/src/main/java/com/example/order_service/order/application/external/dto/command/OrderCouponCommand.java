package com.example.order_service.order.application.external.dto.command;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

public class OrderCouponCommand {

    @Builder
    public record Calculate (
            Long userId,
            Long cartCouponId,
            List<AppliedCouponItem> items
    ) {
        public static Calculate of(Long userId, Long cartCouponId, List<AppliedCouponItem> items) {
            return Calculate.builder()
                    .userId(userId)
                    .cartCouponId(cartCouponId)
                    .items(items)
                    .build();
        }
    }

    @Builder
    public record AppliedCouponItem(
            Long productVariantId,
            Money discountedPrice,
            Integer quantity,
            Long itemCouponId
    ) {
        public static AppliedCouponItem of (Long productVariantId, Money discountedPrice, Integer quantity, Long itemCouponId) {
            return AppliedCouponItem.builder()
                    .productVariantId(productVariantId)
                    .discountedPrice(discountedPrice)
                    .quantity(quantity)
                    .itemCouponId(itemCouponId)
                    .build();
        }
    }
}

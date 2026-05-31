package com.example.order_service.order.application.service.order.dto.command;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

public class OrderCommand {

    @Builder
    public record Create(
            Long userId,
            String orderSheetId
    ) {
    }

    @Builder
    public record OrderItem(
            Long productVariantId,
            Integer quantity
    ) {
        public static OrderItem of(Long productVariantId, Integer quantity) {
            return OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }

    @Builder
    public record CouponCalculate(
            Long userId,
            Long cartCouponId,
            List<AppliedCouponItem> items
    ) {
        public static CouponCalculate of(Long userId, Long cartCouponId, List<AppliedCouponItem> items) {
            return CouponCalculate.builder()
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
        public static AppliedCouponItem of(Long productVariantId,
                                           Money discountedPrice,
                                           Integer quantity,
                                           Long itemCouponId) {
            return AppliedCouponItem.builder()
                    .productVariantId(productVariantId)
                    .discountedPrice(discountedPrice)
                    .quantity(quantity)
                    .itemCouponId(itemCouponId)
                    .build();
        }
    }

}

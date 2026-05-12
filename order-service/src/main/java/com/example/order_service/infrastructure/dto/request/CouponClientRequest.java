package com.example.order_service.infrastructure.dto.request;

import com.example.order_service.infrastructure.dto.command.CouponCommand;
import lombok.Builder;

import java.util.List;

public class CouponClientRequest {

    @Builder
    public record Calculate (
            Long userId,
            Long couponId,
            Long totalAmount
    ) {
        public static Calculate of(Long userId, Long couponId, Long totalAmount) {
            return Calculate.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .totalAmount(totalAmount)
                    .build();
        }
    }

    @Builder
    public record CouponEvaluate(
            Long userId,
            Long totalOrderAmount,
            List<EvaluationItem> items
    ) {
        public static CouponEvaluate from(CouponCommand.CouponEvaluate command) {
            return CouponEvaluate.builder()
                    .userId(command.userId())
                    .totalOrderAmount(command.totalAmount())
                    .items(EvaluationItem.from(command.items()))
                    .build();
        }
    }

    @Builder
    public record EvaluationItem(
            String itemId,
            Long productId,
            Long productVariantId,
            Long price,
            Integer quantity
    ) {

        public static EvaluationItem from(CouponCommand.Item item) {
            return EvaluationItem.builder()
                    .itemId(item.itemId())
                    .productId(item.productId())
                    .productVariantId(item.productVariantId())
                    .price(item.price())
                    .quantity(item.quantity())
                    .build();
        }

        public static List<EvaluationItem> from(List<CouponCommand.Item> items) {
            return items.stream().map(EvaluationItem::from).toList();
        }

    }
}

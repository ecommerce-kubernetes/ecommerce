package com.example.order_service.order.application.saga.dto;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

public class SagaCommand {

    @Builder
    public record StartSaga(
            String orderNo,
            PointDeduction pointDeduction,
            CouponUsage couponUsage,
            List<StockDeduction> stockDeductions
    ) {
    }

    @Builder
    public record PointDeduction(
            Long userId,
            Money usedPoints
    ) {
        public static PointDeduction of(Long userId, Money usedPoints) {
            return PointDeduction.builder()
                    .userId(userId)
                    .usedPoints(usedPoints)
                    .build();
        }
    }

    @Builder
    public record CouponUsage(
            Long userId,
            Long cartCouponId,
            List<Long> itemCouponIds
    ) {
        public static CouponUsage of(Long userId, Long cartCouponId, List<Long> itemCouponIds) {
            return CouponUsage.builder()
                    .userId(userId)
                    .cartCouponId(cartCouponId)
                    .itemCouponIds(itemCouponIds)
                    .build();
        }
    }

    @Builder
    public record StockDeduction(
            Long productVariantId,
            Integer quantity
    ) {
        public static StockDeduction of(Long productVariantId, Integer quantity) {
            return StockDeduction.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }
}

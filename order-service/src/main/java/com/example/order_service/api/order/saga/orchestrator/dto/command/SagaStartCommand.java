package com.example.order_service.api.order.saga.orchestrator.dto.command;

import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SagaStartCommand {
    private Long orderId;
    private Long userId;
    private Long couponId;
    private List<DeductProduct> deductProductList;
    private Long usedPoint;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeductProduct {
        private Long productVariantId;
        private Integer quantity;

        private static DeductProduct of(Long productVariantId, Integer quantity){
            return DeductProduct.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }

        private static DeductProduct from(OrderCreatedEvent.OrderedItem item){
            return of(item.getProductVariantId(), item.getQuantity());
        }
    }

    public static SagaStartCommand from(OrderCreatedEvent event) {
        return SagaStartCommand.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .couponId(event.getCouponId())
                .deductProductList(event.getOrderedItems().stream()
                        .map(DeductProduct::from).toList())
                .usedPoint(event.getUsedPoint())
                .build();
    }
}

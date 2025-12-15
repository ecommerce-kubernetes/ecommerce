package com.example.order_service.api.order.saga.orchestrator.dto.command;

import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class SagaStartCommand {
    private Long orderId;
    private Long userId;
    private Long couponId;
    private List<DeductProduct> deductProductList;
    private Long usedPoint;

    public static class DeductProduct {
        private Long productVariantId;
        private Integer quantity;

        @Builder
        private DeductProduct(Long productVariantId, Integer quantity){
            this.productVariantId = productVariantId;
            this.quantity = quantity;
        }

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

    @Builder
    private SagaStartCommand(Long orderId, Long userId, Long couponId, List<DeductProduct> deductProductList, Long usedPoint) {
        this.orderId = orderId;
        this.userId = userId;
        this.couponId = couponId;
        this.deductProductList = deductProductList;
        this.usedPoint = usedPoint;
    }

    private static SagaStartCommand of(Long orderId, Long userId, Long couponId,
                                       List<DeductProduct> deductProductList, Long usedPoint){
        return SagaStartCommand.builder()
                .orderId(orderId)
                .userId(userId)
                .couponId(couponId)
                .deductProductList(deductProductList)
                .usedPoint(usedPoint)
                .build();
    }

    public static SagaStartCommand from(OrderCreatedEvent event) {
        List<DeductProduct> deductProducts = event.getOrderedItems().stream().map(DeductProduct::from).toList();
        return of(event.getOrderId(), event.getUserId(), event.getCouponId(), deductProducts, event.getUsedPoint());
    }
}

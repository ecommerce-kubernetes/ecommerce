package com.example.order_service.api.order.saga.orchestrator.dto.command;

import com.example.order_service.api.order.facade.event.OrderCreatedEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SagaStartCommand {
    private String orderNo;
    private Long userId;
    private Long couponId;
    private List<DeductProduct> deductProductList;
    private Long usedPoint;

    @Getter
    @Builder
    public static class DeductProduct {
        private Long productVariantId;
        private Integer quantity;

        private static DeductProduct from(OrderCreatedEvent.OrderedItem item){
            return DeductProduct.builder()
                    .productVariantId(item.getProductVariantId())
                    .quantity(item.getQuantity())
                    .build();
        }
    }

    public static SagaStartCommand from(OrderCreatedEvent event) {
        return SagaStartCommand.builder()
                .orderNo(event.getOrderNo())
                .userId(event.getUserId())
                .couponId(event.getCouponId())
                .deductProductList(event.getOrderedItems().stream()
                        .map(DeductProduct::from).toList())
                .usedPoint(event.getUsedPoint())
                .build();
    }
}

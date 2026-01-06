package com.example.order_service.api.order.saga.domain.model.vo;

import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class Payload {
    private Long userId;
    private List<SagaItem> sagaItems;
    private Long couponId;
    private Long useToPoint;

    @Getter
    @Builder
    public static class SagaItem {
        private Long productVariantId;
        private Integer quantity;

        public static SagaItem from(SagaStartCommand.DeductProduct item){
            return SagaItem.builder()
                    .productVariantId(item.getProductVariantId())
                    .quantity(item.getQuantity())
                    .build();
        }
    }

    @Builder
    private Payload(Long userId, List<SagaItem> sagaItems, Long couponId, Long useToPoint){
        if(sagaItems == null || sagaItems.isEmpty()) {
            throw new IllegalArgumentException("Saga Item은 하나 이상이여야 합니다");
        }
        this.userId = userId;
        this.sagaItems = sagaItems;
        this.couponId = couponId;
        this.useToPoint = useToPoint;
    }

    public static Payload from(SagaStartCommand command){
        return Payload.builder()
                .userId(command.getUserId())
                .sagaItems(convert(command.getDeductProductList()))
                .couponId(command.getCouponId())
                .useToPoint(command.getUsedPoint())
                .build();
    }

    public boolean hasCoupon() {
        return this.couponId != null;
    }

    public boolean hasPoints() {
        return this.useToPoint != null && this.useToPoint > 0;
    }

    private static List<SagaItem> convert(List<SagaStartCommand.DeductProduct> items){
        return items.stream().map(SagaItem::from).toList();
    }
}

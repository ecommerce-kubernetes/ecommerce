package com.example.order_service.api.order.application.event;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private Long couponId;
    private List<OrderedItem> orderedItems;
    private Long usedPoint;

    @Builder
    private OrderCreatedEvent(Long orderId, Long userId, Long couponId, List<OrderedItem> orderedItems, Long usedPoint) {
        this.orderId = orderId;
        this.userId = userId;
        this.couponId = couponId;
        this.orderedItems = orderedItems;
        this.usedPoint = usedPoint;
    }

    @Builder
    @Getter
    public static class OrderedItem {
        private Long productVariantId;
        private Integer quantity;

        private static OrderedItem from(OrderItemDto orderItemDto){
            return OrderedItem.builder()
                    .productVariantId(orderItemDto.getProductVariantId())
                    .quantity(orderItemDto.getQuantity())
                    .build();
        }
    }

    private static OrderCreatedEvent of(Long orderId, Long userId, Long couponId, List<OrderedItem> orderedItems,
                                        Long usedPoint) {
        return OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .couponId(couponId)
                .orderedItems(orderedItems)
                .usedPoint(usedPoint)
                .build();
    }

    public static OrderCreatedEvent from(OrderDto result) {
        List<OrderedItem> orderedItems = result.getOrderItemDtoList().stream().map(OrderedItem::from).toList();
        return of(result.getOrderId(), result.getUserId(), result.getAppliedCoupon().getCouponId(), orderedItems, result.getPaymentInfo().getUsedPoint());
    }
}

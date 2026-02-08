package com.example.order_service.api.order.facade.event;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreatedEvent {
    private String orderNo;
    private Long userId;
    private Long couponId;
    private List<OrderedItem> orderedItems;
    private Long usedPoint;

    @Builder
    private OrderCreatedEvent(String orderNo, Long userId, Long couponId, List<OrderedItem> orderedItems, Long usedPoint) {
        this.orderNo = orderNo;
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

        private static OrderedItem from(OrderItemDto orderItemDto) {
            return OrderedItem.builder()
                    .productVariantId(orderItemDto.getOrderedProduct().getProductVariantId())
                    .quantity(orderItemDto.getQuantity())
                    .build();
        }
    }

    public static OrderCreatedEvent from(OrderDto orderDto) {
        List<OrderedItem> orderedItems = orderDto.getOrderItems().stream().map(OrderedItem::from).toList();
        return OrderCreatedEvent.builder()
                .orderNo(orderDto.getOrderNo())
                .userId(orderDto.getOrderer().getUserId())
                .couponId(resolveCouponId(orderDto))
                .orderedItems(orderedItems)
                .usedPoint(orderDto.getOrderPriceInfo().getPointDiscount())
                .build();
    }

    private static Long resolveCouponId(OrderDto orderDto) {
        if (orderDto.getCouponInfo() == null) {
            return null;
        }
        return orderDto.getCouponInfo().getCouponId();
    }
}

package com.example.order_service.order.application.event;

import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import com.example.order_service.order.application.service.order.dto.result.OrderItemDto;
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
            return null;
        }
    }

    public static OrderCreatedEvent from(OrderDto orderDto) {
        return null;
    }

    private static Long resolveCouponId(OrderDto orderDto) {
        return null;
    }
}

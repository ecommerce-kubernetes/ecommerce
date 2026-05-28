package com.example.order_service.order.application.event;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import com.example.order_service.order.application.service.order.dto.result.OrderItemDto;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreatedEvent {
    private String orderNo;
    private Long userId;
    private Long cartCouponId;
    private List<OrderedItem> orderedItems;
    private Money usedPoint;

    @Builder
    private OrderCreatedEvent(String orderNo, Long userId, Long cartCouponId, List<OrderedItem> orderedItems, Money usedPoint) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.cartCouponId = cartCouponId;
        this.orderedItems = orderedItems;
        this.usedPoint = usedPoint;
    }

    @Builder
    @Getter
    public static class OrderedItem {
        private Long productVariantId;
        private Long itemCouponId;
        private Integer quantity;

        public static OrderedItem from(OrderItem orderItem) {
            return OrderedItem.builder()
                    .productVariantId(orderItem.getProduct().getProductVariantId())
                    .itemCouponId(orderItem.getItemCoupon().getCouponId())
                    .quantity(orderItem.getQuantity())
                    .build();
        }

        public static List<OrderedItem> from(List<OrderItem> orderItems) {
            return orderItems.stream().map(OrderedItem::from).toList();
        }
    }

    public static OrderCreatedEvent from(Order order) {
        return OrderCreatedEvent.builder()
                .orderNo(order.getOrderNo())
                .userId(order.getOrderer().getUserId())
                .cartCouponId(order.getCartCoupon().getCouponId())
                .orderedItems(OrderedItem.from(order.getOrderItems()))
                .usedPoint(order.getUsedPoints())
                .build();
    }
}

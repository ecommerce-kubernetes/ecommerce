package com.example.order_service.order.domain.order.event;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderItem;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderAcceptedEvent(
        Long orderId,
        Long userId,
        List<OrderedItem> items,
        Long cartCouponId,
        List<Long> itemCouponIds,
        Money usedPoints
) {

    public static OrderAcceptedEvent from(Order order) {
        Long cartCouponId = order.getAppliedCartCoupon() != null
                ? order.getAppliedCartCoupon().getCartCouponId()
                : null;

        List<Long> itemCouponIds = order.getOrderItems().stream()
                .filter(item -> item.getAppliedItemCoupon() != null)
                .map(item -> item.getAppliedItemCoupon().getItemCouponId())
                .toList();

        return OrderAcceptedEvent.builder()
                .orderId(order.getId())
                .userId(order.getOrderer().getUserId())
                .items(OrderedItem.from(order.getOrderItems()))
                .cartCouponId(cartCouponId)
                .itemCouponIds(itemCouponIds)
                .usedPoints(order.getOrderAmount().getUsedPoints())
                .build();
    }

    @Builder
    public record OrderedItem(
            Long productVariantId,
            Integer quantity
    ) {
        public static OrderedItem from(OrderItem orderItem) {
            return OrderedItem.builder()
                    .productVariantId(orderItem.getProduct().getProductVariantId())
                    .quantity(orderItem.getQuantity())
                    .build();
        }

        public static List<OrderedItem> from(List<OrderItem> orderItems) {
            return orderItems.stream().map(OrderedItem::from).toList();
        }
    }
}

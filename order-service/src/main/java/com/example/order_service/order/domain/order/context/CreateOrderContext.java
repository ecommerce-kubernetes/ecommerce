package com.example.order_service.order.domain.order.context;

import com.example.order_service.order.domain.order.AppliedCartCoupon;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateOrderContext(
        Orderer orderer,
        ShippingAddress shippingAddress,
        List<CreateOrderItemContext> items,
        AppliedCartCoupon appliedCartCoupon,
        OrderAmount orderAmount
) {
}

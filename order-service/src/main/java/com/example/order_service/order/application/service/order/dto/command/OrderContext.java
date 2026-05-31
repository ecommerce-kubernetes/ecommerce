package com.example.order_service.order.application.service.order.dto.command;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.util.List;

public class OrderContext {

    @Builder
    public record CreateOrderContext(
            Orderer orderer,
            ShippingAddress shippingAddress,
            List<ItemContext> orderItems,
            OrderCouponSnapshot cartCoupon,
            Money totalOriginalPrice,
            Money totalProductDiscountAmount,
            Money totalCouponDiscountAmount,
            Money usedPoints,
            Money totalPaymentAmount
    ) {}

    @Builder
    public record ItemContext(
            ProductSnapshot productSnapshot,
            ProductPriceSnapshot itemPrice,
            OrderCouponSnapshot itemCoupon,
            Integer quantity,
            List<ProductOptionSnapshot> options
    ) {}
}

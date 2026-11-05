package com.example.order_service.order.application.service.order.dto.command;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.util.List;

public class OrderContext {

    @Builder
    public record CreateOrderContext(
            Orderer orderer,
            ShippingAddress shippingAddress,
            List<ItemContext> orderItems,
            CartCouponSnapshot cartCoupon,
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
            ItemCouponSnapshot itemCouponSnapshot,
            Integer quantity,
            List<ProductOptionSnapshot> optionSnapshots
    ) {}
}

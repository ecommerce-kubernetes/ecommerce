package com.example.order_service.order.domain.order.context;

import com.example.order_service.order.domain.order.AppliedItemCoupon;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateOrderItemContext(
        ProductSnapshot productSnapshot,
        ProductPriceSnapshot priceSnapshot,
        AppliedItemCoupon appliedItemCoupon,
        int quantity,
        List<ProductOptionSnapshot> options,
        OrderItemAmount orderItemAmount
) {
}

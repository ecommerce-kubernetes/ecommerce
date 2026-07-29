package com.example.order_service.order.application.service.order.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResult(
        Long orderId,
        OrderStatus status,
        String orderName,
        Orderer orderer,
        ShippingAddress shippingAddress,
        List<OrderItemResult> orderItems,
        PaymentSummary paymentSummary,

        LocalDateTime createdAt

) {

    @Builder
    public record OrderItemResult(
            Long orderItemId,
            ProductSnapshot product,
            List<ProductOptionSnapshot> options,
            int quantity,
            ItemPayment itemPayment
    ) {
    }

    @Builder
    public record ItemPayment(
            Money lineTotal,
            Money couponDiscount,
            Money finalItemAmount
    ) {}

    @Builder
    public record PaymentSummary(
            Money totalOriginalAmount,
            Money totalItemDiscount,
            Money totalItemCouponDiscount,
            Money cartCouponDiscount,
            Money usedPoints,
            Money totalPaymentAmount
    ) {}
}

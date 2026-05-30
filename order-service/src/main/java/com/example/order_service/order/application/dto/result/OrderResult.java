package com.example.order_service.order.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResult {

    @Builder
    public record Create(
            String orderNo,
            OrderStatus status,
            String orderName,
            Money totalPaymentAmount,
            LocalDateTime createdAt
    ) {
    }

    @Builder
    public record Detail(
            String orderNo,
            String status,
            String orderName,
            Orderer orderer,
            ShippingAddress shippingAddress,
            OrderCouponSnapshot cartCoupon,
            List<OrderedItem> items,
            Money totalOriginalPrice,
            Money totalProductDiscountAmount,
            Money totalCouponDiscountAmount,
            Money usedPoints,
            Money totalPaymentAmount
    ) {
        public static Detail from(OrderDto.Detail dto) {
            return null;
        }
    }

    @Builder
    public record Summary() {
        public static Summary from(OrderDto.Summary dto) {
            return null;
        }
    }

    @Builder
    public record OrderedItem(
            ProductSnapshot product,
            ProductPriceSnapshot productPrice,
            OrderCouponSnapshot itemCoupon,
            Integer quantity,
            List<ProductOptionSnapshot> options
    ) {
    }
}

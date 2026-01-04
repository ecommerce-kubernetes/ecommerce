package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;

import java.util.List;

public class OrderExternalAdaptorFixture {
    public static final Long USER_ID = 1L;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";
    public static final Long COUPON_ID = 1L;
    public static final Long VARIANT_ID_1 = 10L;
    public static final Long VARIANT_ID_2 = 20L;
    public static final String PAYMENT_KEY = "paymentKey";

    public static OrderUserResponse createUserResponse() {
        return OrderUserResponse.builder()
                .userId(USER_ID)
                .pointBalance(10000L)
                .build();
    }

    public static OrderCouponDiscountResponse createCouponResponse() {
        return OrderCouponDiscountResponse.builder()
                .couponId(COUPON_ID)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
    }

    public static CreateOrderItemDto createOrderItemDto(Long variantId, int quantity) {
        return CreateOrderItemDto.builder()
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    public static OrderProductResponse createProductResponse(Long variantId, int stockQuantity) {
        return OrderProductResponse.builder()
                .productId(100L + variantId)
                .productVariantId(variantId)
                .productName("상품" + variantId)
                .unitPrice(OrderProductResponse.UnitPrice.builder()
                        .originalPrice(10000L)
                        .discountedPrice(10)
                        .discountAmount(1000L)
                        .discountedPrice(9000L)
                        .build())
                .stockQuantity(stockQuantity)
                .thumbnailUrl("http://thumbnail.jpg")
                .itemOptions(List.of())
                .build();
    }

    public static TossPaymentConfirmResponse createPaymentResponse() {
        return TossPaymentConfirmResponse.builder()
                .paymentKey(PAYMENT_KEY)
                .orderId(ORDER_NO)
                .totalAmount(10000L)
                .status("DONE")
                .build();
    }
}

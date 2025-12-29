package com.example.order_service.api.support.fixture;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;

import java.time.LocalDateTime;
import java.util.List;

public class OrderTestFixture {
    public static final Long USER_ID = 1L;
    public static final String ADDRESS = "서울시 테헤란로 123";

    public static CreateOrderDto createOrderRequest(Long userId, CreateOrderItemDto... items) {
        UserPrincipal userPrincipal = UserPrincipal.of(userId, UserRole.ROLE_USER);
        return CreateOrderDto.builder()
                .userPrincipal(userPrincipal)
                .deliveryAddress(ADDRESS)
                .couponId(1L)
                .pointToUse(1000L)
                .expectedPrice(28600L)
                .orderItemDtoList(List.of(items))
                .build();
    }

    public static CreateOrderItemDto createRequestItem(Long variantId, int quantity) {
        return CreateOrderItemDto.builder()
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    public static OrderProductResponse mockProductResponse(Long id, Long price) {
        return OrderProductResponse.builder()
                .productId(id)
                .productVariantId(id)
                .productName("상품" + id)
                .thumbnailUrl("http://thumbnail.jpg")
                .stockQuantity(100)
                .unitPrice(
                        OrderProductResponse.UnitPrice.builder()
                                .originalPrice(price)
                                .discountRate(10)
                                .discountAmount(price / 10)
                                .discountedPrice(price * 9 / 10)
                                .build()
                )
                .itemOptions(List.of())
                .build();
    }

    public static OrderDto mockSavedOrder(OrderStatus status, Long finalAmount) {
        return OrderDto.builder()
                .orderId(1L)
                .userId(USER_ID)
                .orderName("상품1 외 1건")
                .status(status)
                .paymentInfo(PaymentInfo.builder().usedPoint(1000L).finalPaymentAmount(finalAmount).build())
                .orderItemDtoList(List.of(createMockOrderItemDto(1L, 3),
                        createMockOrderItemDto(2L, 5))
                )
                .appliedCoupon(AppliedCoupon.builder()
                        .couponId(1L)
                        .couponName("1000원 할인 쿠폰")
                        .discountAmount(1000L)
                        .build())
                .orderedAt(LocalDateTime.now())
                .build();
    }

    public static OrderUserResponse mockUserResponse() {
        return OrderUserResponse.builder().userId(USER_ID).pointBalance(3000L).build();
    }

    public static OrderCouponCalcResponse mockCouponResponse() {
        return OrderCouponCalcResponse.builder().couponId(1L).discountAmount(1000L).build();
    }

    private static OrderItemDto createMockOrderItemDto(Long variantId, int quantity) {
        return OrderItemDto.builder()
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }
}

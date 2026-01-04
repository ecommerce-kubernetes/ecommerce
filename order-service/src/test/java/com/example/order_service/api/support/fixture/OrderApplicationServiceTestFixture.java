package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.OrderPriceInfo;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderApplicationServiceTestFixture {
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";
    public static final Long USER_ID = 1L;
    public static final String ADDRESS = "서울시 테헤란로 123";

    public static final Long PROD_1_ID = 1L;
    public static final Long PROD_1_PRICE = 3000L;

    public static final Long PROD_2_ID = 2L;
    public static final Long PROD_2_PRICE = 5000L;

    public static final int DISCOUNT_RATE = 10;
    public static final Long COUPON_DISCOUNT = 1000L;
    public static final Long USE_POINT = 1000L;

    public static final Long FIXED_FINAL_PRICE = 28600L;

    public static CreateOrderDto createOrderRequest(Long userId, CreateOrderItemDto... items) {
        if (items.length == 0) {
            items = new CreateOrderItemDto[]{
                    createRequestItem(PROD_1_ID, 3),
                    createRequestItem(PROD_2_ID, 5)
            };
        }

        return CreateOrderDto.builder()
                .userId(USER_ID)
                .deliveryAddress(ADDRESS)
                .couponId(1L)
                .pointToUse(USE_POINT)
                .expectedPrice(FIXED_FINAL_PRICE)
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
                .unitPrice(calculateUnitPrice(price))
                .itemOptions(List.of())
                .build();
    }

    public static OrderDto mockSavedOrder(OrderStatus status, Long finalAmount) {

        long totalOrigin = (PROD_1_PRICE * 3) + (PROD_2_PRICE * 5);
        long totalProdDisc = (totalOrigin * DISCOUNT_RATE) / 100;
        return OrderDto.builder()
                .orderId(1L)
                .orderNo(ORDER_NO)
                .userId(USER_ID)
                .orderName("상품1 외 1건")
                .deliveryAddress(ADDRESS)
                .status(status)
                .orderPriceInfo(
                        OrderPriceInfo.builder()
                                .totalOriginPrice(totalOrigin)
                                .totalProductDiscount(totalProdDisc)
                                .couponDiscount(COUPON_DISCOUNT)
                                .pointDiscount(USE_POINT)
                                .finalPaymentAmount(finalAmount)
                                .build()
                )
                .orderItemDtoList(List.of(
                                createMockOrderItemDto(PROD_1_ID, 3, PROD_1_PRICE),
                                createMockOrderItemDto(PROD_2_ID, 5, PROD_2_PRICE)
                        )
                )
                .appliedCoupon(AppliedCoupon.builder()
                        .couponId(1L)
                        .couponName("1000원 할인 쿠폰")
                        .discountAmount(1000L)
                        .build())
                .paymentInfo(
                        PaymentInfo.builder()
                                .id(1L)
                                .paymentKey("paymentKey")
                                .amount(FIXED_FINAL_PRICE)
                                .method("CARD")
                                .approvedAt(LocalDateTime.now())
                                .build()
                )
                .orderedAt(LocalDateTime.now())
                .build();
    }

    public static OrderDto mockCanceledOrder(OrderFailureCode failureCode) {
        OrderDto order = mockSavedOrder(OrderStatus.CANCELED, FIXED_FINAL_PRICE);
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .orderNo(ORDER_NO)
                .userId(order.getUserId())
                .orderName(order.getOrderName())
                .status(OrderStatus.CANCELED)
                .orderPriceInfo(order.getOrderPriceInfo())
                .orderItemDtoList(order.getOrderItemDtoList())
                .appliedCoupon(order.getAppliedCoupon())
                .orderedAt(order.getOrderedAt())
                .orderFailureCode(failureCode)
                .build();
    }

    public static OrderUserResponse mockUserResponse() {
        return OrderUserResponse.builder().userId(USER_ID).pointBalance(3000L).build();
    }

    public static OrderCouponDiscountResponse mockCouponResponse() {
        return OrderCouponDiscountResponse.builder().couponId(1L).discountAmount(COUPON_DISCOUNT).build();
    }

    public static TossPaymentConfirmResponse mockPaymentResponse(String paymentKey, Long amount) {
        LocalDateTime now = LocalDateTime.now();
        OffsetDateTime offsetDateTime = now.atZone(ZoneId.of("Asia/Seoul"))
                .toOffsetDateTime();
        String approvedAt = offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return TossPaymentConfirmResponse.builder()
                .paymentKey(paymentKey)
                .orderId(1L)
                .totalAmount(amount)
                .status("DONE")
                .approvedAt(approvedAt)
                .build();
    }

    private static OrderItemDto createMockOrderItemDto(Long id, int quantity, Long originalPrice) {
        OrderItemDto.UnitPrice unitPriceObj = calculateItemDtoUnitPrice(originalPrice);

        return OrderItemDto.builder()
                .productId(id)
                .productVariantId(id)
                .quantity(quantity)
                .productName("상품" + id)
                .thumbnailUrl("http://thumbnail.jpg")
                .itemOptionDtos(List.of())
                .unitPrice(unitPriceObj)
                .lineTotal(unitPriceObj.getDiscountedPrice() * quantity)
                .build();
    }
    private static OrderProductResponse.UnitPrice calculateUnitPrice(Long price) {
        long discountAmt = price * DISCOUNT_RATE / 100;
        return OrderProductResponse.UnitPrice.builder()
                .originalPrice(price)
                .discountRate(DISCOUNT_RATE)
                .discountAmount(discountAmt)
                .discountedPrice(price - discountAmt)
                .build();
    }

    private static OrderItemDto.UnitPrice calculateItemDtoUnitPrice(Long price) {
        long discountAmt = price * DISCOUNT_RATE / 100;
        return OrderItemDto.UnitPrice.builder()
                .originalPrice(price)
                .discountRate(DISCOUNT_RATE)
                .discountAmount(discountAmt)
                .discountedPrice(price - discountAmt)
                .build();
    }
}

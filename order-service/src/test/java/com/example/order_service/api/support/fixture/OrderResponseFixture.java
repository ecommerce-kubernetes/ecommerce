package com.example.order_service.api.support.fixture;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse.CouponResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse.OrderPriceResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse.OrdererResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse.PaymentResponse;
import com.example.order_service.api.order.facade.dto.result.OrderItemResponse;
import com.example.order_service.api.order.facade.dto.result.OrderItemResponse.OrderItemOptionResponse;
import com.example.order_service.api.order.facade.dto.result.OrderItemResponse.OrderItemPriceResponse;
import com.example.order_service.api.order.facade.dto.result.OrderListResponse;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseFixture {
    public static final String ORDER_NO = "ORD-20260101-adsvc";
    public static CreateOrderResponse.CreateOrderResponseBuilder anCreateOrderResponse() {
        return CreateOrderResponse.builder()
                .orderNo(ORDER_NO)
                .status(OrderStatus.PENDING.name())
                .orderName("상품")
                .finalPaymentAmount(7000L)
                .createdAt(LocalDateTime.now().toString());
    }

    public static OrderListResponse.OrderListResponseBuilder anOrderListResponse() {
        return OrderListResponse.builder()
                .orderNo(ORDER_NO)
                .orderStatus("COMPLETED")
                .orderItems(List.of(anOrderItemResponse().build()))
                .createdAt(LocalDateTime.now().toString());
    }

    public static PageDto.PageDtoBuilder<OrderListResponse> anOrderListPageResponse() {
        return PageDto.<OrderListResponse>builder()
                .content(List.of(anOrderListResponse().build()))
                .currentPage(1)
                .totalPage(1)
                .pageSize(10)
                .totalElement(1);
    }

    public static OrderDetailResponse.OrderDetailResponseBuilder anOrderDetailResponse() {
        return OrderDetailResponse.builder()
                .orderNo(ORDER_NO)
                .status(OrderStatus.COMPLETED.name())
                .orderName("상품")
                .orderer(anOrdererResponse().build())
                .orderPrice(anOrderPriceResponse().build())
                .coupon(anCouponResponse().build())
                .deliveryAddress("서울시 테헤란로 123")
                .payment(anPaymentResponse().build())
                .orderItems(List.of(anOrderItemResponse().build()))
                .createdAt(LocalDateTime.now().toString());
    }

    public static OrdererResponse.OrdererResponseBuilder anOrdererResponse() {
        return OrdererResponse.builder()
                .userId(1L)
                .userName("유저")
                .phoneNumber("010-1234-5678");
    }

    public static OrderPriceResponse.OrderPriceResponseBuilder anOrderPriceResponse() {
        return OrderPriceResponse.builder()
                .totalOriginPrice(10000L)
                .totalProductDiscount(1000L)
                .couponDiscount(1000L)
                .pointDiscount(1000L)
                .finalPaymentAmount(7000L);
    }

    public static PaymentResponse.PaymentResponseBuilder anPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1L)
                .paymentKey("paymentKey")
                .amount(7000L)
                .status("DONE")
                .method("CARD")
                .approvedAt(LocalDateTime.now().toString());
    }

    public static CouponResponse.CouponResponseBuilder anCouponResponse() {
        return CouponResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .couponDiscount(1000L);
    }

    public static OrderItemResponse.OrderItemResponseBuilder anOrderItemResponse() {
        return OrderItemResponse.builder()
                .productId(1L)
                .productVariantId(1L)
                .productName("상품")
                .thumbnailUrl("http://thumbnail.jpg")
                .quantity(1)
                .unitPrice(anOrderItemPriceResponse().build())
                .lineTotal(9000L)
                .options(List.of(anOrderItemOptionResponse().build()));
    }

    public static OrderItemPriceResponse.OrderItemPriceResponseBuilder anOrderItemPriceResponse() {
        return OrderItemPriceResponse.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L);
    }

    public static OrderItemOptionResponse.OrderItemOptionResponseBuilder anOrderItemOptionResponse() {
        return OrderItemOptionResponse.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }
}

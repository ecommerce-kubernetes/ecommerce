package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.PaymentMethod;
import com.example.order_service.api.order.domain.model.PaymentStatus;
import com.example.order_service.api.order.domain.model.PaymentType;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.CouponSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrderPriceSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrdererSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext.CreateItemOptionSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext.PriceSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext.ProductSpec;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto.CouponInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto.OrderPriceInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto.OrdererInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto.PaymentInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto.OrderItemOptionDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto.OrderItemPriceInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto.OrderedProductInfo;

import java.time.LocalDateTime;
import java.util.List;

public class OrderFixture {
    public static final String ORDER_NO = "ORD-20260101-adsvc";
    public static OrderCreationContext.OrderCreationContextBuilder anOrderCreationContext() {
        return OrderCreationContext.builder()
                .orderer(anOrdererSpec().build())
                .orderPrice(anOrderPriceSpec().build())
                .coupon(anCouponSpec().build())
                .orderItemCreationContexts(List.of(anOrderItemCreationContext().build()))
                .deliveryAddress("서울시 테헤란로 123");
    }

    public static OrdererSpec.OrdererSpecBuilder anOrdererSpec() {
        return OrdererSpec.builder()
                .userId(1L).userName("유저").phoneNumber("010-1234-5678");
    }

    public static OrderPriceSpec.OrderPriceSpecBuilder anOrderPriceSpec() {
        return OrderPriceSpec.builder()
                .totalOriginPrice(10000)
                .totalProductDiscount(1000L)
                .couponDiscount(1000L)
                .pointDiscount(1000L)
                .finalPaymentAmount(7000L);
    }

    public static CouponSpec.CouponSpecBuilder anCouponSpec() {
        return CouponSpec.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L);
    }

    public static OrderItemCreationContext.OrderItemCreationContextBuilder anOrderItemCreationContext() {
        return OrderItemCreationContext.builder()
                .productSpec(anProductSpec().build())
                .priceSpec(anPriceSpec().build())
                .quantity(1)
                .lineTotal(9000L)
                .itemOptionSpecs(List.of(anProductOptionSpec().build()));
    }

    public static ProductSpec.ProductSpecBuilder anProductSpec() {
        return ProductSpec.builder()
                .productId(1L)
                .productVariantId(1L)
                .sku("TEST")
                .productName("상품")
                .thumbnail("http://thumbnail.jpg");
    }

    public static PriceSpec.PriceSpecBuilder anPriceSpec() {
        return PriceSpec.builder()
                .originPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L);
    }

    public static CreateItemOptionSpec.CreateItemOptionSpecBuilder anProductOptionSpec() {
        return CreateItemOptionSpec.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }

    public static PaymentCreationContext.PaymentCreationContextBuilder anPaymentContext() {
        return PaymentCreationContext.builder()
                .orderNo(ORDER_NO)
                .paymentKey("paymentKey")
                .amount(7000L)
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .approvedAt(LocalDateTime.now());
    }

    public static OrderDto.OrderDtoBuilder returnOrderDto() {
        return OrderDto.builder()
                .orderer(returnOrderer().build())
                .orderNo(ORDER_NO)
                .status(OrderStatus.PENDING)
                .orderName("상품")
                .orderer(returnOrderer().build())
                .orderPriceInfo(returnOrderPrice().build())
                .couponInfo(returnCoupon().build())
                .orderItems(List.of(returnOrderItem().build()))
                .deliveryAddress("서울시 테헤란로 123")
                .orderedAt(LocalDateTime.now());
    }

    public static OrdererInfo.OrdererInfoBuilder returnOrderer() {
        return OrdererInfo.builder().userId(1L).userName("유저").phoneNumber("010-1234-5678");
    }

    public static OrderPriceInfo.OrderPriceInfoBuilder returnOrderPrice() {
        return OrderPriceInfo.builder()
                .totalOriginPrice(10000)
                .totalProductDiscount(1000L)
                .couponDiscount(1000L)
                .pointDiscount(1000L)
                .finalPaymentAmount(7000L);
    }

    public static CouponInfo.CouponInfoBuilder returnCoupon() {
        return CouponInfo.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L);
    }

    public static PaymentInfo.PaymentInfoBuilder returnPayment() {
        return PaymentInfo.builder()
                .paymentId(1L)
                .paymentKey("paymentKey")
                .amount(7000L)
                .type(PaymentType.PAYMENT)
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .approvedAt(LocalDateTime.now());
    }

    public static OrderItemDto.OrderItemDtoBuilder returnOrderItem() {
        return OrderItemDto.builder()
                .orderedProduct(returnOrderItemProduct().build())
                .orderItemPrice(returnOrderItemPrice().build())
                .quantity(1)
                .lineTotal(9000L)
                .itemOptions(List.of(returnOrderItemOption().build()));
    }

    public static OrderedProductInfo.OrderedProductInfoBuilder returnOrderItemProduct() {
        return OrderedProductInfo.builder()
                .productId(1L)
                .productVariantId(1L)
                .sku("TEST")
                .productName("상품")
                .thumbnail("http://thumbnail.jpg");
    }

    public static OrderItemPriceInfo.OrderItemPriceInfoBuilder returnOrderItemPrice() {
        return OrderItemPriceInfo.builder()
                .originPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L);
    }

    public static OrderItemOptionDto.OrderItemOptionDtoBuilder returnOrderItemOption() {
        return OrderItemOptionDto.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }
}

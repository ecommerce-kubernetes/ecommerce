package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.CouponInfo;
import com.example.order_service.api.order.domain.model.vo.OrderPriceDetail;
import com.example.order_service.api.order.domain.model.vo.Orderer;
import com.example.order_service.api.order.domain.service.dto.result.*;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderFacadeFixture {
    public CreateOrderItemCommand mockOrderItemCommand(Long variantId, int quantity) {
        return CreateOrderItemCommand.builder()
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    public CreateOrderCommand mockOrderCommand(List<CreateOrderItemCommand> items) {
        return CreateOrderCommand.builder()
                .userId(1L)
                .orderItemCommands(items)
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(1000L)
                .expectedPrice(105000L)
                .build();
    }

    public OrderUserInfo mockUserInfo() {
        return OrderUserInfo.builder()
                .userId(1L)
                .userName("유저 이름")
                .phoneNumber("010-1234-5678")
                .build();
    }

    public OrderProductInfo mockProductInfo() {
        return OrderProductInfo.builder()
                .productId(1L)
                .productVariantId(1L)
                .sku("TEST")
                .productName("상품")
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .thumbnail("http://thumbnail.jpg")
                .productOption(List.of(OrderProductInfo.ProductOption.builder()
                        .optionTypeName("사이즈").optionValueName("XL").build()))
                .build();
    }

    public OrderDto mockOrderDto() {
        Orderer orderer = Orderer.of(1L, "유저 이름", "010-1234-5678");
        OrderPriceDetail orderPriceDetail = OrderPriceDetail.of(100000L, 1000L, 1000L, 1000L, 7000L);
        CouponInfo couponInfo = CouponInfo.of(1L, "1000원 할인 쿠폰", 1000L);
        return OrderDto.builder()
                .id(1L)
                .orderNo("ORDER-20261149-sXvczFv")
                .status(OrderStatus.PENDING)
                .orderName("상품 외 1건")
                .orderer(orderer)
                .orderPriceDetail(orderPriceDetail)
                .couponInfo(couponInfo)
                .build();
    }
}

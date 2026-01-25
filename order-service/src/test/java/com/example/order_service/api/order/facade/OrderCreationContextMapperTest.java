package com.example.order_service.api.order.facade;

import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.CouponSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrderPriceSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrdererSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.*;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(MockitoExtension.class)
public class OrderCreationContextMapperTest {

    @InjectMocks
    private OrderCreationContextMapper mapper;

    private OrderUserInfo mockOrderUserInfo() {
        return OrderUserInfo.builder()
                .userId(1L)
                .userName("유저 이름")
                .phoneNumber("010-1234-5678")
                .build();
    }

    private CalculatedOrderAmounts mockCalculatedOrderAmounts(){
        return CalculatedOrderAmounts.builder()
                .totalOriginalAmount(130000L)
                .totalProductDiscount(23000L)
                .couponDiscountAmount(1000L)
                .usePointAmount(1000L)
                .finalPaymentAmount(105000L)
                .build();
    }

    private OrderCouponInfo mockOrderCouponInfo() {
        return OrderCouponInfo.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
    }

    private CreateOrderItemCommand mockOrderItemCommand(Long variantId, Integer quantity) {
        return CreateOrderItemCommand.builder()
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    private OrderProductInfo mockOrderProductInfo(Long variantId, Long originalPrice, Integer discountRate) {
        long discountAmount = (long) (originalPrice * (discountRate / 100.0));
        return OrderProductInfo.builder()
                .productId(1L)
                .productVariantId(variantId)
                .sku("TEST")
                .productName("상품")
                .thumbnail("http://thumbnail.jpg")
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .discountedPrice(originalPrice - discountAmount)
                .productOption(List.of())
                .build();
    }

    private CreateOrderCommand mockOrderCommand(List<CreateOrderItemCommand> itemCommands) {
        return CreateOrderCommand.builder()
                .userId(1L)
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(1000L)
                .orderItemCommands(itemCommands)
                .expectedPrice(8000L)
                .build();
    }

    @Test
    @DisplayName("주문 생성 Context를 매핑한다")
    void mapOrderCreationContext(){
        //given
        OrderUserInfo user = mockOrderUserInfo();
        CalculatedOrderAmounts amounts = mockCalculatedOrderAmounts();
        OrderCouponInfo coupon = mockOrderCouponInfo();
        List<CreateOrderItemCommand> orderItemCommands = List.of(mockOrderItemCommand(1L, 3), mockOrderItemCommand(2L, 5));
        CreateOrderCommand commands = mockOrderCommand(orderItemCommands);
        List<OrderProductInfo> product = List.of(mockOrderProductInfo(1L, 10000L, 10), mockOrderProductInfo(2L, 20000L, 20));
        //when
        OrderCreationContext result = mapper.mapOrderCreationContext(user, amounts, coupon, commands, product);
        //then
        // 주문자 매핑
        assertThat(result.getOrderer())
                .extracting(OrdererSpec::getUserId, OrdererSpec::getUserName, OrdererSpec::getPhoneNumber)
                .containsExactly(1L, "유저 이름", "010-1234-5678");

        // 주문 가격 매핑
        assertThat(result.getOrderPrice())
                .extracting(OrderPriceSpec::getTotalOriginPrice, OrderPriceSpec::getTotalProductDiscount, OrderPriceSpec::getCouponDiscount,
                        OrderPriceSpec::getPointDiscount, OrderPriceSpec::getFinalPaymentAmount)
                .containsExactly(130000L, 23000L, 1000L, 1000L, 105000L);

        // 쿠폰 정보 매핑
        assertThat(result.getCoupon())
                .extracting(CouponSpec::getCouponId, CouponSpec::getCouponName, CouponSpec::getDiscountAmount)
                .containsExactly(1L, "1000원 할인 쿠폰", 1000L);

        // 주문 상품 정보 매핑
        assertThat(result.getOrderItemCreationContexts())
                .extracting(
                        // 상품 기본 정보 검증
                        itemCtx -> itemCtx.getProductSpec().getProductId(),
                        itemCtx -> itemCtx.getProductSpec().getProductVariantId(),
                        itemCtx -> itemCtx.getProductSpec().getProductName(),
                        itemCtx -> itemCtx.getProductSpec().getSku(),
                        itemCtx -> itemCtx.getProductSpec().getThumbnail(),

                        // 상품 가격 정보 검증
                        itemCtx -> itemCtx.getPriceSpec().getOriginPrice(),
                        itemCtx -> itemCtx.getPriceSpec().getDiscountRate(),
                        itemCtx -> itemCtx.getPriceSpec().getDiscountAmount(),
                        itemCtx -> itemCtx.getPriceSpec().getDiscountedPrice(),
                        OrderItemCreationContext::getQuantity,
                        OrderItemCreationContext::getLineTotal)
                .containsExactly(
                        tuple(1L, 1L, "상품", "TEST", "http://thumbnail.jpg",
                                10000L, 10, 1000L, 9000L, 3, 27000L),
                        tuple(1L, 2L, "상품", "TEST", "http://thumbnail.jpg",
                                20000L, 20, 4000L, 16000L, 5, 80000L)
                );

        // 배송지 매핑
        assertThat(result.getDeliveryAddress()).isEqualTo("서울시 테헤란로 123");
    }

}

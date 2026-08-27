package com.example.order_service.saga.domain.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.saga.domain.OrderSagaPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderSagaContextTest {

    @Test
    @DisplayName("주문 아이디가 누락되면 예외가 발생한다.")
    void orderId_null(){
        //given
        OrderSagaPayload payload = createPayload();
        //when
        //then
        assertThatThrownBy(() -> CreateOrderSagaContext.builder()
                .orderId(null)
                .payload(payload)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 생성시 주문 아이디는 필수이다.");
    }

    @Test
    @DisplayName("페이로드가 누락되면 예외가 발생한다.")
    void payload_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 생성시 페이로드는 필수이다.");
    }

    private OrderSagaPayload createPayload() {
        OrderSagaPayload.OrderLine line = OrderSagaPayload.OrderLine.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(1L)
                .itemCouponIds(List.of(1L, 2L))
                .build();

        return OrderSagaPayload.builder()
                .userId(1L)
                .orderLines(List.of(line))
                .usedCoupons(usedCoupons)
                .usedPoints(Money.wons(1000L))
                .build();
    }
}
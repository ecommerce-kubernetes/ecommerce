package com.example.order_service.saga.domain;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSagaPayloadTest {

    @Test
    @DisplayName("주문 사가 페이로드에 주문 상품이 누락되면 예외가 발생한다.")
    void orderLines_null(){
        //given
        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(1L)
                .itemCouponIds(List.of(2L, 3L))
                .build();

        //when
        //then
        assertThatThrownBy(() -> OrderSagaPayload.builder()
                .userId(1L)
                .orderLines(null)
                .usedCoupons(usedCoupons)
                .usedPoints(Money.wons(1000L))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("페이로드의 주문 상품은 필수이다.");
    }

    @Test
    @DisplayName("주문 사가 페이로드에 주문 상품이 없으면 예외가 발생한다.")
    void orderLines_empty(){
        //given
        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(1L)
                .itemCouponIds(List.of(2L, 3L))
                .build();

        //when
        //then
        assertThatThrownBy(() -> OrderSagaPayload.builder()
                .userId(1L)
                .orderLines(Collections.emptyList())
                .usedCoupons(usedCoupons)
                .usedPoints(Money.wons(1000L))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("페이로드의 주문 상품은 필수이다.");
    }
}
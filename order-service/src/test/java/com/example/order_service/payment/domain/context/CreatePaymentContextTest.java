package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreatePaymentContextTest {

    @Test
    @DisplayName("주문 식별자가 누락되면 예외가 발생한다.")
    void orderId_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> CreatePaymentContext.builder()
                .orderId(null)
                .userId(1L)
                .totalAmount(Money.wons(10000L))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 생성시 주문 아이디는 필수이다.");
    }

    @Test
    @DisplayName("유저 식별자가 누락되면 예외가 발생한다.")
    void userId_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> CreatePaymentContext.builder()
                .orderId(1L)
                .userId(null)
                .totalAmount(Money.wons(10000L))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 생성시 유저 아이디는 필수이다.");
    }

    @Test
    @DisplayName("결제 가격이 누락되면 예외가 발생한다.")
    void totalAmount(){
        //given
        //when
        //then
        assertThatThrownBy(() -> CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 생성시 결제 금액은 필수이다.");
    }
}
package com.example.order_service.payment.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentApprovedEventTest {

    @Test
    @DisplayName("결제 승인 이벤트 생성시 결제 아이디가 누락되면 예외가 발생한다.")
    void paymentId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> PaymentApprovedEvent.builder()
                .paymentId(null)
                .orderId(1L)
                .userId(1L)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인 이벤트의 결제 아이디는 필수이다.");
    }

    @Test
    @DisplayName("결제 승인 이벤트 생성시 주문 아이디가 누락되면 예외가 발생한다.")
    void orderId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> PaymentApprovedEvent.builder()
                .paymentId(1L)
                .orderId(null)
                .userId(1L)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인 이벤트의 주문 아이디는 필수이다.");
    }

    @Test
    @DisplayName("결제 승인 이벤트 생성시 유저 아이디가 누락되면 예외가 발생한다.")
    void userId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> PaymentApprovedEvent.builder()
                .paymentId(1L)
                .orderId(1L)
                .userId(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인 이벤트의 유저 아이디는 필수이다.");
    }
}
package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.PaymentProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ApprovePendingContextTest {

    @Test
    @DisplayName("승인 금액이 누락되면 예외가 발생한다.")
    void amount_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> ApprovePendingContext.builder()
                .amount(null)
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인 대기시 승인 금액은 필수이다.");
    }


    @Test
    @DisplayName("결제사가 누락되면 예외가 발생한다.")
    void provider_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> ApprovePendingContext.builder()
                .amount(Money.wons(1000L))
                .provider(null)
                .paymentKey("paymentKey")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인 대기시 결제사는 필수이다.");
    }

    @Test
    @DisplayName("결제 키가 누락되면 예외가 발생한다.")
    void paymentKey_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> ApprovePendingContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인 대기시 결제 키는 필수이다.");
    }
}
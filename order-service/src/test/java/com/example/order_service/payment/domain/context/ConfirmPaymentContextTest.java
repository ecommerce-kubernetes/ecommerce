package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ConfirmPaymentContextTest {

    @Test
    @DisplayName("결제 방법이 누락되면 예외가 발생한다.")
    void method_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ConfirmPaymentContext.builder()
                .method(null)
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .transactionKey("transactionKey")
                .amount(Money.wons(10000L))
                .occurredAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인시 결제 방법은 필수이다.");
    }

    @Test
    @DisplayName("결제사가 누락되면 예외가 발생한다.")
    void provider_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ConfirmPaymentContext.builder()
                .method(PaymentMethod.CARD)
                .provider(null)
                .paymentKey("paymentKey")
                .transactionKey("transactionKey")
                .amount(Money.wons(10000L))
                .occurredAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인시 결제사는 필수이다.");
    }

    @Test
    @DisplayName("결제 키가 누락되면 예외가 발생한다.")
    void paymentKey_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ConfirmPaymentContext.builder()
                .method(PaymentMethod.CARD)
                .provider(PaymentProvider.TOSS)
                .paymentKey(null)
                .transactionKey("transactionKey")
                .amount(Money.wons(10000L))
                .occurredAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인시 결제 키는 필수이다.");
    }

    @Test
    @DisplayName("결제 트랜잭션 키가 누락되면 예외가 발생한다.")
    void transactionKey_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ConfirmPaymentContext.builder()
                .method(PaymentMethod.CARD)
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .transactionKey(null)
                .amount(Money.wons(10000L))
                .occurredAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인시 결제 트랜잭션 키는 필수이다.");
    }

    @Test
    @DisplayName("승인 금액이 누락되면 예외가 발생한다.")
    void amount_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ConfirmPaymentContext.builder()
                .method(PaymentMethod.CARD)
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .transactionKey("transactionKey")
                .amount(null)
                .occurredAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인시 승인 금액 필수이다.");
    }

    @Test
    @DisplayName("결제 승인 시간이 누락되면 예외가 발생한다.")
    void occurredAt_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ConfirmPaymentContext.builder()
                .method(PaymentMethod.CARD)
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .occurredAt(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인시 승인 시간은 필수이다.");
    }
}
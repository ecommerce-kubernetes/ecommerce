package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelPaymentContextTest {

    @Test
    @DisplayName("트랜잭션 키가 누락되면 예외가 발생한다.")
    void transactionKey_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CancelPaymentContext.builder()
                .transactionKey(null)
                .amount(Money.wons(1000L))
                .cancelReason("환불 사유")
                .occurredAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 환불시 결제 트랜잭션 키는 필수이다.");
    }

    @Test
    @DisplayName("환불 금액이 누락되면 예외가 발생한다.")
    void amount_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CancelPaymentContext.builder()
                .transactionKey("transactionKey")
                .amount(null)
                .cancelReason("환불 사유")
                .occurredAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 환불시 환불 금액은 필수이다.");
    }

    @Test
    @DisplayName("환불 사유가 누락되면 예외가 발생한다.")
    void cancelReason_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CancelPaymentContext.builder()
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .cancelReason(null)
                .occurredAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 환불시 환불 사유는 필수이다.");
    }

    @Test
    @DisplayName("환불 시간이 누락되면 예외가 발생한다.")
    void occurredAt() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CancelPaymentContext.builder()
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .cancelReason("환불 사유")
                .occurredAt(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 환불시 환불 시간은 필수이다.");
    }
}
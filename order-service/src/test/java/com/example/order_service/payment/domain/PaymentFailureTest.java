package com.example.order_service.payment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PaymentFailureTest {

    @Test
    @DisplayName("결제 실패 사유를 생성한다.")
    void of() {
        //given
        String code = "UNSUPPORTED_PROVIDER";
        String message = "지원하지 않는 결제사";
        //when
        PaymentFailure failure = PaymentFailure.of(code, message);
        //then
        assertThat(failure)
                .extracting("code", "message")
                .containsExactly(code, message);
    }

    @Test
    @DisplayName("결제 실패 코드가 누락되면 예외가 발생한다.")
    void of_code_null() {
        //given
        String message = "지원하지 않는 결제사";
        //when
        //then
        assertThatThrownBy(() -> PaymentFailure.of(null, message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 실패 코드는 필수 입니다.");
    }

    @Test
    @DisplayName("결제 실패 메시지가 누락되면 예외가 발생한다.")
    void of_message_null() {
        //given
        String code = "UNSUPPORTED_PROVIDER";
        //when
        //then
        assertThatThrownBy(() -> PaymentFailure.of(code, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 실패 메시지는 필수 입니다.");
    }
}
package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.application.port.dto.PaymentOrderStatus;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentValidatorTest {

    private final PaymentValidator validator = new PaymentValidator();

    @Test
    @DisplayName("주문이 결제 대기인지 검증한다.")
    void validateOrderPending() {
        //given
        PaymentOrderStatus status = PaymentOrderStatus.CANCELED;
        //when
        //then
        assertThatThrownBy(() -> validator.validateOrderPending(status))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.ORDER_NOT_PENDING);
    }
}
package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentContextFactoryTest {

    private final PaymentContextFactory contextFactory = new PaymentContextFactory();

    @Test
    @DisplayName("결제 생성 컨텍스트를 생성한다.")
    void create() {
        //given
        Long orderId = 1L;
        Long userId = 1L;
        Money totalAmount = Money.wons(10000L);
        //when
        CreatePaymentContext createContext = contextFactory.create(orderId, userId, totalAmount);
        //then
        assertThat(createContext)
                .extracting("orderId", "userId", "totalAmount")
                .containsExactly(orderId, userId, totalAmount);
    }
}
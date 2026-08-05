package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@IsolatedTest
@Transactional
class PaymentCommandServiceTest {

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("결제를 생성한다.")
    void create() {
        //given
        CreatePaymentContext context = createContext();
        //when
        Long id = paymentCommandService.create(context);
        //then
        assertThat(id).isNotNull();
    }

    private CreatePaymentContext createContext() {
        return CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.wons(10000L))
                .build();
    }
}
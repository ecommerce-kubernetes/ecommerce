package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@Transactional
class PaymentCommandServiceTest {

    @Autowired
    private PaymentCommandService paymentCommandService;
    @Autowired
    private IdGenerator idGenerator;

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

    @Test
    @DisplayName("결제를 승인 대기로 변경한다.")
    void approvePending() {
        //given
        CreatePaymentContext createContext = createContext();
        Payment payment = paymentRepository.save(Payment.create(createContext, idGenerator));

        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(10000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();
        //when
        paymentCommandService.approvePending(payment.getId(), 1L, approvePendingContext);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVAL_PENDING);
        assertThat(payment.getProvider()).isEqualTo(PaymentProvider.TOSS);
        assertThat(payment.getPaymentKey()).isEqualTo("paymentKey");
    }

    @Test
    @DisplayName("결제를 승인 대기로 변경할 때 결제를 찾을 수 없으면 예외가 발생한다.")
    void approvePending_notFound_payment() {
        //given
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(10000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();
        //when
        //then
        assertThatThrownBy(() -> paymentCommandService.approvePending(999L, 999L, approvePendingContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    private CreatePaymentContext createContext() {
        return CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.wons(10000L))
                .build();
    }
}
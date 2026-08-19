package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@Transactional
class PaymentQueryServiceTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Autowired
    private PaymentQueryService paymentQueryService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("결제를 조회한다.")
    void getPayment() {
        //given
        CreatePaymentContext context = createContext();
        Payment payment = Payment.create(context, idGenerator);

        paymentRepository.save(payment);
        //when
        PaymentResult result = paymentQueryService.getPayment(payment.getId(), 1L);
        //then
        assertThat(result.paymentId()).isEqualTo(payment.getId());
        assertThat(result.orderId()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("결제를 찾을 수 없으면 예외가 발생한다.")
    void getPayment_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> paymentQueryService.getPayment(100L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("완료 상태의 결제를 조회한다.")
    void findCompletedPaymentByOrderId() {
        //given
        CreatePaymentContext context = createContext();
        Payment payment = Payment.create(context, idGenerator);
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(10000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();
        payment.approvePending(approvePendingContext);
        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(10000L))
                .occurredAt(LocalDateTime.now())
                .build();

        payment.approve(approveContext, idGenerator);
        paymentRepository.save(payment);
        //when
        Optional<PaymentResult> result = paymentQueryService.findCompletedPaymentByOrderId(1L);
        //then
        assertThat(result).isPresent();
    }

    private CreatePaymentContext createContext() {
        return CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.wons(10000L))
                .build();
    }

}
package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.domain.*;
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

import static org.assertj.core.api.Assertions.*;

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

    @Test
    @DisplayName("결제를 승인한다.")
    void approve() {
        //given
        CreatePaymentContext createContext = createContext();
        Payment payment = Payment.create(createContext, idGenerator);
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(10000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(10000L))
                .occurredAt(LocalDateTime.now())
                .build();

        payment.approvePending(approvePendingContext);
        paymentRepository.save(payment);
        //when
        paymentCommandService.approve(payment.getId(), 1L, approveContext);
        //then
        Payment findPayment = paymentRepository.findByIdAndUserId(payment.getId(), 1L).orElseThrow();
        assertThat(findPayment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(findPayment.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(findPayment.getPaymentTransactions()).hasSize(1)
                .extracting("type", "amount")
                .containsExactly(
                        tuple(TransactionType.PAYMENT, Money.wons(10000L))
                );
    }

    @Test
    @DisplayName("결제를 승인할 때 결제를 찾을 수 없으면 예외가 발생한다.")
    void approve_notFound_Payment() {
        //given
        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(10000L))
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        //then
        assertThatThrownBy(() -> paymentCommandService.approve(999L, 1L, approveContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제를 실패 처리한다.")
    void abort() {
        //given
        CreatePaymentContext context = createContext();
        Payment payment = Payment.create(context, idGenerator);
        paymentRepository.save(payment);

        PaymentFailure failure = PaymentFailure.of("EXPIRED", "결제 시간이 초과되었습니다.");
        //when
        paymentCommandService.abort(payment.getId(), 1L, failure);
        //then
        Payment findPayment = paymentRepository.findByIdAndUserId(payment.getId(), 1L).orElseThrow();
        assertThat(findPayment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
        assertThat(findPayment.getFailure()).isEqualTo(failure);
    }

    @Test
    @DisplayName("결제를 실패 처리할때 결제를 찾을 수 없으면 예외가 발생한다.")
    void abort_notFound_payment() {
        //given
        PaymentFailure failure = PaymentFailure.of("EXPIRED", "결제 시간이 초과되었습니다.");
        //when
        //then
        assertThatThrownBy(() -> paymentCommandService.abort(999L, 1L, failure))
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
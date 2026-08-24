package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.domain.*;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CancelPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.domain.event.PaymentCompletedEvent;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.support.annotation.IsolatedTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@IsolatedTest
@Transactional
@RecordApplicationEvents
class PaymentCommandServiceTest {

    @Autowired
    private PaymentCommandService paymentCommandService;
    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ApplicationEvents events;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("결제를 생성한다.")
    void create() {
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.wons(10000L))
                .build();
        //when
        Long id = paymentCommandService.create(context);
        flushAndClear();
        //then
        Optional<Payment> payment = paymentRepository.findById(id);
        assertThat(payment).isPresent();
    }

    @Test
    @DisplayName("결제를 승인 대기로 변경한다.")
    void approvePending() {
        //given
        Payment payment = PaymentFixtureBuilder.given().build();
        paymentRepository.save(payment);
        flushAndClear();

        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();
        //when
        paymentCommandService.approvePending(payment.getId(), approvePendingContext);
        flushAndClear();
        //then
        Payment findPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(findPayment.getStatus()).isEqualTo(PaymentStatus.APPROVAL_PENDING);
    }

    @Test
    @DisplayName("결제를 승인 대기로 변경할 때 결제를 찾을 수 없으면 예외가 발생한다.")
    void approvePending_whenPaymentNotFound_thenThrownException() {
        //given
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(10000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();
        //when
        //then
        assertThatThrownBy(() -> paymentCommandService.approvePending(999L,  approvePendingContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제를 승인한다.")
    void approve() {
        //given
        Payment payment = PaymentFixtureBuilder.given().asApprovePending().build();
        paymentRepository.save(payment);
        flushAndClear();

        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        paymentCommandService.approve(payment.getId(), approveContext);
        flushAndClear();
        //then
        Payment findPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(findPayment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(findPayment.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(findPayment.getPaymentTransactions()).hasSize(1)
                .extracting("type", "amount")
                .containsExactly(
                        tuple(TransactionType.PAYMENT, Money.wons(1000L))
                );

        long eventCount = events.stream(PaymentCompletedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    @DisplayName("결제를 승인할 때 결제를 찾을 수 없으면 예외가 발생한다.")
    void approve_whenPaymentNotFound_thenThrownException() {
        //given
        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(10000L))
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        //then
        assertThatThrownBy(() -> paymentCommandService.approve(999L, approveContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제를 실패 처리한다.")
    void abort() {
        //given
        Payment payment = PaymentFixtureBuilder.given().build();
        paymentRepository.save(payment);
        flushAndClear();

        PaymentFailure failure = PaymentFailure.of("EXPIRED", "결제 시간이 초과되었습니다.");
        //when
        paymentCommandService.abort(payment.getId(), failure);
        flushAndClear();
        //then
        Payment findPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(findPayment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
        assertThat(findPayment.getFailure()).isEqualTo(failure);
    }

    @Test
    @DisplayName("결제를 실패 처리할때 결제를 찾을 수 없으면 예외가 발생한다.")
    void abort_whenPaymentNotFound_thenThrownException() {
        //given
        PaymentFailure failure = PaymentFailure.of("EXPIRED", "결제 시간이 초과되었습니다.");
        //when
        //then
        assertThatThrownBy(() -> paymentCommandService.abort(999L, failure))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제를 환불 대기로 변경한다.")
    void refundPending() {
        //given
        Payment payment = PaymentFixtureBuilder.given().asDone().build();
        paymentRepository.save(payment);
        flushAndClear();
        //when
        paymentCommandService.refundPending(payment.getId());
        flushAndClear();
        //then
        Payment findPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(findPayment.getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
    }

    @Test
    @DisplayName("결제를 환불 대기로 변경할때 결제를 찾을 수 없으면 예외가 발생한다.")
    void refundPending_whenPaymentNotFound_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> paymentCommandService.refundPending(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제를 취소한다.")
    void cancel(){
        //given
        Payment payment = PaymentFixtureBuilder.given()
                .withTotalAmount(Money.wons(1000L))
                .asRefundPending()
                .build();
        paymentRepository.save(payment);
        flushAndClear();

        CancelPaymentContext context = CancelPaymentContext.builder()
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .cancelReason("단순 변심")
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        paymentCommandService.cancel(payment.getId(), context);
        flushAndClear();
        //then
        Payment findPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(findPayment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    @DisplayName("결제를 취소할때 결제를 찾을 수 없으면 예외가 발생한다.")
    void cancel_whenPaymentNotFound_thenThrownException(){
        //given
        CancelPaymentContext context = CancelPaymentContext.builder()
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .cancelReason("단순 변심")
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        //then
        assertThatThrownBy(() -> paymentCommandService.cancel(999L, context))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private final IdGenerator idGenerator = new TsidGenerator();

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
        Payment payment = Payment.create(context, idGenerator);
        //then
        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(1L);
        assertThat(payment.getUserId()).isEqualTo(1L);
        assertThat(payment.getTotalAmount()).isEqualTo(Money.wons(10000L));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
    }

    @Test
    @DisplayName("결제를 생성할 때 아이디 생성기가 누락되면 예외가 발생한다.")
    void create_idGenerator_null() {
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.wons(10000L))
                .build();
        //when
        //then
        assertThatThrownBy(() -> Payment.create(context, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 생성시 아이디 생성기는 필수이다.");
    }

    @Test
    @DisplayName("결제 생성시 아이디가 누락되면 예외가 발생한다.")
    void create_id_null() {
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.wons(10000L))
                .build();

        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> Payment.create(context, nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 생성시 아이디는 필수이다.");
    }

    @Test
    @DisplayName("결제를 승인 대기로 변경한다.")
    void approvePending() {
        //given
        CreatePaymentContext context = createPaymentContext();
        Payment payment = Payment.create(context, idGenerator);

        PaymentProvider provider = PaymentProvider.TOSS;
        ApprovePendingPaymentContext approveContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(provider)
                .paymentKey("paymentKey")
                .build();
        //when
        payment.approvePending(approveContext);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVAL_PENDING);
        assertThat(payment.getProvider()).isEqualTo(provider);
        assertThat(payment.getPaymentKey()).isEqualTo("paymentKey");
    }

    @Test
    @DisplayName("결제를 승인 대기로 변경할 때 결제가 준비 상태가 아니면 예외가 발생한다.")
    void approvePending_payment_not_ready() {
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.ZERO)
                .build();
        Payment payment = Payment.create(context, idGenerator);

        PaymentProvider provider = PaymentProvider.TOSS;
        ApprovePendingPaymentContext approveContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(provider)
                .paymentKey("paymentKey")
                .build();
        //when
        //then
        assertThatThrownBy(() -> payment.approvePending(approveContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_READY);
    }

    @Test
    @DisplayName("결제를 승인 대기로 변경할때 지원하지 않는 결제사인 경우 예외가 발생한다.")
    void approvePending_unsupported_provider() {
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.ZERO)
                .build();
        Payment payment = Payment.create(context, idGenerator);
        PaymentProvider provider = PaymentProvider.KAKAO;
        ApprovePendingPaymentContext approveContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(provider)
                .paymentKey("paymentKey")
                .build();
        //when
        //then
        assertThatThrownBy(() -> payment.approvePending(approveContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_READY);
    }

    @Test
    @DisplayName("결제를 승인 대기로 변경할 때 결제 가격이 일치하지 않으면 예외가 발생한다.")
    void approvePending_totalAmount_missMatch() {
        //given
        CreatePaymentContext context = createPaymentContext();
        Payment payment = Payment.create(context, idGenerator);

        PaymentProvider provider = PaymentProvider.TOSS;
        ApprovePendingPaymentContext approveContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(2000L))
                .provider(provider)
                .paymentKey("paymentKey")
                .build();
        //when
        //then
        assertThatThrownBy(() -> payment.approvePending(approveContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }

    @Test
    @DisplayName("결제를 승인한다.")
    void approve() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .occurredAt(LocalDateTime.now())
                .build();
        payment.approvePending(approvePendingContext);
        //when
        payment.approve(approveContext, idGenerator);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(payment.getPaymentTransactions()).hasSize(1);
    }

    @Test
    @DisplayName("결제를 승인할때 승인 대기 상태가 아니면 예외가 발생한다.")
    void approve_payment_not_approvePending() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);
        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        //then
        assertThatThrownBy(() -> payment.approve(approveContext, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_NOT_APPROVE_PENDING);
    }

    @Test
    @DisplayName("결제를 승인할때 지원하지 않는 결제 방식인 경우 예외가 발생한다.")
    void approve_unsupported_method() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);

        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        payment.approvePending(approvePendingContext);

        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.VIRTUAL_ACCOUNT)
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        //then
        assertThatThrownBy(() -> payment.approve(approveContext, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.UNSUPPORTED_PAYMENT_METHOD);
    }

    @Test
    @DisplayName("결제를 승인할 때 승인 금액이 일치하지 않으면 예외가 발생한다.")
    void approve_missMatch_totalAmount() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);

        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        payment.approvePending(approvePendingContext);

        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(2000L))
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        //then
        assertThatThrownBy(() -> payment.approve(approveContext, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.APPROVAL_AMOUNT_MISMATCH);
    }

    @Test
    @DisplayName("결제를 실패 처리한다.")
    void abort() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        PaymentFailure failure = PaymentFailure.of("UNSUPPORTED_PROVIDER", "지원하지 않는 결제사 입니다.");

        payment.approvePending(approvePendingContext);
        //when
        payment.abort(failure);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
        assertThat(payment.getFailure()).isEqualTo(failure);
    }

    @Test
    @DisplayName("결제를 실패 처리할때 결제 상태가 준비 또는 승인 대기가 아니면 예외가 발생한다.")
    void abort_not_ready_or_approval_pending() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .occurredAt(LocalDateTime.now())
                .build();

        PaymentFailure failure = PaymentFailure.of("UNSUPPORTED_PROVIDER", "지원하지 않는 결제사 입니다.");

        payment.approvePending(approvePendingContext);
        payment.approve(approveContext, idGenerator);
        //when
        //then
        assertThatThrownBy(() -> payment.abort(failure))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_CANNOT_ABORT);
    }

    @Test
    @DisplayName("결제를 실패 처리할때 실패 사유가 누락되면 예외가 발생한다.")
    void abort_failure_null() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        payment.approvePending(approvePendingContext);
        //when
        //then
        assertThatThrownBy(() -> payment.abort(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 실패시 실패 사유는 필수입니다.");
    }

    @Test
    @DisplayName("결제를 환불 대기로 변경한다.")
    void refundPending() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        payment.approvePending(approvePendingContext);
        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .occurredAt(LocalDateTime.now())
                .build();
        payment.approve(approveContext, idGenerator);
        //when
        payment.refundPending();
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
    }

    @Test
    @DisplayName("결제 완료가 아니면 환불 대기로 변경할 수 없다")
    void refundPending_notDone() {
        //given
        CreatePaymentContext createContext = createPaymentContext();
        Payment payment = Payment.create(createContext, idGenerator);
        ApprovePendingPaymentContext approvePendingContext = ApprovePendingPaymentContext.builder()
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .paymentKey("paymentKey")
                .build();

        payment.approvePending(approvePendingContext);
        //when
        //then
        assertThatThrownBy(payment::refundPending)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_CANNOT_REFUND_PENDING);
    }

    private CreatePaymentContext createPaymentContext() {
        return CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.wons(1000L))
                .build();
    }
}
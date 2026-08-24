package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CancelPaymentContext;
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
    void create_whenIdGeneratorIsNull_thenThrownException() {
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
    void create_whenIdGeneratorGenerateNullId_thenThrownException() {
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
        Payment payment = PaymentFixtureBuilder.given().build();

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
    void approvePending_whenStatusNotReady_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given().build();

        payment.abort(PaymentFailure.of("결제 실패", "알 수 없는 에러로 결제가 실패됨"));

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
    void approvePending_whenUnsupportedProvider_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given().build();

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
                .isEqualTo(PaymentErrorCode.UNSUPPORTED_PAYMENT_PROVIDER);
    }

    @Test
    @DisplayName("결제를 승인 대기로 변경할 때 결제 가격이 일치하지 않으면 예외가 발생한다.")
    void approvePending_whenTotalAmountNotMatches_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given().build();

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
        Payment payment = PaymentFixtureBuilder.given().asApprovePending().build();

        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .occurredAt(LocalDateTime.now())
                .build();

        //when
        payment.approve(approveContext, idGenerator);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(payment.getPaymentTransactions()).hasSize(1);
    }

    @Test
    @DisplayName("결제를 승인할때 승인 대기 상태가 아니면 예외가 발생한다.")
    void approve_whenStatusNotApprovalPending_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given().build();
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
    void approve_whenUnsupportedMethod_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given().asApprovePending().build();

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
    void approve_whenApproveAmountMismatch_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given()
                .withTotalAmount(Money.wons(1000L))
                .asApprovePending().build();

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
        Payment payment = PaymentFixtureBuilder.given().build();;
        PaymentFailure failure = PaymentFailure.of("TIMEOUT", "만료된 결제입니다.");
        //when
        payment.abort(failure);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
        assertThat(payment.getFailure()).isEqualTo(failure);
    }

    @Test
    @DisplayName("결제를 실패 처리할때 결제 상태가 준비 또는 승인 대기가 아니면 예외가 발생한다.")
    void abort_whenStatusNotReadyOrApprovalPending_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given()
                .asDone()
                .build();

        PaymentFailure failure = PaymentFailure.of("UNSUPPORTED_PROVIDER", "지원하지 않는 결제사 입니다.");
        //when
        //then
        assertThatThrownBy(() -> payment.abort(failure))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_CANNOT_ABORT);
    }

    @Test
    @DisplayName("결제를 실패 처리할때 실패 사유가 누락되면 예외가 발생한다.")
    void abort_whenPaymentFailureIsNull_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given().build();
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
        Payment payment = PaymentFixtureBuilder.given()
                .asDone().build();
        //when
        payment.refundPending();
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
    }

    @Test
    @DisplayName("결제 완료가 아니면 환불 대기로 변경할 수 없다")
    void refundPending_whenStatusNotDone_thenThrownException() {
        //given
        Payment payment = PaymentFixtureBuilder.given().asApprovePending().build();
        //when
        //then
        assertThatThrownBy(payment::refundPending)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_CANNOT_REFUND_PENDING);
    }

    @Test
    @DisplayName("결제를 취소한다.")
    void cancel(){
        //given
        Payment payment = PaymentFixtureBuilder.given().asRefundPending().build();

        CancelPaymentContext context = CancelPaymentContext.builder()
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .cancelReason("단순 변심")
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        payment.cancel(context, idGenerator);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(payment.getPaymentTransactions()).hasSize(2);
    }

    @Test
    @DisplayName("결제가 취소 대기가 아니면 예외가 발생한다.")
    void cancel_whenStatusNotRefundPending_thenThrownException(){
        //given
        Payment payment = PaymentFixtureBuilder.given().asDone().build();

        CancelPaymentContext context = CancelPaymentContext.builder()
                .transactionKey("transactionKey")
                .amount(Money.wons(1000L))
                .cancelReason("단순 변심")
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        //then
        assertThatThrownBy(() -> payment.cancel(context, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_CANNOT_CANCEL);
    }

    @Test
    @DisplayName("환불 금액과 결제 금액이 동일하지 않으면 예외가 발생한다.")
    void cancel_whenCancelAmountMismatches_thenThrownException(){
        //given
        Payment payment = PaymentFixtureBuilder.given()
                .withTotalAmount(Money.wons(1000L))
                .asRefundPending().build();


        CancelPaymentContext context = CancelPaymentContext.builder()
                .transactionKey("transactionKey")
                .amount(Money.wons(2000L))
                .cancelReason("단순 변심")
                .occurredAt(LocalDateTime.now())
                .build();
        //when
        //then
        assertThatThrownBy(() -> payment.cancel(context, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.CANCEL_AMOUNT_MISMATCH);
    }
}
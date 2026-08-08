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
    @DisplayName("결제 금액이 0원인 경우 결제 상태는 완료이다.")
    void create_totalAmount_zero() {
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.ZERO)
                .build();
        //when
        Payment payment = Payment.create(context, idGenerator);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
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
    @DisplayName("")
    void approve() {
        //given
        //when
        //then
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

    private CreatePaymentContext createPaymentContext() {
        return CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(Money.wons(1000L))
                .build();
    }
}
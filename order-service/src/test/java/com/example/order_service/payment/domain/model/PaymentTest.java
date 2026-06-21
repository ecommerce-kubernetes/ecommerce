package com.example.order_service.payment.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PaymentTest {

    @Test
    @DisplayName("결제 엔티티를 생성하면 결제 상태는 승인 대기 이다")
    void create_status_is_ready() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        //when
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        //then
        assertThat(payment)
                .extracting("orderNo", "userId", "paymentKey", "totalAmount")
                .containsExactlyInAnyOrder(orderNo, userId, paymentKey, totalAmount);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
    }

    @Test
    @DisplayName("결제를 승인한다")
    void approve() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        //when
        payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
        //then
        assertThat(payment.getPaymentRecords()).hasSize(1);
        assertThat(payment)
                .extracting("status", "method", "lastTransactionKey")
                .containsExactly(
                        PaymentStatus.DONE, PaymentMethod.CARD, "transactionKey"
                );
    }

    @Test
    @DisplayName("결제가 대기상태가 아니면 예외가 발생한다")
    void approve_status_not_ready() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        payment.abort("abort");
        //when
        //then
        assertThatThrownBy(() -> payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_APPROVAL);
    }

    @Test
    @DisplayName("지원하지 않는 결제 방식인 경우 예외가 발생한다")
    void approve_unsupported_method() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        //when
        //then
        assertThatThrownBy(() -> payment.approve(approval, PaymentStatus.WAITING_FOR_DEPOSIT, PaymentMethod.VIRTUAL_ACCOUNT))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.UNSUPPORTED_PAYMENT_METHOD);
    }

    @Test
    @DisplayName("결제 금액과 결제된 금액이 일치하지 않으면 예외가 발생한다")
    void approve_mismatch_totalAmount() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(9000L), LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        //when
        //then
        assertThatThrownBy(() -> payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PG_APPROVAL_AMOUNT_MISMATCH);
    }

    @Test
    @DisplayName("결제를 취소 처리한다")
    void abort() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        //when
        payment.abort("INSUFFICIENT_BALANCE");
        //then
        assertThat(payment)
                .extracting("status", "failureCode")
                .containsExactly(
                        PaymentStatus.ABORTED, "INSUFFICIENT_BALANCE"
                );
    }

    @Test
    @DisplayName("결제 취소할 수 없는 상태이면 예외가 발생한다")
    void abort_status_not_ready() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
        //when
        //then
        assertThatThrownBy(() -> payment.abort("INSUFFICIENT_BALANCE"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_FAIL);
    }

    @Test
    @DisplayName("결제를 환불 대기 상태로 변경한다")
    void refundPending() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
        //when
        payment.refundPending();
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
    }

    @Test
    @DisplayName("환불 대기로 변경할 수 없는 상태면 예외가 발생한다")
    void refundPending_status_not_done() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        payment.abort("fail");
        //when
        //then
        assertThatThrownBy(payment::refundPending)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING);
    }

    @Test
    @DisplayName("결제를 취소한다")
    void cancel() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        PaymentRecord cancellation = PaymentRecord.createCancellation("lastTransactionKey", Money.wons(10000L), "테스트 결제 취소", LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
        payment.refundPending();
        //when
        payment.cancel(cancellation, PaymentStatus.CANCELED);
        //then
        assertThat(payment.getPaymentRecords()).hasSize(2);
        assertThat(payment)
                .extracting("status", "lastTransactionKey")
                .containsExactly(
                        PaymentStatus.CANCELED, "lastTransactionKey"
                );
    }

    @Test
    @DisplayName("환불 가능한 금액을 초과하면 예외가 발생한다")
    void cancel_exceeded_refundable_balance() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        PaymentRecord cancellation = PaymentRecord.createCancellation("lastTransactionKey", Money.wons(11000L), "테스트 결제 취소", LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
        payment.refundPending();
        //when
        //then
        assertThatThrownBy(() -> payment.cancel(cancellation, PaymentStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.EXCEEDED_REFUNDABLE_AMOUNT);
    }

    @Test
    @DisplayName("결제를 환불 할때 환불대기 상태가 아니라면 예외가 발생한다")
    void cancel_payment_status_not_refundPending() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        PaymentRecord cancellation = PaymentRecord.createCancellation("lastTransactionKey", Money.wons(10000L), "테스트 결제 취소", LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
        //when
        //then
        assertThatThrownBy(() -> payment.cancel(cancellation, PaymentStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND);
    }

    @Test
    @DisplayName("총 할인된 가격을 반환한다")
    void calculateTotalCanceledAmount(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        PaymentRecord cancellation = PaymentRecord.createCancellation("lastTransactionKey", Money.wons(7000L), "테스트 결제 취소", LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
        payment.refundPending();
        payment.cancel(cancellation, PaymentStatus.PARTIAL_CANCELED);
        //when
        Money money = payment.calculateTotalCanceledAmount();
        //then
        assertThat(money).isEqualTo(Money.wons(7000L));
    }

    @Test
    @DisplayName("할인 가능한 금액을 반환한다")
    void calculateRemainingAmount(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
        PaymentRecord cancellation = PaymentRecord.createCancellation("lastTransactionKey", Money.wons(7000L), "테스트 결제 취소", LocalDateTime.now());
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
        payment.refundPending();
        payment.cancel(cancellation, PaymentStatus.PARTIAL_CANCELED);
        //when
        Money money = payment.calculateRemainingAmount();
        //then
        assertThat(money).isEqualTo(Money.wons(3000L));
    }
}

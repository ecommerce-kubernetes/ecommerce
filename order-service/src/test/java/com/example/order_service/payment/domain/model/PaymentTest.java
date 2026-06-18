package com.example.order_service.payment.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;

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
    @DisplayName("결제 승인 레코드를 추가한다")
    void approval() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord record = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        //when
        payment.approval(record, PaymentStatus.DONE);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getPaymentRecords()).hasSize(1)
                .extracting("type", "amount")
                .containsExactlyInAnyOrder(
                        tuple(TransactionType.PAYMENT, Money.wons(10000L))
                );
    }

    @Test
    @DisplayName("결제 승인 레코드 추가시 결제 상태가 결제 대기 상태가 아니면 예외가 발생한다")
    void approval_payment_not_ready() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord record = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        payment.approval(record, PaymentStatus.DONE);
        //when
        //then
        assertThatThrownBy(() -> payment.approval(record, PaymentStatus.DONE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_APPROVAL);
    }

    @Test
    @DisplayName("환불 대기로 변경한다")
    void refundPending() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord record = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        payment.approval(record, PaymentStatus.DONE);
        //when
        payment.refundPending();
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
    }

    @Test
    @DisplayName("환불 대기로 변경할때 결제 상태가 완료가 아니면 예외가 발생한다")
    void refundPending_payment_status_is_not_done(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord record = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        payment.approval(record, PaymentStatus.ABORTED);
        //when
        //then
        assertThatThrownBy(payment::refundPending)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING);
    }

    @Test
    @DisplayName("결제 취소 레코드를 추가한다")
    void cancel(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord done = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        PaymentRecord cancel = PaymentRecord.createCancellation(Money.wons(10000L), PaymentMethod.CARD, "정상 승인", LocalDateTime.now());
        payment.approval(done, PaymentStatus.DONE);
        payment.refundPending();
        //when
        payment.cancel(cancel, PaymentStatus.CANCELED);
        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(payment.getPaymentRecords()).hasSize(2)
                .extracting("type", "amount")
                .containsExactlyInAnyOrder(
                        tuple(TransactionType.PAYMENT, Money.wons(10000L)),
                        tuple(TransactionType.REFUND, Money.wons(10000L))
                );
    }

    @Test
    @DisplayName("결제 취소시 결제가 환불 대기 상태가 아니면 예외가 발생한다")
    void cancel_payment_is_not_refund_pending(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord done = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        PaymentRecord cancel = PaymentRecord.createCancellation(Money.wons(10000L), PaymentMethod.CARD, "정상 승인", LocalDateTime.now());
        payment.approval(done, PaymentStatus.DONE);
        //when
        //then
        assertThatThrownBy(() -> payment.cancel(cancel, PaymentStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND);
    }

    @Test
    @DisplayName("환불 가능 금액을 초과한 경우 예외가 발생한다")
    void cancel_exceeded_refundable_amount(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord done = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        PaymentRecord cancel = PaymentRecord.createCancellation(Money.wons(11000L), PaymentMethod.CARD, "정상 승인", LocalDateTime.now());
        payment.approval(done, PaymentStatus.DONE);
        payment.refundPending();
        //when
        //then
        assertThatThrownBy(() -> payment.cancel(cancel, PaymentStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.EXCEEDED_REFUNDABLE_AMOUNT);
    }

    @Test
    @DisplayName("환불된 금액을 계산한다")
    void calculateTotalCanceledAmount(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord done = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        PaymentRecord cancel = PaymentRecord.createCancellation(Money.wons(8000L), PaymentMethod.CARD, "정상 승인", LocalDateTime.now());
        payment.approval(done, PaymentStatus.DONE);
        payment.refundPending();
        payment.cancel(cancel, PaymentStatus.CANCELED);
        //when
        Money money = payment.calculateTotalCanceledAmount();
        //then
        assertThat(money).isEqualTo(Money.wons(8000L));
    }

    @Test
    @DisplayName("남은 환불 가능 금액을 계산한다")
    void calculateRemainingAmount(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        String paymentKey = "paymentKey";
        Money totalAmount = Money.wons(10000L);
        Payment payment = Payment.create(orderNo, userId, paymentKey, totalAmount);
        PaymentRecord done = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
        PaymentRecord cancel = PaymentRecord.createCancellation(Money.wons(8000L), PaymentMethod.CARD, "정상 승인", LocalDateTime.now());
        payment.approval(done, PaymentStatus.DONE);
        payment.refundPending();
        payment.cancel(cancel, PaymentStatus.CANCELED);
        //when
        Money money = payment.calculateRemainingAmount();
        //then
        assertThat(money).isEqualTo(Money.wons(2000L));
    }
}

package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentFixtureBuilder;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.support.annotation.IsolatedTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("결제를 조회한다.")
    void getPayment() {
        //given
        Payment payment = PaymentFixtureBuilder.given().build();
        paymentRepository.save(payment);
        flushAndClear();
        //when
        PaymentResult result = paymentQueryService.getPayment(payment.getId(), payment.getUserId());
        flushAndClear();
        //then
        assertThat(result.paymentId()).isEqualTo(payment.getId());
        assertThat(result.orderId()).isEqualTo(payment.getOrderId());
        assertThat(result.userId()).isEqualTo(payment.getUserId());
    }

    @Test
    @DisplayName("결제를 찾을 수 없으면 예외가 발생한다.")
    void getPayment_whenPaymentNotFound_thenThrownException() {
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
        Payment payment = PaymentFixtureBuilder.given().asDone().build();
        paymentRepository.save(payment);
        flushAndClear();
        //when
        Optional<PaymentResult> result = paymentQueryService.findCompletedPaymentByOrderId(1L);
        //then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("타임아웃된 준비중 결제를 조회한다.")
    void getPaymentsByReadyAndCreatedAtBefore() {
        //given
        Payment payment1 = PaymentFixtureBuilder.given().build();
        Payment payment2 = PaymentFixtureBuilder.given().build();

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(40);
        em.createNativeQuery("UPDATE payment SET created_at = :pastTime WHERE id IN (:id)")
                .setParameter("pastTime", pastTime)
                .setParameter("id", payment1.getId())
                .executeUpdate();

        flushAndClear();

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);
        //when
        List<PaymentResult> result = paymentQueryService.getPaymentsByReadyAndCreatedAtBefore(timeoutThreshold);
        //then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("paymentId")
                .containsExactlyInAnyOrder(payment1.getId());
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
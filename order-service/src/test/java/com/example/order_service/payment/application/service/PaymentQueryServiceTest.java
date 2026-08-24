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

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
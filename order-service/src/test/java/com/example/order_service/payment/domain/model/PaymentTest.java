package com.example.order_service.payment.domain.model;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

}

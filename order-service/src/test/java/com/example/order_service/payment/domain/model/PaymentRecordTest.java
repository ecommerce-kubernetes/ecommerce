package com.example.order_service.payment.domain.model;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentRecordTest {

    @Test
    @DisplayName("결제 승인 레코드를 생성한다")
    void createApproval(){
        //given
        Money amount = Money.wons(10000L);
        PaymentMethod method = PaymentMethod.CARD;
        LocalDateTime approvedAt = LocalDateTime.now();
        //when
        PaymentRecord approval = PaymentRecord.createApproval(amount, method, approvedAt);
        //then
        assertThat(approval)
                .extracting("type", "amount", "method")
                .containsExactlyInAnyOrder(TransactionType.PAYMENT, Money.wons(10000L), PaymentMethod.CARD);
    }

    @Test
    @DisplayName("결제 취소 레코드를 생성한다")
    void createCancellation(){
        //given
        Money amount = Money.wons(10000L);
        PaymentMethod method = PaymentMethod.CARD;
        LocalDateTime approvedAt = LocalDateTime.now();
        //when
        PaymentRecord cancellation = PaymentRecord.createCancellation(amount, method, "정상 승인", approvedAt);
        //then
        assertThat(cancellation)
                .extracting("type", "amount", "method")
                .containsExactlyInAnyOrder(TransactionType.REFUND, Money.wons(10000L), PaymentMethod.CARD);
    }
}

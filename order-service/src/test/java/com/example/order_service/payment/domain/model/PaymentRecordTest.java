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
        String transactionKey = "transactionKey";
        Money amount = Money.wons(1000L);
        LocalDateTime occurredAt = LocalDateTime.now();
        //when
        PaymentRecord approval = PaymentRecord.createApproval(transactionKey, amount, occurredAt);
        //then
        assertThat(approval)
                .extracting("transactionKey", "type", "amount", "reason", "occurredAt")
                .containsExactly(
                        "transactionKey", TransactionType.PAYMENT, Money.wons(1000L), "정상 승인",
                        occurredAt
                );
    }

    @Test
    @DisplayName("환불 레코드를 생성한다")
    void createCancellation(){
        //given
        String transactionKey = "transactionKey";
        Money amount = Money.wons(1000L);
        String reason = "테스트 결제 환불";
        LocalDateTime occurredAt = LocalDateTime.now();
        //when
        PaymentRecord cancellation = PaymentRecord.createCancellation(transactionKey, amount, reason, occurredAt);
        //then
        assertThat(cancellation)
                .extracting("transactionKey", "type", "amount", "reason", "occurredAt")
                .containsExactly(
                        "transactionKey", TransactionType.REFUND, Money.wons(1000L), reason, occurredAt
                );
    }
}

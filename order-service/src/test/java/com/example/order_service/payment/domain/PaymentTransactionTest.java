package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTransactionTest {
    private IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("결제 승인 내역을 생성한다.")
    void createConfirm() {
        //given
        LocalDateTime occurredAt = LocalDateTime.now();
        //when
        PaymentTransaction transaction = PaymentTransaction.createConfirm("transactionKey", Money.wons(1000L),
                occurredAt, idGenerator);
        //then
        assertThat(transaction)
                .extracting("transactionKey", "type", "amount", "occurredAt")
                .containsExactly(
                        "transactionKey", TransactionType.PAYMENT, Money.wons(1000L), occurredAt
                );
    }

    @Test
    @DisplayName("결제 승인 내역 생성시 아이디 생성기가 누락되면 예외가 발생한다.")
    void createConfirm_idGenerator_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> PaymentTransaction.createConfirm(
                "transactionKey",
                Money.wons(1000L),
                LocalDateTime.now(),
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인시 아이디 생성기는 필수이다.");
    }

    @Test
    @DisplayName("결제 승인 내역 생성시 아이디가 누락되면 예외가 발생한다.")
    void createConfirm_id_null() {
        //given
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> PaymentTransaction.createConfirm(
                "transactionKey",
                Money.wons(1000L),
                LocalDateTime.now(),
                nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인시 아이디는 필수이다.");
    }

}
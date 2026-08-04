package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("결제를 생성한다.")
    void create(){
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

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVAL_PENDING);
    }

    @Test
    @DisplayName("결제를 생성할 때 주문 아이디가 누락되면 예외가 발생한다.")
    void create_orderId_null(){
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(null)
                .userId(1L)
                .totalAmount(Money.wons(10000L))
                .build();
        //when
        //then
        assertThatThrownBy(() -> Payment.create(context, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 생성시 주문 아이디는 필수이다.");
    }

    @Test
    @DisplayName("결제를 생성할 때 유저 아이디가 누락되면 예외가 발생한다.")
    void create_userId_null(){
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(1L)
                .userId(null)
                .totalAmount(Money.wons(10000L))
                .build();
        //when
        //then
        assertThatThrownBy(() -> Payment.create(context, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 생성시 유저 아이디는 필수이다.");
    }

    @Test
    @DisplayName("결제를 생성할 때 결제 금액이 누락되면 예외가 발생한다.")
    void create_totalAmount_null(){
        //given
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(1L)
                .userId(1L)
                .totalAmount(null)
                .build();
        //when
        //then
        assertThatThrownBy(() -> Payment.create(context, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 생성시 결제 금액은 필수이다.");
    }

    @Test
    @DisplayName("결제를 생성할 때 아이디 생성기가 누락되면 예외가 발생한다.")
    void create_idGenerator_null(){
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
    void create_id_null(){
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
                .hasMessage("결제 생성시 아이디는 필수이다");
    }
}
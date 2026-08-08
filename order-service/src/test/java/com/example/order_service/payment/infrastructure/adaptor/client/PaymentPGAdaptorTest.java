package com.example.order_service.payment.infrastructure.adaptor.client;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentPGStatus;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import com.example.order_service.payment.infrastructure.adaptor.client.pg.toss.TossPGProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentPGAdaptorTest {

    private PaymentPGAdaptor paymentPGAdaptor;

    @Mock
    private TossPGProcessor tossPGProcessor;

    @BeforeEach
    void setUp() {
        given(tossPGProcessor.getSupportedProvider()).willReturn(PaymentProvider.TOSS);
        paymentPGAdaptor = new PaymentPGAdaptor(List.of(tossPGProcessor));
    }

    @Test
    @DisplayName("결제사 프로세서를 찾아 결제 승인을 위임한다.")
    void confirm(){
        //given
        Long orderId = 1L;
        String paymentKey = "paymentKey";
        Money amount = Money.wons(1000L);
        PaymentProvider provider = PaymentProvider.TOSS;

        PGConfirmResult result = PGConfirmResult.builder()
                .status(PaymentPGStatus.DONE)
                .amount(amount)
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .approvedAt(LocalDateTime.now())
                .build();

        given(tossPGProcessor.confirm(anyLong(), anyString(), any()))
                .willReturn(result);
        //when
        PGConfirmResult confirm = paymentPGAdaptor.confirm(orderId, paymentKey, amount, provider);
        //then
        assertThat(confirm.status()).isEqualTo(PaymentPGStatus.DONE);
    }

    @Test
    @DisplayName("결제사 프로세서를 찾을 수 없으면 예외가 발생한다.")
    void confirm_notFound_processor(){
        //given
        Long orderId = 1L;
        String paymentKey = "paymentKey";
        Money amount = Money.wons(1000L);
        PaymentProvider provider = PaymentProvider.KAKAO;
        //when
        //then
        assertThatThrownBy(() -> paymentPGAdaptor.confirm(orderId, paymentKey, amount, provider))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.UNSUPPORTED_PROVIDER);
    }
}
package com.example.order_service.api.order.infrastructure.client.payment;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.order.infrastructure.client.payment.dto.request.TossPaymentConfirmRequest;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import com.example.order_service.api.support.ExcludeInfraTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

public class TossPaymentClientServiceTest extends ExcludeInfraTest {

    @Autowired
    private TossPaymentClientService tossPaymentClientService;
    @MockitoBean
    private TossPaymentClient tossPaymentClient;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @Test
    @DisplayName("토스페이먼츠로 결제 승인을 요청한다")
    void confirmPayment(){
        //given
        TossPaymentConfirmResponse response = TossPaymentConfirmResponse.builder()
                .paymentKey("paymentKey")
                .orderNo(ORDER_NO)
                .totalAmount(10000L)
                .status("DONE")
                .build();
        given(tossPaymentClient.confirmPayment(any(TossPaymentConfirmRequest.class)))
                .willReturn(response);
        //when
        TossPaymentConfirmResponse result = tossPaymentClientService.confirmPayment(ORDER_NO, "paymentKey", 10000L);
        //then
        assertThat(result)
                .extracting(TossPaymentConfirmResponse::getPaymentKey, TossPaymentConfirmResponse::getOrderNo, TossPaymentConfirmResponse::getTotalAmount,
                        TossPaymentConfirmResponse::getStatus)
                .containsExactly("paymentKey", ORDER_NO, 10000L, "DONE");
    }

    @Test
    @DisplayName("서킷브레이커가 열렸을때 결제 승인을 요청하면 예외를 던진다")
    void confirmPayment_when_open_circuitbreaker(){
        //given
        willThrow(CallNotPermittedException.class)
                .given(tossPaymentClient)
                .confirmPayment(any(TossPaymentConfirmRequest.class));
        //when
        //then
        assertThatThrownBy(() -> tossPaymentClientService.confirmPayment(ORDER_NO, "paymentKey", 10000L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.UNAVAILABLE);
    }

    @Test
    @DisplayName("결제 승인중 예외가 발생하면 그대로 예외를 던진다")
    void confirmPayment_when_PaymentException(){
        //given
        willThrow(BusinessException.class)
                .given(tossPaymentClient)
                .confirmPayment(any(TossPaymentConfirmRequest.class));
        //when
        //then
        assertThatThrownBy(() -> tossPaymentClientService.confirmPayment(ORDER_NO, "paymentKey", 10000L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("결제 승인중 알 수 없는 예외가 발생하면 예외를 던진다")
    void confirmPayment_when_InternalServerError(){
        //given
        willThrow(new RuntimeException("쿠폰 서비스 오류 발생"))
                .given(tossPaymentClient)
                .confirmPayment(any(TossPaymentConfirmRequest.class));
        //when
        //then
        assertThatThrownBy(() -> tossPaymentClientService.confirmPayment(ORDER_NO, "paymentKey", 1000L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.SYSTEM_ERROR);
    }
}

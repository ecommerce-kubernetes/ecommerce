package com.example.order_service.infrastructure.gateway;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.TossFeignClient;
import com.example.order_service.infrastructure.dto.request.TossCancelRequest;
import com.example.order_service.infrastructure.dto.request.TossConfirmRequest;
import com.example.order_service.infrastructure.dto.response.pg.TossCancelResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@IsolatedTest
public class TossGatewayTest {
    @Autowired
    private TossGateway tossGateway;
    @MockitoBean
    private TossFeignClient client;
    @MockitoBean
    private ExternalExceptionTranslator translator;


    @Test
    @DisplayName("토스 페이먼츠에 결제 승인을 요청한다")
    void confirmPayment(){
        //given
        TossConfirmResponse mockResponse = Instancio.create(TossConfirmResponse.class);
        given(client.confirmPayment(any(TossConfirmRequest.class)))
                .willReturn(mockResponse);
        //when
        TossConfirmResponse response = tossGateway.confirmPayment(1L, "paymentKey", 10000L);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("토스 페이먼츠에 결제 승인 요청중 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
    void confirmPayment_fallback_delegate_to_translator() throws Throwable {
        //given
        Long orderId = 1L;
        String paymentKey = "paymentKey";
        Long totalAmount = 10000L;

        RuntimeException feignException = new RuntimeException("feignClient 예외");
        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
        willThrow(feignException).given(client).confirmPayment(any(TossConfirmRequest.class));
        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> tossGateway.confirmPayment(orderId, paymentKey, totalAmount))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }


    @Test
    @DisplayName("토스 페이먼츠에 결제 취소를 요청한다")
    void cancelPayment(){
        //given
        TossCancelResponse mockResponse = Instancio.create(TossCancelResponse.class);
        given(client.cancelPayment(anyString(), any(TossCancelRequest.class)))
                .willReturn(mockResponse);
        //when
        TossCancelResponse response = tossGateway.cancelPayment("paymentKey", "환불요청", 10000L);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("토스 페이먼츠 결제 취소 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
    void cancelPayment_fallback_delegate_to_translator() throws Throwable {
        //given
        String paymentKey = "paymentKey";
        String cancelReason = "reason";
        Long cancelAmount = 10000L;

        RuntimeException feignException = new RuntimeException("feignClient 예외");

        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
        willThrow(feignException).given(client).cancelPayment(anyString(), any(TossCancelRequest.class));
        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> tossGateway.cancelPayment(paymentKey, cancelReason, cancelAmount))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }
}

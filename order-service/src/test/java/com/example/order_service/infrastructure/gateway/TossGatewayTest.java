package com.example.order_service.infrastructure.gateway;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.TossFeignClient;
import com.example.order_service.infrastructure.dto.request.TossClientRequest;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("결제 승인")
    class Confirm {

        @Test
        @DisplayName("토스 페이먼츠에 결제 승인을 요청한다")
        void confirmPayment(){
            //given
            TossClientResponse.Confirm mockResponse = Instancio.create(TossClientResponse.Confirm.class);
            given(client.confirmPayment(any(TossClientRequest.Confirm.class)))
                    .willReturn(mockResponse);
            //when
            TossClientResponse.Confirm response = tossGateway.confirmPayment("orderNo", "paymentKey", 10000L);
            //then
            assertThat(response)
                    .usingRecursiveComparison()
                    .isEqualTo(mockResponse);
        }

        @Test
        @DisplayName("토스 페이먼츠에 결제 승인 요청중 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
        void confirmPayment_fallback_delegate_to_translator() throws Throwable {
            //given
            String orderId = "orderNo";
            String paymentKey = "paymentKey";
            Long totalAmount = 10000L;
            //발생한 예외
            RuntimeException feignException = new RuntimeException("feignClient 예외");
            //변환된 예외
            ExternalSystemUnavailableException translatedException =
                    new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
            willThrow(feignException).given(client).confirmPayment(any(TossClientRequest.Confirm.class));
            given(translator.translate(anyString(), any(Throwable.class)))
                    .willReturn(translatedException);
            //when
            //then
            assertThatThrownBy(() -> tossGateway.confirmPayment(orderId, paymentKey, totalAmount))
                    .isInstanceOf(ExternalSystemUnavailableException.class);
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class Cancel {

        @Test
        @DisplayName("토스 페이먼츠에 결제 취소를 요청한다")
        void cancelPayment(){
            //given
            TossClientResponse.Cancel mockResponse = Instancio.create(TossClientResponse.Cancel.class);
            given(client.cancelPayment(anyString(), any(TossClientRequest.Cancel.class)))
                    .willReturn(mockResponse);
            //when
            TossClientResponse.Cancel response = tossGateway.cancelPayment("paymentKey", "환불요청", 10000L);
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
            //발생한 예외
            RuntimeException feignException = new RuntimeException("feignClient 예외");
            //변환된 예외
            ExternalSystemUnavailableException translatedException =
                    new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
            willThrow(feignException).given(client).cancelPayment(anyString(), any(TossClientRequest.Cancel.class));
            given(translator.translate(anyString(), any(Throwable.class)))
                    .willReturn(translatedException);
            //when
            //then
            assertThatThrownBy(() -> tossGateway.cancelPayment(paymentKey, cancelReason, cancelAmount))
                    .isInstanceOf(ExternalSystemUnavailableException.class);
        }
    }

    @Nested
    @DisplayName("결제 조회")
    class Inquiry {

        @Test
        @DisplayName("토스 페이먼츠 결제를 조회한다")
        void inquirePayment(){
            //given
            String paymentKey = "paymentKey";
            TossClientResponse.Inquiry mockResponse = Instancio.create(TossClientResponse.Inquiry.class);
            given(client.inquirePayment(anyString())).willReturn(mockResponse);
            //when
            TossClientResponse.Inquiry response = tossGateway.inquirePayment(paymentKey);

            //then
            assertThat(response)
                    .usingRecursiveComparison()
                    .isEqualTo(mockResponse);
        }

        @Test
        @DisplayName("토스 페이먼츠 결제 조회 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
        void inquirePayment_fallback_delegate_to_translator() throws Throwable {
            //given
            String paymentKey = "paymentKey";
            RuntimeException feignException = new RuntimeException("feignClient 예외");
            ExternalSystemUnavailableException translatedException =
                    new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
            willThrow(feignException).given(client).inquirePayment(anyString());
            given(translator.translate(anyString(), any(Throwable.class)))
                    .willReturn(translatedException);
            //when
            //then
            assertThatThrownBy(() -> tossGateway.inquirePayment(paymentKey))
                    .isInstanceOf(ExternalSystemUnavailableException.class);
        }
    }
}

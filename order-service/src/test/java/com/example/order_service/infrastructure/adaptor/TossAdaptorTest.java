package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.TossFeignClient;
import com.example.order_service.infrastructure.dto.request.TossClientRequest;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.example.order_service.support.TestFixtureUtil.giveMeOne;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@IsolatedTest
public class TossAdaptorTest {
    @Autowired
    private TossAdaptor tossAdaptor;
    @MockitoBean
    private TossFeignClient client;

    @Nested
    @DisplayName("결제 승인")
    class Confirm {

        @Test
        @DisplayName("토스 페이먼츠에 결제 승인을 요청한다")
        void confirmPayment(){
            //given
            TossClientResponse.Confirm mockResponse = giveMeOne(TossClientResponse.Confirm.class);
            given(client.confirmPayment(any(TossClientRequest.Confirm.class)))
                    .willReturn(mockResponse);
            //when
            TossClientResponse.Confirm response = tossAdaptor.confirmPayment("orderNo", "paymentKey", 10000L);
            //then
            assertThat(response)
                    .usingRecursiveComparison()
                    .isEqualTo(mockResponse);
        }

        @Test
        @DisplayName("토스 페이먼츠에 결제 승인을 요청할때 서킷브레이커가 열렸다면 시스템 예외로 변환하여 예외를 던진다")
        void confirmPayment_circuitbreaker_open(){
            //given
            CallNotPermittedException circuitException = CallNotPermittedException
                    .createCallNotPermittedException(CircuitBreaker.ofDefaults("test"));
            willThrow(circuitException).given(client)
                    .confirmPayment(any(TossClientRequest.Confirm.class));
            //when
            //then
            assertThatThrownBy(() -> tossAdaptor.confirmPayment("orderNo", "paymentKey", 10000L))
                    .isInstanceOf(ExternalSystemUnavailableException.class)
                    .hasMessage("토스 페이먼츠 서킷 브레이커 열림")
                    .extracting("errorCode")
                    .isEqualTo("CIRCUIT_BREAKER_OPEN");
        }

        @Test
        @DisplayName("토스 페이먼츠에서 결제 승인을 요청할때 external system 예외가 던져지면 그대로 던진다")
        void confirmPayment_external_system_exception(){
            //given
            willThrow(ExternalSystemException.class).given(client)
                    .confirmPayment(any(TossClientRequest.Confirm.class));
            //when
            //then
            assertThatThrownBy(() -> tossAdaptor.confirmPayment("orderNo", "paymentKey", 10000L))
                    .isInstanceOf(ExternalSystemException.class);
        }

        @Test
        @DisplayName("토스 페이먼츠에서 결제 승인을 요청할때 예외(error decoder 변환 x) 가 던져지면 시스템 예외로 변환하여 던진다")
        void confirmPayment_other_exception(){
            //given
            willThrow(RuntimeException.class).given(client)
                    .confirmPayment(any(TossClientRequest.Confirm.class));
            //when
            //then
            assertThatThrownBy(() -> tossAdaptor.confirmPayment("orderNo", "paymentKey", 10000L))
                    .isInstanceOf(ExternalSystemUnavailableException.class)
                    .hasMessage("토스 페이먼츠 통신 장애")
                    .extracting("errorCode")
                    .isEqualTo("SERVICE_UNAVAILABLE");
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class Cancel {

        @Test
        @DisplayName("토스 페이먼츠에 결제 취소를 요청한다")
        void cancelPayment(){
            //given
            TossClientResponse.Cancel mockResponse = giveMeOne(TossClientResponse.Cancel.class);
            given(client.cancelPayment(anyString(), any(TossClientRequest.Cancel.class)))
                    .willReturn(mockResponse);
            //when
            TossClientResponse.Cancel response = tossAdaptor.cancelPayment("paymentKey", "환불요청", 10000L);
            //then
            assertThat(response)
                    .usingRecursiveComparison()
                    .isEqualTo(mockResponse);
        }

        @Test
        @DisplayName("토스 페이먼츠에 결제 취소를 요청할때 서킷브레이커가 열렸다면 시스템 예외로 변환하여 예외를 던진다")
        void cancelPayment_circuitbreaker_open(){
            //given
            CallNotPermittedException circuitException = CallNotPermittedException
                    .createCallNotPermittedException(CircuitBreaker.ofDefaults("test"));
            willThrow(circuitException).given(client)
                    .cancelPayment(anyString(), any(TossClientRequest.Cancel.class));
            //when
            //then
            assertThatThrownBy(() -> tossAdaptor.cancelPayment("paymentKey", "환불요청", 10000L))
                    .isInstanceOf(ExternalSystemUnavailableException.class)
                    .hasMessage("토스 페이먼츠 서킷 브레이커 열림")
                    .extracting("errorCode")
                    .isEqualTo("CIRCUIT_BREAKER_OPEN");
        }

        @Test
        @DisplayName("토스 페이먼츠에서 결제 취소를 요청할때 external system 예외가 던져지면 그대로 던진다")
        void cancelPayment_external_system_exception(){
            //given
            willThrow(ExternalSystemException.class).given(client)
                    .cancelPayment(anyString(), any(TossClientRequest.Cancel.class));
            //when
            //then
            assertThatThrownBy(() -> tossAdaptor.cancelPayment("paymentKey", "환불요청", 10000L))
                    .isInstanceOf(ExternalSystemException.class);
        }

        @Test
        @DisplayName("토스 페이먼츠에서 결제 취소를 요청할때 예외(error decoder 변환 x) 가 던져지면 시스템 예외로 변환하여 던진다")
        void cancelPayment_other_exception(){
            //given
            willThrow(RuntimeException.class).given(client)
                    .cancelPayment(anyString(), any(TossClientRequest.Cancel.class));
            //when
            //then
            assertThatThrownBy(() -> tossAdaptor.cancelPayment("paymentKey", "환불요청", 10000L))
                    .isInstanceOf(ExternalSystemUnavailableException.class)
                    .hasMessage("토스 페이먼츠 통신 장애")
                    .extracting("errorCode")
                    .isEqualTo("SERVICE_UNAVAILABLE");
        }
    }
}

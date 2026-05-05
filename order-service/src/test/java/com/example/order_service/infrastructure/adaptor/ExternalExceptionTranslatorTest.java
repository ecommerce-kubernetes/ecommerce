package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalExceptionTranslatorTest {

    private final ExternalExceptionTranslator translator = new ExternalExceptionTranslator();

    @Test
    @DisplayName("서킷 브레이커가 열린 경우 시스템 예외로 변환한다")
    void translate_circuitbreaker() throws Throwable {
        //given
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("test");
        circuitBreaker.transitionToOpenState();
        CallNotPermittedException circuitException = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
        //when
        Throwable result = translator.translate("SERVICE", circuitException);
        //then
        assertThat(result)
                .isInstanceOf(ExternalSystemUnavailableException.class)
                .hasMessage("SERVICE 서킷 브레이커 열림")
                .extracting("errorCode")
                .isEqualTo("CIRCUIT_BREAKER_OPEN");
    }

    @Test
    @DisplayName("시스템 예외가 던져진 경우 그대로 던진다")
    void translate_external_system_exception() throws Throwable {
        //given
        ExternalSystemException exception = new ExternalSystemException("ERROR_CODE", "message");
        //when
        Throwable result = translator.translate("SERVICE", exception);
        //then
        assertThat(result)
                .isInstanceOf(ExternalSystemException.class)
                .hasMessage("message")
                .extracting("errorCode")
                .isEqualTo("ERROR_CODE");
    }

    @Test
    @DisplayName("시스템 예외가 아닌 다른 예외 발생시 시스템 예외로 변환하여 던진다")
    void translate_other_error() throws Throwable {
        //given0
        RuntimeException unknownException = new RuntimeException();
        //when
        Throwable result = translator.translate("SERVICE", unknownException);
        //then
        assertThat(result)
                .isInstanceOf(ExternalSystemUnavailableException.class)
                .hasMessage("SERVICE 통신 장애")
                .extracting("errorCode")
                .isEqualTo("SERVICE_UNAVAILABLE");
    }
}

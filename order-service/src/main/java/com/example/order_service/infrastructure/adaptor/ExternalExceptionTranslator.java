package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalExceptionTranslator {

    public Throwable translate(String service, Throwable throwable) throws Throwable {
        if (throwable instanceof CallNotPermittedException) {
            log.error("{} 서킷 브레이커 열림", service);
            return new ExternalSystemUnavailableException("CIRCUIT_BREAKER_OPEN", service + " 서킷 브레이커 열림", throwable);
        }

        if (throwable instanceof ExternalSystemException) {
            return throwable;
        }

        return new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", service + " 통신 장애", throwable);
    }
}

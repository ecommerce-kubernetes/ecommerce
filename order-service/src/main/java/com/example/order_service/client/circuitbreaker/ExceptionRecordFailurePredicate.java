package com.example.order_service.client.circuitbreaker;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
@Slf4j
public class ExceptionRecordFailurePredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof FeignException feignException) {
            int status = feignException.status();
            if (status >= 400 && status < 500) {
                return false;
            }
            if (status >= 500) {
                return true;
            }
        }
        return true;
    }
}

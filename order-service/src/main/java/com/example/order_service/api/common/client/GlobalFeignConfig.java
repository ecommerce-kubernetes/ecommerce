package com.example.order_service.api.common.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalFeignConfig {

    @Bean
    public RequestInterceptor feignTracingInterceptor(Tracer tracer, Propagator propagator) {
        return template -> {
            if (tracer.currentSpan() != null) {
                TraceContext traceContext = tracer.currentSpan().context();
                propagator.inject(
                        traceContext, // 올바른 컨텍스트 객체 전달
                        template,
                        (RequestTemplate carrier, String key, String value) -> carrier.header(key, value)
                );
            }
        };
    }
}

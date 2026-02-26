package com.example.apigatewayservice.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;


@Configuration
public class MonitoringConfig {

    @Bean
    public ObservationPredicate ignoreObservation() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                String path = serverContext.getCarrier().getURI().getPath();
                return !(path.startsWith("/actuator") || path.startsWith("/eureka"));
            }
            return true;
        };
    }

    @Bean
    public MeterFilter ignoreActuatorMetrics() {
        return new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                String uri = id.getTag("uri");
                if (uri != null && (uri.startsWith("/actuator") || uri.startsWith("/eureka"))) {
                    return MeterFilterReply.DENY;
                }
                return MeterFilterReply.NEUTRAL;
            }
        };
    }
}

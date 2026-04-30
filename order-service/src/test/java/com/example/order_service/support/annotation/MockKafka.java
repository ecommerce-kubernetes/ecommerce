package com.example.order_service.support.annotation;

import com.example.order_service.support.config.MockKafkaConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@TestPropertySource(properties = {
        "spring.cloud.bus.enabled=false",
        "spring.cloud.stream.enabled=false"
})
@Import(MockKafkaConfig.class)
public @interface MockKafka {
}

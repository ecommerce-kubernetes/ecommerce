package com.example.userservice.support.annotation;

import com.example.userservice.support.config.MockKafkaConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource(properties = {
        "spring.cloud.bus.enabled=false",
        "spring.cloud.stream.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.kafka.listener.auto-startup=false"
})
@Import(MockKafkaConfig.class)
public @interface MockKafka {
}

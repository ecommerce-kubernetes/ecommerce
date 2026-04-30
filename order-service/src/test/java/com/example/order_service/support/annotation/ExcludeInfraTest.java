package com.example.order_service.support.annotation;

import com.example.order_service.support.MockInfraConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
        "spring.cloud.bus.enabled=false",
        "spring.cloud.stream.enabled=false"
})
@EnableAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        RedisAutoConfiguration.class
})
@Import(MockInfraConfig.class)
public @interface ExcludeInfraTest {
}

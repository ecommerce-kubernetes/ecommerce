package com.example.userservice.support.annotation;

import com.example.userservice.support.config.MockRedisConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
@Import(MockRedisConfig.class)
public @interface MockRedis {
}

package com.example.order_service.support.annotation;

import com.example.order_service.support.config.RedisTestConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RedisTestConfig.class)
public @interface WithRedis {
}

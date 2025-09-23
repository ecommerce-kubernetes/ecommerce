package com.example.product_service.controller.util.specification.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminApi {
    String description() default "";
}

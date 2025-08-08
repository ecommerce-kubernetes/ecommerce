package com.example.product_service.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldNotNullValidator.class)
public @interface AtLeastOneFieldNotNull {
    String message() default "적어도 하나의 필드는 입력해야합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] fieldNames() default {};
}

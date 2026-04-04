package com.example.product_service.api.product.controller.validation.annotation;

import com.example.product_service.api.product.controller.validation.validator.UniqueOptionTypesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueOptionTypesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueOptionTypes {
    String message() default "error";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

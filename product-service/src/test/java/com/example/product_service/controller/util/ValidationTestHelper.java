package com.example.product_service.controller.util;

import com.example.product_service.dto.request.category.CategoryRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public final class ValidationTestHelper {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private ValidationTestHelper(){
    }

    public static void injectFieldValue(String fieldName, Object value, Object target){
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(target);
        beanWrapper.setPropertyValue(fieldName, value);
    }

    public static <T> Set<ConstraintViolation<T>> validateField(T target){
        return VALIDATOR.validate(target);
    }

    public static <T> void assertViolation(Set<ConstraintViolation<T>> violations, String fieldName, String expectedMessage){
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals(fieldName)
                        && v.getMessage().equals(expectedMessage));
    }

    public static <T> void assertFieldViolation(T target, String fieldName, Object injectValue, String expectedMessage){
        injectFieldValue(fieldName, injectValue, target);
        Set<ConstraintViolation<T>> violations = validateField(target);
        assertViolation(violations, fieldName, expectedMessage);
    }

}

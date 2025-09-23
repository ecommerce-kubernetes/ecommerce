package com.example.order_service.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationTestHelper {
    private static final Validator VALIDATOR;

    static{
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages");
        ms.setDefaultEncoding("UTF-8");

        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(ms);
        factory.afterPropertiesSet();
        VALIDATOR = factory.getValidator();
    }

    private ValidationTestHelper(){

    }

    public static void injectFieldValue(String fieldName, Object value, Object target){
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(target);
        beanWrapper.setPropertyValue(fieldName, value);
    }

    public static <T> Set<ConstraintViolation<T>> validateField(T target){
        return VALIDATOR.validate(target);
    }

    public static <T> void assertViolation(Set<ConstraintViolation<T>> violations, String fieldName, String exceptedMessage){
        assertThat(violations)
                .anyMatch(v -> {
                    String path = v.getPropertyPath().toString();
                    boolean fieldMatches =
                            path.equals(fieldName) ||
                                    path.startsWith(fieldName + "[") ||
                                    path.startsWith(fieldName + ".");
                    return fieldMatches && v.getMessage().equals(exceptedMessage);
                });
    }

    public static <T> void assertFieldViolation(T target, String fieldName, Object injectValue, String expectedMessage){
        injectFieldValue(fieldName, injectValue, target);
        Set<ConstraintViolation<T>> violations = validateField(target);
        assertViolation(violations, fieldName, expectedMessage);
    }
}

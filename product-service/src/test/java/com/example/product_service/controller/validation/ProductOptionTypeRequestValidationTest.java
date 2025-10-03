package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.product_service.common.MessagePath.NOT_NULL;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static com.example.product_service.util.ValidationTestHelper.assertFieldViolation;
import static com.example.product_service.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProductOptionTypeRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidProductOptionTypeRequestFieldProvider")
    void productOptionTypeRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ProductOptionTypeRequest request = new ProductOptionTypeRequest(1L, 0);
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("ProductOptionTypeRequest 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void productOptionTypeRequestValidation_multiple(){
        ProductOptionTypeRequest request = new ProductOptionTypeRequest();
        Set<ConstraintViolation<ProductOptionTypeRequest>> violations = validateField(request);
        List<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).toList();

        assertThat(violations).hasSize(2);
        assertThat(fields).containsExactlyInAnyOrder("optionTypeId", "priority");
    }

    @Test
    @DisplayName("ProductOptionTypeRequest 오류 없음")
    void productOptionTypeRequestValidation_thenOk(){
        ProductOptionTypeRequest request = new ProductOptionTypeRequest(1L, 0);
        Set<ConstraintViolation<ProductOptionTypeRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> invalidProductOptionTypeRequestFieldProvider(){
        return Stream.of(
                Arguments.of("optionTypeId", null, getMessage(NOT_NULL)),
                Arguments.of("optionTypeId", "", getMessage(NOT_NULL)),
                Arguments.of("priority", null, getMessage(NOT_NULL)),
                Arguments.of("priority", -1, "must be at least 0")
        );

    }
}

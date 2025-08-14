package com.example.product_service.controller.validation;

import com.example.product_service.controller.util.MessagePath;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
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

import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static com.example.product_service.controller.util.ValidationTestHelper.assertFieldViolation;
import static com.example.product_service.controller.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class VariantOptionValueRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidVariantOptionValueRequestProvider")
    void variantOptionValueRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        VariantOptionValueRequest request = new VariantOptionValueRequest(1L, 1L);
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("variantOptionValueRequest 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void updateProductVariantRequestValidation_multiple(){
        VariantOptionValueRequest request = new VariantOptionValueRequest(null, null);
        Set<ConstraintViolation<VariantOptionValueRequest>> violations = validateField(request);
        List<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).toList();

        assertThat(violations).hasSize(2);
        assertThat(fields).containsExactlyInAnyOrder("optionTypeId", "optionValueId");
    }

    static Stream<Arguments> invalidVariantOptionValueRequestProvider(){
        return Stream.of(
                Arguments.of("optionTypeId", null, getMessage(NOT_NULL)),
                Arguments.of("optionValueId", null, getMessage(NOT_NULL))
        );
    }
}

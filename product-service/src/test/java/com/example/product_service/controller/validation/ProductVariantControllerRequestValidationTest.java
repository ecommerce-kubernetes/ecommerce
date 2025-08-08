package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.product_service.controller.util.TestMessageUtil.*;
import static com.example.product_service.controller.util.ValidationTestHelper.assertFieldViolation;
import static com.example.product_service.controller.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class ProductVariantControllerRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidUpdateProductValiantRequestProvider")
    void updateProductVariantRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        UpdateProductVariantRequest request = new UpdateProductVariantRequest(100, 100, 10, List.of(1L, 2L));
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("updateProductRequest 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void updateProductVariantRequestValidation_multiple(){
        UpdateProductVariantRequest request = new UpdateProductVariantRequest(-1, 0, -1,List.of(1L, 2L));
        Set<ConstraintViolation<UpdateProductVariantRequest>> violations = validateField(request);
        List<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).toList();

        assertThat(violations).hasSize(3);
        assertThat(fields).containsExactlyInAnyOrder("price", "stockQuantity", "discountRate");
    }

    @Test
    @DisplayName("UpdateProductRequest 오류 없음")
    void updateProductVariantRequestValidation_thenOk(){
        UpdateProductVariantRequest request = new UpdateProductVariantRequest(100, 10, 5, List.of(1L, 2L));
        Set<ConstraintViolation<UpdateProductVariantRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> invalidUpdateProductValiantRequestProvider(){
        return Stream.of(
                Arguments.of("price", -1, "must be at least 0"),
                Arguments.of("price", 100000001, "must not be greater than 100000000"),
                Arguments.of("stockQuantity", 0, "must be at least 1"),
                Arguments.of("discountRate", -1, "must be at least 0"),
                Arguments.of("discountRate", 101, "must not be greater than 100")
        );
    }
}

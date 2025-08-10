package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.variant.ProductVariantRequest;
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

import static com.example.product_service.controller.util.MessagePath.NOT_BLANK;
import static com.example.product_service.controller.util.MessagePath.NOT_NULL;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static com.example.product_service.controller.util.ValidationTestHelper.assertFieldViolation;
import static com.example.product_service.controller.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProductVariantRequestValidationTest {
    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidProductVariantRequestFieldProvider")
    void productVariantRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ProductVariantRequest request = createProductVariantRequest();
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("ProductVariantRequest 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void productVariantRequestValidation_multiple(){
        ProductVariantRequest request = new ProductVariantRequest();
        Set<ConstraintViolation<ProductVariantRequest>> violations = validateField(request);
        List<String> field = violations.stream().map(v -> v.getPropertyPath().toString()).toList();

        assertThat(violations).hasSize(3);

        assertThat(field).containsExactlyInAnyOrder("sku", "price", "stockQuantity");
    }

    static Stream<Arguments> invalidProductVariantRequestFieldProvider(){
        return Stream.of(
                Arguments.of("sku", null, getMessage(NOT_BLANK)),
                Arguments.of("sku", "", getMessage(NOT_BLANK)),
                Arguments.of("price", null, getMessage(NOT_NULL)),
                Arguments.of("price", "", getMessage(NOT_NULL)),
                Arguments.of("price", -1, "must be at least 0"),
                Arguments.of("price", 100000001, "must not be greater than 100000000"),
                Arguments.of("stockQuantity", null, getMessage(NOT_NULL)),
                Arguments.of("stockQuantity", 0, "must be at least 1"),
                Arguments.of("discountRate", -1, "must be at least 0"),
                Arguments.of("discountRate", 101, "must not be greater than 100")
        );
    }

    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest(
                "sku", 100, 100, 10,
                List.of(1L, 2L)
        );
    }
}

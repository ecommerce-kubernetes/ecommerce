package com.example.product_service.controller.validation;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
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

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.*;
import static com.example.product_service.util.ValidationTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProductRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidProductRequestFieldProvider")
    void productRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ProductRequest request = createProductRequest();
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("ProductRequest 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void productRequestValidation_multiple(){
        ProductRequest productRequest = new ProductRequest();
        Set<ConstraintViolation<ProductRequest>> violations = validateField(productRequest);
        assertThat(violations).hasSize(4);
        List<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).toList();
        assertThat(fields).containsExactlyInAnyOrder("name", "categoryId", "images", "productVariants");
    }

    @Test
    @DisplayName("ProductRequest 오류 없음")
    void productRequestValidation_thenOk(){
        ProductRequest request = createProductRequest();
        Set<ConstraintViolation<ProductRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> invalidProductRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", getMessage(NOT_BLANK)),
                Arguments.of("categoryId", null, getMessage(NOT_NULL)),
                Arguments.of("images", List.of(), getMessage(NOT_EMPTY)),
                Arguments.of("images", null, getMessage(NOT_EMPTY)),
                Arguments.of("images", List.of("invalid"), getMessage(INVALID_URL_MESSAGE)),
                Arguments.of("productVariants", List.of(), getMessage(NOT_EMPTY))
        );
    }

    private ProductRequest createProductRequest() {
        return new ProductRequest("name",
                "description",
                1L,
                List.of("http://test.jpg"),
                List.of(new ProductOptionTypeRequest(1L, 0)),
                createProductVariantRequestList());
    }
    private List<ProductVariantRequest> createProductVariantRequestList(){
        return List.of(createProductVariantRequest());
    }

    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest(
                "sku", 100, 100, 10,
                List.of(new VariantOptionValueRequest(1L, 1L),
                        new VariantOptionValueRequest(2L, 5L))
        );
    }
}

package com.example.product_service.controller.validation;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
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
import static com.example.product_service.controller.util.ValidationTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class ProductControllerRequestValidationTest {

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

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidImageRequestFieldProvider")
    void imageRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage ){
        ImageRequest request = new ImageRequest("http://test.jpg", 0);
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    static Stream<Arguments> invalidProductRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", getMessage("product.name.notBlank")),
                Arguments.of("categoryId", null, getMessage("product.categoryId.notNull")),
                Arguments.of("images", List.of(), getMessage("product.images.notEmpty")),
                Arguments.of("images", null, getMessage("product.images.notEmpty")),
                Arguments.of("productVariants", List.of(), getMessage("product.productVariants.notEmpty"))
        );
    }

    static Stream<Arguments> invalidImageRequestFieldProvider(){
        return Stream.of(
                Arguments.of("url", "", getMessage("image.url.notBlank")),
                Arguments.of("url", "invalidUrl", getMessage("invalid.url")),
                Arguments.of("sortOrder", null, getMessage("image.sortOrder.notNull")),
                Arguments.of("sortOrder", "", getMessage("image.sortOrder.notNull")),
                Arguments.of("sortOrder", -1, getMessage("image.sortOrder.min"))
        );
    }

    private ProductRequest createProductRequest() {
        return new ProductRequest("name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 0)),
                List.of(new ProductVariantRequest(
                        "sku", 100, 100, 10,
                        List.of(1L, 2L)
                )));
    }
}

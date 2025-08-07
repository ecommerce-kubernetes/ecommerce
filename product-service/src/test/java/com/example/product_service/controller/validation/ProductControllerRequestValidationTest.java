package com.example.product_service.controller.validation;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
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

    private static final String NOT_BLANK_MESSAGE_PATH = "NotBlank";
    private static final String NOT_EMPTY_MESSAGE_PATH = "NotEmpty";
    private static final String NOTNULL_MESSAGE_PATH = "NotNull";
    private static final String INVALID_URL_MESSAGE_PATH = "InvalidUrl";

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

    @Test
    @DisplayName("ImageRequest 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void imageRequestValidation_multiple(){
        ImageRequest request = new ImageRequest();
        Set<ConstraintViolation<ImageRequest>> violations = validateField(request);
        List<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).toList();

        assertThat(violations).hasSize(2);
        assertThat(fields).containsExactlyInAnyOrder("url", "sortOrder");
    }

    @Test
    @DisplayName("ImageRequest 오류 없음")
    void imageRequestValidation_thenOk(){
        ImageRequest request = new ImageRequest("http://test.jpg", 0);
        Set<ConstraintViolation<ImageRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

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

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidUpdateProductBasicRequestFieldProvider")
    void updateProductBasicRequestValidation_field(String fieldNane, Object invalidValue, String expectedMessage){
        UpdateProductBasicRequest request = createUpdateProductBasicRequest();
        assertFieldViolation(request, fieldNane, invalidValue, expectedMessage);
    }

    static Stream<Arguments> invalidUpdateProductBasicRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", getMessage(NOT_BLANK_MESSAGE_PATH))
        );
    }

    static Stream<Arguments> invalidProductRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", getMessage(NOT_BLANK_MESSAGE_PATH)),
                Arguments.of("categoryId", null, getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("images", List.of(), getMessage(NOT_EMPTY_MESSAGE_PATH)),
                Arguments.of("images", null, getMessage(NOT_EMPTY_MESSAGE_PATH)),
                Arguments.of("productVariants", List.of(), getMessage(NOT_EMPTY_MESSAGE_PATH))
        );
    }

    static Stream<Arguments> invalidImageRequestFieldProvider(){
        return Stream.of(
                Arguments.of("url", "", getMessage(NOT_BLANK_MESSAGE_PATH)),
                Arguments.of("url", "invalidUrl", getMessage(INVALID_URL_MESSAGE_PATH)),
                Arguments.of("sortOrder", null, getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("sortOrder", "", getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("sortOrder", -1, "must be at least 0")
        );
    }

    static Stream<Arguments> invalidProductOptionTypeRequestFieldProvider(){
        return Stream.of(
                Arguments.of("optionTypeId", null, getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("optionTypeId", "", getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("priority", null, getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("priority", -1, "must be at least 0")
        );

    }

    static Stream<Arguments> invalidProductVariantRequestFieldProvider(){
        return Stream.of(
                Arguments.of("sku", null, getMessage(NOT_BLANK_MESSAGE_PATH)),
                Arguments.of("sku", "", getMessage(NOT_BLANK_MESSAGE_PATH)),
                Arguments.of("price", null, getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("price", "", getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("price", -1, "must be at least 0"),
                Arguments.of("price", 100000001, "must not be greater than 100000000"),
                Arguments.of("stockQuantity", null, getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("stockQuantity", 0, "must be at least 1"),
                Arguments.of("discountRate", -1, "must be at least 0"),
                Arguments.of("discountRate", 101, "must not be greater than 100")
        );
    }
    private ProductRequest createProductRequest() {
        return new ProductRequest("name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 0)),
                createProductVariantRequestList());
    }
    private List<ProductVariantRequest> createProductVariantRequestList(){
        return List.of(createProductVariantRequest());
    }

    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest(
                "sku", 100, 100, 10,
                List.of(1L, 2L)
        );
    }

    private UpdateProductBasicRequest createUpdateProductBasicRequest(){
        return new UpdateProductBasicRequest("updated", "description", 1L);
    }
}

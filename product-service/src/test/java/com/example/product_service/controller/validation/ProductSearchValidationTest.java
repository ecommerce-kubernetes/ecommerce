package com.example.product_service.controller.validation;

import com.example.product_service.dto.ProductSearch;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.product_service.controller.util.MessagePath.NOT_BLANK;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static com.example.product_service.controller.util.ValidationTestHelper.assertFieldViolation;
import static com.example.product_service.controller.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProductSearchValidationTest {
    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidProductSearchFieldProvider")
    void productSearchValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ProductSearch search = new ProductSearch(1L, "search", 3);
        assertFieldViolation(search, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("ProductSearch 필드 동시 오류 검증")
    void productSearchValidation_multiple(){
        ProductSearch search = new ProductSearch(-1L, "", -1);
        Set<ConstraintViolation<ProductSearch>> violations = validateField(search);
        List<String> field =
                violations.stream().map(v -> v.getPropertyPath().toString()).toList();
        assertThat(violations).hasSize(3);

        assertThat(field).containsExactlyInAnyOrder("categoryId", "name", "rating");
    }

    static Stream<Arguments> invalidProductSearchFieldProvider(){
        return Stream.of(
                Arguments.of("categoryId", 0, "must be at least 1"),
                Arguments.of("name", "", getMessage(NOT_BLANK)),
                Arguments.of("rating", -1, "must be at least 0"),
                Arguments.of("rating", 6, "must not be greater than 5")
        );
    }
}

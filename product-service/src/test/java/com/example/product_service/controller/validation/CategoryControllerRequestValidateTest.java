package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
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
import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class CategoryControllerRequestValidateTest {
    private static final String INVALID_URL = "invalidUrl";
    private static final String VALID_URL = "http://test.jpg";

    private static final String NAME_FIELD_ERROR_MESSAGE_PATH = "category.name.notBlank";
    private static final String URL_FIELD_ERROR_MESSAGE_PATH = "invalid.url";

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidCategoryRequestFieldProvider")
    void categoryRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        CategoryRequest request =
                new CategoryRequest("노트북", 1L, VALID_URL);
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidUpdateCategoryRequestFieldProvider")
    void updateCategoryRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        UpdateCategoryRequest request = new UpdateCategoryRequest("노트북", 1L, VALID_URL);
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("Category Request 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void categoryRequestValidation_multiple(){
        CategoryRequest request = new CategoryRequest("", 1L, INVALID_URL);

        Set<ConstraintViolation<CategoryRequest>> violations = validateField(request);

        assertThat(violations).hasSize(2);

        List<String> fields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(fields)
                .containsExactlyInAnyOrder("name", "iconUrl");
    }

    @Test
    @DisplayName("Category Request 오류 없음")
    void categoryRequestValidation_thenOk(){
        CategoryRequest categoryRequest = new CategoryRequest("name", 1L, VALID_URL);
        Set<ConstraintViolation<CategoryRequest>> violations = validateField(categoryRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateCategoryRequest 오류 없음")
    void updateCategoryRequestValidation_thenOk(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("name", 1L, VALID_URL);
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> invalidCategoryRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", getMessage(NAME_FIELD_ERROR_MESSAGE_PATH)),
                Arguments.of("iconUrl", INVALID_URL, getMessage(URL_FIELD_ERROR_MESSAGE_PATH))
        );
    }

    static Stream<Arguments> invalidUpdateCategoryRequestFieldProvider(){
        return Stream.of(
                Arguments.of("iconUrl", INVALID_URL, getMessage(URL_FIELD_ERROR_MESSAGE_PATH))
        );
    }
}

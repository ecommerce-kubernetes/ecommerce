package com.example.product_service.controller.validation;

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

import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static com.example.product_service.controller.util.ValidationTestHelper.assertFieldViolation;
import static com.example.product_service.controller.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class UpdateCategoryRequestValidationTest {

    private static final String INVALID_URL = "invalidUrl";
    private static final String VALID_URL = "http://test.jpg";

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidUpdateCategoryRequestFieldProvider")
    void updateCategoryRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        UpdateCategoryRequest request = new UpdateCategoryRequest("노트북", 1L, VALID_URL);
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("UpdateCategoryRequest 오류 없음")
    void updateCategoryRequestValidation_thenOk(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("name", 1L, VALID_URL);
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateCategoryRequest 필드가 모두 null 일때")
    void updateCategoryRequestValidation_allFieldNull(){
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validateField(request);
        assertThat(violations).isNotEmpty();
        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage).toList();

        assertThat(messages).containsExactlyInAnyOrder(getMessage(EMPTY_REQUEST));
    }

    static Stream<Arguments> invalidUpdateCategoryRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", getMessage(NOT_BLANK)),
                Arguments.of("iconUrl", INVALID_URL, getMessage(INVALID_URL_MESSAGE))
        );
    }
}

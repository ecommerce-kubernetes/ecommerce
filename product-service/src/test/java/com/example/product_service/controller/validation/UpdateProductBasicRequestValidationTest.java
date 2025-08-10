package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
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

import static com.example.product_service.controller.util.MessagePath.EMPTY_REQUEST;
import static com.example.product_service.controller.util.MessagePath.NOT_BLANK;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static com.example.product_service.controller.util.ValidationTestHelper.assertFieldViolation;
import static com.example.product_service.controller.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UpdateProductBasicRequestValidationTest {
    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidUpdateProductBasicRequestFieldProvider")
    void updateProductBasicRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        UpdateProductBasicRequest request = createUpdateProductBasicRequest();
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("UpdateProductBasicRequest 모든 필드가 null일때")
    void updateProductValidation_allFieldNull(){
        UpdateProductBasicRequest request = new UpdateProductBasicRequest();
        Set<ConstraintViolation<UpdateProductBasicRequest>> violations = validateField(request);
        List<String> messages = violations.stream().map(ConstraintViolation::getMessage).toList();
        assertThat(violations).isNotEmpty();
        assertThat(messages).containsExactlyInAnyOrder(getMessage(EMPTY_REQUEST));
    }

    private UpdateProductBasicRequest createUpdateProductBasicRequest(){
        return new UpdateProductBasicRequest("updated", "description", 1L);
    }

    static Stream<Arguments> invalidUpdateProductBasicRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", getMessage(NOT_BLANK)),
                Arguments.of("name", " ", getMessage(NOT_BLANK))
        );
    }
}

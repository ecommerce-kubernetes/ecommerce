package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.UpdateOptionValueRequest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;
import java.util.stream.Stream;

import static com.example.product_service.controller.util.TestMessageUtil.*;
import static com.example.product_service.controller.util.ValidationTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class OptionValueControllerRequestValidateTest {

    private static final String NOT_BLANK_ERROR_MESSAGE_PATH = "NotBlank";

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidOptionValueRequestFieldProvider")
    void optionValueRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        OptionValueRequest request = new OptionValueRequest("value");
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("UpdateOptionValueRequest 필드 검증")
    void updateOptionValueRequestValidation_field(){
        UpdateOptionValueRequest request = new UpdateOptionValueRequest(1L, "value");
        assertFieldViolation(request, "value", "", getMessage(NOT_BLANK_ERROR_MESSAGE_PATH));
    }

    @Test
    @DisplayName("OptionValueRequest 오류 없음")
    void optionValueRequestValidation_thenOk(){
        OptionValueRequest request = new OptionValueRequest("value");
        Set<ConstraintViolation<OptionValueRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> invalidOptionValueRequestFieldProvider(){
        return Stream.of(
                Arguments.of("value", "", getMessage(NOT_BLANK_ERROR_MESSAGE_PATH))
        );
    }
}

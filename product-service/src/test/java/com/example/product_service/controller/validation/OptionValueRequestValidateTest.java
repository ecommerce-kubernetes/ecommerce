package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.options.OptionValueRequest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.Stream;

import static com.example.product_service.common.MessagePath.NOT_BLANK;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static com.example.product_service.util.ValidationTestHelper.assertFieldViolation;
import static com.example.product_service.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class OptionValueRequestValidateTest {


    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidOptionValueRequestFieldProvider")
    void optionValueRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        OptionValueRequest request = new OptionValueRequest("value");
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
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
                Arguments.of("valueName", "", getMessage(NOT_BLANK))
        );
    }
}

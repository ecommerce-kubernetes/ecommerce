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

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.product_service.controller.util.TestMessageUtil.*;
import static com.example.product_service.controller.util.ValidationTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class OptionValueControllerRequestValidateTest {

    private static final String OPTION_TYPE_ID_FIELD_ERROR_MESSAGE_PATH = "NotNull";
    private static final String VALUE_FIELD_ERROR_MESSAGE_PATH = "NotBlank";

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidOptionValueRequestFieldProvider")
    void optionValueRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        OptionValueRequest request = new OptionValueRequest(1L, "value");
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("OptionValueRequest 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void optionValueRequestValidation_multiple(){
        OptionValueRequest request = new OptionValueRequest(null, "");

        Set<ConstraintViolation<OptionValueRequest>> violations = validateField(request);

        assertThat(violations).hasSize(2);

        List<String> fields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(fields)
                .containsExactlyInAnyOrder("optionTypeId", "value");
    }

    @Test
    @DisplayName("UpdateOptionValueRequest 필드 검증")
    void updateOptionValueRequestValidation_field(){
        UpdateOptionValueRequest request = new UpdateOptionValueRequest(1L, "value");
        assertFieldViolation(request, "value", "", getMessage(VALUE_FIELD_ERROR_MESSAGE_PATH));
    }

    @Test
    @DisplayName("OptionValueRequest 오류 없음")
    void optionValueRequestValidation_thenOk(){
        OptionValueRequest request = new OptionValueRequest(1L, "value");
        Set<ConstraintViolation<OptionValueRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> invalidOptionValueRequestFieldProvider(){
        return Stream.of(
                Arguments.of("optionTypeId", null, getMessage(OPTION_TYPE_ID_FIELD_ERROR_MESSAGE_PATH)),
                Arguments.of("optionTypeId", "", getMessage(OPTION_TYPE_ID_FIELD_ERROR_MESSAGE_PATH)),
                Arguments.of("value", "", getMessage(VALUE_FIELD_ERROR_MESSAGE_PATH))
        );
    }
}

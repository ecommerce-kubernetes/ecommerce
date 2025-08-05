package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.options.OptionValueRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static com.example.product_service.controller.util.ValidationTestHelper.*;

@ExtendWith(SpringExtension.class)
public class OptionValueControllerRequestValidateTest {

    private static final String OPTION_TYPE_ID_FIELD_ERROR_MESSAGE_PATH = "optionValue.optionTypeId.notNull";
    private static final String VALUE_FIELD_ERROR_MESSAGE_PATH = "optionValue.value.notBlank";
    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidOptionValueRequestFieldProvider")
    void optionValueRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        OptionValueRequest request = new OptionValueRequest(1L, "value");
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    static Stream<Arguments> invalidOptionValueRequestFieldProvider(){
        return Stream.of(
                Arguments.of("optionTypeId", null, getMessage(OPTION_TYPE_ID_FIELD_ERROR_MESSAGE_PATH)),
                Arguments.of("optionTypeId", "", getMessage(OPTION_TYPE_ID_FIELD_ERROR_MESSAGE_PATH)),
                Arguments.of("value", "", getMessage(VALUE_FIELD_ERROR_MESSAGE_PATH))
        );
    }
}

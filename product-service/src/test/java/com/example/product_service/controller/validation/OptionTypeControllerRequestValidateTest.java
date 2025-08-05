package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.options.OptionTypeRequest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static com.example.product_service.controller.util.ValidationTestHelper.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class OptionTypeControllerRequestValidateTest {

    private static final String NAME_FIELD_ERROR_MESSAGE_PATH = "optionType.name.notBlank";

    @Test
    @DisplayName("OptionTypeRequest 필드 검증")
    void optionTypeRequestValidation_field(){
        OptionTypeRequest request = new OptionTypeRequest("optionType");
        assertFieldViolation(request, "name", "", getMessage(NAME_FIELD_ERROR_MESSAGE_PATH));
    }

    @Test
    @DisplayName("OptionTypeRequest 검증 성공")
    void optionTypeRequestValidation_thenOk(){
        OptionTypeRequest request = new OptionTypeRequest("name");
        Set<ConstraintViolation<OptionTypeRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }
}

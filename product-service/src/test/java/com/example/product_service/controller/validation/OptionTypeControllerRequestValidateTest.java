package com.example.product_service.controller.validation;

import com.example.product_service.controller.util.ValidationTestHelper;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.example.product_service.controller.util.ValidationTestHelper.*;

@ExtendWith(SpringExtension.class)
public class OptionTypeControllerRequestValidateTest {

    @Test
    @DisplayName("OptionTypeRequest 필드 검증")
    void optionTypeRequestValidation_field(){
        OptionTypeRequest request = new OptionTypeRequest("optionType");
        assertFieldViolation(request, "name", "", "name is required");
    }
}

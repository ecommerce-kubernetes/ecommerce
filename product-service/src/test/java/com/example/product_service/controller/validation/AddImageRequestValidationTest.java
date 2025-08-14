package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.image.AddImageRequest;
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

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static com.example.product_service.util.ValidationTestHelper.*;

@ExtendWith(MockitoExtension.class)
public class AddImageRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidAddImageRequestFieldProvider")
    void addImageRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        AddImageRequest request = new AddImageRequest();
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("ImageRequestUrl 검증")
    void addImageRequestValidation_url(){
        AddImageRequest request = new AddImageRequest(List.of("invalid"));
        Set<ConstraintViolation<AddImageRequest>> violations = validateField(request);

        assertViolation(violations, "imageUrls[0].<list element>", getMessage(INVALID_URL_MESSAGE));
    }

    static Stream<Arguments> invalidAddImageRequestFieldProvider(){
        return Stream.of(
                Arguments.of("imageUrls", null, getMessage(NOT_NULL)),
                Arguments.of("imageUrls", List.of(), getMessage(NOT_EMPTY))
        );
    }
}

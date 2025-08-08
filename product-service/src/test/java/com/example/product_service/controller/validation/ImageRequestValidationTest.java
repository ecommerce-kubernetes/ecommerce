package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.image.ImageRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static com.example.product_service.controller.util.ValidationTestHelper.*;

@ExtendWith(SpringExtension.class)
public class ImageRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidImageRequestFieldProvider")
    void ImageRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ImageRequest request = new ImageRequest("http:test.jpg", 0);
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    static Stream<Arguments> invalidImageRequestFieldProvider(){
        return Stream.of(
                Arguments.of("url", "", getMessage(NOT_BLANK)),
                Arguments.of("url", " ", getMessage(NOT_BLANK)),
                Arguments.of("sortOrder" , -1 , "must be at least 0")
        );
    }
}

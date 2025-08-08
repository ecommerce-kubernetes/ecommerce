package com.example.product_service.controller.validation;

import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.controller.util.ValidationTestHelper;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
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

import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static com.example.product_service.controller.util.ValidationTestHelper.*;

@ExtendWith(SpringExtension.class)
public class ProductImageControllerRequestValidationTest {

    private static final String NOTNULL_MESSAGE_PATH = "NotNull";
    private static final String NOT_EMPTY_MESSAGE_PATH = "NotEmpty";
    private static final String NOT_BLANK_MESSAGE_PATH = "NotBlank";

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidAddImageRequestFieldProvider")
    void addImageRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        AddImageRequest request = new AddImageRequest();
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidImageRequestFieldProvider")
    void ImageRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ImageRequest request = new ImageRequest("http:test.jpg", 0);
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("ImageRequestUrl 검증")
    void addImageRequestValidation_url(){
        AddImageRequest request = new AddImageRequest(List.of("invalid"));
        Set<ConstraintViolation<AddImageRequest>> violations = validateField(request);

        assertViolation(violations, "imageUrls[0].<list element>", getMessage("InvalidUrl"));
    }

    static Stream<Arguments> invalidAddImageRequestFieldProvider(){
        return Stream.of(
                Arguments.of("imageUrls", null, getMessage(NOTNULL_MESSAGE_PATH)),
                Arguments.of("imageUrls", List.of(), getMessage(NOT_EMPTY_MESSAGE_PATH))
        );
    }

    static Stream<Arguments> invalidImageRequestFieldProvider(){
        return Stream.of(
                Arguments.of("url", "", getMessage(NOT_BLANK_MESSAGE_PATH)),
                Arguments.of("url", " ", getMessage(NOT_BLANK_MESSAGE_PATH)),
                Arguments.of("sortOrder" , -1 , "must be at least 0")
        );
    }
}

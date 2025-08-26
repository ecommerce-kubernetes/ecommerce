package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.image.ImageRequest;
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
import static com.example.product_service.util.ValidationTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ImageRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidImageRequestFieldProvider")
    void ImageRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ImageRequest request = new ImageRequest("http:test.jpg");
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("ImageRequest 오류 없음")
    void imageRequestValidation_thenOk(){
        ImageRequest request = new ImageRequest("http://test.jpg");
        Set<ConstraintViolation<ImageRequest>> violations = validateField(request);
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> invalidImageRequestFieldProvider(){
        return Stream.of(
                Arguments.of("url", "", getMessage(NOT_BLANK)),
                Arguments.of("url", " ", getMessage(NOT_BLANK))
        );
    }
}

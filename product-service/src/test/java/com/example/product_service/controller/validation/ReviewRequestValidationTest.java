package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.review.ReviewRequest;
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

import static com.example.product_service.common.MessagePath.NOT_BLANK;
import static com.example.product_service.common.MessagePath.NOT_NULL;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static com.example.product_service.util.ValidationTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ReviewRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidReviewRequestProvider")
    void reviewRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ReviewRequest request = new ReviewRequest(1L, 3, "good", List.of("http://test.jpg"));
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("reviewRequest url 검증")
    void reviewRequestValidation_url(){
        ReviewRequest request = new ReviewRequest(1L, 3, "good", List.of(""));
        Set<ConstraintViolation<ReviewRequest>> violations = validateField(request);

        assertThat(violations).hasSize(1);
        assertViolation(violations, "imageUrls[0].<list element>", getMessage("NotBlank"));

        request.setImageUrls(List.of("asdfasdf"));
        violations = validateField(request);

        assertThat(violations).hasSize(1);
        assertViolation(violations, "imageUrls[0].<list element>", getMessage("InvalidUrl"));

    }

    static Stream<Arguments> invalidReviewRequestProvider(){
        return Stream.of(
                Arguments.of("orderId", null, getMessage(NOT_NULL)),
                Arguments.of("orderId", "", getMessage(NOT_NULL)),
                Arguments.of("rating", null , getMessage(NOT_NULL)),
                Arguments.of("rating", "", getMessage(NOT_NULL)),
                Arguments.of("rating", 0, "must be at least 1"),
                Arguments.of("rating", 6, "must not be greater than 5"),
                Arguments.of("content", "", getMessage(NOT_BLANK))
        );
    }
}

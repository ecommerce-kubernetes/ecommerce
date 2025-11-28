package com.example.order_service.controller.validation;

import com.example.order_service.dto.request.CartItemRequest;
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

import static com.example.order_service.common.MessagePath.NOT_NULL;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static com.example.order_service.util.ValidationTestHelper.assertFieldViolation;
import static com.example.order_service.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CartItemRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidCartItemRequestFieldProvider")
    void cartItemRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        CartItemRequest request = createCartItemRequest();
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("CartItemRequest 필드 동시 오류 검증 테스트")
    void cartItemRequestValidation_multiple(){
        CartItemRequest request = new CartItemRequest();
        Set<ConstraintViolation<CartItemRequest>> violations = validateField(request);
        List<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).toList();

        assertThat(violations).hasSize(2);
        assertThat(fields).containsExactlyInAnyOrder("productVariantId", "quantity");
    }

    @Test
    @DisplayName("CartItemRequest 오류 없음")
    void cartItemRequestValidation_thenOk(){
        CartItemRequest cartItemRequest = createCartItemRequest();
        Set<ConstraintViolation<CartItemRequest>> violations = validateField(cartItemRequest);
        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> invalidCartItemRequestFieldProvider(){
        return Stream.of(
                Arguments.of("productVariantId", null, getMessage(NOT_NULL)),
                Arguments.of("quantity", null, getMessage(NOT_NULL)),
                Arguments.of("productVariantId", "", getMessage(NOT_NULL)),
                Arguments.of("quantity", "", getMessage(NOT_NULL))
        );
    }

    private CartItemRequest createCartItemRequest(){
//        return new CartItemRequest(1L, 10);

        return null;
    }
}

package com.example.order_service.controller.validation;

import com.example.order_service.dto.request.OrderItemRequest;
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

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static com.example.order_service.util.ValidationTestHelper.assertFieldViolation;
import static com.example.order_service.util.ValidationTestHelper.validateField;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class OrderItemRequestValidationTest {

    @ParameterizedTest
    @MethodSource("invalidOrderItemRequestFieldProvider")
    void orderItemRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        OrderItemRequest request = createOrderItemRequest();
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("OrderItemRequest 다중 필드 검증 테스트")
    void orderItemRequestValidation_multiple(){
        OrderItemRequest request = new OrderItemRequest();
        Set<ConstraintViolation<OrderItemRequest>> violations = validateField(request);
        List<String> field = violations.stream().map(v -> v.getPropertyPath().toString()).toList();

        assertThat(violations).hasSize(2);
        assertThat(field).containsExactlyInAnyOrder("productVariantId", "quantity");
    }

    @Test
    @DisplayName("OrderItemRequest 필드 검증 성공")
    void orderItemRequestValidation_thenOk(){
        OrderItemRequest request = createOrderItemRequest();
        Set<ConstraintViolation<OrderItemRequest>> violations = validateField(request);
        assertThat(violations).hasSize(0);
    }

    private OrderItemRequest createOrderItemRequest(){
        return new OrderItemRequest(1L, 10);
    }

    static Stream<Arguments> invalidOrderItemRequestFieldProvider(){
        return Stream.of(
                Arguments.of("productVariantId", null, getMessage(NOT_NULL)),
                Arguments.of("productVariantId", "", getMessage(NOT_NULL)),
                Arguments.of("productVariantId", " ", getMessage(NOT_NULL)),
                Arguments.of("quantity", null, getMessage(NOT_NULL)),
                Arguments.of("quantity", "", getMessage(NOT_NULL)),
                Arguments.of("quantity", " ", getMessage(NOT_NULL)),
                Arguments.of("quantity", 0, "must be at least 1")
        );
    }
}

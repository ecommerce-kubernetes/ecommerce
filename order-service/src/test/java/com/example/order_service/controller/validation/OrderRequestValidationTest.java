package com.example.order_service.controller.validation;

import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
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
public class OrderRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidOrderRequestFieldProvider")
    void orderRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        OrderRequest request = createOrderRequest();
        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }

    @Test
    @DisplayName("OrderRequest 다중 필드 검증 테스트")
    void orderRequestValidation_multiple(){
        OrderRequest request = new OrderRequest();
        Set<ConstraintViolation<OrderRequest>> violations = validateField(request);
        List<String> field = violations.stream().map(v -> v.getPropertyPath().toString()).toList();
        assertThat(violations).hasSize(2);
        assertThat(field).containsExactlyInAnyOrder("items", "deliveryAddress");
    }

    private OrderRequest createOrderRequest(){
        List<OrderItemRequest> orderItemRequests =
                List.of(
                        new OrderItemRequest(1L, 10),
                        new OrderItemRequest(2L, 10)
                );
        return new OrderRequest(orderItemRequests, "deliveryAddress", 1L, 3000L, 5000L);
    }

    static Stream<Arguments> invalidOrderRequestFieldProvider(){
        return Stream.of(
                Arguments.of("items", null, getMessage(NOT_EMPTY)),
                Arguments.of("items", List.of(), getMessage(NOT_EMPTY)),
                Arguments.of("deliveryAddress", null, getMessage(NOT_BLANK)),
                Arguments.of("deliveryAddress", "", getMessage(NOT_BLANK)),
                Arguments.of("deliveryAddress", " ", getMessage(NOT_BLANK))
        );
    }
}

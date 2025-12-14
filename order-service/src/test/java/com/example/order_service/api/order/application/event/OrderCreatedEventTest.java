package com.example.order_service.api.order.application.event;

import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderCreatedEventTest {

    @Test
    @DisplayName("주문 생성 이벤트를 생성한다")
    void of(){
        //given
        OrderItemDto orderItem1 = createOrderItemDto(1L, 1L, 1L);
        OrderItemDto orderItem2 = createOrderItemDto(2L, 2L, 2L);
        OrderCreationResult orderCreationresult = createOrderCreationresult(1L, List.of(orderItem1, orderItem2));
        //when
        OrderCreatedEvent orderEvent = OrderCreatedEvent.from(orderCreationresult);
        //then
        assertThat(orderEvent.getUserId()).isEqualTo(1L);
        assertThat(orderEvent.getOrderedVariantIds()).hasSize(2)
                .contains(1L, 2L);
    }

    private OrderCreationResult createOrderCreationresult(Long userId, List<OrderItemDto> orderItems){
        return OrderCreationResult.of(1L, userId, "PENDING", "상품1 외 1건", "서울시 테헤란로 123",
                LocalDateTime.now(), PaymentInfo.builder().build(), orderItems, AppliedCoupon.builder().build());
    }

    private OrderItemDto createOrderItemDto(Long orderItemId, Long productId, Long productVariantId) {
        return OrderItemDto.builder()
                .orderItemId(orderItemId)
                .productId(productId)
                .productVariantId(productVariantId)
                .productName("상품")
                .thumbnailUrl("http://thumbnail.jpg")
                .quantity(3)
                .lineTotal(10000L)
                .unitPrice(null)
                .itemOptionDtos(null)
                .build();
    }
}

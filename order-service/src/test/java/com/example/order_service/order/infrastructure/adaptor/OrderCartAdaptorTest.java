package com.example.order_service.order.infrastructure.adaptor;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.common.exception.gateway.CartGatewayErrorCode;
import com.example.order_service.common.exception.gateway.DefaultPortException;
import com.example.order_service.order.application.port.dto.result.OrderCartItemsResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderCartAdaptorTest {

    @InjectMocks
    private OrderCartAdaptor orderCartAdaptor;

    @Mock
    private CartQueryService cartQueryService;

    @Test
    @DisplayName("장바구니 항목 정보를 조회한다.")
    void getCartItems() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        CartItemData cartItem = CartItemData.builder()
                .cartItemId(cartItemId)
                .productVariantId(1L)
                .quantity(3)
                .build();

        given(cartQueryService.findCartItemsByCartItemIds(anyLong(), anyList())).willReturn(List.of(cartItem));
        //when
        OrderCartItemsResult result = orderCartAdaptor.getCartItems(userId, List.of(cartItemId));
        //then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items())
                .extracting("cartItemId", "productVariantId", "quantity")
                .containsExactly(tuple(cartItemId, 1L, 3));
    }

    @Test
    @DisplayName("장바구니 항목 조회중 예외가 발생하면 포트 예외로 변환된다.")
    void getCartItems_throw_exception() {
        //given
        given(cartQueryService.findCartItemsByCartItemIds(anyLong(), anyList()))
                .willThrow(new RuntimeException("장바구니 항목 조회중 예외 발생"));
        //when
        //then
        assertThatThrownBy(() -> orderCartAdaptor.getCartItems(1L, List.of(1L, 2L)))
                .isInstanceOf(DefaultPortException.class)
                .extracting("errorCode")
                .isEqualTo(CartGatewayErrorCode.CART_SERVER_ERROR);
    }
}
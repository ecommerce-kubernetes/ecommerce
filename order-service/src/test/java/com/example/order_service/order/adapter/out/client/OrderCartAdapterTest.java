package com.example.order_service.order.adapter.out.client;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.order.application.port.dto.OrderCartItemsResult;
import com.example.order_service.order.exception.OrderCartPortErrorCode;
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
class OrderCartAdapterTest {

    @InjectMocks
    private OrderCartAdapter orderCartAdapter;

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
        OrderCartItemsResult result = orderCartAdapter.getCartItems(userId, List.of(cartItemId));
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
        String errorCode = "장바구니 항목 조회중 예외 발생";
        given(cartQueryService.findCartItemsByCartItemIds(anyLong(), anyList()))
                .willThrow(new RuntimeException(errorCode));
        //when
        //then
        assertThatThrownBy(() -> orderCartAdapter.getCartItems(1L, List.of(1L, 2L)))
                .isInstanceOf(PortException.class)
                .hasMessage(String.format("Port Error: [%s] %s", errorCode, "장바구니 상품 조회중 에러 발생"))
                .extracting("errorCode")
                .isEqualTo(OrderCartPortErrorCode.CART_SERVER_ERROR);
    }
}
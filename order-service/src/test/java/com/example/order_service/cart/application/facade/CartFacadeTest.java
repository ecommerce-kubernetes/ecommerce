package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.CartCommand;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.CartItemAvailability;
import com.example.order_service.cart.application.dto.result.CartItemResult;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartService;
import com.example.order_service.cart.application.dto.result.CartItemDto;
import com.example.order_service.cart.application.dto.result.CartResult;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import com.example.order_service.common.exception.application.ErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartFacadeTest {

    @InjectMocks
    private CartFacade cartFacade;
    @Mock
    private CartProductGateway cartProductGateway;
    @Mock
    private CartCommandService cartCommandService;
    @Mock
    private CartItemValidator validator;
    @Mock
    private CartService cartService;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {

        @Test
        @DisplayName("상품을 조회하여 상품 검증 후 장바구니에 추가한다")
        void addItem() {
            //given
            AddCartItemsCommand.Item item = Instancio.of(AddCartItemsCommand.Item.class)
                    .set(field("productVariantId"), 1L)
                    .create();
            AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                    .set(field("items"), List.of(item))
                    .create();
            CartProductResult product = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item.productVariantId())
                    .set(field("status"), CartProductStatus.ON_SALE)
                    .set(field("stock"), item.quantity() + 100)
                    .create();
            CartProductListResult productList = Instancio.of(CartProductListResult.class)
                    .set(field("products"), List.of(product))
                    .create();
            CartItemData savedItem = Instancio.of(CartItemData.class)
                    .set(field("productVariantId"), item.productVariantId())
                    .set(field("quantity"), item.quantity())
                    .create();
            given(cartProductGateway.getProducts(anyList())).willReturn(productList);
            doNothing().when(validator).validate(any(AddCartItemsCommand.class), any());
            given(cartCommandService.addCartItems(any(AddCartItemsCommand.class))).willReturn(List.of(savedItem));
            //when
            CartResult cartResult = cartFacade.addItems(command);
            //then
            assertThat(cartResult.items()).hasSize(1);
            assertThat(cartResult.items())
                    .extracting("status", "productVariantId")
                    .containsExactly(
                            tuple(CartItemAvailability.AVAILABLE, item.productVariantId())
                    );
        }
    }

    @Nested
    @DisplayName("장바구니 목록 조회")
    class GetCartDetails {

        @Test
        @DisplayName("장바구니에 담긴 상품이 없는 경우 빈 리스트를 반환한다")
        void getCartDetails_empty_cart() {
            //given
            given(cartService.getCartItems(anyLong()))
                    .willReturn(List.of());
            //when
            CartResult result = cartFacade.getCartDetails(1L);
            //then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("장바구니 목록을 조회한다")
        void getCartDetails() {
            //given
            List<CartItemDto> cartItems = List.of(
                    Instancio.of(CartItemDto.class)
                            .set(field("productVariantId"), 1L)
                            .set(field("quantity"), 1)
                            .create(),
                    Instancio.of(CartItemDto.class)
                            .set(field("productVariantId"), 2L)
                            .set(field("quantity"), 1)
                            .create(),
                    Instancio.of(CartItemDto.class)
                            .set(field("productVariantId"), 3L)
                            .set(field("quantity"), 1)
                            .create()
            );

            given(cartService.getCartItems(1L))
                    .willReturn(cartItems);
            //when
            CartResult result = cartFacade.getCartDetails(1L);
            //then
            assertThat(result.items()).hasSize(3)
                    .extracting(CartItemResult::productVariantId);
        }
    }

    @Nested
    @DisplayName("장바구니 수정")
    class Update {

        @Test
        @DisplayName("장바구니에 상품 수량을 변경한다")
        void updateCartItemQuantity() {
            //given
            CartCommand.UpdateQuantity command = CartCommand.UpdateQuantity.builder()
                    .userId(1L)
                    .cartItemId(1L)
                    .quantity(3)
                    .build();
            CartItemDto updatedCartItem = CartItemDto.builder()
                    .id(1L)
                    .productVariantId(1L)
                    .quantity(3)
                    .build();
            given(cartService.updateQuantity(anyLong(), anyLong(), anyInt())).willReturn(updatedCartItem);
            //when
            CartResult result = cartFacade.updateCartItemQuantity(command);
            //then
        }
    }
}

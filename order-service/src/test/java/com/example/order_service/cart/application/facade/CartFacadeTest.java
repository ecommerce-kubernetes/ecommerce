package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.CartCommand;

import com.example.order_service.cart.application.dto.result.CartItemResult;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.service.CartService;
import com.example.order_service.cart.application.dto.result.CartItemDto;
import com.example.order_service.cart.application.dto.result.CartResult;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    private CartService cartService;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {

        @Test
        @DisplayName("장바구니에 상품이 추가되면 상품 정보가 포함된 응답값을 반환한다")
        void addItem() {
            //given
            AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                    .generate(field(CartCommand.AddItems::items),
                            gen -> gen.collection().size(2))
                    .create();
        }

        @Test
        @DisplayName("요청한 상품 중 장바구니에 추가할 수 없는 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_ProductNotOnSale() {
            //given
            AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                    .generate(field(AddCartItemsCommand::items),
                            gen -> gen.collection().size(2))
                    .create();
            //when
            //then
        }

        @Test
        @DisplayName("상품 정보에 누락된 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_product_not_found() {
            //given
            AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                    .generate(field(CartCommand.AddItems::items),
                            gen -> gen.collection().size(2))
                    .create();
            //when
            //then
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

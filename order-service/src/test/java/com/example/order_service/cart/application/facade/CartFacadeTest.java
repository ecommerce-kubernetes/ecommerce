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
    private CartCommandService cartCommandService;
    @Mock
    private CartService cartService;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {

        @Test
        @DisplayName("상품을 조회하여 상품 검증 후 장바구니에 추가한다")
        void addItem() {
            //given
            AddCartItemsCommand.Item item = Instancio.create(AddCartItemsCommand.Item.class);
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

        @Test
        @DisplayName("상품 정보에 누락된 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_product_not_found() {
            //given
            AddCartItemsCommand.Item item1 = Instancio.create(AddCartItemsCommand.Item.class);
            AddCartItemsCommand.Item item2 = Instancio.create(AddCartItemsCommand.Item.class);
            AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                    .set(field("items"), List.of(item1, item2))
                    .create();

            CartProductResult product = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item1.productVariantId())
                    .set(field("status"), CartProductStatus.ON_SALE)
                    .set(field("stock"), item1.quantity() + 100)
                    .create();
            CartProductListResult productList = Instancio.of(CartProductListResult.class)
                    .set(field("products"), List.of(product))
                    .create();

            given(cartProductGateway.getProducts(anyList())).willReturn(productList);
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(command))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("요청한 상품 중 장바구니에 추가할 수 없는 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_ProductNotOnSale() {
            //given
            AddCartItemsCommand.Item item1 = Instancio.create(AddCartItemsCommand.Item.class);
            AddCartItemsCommand.Item item2 = Instancio.create(AddCartItemsCommand.Item.class);
            AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                    .set(field("items"), List.of(item1, item2))
                    .create();

            CartProductResult product1 = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item1.productVariantId())
                    .set(field("status"), CartProductStatus.ON_SALE)
                    .set(field("stock"), item1.quantity() + 100)
                    .create();
            CartProductResult product2 = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item1.productVariantId())
                    .set(field("status"), CartProductStatus.STOP_SALE)
                    .set(field("stock"), item2.quantity() + 100)
                    .create();
            CartProductListResult productList = Instancio.of(CartProductListResult.class)
                    .set(field("products"), List.of(product1, product2))
                    .create();

            given(cartProductGateway.getProducts(anyList())).willReturn(productList);
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(command))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("요청한 상품 중 수량이 부족한 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_product_stock_insufficient() {
            //given
            AddCartItemsCommand.Item item1 = Instancio.create(AddCartItemsCommand.Item.class);
            AddCartItemsCommand.Item item2 = Instancio.create(AddCartItemsCommand.Item.class);
            AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                    .set(field("items"), List.of(item1, item2))
                    .create();

            CartProductResult product1 = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item1.productVariantId())
                    .set(field("status"), CartProductStatus.ON_SALE)
                    .set(field("stock"), item1.quantity() + 100)
                    .create();
            CartProductResult product2 = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item1.productVariantId())
                    .set(field("status"), CartProductStatus.ON_SALE)
                    .set(field("stock"), item2.quantity() - 10)
                    .create();
            CartProductListResult productList = Instancio.of(CartProductListResult.class)
                    .set(field("products"), List.of(product1, product2))
                    .create();

            given(cartProductGateway.getProducts(anyList())).willReturn(productList);
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(command))
                    .isInstanceOf(RuntimeException.class);
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

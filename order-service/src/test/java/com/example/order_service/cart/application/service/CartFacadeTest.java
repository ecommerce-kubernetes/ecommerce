package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.fixture.CartCommandFixture;
import com.example.order_service.cart.application.fixture.CartDataFixture;
import com.example.order_service.cart.application.fixture.CartProductFixture;
import com.example.order_service.cart.application.port.CartProductPort;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.service.dto.data.CartItemData;
import com.example.order_service.cart.application.service.dto.result.*;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartFacadeTest {

    @InjectMocks
    private CartFacade cartFacade;
    @Mock
    private CartProductPort cartProductPort;
    @Mock
    private CartCommandService cartCommandService;
    @Mock
    private CartQueryService cartQueryService;


    @BeforeEach
    void setUp() {
        CartItemValidator cartItemValidator = new CartItemValidator();
        CartContextFactory cartContextFactory = new CartContextFactory();
        cartFacade = new CartFacade(cartCommandService, cartProductPort, cartQueryService, cartItemValidator, cartContextFactory);
    }

    @Test
    @DisplayName("장바구니에 상품을 추가한다.")
    void addItems() {
        //given
        AddCartItemsCommand addCommand = CartCommandFixture.anAddCommand().build();
        CartProductResult productResult = CartProductFixture.anProducts().build();

        given(cartProductPort.getProducts(anyList())).willReturn(productResult);
        given(cartCommandService.addCartItems(anyLong(), any(AddCartItemsContext.class))).willReturn(List.of(1L));
        //when
        AddCartItemsResult result = cartFacade.addItems(addCommand);
        //then
        assertThat(result.items())
                .hasSize(1);

        assertThat(result.items())
                .allSatisfy(item ->
                        assertThat(item.cartItemId()).isNotNull());
    }

    @Test
    @DisplayName("장바구니가 빈 경우 상품을 조회하지 않고 빈 결과를 반환한다")
    void getCartDetails_whenCartEmpty_thenReturnEmptyListWithoutQueryingProducts() {
        //given
        Long userId = 1L;
        given(cartQueryService.findCartItems(anyLong())).willReturn(Collections.emptyList());
        //when
        CartResult result = cartFacade.getCartDetails(userId);
        //then
        assertThat(result.items()).isEmpty();
        verify(cartProductPort, never()).getProducts(any());
    }

    @Test
    @DisplayName("상품 정보가 없는 장바구니 항목도 판매 불가 상태로 반환한다")
    void getCartDetails_whenProductNotFound_thenReturnItemAsNotForSale() {
        //given
        Long userId = 1L;
        Long productVariantId = 1L;
        int quantity = 3;
        CartItemData cartItemData = CartDataFixture.anCartItemData().productVariantId(productVariantId).quantity(quantity).build();

        CartProductResult productResult = CartProductFixture.anProducts().products(Collections.emptyList()).build();

        given(cartQueryService.findCartItems(anyLong())).willReturn(List.of(cartItemData));
        given(cartProductPort.getProducts(anyList())).willReturn(productResult);
        //when
        CartResult result = cartFacade.getCartDetails(userId);
        //then
        assertThat(result.items()).hasSize(1);

        assertThat(result.items())
                .extracting("productVariantId", "status", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(productVariantId, CartItemAvailability.NOT_FOR_SALE, quantity)
                );
    }

    @Test
    @DisplayName("장바구니 항목 상품 정보를 반환한다")
    void getCartDetails() {
        //given
        Long userId = 1L;
        CartItemData item1 = CartDataFixture.anCartItemData().productVariantId(1L).quantity(3).build();
        CartItemData item2 = CartDataFixture.anCartItemData().productVariantId(2L).quantity(3).build();
        CartItemData item3 = CartDataFixture.anCartItemData().productVariantId(3L).quantity(3).build();
        CartItemData item4 = CartDataFixture.anCartItemData().productVariantId(4L).quantity(3).build();

        CartProductResult.CartProductDetail product1 = CartProductFixture.anProduct()
                .productVariantId(item1.productVariantId()).status(CartProductStatus.ON_SALE).build();

        CartProductResult.CartProductDetail product2 = CartProductFixture.anProduct()
                .productVariantId(item2.productVariantId()).status(CartProductStatus.STOP_SALE).build();

        CartProductResult.CartProductDetail product3 = CartProductFixture.anProduct()
                .productVariantId(item3.productVariantId()).status(CartProductStatus.ON_SALE).stock(1).build();

        CartProductResult productResult = CartProductFixture.anProducts().products(List.of(product1, product2, product3)).build();

        given(cartQueryService.findCartItems(anyLong())).willReturn(List.of(item1, item2, item3, item4));
        given(cartProductPort.getProducts(anyList())).willReturn(productResult);
        //when
        CartResult result = cartFacade.getCartDetails(userId);
        //then
        assertThat(result.items()).hasSize(4);
        assertThat(result.items())
                .extracting("productVariantId", "status", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(item1.productVariantId(), CartItemAvailability.AVAILABLE, item1.quantity()),
                        tuple(item2.productVariantId(), CartItemAvailability.NOT_FOR_SALE, item2.quantity()),
                        tuple(item3.productVariantId(), CartItemAvailability.LACK_OF_STOCK, item3.quantity()),
                        tuple(item4.productVariantId(), CartItemAvailability.NOT_FOR_SALE, item4.quantity())
                );
    }

    @Test
    @DisplayName("장바구니 항목 정보를 조회한다")
    void getCartItemDetails() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        CartItemData cartItem = CartDataFixture.anCartItemData().cartItemId(cartItemId).build();
        CartProductResult productResult = CartProductFixture.anProducts().build();

        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItem);
        given(cartProductPort.getProducts(anyList())).willReturn(productResult);
        //when
        CartItemResult result = cartFacade.getCartItemDetails(userId, cartItemId);
        //then
        assertThat(result)
                .extracting("cartItemId", "status", "quantity")
                .containsExactly(
                        cartItem.cartItemId(), CartItemAvailability.AVAILABLE, cartItem.quantity()
                );
    }

    @Test
    @DisplayName("상품 정보가 없는 장바구니 항목도 판매 불가 상태로 반환한다")
    void getCartItemDetails_whenProductNotFound_thenReturnItemAsNotForSale() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;

        CartItemData cartItem = CartDataFixture.anCartItemData().cartItemId(cartItemId).build();
        CartProductResult productResult = CartProductFixture.anProducts().products(Collections.emptyList()).build();

        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItem);
        given(cartProductPort.getProducts(anyList())).willReturn(productResult);
        //when
        CartItemResult result = cartFacade.getCartItemDetails(userId, cartItemId);
        //then
        assertThat(result)
                .extracting("cartItemId", "status", "quantity")
                .containsExactly(
                        cartItemId, CartItemAvailability.NOT_FOR_SALE, cartItem.quantity()
                );
    }

    @Test
    @DisplayName("장바구니 항목이 판매 불가인 경우 판매 불가 상태를 반환한다")
    void getCartItemDetails_whenProductNotOnSale_thenReturnNotForSale() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;

        CartItemData cartItem = CartDataFixture.anCartItemData().cartItemId(cartItemId).build();
        CartProductResult.CartProductDetail product = CartProductFixture.anProduct().status(CartProductStatus.STOP_SALE).build();
        CartProductResult productResult = CartProductFixture.anProducts().products(List.of(product)).build();

        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItem);
        given(cartProductPort.getProducts(anyList())).willReturn(productResult);
        //when
        CartItemResult result = cartFacade.getCartItemDetails(userId, cartItemId);
        //then
        assertThat(result)
                .extracting("cartItemId", "status", "quantity")
                .containsExactly(
                        cartItemId, CartItemAvailability.NOT_FOR_SALE, cartItem.quantity()
                );
    }

    @Test
    @DisplayName("장바구니 항목의 상품 재고가 부족한 경우 판매 재고 부족를 반환한다")
    void getCartItemDetails_whenInsufficientProductStock_thenReturnLackOfStock() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;

        CartItemData cartItem = CartDataFixture.anCartItemData().cartItemId(cartItemId).quantity(3).build();
        CartProductResult.CartProductDetail product = CartProductFixture.anProduct().stock(1).build();
        CartProductResult productResult = CartProductFixture.anProducts().products(List.of(product)).build();

        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItem);
        given(cartProductPort.getProducts(anyList())).willReturn(productResult);
        //when
        CartItemResult result = cartFacade.getCartItemDetails(userId, cartItemId);
        //then
        assertThat(result)
                .extracting("cartItemId", "status", "quantity")
                .containsExactly(
                        cartItemId, CartItemAvailability.LACK_OF_STOCK, cartItem.quantity()
                );
    }

    @Test
    @DisplayName("장바구니 항목의 수량을 변경한다")
    void updateCartItemQuantity() {
        //given
        int quantity = 3;

        UpdateCartItemQuantityCommand updateQuantityCommand = CartCommandFixture.anUpdateQuantityCommand().quantity(quantity).build();
        CartItemData cartItem = CartDataFixture.anCartItemData().build();

        CartProductResult productResult = CartProductFixture.anProducts().build();

        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItem);
        given(cartProductPort.getProducts(anyList())).willReturn(productResult);

        doNothing().when(cartCommandService).updateCartItemQuantity(anyLong(), any(UpdateCartItemContext.class));
        //when
        UpdateCartItemQuantityResult result = cartFacade.updateCartItemQuantity(updateQuantityCommand);
        //then
        assertThat(result.cartItemId()).isEqualTo(cartItem.cartItemId());
    }

    @Test
    @DisplayName("장바구니 상품을 삭제한다")
    void deleteCartItems() {
        //given
        DeleteCartItemsCommand command = CartCommandFixture.anDeleteCommand().build();
        doNothing().when(cartCommandService).deleteCartItems(anyLong(), anyList());
        //when
        //then
        assertThatCode(() -> cartFacade.deleteCartItems(command))
                .doesNotThrowAnyException();
    }
}

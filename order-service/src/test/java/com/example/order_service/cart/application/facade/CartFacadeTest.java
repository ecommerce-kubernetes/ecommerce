package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.*;
import com.example.order_service.cart.application.facade.mapper.CartMapper;
import com.example.order_service.cart.application.port.CartProductPort;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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
    private CartProductPort cartProductPort;
    @Mock
    private CartCommandService cartCommandService;
    @Mock
    private CartQueryService cartQueryService;
    @Mock
    private CartItemValidator validator;
    @Spy
    private CartMapper cartMapper = Mappers.getMapper(CartMapper.class);


    @Test
    @DisplayName("장바구니에 상품을 추가한다.")
    void addItems() {
        //given
        AddCartItemsCommand addCommand = createAddCommand(1L, 3);
        CartProductResult productData = createProductList(1L, CartProductStatus.ON_SALE, 100);

        given(cartProductPort.getProducts(anyList())).willReturn(productData);
        doNothing().when(validator).validatePurchasable(anyList());
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
    void getCartDetails_empty_cart() {
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
    @DisplayName("장바구니 조회 시 상품 정보가 없는 항목도 함께 반환한다")
    void getCartDetails_missing_productData() {
        //given
        Long userId = 1L;
        Long productVariantId = 1L;
        int quantity = 2;
        CartItemData cartItemData = createCartItemData(productVariantId, quantity);

        CartProductResult productData = Instancio.of(CartProductResult.class)
                .set(field("products"), Collections.emptyList())
                .create();

        given(cartQueryService.findCartItems(anyLong())).willReturn(List.of(cartItemData));
        given(cartProductPort.getProducts(anyList())).willReturn(productData);
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
        CartItemData item1 = createCartItemData(1L, 3);
        CartItemData item2 = createCartItemData(2L, 3);
        CartItemData item3 = createCartItemData(3L, 3);
        CartItemData item4 = createCartItemData(4L, 3);

        CartProductResult.CartProductDetail product1 = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), item1.productVariantId())
                .set(field("status"), CartProductStatus.ON_SALE)
                .create();

        CartProductResult.CartProductDetail product2 = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), item2.productVariantId())
                .set(field("status"), CartProductStatus.STOP_SALE)
                .create();

        CartProductResult.CartProductDetail product3 = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), item3.productVariantId())
                .set(field("status"), CartProductStatus.ON_SALE)
                .set(field("stock"), 2)
                .create();

        CartProductResult productData = Instancio.of(CartProductResult.class)
                .set(field("products"), List.of(product1, product2, product3))
                .create();

        given(cartQueryService.findCartItems(anyLong())).willReturn(List.of(item1, item2, item3, item4));
        given(cartProductPort.getProducts(anyList())).willReturn(productData);
        //when
        CartResult result = cartFacade.getCartDetails(userId);
        //then
        assertThat(result.items()).hasSize(4);
        assertThat(result.items())
                .extracting("productVariantId", "status", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(1L, CartItemAvailability.AVAILABLE, 3),
                        tuple(2L, CartItemAvailability.NOT_FOR_SALE, 3),
                        tuple(3L, CartItemAvailability.LACK_OF_STOCK, 3),
                        tuple(4L, CartItemAvailability.NOT_FOR_SALE, 3)
                );
    }

    @Test
    @DisplayName("장바구니 항목 정보를 조회한다")
    void getCartItemDetails() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        CartItemData cartItemData = createCartItemData(1L, 3);
        CartProductResult productData = createProductList(1L, CartProductStatus.ON_SALE, 100);
        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItemData);
        given(cartProductPort.getProducts(anyList())).willReturn(productData);
        //when
        CartItemResult result = cartFacade.getCartItemDetails(userId, cartItemId);
        //then
        assertThat(result)
                .extracting("cartItemId", "status", "quantity")
                .containsExactly(
                        cartItemData.cartItemId(), CartItemAvailability.AVAILABLE, 3
                );
    }

    @Test
    @DisplayName("장바구니 항목의 상품 정보가 누락된 경우 구매불가 상태의 기본 정보를 반환한다")
    void getCartItemDetails_missing_product() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        CartItemData cartItemData = createCartItemData(1L, 3);
        CartProductResult productData = CartProductResult.builder().products(Collections.emptyList()).build();
        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItemData);
        given(cartProductPort.getProducts(anyList())).willReturn(productData);
        //when
        CartItemResult result = cartFacade.getCartItemDetails(userId, cartItemId);
        //then
        assertThat(result)
                .extracting("cartItemId", "status", "quantity")
                .containsExactly(
                        cartItemData.cartItemId(), CartItemAvailability.NOT_FOR_SALE, 3
                );
    }

    @Test
    @DisplayName("장바구니 항목이 판매 불가인 경우 판매 불가 상태를 반환한다")
    void getCartItemDetails_item_not_availability() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        CartItemData cartItemData = createCartItemData(1L, 3);
        CartProductResult productData = createProductList(1L, CartProductStatus.STOP_SALE, 100);
        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItemData);
        given(cartProductPort.getProducts(anyList())).willReturn(productData);
        //when
        CartItemResult result = cartFacade.getCartItemDetails(userId, cartItemId);
        //then
        assertThat(result)
                .extracting("cartItemId", "status", "quantity")
                .containsExactly(
                        cartItemData.cartItemId(), CartItemAvailability.NOT_FOR_SALE, 3
                );
    }

    @Test
    @DisplayName("장바구니 항목의 상품 재고가 부족한 경우 판매 재고 부족를 반환한다")
    void getCartItemDetails_item_insufficient_stock() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        CartItemData cartItemData = createCartItemData(1L, 3);
        CartProductResult productData = createProductList(1L, CartProductStatus.ON_SALE, 2);
        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItemData);
        given(cartProductPort.getProducts(anyList())).willReturn(productData);
        //when
        CartItemResult result = cartFacade.getCartItemDetails(userId, cartItemId);
        //then
        assertThat(result)
                .extracting("cartItemId", "status", "quantity")
                .containsExactly(
                        cartItemData.cartItemId(), CartItemAvailability.LACK_OF_STOCK, 3
                );
    }

    @Test
    @DisplayName("장바구니 항목의 수량을 변경한다")
    void updateCartItemQuantity() {
        //given
        Long cartItemId = 1L;
        Long productVariantId = 1L;
        int quantity = 3;

        UpdateCartItemQuantityCommand command = createUpdateQuantityCommand(cartItemId, quantity);
        CartItemData cartItemData = createCartItemData(productVariantId, quantity);

        CartProductResult productData = createProductList(productVariantId, CartProductStatus.ON_SALE, 100);

        given(cartQueryService.getCartItem(anyLong(), anyLong())).willReturn(cartItemData);
        given(cartProductPort.getProducts(anyList())).willReturn(productData);

        doNothing().when(validator).validatePurchasable(any(CartProductResult.CartProductDetail.class));
        doNothing().when(cartCommandService).updateCartItemQuantity(any());
        //when
        UpdateCartItemQuantityResult result = cartFacade.updateCartItemQuantity(command);
        //then
        assertThat(result.cartItemId()).isNotNull();
    }

    @Nested
    @DisplayName("장바구니 상품 삭제")
    class DeleteCartItems {

        @Test
        @DisplayName("장바구니 상품을 삭제한다")
        void deleteCartItems() {
            //given
            Long cartItemId = 1L;
            DeleteCartItemsCommand command = createDeleteCommand(cartItemId);
            doNothing().when(cartCommandService).deleteCartItems(anyLong(), anyList());
            //when
            //then
            assertThatCode(() -> cartFacade.deleteCartItems(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("상품 삭제중 예외가 발생하면 예외를 전파한다")
        void deleteCartItems_commandService_thrown_exception() {
            //given
            Long cartItemId = 1L;
            DeleteCartItemsCommand command = createDeleteCommand(cartItemId);
            willThrow(new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND))
                    .given(cartCommandService).deleteCartItems(anyLong(), anyList());
            //when
            //then
            assertThatThrownBy(() -> cartFacade.deleteCartItems(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    private DeleteCartItemsCommand createDeleteCommand(Long cartItemId) {
        return Instancio.of(DeleteCartItemsCommand.class)
                .set(field("cartItemIds"), List.of(cartItemId))
                .create();
    }

    private UpdateCartItemQuantityCommand createUpdateQuantityCommand(Long cartItemId, int quantity) {
        return Instancio.of(UpdateCartItemQuantityCommand.class)
                .set(field("cartItemId"), cartItemId)
                .set(field("quantity"), quantity)
                .create();
    }

    private AddCartItemsCommand createAddCommand(Long productVariantId, int quantity) {
        AddCartItemsCommand.Item item = Instancio.of(AddCartItemsCommand.Item.class)
                .set(field("productVariantId"), productVariantId)
                .set(field("quantity"), quantity)
                .create();

        return Instancio.of(AddCartItemsCommand.class)
                .set(field("items"), List.of(item))
                .create();
    }

    private CartProductResult createProductList(Long productVariantId, CartProductStatus status, int stock) {
        CartProductResult.CartProductDetail product = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), productVariantId)
                .set(field("status"), status)
                .set(field("stock"), stock)
                .create();
        return Instancio.of(CartProductResult.class)
                .set(field("products"), List.of(product))
                .create();
    }

    private CartItemData createCartItemData(Long productVariantId, int quantity) {
        return Instancio.of(CartItemData.class)
                .set(field("productVariantId"), productVariantId)
                .set(field("quantity"), quantity)
                .create();
    }
}

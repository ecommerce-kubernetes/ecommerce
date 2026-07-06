package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.CartItemAvailability;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.cart.application.dto.result.CartResult;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
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
    private CartQueryService cartQueryService;
    @Mock
    private CartItemValidator validator;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {

        @Test
        @DisplayName("상품을 검증 후 장바구니에 추가하고 AVAILABLE 상태의 응답을 반환한다")
        void addItems() {
            //given
            AddCartItemsCommand command = createAddCommand(1L, 3);
            CartProductListResult productList = createProductList(1L, CartProductStatus.ON_SALE);
            CartItemData savedItem = createCartItemData(1L, 3);
            given(cartProductGateway.getProducts(anyList())).willReturn(productList);
            doNothing().when(validator).validate(any(AddCartItemsCommand.class), any());
            doNothing().when(cartCommandService).addCartItems(any(AddCartItemsCommand.class));
            given(cartQueryService.getCartItems(anyLong())).willReturn(List.of(savedItem));
            //when
            CartResult cartResult = cartFacade.addItems(command);
            //then
            assertThat(cartResult.items()).hasSize(1);
            assertThat(cartResult.items())
                    .extracting("status", "productVariantId")
                    .containsExactly(
                            tuple(CartItemAvailability.AVAILABLE, 1L)
                    );

            verify(cartProductGateway, times(2)).getProducts(anyList());
            verify(cartCommandService, times(1)).addCartItems(any());
        }
        
        @Test
        @DisplayName("장바구니 조회 후 누락된 상품이 존재한다면 NOT_FOR_SALE 상태의 응답을 반환한다")
        void addItems_missing_product() {
            //given
            Long addItemId = 1L;
            Long existItemId = 2L;
            AddCartItemsCommand command = createAddCommand(addItemId, 3);
            CartProductListResult addItemResult = createProductList(addItemId, CartProductStatus.ON_SALE);

            given(cartProductGateway.getProducts(List.of(addItemId))).willReturn(addItemResult);
            willDoNothing().given(validator).validate(any(), any());
            willDoNothing().given(cartCommandService).addCartItems(any());

            CartItemData existingSavedItem = createCartItemData(existItemId, 1);
            CartItemData newSavedItem = createCartItemData(addItemId, 3);
            given(cartQueryService.getCartItems(anyLong()))
                    .willReturn(List.of(existingSavedItem, newSavedItem));

            given(cartProductGateway.getProducts(List.of(existItemId, addItemId)))
                    .willReturn(addItemResult);
            //when
            CartResult cartResult = cartFacade.addItems(command);
            //then
            assertThat(cartResult.items()).hasSize(2);
            assertThat(cartResult.items())
                    .extracting("status", "productVariantId")
                    .containsExactlyInAnyOrder(
                            tuple(CartItemAvailability.NOT_FOR_SALE, existItemId),
                            tuple(CartItemAvailability.AVAILABLE, addItemId)
                    );
        }
        
        @Test
        @DisplayName("이미 장바구니에 담겨진 상품의 수량이 상품 재보다 많으면 LACK_OF_STOCK 상태 응답을 반환한다")
        void addItem_cartItemQuantityExceedStock() {
            //given
            //when
            //then
        }
        
        @Test
        @DisplayName("validator 검증이 실패한 경우 예외를 던지고 장바구니에 상품은 추가되지 않는다")
        void addItems_validation_fail() {
            //given
            AddCartItemsCommand command = createAddCommand(1L, 3);
            CartProductListResult productList = createProductList(1L, CartProductStatus.ON_SALE);
            given(cartProductGateway.getProducts(anyList())).willReturn(productList);
            willThrow(new BusinessException(CartErrorCode.CART_PRODUCT_STOCK_INSUFFICIENT))
                    .given(validator).validate(any(), any());
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_PRODUCT_STOCK_INSUFFICIENT);

            verify(cartCommandService, never()).addCartItems(any());
            verify(cartQueryService, never()).getCartItems(anyLong());
        }
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

    private CartProductListResult createProductList(Long productVariantId, CartProductStatus status) {
        CartProductResult product = Instancio.of(CartProductResult.class)
                .set(field("productVariantId"), productVariantId)
                .set(field("status"), status)
                .create();
        return Instancio.of(CartProductListResult.class)
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

package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;

import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.AddCartItemsResult;
import com.example.order_service.cart.application.dto.result.CartItemAvailability;
import com.example.order_service.cart.application.dto.result.UpdateCartItemQuantityResult;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.cart.application.dto.result.CartResult;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import com.example.order_service.common.exception.application.DefaultGatewayException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        @DisplayName("장바구니에 상품을 추가한 뒤 추가된 장바구니 상품 정보를 조회하여 반환한다")
        void addItems() {
            //given
            AddCartItemsCommand addCommand = createAddCommand(1L, 3);
            CartProductListResult productData = createProductList(1L, CartProductStatus.ON_SALE);
            CartItemData cartItemData = createCartItemData(1L, 3);

            given(cartProductGateway.getProducts(anyList())).willReturn(productData);
            doNothing().when(validator).validate(any(AddCartItemsCommand.class), any(CartProductListResult.class));
            doNothing().when(cartCommandService).addCartItems(any(AddCartItemsCommand.class));
            given(cartQueryService.getCartItems(anyLong(), anyList())).willReturn(List.of(cartItemData));
            //when
            AddCartItemsResult result = cartFacade.addItems(addCommand);
            //then

            assertThat(result.items())
                    .hasSize(1);

            assertThat(result.items())
                    .allSatisfy(item ->
                            assertThat(item.cartItemId()).isNotNull());

            assertThat(result.items())
                    .extracting("productVariantId", "quantity")
                    .containsExactly(
                            tuple(1L, 3)
                    );
        }

        @Test
        @DisplayName("상품 정보 조회중 예외가 발생하면 예외를 전파한다")
        void addItems_cartProductGateway_thrown_gatewayException() {
            //given
            AddCartItemsCommand addCommand = createAddCommand(1L, 3);
            willThrow(new DefaultGatewayException(CartErrorCode.CART_PRODUCT_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "알 수 없는 에러가 발생했습니다"))
                    .given(cartProductGateway).getProducts(anyList());
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(addCommand))
                    .isInstanceOf(DefaultGatewayException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_PRODUCT_SERVER_ERROR);
        }

        @Test
        @DisplayName("상품 검증중 예외가 발생하면 예외를 전파한다")
        void addItems_CartItemValidator_thrown_BusinessException() {
            //given
            AddCartItemsCommand addCommand = createAddCommand(1L, 3);
            CartProductListResult productData = createProductList(1L, CartProductStatus.ON_SALE);
            given(cartProductGateway.getProducts(anyList())).willReturn(productData);
            willThrow(new BusinessException(CartErrorCode.CART_PRODUCT_CANNOT_ADD))
                    .given(validator).validate(any(AddCartItemsCommand.class), any(CartProductListResult.class));
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(addCommand))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_PRODUCT_CANNOT_ADD);
        }
    }

    @Nested
    @DisplayName("장바구니 조회")
    class GetCartDetails {

        @Test
        @DisplayName("장바구니가 빈 경우 상품을 조회하지 않고 빈 결과를 반환한다")
        void getCartDetails_empty_cart() {
            //given
            Long userId = 1L;
            given(cartQueryService.getCartItems(anyLong())).willReturn(Collections.emptyList());
            //when
            CartResult result = cartFacade.getCartDetails(userId);
            //then
            assertThat(result.items()).isEmpty();
            verify(cartProductGateway, never()).getProducts(any());
        }

        @Test
        @DisplayName("장바구니 조회 시 상품 정보가 없는 항목도 함께 반환한다")
        void getCartDetails_missing_productData() {
            //given
            Long userId = 1L;
            Long productVariantId = 1L;
            int quantity = 2;
            CartItemData cartItemData = createCartItemData(productVariantId, quantity);

            CartProductListResult productData = Instancio.of(CartProductListResult.class)
                    .set(field("products"), Collections.emptyList())
                    .create();

            given(cartQueryService.getCartItems(anyLong())).willReturn(List.of(cartItemData));
            given(cartProductGateway.getProducts(anyList())).willReturn(productData);
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

            CartProductResult product1 = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item1.productVariantId())
                    .set(field("status"), CartProductStatus.ON_SALE)
                    .create();

            CartProductResult product2 = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item2.productVariantId())
                    .set(field("status"), CartProductStatus.STOP_SALE)
                    .create();

            CartProductListResult productData = Instancio.of(CartProductListResult.class)
                    .set(field("products"), List.of(product1, product2))
                    .create();

            given(cartQueryService.getCartItems(anyLong())).willReturn(List.of(item1, item2));
            given(cartProductGateway.getProducts(anyList())).willReturn(productData);
            //when
            CartResult result = cartFacade.getCartDetails(userId);
            //then
            assertThat(result.items()).hasSize(2);
            assertThat(result.items())
                    .extracting("productVariantId", "status", "quantity")
                    .containsExactlyInAnyOrder(
                            tuple(1L, CartItemAvailability.AVAILABLE, 3),
                            tuple(2L, CartItemAvailability.NOT_FOR_SALE, 3)
                    );
        }

        @Test
        @DisplayName("상품 조회중 예외가 발생하면 예외를 전파한다")
        void getCartDetails_gateway_thrown_gatewayException() {
            //given
            Long userId = 1L;
            CartItemData cartItemData = createCartItemData(1L, 3);

            given(cartQueryService.getCartItems(anyLong())).willReturn(List.of(cartItemData));
            willThrow(new DefaultGatewayException(CartErrorCode.CART_PRODUCT_SERVER_ERROR, "INTERNAL_ERROR", "알 수 없는 예외가 발생했습니다"))
                    .given(cartProductGateway).getProducts(anyList());
            //when
            //then
            assertThatThrownBy(() -> cartFacade.getCartDetails(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_PRODUCT_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("장바구니 항목 수량 변경")
    class UpdateCartItemQuantity {

        @Test
        @DisplayName("장바구니 항목의 수량을 변경한다")
        void updateCartItemQuantity() {
            //given
            Long cartItemId = 1L;
            Long productVariantId = 1L;
            int quantity = 3;
            UpdateCartItemQuantityCommand command = createUpdateQuantityCommand(cartItemId, quantity);
            CartItemData cartItemData = createCartItemData(productVariantId, quantity);
            doNothing().when(cartCommandService).updateCartItemQuantity(any());
            given(cartQueryService.getCartItem(anyLong())).willReturn(cartItemData);
            //when
            UpdateCartItemQuantityResult result = cartFacade.updateCartItemQuantity(command);
            //then
            assertThat(result.productVariantId()).isEqualTo(productVariantId);
            assertThat(result.quantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("수량 변경중 예외가 발생하면 예외를 전파한다")
        void updateCartItemQuantity_commandService_thrown_exception() {
            //given
            Long cartItemId = 1L;
            int quantity = 3;
            UpdateCartItemQuantityCommand command = createUpdateQuantityCommand(cartItemId, quantity);
            willThrow(new BusinessException(CartErrorCode.CART_ITEM_MINIMUM_ONE_REQUIRED))
                    .given(cartCommandService).updateCartItemQuantity(any(UpdateCartItemQuantityCommand.class));
            //when
            //then
            assertThatThrownBy(() -> cartFacade.updateCartItemQuantity(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_MINIMUM_ONE_REQUIRED);
        }
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

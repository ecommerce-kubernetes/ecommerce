package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.AddCartItemsResult;
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
import com.example.order_service.common.exception.application.DefaultGatewayException;
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
        @DisplayName("장바구니에 상품을 추가한 뒤 추가된 장바구니 상품 정보를 조회하여 반환한다")
        void addItems(){
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
        void addItems_cartProductGateway_thrown_gatewayException(){
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
        void addItems_CartItemValidator_thrown_BusinessException(){
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

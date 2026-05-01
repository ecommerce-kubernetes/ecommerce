package com.example.order_service.cart.application;

import com.example.order_service.cart.application.dto.command.CartCommand;
import com.example.order_service.cart.application.dto.result.CartProductResult;
import com.example.order_service.cart.application.dto.result.CartResult;
import com.example.order_service.cart.application.dto.result.ProductStatus;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.domain.service.CartService;
import com.example.order_service.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.CartErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static com.example.order_service.support.TestFixtureUtil.sample;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartAppServiceTest {

    @InjectMocks
    private CartAppService cartAppService;
    @Mock
    private CartProductGateway cartProductGateway;
    @Mock
    private CartService cartService;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {

        @Test
        @DisplayName("요청한 상품 중 판매중이 아닌 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_ProductNotOnSale() {
            //given
            CartCommand.AddItems command = sample(fixtureMonkey.giveMeBuilder(CartCommand.AddItems.class)
                    .size("items", 2));
            Long firstId = command.items().getFirst().productVariantId();
            Long secondId = command.items().get(1).productVariantId();
            CartProductResult.Info onSaleProduct = sample(fixtureMonkey.giveMeBuilder(CartProductResult.Info.class)
                    .set("productVariantId", firstId)
                    .set("status", ProductStatus.ON_SALE));
            CartProductResult.Info stopSaleProduct = sample(fixtureMonkey.giveMeBuilder(CartProductResult.Info.class)
                    .set("productVariantId", secondId)
                    .set("status", ProductStatus.STOP_SALE));

            given(cartProductGateway.getProducts(anyList()))
                    .willReturn(List.of(onSaleProduct, stopSaleProduct));
            //when
            //then
            assertThatThrownBy(() -> cartAppService.addItems(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }

        @Test
        @DisplayName("장바구니에 상품이 추가되면 상품 정보가 포함된 응답값을 반환한다")
        void addItem() {
            //given
            CartCommand.AddItems command = sample(fixtureMonkey.giveMeBuilder(CartCommand.AddItems.class)
                    .size("items", 2));
            Long firstId = command.items().get(0).productVariantId();
            int firstQuantity = command.items().getFirst().quantity();
            Long secondId = command.items().get(1).productVariantId();
            int secondQuantity = command.items().get(1).quantity();
            CartProductResult.Info firstProduct = sample(
                    fixtureMonkey.giveMeBuilder(CartProductResult.Info.class)
                            .set("productVariantId", firstId)
                            .set("status", ProductStatus.ON_SALE) // 통과 조건
            );
            CartProductResult.Info secondProduct = sample(
                    fixtureMonkey.giveMeBuilder(CartProductResult.Info.class)
                            .set("productVariantId", secondId)
                            .set("status", ProductStatus.ON_SALE)
            );
            CartItemDto firstDto = sample(
                    fixtureMonkey.giveMeBuilder(CartItemDto.class)
                            .set("productVariantId", firstId)
                            .set("quantity", firstQuantity)
            );
            CartItemDto secondDto = sample(
                    fixtureMonkey.giveMeBuilder(CartItemDto.class)
                            .set("productVariantId", secondId)
                            .set("quantity", secondQuantity)
            );

            given(cartProductGateway.getProducts(anyList()))
                    .willReturn(List.of(firstProduct, secondProduct));
            given(cartService.addItemToCart(any(CartCommand.AddItems.class)))
                    .willReturn(List.of(firstDto, secondDto));
            //when
            CartResult.Cart result = cartAppService.addItems(command);
            //then
            assertThat(result.items()).hasSize(2);
            assertThat(result.items())
                    .extracting("productVariantId", "quantity")
                    .containsExactlyInAnyOrder(
                            tuple(firstId, firstQuantity),
                            tuple(secondId, secondQuantity)
                    );
            verify(cartProductGateway, times(1)).getProducts(anyList());
            verify(cartService, times(1)).addItemToCart(command);
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
            CartResult.Cart result = cartAppService.getCartDetails(1L);
            //then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("장바구니 목록을 조회한다")
        void getCartDetails() {
            //given
            List<CartItemDto> cartItems = List.of(
                    sample(fixtureMonkey.giveMeBuilder(CartItemDto.class).set("productVariantId", 1L)),
                    sample(fixtureMonkey.giveMeBuilder(CartItemDto.class).set("productVariantId", 2L)),
                    sample(fixtureMonkey.giveMeBuilder(CartItemDto.class).set("productVariantId", 3L))
            );

            List<CartProductResult.Info> productInfos = List.of(
                    sample(fixtureMonkey.giveMeBuilder(CartProductResult.Info.class)
                            .set("productVariantId", 1L)
                            .set("status", ProductStatus.ON_SALE)),
                    sample(fixtureMonkey.giveMeBuilder(CartProductResult.Info.class)
                            .set("productVariantId", 2L)
                            .set("status", ProductStatus.STOP_SALE))
            );

            given(cartService.getCartItems(1L))
                    .willReturn(cartItems);
            given(cartProductGateway.getProducts(anyList()))
                    .willReturn(productInfos);
            //when
            CartResult.Cart result = cartAppService.getCartDetails(1L);
            //then
            assertThat(result.items()).hasSize(3)
                    .extracting(CartResult.CartItemResult::productVariantId, CartResult.CartItemResult::isAvailable)
                    .containsExactlyInAnyOrder(
                            tuple(1L, true),
                            tuple(2L, false),
                            tuple(3L, false));
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
            CartResult.Update result = cartAppService.updateCartItemQuantity(command);
            //then
            assertThat(result)
                    .extracting(CartResult.Update::id, CartResult.Update::quantity)
                    .containsExactly(1L, 3);
        }
    }

    @Nested
    @DisplayName("장바구니 상품 삭제")
    class Delete {

        @Test
        @DisplayName("장바구니에 담긴 상품을 삭제한다")
        void removeCartItem() {
            //given
            willDoNothing().given(cartService).deleteCartItems(anyLong(), anyList());
            //when
            cartAppService.removeCartItems(1L, List.of(1L, 2L));
            //then
            verify(cartService).deleteCartItems(1L, List.of(1L, 2L));
        }

        @Test
        @DisplayName("결제가 완료하면 주문한 상품을 장바구니에서 지운다")
        void removePurchasedItems() {
            //given
            Long userId = 1L;
            List<Long> productVariantIds = List.of(1L, 2L);
            //when
            cartAppService.removePurchasedItems(userId, productVariantIds);
            //then
            verify(cartService).deleteByProductVariantIds(userId, productVariantIds);
        }
    }
}

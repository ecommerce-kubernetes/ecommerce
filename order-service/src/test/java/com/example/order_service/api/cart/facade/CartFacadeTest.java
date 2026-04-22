package com.example.order_service.api.cart.facade;

import com.example.order_service.api.cart.domain.model.ProductStatus;
import com.example.order_service.api.cart.domain.service.CartProductService;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.facade.dto.command.CartCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartItemStatus;
import com.example.order_service.api.cart.facade.dto.result.CartResult;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import com.example.order_service.api.support.BaseTestSupport;
import com.example.order_service.api.support.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static com.example.order_service.api.support.fixture.cart.CartCommandFixture.anUpdateQuantityCommand;
import static com.example.order_service.api.support.fixture.cart.CartFixture.anCartItemDto;
import static com.example.order_service.api.support.fixture.cart.CartProductFixture.anCartProductInfo;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

public class CartFacadeTest extends BaseTestSupport {

    @InjectMocks
    private CartFacade cartFacade;
    @Mock
    private CartProductService cartProductService;
    @Mock
    private CartService cartService;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {

        @Test
        @DisplayName("요청한 상품 중 판매중이 아닌 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_ProductNotOnSale() {
            //given
            CartCommand.AddItems command = TestUtil.sample(fixtureMonkey.giveMeBuilder(CartCommand.AddItems.class)
                    .size("items", 2));
            Long firstId = command.items().getFirst().productVariantId();
            Long secondId = command.items().get(1).productVariantId();
            CartProductInfo onSaleProduct = TestUtil.sample(fixtureMonkey.giveMeBuilder(CartProductInfo.class)
                    .set("productVariantId", firstId)
                    .set("status", ProductStatus.ON_SALE));
            CartProductInfo stopSaleProduct = TestUtil.sample(fixtureMonkey.giveMeBuilder(CartProductInfo.class)
                    .set("productVariantId", secondId)
                    .set("status", ProductStatus.STOP_SALE));

            given(cartProductService.getProductInfos(anyList()))
                    .willReturn(List.of(onSaleProduct, stopSaleProduct));
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }

        @Test
        @DisplayName("장바구니에 상품이 추가되면 상품 정보가 포함된 응답값을 반환한다")
        void addItem(){
            //given
            CartCommand.AddItems command = TestUtil.sample(fixtureMonkey.giveMeBuilder(CartCommand.AddItems.class)
                    .size("items", 2));
            Long firstId = command.items().get(0).productVariantId();
            int firstQuantity = command.items().getFirst().quantity();
            Long secondId = command.items().get(1).productVariantId();
            int secondQuantity = command.items().get(1).quantity();
            CartProductInfo firstProduct = TestUtil.sample(
                    fixtureMonkey.giveMeBuilder(CartProductInfo.class)
                            .set("productVariantId", firstId)
                            .set("status", ProductStatus.ON_SALE) // 통과 조건
            );
            CartProductInfo secondProduct = TestUtil.sample(
                    fixtureMonkey.giveMeBuilder(CartProductInfo.class)
                            .set("productVariantId", secondId)
                            .set("status", ProductStatus.ON_SALE)
            );
            CartItemDto firstDto = TestUtil.sample(
                    fixtureMonkey.giveMeBuilder(CartItemDto.class)
                            .set("productVariantId", firstId)
                            .set("quantity", firstQuantity)
            );
            CartItemDto secondDto = TestUtil.sample(
                    fixtureMonkey.giveMeBuilder(CartItemDto.class)
                            .set("productVariantId", secondId)
                            .set("quantity", secondQuantity)
            );

            given(cartProductService.getProductInfos(anyList()))
                    .willReturn(List.of(firstProduct, secondProduct));
            given(cartService.addItemToCart(any(CartCommand.AddItems.class)))
                    .willReturn(List.of(firstDto, secondDto));
            //when
            CartResult.Cart result = cartFacade.addItems(command);
            //then
            assertThat(result.items()).hasSize(2);
            assertThat(result.items())
                    .extracting("productVariantId", "quantity")
                    .containsExactlyInAnyOrder(
                            tuple(firstId, firstQuantity),
                            tuple(secondId, secondQuantity)
                    );
            verify(cartProductService, times(1)).getProductInfos(anyList());
            verify(cartService, times(1)).addItemToCart(command);
        }
    }

    @Nested
    @DisplayName("장바구니 목록 조회")
    class GetCartDetails {

        @Test
        @DisplayName("장바구니에 담긴 상품이 없는 경우 빈 리스트를 반환한다")
        void getCartDetails_empty_cart(){
            //given
            given(cartService.getCartItems(anyLong()))
                    .willReturn(List.of());
            //when
            CartResult.Cart result = cartFacade.getCartDetails(1L);
            //then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("장바구니 목록을 조회한다")
        void getCartDetails(){
            //given
            List<CartItemDto> cartItems = List.of(
                    TestUtil.sample(fixtureMonkey.giveMeBuilder(CartItemDto.class).set("productVariantId", 1L)),
                    TestUtil.sample(fixtureMonkey.giveMeBuilder(CartItemDto.class).set("productVariantId", 2L)),
                    TestUtil.sample(fixtureMonkey.giveMeBuilder(CartItemDto.class).set("productVariantId", 3L))
            );

            List<CartProductInfo> productInfos = List.of(
                    TestUtil.sample(fixtureMonkey.giveMeBuilder(CartProductInfo.class)
                            .set("productVariantId", 1L)
                            .set("status", ProductStatus.ON_SALE)),
                    TestUtil.sample(fixtureMonkey.giveMeBuilder(CartProductInfo.class)
                            .set("productVariantId", 2L)
                            .set("status", ProductStatus.STOP_SALE))
            );

            given(cartService.getCartItems(1L))
                    .willReturn(cartItems);
            given(cartProductService.getProductInfos(anyList()))
                    .willReturn(productInfos);
            //when
            CartResult.Cart result = cartFacade.getCartDetails(1L);
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
        @DisplayName("장바구니에 상품 수량을 수정하고 수정된 상품 정보가 포함된 응답을 반환한다")
        void updateCartItemQuantity() {
            //given
            UpdateQuantityCommand command = anUpdateQuantityCommand().quantity(2).build();
            CartItemDto cartItem = anCartItemDto().build();
            CartItemDto updatedCartItem = anCartItemDto().quantity(2).build();
            CartProductInfo product = anCartProductInfo().build();
            given(cartService.getCartItem(anyLong(), anyLong())).willReturn(cartItem);
            given(cartProductService.getProductInfo(anyLong())).willReturn(product);
            given(cartService.updateQuantity(anyLong(), anyLong(), anyInt())).willReturn(updatedCartItem);
            //when
            CartItemResponse result = cartFacade.updateCartItemQuantity(command);
            //then
            assertThat(result)
                    .extracting(CartItemResponse::getId, CartItemResponse::getQuantity, CartItemResponse::getLineTotal, CartItemResponse::getStatus)
                    .containsExactly(1L, 2, 18000L, CartItemStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("장바구니 상품 삭제")
    class Delete {

        @Test
        @DisplayName("장바구니에 담긴 상품을 삭제한다")
        void removeCartItem(){
            //given
            willDoNothing().given(cartService).deleteCartItem(anyLong(), anyLong());
            //when
            cartFacade.removeCartItem(1L, 1L);
            //then
            verify(cartService).deleteCartItem(1L, 1L);
        }

        @Test
        @DisplayName("장바구니를 비운다")
        void clearCart(){
            //given
            willDoNothing().given(cartService).clearCart(anyLong());
            //when
            cartFacade.clearCart(1L);
            //then
            verify(cartService)
                    .clearCart(1L);
        }

        @Test
        @DisplayName("결제가 완료하면 주문한 상품을 장바구니에서 지운다")
        void removePurchasedItems(){
            //given
            Long userId = 1L;
            List<Long> productVariantIds = List.of(1L, 2L);
            //when
            cartFacade.removePurchasedItems(userId, productVariantIds);
            //then
            verify(cartService).deleteByProductVariantIds(userId, productVariantIds);
        }
    }
}

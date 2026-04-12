package com.example.order_service.api.cart.facade;

import com.example.order_service.api.cart.domain.model.ProductStatus;
import com.example.order_service.api.cart.domain.service.CartProductService;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.facade.dto.command.CartCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.AllCartResponse;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartItemStatus;
import com.example.order_service.api.cart.facade.dto.result.CartResult;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import com.example.order_service.api.support.BaseTestSupport;
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
            CartCommand.AddItems command = sample(fixtureMonkey.giveMeBuilder(CartCommand.AddItems.class)
                    .size("items", 2));
            Long firstId = command.items().getFirst().productVariantId();
            Long secondId = command.items().get(1).productVariantId();
            CartProductInfo onSaleProduct = sample(fixtureMonkey.giveMeBuilder(CartProductInfo.class)
                    .set("productVariantId", firstId)
                    .set("status", ProductStatus.ON_SALE));
            CartProductInfo stopSaleProduct = sample(fixtureMonkey.giveMeBuilder(CartProductInfo.class)
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
            CartCommand.AddItems command = sample(fixtureMonkey.giveMeBuilder(CartCommand.AddItems.class)
                    .size("items", 2));
            Long firstId = command.items().get(0).productVariantId();
            int firstQuantity = command.items().getFirst().quantity();
            Long secondId = command.items().get(1).productVariantId();
            int secondQuantity = command.items().get(1).quantity();
            CartProductInfo firstProduct = sample(
                    fixtureMonkey.giveMeBuilder(CartProductInfo.class)
                            .set("productVariantId", firstId)
                            .set("status", ProductStatus.ON_SALE) // 통과 조건
            );
            CartProductInfo secondProduct = sample(
                    fixtureMonkey.giveMeBuilder(CartProductInfo.class)
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

            given(cartProductService.getProductInfos(anyList()))
                    .willReturn(List.of(firstProduct, secondProduct));
            given(cartService.addItemToCart(any(CartCommand.AddItems.class)))
                    .willReturn(List.of(firstDto, secondDto));
            //when
            CartResult.CartAddResult result = cartFacade.addItems(command);
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
            AllCartResponse result = cartFacade.getCartDetails(1L);
            //then
            assertThat(result.getCartTotalPrice()).isEqualTo(0L);
            assertThat(result.getCartItems()).isEmpty();
        }

        @Test
        @DisplayName("장바구니 목록을 조회한다")
        void getCartDetails(){
            //given
            CartItemDto cartItem1 = anCartItemDto().id(1L).productVariantId(1L).quantity(3).build();
            CartItemDto cartItem2 = anCartItemDto().id(2L).productVariantId(2L).quantity(5).build();
            // 찾을 수 없는 상품
            CartItemDto cartItem3 = anCartItemDto().id(3L).productVariantId(3L).quantity(2).build();
            // 판매 중지된 상품
            CartItemDto cartItem4 = anCartItemDto().id(4L).productVariantId(4L).quantity(1).build();
            // 준비중인 상품
            CartItemDto cartItem5 = anCartItemDto().id(5L).productVariantId(5L).quantity(1).build();
            // 삭제된 상품
            CartItemDto cartItem6 = anCartItemDto().id(6L).productVariantId(6L).quantity(1).build();

            CartProductInfo product1 = anCartProductInfo().productId(1L).productVariantId(1L).status(ProductStatus.ON_SALE).build();
            CartProductInfo product2 = anCartProductInfo().productId(2L).productVariantId(2L).status(ProductStatus.ON_SALE).build();
            CartProductInfo product4 = anCartProductInfo().productId(4L).productVariantId(4L).status(ProductStatus.STOP_SALE).build();
            CartProductInfo product5 = anCartProductInfo().productId(5L).productVariantId(5L).status(ProductStatus.PREPARING).build();
            CartProductInfo product6 = anCartProductInfo().productId(6L).productVariantId(6L).status(ProductStatus.DELETED).build();

            given(cartService.getCartItems(1L))
                    .willReturn(List.of(cartItem1, cartItem2, cartItem3, cartItem4, cartItem5, cartItem6));
            given(cartProductService.getProductInfos(anyList()))
                    .willReturn(List.of(product1, product2, product4, product5, product6));
            //when
            AllCartResponse result = cartFacade.getCartDetails(1L);
            //then
            assertThat(result.getCartTotalPrice()).isEqualTo(72000L);
            assertThat(result.getCartItems()).hasSize(6)
                    .extracting(
                            CartItemResponse::getProductId, CartItemResponse::getProductVariantId, CartItemResponse::getQuantity, CartItemResponse::getLineTotal,
                            CartItemResponse::isAvailable)
                    .containsExactlyInAnyOrder(
                            tuple(1L, 1L, 3, 27000L, true),
                            tuple(2L, 2L, 5, 45000L, true),
                            tuple(null, 3L, 2, 0L, false),
                            tuple(4L, 4L, 1, 9000L, false),
                            tuple(5L, 5L, 1, 9000L, false),
                            tuple(6L, 6L, 1, 9000L, false)
                    );
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

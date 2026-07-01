package com.example.order_service.cart.application;

import com.example.order_service.cart.application.service.dto.command.CartCommand;
import com.example.order_service.cart.application.external.dto.result.CartProductResult;
import com.example.order_service.cart.application.service.dto.result.CartResult;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.service.CartFacade;
import com.example.order_service.cart.domain.model.vo.ProductStatus;
import com.example.order_service.cart.domain.service.CartService;
import com.example.order_service.cart.domain.service.dto.result.CartItemDto;
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
    private CartService cartService;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {

        @Test
        @DisplayName("요청한 상품 중 장바구니에 추가할 수 없는 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_ProductNotOnSale() {
            //given
            CartCommand.AddItems command = Instancio.of(CartCommand.AddItems.class)
                    .generate(field(CartCommand.AddItems::items),
                            gen -> gen.collection().size(2))
                    .create();
            Long firstId = command.items().getFirst().productVariantId();
            Long secondId = command.items().get(1).productVariantId();
            CartProductResult.Info onSaleProduct = Instancio.of(CartProductResult.Info.class)
                    .set(field("productVariantId"), firstId)
                    .set(field("status"), ProductStatus.AVAILABLE)
                    .create();
            CartProductResult.Info stopSaleProduct = Instancio.of(CartProductResult.Info.class)
                    .set(field("productVariantId"), secondId)
                    .set(field("status"), ProductStatus.UNAVAILABLE)
                    .create();
            given(cartProductGateway.getProducts(anyList()))
                    .willReturn(List.of(onSaleProduct, stopSaleProduct));
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_PRODUCT_CANNOT_ADD);
        }

        @Test
        @DisplayName("상품 정보에 누락된 상품이 있는 경우 예외가 발생한다")
        void addItem_fail_product_not_found() {
            //given
            CartCommand.AddItems command = Instancio.of(CartCommand.AddItems.class)
                    .generate(field(CartCommand.AddItems::items),
                            gen -> gen.collection().size(2))
                    .create();
            Long firstId = command.items().getFirst().productVariantId();
            CartProductResult.Info onSaleProduct = Instancio.of(CartProductResult.Info.class)
                    .set(field("productVariantId"), firstId)
                    .set(field("status"), ProductStatus.AVAILABLE)
                    .create();
            given(cartProductGateway.getProducts(anyList()))
                    .willReturn(List.of(onSaleProduct));
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItems(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("장바구니에 상품이 추가되면 상품 정보가 포함된 응답값을 반환한다")
        void addItem() {
            //given
            CartCommand.AddItems command = Instancio.of(CartCommand.AddItems.class)
                    .generate(field(CartCommand.AddItems::items),
                            gen -> gen.collection().size(2))
                    .create();
            Long firstId = command.items().get(0).productVariantId();
            int firstQuantity = command.items().getFirst().quantity();
            Long secondId = command.items().get(1).productVariantId();
            int secondQuantity = command.items().get(1).quantity();
            CartProductResult.Info firstProduct = Instancio.of(CartProductResult.Info.class)
                    .set(field("productVariantId"), firstId)
                    .set(field("status"), ProductStatus.AVAILABLE)
                    .create();
            CartProductResult.Info secondProduct = Instancio.of(CartProductResult.Info.class)
                    .set(field("productVariantId"), secondId)
                    .set(field("status"), ProductStatus.AVAILABLE)
                    .create();
            CartItemDto firstDto = Instancio.of(CartItemDto.class)
                    .set(field("productVariantId"), firstId)
                    .set(field("quantity"), firstQuantity)
                    .create();
            CartItemDto secondDto = Instancio.of(CartItemDto.class)
                    .set(field("productVariantId"), secondId)
                    .set(field("quantity"), secondQuantity)
                    .create();

            given(cartProductGateway.getProducts(anyList()))
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
            CartResult.Cart result = cartFacade.getCartDetails(1L);
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

            List<CartProductResult.Info> productInfos = List.of(
                    Instancio.of(CartProductResult.Info.class)
                            .set(field("productVariantId"), 1L)
                            .set(field("status"), ProductStatus.AVAILABLE)
                            .create(),
                    Instancio.of(CartProductResult.Info.class)
                            .set(field("productVariantId"), 2L)
                            .set(field("status"), ProductStatus.UNAVAILABLE)
                            .create());

            given(cartService.getCartItems(1L))
                    .willReturn(cartItems);
            given(cartProductGateway.getProducts(anyList()))
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
            CartResult.Cart result = cartFacade.updateCartItemQuantity(command);
            //then
//            assertThat(result)
//                    .extracting(CartResult.Update::id, CartResult.Update::quantity)
//                    .containsExactly(1L, 3);
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
            cartFacade.removeCartItems(1L, List.of(1L, 2L));
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
            cartFacade.removePurchasedItems(userId, productVariantIds);
            //then
            verify(cartService).deleteByProductVariantIds(userId, productVariantIds);
        }
    }
}

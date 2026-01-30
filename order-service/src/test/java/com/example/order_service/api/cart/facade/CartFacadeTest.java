package com.example.order_service.api.cart.facade;

import com.example.order_service.api.cart.domain.service.CartProductService;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartItemStatus;
import com.example.order_service.api.cart.facade.dto.result.CartResponse;
import com.example.order_service.api.cart.infrastructure.client.CartProductAdaptor;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.cart.domain.model.ProductStatus;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.api.support.fixture.cart.CartCommandFixture.anAddCartItemCommand;
import static com.example.order_service.api.support.fixture.cart.CartCommandFixture.anUpdateQuantityCommand;
import static com.example.order_service.api.support.fixture.cart.CartFixture.anCartItemDto;
import static com.example.order_service.api.support.fixture.cart.CartProductFixture.anCartProductInfo;
import static com.example.order_service.api.support.fixture.cart.CartResponseFixture.anCartItemResponse;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartFacadeTest {

    @InjectMocks
    private CartFacade cartFacade;
    @Mock
    private CartProductAdaptor cartProductAdaptor;
    @Mock
    private CartProductService cartProductService;
    @Mock
    private CartService cartService;


    private CartProductResponse createProductResponse(Long productId, Long productVariantId, ProductStatus status) {
        return CartProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .status("ON_SALE")
                .productName("상품 이름")
                .unitPrice(
                        CartProductResponse.UnitPrice.builder()
                                .originalPrice(10000L)
                                .discountRate(10)
                                .discountAmount(1000L)
                                .discountedPrice(9000L)
                                .build())
                .thumbnailUrl("http://thumbnail.jpg")
                .itemOptions(List.of())
                .build();
    }

    private CartItemDto createCartItemDto(Long id, Long productVariantId, Integer quantity) {
        return CartItemDto.builder()
                .id(id)
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    @Nested
    @DisplayName("장바구니 추가")
    class AddItem {

        @Test
        @DisplayName("장바구니에 상품이 추가되면 상품 정보가 포함된 응답값을 반환한다")
        void addItem(){
            //given
            AddCartItemCommand command = anAddCartItemCommand().build();
            CartProductInfo product = anCartProductInfo().build();
            CartItemDto cartItemDto = anCartItemDto().build();
            CartItemResponse expectedResult = anCartItemResponse().build();
            given(cartProductService.getProductInfo(anyLong()))
                    .willReturn(product);
            given(cartService.addItemToCart(anyLong(), anyLong(), anyInt()))
                    .willReturn(cartItemDto);
            //when
            CartItemResponse result = cartFacade.addItem(command);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult);
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
            CartResponse result = cartFacade.getCartDetails(1L);
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
            CartResponse result = cartFacade.getCartDetails(1L);
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
            verify(cartService, times(1)).deleteCartItem(1L, 1L);
        }

        @Test
        @DisplayName("장바구니를 비운다")
        void clearCart(){
            //given
            willDoNothing().given(cartService).clearCart(anyLong());
            //when
            cartFacade.clearCart(1L);
            //then
            verify(cartService, times(1))
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
            verify(cartService, times(1)).deleteByProductVariantIds(userId, productVariantIds);
        }
    }
}

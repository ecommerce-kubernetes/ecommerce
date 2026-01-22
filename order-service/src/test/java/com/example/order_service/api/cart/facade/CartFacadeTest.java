package com.example.order_service.api.cart.facade;

import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartItemStatus;
import com.example.order_service.api.cart.facade.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.cart.infrastructure.client.dto.ProductStatus;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartFacadeTest {

    @InjectMocks
    private CartFacade cartFacade;
    @Mock
    private CartProductClientService cartProductClientService;
    @Mock
    private CartService cartService;

    private AddCartItemCommand mockAddCartItemCommand(Long userId, Long variantId, int quantity){
        return AddCartItemCommand.builder()
                .userId(userId)
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    private UpdateQuantityCommand mockUpdateQuantityCommand(Long userId, Long cartItemId, Integer quantity) {
        return UpdateQuantityCommand.builder()
                .userId(userId)
                .cartItemId(cartItemId)
                .quantity(quantity)
                .build();
    }

    private CartProductResponse createProductResponse(Long productId, Long productVariantId, ProductStatus status) {
        return CartProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .status(status)
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
            AddCartItemCommand command = mockAddCartItemCommand(1L, 1L, 3);
            CartProductResponse product = createProductResponse(1L, 1L, ProductStatus.ON_SALE);
            CartItemDto cartItem = createCartItemDto(1L, 1L, 3);
            given(cartProductClientService.getProduct(anyLong()))
                    .willReturn(product);
            given(cartService.addItemToCart(anyLong(), anyLong(), anyInt()))
                    .willReturn(cartItem);
            //when
            CartItemResponse result = cartFacade.addItem(command);
            //then
            assertThat(result.getId()).isNotNull();
            assertThat(result)
                    .extracting(
                            CartItemResponse::getProductId, CartItemResponse::getProductVariantId, CartItemResponse::getQuantity, CartItemResponse::getLineTotal,
                            CartItemResponse::isAvailable)
                    .containsExactly(1L, 1L, 3, 9000L * 3, true);
        }

        @Test
        @DisplayName("판매중이 아닌 상품은 장바구니에 추가할 수 없다")
        void addItem_cannot_be_added_product(){
            //given
            AddCartItemCommand command = mockAddCartItemCommand(1L, 1L, 3);
            CartProductResponse product = createProductResponse(1L, 1L, ProductStatus.DELETED);
            given(cartProductClientService.getProduct(anyLong()))
                    .willReturn(product);
            //when
            //then
            assertThatThrownBy(() -> cartFacade.addItem(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.PRODUCT_NOT_ON_SALE);
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
            CartItemDto cartItem1 = createCartItemDto(1L, 1L, 3);
            CartItemDto cartItem2 = createCartItemDto(2L, 2L, 5);
            // 찾을 수 없는 상품
            CartItemDto cartItem3 = createCartItemDto(3L, 3L, 2);
            // 판매 중지된 상품
            CartItemDto cartItem4 = createCartItemDto(4L, 4L, 1);
            // 준비중인 상품
            CartItemDto cartItem5 = createCartItemDto(5L, 5L, 1);
            // 삭제된 상품
            CartItemDto cartItem6 = createCartItemDto(6L, 6L, 1);
            CartProductResponse product1 = createProductResponse(1L, 1L, ProductStatus.ON_SALE);
            CartProductResponse product2 = createProductResponse(2L, 2L, ProductStatus.ON_SALE);
            CartProductResponse product4 = createProductResponse(4L, 4L, ProductStatus.STOP_SALE);
            CartProductResponse product5 = createProductResponse(5L, 5L, ProductStatus.PREPARING);
            CartProductResponse product6 = createProductResponse(6L, 6L, ProductStatus.DELETED);

            given(cartService.getCartItems(1L))
                    .willReturn(List.of(cartItem1, cartItem2, cartItem3, cartItem4, cartItem5, cartItem6));
            given(cartProductClientService.getProducts(anyList()))
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
            UpdateQuantityCommand command = mockUpdateQuantityCommand(1L, 1L, 3);
            CartItemDto cartItem = createCartItemDto(1L, 1L, 1);
            CartItemDto updatedCartItem = createCartItemDto(1L, 1L, 3);
            given(cartService.getCartItem(anyLong(), anyLong())).willReturn(cartItem);
            CartProductResponse product = createProductResponse(1L, 1L, ProductStatus.ON_SALE);
            given(cartProductClientService.getProduct(anyLong())).willReturn(product);
            given(cartService.updateQuantity(anyLong(), anyLong(), anyInt())).willReturn(updatedCartItem);
            //when
            CartItemResponse result = cartFacade.updateCartItemQuantity(command);
            //then
            assertThat(result)
                    .extracting(CartItemResponse::getId, CartItemResponse::getQuantity, CartItemResponse::getLineTotal, CartItemResponse::getStatus)
                            .containsExactly(1L, 3, 27000L, CartItemStatus.AVAILABLE);
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

    }

    @Test
    @DisplayName("결제가 완료하면 주문한 상품을 장바구니에서 지운다")
    void cleanUpCartAfterOrder(){
        //given
        Long userId = 1L;
        List<Long> productVariantIds = List.of(1L, 2L);
        //when
        cartFacade.cleanUpCartAfterOrder(userId, productVariantIds);
        //then
        verify(cartService, times(1)).deleteByProductVariantIds(userId, productVariantIds);
    }
}

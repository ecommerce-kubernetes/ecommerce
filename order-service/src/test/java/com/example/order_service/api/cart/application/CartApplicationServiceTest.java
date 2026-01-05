package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.service.CartDomainService;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartApplicationServiceTest {

    @InjectMocks
    private CartApplicationService cartApplicationService;
    @Mock
    private CartProductClientService cartProductClientService;
    @Mock
    private CartDomainService cartDomainService;

    @Test
    @DisplayName("장바구니에 상품이 추가되면 상품 정보가 포함된 응답값을 반환한다")
    void addItem(){
        //given
        AddCartItemDto addDto = mockAddCartItemDto(1L, 1L, 3);
        CartProductResponse product = createProductResponse(1L, 1L, "상품1", 10000L,
                10, "http://thumbnail.jpg",
                List.of(CartProductResponse.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        CartItemDto cartItem = mockCartItemDto(1L, 1L, 3);
        given(cartProductClientService.getProduct(anyLong()))
                .willReturn(product);
        given(cartDomainService.addItemToCart(anyLong(), anyLong(), anyInt()))
                .willReturn(cartItem);
        //when
        CartItemResponse result = cartApplicationService.addItem(addDto);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(
                        CartItemResponse::getProductId, CartItemResponse::getProductVariantId, CartItemResponse::getProductName,
                        CartItemResponse::getThumbnailUrl, CartItemResponse::getQuantity, CartItemResponse::getLineTotal,
                        CartItemResponse::isAvailable)
                        .containsExactly(1L, 1L, "상품1", "http://thumbnail.jpg", 3, 9000L * 3, true);
        assertThat(result.getPrice())
                .satisfies(price -> {
                    assertThat(price.getOriginalPrice()).isEqualTo(10000L);
                    assertThat(price.getDiscountRate()).isEqualTo(10);
                    assertThat(price.getDiscountAmount()).isEqualTo(1000L);
                    assertThat(price.getDiscountedPrice()).isEqualTo(9000L);
                });

        assertThat(result.getOptions())
                .hasSize(1)
                .extracting(CartItemResponse.CartItemOption::getOptionTypeName,
                        CartItemResponse.CartItemOption::getOptionValueName)
                .containsExactly(
                        tuple("사이즈", "XL")
                );
    }

    @Test
    @DisplayName("장바구니에 담긴 상품 목록을 조회해 상품정보가 포함된 응답값을 반환한다")
    void getCartDetails(){
        //given
        CartItemDto item1 = mockCartItemDto(1L, 1L, 3);
        CartItemDto item2 = mockCartItemDto(2L, 2L, 2);
        CartProductResponse product1 = createProductResponse(1L, 1L, "상품1",
                3000L, 10, "http://thumbnail1.jpg",
                List.of(CartProductResponse.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()));
        CartProductResponse product2 = createProductResponse(2L, 2L, "상품2",
                5000L, 10, "http://thumbnail2.jpg",
                List.of(CartProductResponse.ItemOption.builder().optionTypeName("용량").optionValueName("256GB").build()));

        given(cartDomainService.getCartItems(anyLong()))
                .willReturn(List.of(item1, item2));
        given(cartProductClientService.getProducts(anyList()))
                .willReturn(List.of(product1, product2));
        //when
        CartResponse response = cartApplicationService.getCartDetails(1L);
        //then
        assertThat(response.getCartItems())
                .hasSize(2)
                .extracting("productId", "productName", "lineTotal", "price.originalPrice")
                .containsExactlyInAnyOrder(
                        tuple(1L, "상품1", 8100L, 3000L),
                        tuple(2L, "상품2", 9000L, 5000L)
                );
        assertThat(response.getCartItems())
                .flatExtracting(CartItemResponse::getOptions)
                .extracting("optionTypeName", "optionValueName")
                .containsExactlyInAnyOrder(
                        tuple("사이즈", "XL"),
                        tuple("용량", "256GB")
                );

        assertThat(response.getCartTotalPrice())
                .isEqualTo(17100L);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 조회할때 장바구니에 상품이 없는 경우 빈 응답을 반환한다")
    void getCartDetails_When_Empty_CartItems(){
        //given
        given(cartDomainService.getCartItems(anyLong()))
                .willReturn(List.of());
        //when
        CartResponse response = cartApplicationService.getCartDetails(1L);
        //then
        assertThat(response.getCartItems()).isEmpty();
        assertThat(response.getCartTotalPrice()).isEqualTo(0L);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 조회할때 해당 상품이 상품서비스에서 찾을 수 없는 경우 해당 상품은 실패 응답으로 채워 반환한다")
    void getCartDetails_When_ProductInfoEmpty(){
        //given
        CartItemDto item1 = mockCartItemDto(1L, 1L, 3);
        CartItemDto item2 = mockCartItemDto(2L, 2L, 2);

        CartProductResponse product1 = createProductResponse(1L, 1L, "상품1",
                3000L, 10, "http://thumbnail1.jpg",
                List.of(CartProductResponse.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()));

        given(cartDomainService.getCartItems(1L))
                .willReturn(List.of(item1, item2));

        given(cartProductClientService.getProducts(anyList()))
                .willReturn(List.of(product1));
        //when
        CartResponse cartDetails = cartApplicationService.getCartDetails(1L);
        //then
        assertThat(cartDetails.getCartItems())
                .hasSize(2)
                .extracting(
                        "productId",
                        "productName",
                        "lineTotal",
                        "available",
                        "price.originalPrice",
                        "price.discountedPrice"
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, "상품1", 8100L, true, 3000L, 2700L),

                        tuple(null, "정보를 불러올 수 없거나 판매 중지된 상품입니다", 0L, false, null, null)
                );
        assertThat(cartDetails.getCartTotalPrice()).isEqualTo(8100L);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 삭제한다")
    void removeCartItem() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        willDoNothing().given(cartDomainService).deleteCartItem(anyLong(), anyLong());
        //when
        cartApplicationService.removeCartItem(userId, cartItemId);
        //then
        verify(cartDomainService, times(1)).deleteCartItem(userId, cartItemId);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 모두 삭제")
    void clearCart() {
        //given
        willDoNothing().given(cartDomainService).clearCart(anyLong());
        //when
        cartApplicationService.clearCart(1L);
        //then
        verify(cartDomainService, times(1))
                .clearCart(1L);
    }
    
    @Test
    @DisplayName("장바구니에 상품 수량을 수정하고 수정된 상품 정보가 포함된 응답을 반환한다")
    void updateCartItemQuantity() {
        //given
        UpdateQuantityDto dto = UpdateQuantityDto.builder()
                .userId(1L)
                .cartItemId(1L)
                .quantity(3)
                .build();
        given(cartDomainService.getCartItem(anyLong()))
                .willReturn(
                        CartItemDto.builder()
                                .id(1L)
                                .productVariantId(1L)
                                .quantity(1)
                                .build()
                );
        CartProductResponse product = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail.jpg", List.of(
                        CartProductResponse.ItemOption.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                ));

        given(cartProductClientService.getProduct(anyLong()))
                .willReturn(product);

        given(cartDomainService.updateQuantity(anyLong(), anyInt()))
                .willReturn(
                        CartItemDto.builder()
                                .id(1L)
                                .productVariantId(1L)
                                .quantity(3)
                                .build()
                );
        //when
        CartItemResponse result = cartApplicationService.updateCartItemQuantity(dto);
        //then

        assertThat(result.getId()).isNotNull();

        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductVariantId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("상품1");
        assertThat(result.getThumbnailUrl()).isEqualTo("http://thumbnail.jpg");
        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getLineTotal()).isEqualTo(8100L);
        assertThat(result.isAvailable()).isTrue();

        assertThat(result.getPrice())
                .satisfies(price -> {
                    assertThat(price.getOriginalPrice()).isEqualTo(3000L);
                    assertThat(price.getDiscountRate()).isEqualTo(10);
                    assertThat(price.getDiscountAmount()).isEqualTo(300L);
                    assertThat(price.getDiscountedPrice()).isEqualTo(2700L);
                });

        assertThat(result.getOptions())
                .hasSize(1)
                .extracting("optionTypeName", "optionValueName")
                .containsExactly(
                        tuple("사이즈", "XL")
                );
    }

    @Test
    @DisplayName("결제가 완료하면 주문한 상품을 장바구니에서 지운다")
    void cleanUpCartAfterOrder(){
        //given
        Long userId = 1L;
        List<Long> productVariantIds = List.of(1L, 2L);
        //when
        cartApplicationService.cleanUpCartAfterOrder(userId, productVariantIds);
        //then
        verify(cartDomainService, times(1)).deleteByProductVariantIds(userId, productVariantIds);
    }

    private CartProductResponse createProductResponse(Long productId, Long productVariantId,
                                                      String productName, Long originalPrice, int discountRate,
                                                      String thumbnail, List<CartProductResponse.ItemOption> options){
        long discountAmount = originalPrice * discountRate / 100;
        return CartProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .unitPrice(
                        CartProductResponse.UnitPrice.builder()
                                .originalPrice(originalPrice)
                                .discountRate(discountRate)
                                .discountAmount(discountAmount)
                                .discountedPrice(originalPrice - discountAmount)
                                .build())
                .thumbnailUrl(thumbnail)
                .itemOptions(options)
                .build();
    }

    private AddCartItemDto mockAddCartItemDto(Long userId, Long variantId, int quantity){
        return AddCartItemDto.builder()
                .userId(userId)
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    private CartItemDto mockCartItemDto(Long cartItemId, Long variantId, int quantity){
        return CartItemDto.builder()
                .id(cartItemId)
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }
}

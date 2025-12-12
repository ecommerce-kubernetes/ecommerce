package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.service.CartDomainService;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
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
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        AddCartItemDto command = AddCartItemDto.builder()
                .userPrincipal(userPrincipal)
                .productVariantId(1L)
                .quantity(3)
                .build();

        CartProductResponse product = createProductResponse(1L, 1L, "상품1", 3000L,
                10, "http://thumbnail.jpg",
                List.of(CartProductResponse.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()));

        CartItemDto cartItem = CartItemDto.builder()
                .id(1L)
                .productVariantId(1L)
                .quantity(3)
                .build();

        given(cartProductClientService.getProduct(anyLong()))
                .willReturn(product);

        given(cartDomainService.addItemToCart(anyLong(), anyLong(), anyInt()))
                .willReturn(cartItem);
        //when
        CartItemResponse result = cartApplicationService.addItem(command);
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
    @DisplayName("장바구니에 담긴 상품 목록을 조회해 상품정보가 포함된 응답값을 반환한다")
    void getCartDetails(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CartItemDto item1 = CartItemDto.builder()
                .id(1L)
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItemDto item2 = CartItemDto.builder()
                .id(2L)
                .productVariantId(2L)
                .quantity(2)
                .build();

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
        CartResponse response = cartApplicationService.getCartDetails(userPrincipal);
        //then
        assertThat(response.getCartItems()).hasSize(2)
                        .satisfiesExactlyInAnyOrder(
                                itemResponse1 -> {
                                    assertThat(itemResponse1.getId()).isNotNull();
                                    assertThat(itemResponse1.getProductId()).isEqualTo(1L);
                                    assertThat(itemResponse1.getProductName()).isEqualTo("상품1");
                                    assertThat(itemResponse1.getThumbnailUrl()).isEqualTo("http://thumbnail1.jpg");
                                    assertThat(itemResponse1.getQuantity()).isEqualTo(3);
                                    assertThat(itemResponse1.getLineTotal()).isEqualTo(8100);
                                    assertThat(itemResponse1.isAvailable()).isTrue();
                                    assertThat(itemResponse1.getPrice())
                                            .isNotNull()
                                            .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                            .containsExactly(3000L, 10, 300L, 2700L);
                                    assertThat(itemResponse1.getOptions())
                                            .hasSize(1)
                                            .extracting("optionTypeName", "optionValueName")
                                            .containsExactly(tuple("사이즈", "XL"));
                                },
                                itemResponse2 -> {
                                    assertThat(itemResponse2.getId()).isNotNull();
                                    assertThat(itemResponse2.getProductId()).isEqualTo(2L);
                                    assertThat(itemResponse2.getProductName()).isEqualTo("상품2");
                                    assertThat(itemResponse2.getThumbnailUrl()).isEqualTo("http://thumbnail2.jpg");
                                    assertThat(itemResponse2.getQuantity()).isEqualTo(2);
                                    assertThat(itemResponse2.getLineTotal()).isEqualTo(9000);
                                    assertThat(itemResponse2.isAvailable()).isTrue();
                                    assertThat(itemResponse2.getPrice())
                                            .isNotNull()
                                            .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                            .containsExactly(5000L, 10, 500L, 4500L);
                                    assertThat(itemResponse2.getOptions())
                                            .hasSize(1)
                                            .extracting("optionTypeName", "optionValueName")
                                            .containsExactly(tuple("용량", "256GB"));
                                }
                        );
        assertThat(response.getCartTotalPrice()).isEqualTo(17100);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 조회할때 장바구니에 상품이 없는 경우 빈 응답을 반환한다")
    void getCartDetails_When_Empty_CartItems(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        given(cartDomainService.getCartItems(anyLong()))
                .willReturn(List.of());
        //when
        CartResponse response = cartApplicationService.getCartDetails(userPrincipal);
        //then
        assertThat(response.getCartItems()).isEmpty();
        assertThat(response.getCartTotalPrice()).isEqualTo(0L);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 조회할때 해당 상품이 상품서비스에서 찾을 수 없는 경우 해당 상품은 실패 응답으로 채워 반환한다")
    void getCartDetails_When_ProductInfoEmpty(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        CartItemDto item1 = CartItemDto.builder()
                .id(1L)
                .productVariantId(1L)
                .quantity(3)
                .build();

        CartItemDto item2 = CartItemDto.builder()
                .id(2L)
                .productVariantId(2L)
                .quantity(2)
                .build();

        CartProductResponse product1 = createProductResponse(1L, 1L, "상품1",
                3000L, 10, "http://thumbnail1.jpg",
                List.of(CartProductResponse.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()));

        given(cartDomainService.getCartItems(1L))
                .willReturn(List.of(item1, item2));

        given(cartProductClientService.getProducts(anyList()))
                .willReturn(List.of(product1));
        //when
        CartResponse cartDetails = cartApplicationService.getCartDetails(userPrincipal);
        //then
        assertThat(cartDetails.getCartItems()).hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        itemResponse1 -> {
                            assertThat(itemResponse1.getId()).isNotNull();
                            assertThat(itemResponse1.getProductId()).isEqualTo(1L);
                            assertThat(itemResponse1.getProductName()).isEqualTo("상품1");
                            assertThat(itemResponse1.getThumbnailUrl()).isEqualTo("http://thumbnail1.jpg");
                            assertThat(itemResponse1.getQuantity()).isEqualTo(3);
                            assertThat(itemResponse1.getLineTotal()).isEqualTo(8100);
                            assertThat(itemResponse1.isAvailable()).isTrue();
                            assertThat(itemResponse1.getPrice())
                                    .isNotNull()
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .containsExactly(3000L, 10, 300L, 2700L);
                            assertThat(itemResponse1.getOptions())
                                    .hasSize(1)
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactly(tuple("사이즈", "XL"));
                        },
                        itemResponse2 -> {
                            assertThat(itemResponse2.getId()).isNotNull();
                            assertThat(itemResponse2.getProductId()).isNull();
                            assertThat(itemResponse2.getProductName()).isEqualTo("정보를 불러올 수 없거나 판매 중지된 상품입니다");
                            assertThat(itemResponse2.getThumbnailUrl()).isNull();
                            assertThat(itemResponse2.getQuantity()).isEqualTo(2);
                            assertThat(itemResponse2.getLineTotal()).isEqualTo(0);
                            assertThat(itemResponse2.isAvailable()).isFalse();
                            assertThat(itemResponse2.getPrice()).isNull();
                            assertThat(itemResponse2.getOptions()).isNull();
                        }
                );
        assertThat(cartDetails.getCartTotalPrice()).isEqualTo(8100L);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 삭제한다")
    void removeCartItem() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        UserPrincipal userPrincipal = createUserPrincipal(userId, UserRole.ROLE_USER);
        willDoNothing().given(cartDomainService).deleteCartItem(anyLong(), anyLong());
        //when
        cartApplicationService.removeCartItem(userPrincipal, cartItemId);
        //then
        verify(cartDomainService, times(1)).deleteCartItem(userId, cartItemId);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 모두 삭제")
    void clearCart() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        willDoNothing().given(cartDomainService).clearCart(anyLong());
        //when
        cartApplicationService.clearCart(userPrincipal);
        //then
        verify(cartDomainService, times(1))
                .clearCart(1L);
    }
    
    @Test
    @DisplayName("장바구니에 상품 수량을 수정하고 수정된 상품 정보가 포함된 응답을 반환한다")
    void updateCartItemQuantity() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        UpdateQuantityDto dto = UpdateQuantityDto.builder()
                .userPrincipal(userPrincipal)
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
    @DisplayName("장바구니의 상품 수량을 수정할때 상품 서비스에서 상품을 찾을 수 없는 경우 수량을 변경하지 않고 실패 응답으로 채워 반환한다")
    void updateCartItemQuantity_When_ProductService_NotFound_Exception(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        UpdateQuantityDto dto = UpdateQuantityDto.builder()
                .userPrincipal(userPrincipal)
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
        willThrow(new NotFoundException("상품을 찾을 수 없습니다"))
                .given(cartProductClientService)
                .getProduct(anyLong());
        //when
        CartItemResponse result = cartApplicationService.updateCartItemQuantity(dto);
        //then
        assertThat(result.getId()).isNotNull();

        assertThat(result)
                .extracting("productId",
                        "productVariantId",
                        "productName",
                        "thumbnailUrl",
                        "quantity",
                        "lineTotal",
                        "available",
                        "price",
                        "options")
                        .contains(null, null, "정보를 불러올 수 없거나 판매 중지된 상품입니다", null, 1, 0L, false, null, null);
    }

    private UserPrincipal createUserPrincipal(Long userId, UserRole userRole){
        return UserPrincipal.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
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
}

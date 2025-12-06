package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.common.security.UserRole;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartApplicationServiceTest {

    @InjectMocks
    private CartApplicationService cartApplicationService;
    @Mock
    private ProductClientService productClientService;
    @Mock
    private CartService cartService;

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

        ProductResponse product = createProductResponse(1L, 1L, "상품1", 3000L,
                10, "http://thumbnail.jpg",
                List.of(ItemOptionResponse.builder().optionTypeName("사이즈").optionValueName("XL").build()));

        CartItemDto cartItem = CartItemDto.builder()
                .id(1L)
                .productVariantId(1L)
                .quantity(3)
                .build();

        given(productClientService.fetchProductByVariantId(anyLong()))
                .willReturn(product);

        given(cartService.addItemToCart(anyLong(), anyLong(), anyInt()))
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

        assertThat(result.getUnitPrice())
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
    @DisplayName("장바구니에 상품을 추가하는 과정에서 " +
            "ProductClientService가 NotFoundException을 던지면 CartApplicationService도 NotFoundException을 던진다")
    void addItem_When_NotFoundException_Thrown_In_ProductClientService(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        AddCartItemDto command = AddCartItemDto.builder()
                .userPrincipal(userPrincipal)
                .productVariantId(1L)
                .quantity(3)
                .build();

        willThrow(new NotFoundException("해당 상품을 찾을 수 없습니다"))
                .given(productClientService).fetchProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartApplicationService.addItem(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("장바구니에 상품을 추가하는 과정에서 " +
            "ProductClientService가 UnavailableServiceException을 던지면 CartApplicationService 도 UnavailableServiceException을 던진다")
    void addItem_When_UnavailableServiceException_Thrown_In_ProductClientService(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        AddCartItemDto command = AddCartItemDto.builder()
                .userPrincipal(userPrincipal)
                .productVariantId(1L)
                .quantity(3)
                .build();

        willThrow(new UnavailableServiceException("서비스가 응답하지 않습니다 잠시후에 다시 시도해주세요"))
                .given(productClientService).fetchProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartApplicationService.addItem(command))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("서비스가 응답하지 않습니다 잠시후에 다시 시도해주세요");
    }

    @Test
    @DisplayName("장바구니에 상품을 추가하는 과정에서 " +
            "ProductClientService가 InternalServerException을 던지면 CartApplicationService 도 InternalServerException을 던진다")
    void addItem_When_InternalServerException_Thrown_In_ProductClientService(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        AddCartItemDto command = AddCartItemDto.builder()
                .userPrincipal(userPrincipal)
                .productVariantId(1L)
                .quantity(3)
                .build();

        willThrow(new InternalServerException("서비스에 오류가 발생했습니다"))
                .given(productClientService).fetchProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartApplicationService.addItem(command))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("서비스에 오류가 발생했습니다");
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

        ProductResponse product1 = createProductResponse(1L, 1L, "상품1",
                3000L, 10, "http://thumbnail1.jpg",
                List.of(ItemOptionResponse.builder().optionTypeName("사이즈").optionValueName("XL").build()));

        ProductResponse product2 = createProductResponse(2L, 2L, "상품2",
                5000L, 10, "http://thumbnail2.jpg",
                List.of(ItemOptionResponse.builder().optionTypeName("용량").optionValueName("256GB").build()));

        given(cartService.getCartItems(anyLong()))
                .willReturn(List.of(item1, item2));

        given(productClientService.fetchProductByVariantIds(anyList()))
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
                                    assertThat(itemResponse1.getUnitPrice())
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
                                    assertThat(itemResponse2.getUnitPrice())
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
    @DisplayName("장바구니에 담긴 상품을 조회할때 장바구니에 상품이 없는 경우 비어있는 응답을 반환한다")
    void getCartDetailsWhenEmptyCartItems(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        given(cartService.getCartItems(anyLong()))
                .willReturn(List.of());
        //when
        CartResponse response = cartApplicationService.getCartDetails(userPrincipal);
        //then
        assertThat(response.getCartItems()).isEmpty();
        assertThat(response.getCartTotalPrice()).isEqualTo(0L);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 조회할때 ProductClientService 에서 반환되지 않은 응답은 해당 상품의 정보는 찾을 수 없음으로 반환한다")
    void getCartDetailsWhenProductInfoEmpty(){
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

        ProductResponse product1 = createProductResponse(1L, 1L, "상품1",
                3000L, 10, "http://thumbnail1.jpg",
                List.of(ItemOptionResponse.builder().optionTypeName("사이즈").optionValueName("XL").build()));

        given(cartService.getCartItems(1L))
                .willReturn(List.of(item1, item2));

        given(productClientService.fetchProductByVariantIds(anyList()))
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
                            assertThat(itemResponse1.getUnitPrice())
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
                            assertThat(itemResponse2.getUnitPrice()).isNull();
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
        willDoNothing().given(cartService).deleteCartItem(anyLong(), anyLong());
        //when
        cartApplicationService.removeCartItem(userPrincipal, cartItemId);
        //then
        verify(cartService, times(1)).deleteCartItem(userId, cartItemId);
    }

    @Test
    @DisplayName("장바구니에 존재하지 않는 상품을 삭제하려 하면 NotFoundException이 발생한다")
    void removeCartItemWhenNotFoundException() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        UserPrincipal userPrincipal = createUserPrincipal(userId, UserRole.ROLE_USER);
        willThrow(new NotFoundException("장바구니에서 상품을 찾을 수 없습니다"))
                .given(cartService).deleteCartItem(anyLong(), anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartApplicationService.removeCartItem(userPrincipal, cartItemId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("장바구니에서 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 장바구니 상품을 삭제하려 하면 NoPermissionException이 발생한다")
    void removeCartItemWhenNoPermissionException() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        UserPrincipal userPrincipal = createUserPrincipal(userId, UserRole.ROLE_USER);
        willThrow(new NoPermissionException("장바구니의 상품을 삭제할 권한이 없습니다"))
                .given(cartService).deleteCartItem(anyLong(), anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartApplicationService.removeCartItem(userPrincipal, cartItemId))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage("장바구니의 상품을 삭제할 권한이 없습니다");
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 모두 삭제")
    void clearCart() {
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        willDoNothing().given(cartService).clearCart(anyLong());
        //when
        cartApplicationService.clearCart(userPrincipal);
        //then
        verify(cartService, times(1))
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
        given(cartService.getCartItem(anyLong()))
                .willReturn(
                        CartItemDto.builder()
                                .id(1L)
                                .productVariantId(1L)
                                .quantity(1)
                                .build()
                );
        ProductResponse product = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail.jpg", List.of(
                        ItemOptionResponse.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                ));

        given(productClientService.fetchProductByVariantId(anyLong()))
                .willReturn(product);

        given(cartService.updateQuantity(anyLong(), anyInt()))
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

        assertThat(result.getUnitPrice())
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
    @DisplayName("장바구니에 담긴 상품의 수량을 수정할때 장바구니에 해당 상품을 찾을 수 없는 경우 NotFoundException을 반환한다")
    void updateCartItemQuantityWhenNotFoundCartItem(){
        //given
        UserPrincipal userPrincipal = createUserPrincipal(1L, UserRole.ROLE_USER);
        UpdateQuantityDto dto = UpdateQuantityDto
                .builder()
                .userPrincipal(userPrincipal)
                .cartItemId(1L)
                .quantity(3)
                .build();
        willThrow(new NotFoundException("장바구니에서 해당 상품을 찾을 수 없습니다"))
                .given(cartService).getCartItem(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartApplicationService.updateCartItemQuantity(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("장바구니에서 해당 상품을 찾을 수 없습니다");
    }

    private UserPrincipal createUserPrincipal(Long userId, UserRole userRole){
        return UserPrincipal.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
    }

    private ProductResponse createProductResponse(Long productId, Long productVariantId,
                                                  String productName, Long originalPrice, int discountRate,
                                                  String thumbnail, List<ItemOptionResponse> options){
        long discountAmount = originalPrice * discountRate / 100;
        return ProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .unitPrice(
                        UnitPrice.builder()
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

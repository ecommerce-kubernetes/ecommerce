package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.controller.dto.response.CartItemResponse;
import com.example.order_service.api.cart.controller.dto.response.CartResponse;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.common.security.UserRole;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

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
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();
        AddCartItemDto command = AddCartItemDto.builder()
                .userPrincipal(userPrincipal)
                .productVariantId(1L)
                .quantity(3)
                .build();

        UnitPrice unitPrice = UnitPrice.builder()
                .originalPrice(3000)
                .discountRate(10)
                .discountAmount(300)
                .discountedPrice(2700)
                .build();

        ProductResponse product = ProductResponse.builder()
                .productId(1L)
                .productVariantId(1L)
                .productName("상품1")
                .unitPrice(unitPrice)
                .thumbnailUrl("http://thumbnail.jpg")
                .itemOptions(
                        List.of(ItemOptionResponse.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                        )
                ).build();

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
    }

    @Test
    @DisplayName("상품 서비스에서 해당 상품을 찾을 수 없는 경우 NotFoundException을 던진다")
    void addItemWhenProductServiceNotFound(){
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();
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
}

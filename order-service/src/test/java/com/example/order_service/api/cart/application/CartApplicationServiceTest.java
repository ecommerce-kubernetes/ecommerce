package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.common.security.UserRole;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.exception.server.InternalServerException;
import com.example.order_service.exception.server.UnavailableServiceException;
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
    @DisplayName("장바구니에 상품을 추가하는 과정에서 " +
            "ProductClientService가 NotFoundException을 던지면 CartApplicationService도 NotFoundException을 던진다")
    void addItem_When_NotFoundException_Thrown_In_ProductClientService(){
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

    @Test
    @DisplayName("장바구니에 상품을 추가하는 과정에서 " +
            "ProductClientService가 UnavailableServiceException을 던지면 CartApplicationService 도 UnavailableServiceException을 던진다")
    void addItem_When_UnavailableServiceException_Thrown_In_ProductClientService(){
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
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();
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
}

package com.example.order_service.service.unit;

import com.example.order_service.common.MessagePath;
import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartItemsRepository;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.CartService;
import com.example.order_service.util.TestMessageUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiremock.org.checkerframework.checker.units.qual.C;

import java.util.Optional;

import static com.example.order_service.common.MessagePath.CART_ITEM_NOT_FOUND;
import static com.example.order_service.common.MessagePath.CART_ITEM_NO_PERMISSION;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @InjectMocks
    CartService cartService;

    @Mock
    CartsRepository cartsRepository;
    @Mock
    CartItemsRepository cartItemsRepository;
    @Mock
    MessageSourceUtil ms;

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-성공")
    void deleteCartItemByIdTest_unit_success(){
        Carts cart = new Carts(1L);
        CartItems cartItem = new CartItems(1L, 10);
        cart.addCartItem(cartItem);
        when(cartItemsRepository.findWithCartById(1L))
                .thenReturn(Optional.of(cartItem));

        cartService.deleteCartItemById(1L, 1L);

        assertThat(cart.getCartItems()).hasSize(0);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(장바구니 상품을 찾을 수 없을경우)")
    void deleteCartItemByIdTest_unit_notFoundCartItem(){
        when(cartItemsRepository.findWithCartById(1L)).thenReturn(Optional.empty());
        when(ms.getMessage(CART_ITEM_NOT_FOUND))
                .thenReturn(getMessage(CART_ITEM_NOT_FOUND));
        assertThatThrownBy(() -> cartService.deleteCartItemById(1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CART_ITEM_NOT_FOUND));
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(장바구니 상품을 삭제할 권한이 없는 경우)")
    void deleteCartItemByIdTest_unit_noPermission(){
        Carts cart = new Carts(1L);
        CartItems cartItem = new CartItems(1L, 10);
        cart.addCartItem(cartItem);
        when(cartItemsRepository.findWithCartById(1L))
                .thenReturn(Optional.of(cartItem));
        when(ms.getMessage(CART_ITEM_NO_PERMISSION))
                .thenReturn(getMessage(CART_ITEM_NO_PERMISSION));

        assertThatThrownBy(() -> cartService.deleteCartItemById(99L, 1L))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage(getMessage(CART_ITEM_NO_PERMISSION));
    }

    @Test
    @DisplayName("장바구니 비우기 테스트-성공")
    void clearAllCartItemsTest_unit_success(){
        Carts cart = new Carts(1L);
        cart.addCartItem(new CartItems(1L, 10));

        when(cartsRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        cartService.clearAllCartItems(1L);

        assertThat(cart.getCartItems().size()).isEqualTo(0);
    }


}

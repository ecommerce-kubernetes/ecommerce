package com.example.order_service.service.unit;

import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @InjectMocks
    CartService cartService;

    @Mock
    CartsRepository cartsRepository;

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

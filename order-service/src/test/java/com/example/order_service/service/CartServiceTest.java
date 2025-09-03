package com.example.order_service.service;

import com.example.order_service.common.MessagePath;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.util.TestMessageUtil;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.example.order_service.common.MessagePath.CART_ITEM_NOT_FOUND;
import static com.example.order_service.common.MessagePath.CART_ITEM_NO_PERMISSION;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CartServiceTest {

    @Autowired
    CartsRepository cartsRepository;
    @Autowired
    EntityManager em;

    @Autowired
    CartService cartService;
    Carts cart;
    CartItems cartItem;
    @BeforeEach
    void setFixture(){
        cart = new Carts(1L);
        cartItem = new CartItems(1L, 10);
        cart.addCartItem(cartItem);
        cartsRepository.save(cart);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-성공")
    @Transactional
    void deleteCartItemByIdTest_integration_success(){
        cartService.deleteCartItemById(1L, cartItem.getId());
        em.flush(); em.clear();

        Carts cart = cartsRepository.findByUserId(1L).get();

        assertThat(cart.getCartItems()).hasSize(0);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(장바구니 상품을 찾을 수 없는 경우)")
    @Transactional
    void deleteCartItemByIdTest_integration_notFound_cartItem(){
        assertThatThrownBy(() -> cartService.deleteCartItemById(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CART_ITEM_NOT_FOUND));
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(삭제할 권한이 없는 경우)")
    @Transactional
    void deleteCartItemByIdTest_integration_noPermission(){
        assertThatThrownBy(() -> cartService.deleteCartItemById(99L, cartItem.getId()))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage(getMessage(CART_ITEM_NO_PERMISSION));
    }

    @Test
    @DisplayName("장바구니 비우기 테스트-성공")
    @Transactional
    void clearAllCartItemTest_integration_success(){
        cartService.clearAllCartItems(1L);
        em.flush(); em.clear();

        Carts findCart = cartsRepository.findByUserId(1L).get();

        assertThat(findCart.getCartItems()).hasSize(0);
    }
}
package com.example.order_service.service;

import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.repository.CartsRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("장바구니 비우기 테스트-성공")
    @Transactional
    void clearAllCartItemTest_integration_success(){
        cartService.clearAllCartItems(1L);
        em.flush(); em.clear();

        Carts findCart = cartsRepository.findByUserId(1L).get();

        assertThat(findCart.getCartItems()).hasSize(0);
    }
}
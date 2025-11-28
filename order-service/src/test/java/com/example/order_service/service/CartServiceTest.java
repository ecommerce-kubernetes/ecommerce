package com.example.order_service.service;

import com.example.order_service.common.scheduler.PendingOrderTimeoutScheduler;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class CartServiceTest {

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry){
        registry.add("product-service.url", () -> "http://localhost:${wiremock.server.port}");
    }
    @MockitoBean
    PendingOrderTimeoutScheduler pendingOrderTimeoutScheduler;
    @Autowired
    CartsRepository cartsRepository;
    @Autowired
    EntityManager em;
    ObjectMapper mapper = new ObjectMapper();

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

    @AfterEach
    void clearDB(){
        cartsRepository.deleteAll();
    }


    @Test
    @DisplayName("장바구니 목록 조회 테스트-성공(장바구니가 없는 경우)")
    @Transactional
    void getCartItemListTest_integration_noCart(){
        CartResponse response = cartService.getCartItemList(2L);
        assertThat(response.getCartTotalPrice()).isEqualTo(0);
        assertThat(response.getCartItems()).isEmpty();
    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-성공(장바구니에 상품이 없는 경우)")
    @Transactional
    void getCartItemListTest_integration_noItems(){
        cartsRepository.save(new Carts(2L));
        em.flush(); em.clear();

        CartResponse response = cartService.getCartItemList(2L);
        assertThat(response.getCartItems()).isEmpty();
        assertThat(response.getCartTotalPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-성공")
    @Transactional
    void deleteCartItemByIdTest_integration_success(){
        cartService.deleteCartItemById(1L, cartItem.getId());
        em.flush(); em.clear();

        Carts cart = cartsRepository.findWithItemsByUserId(1L).get();

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

        Carts findCart = cartsRepository.findWithItemsByUserId(1L).get();

        assertThat(findCart.getCartItems()).hasSize(0);
    }
}
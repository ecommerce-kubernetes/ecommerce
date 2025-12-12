package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.model.CartItems;
import com.example.order_service.api.cart.domain.model.Carts;
import com.example.order_service.api.cart.domain.repository.CartsRepository;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.support.ExcludeInfraServiceTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
class CartDomainServiceTest extends ExcludeInfraServiceTest {
    @Autowired
    private CartDomainService cartDomainService;
    @Autowired
    private CartsRepository cartsRepository;
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("처음 장바구니에 상품을 추가하면 장바구니를 생성하고 상품을 추가한다")
    void addItemToCartWhenFirstAdd(){
        //given
        //when
        CartItemDto result = cartDomainService.addItemToCart(1L, 1L, 3);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting("productVariantId", "quantity")
                .contains(1L, 3);

        Optional<Carts> cart = cartsRepository.findByUserId(1L);
        assertThat(cart).isNotNull();
    }

    @Test
    @DisplayName("처음 이후 장바구니에 새로운 상품을 추가하면 기존 장바구니에 상품을 추가한다")
    void addItemToCartWhenAddAfterSecond(){
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        cartsRepository.save(cart);
        //when
        CartItemDto result = cartDomainService.addItemToCart(1L, 1L, 3);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting("productVariantId", "quantity")
                .contains(1L, 3);
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 추가하려는 상품이 이미 장바구니에 존재하는 상품이면 수량을 요청 수량만큼 증가시킨다")
    void addItemToCartWhenExistCartItem() {
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        CartItems cartItem = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        cart.addCartItem(cartItem);
        cartsRepository.save(cart);
        //when
        CartItemDto result = cartDomainService.addItemToCart(1L, 1L, 2);
        //then
        assertThat(result.getId()).isNotNull();

        assertThat(result)
                .extracting("productVariantId", "quantity")
                .contains(1L, 5);
    }

    @Test
    @DisplayName("장바구니 상품을 조회한다")
    void getCartItem() {
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        CartItems item = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        cart.addCartItem(item);
        cartsRepository.save(cart);
        //when
        CartItemDto cartItem = cartDomainService.getCartItem(item.getId());
        //then
        assertThat(cartItem.getId()).isEqualTo(item.getId());
        assertThat(cartItem.getProductVariantId()).isEqualTo(1L);
        assertThat(cartItem.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("장바구니 상품을 찾을 수 없을때는 NotFoundException을 반환한다")
    void getCartItemWhenNotFoundException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> cartDomainService.getCartItem(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("장바구니에서 해당 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("장바구니 상품 목록을 조회한다")
    void getCartItems(){
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();
        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);
        //when
        List<CartItemDto> cartItems = cartDomainService.getCartItems(1L);
        //then
        assertThat(cartItems).hasSize(2);
        assertThat(cartItems)
                .extracting("id", "productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(item1.getId(), 1L, 3),
                        tuple(item2.getId(), 2L, 2)
                );
    }

    @Test
    @DisplayName("장바구니 상품 목록을 조회할때 해당 유저의 장바구니가 존재하지 않으면 빈 리스트를 반환한다")
    void getCartItemsWhenNotFoundCart(){
        //given
        //when
        List<CartItemDto> cartItems = cartDomainService.getCartItems(1L);
        //then
        assertThat(cartItems).hasSize(0);
    }

    @Test
    @DisplayName("장바구니 상품 목록을 조회할때 해당 유저의 장바구니에 상품이 존재하지 않으면 빈 리스트를 반환한다")
    void getCatItemsWhenCartItemEmpty() {
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        cartsRepository.save(cart);
        //when
        List<CartItemDto> cartItems = cartDomainService.getCartItems(1L);
        //then
        assertThat(cartItems).hasSize(0);
    }
    
    @Test
    @DisplayName("장바구니에 담긴 상품을 삭제한다")
    void deleteCartItem() {
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();
        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);
        //when
        cartDomainService.deleteCartItem(1L, item1.getId());
        em.flush();
        em.clear();
        //then
        Optional<Carts> findCart = cartsRepository.findWithItemsByUserId(1L);
        assertThat(findCart).isNotEmpty();
        assertThat(findCart.get().getCartItems()).hasSize(1);
        assertThat(findCart.get().getCartItems())
                .extracting("productVariantId", "quantity")
                .contains(
                        tuple(2L, 2)
                );
    }

    @Test
    @DisplayName("없는 상품을 삭제하려는 경우 NotFoundException 예외를 반환한다")
    void deleteCartItemWhenNotFoundException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> cartDomainService.deleteCartItem(1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("장바구니에서 해당 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 장바구니 상품을 삭제하려 하면 NoPermissionException이 발생한다")
    void deleteCartItemWhenNoPermissionException() {
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();
        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);
        //when
        //then
        assertThatThrownBy(() -> cartDomainService.deleteCartItem(2L, item1.getId()))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage("장바구니의 상품을 삭제할 권한이 없습니다");
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 모두 삭제한다")
    void clearCart() {
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();

        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);
        //when
        cartDomainService.clearCart(1L);
        //then
        Optional<Carts> findCart = cartsRepository.findWithItemsByUserId(1L);
        assertThat(findCart).isNotEmpty();
        assertThat(findCart.get().getCartItems()).hasSize(0);
    }

    @Test
    @DisplayName("장바구니 상품의 수량을 변경한다")
    void updateQuantity(){
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        cart.addCartItem(item);
        cartsRepository.save(cart);
        //when
        CartItemDto cartItemDto = cartDomainService.updateQuantity(item.getId(), 5);
        //then
        assertThat(cartItemDto.getId()).isEqualTo(item.getId());
        assertThat(cartItemDto.getQuantity()).isEqualTo(5);
    }
}
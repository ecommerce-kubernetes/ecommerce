package com.example.order_service.api.cart.domain.repository;

import com.example.order_service.api.cart.domain.model.Cart;
import com.example.order_service.api.cart.domain.model.CartItem;
import com.example.order_service.api.support.ExcludeInfraTest;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class CartRepositoryTest extends ExcludeInfraTest {
    @Autowired
    private CartsRepository cartsRepository;
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("UserId로 장바구니를 조회할때 해당 UserId의 장바구니가 없는 경우 비어있는 Optional을 반환한다")
    void findByUserId(){
        //given
        //when
        Optional<Cart> find = cartsRepository.findByUserId(1L);
        //then
        assertThat(find).isEmpty();
    }

    @Test
    @DisplayName("UserId로 장바구니를 조회할때 해당 UserId의 장바구니가 있는 경우 Optional에 Carts를 담아 반환한다")
    void findByUserIdWithExistCarts(){
        //given
        Cart cart = Cart.builder()
                .userId(1L)
                .build();

        cartsRepository.save(cart);
        //when
        Optional<Cart> find = cartsRepository.findByUserId(1L);
        //then
        assertThat(find).isNotEmpty();
        assertThat(find.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("UserId로 장바구니를 조회할때 장바구니의 CartItem을 함께 가져온다")
    void findWithItemsByUserId(){
        //given
        Cart carts = Cart.builder()
                .userId(1L)
                .build();
        CartItem cartItem = CartItem.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        carts.addCartItem(cartItem);
        cartsRepository.save(carts);

        Session session = em.unwrap(Session.class);
        session.getSessionFactory().getStatistics().setStatisticsEnabled(true);
        session.getSessionFactory().getStatistics().clear();
        em.clear();
        //when
        Optional<Cart> cart = cartsRepository.findWithItemsByUserId(1L);
        //then
        assertThat(cart).isNotEmpty();
        long queryCount = session.getSessionFactory().getStatistics().getPrepareStatementCount();
        assertThat(queryCount).isEqualTo(1);

        assertThat(Hibernate.isInitialized(cart.get().getCartItems())).isTrue();
    }

}

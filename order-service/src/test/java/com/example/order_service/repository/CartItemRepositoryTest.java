package com.example.order_service.repository;

import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CartItemRepositoryTest {

    @Autowired
    private CartsRepository cartsRepository;

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("cartItemId로 상품을 조회할때 Cart도 함께 가져온다")
    void findWithCartById(){
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        CartItems item = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        cart.getCartItems().add(item);
        cartsRepository.save(cart);

        Session session = em.unwrap(Session.class);
        session.getSessionFactory().getStatistics().setStatisticsEnabled(true);
        session.getSessionFactory().getStatistics().clear();
        em.clear();
        //when
        Optional<CartItems> cartItem = cartItemsRepository.findWithCartById(item.getId());
        //then
        assertThat(cartItem).isNotEmpty();
        long queryCount = session.getSessionFactory().getStatistics().getPrepareStatementCount();
        assertThat(queryCount).isEqualTo(1);

        assertThat(Hibernate.isInitialized(cartItem.get().getCart())).isTrue();
    }
}

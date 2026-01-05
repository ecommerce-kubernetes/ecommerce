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
public class CartItemRepositoryTest extends ExcludeInfraTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("cartItemId로 상품을 조회할때 Cart도 함께 가져온다")
    void findWithCartById(){
        //given
        Cart cart = Cart.create(1L);
        CartItem item = CartItem.create(1L,3);

        cart.addCartItem(item);
        cartRepository.save(cart);

        Session session = em.unwrap(Session.class);
        session.getSessionFactory().getStatistics().setStatisticsEnabled(true);
        session.getSessionFactory().getStatistics().clear();
        em.clear();
        //when
        Optional<CartItem> cartItem = cartItemRepository.findWithCartById(item.getId());
        //then
        assertThat(cartItem).isNotEmpty();
        long queryCount = session.getSessionFactory().getStatistics().getPrepareStatementCount();
        assertThat(queryCount).isEqualTo(1);

        assertThat(Hibernate.isInitialized(cartItem.get().getCart())).isTrue();
    }
}

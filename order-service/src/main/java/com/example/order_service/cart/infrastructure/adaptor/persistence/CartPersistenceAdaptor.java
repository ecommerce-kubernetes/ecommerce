package com.example.order_service.cart.infrastructure.adaptor.persistence;

import com.example.order_service.cart.application.port.CartRepository;
import com.example.order_service.cart.domain.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartPersistenceAdaptor implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Optional<Cart> findWithItemsByUserId(Long userId) {
        return cartJpaRepository.findWithItemsByUserId(userId);
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return cartJpaRepository.findByUserId(userId);
    }

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(cart);
    }
}

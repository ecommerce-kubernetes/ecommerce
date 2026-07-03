package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.service.dto.result.CartResult;
import com.example.order_service.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartQueryService {

    private final CartRepository cartRepository;

    public CartResult.Cart getCart() {
        return null;
    }
}

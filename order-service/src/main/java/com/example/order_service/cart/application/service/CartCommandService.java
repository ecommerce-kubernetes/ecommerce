package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartCommandService {
    private final CartRepository cartRepository;


    public List<CartItemData> addCartItems(AddCartItemsCommand command) {
        return null;
    }
}

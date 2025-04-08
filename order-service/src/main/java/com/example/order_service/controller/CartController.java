package com.example.order_service.controller;

import com.example.order_service.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
@Slf4j
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity addCartItem(){
        return null;
    }
}

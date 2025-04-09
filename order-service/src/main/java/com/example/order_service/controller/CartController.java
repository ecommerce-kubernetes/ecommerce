package com.example.order_service.controller;

import com.example.order_service.dto.request.CartItemRequestDto;
import com.example.order_service.dto.response.CartItemResponseDto;
import com.example.order_service.dto.response.CartResponseDto;
import com.example.order_service.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
@Slf4j
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartItemResponseDto> addCartItem(@RequestBody @Validated CartItemRequestDto cartItemRequestDto,
                                                           @RequestHeader("user-id") String userIdHeader){

        Long userId = Long.parseLong(userIdHeader);

        CartItemResponseDto cartItemResponseDto = cartService.addItem(userId, cartItemRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemResponseDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponseDto> getAllCartItem(@PathVariable("userId") Long userId){
        CartResponseDto cartItemList = cartService.getCartItemList(userId);
        return ResponseEntity.ok(cartItemList);
    }
}

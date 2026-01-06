package com.example.order_service.api.cart.controller;

import com.example.order_service.api.cart.application.CartApplicationService;
import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.api.common.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartController {
    private final CartApplicationService cartApplicationService;

    @PostMapping
    public ResponseEntity<CartItemResponse> addCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                        @RequestBody @Validated CartItemRequest request){
        AddCartItemDto dto = AddCartItemDto.of(userPrincipal.getUserId(), request.getProductVariantId(), request.getQuantity());
        CartItemResponse response = cartApplicationService.addItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getAllCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        CartResponse cartItemList = cartApplicationService.getCartDetails(userPrincipal.getUserId());
        return ResponseEntity.ok(cartItemList);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable("cartItemId") Long cartItemId){
        cartApplicationService.removeCartItem(userPrincipal.getUserId(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal userPrincipal){
        cartApplicationService.clearCart(userPrincipal.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @PathVariable("cartItemId") Long cartItemId,
                                                           @RequestBody @Validated UpdateQuantityRequest request){
        UpdateQuantityDto updateQuantityDto = UpdateQuantityDto.of(userPrincipal.getUserId(), cartItemId, request);
        CartItemResponse response = cartApplicationService.updateCartItemQuantity(updateQuantityDto);
        return ResponseEntity.ok(response);
    }
}

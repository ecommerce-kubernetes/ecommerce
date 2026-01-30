package com.example.order_service.api.cart.controller;

import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.api.cart.facade.CartFacade;
import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartResponse;
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
    private final CartFacade cartFacade;

    @PostMapping
    public ResponseEntity<CartItemResponse> addCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                        @RequestBody @Validated CartItemRequest request){
        AddCartItemCommand dto = AddCartItemCommand.of(userPrincipal.getUserId(), request.getProductVariantId(), request.getQuantity());
        CartItemResponse response = cartFacade.addItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getAllCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        CartResponse cartItemList = cartFacade.getCartDetails(userPrincipal.getUserId());
        return ResponseEntity.ok(cartItemList);
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @PathVariable("cartItemId") Long cartItemId,
                                                           @RequestBody @Validated UpdateQuantityRequest request){
        UpdateQuantityCommand updateQuantityCommand = UpdateQuantityCommand.of(userPrincipal.getUserId(), cartItemId, request);
        CartItemResponse response = cartFacade.updateCartItemQuantity(updateQuantityCommand);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable("cartItemId") Long cartItemId){
        cartFacade.removeCartItem(userPrincipal.getUserId(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal userPrincipal){
        cartFacade.clearCart(userPrincipal.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

package com.example.order_service.api.cart.controller;

import com.example.order_service.api.cart.controller.dto.request.CartRequest;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.api.cart.controller.dto.response.CartResponse;
import com.example.order_service.api.cart.facade.CartFacade;
import com.example.order_service.api.cart.facade.dto.command.CartCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartResult;
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
    public ResponseEntity<CartResponse.Cart> addCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                    @RequestBody @Validated CartRequest.AddItems request){
        CartCommand.AddItems command = request.toCommand(userPrincipal.getUserId());
        CartResult.Cart result = cartFacade.addItems(command);
        CartResponse.Cart response = CartResponse.Cart.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse.Cart> getAllCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        CartResult.Cart result = cartFacade.getCartDetails(userPrincipal.getUserId());
        CartResponse.Cart response = CartResponse.Cart.from(result);
        return ResponseEntity.ok(response);
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

package com.example.order_service.cart.api;

import com.example.order_service.api.common.security.model.UserPrincipal;
import com.example.order_service.cart.api.dto.request.CartRequest;
import com.example.order_service.cart.api.dto.response.CartResponse;
import com.example.order_service.cart.application.CartFacade;
import com.example.order_service.cart.application.dto.command.CartCommand;
import com.example.order_service.cart.application.dto.result.CartResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    public ResponseEntity<CartResponse.Update> updateQuantity(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @PathVariable("cartItemId") Long cartItemId,
                                                           @RequestBody @Validated CartRequest.UpdateQuantity request){
        CartCommand.UpdateQuantity command = request.toCommand(userPrincipal.getUserId(), cartItemId);
        CartResult.Update result = cartFacade.updateCartItemQuantity(command);
        CartResponse.Update response = CartResponse.Update.from(result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCartItems(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @RequestParam List<Long> cartItemIds){
        cartFacade.removeCartItems(userPrincipal.getUserId(), cartItemIds);
        return ResponseEntity.noContent().build();
    }
}

package com.example.order_service.cart.api;

import com.example.order_service.cart.api.dto.request.AddCartItemsRequest;
import com.example.order_service.cart.api.dto.request.UpdateCartItemQuantityRequest;
import com.example.order_service.cart.api.dto.response.AddCartItemsResponse;
import com.example.order_service.cart.api.dto.response.CartResponse;
import com.example.order_service.cart.api.dto.response.UpdateCartItemQuantityResponse;
import com.example.order_service.cart.application.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.result.AddCartItemsResult;
import com.example.order_service.cart.application.dto.result.UpdateCartItemQuantityResult;
import com.example.order_service.cart.application.facade.CartFacade;
import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.result.CartResult;
import com.example.order_service.common.security.model.UserPrincipal;
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
    public ResponseEntity<AddCartItemsResponse> addCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                            @RequestBody @Validated AddCartItemsRequest request){
        AddCartItemsCommand command = request.toCommand(userPrincipal.getUserId());
        AddCartItemsResult result = cartFacade.addItems(command);
        AddCartItemsResponse response = AddCartItemsResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getAllCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        CartResult result = cartFacade.getCartDetails(userPrincipal.getUserId());
        CartResponse response = CartResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<UpdateCartItemQuantityResponse> updateQuantity(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                         @PathVariable("cartItemId") Long cartItemId,
                                                                         @RequestBody @Validated UpdateCartItemQuantityRequest request){
        UpdateCartItemQuantityCommand command = request.toCommand(userPrincipal.getUserId(), cartItemId);
        UpdateCartItemQuantityResult result = cartFacade.updateCartItemQuantity(command);
        UpdateCartItemQuantityResponse response = UpdateCartItemQuantityResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCartItems(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @RequestParam List<Long> cartItemIds){
        DeleteCartItemsCommand command = DeleteCartItemsCommand.of(userPrincipal.getUserId(), cartItemIds);
        cartFacade.removeCartItems(command);
        return ResponseEntity.noContent().build();
    }
}

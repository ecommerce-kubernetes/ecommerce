package com.example.order_service.cart.api;

import com.example.order_service.cart.api.dto.request.AddCartItemsRequest;
import com.example.order_service.cart.api.dto.request.UpdateCartItemQuantityRequest;
import com.example.order_service.cart.api.dto.response.AddCartItemsResponse;
import com.example.order_service.cart.api.dto.response.GetCartResponse;
import com.example.order_service.cart.api.dto.response.UpdateCartItemQuantityResponse;
import com.example.order_service.cart.application.service.CartFacade;
import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.CartCommand;
import com.example.order_service.cart.application.service.dto.result.GetCartResult;
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
        GetCartResult result = cartFacade.addItems(command);
        AddCartItemsResponse response = AddCartItemsResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<GetCartResponse> getAllCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        GetCartResult result = cartFacade.getCartDetails(userPrincipal.getUserId());
        GetCartResponse response = GetCartResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<UpdateCartItemQuantityResponse> updateQuantity(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                         @PathVariable("cartItemId") Long cartItemId,
                                                                         @RequestBody @Validated UpdateCartItemQuantityRequest request){
        CartCommand.UpdateQuantity command = request.toCommand(userPrincipal.getUserId(), cartItemId);
        GetCartResult result = cartFacade.updateCartItemQuantity(command);
        UpdateCartItemQuantityResponse response = UpdateCartItemQuantityResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCartItems(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @RequestParam List<Long> cartItemIds){
        cartFacade.removeCartItems(userPrincipal.getUserId(), cartItemIds);
        return ResponseEntity.noContent().build();
    }
}

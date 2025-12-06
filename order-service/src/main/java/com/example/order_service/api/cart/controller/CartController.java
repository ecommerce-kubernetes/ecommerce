package com.example.order_service.api.cart.controller;

import com.example.order_service.api.cart.application.CartApplicationService;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.api.common.util.specification.annotation.BadRequestApiResponse;
import com.example.order_service.api.common.util.specification.annotation.NotFoundApiResponse;
import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/carts")
@Tag(name = "Cart", description = "장바구니 관련 API")
@RequiredArgsConstructor
public class CartController {
    private final CartApplicationService cartApplicationService;

    @Operation(summary = "상품 추가")
    @ApiResponse(responseCode = "201", description = "상품 추가 성공")
    @BadRequestApiResponse @NotFoundApiResponse
    @PostMapping
    public ResponseEntity<CartItemResponse> addCartItem(@RequestBody @Validated CartItemRequest cartItemRequest,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal){
        AddCartItemDto dto = AddCartItemDto.of(userPrincipal, cartItemRequest);
        CartItemResponse response = cartApplicationService.addItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "장바구니 목록 조회")
    @BadRequestApiResponse
    @GetMapping
    public ResponseEntity<CartResponse> getAllCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        CartResponse cartItemList = cartApplicationService.getCartDetails(userPrincipal);
        return ResponseEntity.ok(cartItemList);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable("cartItemId") Long cartItemId){
        cartApplicationService.removeCartItem(userPrincipal, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "장바구니 비우기")
    @BadRequestApiResponse
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal userPrincipal){
        cartApplicationService.clearCart(userPrincipal);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @PathVariable("cartItemId") Long cartItemId,
                                                           @RequestBody @Validated UpdateQuantityRequest request){
        UpdateQuantityDto updateQuantityDto = UpdateQuantityDto.of(userPrincipal, cartItemId, request);
        CartItemResponse response = cartApplicationService.updateCartItemQuantity(updateQuantityDto);
        return ResponseEntity.ok(response);
    }
}

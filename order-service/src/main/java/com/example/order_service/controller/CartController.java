package com.example.order_service.controller;

import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.controller.dto.UpdateQuantityRequest;
import com.example.order_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.order_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.order_service.controller.dto.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.service.CartService;
import com.example.order_service.service.dto.AddCartItemDto;
import com.example.order_service.service.dto.UpdateQuantityDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cart", description = "장바구니 관련 API")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @Operation(summary = "상품 추가")
    @ApiResponse(responseCode = "201", description = "상품 추가 성공")
    @BadRequestApiResponse @NotFoundApiResponse
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<CartItemResponse> addCartItem(@RequestBody @Validated CartItemRequest cartItemRequest,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal){
        AddCartItemDto dto = AddCartItemDto.of(userPrincipal, cartItemRequest);
        CartItemResponse response = cartService.addItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "장바구니 목록 조회")
    @BadRequestApiResponse
    @GetMapping
    public ResponseEntity<CartResponse> getAllCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        CartResponse cartItemList = cartService.getCartItemList(userPrincipal);
        return ResponseEntity.ok(cartItemList);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable("cartItemId") Long cartItemId){
        cartService.deleteCartItemById(userPrincipal, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "장바구니 비우기")
    @BadRequestApiResponse
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal userPrincipal){
        cartService.clearAllCartItems(userPrincipal);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @RequestBody @Validated UpdateQuantityRequest request){
        UpdateQuantityDto updateQuantityDto = UpdateQuantityDto.of(userPrincipal, request);
        CartItemResponse response = cartService.updateCartItemQuantity(updateQuantityDto);
        return ResponseEntity.ok(response);
    }
}

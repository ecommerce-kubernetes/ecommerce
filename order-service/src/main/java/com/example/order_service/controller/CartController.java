package com.example.order_service.controller;

import com.example.order_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.order_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponseDto;
import com.example.order_service.dto.response.CartResponseDto;
import com.example.order_service.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<CartItemResponseDto> addCartItem(@RequestBody @Validated CartItemRequest cartItemRequest,
                                                           @RequestHeader("user-id") String userIdHeader){

        Long userId = Long.parseLong(userIdHeader);

        CartItemResponseDto cartItemResponseDto = cartService.addItem(userId, cartItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemResponseDto);
    }

    @Operation(summary = "장바구니 목록 조회")
    @NotFoundApiResponse
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponseDto> getAllCartItem(@PathVariable("userId") Long userId) {
        CartResponseDto cartItemList = cartService.getCartItemList(userId);
        return ResponseEntity.ok(cartItemList);
    }

    @Operation(summary = "장바구니 상품 삭제")
    @NotFoundApiResponse
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable("cartItemId") Long cartItemId){
        cartService.deleteCartItemById(cartItemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "장바구니 비우기")
    @NotFoundApiResponse
    @DeleteMapping("/{cartId}/all")
    public ResponseEntity<Void> removeCartAll(@PathVariable("cartId") Long cartId){
        cartService.deleteCartAll(cartId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

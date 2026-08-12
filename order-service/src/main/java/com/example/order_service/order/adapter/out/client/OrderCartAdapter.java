package com.example.order_service.order.adapter.out.client;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.order.application.port.OrderCartPort;
import com.example.order_service.order.application.port.dto.OrderCartItemsResult;
import com.example.order_service.order.exception.OrderCartPortErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCartAdapter implements OrderCartPort {

    private final CartQueryService cartQueryService;

    @Override
    public OrderCartItemsResult getCartItems(Long userId, List<Long> cartItemIds) {
        List<CartItemData> cartItems = executeGetCartItems(userId, cartItemIds);
        return mapToOrderCartProductResult(cartItems);
    }

    private OrderCartItemsResult mapToOrderCartProductResult(List<CartItemData> cartItems) {
        List<OrderCartItemsResult.Item> items = cartItems.stream().map(item -> OrderCartItemsResult.Item.builder()
                .cartItemId(item.cartItemId())
                .productVariantId(item.productVariantId())
                .quantity(item.quantity()).build()).toList();

        return OrderCartItemsResult.builder()
                .items(items)
                .build();
    }

    private List<CartItemData> executeGetCartItems(Long userId, List<Long> cartItemIds) {
        try {
            return cartQueryService.findCartItemsByCartItemIds(userId, cartItemIds);
        } catch (Exception e) {
            throw new PortException(OrderCartPortErrorCode.CART_SERVER_ERROR, e.getMessage(), "장바구니 상품 조회중 에러 발생");
        }
    }
}

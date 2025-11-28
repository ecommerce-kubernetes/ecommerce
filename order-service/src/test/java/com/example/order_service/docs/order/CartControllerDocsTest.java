package com.example.order_service.docs.order;

import com.example.order_service.controller.CartController;
import com.example.order_service.docs.RestDocSupport;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.ProductInfo;
import com.example.order_service.dto.response.UnitPriceInfo;
import com.example.order_service.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;

public class CartControllerDocsTest extends RestDocSupport {

    private CartService cartService = Mockito.mock(CartService.class);

    @Override
    protected Object initController() {
        return new CartController(cartService);
    }

    @Test
    @DisplayName("장바구니 추가 API")
    void addCartItem(){
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        CartItemResponse cartItemResponse = createCartItemResponse();
        given(cartService.addItem(anyLong(), any(CartItemRequest.class)))
                .willReturn(cartItemResponse);
        //when
        //then
    }

    private HttpHeaders createUserHeader(String userRole){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
    }

    private CartItemResponse createCartItemResponse(){
        return CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("상품1")
                .thumbNailUrl("http://thumbnail.jpg")
                .quantity(2)
                .unitPriceInfo(
                        UnitPriceInfo.builder()
                                .originalPrice(3000)
                                .discountRate(10)
                                .discountAmount(300)
                                .discountedPrice(2700)
                                .build()
                )
                .lineTotal(5700)
                .options(List.of(new ItemOptionResponse("사이즈", "XL")))
                .build();

    }
}

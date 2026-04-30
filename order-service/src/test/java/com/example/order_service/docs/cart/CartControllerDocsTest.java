package com.example.order_service.docs.cart;

import com.example.order_service.api.cart.controller.CartController;
import com.example.order_service.api.cart.controller.dto.request.CartRequest;
import com.example.order_service.api.cart.controller.dto.response.CartResponse;
import com.example.order_service.api.cart.facade.CartFacade;
import com.example.order_service.api.cart.facade.dto.command.CartCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemStatus;
import com.example.order_service.api.cart.facade.dto.result.CartResult;
import com.example.order_service.docs.descriptor.CartDescriptor;
import com.example.order_service.support.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CartControllerDocsTest extends RestDocSupport {

    private CartFacade cartFacade = Mockito.mock(CartFacade.class);

    @Override
    protected String getTag() {
        return "Cart";
    }

    private static final String TAG = "CART";

    @Override
    protected Object initController() {
        return new CartController(cartFacade);
    }

    @Test
    @DisplayName("장바구니 추가 API")
    void addCartItem() throws Exception {
        //given
        CartRequest.Item item = CartRequest.Item.builder()
                .productVariantId(1L)
                .quantity(2)
                .build();
        CartRequest.AddItems request = CartRequest.AddItems.builder()
                .items(List.of(item))
                .build();

        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        CartResult.Cart result = createCartAddResult();
        given(cartFacade.addItems(any(CartCommand.AddItems.class)))
                .willReturn(result);
        CartResponse.Cart response = CartResponse.Cart.from(result);
        //when
        //then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("02-cart-01-add-cartItem",
                        "장바구니 상품 추가",
                        "장바구니에 상품을 추가",
                        CartDescriptor.getAddCartItemRequest(),
                        CartDescriptor.getCartItemResponse())
                );
    }

    @Test
    @DisplayName("장바구니 목록 조회")
    void addAllCartItem() throws Exception {
        //given
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        CartResult.Cart result = createCartResult();
        CartResponse.Cart response = CartResponse.Cart.from(result);
        given(cartFacade.getCartDetails(anyLong()))
                .willReturn(result);

        //when
        //then
        mockMvc.perform(get("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("02-cart-02-get-list",
                        "장바구니 목록 조회",
                        "장바구니 상품 목록을 조회한다",
                        CartDescriptor.getCartItemResponse()));
    }

    @Test
    @DisplayName("장바구니 상품 삭제")
    void removeCartItem() throws Exception {
        //given
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        willDoNothing().given(cartFacade).removeCartItems(anyLong(), anyList());
        //when
        //then
        mockMvc.perform(delete("/carts")
                        .headers(roleUser)
                        .queryParam("cartItemIds", "1,2,3,4"))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(createSecuredDocumentWithQuery("02-cart-03-delete-item",
                        "장바구니 상품 삭제",
                        "장바구니 상품을 삭제한다",
                        parameterWithName("cartItemIds").description("삭제할 장바구니 상품 ID 목록 (콤마로 구분하여 전달)")));
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경")
    void updateQuantity() throws Exception {
        //given
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        CartRequest.UpdateQuantity request = CartRequest.UpdateQuantity.builder()
                .quantity(3)
                .build();
        CartResult.Update result = CartResult.Update.builder()
                .id(1L)
                .quantity(3)
                .build();
        given(cartFacade.updateCartItemQuantity(any(CartCommand.UpdateQuantity.class)))
                .willReturn(result);


        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(createSecuredDocument(
                                "02-cart-05-update-quantity",
                                "장바구니 상품 수량 변경",
                                "장바구니의 상품 수량을 변경한다",
                                CartDescriptor.getCartUpdateRequest(),
                                CartDescriptor.getUpdateCartResponse(),
                                parameterWithName("cartItemId").description("장바구니 상품 ID(장바구니 상품 식별자)")
                        )
                );
    }

    private HttpHeaders createUserHeader(String userRole) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
    }

    private CartResult.Cart createCartAddResult() {
        CartResult.CartItemResult cartResult = CartResult.CartItemResult.builder()
                .id(1L)
                .productId(1L)
                .productVariantId(1L)
                .productName("상품1")
                .status(CartItemStatus.AVAILABLE)
                .isAvailable(true)
                .thumbnail("/product/product/PROD1_thumbnail.jpg")
                .quantity(2)
                .price(
                        CartResult.CartItemPrice.builder()
                                .originalPrice(3000)
                                .discountAmount(300)
                                .discountedPrice(2700)
                                .discountRate(10)
                                .build()
                )
                .lineTotal(5400)
                .options(
                        List.of(
                                CartResult.CartItemOption.builder()
                                        .optionTypeName("사이즈")
                                        .optionValueName("XL")
                                        .build()
                        )
                )
                .build();
        return CartResult.Cart.builder().items(List.of(cartResult)).build();
    }

    private CartResult.Cart createCartResult() {
        CartResult.CartItemResult cartResult = CartResult.CartItemResult.builder()
                .id(1L)
                .productId(1L)
                .productVariantId(1L)
                .productName("상품1")
                .status(CartItemStatus.AVAILABLE)
                .isAvailable(true)
                .thumbnail("/product/product/PROD1_thumbnail.jpg")
                .quantity(2)
                .price(
                        CartResult.CartItemPrice.builder()
                                .originalPrice(3000)
                                .discountAmount(300)
                                .discountedPrice(2700)
                                .discountRate(10)
                                .build()
                )
                .lineTotal(5400)
                .options(
                        List.of(
                                CartResult.CartItemOption.builder()
                                        .optionTypeName("사이즈")
                                        .optionValueName("XL")
                                        .build()
                        )
                )
                .build();
        return CartResult.Cart.builder().items(List.of(cartResult))
                .build();
    }
}

package com.example.order_service.docs.cart;

import com.example.order_service.cart.api.CartController;
import com.example.order_service.cart.api.dto.request.AddCartItemsRequest;
import com.example.order_service.cart.api.dto.request.UpdateCartItemQuantityRequest;
import com.example.order_service.cart.application.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.dto.result.*;
import com.example.order_service.cart.application.facade.CartFacade;

import com.example.order_service.common.domain.vo.Money;
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
    @DisplayName("장바구니 추가")
    void addCartItem() throws Exception {
        //given
        AddCartItemsRequest.Item item = AddCartItemsRequest.Item.builder()
                .productVariantId(1L)
                .quantity(2)
                .build();
        AddCartItemsRequest request = AddCartItemsRequest.builder()
                .items(List.of(item))
                .build();

        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        //when
        //then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
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
        CartResult result = createCartResult();
        given(cartFacade.getCartDetails(anyLong()))
                .willReturn(result);

        //when
        //then
        mockMvc.perform(get("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
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
        willDoNothing().given(cartFacade).deleteCartItems(any(DeleteCartItemsCommand.class));
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
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(3)
                .build();
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
                                CartDescriptor.getCartItemResponse(),
                                parameterWithName("cartItemId").description("장바구니 상품 ID(장바구니 상품 식별자)")
                        )
                );
    }

    private CartResult createCartResult() {
        CartItemResult cartResult = CartItemResult.builder()
                .cartItemId(1L)
                .productId(1L)
                .productVariantId(1L)
                .productName("상품1")
                .status(CartItemAvailability.AVAILABLE)
                .thumbnail("/product/product/PROD1_thumbnail.jpg")
                .quantity(2)
                .price(
                        CartItemPrice.builder()
                                .originalPrice(Money.wons(3000L))
                                .discountAmount(Money.wons(300L))
                                .discountedPrice(Money.wons(2700L))
                                .discountRate(10)
                                .build()
                )
                .lineTotal(Money.wons(5400L))
                .options(
                        List.of(
                                CartItemOption.builder()
                                        .optionTypeName("사이즈")
                                        .optionValueName("XL")
                                        .build()
                        )
                )
                .build();
        return CartResult.builder().items(List.of(cartResult))
                .build();
    }
}

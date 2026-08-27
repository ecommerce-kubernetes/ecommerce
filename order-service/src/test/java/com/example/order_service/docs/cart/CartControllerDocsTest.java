package com.example.order_service.docs.cart;

import com.example.order_service.cart.adapter.in.web.CartController;
import com.example.order_service.cart.adapter.in.web.dto.request.AddCartItemsRequest;
import com.example.order_service.cart.adapter.in.web.dto.request.UpdateCartItemQuantityRequest;
import com.example.order_service.cart.application.service.CartFacade;
import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.service.dto.result.*;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.support.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static com.example.order_service.docs.descriptor.CartDescriptor.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CartControllerDocsTest extends RestDocSupport {

    private CartFacade cartFacade = Mockito.mock(CartFacade.class);

    @Override
    protected Object initController() {
        return new CartController(cartFacade);
    }

    @Test
    @DisplayName("장바구니 추가")
    void addCartItems() throws Exception {
        //given
        AddCartItemsRequest.Item item = AddCartItemsRequest.Item.builder()
                .productVariantId(1L)
                .quantity(2)
                .build();
        AddCartItemsRequest request = AddCartItemsRequest.builder()
                .items(List.of(item))
                .build();

        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        AddCartItemsResult addCartItemsResult = createAddCartItemsResult();
        given(cartFacade.addItems(any(AddCartItemsCommand.class)))
                .willReturn(addCartItemsResult);
        //when
        //then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "carts/add",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(addCartItemsRequest()),
                        responseFields(addCartItemsResponse())
                ));
    }

    @Test
    @DisplayName("장바구니 전체 항목 조회")
    void getCart() throws Exception {
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
                .andDo(document(
                        "carts/get",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(cartResponse())
                ));
    }

    @Test
    @DisplayName("장바구니 항목 조회")
    void getCartItem() throws Exception {
        //given
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        CartItemResult cartItemResult = createCartItemResult();
        given(cartFacade.getCartItemDetails(anyLong(), anyLong()))
                .willReturn(cartItemResult);
        //when
        //then
        mockMvc.perform(get("/carts/{cartItemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "carts/get-item",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(
                                parameterWithName("cartItemId")
                                        .description("장바구니 항목 ID(장바구니 항목 식별자)")
                        ),
                        responseFields(
                                cartItemResponse()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경")
    void updateQuantity() throws Exception {
        //given
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(3)
                .build();
        UpdateCartItemQuantityResult result = createUpdateCartItemQuantityResult();

        given(cartFacade.updateCartItemQuantity(any(UpdateCartItemQuantityCommand.class)))
                .willReturn(result);

        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "carts/update-quantity",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(
                                parameterWithName("cartItemId")
                                        .description("장바구니 항목 ID(장바구니 항목 식별자)")
                        ),
                        requestFields(
                                updateCartItemQuantityRequest()
                        ),
                        responseFields(
                                updateCartItemQuantityResponse()
                        ))
                );
    }

    @Test
    @DisplayName("장바구니 상품 삭제")
    void deleteCartItems() throws Exception {
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
                .andDo(document(
                        "carts/delete",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        requestHeaders(AUTH_HEADER),
                        queryParameters(
                                parameterWithName("cartItemIds")
                                        .description("장바구니 항목 ID(장바구니 항목 식별자")
                        )
                ));
    }

    private CartResult createCartResult() {
        CartItemResult cartItemResult = createCartItemResult();
        return CartResult.builder()
                .items(List.of(cartItemResult))
                .build();
    }

    private CartItemResult createCartItemResult() {
        return CartItemResult.builder()
                .cartItemId(1L)
                .status(CartItemAvailability.AVAILABLE)
                .productId(1L)
                .productVariantId(1L)
                .productName("청바지")
                .thumbnail("/product/product/jean_1.jpg")
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
    }

    private AddCartItemsResult createAddCartItemsResult() {
        AddCartItemsResult.AddedItemResult item = AddCartItemsResult.AddedItemResult.builder()
                .cartItemId(1L)
                .build();
        return AddCartItemsResult.builder()
                .items(List.of(item))
                .build();
    }

    private UpdateCartItemQuantityResult createUpdateCartItemQuantityResult() {
        return UpdateCartItemQuantityResult.builder()
                .cartItemId(1L)
                .build();
    }
}

package com.example.order_service.docs.cart;

import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.controller.CartController;
import com.example.order_service.controller.dto.UpdateQuantityRequest;
import com.example.order_service.docs.RestDocSupport;
import com.example.order_service.controller.dto.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.service.CartService;
import com.example.order_service.service.dto.AddCartItemDto;
import com.example.order_service.service.dto.UpdateQuantityDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class CartControllerDocsTest extends RestDocSupport {

    private CartService cartService = Mockito.mock(CartService.class);

    @Override
    protected Object initController() {
        return new CartController(cartService);
    }

    @Test
    @DisplayName("장바구니 추가 API")
    void addCartItem() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        CartItemResponse cartItemResponse = createCartItemResponse();
        given(cartService.addItem(any(AddCartItemDto.class)))
                .willReturn(cartItemResponse);
        //when
        //then
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(roleUser)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "add-cartItem",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                requestFields(
                                        fieldWithPath("productVariantId").description("상품 변형 ID").optional(),
                                        fieldWithPath("quantity").description("수량").optional()
                                ),
                                responseFields(
                                        fieldWithPath("id").description("장바구니 상품 ID(장바구니 상품 식별자)"),
                                        fieldWithPath("productId").description("상품 ID(상품 식별자)"),
                                        fieldWithPath("productName").description("상품 이름"),
                                        fieldWithPath("thumbNailUrl").description("상품 썸네일"),
                                        fieldWithPath("quantity").description("수량"),
                                        fieldWithPath("unitPrice.originalPrice").description("상품 원본 가격"),
                                        fieldWithPath("unitPrice.discountRate").description("상품 할인율"),
                                        fieldWithPath("unitPrice.discountAmount").description("상품 할인 금액"),
                                        fieldWithPath("unitPrice.discountedPrice").description("할인된 가격"),
                                        fieldWithPath("lineTotal").description("항목 총액 (상품 할인 가격 X 수량)"),
                                        fieldWithPath("options[].optionTypeName").description("상품 옵션 타입 (예: 사이즈)"),
                                        fieldWithPath("options[].optionValueName").description("상품 옵션 값 (예: XL)"),
                                        fieldWithPath("available").description("주문 가능 여부")
                                )
                        )
                );

    }
    
    @Test
    @DisplayName("장바구니 목록 조회")
    void addAllCartItem() throws Exception {
        //given
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        CartItemResponse cartItem = createCartItemResponse();

        CartResponse response = CartResponse.builder()
                .cartItems(List.of(cartItem))
                .cartTotalPrice(5700)
                .build();
        given(cartService.getCartItemList(any(UserPrincipal.class)))
                .willReturn(response);
        //when
        //then

        mockMvc.perform(get("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "getAllCartItem",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                responseFields(
                                        fieldWithPath("cartItems[].id").description("장바구니 상품 ID(장바구니 상품 식별자)"),
                                        fieldWithPath("cartItems[].productId").description("상품 ID(상품 식별자)"),
                                        fieldWithPath("cartItems[].productName").description("상품 이름"),
                                        fieldWithPath("cartItems[].thumbNailUrl").description("상품 썸네일"),
                                        fieldWithPath("cartItems[].quantity").description("수량"),
                                        fieldWithPath("cartItems[].unitPrice.originalPrice").description("상품 원본 가격"),
                                        fieldWithPath("cartItems[].unitPrice.discountRate").description("상품 할인율"),
                                        fieldWithPath("cartItems[].unitPrice.discountAmount").description("상품 할인 금액"),
                                        fieldWithPath("cartItems[].unitPrice.discountedPrice").description("할인된 가격"),
                                        fieldWithPath("cartItems[].lineTotal").description("항목 총액 (상품 할인 가격 X 수량)"),
                                        fieldWithPath("cartItems[].options[].optionTypeName").description("상품 옵션 타입 (예: 사이즈)"),
                                        fieldWithPath("cartItems[].options[].optionValueName").description("상품 옵션 값 (예: XL)"),
                                        fieldWithPath("cartItems[].available").description("주문 가능 여부"),
                                        fieldWithPath("cartTotalPrice").description("장바구니 총액")
                                )
                        )
                );
    }

    @Test
    @DisplayName("장바구니 상품 삭제")
    void removeCartItem() throws Exception {
        //given
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        willDoNothing().given(cartService).deleteCartItemById(anyLong(), anyLong());
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "removeCartItem",
                        requestHeaders(
                                headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                        ),
                        pathParameters(
                                parameterWithName("cartItemId").description("장바구니 상품 ID(장바구니 상품 식별자)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 비우기")
    void clearCart() throws Exception {
        //given
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        willDoNothing().given(cartService).clearAllCartItems(anyLong());
        //when
        //then
        mockMvc.perform(delete("/carts")
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "clearCart",
                        requestHeaders(
                                headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경")
    void updateQuantity() throws Exception {
        //given
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        UpdateQuantityRequest request = UpdateQuantityRequest.builder()
                .quantity(3)
                .build();
        CartItemResponse cartItemResponse = createCartItemResponse();
        given(cartService.updateCartItemQuantity(any(UpdateQuantityDto.class)))
                .willReturn(cartItemResponse);
        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("updateCartItemQuantity",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                requestFields(
                                        fieldWithPath("quantity").description("변경할 수량").optional()
                                ),
                                responseFields(
                                        fieldWithPath("id").description("장바구니 상품 ID(장바구니 상품 식별자)"),
                                        fieldWithPath("productId").description("상품 ID(상품 식별자)"),
                                        fieldWithPath("productName").description("상품 이름"),
                                        fieldWithPath("thumbNailUrl").description("상품 썸네일"),
                                        fieldWithPath("quantity").description("수량"),
                                        fieldWithPath("unitPrice.originalPrice").description("상품 원본 가격"),
                                        fieldWithPath("unitPrice.discountRate").description("상품 할인율"),
                                        fieldWithPath("unitPrice.discountAmount").description("상품 할인 금액"),
                                        fieldWithPath("unitPrice.discountedPrice").description("할인된 가격"),
                                        fieldWithPath("lineTotal").description("항목 총액 (상품 할인 가격 X 수량)"),
                                        fieldWithPath("options[].optionTypeName").description("상품 옵션 타입 (예: 사이즈)"),
                                        fieldWithPath("options[].optionValueName").description("상품 옵션 값 (예: XL)"),
                                        fieldWithPath("available").description("주문 가능 여부")
                                )
                        )
                );
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
                .unitPrice(
                        UnitPrice.builder()
                                .originalPrice(3000)
                                .discountRate(10)
                                .discountAmount(300)
                                .discountedPrice(2700)
                                .build()
                )
                .lineTotal(5700)
                .options(List.of(
                        ItemOptionResponse.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                ))
                .build();

    }
}

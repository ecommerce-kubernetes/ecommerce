package com.example.order_service.api.cart.controller;

import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartResponse;
import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.support.ControllerTestSupport;
import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@Import(TestSecurityConfig.class)
class CartControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("장바구니에 상품을 추가한다")
    @WithCustomMockUser
    void addCartItem() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        CartItemResponse response = createCartItemResponse().build();
        given(cartFacade.addItem(any(AddCartItemCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할 때는 유저 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void addCartItemWithAdminPrincipal() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        //when
        //then
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 장바구니에 상품을 추가할 수 없다")
    void addCartItem_unAuthorized() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        //when
        //then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("장바구니에 상품 추가시 유효성 검증에 실패하면 400 에러를 반환한다")
    @MethodSource("provideInvalidAddRequest")
    @WithCustomMockUser
    void addCartItem_Validation(String description, CartItemRequest request, String errorMessage) throws Exception {
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니 목록을 조회한다")
    @WithCustomMockUser
    void getAllCartItem() throws Exception {
        //given
        CartItemResponse cartItemResponse = createCartItemResponse().build();
        CartResponse response = CartResponse.builder()
                .cartItems(List.of(cartItemResponse))
                .cartTotalPrice(cartItemResponse.getLineTotal())
                .build();

        given(cartFacade.getCartDetails(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/carts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("장바구니 목록을 조회할때는 유저 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void getAllCartItemWithAdminPrincipal() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/carts")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("로그인 하지 않은 회원은 장바구니를 조회할 수 없다")
    void getAllCartItem_unAuthorized() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/carts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니에서 상품을 삭제한다")
    @WithCustomMockUser
    void deleteCartItem() throws Exception {
        //given
        willDoNothing().given(cartFacade).removeCartItem(anyLong(), anyLong());
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("장바구에서 상품을 삭제할때는 유저 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void deleteCartItemWithAdminPrincipal() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 장바구니에 상품을 삭제할 수 없다")
    void deleteCartItem_unAuthorized() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }

    @Test
    @DisplayName("장바구니 상품 전체를 삭제한다")
    @WithCustomMockUser
    void clearCart() throws Exception {
        //given
        willDoNothing().given(cartFacade).clearCart(anyLong());
        //when
        //then
        mockMvc.perform(delete("/carts")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("장바구니 비우기는 유저 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void clearCart_Admin_Role() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/carts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 장바구니를 비울 수 없다")
    void clearCart_unAuthorized() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/carts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니에 담긴 상품의 수량을 수정한다")
    @WithCustomMockUser
    void updateQuantity() throws Exception {
        //given
        UpdateQuantityRequest request = UpdateQuantityRequest.builder()
                .quantity(3)
                .build();

        CartItemResponse response = createCartItemResponse()
                .quantity(3).lineTotal(2700L * 3).build();
        given(cartFacade.updateCartItemQuantity(any(UpdateQuantityCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("장바구니 상품 수량 수정은 유저 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void updateQuantity_Admin_role() throws Exception {
        //given
        UpdateQuantityRequest request = UpdateQuantityRequest.builder()
                .quantity(3)
                .build();
        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 장바구니 상품의 수량을 수정할 수 없다")
    void updateQuantity_unAuthorized() throws Exception {
        //given
        UpdateQuantityRequest request = UpdateQuantityRequest.builder()
                .quantity(3)
                .build();
        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }


    @Test
    @DisplayName("장바구니 상품 수량을 변경할때 수량은 1이상 이여야 한다")
    @WithCustomMockUser
    void updateQuantityWithQuantityLessThan1() throws Exception {
        //given
        UpdateQuantityRequest request = UpdateQuantityRequest.builder()
                .quantity(0)
                .build();
        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("quantity는 1이상 이여야 합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }

    private CartItemResponse.CartItemResponseBuilder createCartItemResponse() {
        return CartItemResponse
                .builder()
                .id(1L)
                .productId(1L)
                .productVariantId(1L)
                .productName("상품1")
                .thumbnailUrl("http://thumbnail.jpg")
                .quantity(1)
                .price(
                        CartItemResponse.CartItemPrice.builder()
                                .originalPrice(3000L)
                                .discountRate(10)
                                .discountAmount(300L)
                                .discountedPrice(2700L)
                                .build()
                )
                .lineTotal(2700L)
                .options(
                        List.of(CartItemResponse.CartItemOption
                                .builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                        )
                )
                .isAvailable(true);
    }

    private static Stream<Arguments> provideInvalidAddRequest() {
        return Stream.of(
                Arguments.of("상품 Id null",
                        CartItemRequest.builder().productVariantId(null).quantity(1).build(), "productVariantId는 필수값입니다"),
                Arguments.of("수량 null",
                        CartItemRequest.builder().productVariantId(1L).quantity(null).build(), "quantity는 필수값입니다"),
                Arguments.of("요청 수량 0이하",
                        CartItemRequest.builder().productVariantId(1L).quantity(0).build(), "quantity는 1이상 이여야 합니다")
        );
    }

}
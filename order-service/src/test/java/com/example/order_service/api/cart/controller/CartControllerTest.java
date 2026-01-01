package com.example.order_service.api.cart.controller;

import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.support.ControllerTestSupport;
import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.servlet.ResultMatcher;

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
        given(cartApplicationService.addItem(any(AddCartItemDto.class)))
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
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("권한이 부족합니다"))
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
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("장바구니 상품 추가 중 서비스 예외 발생시 에러 응답을 반환한다")
    @MethodSource("provideServiceExceptions")
    @WithCustomMockUser
    void addCartItem_exceptions(String description, Exception e, ResultMatcher expectedStatus, String errorCode, String expectedMessage) throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        willThrow(e)
                .given(cartApplicationService).addItem(any(AddCartItemDto.class));
        //when, then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(expectedStatus)
                .andExpect(jsonPath("$.error").value(errorCode))
                .andExpect(jsonPath("$.message").value(expectedMessage))
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

        given(cartApplicationService.getCartDetails(any(UserPrincipal.class)))
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
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니에서 상품을 삭제한다")
    @WithCustomMockUser
    void deleteCartItem() throws Exception {
        //given
        willDoNothing().given(cartApplicationService).removeCartItem(any(UserPrincipal.class), anyLong());
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("장바구에서 상품을 삭제할때는 유저 권한이여야 한다")
    void deleteCartItemWithAdminPrincipal() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }

    @Test
    @DisplayName("장바구니에 없는 상품을 삭제하려 시도하면 404 예외 응답을 반환한다")
    @WithCustomMockUser
    void deleteCartItemThrowNotFound() throws Exception {
        //given
        willThrow(new NotFoundException("장바구니에 해당 상품을 찾을 수 없습니다"))
                .given(cartApplicationService).removeCartItem(any(UserPrincipal.class), anyLong());
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("장바구니에 해당 상품을 찾을 수 없습니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }

    @Test
    @DisplayName("장바구니 상품 전체를 삭제한다")
    @WithCustomMockUser
    void clearCart() throws Exception {
        //given
        willDoNothing().given(cartApplicationService).clearCart(any(UserPrincipal.class));
        //when
        //then
        mockMvc.perform(delete("/carts")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
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
        given(cartApplicationService.updateCartItemQuantity(any(UpdateQuantityDto.class)))
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
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("quantity는 1이상 이여야 합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }

    @Test
    @DisplayName("장바구니 상품 수량을 변경할때 장바구니에서 상품을 찾을 수 없는 경우 404 예외 응답을 반환한다")
    @WithCustomMockUser
    void updateQuantityThrowNotFound() throws Exception {
        //given
        UpdateQuantityRequest request = UpdateQuantityRequest.builder()
                .quantity(5)
                .build();

        willThrow(new NotFoundException("장바구니에 해당 상품을 찾을 수 없습니다"))
                .given(cartApplicationService).updateCartItemQuantity(any(UpdateQuantityDto.class));

        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("장바구니에 해당 상품을 찾을 수 없습니다"))
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

    private static Stream<Arguments> provideServiceExceptions() {
        return Stream.of(
                Arguments.of("존재하지 않는 상품", new NotFoundException("해당 상품을 찾을 수 없습니다"),
                        status().isNotFound(), "NOT_FOUND", "해당 상품을 찾을 수 없습니다"),
                Arguments.of("상품 서비스 미응답", new UnavailableServiceException("상품을 불러올 수 없습니다 잠시 후 다시 시도해 주세요"),
                        status().isServiceUnavailable(), "SERVICE_UNAVAILABLE", "상품을 불러올 수 없습니다 잠시 후 다시 시도해 주세요"),
                Arguments.of("상품 서비스 오류", new InternalServerException("장바구니 상품 추가중 서버에 오류가 발생했습니다"),
                        status().isInternalServerError(), "INTERNAL_SERVER_ERROR", "장바구니 상품 추가중 서버에 오류가 발생했습니다")
        );
    }
}
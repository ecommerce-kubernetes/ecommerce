package com.example.order_service.api.cart.controller;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.config.TestConfig;
import com.example.order_service.support.ControllerTestSupport;
import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.support.security.config.TestSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({TestConfig.class, TestSecurityConfig.class})
class CartControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("장바구니에 상품을 추가한다")
    @WithCustomMockUser
    void addCartItem() throws Exception {
        Long productVariantId = 1L;
        int quantity = 2;
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();

        CartItemResponse.CartItemPrice cartItemPrice = createCartItemPrice(3000, 10);

        CartItemResponse.CartItemOption cartItemOption = CartItemResponse.CartItemOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();

        CartItemResponse response = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productVariantId(1L)
                .productName("상품1")
                .thumbnailUrl("http://thumbNail.jpg")
                .quantity(quantity)
                .price(cartItemPrice)
                .lineTotal(cartItemPrice.getDiscountedPrice() * quantity)
                .options(List.of(cartItemOption))
                .isAvailable(true)
                .build();

        given(cartApplicationService.addItem(any(AddCartItemDto.class)))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.productId").value(response.getProductId()))
                .andExpect(jsonPath("$.productVariantId").value(response.getProductVariantId()))
                .andExpect(jsonPath("$.productName").value(response.getProductName()))
                .andExpect(jsonPath("$.thumbnailUrl").value(response.getThumbnailUrl()))
                .andExpect(jsonPath("$.quantity").value(response.getQuantity()))
                .andExpect(jsonPath("$.price.originalPrice").value(response.getPrice().getOriginalPrice()))
                .andExpect(jsonPath("$.price.discountRate").value(response.getPrice().getDiscountRate()))
                .andExpect(jsonPath("$.price.discountAmount").value(response.getPrice().getDiscountAmount()))
                .andExpect(jsonPath("$.price.discountedPrice").value(response.getPrice().getDiscountedPrice()))
                .andExpect(jsonPath("$.available").value(response.isAvailable()));
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

    @Test
    @DisplayName("장바구니에 상품을 추가할때 productVariantId는 필수값이다")
    @WithCustomMockUser
    void addCartItemWithNoProductVariantId() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .quantity(1).build();
        //when
        //then
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("productVariantId는 필수값입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 quantity는 필수값이다")
    @WithCustomMockUser
    void addCartItemWithNoQuantity() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .build();
        //when
        //then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("quantity는 필수값입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 수량은 1이상 이여야 한다")
    @WithCustomMockUser
    void addCartItemWithQuantityLessThan1() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(0)
                .build();
        //when
        //then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("quantity는 1이상 이여야 합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 CartApplicationService 에서 NotFoundException이 던져지면 404 에러 응답을 반환한다")
    @WithCustomMockUser
    void addCartItem_When_NotFoundException_Thrown_In_CartApplicationService() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        willThrow(new NotFoundException("해당 상품을 찾을 수 없습니다"))
                .given(cartApplicationService).addItem(any(AddCartItemDto.class));
        //when
        //then
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 상품을 찾을 수 없습니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }
    
    @Test
    @DisplayName("장바구니에 상품을 추가할때 CartApplicationService 에서 UnavailableServiceException이 던져지면 503 에러 응답을 반환한다")
    @WithCustomMockUser
    void addCartItem_When_UnavailableServiceException_Thrown_In_CartApplicationService() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        willThrow(new UnavailableServiceException("상품을 불러올 수 없습니다 잠시 후 다시 시도해 주세요"))
                .given(cartApplicationService).addItem(any(AddCartItemDto.class));
        //when
        //then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("상품을 불러올 수 없습니다 잠시 후 다시 시도해 주세요"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 CartApplicationService 에서 InternalServerException이 던져지면 500 에러 응답을 반환한다")
    @WithCustomMockUser
    void addCartItem_When_InternalServerException_Thrown_In_CartApplicationService() throws Exception {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        willThrow(new InternalServerException("장바구니 상품 추가중 서버에 오류가 발생했습니다"))
                .given(cartApplicationService).addItem(any(AddCartItemDto.class));
        //when
        //then
        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("장바구니 상품 추가중 서버에 오류가 발생했습니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니 목록을 조회한다")
    @WithCustomMockUser
    void getAllCartItem() throws Exception {
        //given
        CartItemResponse.CartItemPrice cartItemPrice = createCartItemPrice(3000, 10);
        CartItemResponse.CartItemOption cartItemOption = CartItemResponse.CartItemOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();

        CartItemResponse cartItem = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("상품1")
                .thumbnailUrl("http://thumbnail.jpg")
                .quantity(2)
                .price(cartItemPrice)
                .lineTotal(cartItemPrice.getDiscountedPrice() * 2)
                .options(List.of(cartItemOption))
                .isAvailable(true)
                .build();

        CartResponse response = CartResponse.builder()
                .cartItems(List.of(cartItem))
                .cartTotalPrice(cartItem.getLineTotal())
                .build();

        given(cartApplicationService.getCartDetails(any(UserPrincipal.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/carts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItems[0].id").value(1L))
                .andExpect(jsonPath("$.cartItems[0].productId").value(1L))
                .andExpect(jsonPath("$.cartItems[0].productName").value("상품1"))
                .andExpect(jsonPath("$.cartItems[0].thumbnailUrl").value("http://thumbnail.jpg"))
                .andExpect(jsonPath("$.cartItems[0].quantity").value(2))
                .andExpect(jsonPath("$.cartItems[0].price.originalPrice").value(3000))
                .andExpect(jsonPath("$.cartItems[0].price.discountRate").value(10))
                .andExpect(jsonPath("$.cartItems[0].price.discountAmount").value(300))
                .andExpect(jsonPath("$.cartItems[0].price.discountedPrice").value(2700))
                .andExpect(jsonPath("$.cartItems[0].lineTotal").value(5400))
                .andExpect(jsonPath("$.cartItems[0].available").value(true))
                .andExpect(jsonPath("$.cartItems.length()").value(1))
                .andExpect(jsonPath("$.cartTotalPrice").value(5400));
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

        CartItemResponse.CartItemPrice cartItemPrice = createCartItemPrice(3000, 10);
        CartItemResponse.CartItemOption cartItemOption = CartItemResponse.CartItemOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();

        CartItemResponse response = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("상품1")
                .thumbnailUrl("http://thumbNail.jpg")
                .quantity(3)
                .price(cartItemPrice)
                .lineTotal(cartItemPrice.getDiscountedPrice() * 3)
                .options(List.of(cartItemOption))
                .isAvailable(true)
                .build();
        given(cartApplicationService.updateCartItemQuantity(any(UpdateQuantityDto.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.productId").value(response.getProductId()))
                .andExpect(jsonPath("$.productName").value(response.getProductName()))
                .andExpect(jsonPath("$.thumbnailUrl").value(response.getThumbnailUrl()))
                .andExpect(jsonPath("$.quantity").value(response.getQuantity()))
                .andExpect(jsonPath("$.price.originalPrice").value(response.getPrice().getOriginalPrice()))
                .andExpect(jsonPath("$.price.discountRate").value(response.getPrice().getDiscountRate()))
                .andExpect(jsonPath("$.price.discountAmount").value(response.getPrice().getDiscountAmount()))
                .andExpect(jsonPath("$.price.discountedPrice").value(response.getPrice().getDiscountedPrice()))
                .andExpect(jsonPath("$.available").value(true));
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

    private CartItemResponse.CartItemPrice createCartItemPrice(long originalPrice, int discountRate){
        long discountAmount = calcDiscountAmount(originalPrice, discountRate);
        return CartItemResponse.CartItemPrice.builder()
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .discountedPrice(originalPrice - discountAmount)
                .build();
    }

    private long calcDiscountAmount(long originalPrice, int discountRate){
        return originalPrice * discountRate / 100;
    }
}
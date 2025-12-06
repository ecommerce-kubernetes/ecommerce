package com.example.order_service.api.cart.controller;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.config.TestConfig;
import com.example.order_service.support.ControllerTestSupport;
import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.service.SseConnectionService;
import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({TestConfig.class})
class CartControllerTest extends ControllerTestSupport {

    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    SseConnectionService sseConnectionService;

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
        UnitPrice unitPrice = createUnitPrice(3000, 10);

        ItemOptionResponse itemOption = ItemOptionResponse.builder()
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
                .unitPrice(unitPrice)
                .lineTotal(unitPrice.getDiscountedPrice() * 2)
                .options(List.of(itemOption))
                .isAvailable(true)
                .build();

        given(cartApplicationService.addItem(any(AddCartItemDto.class)))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.productId").value(response.getProductId()))
                .andExpect(jsonPath("$.productVariantId").value(response.getProductVariantId()))
                .andExpect(jsonPath("$.productName").value(response.getProductName()))
                .andExpect(jsonPath("$.thumbnailUrl").value(response.getThumbnailUrl()))
                .andExpect(jsonPath("$.quantity").value(response.getQuantity()))
                .andExpect(jsonPath("$.unitPrice.originalPrice").value(response.getUnitPrice().getOriginalPrice()))
                .andExpect(jsonPath("$.unitPrice.discountRate").value(response.getUnitPrice().getDiscountRate()))
                .andExpect(jsonPath("$.unitPrice.discountAmount").value(response.getUnitPrice().getDiscountAmount()))
                .andExpect(jsonPath("$.unitPrice.discountedPrice").value(response.getUnitPrice().getDiscountedPrice()))
                .andExpect(jsonPath("$.available").value(response.isAvailable()));
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
                .with(SecurityMockMvcRequestPostProcessors.csrf())
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
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
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
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
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
                .content(objectMapper.writeValueAsString(request))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
        UnitPrice unitPrice = createUnitPrice(3000, 10);
        ItemOptionResponse itemOption = ItemOptionResponse.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();

        CartItemResponse cartItem = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("상품1")
                .thumbnailUrl("http://thumbnail.jpg")
                .quantity(2)
                .unitPrice(unitPrice)
                .lineTotal(unitPrice.getDiscountedPrice() * 2)
                .options(List.of(itemOption))
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
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItems[0].id").value(1L))
                .andExpect(jsonPath("$.cartItems[0].productId").value(1L))
                .andExpect(jsonPath("$.cartItems[0].productName").value("상품1"))
                .andExpect(jsonPath("$.cartItems[0].thumbnailUrl").value("http://thumbnail.jpg"))
                .andExpect(jsonPath("$.cartItems[0].quantity").value(2))
                .andExpect(jsonPath("$.cartItems[0].unitPrice.originalPrice").value(3000))
                .andExpect(jsonPath("$.cartItems[0].unitPrice.discountRate").value(10))
                .andExpect(jsonPath("$.cartItems[0].unitPrice.discountAmount").value(300))
                .andExpect(jsonPath("$.cartItems[0].unitPrice.discountedPrice").value(2700))
                .andExpect(jsonPath("$.cartItems[0].lineTotal").value(5400))
                .andExpect(jsonPath("$.cartItems[0].available").value(true))
                .andExpect(jsonPath("$.cartItems.length()").value(1))
                .andExpect(jsonPath("$.cartTotalPrice").value(5400));
    }

    @Test
    @DisplayName("장바구니에서 상품을 삭제한다")
    @WithCustomMockUser
    void deleteCartItem() throws Exception {
        //given
        willDoNothing().given(cartService).deleteCartItemById(any(UserPrincipal.class), anyLong());
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("장바구니에 없는 상품을 삭제하려 시도하면 404 예외 응답을 반환한다")
    @WithCustomMockUser
    void deleteCartItemThrowNotFound() throws Exception {
        //given
        willThrow(new NotFoundException("장바구니에 해당 상품을 찾을 수 없습니다"))
                .given(cartService).deleteCartItemById(any(UserPrincipal.class), anyLong());
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
        willDoNothing().given(cartService).clearAllCartItems(any(UserPrincipal.class));
        //when
        //then
        mockMvc.perform(delete("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
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

        UnitPrice unitPrice = createUnitPrice(3000, 10);

        ItemOptionResponse itemOption = ItemOptionResponse.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();

        CartItemResponse response = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("상품1")
                .thumbnailUrl("http://thumbNail.jpg")
                .quantity(3)
                .unitPrice(unitPrice)
                .lineTotal(unitPrice.getDiscountedPrice() * 3)
                .options(List.of(itemOption))
                .isAvailable(true)
                .build();
        given(cartService.updateCartItemQuantity(any(UpdateQuantityDto.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.productId").value(response.getProductId()))
                .andExpect(jsonPath("$.productName").value(response.getProductName()))
                .andExpect(jsonPath("$.thumbnailUrl").value(response.getThumbnailUrl()))
                .andExpect(jsonPath("$.quantity").value(response.getQuantity()))
                .andExpect(jsonPath("$.unitPrice.originalPrice").value(response.getUnitPrice().getOriginalPrice()))
                .andExpect(jsonPath("$.unitPrice.discountRate").value(response.getUnitPrice().getDiscountRate()))
                .andExpect(jsonPath("$.unitPrice.discountAmount").value(response.getUnitPrice().getDiscountAmount()))
                .andExpect(jsonPath("$.unitPrice.discountedPrice").value(response.getUnitPrice().getDiscountedPrice()))
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
                .content(objectMapper.writeValueAsString(request))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
                .given(cartService).updateCartItemQuantity(any(UpdateQuantityDto.class));

        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("장바구니에 해당 상품을 찾을 수 없습니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/carts/1"));

    }

    private UnitPrice createUnitPrice(long originalPrice, int discountRate){
        long discountAmount = calcDiscountAmount(originalPrice, discountRate);
        return UnitPrice.builder()
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
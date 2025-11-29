package com.example.order_service.controller.cart;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.ErrorResponseEntityFactory;
import com.example.order_service.config.TestConfig;
import com.example.order_service.controller.ControllerTestSupport;
import com.example.order_service.controller.dto.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.service.SseConnectionService;
import com.example.order_service.service.dto.AddCartItemDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.util.ControllerTestHelper.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({ErrorResponseEntityFactory.class, TestConfig.class})
class CartControllerTest extends ControllerTestSupport {
    private static final String BASE_PATH = "/carts";
    private static final String ID_PATH = BASE_PATH + "/1";

    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    SseConnectionService sseConnectionService;

    @Test
    @DisplayName("장바구니에 상품을 추가한다")
    @WithMockUser
    void addCartItem() throws Exception {
        Long productVariantId = 1L;
        int quantity = 2;
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();

        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        UnitPrice unitPrice = createUnitPrice(3000, 10);

        ItemOptionResponse itemOption = ItemOptionResponse.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();

        CartItemResponse response = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("상품1")
                .thumbNailUrl("http://thumbNail.jpg")
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineTotal(unitPrice.getDiscountedPrice() * 2)
                .options(List.of(itemOption))
                .isAvailable(true)
                .build();

        given(cartService.addItem(any(AddCartItemDto.class)))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(roleUser)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.productId").value(response.getProductId()))
                .andExpect(jsonPath("$.productName").value(response.getProductName()))
                .andExpect(jsonPath("$.thumbNailUrl").value(response.getThumbNailUrl()))
                .andExpect(jsonPath("$.quantity").value(response.getQuantity()))
                .andExpect(jsonPath("$.unitPrice.originalPrice").value(response.getUnitPrice().getOriginalPrice()))
                .andExpect(jsonPath("$.unitPrice.discountRate").value(response.getUnitPrice().getDiscountRate()))
                .andExpect(jsonPath("$.unitPrice.discountAmount").value(response.getUnitPrice().getDiscountAmount()))
                .andExpect(jsonPath("$.unitPrice.discountedPrice").value(response.getUnitPrice().getDiscountedPrice()));
    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-실패(헤더 없음)")
    void getAllCartItemTest_noHeader() throws Exception {
        ResultActions perform = performWithBody(mockMvc, get(BASE_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                BASE_PATH);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-성공")
    void removeCartItemTest_success() throws Exception {
        doNothing().when(cartService).deleteCartItemById(anyLong(), anyLong());
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, delete(ID_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(헤더 없음)")
    void removeCartItemTest_noHeader() throws Exception {
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                ID_PATH
        );
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(장바구니 상품을 찾을 수 없음)")
    void removeCartItemTest_notFound_cartItem() throws Exception {
        doThrow(new NotFoundException(getMessage(CART_ITEM_NOT_FOUND)))
                .when(cartService).deleteCartItemById(anyLong(), anyLong());
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CART_ITEM_NOT_FOUND), ID_PATH);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(삭제할 권한이 없음)")
    void removeCartItemTest_noPermission() throws Exception {
        doThrow(new NoPermissionException(getMessage(CART_ITEM_NO_PERMISSION)))
                .when(cartService).deleteCartItemById(anyLong(), anyLong());

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isForbidden(), getMessage(FORBIDDEN),
                getMessage(CART_ITEM_NO_PERMISSION), ID_PATH);
    }

    @Test
    @DisplayName("장바구니 비우기 테스트-성공")
    void clearCartTest_success() throws Exception {
        doNothing().when(cartService).clearAllCartItems(anyLong());
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, delete(ID_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("장바구니 비우기 테스트-실패(헤더 없음)")
    void clearCartTest_noHeader() throws Exception {
        ResultActions perform = performWithBody(mockMvc, delete(BASE_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                BASE_PATH
        );
    }

    private HttpHeaders createUserHeader(String userRole){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
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
package com.example.order_service.cart.api;

import com.example.order_service.cart.api.dto.request.AddCartItemsRequest;

import com.example.order_service.cart.api.dto.request.UpdateCartItemQuantityRequest;
import com.example.order_service.cart.application.service.CartFacade;
import com.example.order_service.cart.application.service.dto.command.CartCommand;
import com.example.order_service.cart.application.service.dto.result.CartResult;
import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.support.annotation.WithCustomMockUser;
import com.example.order_service.support.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CartFacade cartFacade;

    @Nested
    @DisplayName("장바구니 상품 추가")
    class AddCartItems {

        @Test
        @DisplayName("장바구니에 상품을 추가한다")
        @WithCustomMockUser
        void addCartItem() throws Exception {
            //given
            AddCartItemsRequest request = Instancio.create(AddCartItemsRequest.class);
            CartResult.CartItemResult item = Instancio.create(CartResult.CartItemResult.class);
            CartResult.Cart result = Instancio.of(CartResult.Cart.class)
                    .set(field("items"), List.of(item))
                    .create();
            given(cartFacade.addItems(any(CartCommand.AddItems.class)))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(post("/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items").isNotEmpty())
                    .andExpect(jsonPath("$.items[0].cartItemId").value(item.id()))
                    .andExpect(jsonPath("$.items[0].status").value(item.status().name()));
        }

        @Test
        @DisplayName("장바구니에 상품을 추가할 때는 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void addCartItemWithAdminPrincipal() throws Exception {
            //given
            AddCartItemsRequest request = Instancio.create(AddCartItemsRequest.class);
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
            AddCartItemsRequest request = Instancio.create(AddCartItemsRequest.class);
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
        void addCartItem_Validation(String description, AddCartItemsRequest request, String errorMessage) throws Exception {
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

        private static Stream<Arguments> provideInvalidAddRequest() {
            return Stream.of(
                    Arguments.of("추가할 아이템 리스트가 0",
                            AddCartItemsRequest.builder().items(List.of()).build(),
                            "장바구니에 추가할 상품이 하나 이상 있어야 합니다."),
                    Arguments.of("상품 Id null",
                            AddCartItemsRequest.builder().items(
                                            List.of(AddCartItemsRequest.Item.builder()
                                                    .quantity(1)
                                                    .build()))
                                    .build(),
                            "productVariantId는 필수값입니다"),
                    Arguments.of("수량 null",
                            AddCartItemsRequest.builder().items(
                                            List.of(AddCartItemsRequest.Item.builder()
                                                    .productVariantId(1L)
                                                    .build()))
                                    .build(),
                            "quantity는 필수값입니다"),
                    Arguments.of("요청 수량 0이하",
                            AddCartItemsRequest.builder().items(
                                            List.of(AddCartItemsRequest.Item.builder()
                                                    .productVariantId(1L)
                                                    .quantity(0)
                                                    .build()))
                                    .build(),
                            "quantity는 1이상 이여야 합니다")
            );
        }
    }

    @Nested
    @DisplayName("장바구니 목록 조회")
    class GetCart {
        @Test
        @DisplayName("장바구니 목록을 조회한다")
        @WithCustomMockUser
        void getAllCartItem() throws Exception {
            //given
            CartResult.CartItemResult item = Instancio.create(CartResult.CartItemResult.class);
            CartResult.Cart result = Instancio.of(CartResult.Cart.class)
                    .set(field("items"), List.of(item))
                    .create();
            given(cartFacade.getCartDetails(anyLong()))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(get("/carts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isNotEmpty())
                    .andExpect(jsonPath("$.items[0].cartItemId").value(item.id()))
                    .andExpect(jsonPath("$.items[0].status").value(item.status().name()));
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
    }

    @Nested
    @DisplayName("장바구니 상품 삭제")
    class DeleteCartItems {

        @Test
        @DisplayName("장바구니에서 상품을 삭제한다")
        @WithCustomMockUser
        void deleteCartItems() throws Exception {
            //given
            willDoNothing().given(cartFacade).removeCartItems(anyLong(), anyList());
            //when
            //then
            mockMvc.perform(delete("/carts")
                            .param("cartItemIds", "1,2,3,4")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }


        @Test
        @DisplayName("장바구에서 상품을 삭제할때는 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void deleteCartItemsWithAdminPrincipal() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(delete("/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("cartItemIds", "1,2,3,4"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 장바구니에 상품을 삭제할 수 없다")
        void deleteCartItems_unAuthorized() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(delete("/carts")
                            .param("cartItemIds", "1,2,3,4")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts"));
        }
    }

    @Nested
    @DisplayName("장바구니 상품 수량 변경")
    class UpdateQuantity {

        @Test
        @DisplayName("장바구니에 담긴 상품 수량을 수정한다")
        @WithCustomMockUser
        void updateQuantity() throws Exception {
            //given
            UpdateCartItemQuantityRequest request = Instancio.create(UpdateCartItemQuantityRequest.class);
            CartResult.CartItemResult cartItemResult = Instancio.create(CartResult.CartItemResult.class);
            CartResult.Cart result = Instancio.of(CartResult.Cart.class)
                    .set(field("items"), List.of(cartItemResult))
                    .create();
            given(cartFacade.updateCartItemQuantity(any(CartCommand.UpdateQuantity.class)))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(patch("/carts/{cartItemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].cartItemId").value(cartItemResult.id()))
                    .andExpect(jsonPath("$.items[0].status").value(cartItemResult.status().name()))
                    .andExpect(jsonPath("$.items[0].price.originalPrice").value(cartItemResult.price().originalPrice().longValue()))
                    .andExpect(jsonPath("$.items[0].options").isArray());
        }

        @Test
        @DisplayName("장바구니 상품 수량 수정은 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void updateQuantity_Admin_role() throws Exception {
            //given
            UpdateCartItemQuantityRequest request = Instancio.create(UpdateCartItemQuantityRequest.class);
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
            UpdateCartItemQuantityRequest request = Instancio.create(UpdateCartItemQuantityRequest.class);
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

        @ParameterizedTest(name = "{0}")
        @DisplayName("장바구니 상품 수량 변경 검증 테스트")
        @MethodSource("provideInvalidUpdateRequest")
        @WithCustomMockUser
        void updateQuantityValidation(String description, UpdateCartItemQuantityRequest request, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(patch("/carts/{cartItemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts/1"));
        }

        private static Stream<Arguments> provideInvalidUpdateRequest() {
            return Stream.of(
                    Arguments.of(
                            "수량이 null",
                            UpdateCartItemQuantityRequest.builder().build(),
                            "수량은 필수값 입니다"
                    ),
                    Arguments.of(
                            "수량이 1미만",
                            UpdateCartItemQuantityRequest.builder().quantity(0).build(),
                            "수량은 1이상이여야 합니다"
                    )
            );
        }
    }
}
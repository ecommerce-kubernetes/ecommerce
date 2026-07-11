package com.example.order_service.cart.api;

import com.example.order_service.cart.api.dto.request.AddCartItemsRequest;

import com.example.order_service.cart.api.dto.request.UpdateCartItemQuantityRequest;
import com.example.order_service.cart.application.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.result.AddCartItemsResult;
import com.example.order_service.cart.application.dto.result.UpdateCartItemQuantityResult;
import com.example.order_service.cart.application.facade.CartFacade;
import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.result.CartItemResult;
import com.example.order_service.cart.application.dto.result.CartResult;
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
import java.util.stream.IntStream;
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
        void addCartItems() throws Exception {
            //given
            AddCartItemsRequest request = Instancio.of(AddCartItemsRequest.class)
                    .generate(field(AddCartItemsRequest.class, "items"), gen -> gen.collection().minSize(1))
                    .generate(field(AddCartItemsRequest.Item.class, "quantity"), gen -> gen.ints().range(1, 100))
                    .create();
            AddCartItemsResult.CartItemResult item = Instancio.create(AddCartItemsResult.CartItemResult.class);
            AddCartItemsResult result = Instancio.of(AddCartItemsResult.class)
                    .set(field("items"), List.of(item))
                    .create();
            given(cartFacade.addItems(any(AddCartItemsCommand.class))).willReturn(result);
            //when
            //then
            mockMvc.perform(post("/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items").isNotEmpty())
                    .andExpect(jsonPath("$.items[0].cartItemId").value(item.cartItemId()))
                    .andExpect(jsonPath("$.items[0].productVariantId").value(item.productVariantId()));
        }

        @Test
        @DisplayName("장바구니에 상품을 추가할 때는 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void addCartItemsWithAdminPrincipal() throws Exception {
            //given
            AddCartItemsRequest request = Instancio.of(AddCartItemsRequest.class)
                    .generate(field(AddCartItemsRequest.class, "items"), gen -> gen.collection().minSize(1))
                    .generate(field(AddCartItemsRequest.Item.class, "quantity"), gen -> gen.ints().range(1, 100))
                    .create();
            //when
            //then
            mockMvc.perform(post("/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 장바구니에 상품을 추가할 수 없다")
        void addCartItems_unAuthorized() throws Exception {
            //given
            AddCartItemsRequest request = Instancio.of(AddCartItemsRequest.class)
                    .generate(field(AddCartItemsRequest.class, "items"), gen -> gen.collection().minSize(1))
                    .generate(field(AddCartItemsRequest.Item.class, "quantity"), gen -> gen.ints().range(1, 100))
                    .create();
            //when
            //then
            mockMvc.perform(post("/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("장바구니에 상품 추가시 유효성 검증에 실패하면 400 에러를 반환한다")
        @MethodSource("provideInvalidAddRequest")
        @WithCustomMockUser
        void addCartItems_Validation(String description, AddCartItemsRequest request, String expectedField, String expectedMessage) throws Exception {
            mockMvc.perform(post("/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                    .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                    .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts"));
        }

        private static Stream<Arguments> provideInvalidAddRequest() {
            return Stream.of(
                    Arguments.of("items 리스트가 비어있는 경우 검증에 실패한다",
                            AddCartItemsRequest.builder().items(List.of()).build(),
                            "items",
                            "장바구니에 담을 수 있는 상품 종류는 1개에서 최대 20개까지 입니다."),

                    Arguments.of("items 리스트의 크기가 20개를 초과하는 경우 검증에 실패한다",
                            AddCartItemsRequest.builder()
                                    .items(
                                            IntStream.rangeClosed(1, 21)
                                                    .mapToObj(i -> AddCartItemsRequest.Item.builder()
                                                            .productVariantId((long) i)
                                                            .quantity(1)
                                                            .build())
                                                    .toList()
                                    ).build(),
                            "items",
                            "장바구니에 담을 수 있는 상품 종류는 1개에서 최대 20개까지 입니다."
                    ),

                    Arguments.of("상품 변형 Id가 없는 경우 검증에 실패한다",
                            AddCartItemsRequest.builder().items(
                                            List.of(AddCartItemsRequest.Item.builder()
                                                    .quantity(1)
                                                    .build()))
                                    .build(),
                            "items[0].productVariantId",
                            "상품 식별자(productVariantId)는 필수값입니다."),

                    Arguments.of("수량이 없는 경우 검증에 실패한다",
                            AddCartItemsRequest.builder().items(
                                            List.of(AddCartItemsRequest.Item.builder()
                                                    .productVariantId(1L)
                                                    .build()))
                                    .build(),
                            "items[0].quantity",
                            "수량(quantity)은 필수값입니다."),

                    Arguments.of("수량이 0 이하인 경우 검증에 실패한다",
                            AddCartItemsRequest.builder().items(
                                            List.of(AddCartItemsRequest.Item.builder()
                                                    .productVariantId(1L)
                                                    .quantity(0)
                                                    .build()))
                                    .build(),
                            "items[0].quantity",
                            "수량(quantity)은 1개 이상이어야 합니다.")
            );
        }
    }

    @Nested
    @DisplayName("장바구니 목록 조회")
    class GetCart {

        @Test
        @DisplayName("장바구니 목록을 조회한다")
        @WithCustomMockUser
        void getCart() throws Exception {
            //given
            CartItemResult item = Instancio.create(CartItemResult.class);
            CartResult result = Instancio.of(CartResult.class)
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
                    .andExpect(jsonPath("$.items[0].cartItemId").value(item.cartItemId()))
                    .andExpect(jsonPath("$.items[0].status").value(item.status().name()));
        }

        @Test
        @DisplayName("장바구니 목록을 조회할때는 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void getCartWithAdminPrincipal() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(get("/carts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts"));
        }

        @Test
        @DisplayName("로그인 하지 않은 회원은 장바구니를 조회할 수 없다")
        void getCart_unAuthorized() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(get("/carts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts"));
        }
    }

    @Nested
    @DisplayName("장바구니 상품 조회")
    class GetCartItem {

        @Test
        @DisplayName("장바구니 상품 정보를 조회한다")
        @WithCustomMockUser
        void getCartItem() throws Exception {
            //given
            Long cartItemId = 1L;
            CartItemResult cartItem = Instancio.of(CartItemResult.class)
                    .set(field("cartItemId"), cartItemId)
                    .create();
            given(cartFacade.getCartItemDetails(anyLong(), anyLong())).willReturn(cartItem);
            //when
            //then
            mockMvc.perform(get("/carts/{cartItemId}", cartItemId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("cartItemId").value(cartItem.cartItemId()))
                    .andExpect(jsonPath("quantity").value(cartItem.quantity()));
        }

        @Test
        @DisplayName("장바구니 상품 정보를 조회할때는 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void getCartItemWithAdminPrincipal() throws Exception {
            //given
            Long cartItemId = 1L;
            //when
            //then
            mockMvc.perform(get("/carts/{cartItemId}", cartItemId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts/" + cartItemId));
        }

        @Test
        @DisplayName("로그인 하지 않은 회원은 장바구니 상품을 조회할 수 없다")
        void getCartItem_unAuthorized() throws Exception {
            //given
            Long cartItemId = 1L;
            //when
            //then
            mockMvc.perform(get("/carts/{cartItemId}", cartItemId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts/" + cartItemId));
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
            willDoNothing().given(cartFacade).deleteCartItems(any(DeleteCartItemsCommand.class));
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
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
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
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
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
            UpdateCartItemQuantityRequest request = Instancio.of(UpdateCartItemQuantityRequest.class)
                    .generate(field("quantity"), gen -> gen.ints().range(1, 100))
                    .create();
            UpdateCartItemQuantityResult result = Instancio.create(UpdateCartItemQuantityResult.class);
            given(cartFacade.updateCartItemQuantity(any(UpdateCartItemQuantityCommand.class)))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(patch("/carts/{cartItemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("cartItemId").value(result.cartItemId()))
                    .andExpect(jsonPath("productVariantId").value(result.productVariantId()))
                    .andExpect(jsonPath("quantity").value(result.quantity()));
        }

        @Test
        @DisplayName("장바구니 상품 수량 수정은 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void updateQuantity_Admin_role() throws Exception {
            //given
            UpdateCartItemQuantityRequest request = Instancio.of(UpdateCartItemQuantityRequest.class)
                    .generate(field("quantity"), gen -> gen.ints().range(1, 100))
                    .create();
            //when
            //then
            mockMvc.perform(patch("/carts/{cartItemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts/1"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 장바구니 상품의 수량을 수정할 수 없다")
        void updateQuantity_unAuthorized() throws Exception {
            //given
            UpdateCartItemQuantityRequest request = Instancio.of(UpdateCartItemQuantityRequest.class)
                    .generate(field("quantity"), gen -> gen.ints().range(1, 100))
                    .create();
            //when
            //then
            mockMvc.perform(patch("/carts/{cartItemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts/1"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("장바구니 상품 수량 변경 검증 테스트")
        @MethodSource("provideInvalidUpdateRequest")
        @WithCustomMockUser
        void updateQuantityValidation(String description, UpdateCartItemQuantityRequest request, String expectedField, String expectedMessage) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(patch("/carts/{cartItemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                    .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                    .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/carts/1"));
        }

        private static Stream<Arguments> provideInvalidUpdateRequest() {
            return Stream.of(
                    Arguments.of(
                            "수량이 없는 경우 검증에 실패한다",
                            UpdateCartItemQuantityRequest.builder().build(),
                            "quantity",
                            "수량(quantity)은 필수값입니다."
                    ),
                    Arguments.of(
                            "수량이 0 이하인 경우 검증에 실패한다",
                            UpdateCartItemQuantityRequest.builder().quantity(0).build(),
                            "quantity",
                            "수량(quantity)은 1개 이상이어야 합니다."
                    )
            );
        }
    }
}
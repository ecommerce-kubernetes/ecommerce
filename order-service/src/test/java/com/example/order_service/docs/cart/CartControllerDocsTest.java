package com.example.order_service.docs.cart;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.order_service.api.cart.controller.CartController;
import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.api.cart.facade.CartFacade;
import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartResponse;
import com.example.order_service.docs.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CartControllerDocsTest extends RestDocSupport {

    private CartFacade cartFacade = Mockito.mock(CartFacade.class);

    private static final String TAG = "CART";
    @Override
    protected Object initController() {
        return new CartController(cartFacade);
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
        given(cartFacade.addItem(any(AddCartItemCommand.class)))
                .willReturn(cartItemResponse);

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("productVariantId").description("상품 변형 ID"),
                fieldWithPath("quantity").description("수량")
        };


        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("장바구니 상품 ID(장바구니 상품 식별자)"),
                fieldWithPath("status").description("장바구니 상품 상태[주문 가능, 삭제됨, 준비중]"),
                fieldWithPath("available").description("주문 가능 여부"),
                fieldWithPath("productId").description("상품 ID(상품 식별자)"),
                fieldWithPath("productVariantId").description("상품 변형 ID"),
                fieldWithPath("productName").description("상품 이름"),
                fieldWithPath("thumbnailUrl").description("상품 썸네일"),
                fieldWithPath("quantity").description("수량"),
                fieldWithPath("price.originalPrice").description("상품 원본 가격"),
                fieldWithPath("price.discountRate").description("상품 할인율"),
                fieldWithPath("price.discountAmount").description("상품 할인 금액"),
                fieldWithPath("price.discountedPrice").description("할인된 가격"),
                fieldWithPath("lineTotal").description("항목 총액 (상품 할인 가격 X 수량)"),
                fieldWithPath("options[].optionTypeName").description("상품 옵션 타입 (예: 사이즈)"),
                fieldWithPath("options[].optionValueName").description("상품 옵션 값 (예: XL)")
        };

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
                                "02-cart-01-add-cartItem",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("장바구니 상품 추가")
                                                .description("장바구니에 상품을 추가")
                                                .requestHeaders(requestHeaders)
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                requestFields(requestFields),
                                responseFields(responseFields)
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
        given(cartFacade.getCartDetails(anyLong()))
                .willReturn(response);

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("cartItems[].id").description("장바구니 상품 ID(장바구니 상품 식별자)"),
                fieldWithPath("cartItems[].productId").description("상품 ID(상품 식별자)"),
                fieldWithPath("cartItems[].status").description("장바구니 상품 상태"),
                fieldWithPath("cartItems[].available").description("주문 가능 여부"),
                fieldWithPath("cartItems[].productVariantId").description("상품 변형 ID"),
                fieldWithPath("cartItems[].productName").description("상품 이름"),
                fieldWithPath("cartItems[].thumbnailUrl").description("상품 썸네일"),
                fieldWithPath("cartItems[].quantity").description("수량"),
                fieldWithPath("cartItems[].price.originalPrice").description("상품 원본 가격"),
                fieldWithPath("cartItems[].price.discountRate").description("상품 할인율"),
                fieldWithPath("cartItems[].price.discountAmount").description("상품 할인 금액"),
                fieldWithPath("cartItems[].price.discountedPrice").description("할인된 가격"),
                fieldWithPath("cartItems[].lineTotal").description("항목 총액 (상품 할인 가격 X 수량)"),
                fieldWithPath("cartItems[].options[].optionTypeName").description("상품 옵션 타입 (예: 사이즈)"),
                fieldWithPath("cartItems[].options[].optionValueName").description("상품 옵션 값 (예: XL)"),
                fieldWithPath("cartItems[].available").description("주문 가능 여부"),
                fieldWithPath("cartTotalPrice").description("장바구니 총액")
        };

        //when
        //then
        mockMvc.perform(get("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "02-cart-02-get-list",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("장바구니 목록 조회")
                                                .description("장바구니 상품 목록을 조회한다")
                                                .requestHeaders(requestHeaders)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                responseFields(responseFields)
                        )
                );
    }

    @Test
    @DisplayName("장바구니 상품 삭제")
    void removeCartItem() throws Exception {
        //given
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        willDoNothing().given(cartFacade).removeCartItem(anyLong(), anyLong());
        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };
        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("cartItemId").description("장바구니 상품 ID(장바구니 상품 식별자)")
        };
        //when
        //then
        mockMvc.perform(delete("/carts/{cartItemId}", 1)
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "02-cart-03-delete-item",
                        preprocessRequest(prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                                        .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(TAG)
                                        .summary("장바구니 상품 삭제")
                                        .description("장바구니 상품을 삭제한다")
                                        .requestHeaders(requestHeaders)
                                        .pathParameters(pathParameters)
                                        .build()
                        ),
                        requestHeaders(requestHeaders),
                        pathParameters(pathParameters)
                ));
    }

    @Test
    @DisplayName("장바구니 비우기")
    void clearCart() throws Exception {
        //given
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        willDoNothing().given(cartFacade).clearCart(anyLong());
        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };
        //when
        //then
        mockMvc.perform(delete("/carts")
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "02-cart-04-clear",
                        preprocessRequest(prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                                        .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(TAG)
                                        .summary("장바구니 비우기")
                                        .description("장바구니에 있는 모든 상품을 삭제한다")
                                        .requestHeaders(requestHeaders)
                                        .build()
                        ),
                        requestHeaders(requestHeaders)
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
        given(cartFacade.updateCartItemQuantity(any(UpdateQuantityCommand.class)))
                .willReturn(cartItemResponse);

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("cartItemId").description("장바구니 상품 ID(장바구니 상품 식별자)")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("quantity").description("변경할 수량")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("장바구니 상품 ID(장바구니 상품 식별자)"),
                fieldWithPath("status").description("장바구니 상품 상태"),
                fieldWithPath("productId").description("상품 ID(상품 식별자)"),
                fieldWithPath("productVariantId").description("상품 변형 ID"),
                fieldWithPath("productName").description("상품 이름"),
                fieldWithPath("thumbnailUrl").description("상품 썸네일"),
                fieldWithPath("quantity").description("수량"),
                fieldWithPath("price.originalPrice").description("상품 원본 가격"),
                fieldWithPath("price.discountRate").description("상품 할인율"),
                fieldWithPath("price.discountAmount").description("상품 할인 금액"),
                fieldWithPath("price.discountedPrice").description("할인된 가격"),
                fieldWithPath("lineTotal").description("항목 총액 (상품 할인 가격 X 수량)"),
                fieldWithPath("options[].optionTypeName").description("상품 옵션 타입 (예: 사이즈)"),
                fieldWithPath("options[].optionValueName").description("상품 옵션 값 (예: XL)"),
                fieldWithPath("available").description("주문 가능 여부")
        };

        //when
        //then
        mockMvc.perform(patch("/carts/{cartItemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("02-cart-05-update-quantity",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("장바구니 상품 수량 변경")
                                                .description("장바구니의 상품 수량을 변경한다")
                                                .requestHeaders(requestHeaders)
                                                .pathParameters(pathParameters)
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                pathParameters(pathParameters),
                                requestFields(requestFields),
                                responseFields(responseFields)
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
                .productVariantId(1L)
                .productName("상품1")
                .thumbnailUrl("http://thumbnail.jpg")
                .quantity(2)
                .price(
                        CartItemResponse.CartItemPrice.builder()
                                .originalPrice(3000)
                                .discountRate(10)
                                .discountAmount(300)
                                .discountedPrice(2700)
                                .build()
                )
                .lineTotal(5400)
                .options(List.of(
                        CartItemResponse.CartItemOption.builder()
                                .optionTypeName("사이즈")
                                .optionValueName("XL")
                                .build()
                ))
                .build();

    }
}

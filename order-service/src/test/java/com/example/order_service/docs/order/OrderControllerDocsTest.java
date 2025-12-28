package com.example.order_service.docs.order;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.common.util.DomainType;
import com.example.order_service.api.common.util.validator.OrderPageableValidator;
import com.example.order_service.api.common.util.validator.PageableValidatorFactory;
import com.example.order_service.api.order.application.OrderApplicationService;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderItemResponse;
import com.example.order_service.api.order.application.dto.result.OrderResponse;
import com.example.order_service.api.order.controller.OrderController;
import com.example.order_service.api.order.controller.dto.request.CreateOrderItemRequest;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.controller.dto.request.OrderConfirmRequest;
import com.example.order_service.docs.RestDocSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerDocsTest extends RestDocSupport {
    private OrderApplicationService orderApplicationService = mock(OrderApplicationService.class);
    private PageableValidatorFactory factory = mock(PageableValidatorFactory.class);

    @BeforeEach
    void validatorSetUp(){
        given(factory.getValidator(DomainType.ORDER))
                .willReturn(new OrderPageableValidator());
    }

    @Override
    protected Object initController() {
        return new OrderController(orderApplicationService, factory);
    }

    @Test
    @DisplayName("주문 생성 API")
    void createOrder() throws Exception {
        //given
        CreateOrderItemRequest item = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(item))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(5400L)
                .build();

        CreateOrderResponse response = CreateOrderResponse.builder()
                .orderId(1L)
                .status("PENDING")
                .orderName("상품1 외 1건")
                .finalPaymentAmount(5400L)
                .createAt(LocalDateTime.now())
                .build();

        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        given(orderApplicationService.createOrder(any(CreateOrderDto.class)))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(roleUser)
                    .content(objectMapper.writeValueAsString(createOrderRequest))
                )
                .andDo(print())
                .andExpect(status().isAccepted())
                .andDo(
                        document(
                                "order-create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                requestFields(
                                        fieldWithPath("items[].productVariantId").description("상품 변형 Id (상품 변형 식별자)").optional(),
                                        fieldWithPath("items[].quantity").description("주문 수량").optional(),
                                        fieldWithPath("deliveryAddress").description("배송지").optional(),
                                        fieldWithPath("couponId").description("사용 쿠폰 Id"),
                                        fieldWithPath("pointToUse").description("사용 포인트").optional(),
                                        fieldWithPath("expectedPrice").description("예상 결제 금액").optional()
                                ),
                                responseFields(
                                        fieldWithPath("orderId").description("주문 ID(주문 식별자)"),
                                        fieldWithPath("status").description("주문 상태"),
                                        fieldWithPath("createAt").description("주문 일시"),
                                        fieldWithPath("orderName").description("주문 설명"),
                                        fieldWithPath("finalPaymentAmount").description("최종 결제 금액")
                                )
                        )
                );
    }

    @Test
    @DisplayName("주문 결제 검증 API")
    void confirm() throws Exception {
        //given
        Long orderId = 1L;
        OrderConfirmRequest request = OrderConfirmRequest.builder()
                .orderId(orderId)
                .paymentKey("paymentKey")
                .build();
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        OrderResponse orderResponse = createOrderResponse(orderId);
        given(orderApplicationService.confirmOrder(anyLong(), anyString()))
                .willReturn(orderResponse);
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("order-confirm",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                        ),
                        requestFields(
                                fieldWithPath("orderId").description("주문 ID").optional(),
                                fieldWithPath("paymentKey").description("결제 키").optional()
                        ),
                        responseFields(
                                fieldWithPath("orderId").description("주문 ID"),
                                fieldWithPath("userId").description("유저 ID"),
                                fieldWithPath("orderStatus").description("주문 상태"),
                                fieldWithPath("orderName").description("주문 이름"),
                                fieldWithPath("deliveryAddress").description("배송지"),
                                fieldWithPath("createdAt").description("주문 시각"),

                                fieldWithPath("paymentResponse.totalOriginPrice").description("할인 전 주문 금액"),
                                fieldWithPath("paymentResponse.totalProductDiscount").description("상품 총 할인 금액"),
                                fieldWithPath("paymentResponse.couponDiscount").description("쿠폰 할인 금액"),
                                fieldWithPath("paymentResponse.pointDiscount").description("포인트 할인 금액"),
                                fieldWithPath("paymentResponse.finalPaymentAmount").description("최종 주문 금액"),

                                fieldWithPath("couponResponse.couponId").description("사용 쿠폰 ID"),
                                fieldWithPath("couponResponse.couponName").description("쿠폰 이름"),
                                fieldWithPath("couponResponse.couponDiscount").description("쿠폰 할인 금액"),

                                fieldWithPath("orderItems[].productId").description("주문 상품 ID(상품(Product) 식별자)"),
                                fieldWithPath("orderItems[].productVariantId").description("주문 상품 변형 ID"),
                                fieldWithPath("orderItems[].productName").description("주문 상품 이름"),
                                fieldWithPath("orderItems[].thumbNailUrl").description("주문 상품 썸네일"),
                                fieldWithPath("orderItems[].quantity").description("주문 수량"),
                                fieldWithPath("orderItems[].unitPrice.originalPrice").description("주문 상품 원본 가격"),
                                fieldWithPath("orderItems[].unitPrice.discountRate").description("상품 할인율"),
                                fieldWithPath("orderItems[].unitPrice.discountAmount").description("상품 할인 금액"),
                                fieldWithPath("orderItems[].unitPrice.discountedPrice").description("할인된 가격"),
                                fieldWithPath("orderItems[].lineTotal").description("주문 항목 총액"),
                                fieldWithPath("orderItems[].options[].optionTypeName").description("주문 상품 옵션 타입 (예: 사이즈)"),
                                fieldWithPath("orderItems[].options[].optionValueName").description("주문 상품 옵션 값 (예: XL)")
                                ))
                );
    }

    @Test
    @DisplayName("주문 정보를 조회한다")
    void getOrder() throws Exception {
        //given
        Long orderId = 1L;
        OrderResponse orderResponse = createOrderResponse(orderId);
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        given(orderApplicationService.getOrder(any(UserPrincipal.class), anyLong()))
                .willReturn(orderResponse);
        //when
        //then
        mockMvc.perform(get("/orders/{orderId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("get-order",
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                responseFields(
                                        fieldWithPath("orderId").description("주문 ID"),
                                        fieldWithPath("userId").description("유저 ID"),
                                        fieldWithPath("orderStatus").description("주문 상태"),
                                        fieldWithPath("orderName").description("주문 이름"),
                                        fieldWithPath("deliveryAddress").description("배송지"),
                                        fieldWithPath("createdAt").description("주문 시각"),

                                        fieldWithPath("paymentResponse.totalOriginPrice").description("할인 전 주문 금액"),
                                        fieldWithPath("paymentResponse.totalProductDiscount").description("상품 총 할인 금액"),
                                        fieldWithPath("paymentResponse.couponDiscount").description("쿠폰 할인 금액"),
                                        fieldWithPath("paymentResponse.pointDiscount").description("포인트 할인 금액"),
                                        fieldWithPath("paymentResponse.finalPaymentAmount").description("최종 주문 금액"),

                                        fieldWithPath("couponResponse.couponId").description("사용 쿠폰 ID"),
                                        fieldWithPath("couponResponse.couponName").description("쿠폰 이름"),
                                        fieldWithPath("couponResponse.couponDiscount").description("쿠폰 할인 금액"),

                                        fieldWithPath("orderItems[].productId").description("주문 상품 ID(상품(Product) 식별자)"),
                                        fieldWithPath("orderItems[].productVariantId").description("주문 상품 변형 ID"),
                                        fieldWithPath("orderItems[].productName").description("주문 상품 이름"),
                                        fieldWithPath("orderItems[].thumbNailUrl").description("주문 상품 썸네일"),
                                        fieldWithPath("orderItems[].quantity").description("주문 수량"),
                                        fieldWithPath("orderItems[].unitPrice.originalPrice").description("주문 상품 원본 가격"),
                                        fieldWithPath("orderItems[].unitPrice.discountRate").description("상품 할인율"),
                                        fieldWithPath("orderItems[].unitPrice.discountAmount").description("상품 할인 금액"),
                                        fieldWithPath("orderItems[].unitPrice.discountedPrice").description("할인된 가격"),
                                        fieldWithPath("orderItems[].lineTotal").description("주문 항목 총액"),
                                        fieldWithPath("orderItems[].options[].optionTypeName").description("주문 상품 옵션 타입 (예: 사이즈)"),
                                        fieldWithPath("orderItems[].options[].optionValueName").description("주문 상품 옵션 값 (예: XL)")
                                )
                        )
                );

    }

    @Test
    @DisplayName("주문 목록 조회 API")
    void getOrders() throws Exception {
        //given
//        LocalDateTime createAt = LocalDateTime.of(2025, 11, 27, 15, 30, 30);
//        OrderItemResponse orderItem = createOrderItemResponse(1L, "상품1", "http://product1.jpg");
//        OrderResponse orderResponse = createOrderResponse(1L, "COMPLETED", createAt, List.of(orderItem));
//        given(orderDomainService.getOrderList(any(Pageable.class), anyLong(), anyString(), anyString()))
//                .willReturn(
//                        PageDto.<OrderResponse>builder()
//                                .content(List.of(orderResponse))
//                                .currentPage(0)
//                                .totalPage(10)
//                                .pageSize(10)
//                                .totalElement(100)
//                                .build());
//        //when
//        //then
//        mockMvc.perform(get("/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("X-User-Id", "1")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .param("sort", "id,asc")
//                        .param("keyword", "keyword")
//                        .param("year", "2025"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document("getOrderList",
//                        preprocessResponse(prettyPrint()),
//                        requestHeaders(
//                                headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
//                                headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
//                        ),
//                        queryParameters(
//                                parameterWithName("page").description("페이지 번호(0-based)"),
//                                parameterWithName("size").description("페이지 크기"),
//                                parameterWithName("sort").description("정렬 기준 속성, 뱡향 (예: id,asc)"),
//                                parameterWithName("year").description("조회 연도"),
//                                parameterWithName("keyword").description("검색 키워드")
//                        ),
//                        responseFields(
//                                fieldWithPath("content[].id").description("주문 ID"),
//                                fieldWithPath("content[].status").description("주문 상태"),
//                                fieldWithPath("content[].createAt").description("주문 생성 시각"),
//                                fieldWithPath("content[].orderItems[].productId").description("주문 상품 ID(상품(Product) 식별자)"),
//                                fieldWithPath("content[].orderItems[].productName").description("주문 상품 이름"),
//                                fieldWithPath("content[].orderItems[].thumbNailUrl").description("주문 상품 썸네일"),
//                                fieldWithPath("content[].orderItems[].quantity").description("주문 수량"),
//                                fieldWithPath("content[].orderItems[].unitPrice.originalPrice").description("주문 상품 원본 가격"),
//                                fieldWithPath("content[].orderItems[].unitPrice.discountRate").description("상품 할인율"),
//                                fieldWithPath("content[].orderItems[].unitPrice.discountAmount").description("상품 할인 금액"),
//                                fieldWithPath("content[].orderItems[].unitPrice.discountedPrice").description("할인된 가격"),
//                                fieldWithPath("content[].orderItems[].lineTotal").description("주문 항목 총액"),
//                                fieldWithPath("content[].orderItems[].options[].optionTypeName").description("주문 상품 옵션 타입 (예: 사이즈)"),
//                                fieldWithPath("content[].orderItems[].options[].optionValueName").description("주문 상품 옵션 값 (예: XL)"),
//                                fieldWithPath("currentPage").description("현재 페이지"),
//                                fieldWithPath("totalPage").description("총 페이지"),
//                                fieldWithPath("pageSize").description("페이지 사이즈"),
//                                fieldWithPath("totalElement").description("총 Element")
//                        )
//                ));
    }

    private OrderResponse createOrderResponse(Long orderId) {
        return OrderResponse.builder()
                .orderId(orderId)
                .userId(1L)
                .orderStatus("COMPLETED")
                .orderName("상품1")
                .deliveryAddress("서울시 테헤란로 123")
                .paymentResponse(
                        OrderResponse.PaymentResponse.builder()
                                .totalOriginPrice(30000L)
                                .totalProductDiscount(3000L)
                                .couponDiscount(1000L)
                                .pointDiscount(1000L)
                                .finalPaymentAmount(25000L)
                                .build()
                )
                .couponResponse(
                        OrderResponse.CouponResponse.builder()
                                .couponId(1L)
                                .couponName("1000원 할인 쿠폰")
                                .couponDiscount(1000L)
                                .build()
                )
                .orderItems(
                        List.of(
                                OrderItemResponse.builder()
                                        .productId(1L)
                                        .productVariantId(1L)
                                        .productName("상품1")
                                        .thumbNailUrl("http://thumbanil.jpg")
                                        .quantity(1)
                                        .unitPrice(
                                                OrderItemResponse.OrderItemPrice.builder()
                                                        .originalPrice(30000L)
                                                        .discountAmount(3000L)
                                                        .discountRate(10)
                                                        .discountedPrice(27000L).build()
                                        )
                                        .lineTotal(27000L)
                                        .options(
                                                List.of(OrderItemResponse.OrderItemOption.builder()
                                                        .optionTypeName("사이즈")
                                                        .optionValueName("XL")
                                                        .build())
                                        )
                                        .build()
                        )
                )
                .createdAt(LocalDateTime.now())
                .build();

    }

    private HttpHeaders createUserHeader(String userRole){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
    }

}

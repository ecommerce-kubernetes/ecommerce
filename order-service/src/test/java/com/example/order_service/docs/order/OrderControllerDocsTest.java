package com.example.order_service.docs.order;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.order.controller.OrderController;
import com.example.order_service.api.order.controller.dto.request.CreateOrderItemRequest;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.controller.dto.request.OrderConfirmRequest;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.facade.OrderFacade;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.facade.dto.result.OrderItemResponse;
import com.example.order_service.api.order.facade.dto.result.OrderListResponse;
import com.example.order_service.docs.RestDocSupport;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerDocsTest extends RestDocSupport {
    private static final String ORDER_NO = "ORD-20260101-AB12FVC";
    private OrderFacade orderFacade = mock(OrderFacade.class);

    @Override
    protected Object initController() {
        return new OrderController(orderFacade);
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
                .orderNo(ORDER_NO)
                .status("PENDING")
                .orderName("상품1 외 1건")
                .finalPaymentAmount(5400L)
                .createdAt(LocalDateTime.now().toString())
                .build();

        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        given(orderFacade.initialOrder(any(CreateOrderCommand.class)))
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
                                        fieldWithPath("orderNo").description("주문 번호"),
                                        fieldWithPath("status").description("주문 상태"),
                                        fieldWithPath("createdAt").description("주문 일시"),
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
        OrderConfirmRequest request = OrderConfirmRequest.builder()
                .orderNo(ORDER_NO)
                .paymentKey("paymentKey")
                .amount(1000L)
                .build();
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        OrderDetailResponse orderDetailResponse = createOrderResponse();
        given(orderFacade.finalizeOrder(anyString(), anyLong(), anyString(), anyLong()))
                .willReturn(orderDetailResponse);
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
                                fieldWithPath("orderNo").description("주문 번호").optional(),
                                fieldWithPath("paymentKey").description("결제 키").optional(),
                                fieldWithPath("amount").description("결제 금액").optional()
                        ),
                        responseFields(
                                fieldWithPath("orderNo").description("주문 번호"),
                                fieldWithPath("userId").description("유저 ID"),
                                fieldWithPath("orderStatus").description("주문 상태"),
                                fieldWithPath("orderName").description("주문 이름"),
                                fieldWithPath("deliveryAddress").description("배송지"),
                                fieldWithPath("createdAt").description("주문 시각"),

                                fieldWithPath("orderPriceResponse.totalOriginPrice").description("할인 전 주문 금액"),
                                fieldWithPath("orderPriceResponse.totalProductDiscount").description("상품 총 할인 금액"),
                                fieldWithPath("orderPriceResponse.couponDiscount").description("쿠폰 할인 금액"),
                                fieldWithPath("orderPriceResponse.pointDiscount").description("포인트 할인 금액"),
                                fieldWithPath("orderPriceResponse.finalPaymentAmount").description("최종 주문 금액"),

                                fieldWithPath("paymentResponse.paymentId").description("결제 ID"),
                                fieldWithPath("paymentResponse.paymentKey").description("결제 키"),
                                fieldWithPath("paymentResponse.amount").description("결제 금액"),
                                fieldWithPath("paymentResponse.method").description("결제 방법"),
                                fieldWithPath("paymentResponse.approvedAt").description("결제 시각"),

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
        OrderDetailResponse orderDetailResponse = createOrderResponse();
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        given(orderFacade.getOrder(anyLong(), anyString()))
                .willReturn(orderDetailResponse);
        //when
        //then
        mockMvc.perform(get("/orders/{orderNo}", ORDER_NO)
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
                                        fieldWithPath("orderNo").description("주문 번호"),
                                        fieldWithPath("userId").description("유저 ID"),
                                        fieldWithPath("orderStatus").description("주문 상태"),
                                        fieldWithPath("orderName").description("주문 이름"),
                                        fieldWithPath("deliveryAddress").description("배송지"),
                                        fieldWithPath("createdAt").description("주문 시각"),

                                        fieldWithPath("orderPriceResponse.totalOriginPrice").description("할인 전 주문 금액"),
                                        fieldWithPath("orderPriceResponse.totalProductDiscount").description("상품 총 할인 금액"),
                                        fieldWithPath("orderPriceResponse.couponDiscount").description("쿠폰 할인 금액"),
                                        fieldWithPath("orderPriceResponse.pointDiscount").description("포인트 할인 금액"),
                                        fieldWithPath("orderPriceResponse.finalPaymentAmount").description("최종 주문 금액"),

                                        fieldWithPath("paymentResponse.paymentId").description("결제 ID"),
                                        fieldWithPath("paymentResponse.paymentKey").description("결제 키"),
                                        fieldWithPath("paymentResponse.amount").description("결제 금액"),
                                        fieldWithPath("paymentResponse.method").description("결제 방법"),
                                        fieldWithPath("paymentResponse.approvedAt").description("결제 시각"),

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
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        OrderListResponse orderListResponse = createOrderListResponse();
        PageDto<OrderListResponse> response = PageDto.<OrderListResponse>builder().content(List.of(orderListResponse))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();
        given(orderFacade.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "latest")
                        .param("year", "2023")
                        .param("productName", "나이키"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("get-orders",
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                queryParameters(
                                        parameterWithName("page").description("페이지 번호 (기본값: 1)"),
                                        parameterWithName("size").description("페이지 크기 (기본값: 20, 최대: 100)"),
                                        parameterWithName("sort").description("정렬 기준 (latest: 최신순, price_high: 높은가격순 등)"),
                                        parameterWithName("year").description("조회 연도 필터"),
                                        parameterWithName("productName").description("상품명 검색 키워드")
                                ),
                                responseFields(
                                        fieldWithPath("content[].orderNo").description("주문 번호"),
                                        fieldWithPath("content[].userId").description("유저 ID"),
                                        fieldWithPath("content[].orderStatus").description("주문 상태"),
                                        fieldWithPath("content[].createdAt").description("주문 시각"),

                                        fieldWithPath("content[].orderItems[].productId").description("주문 상품 ID(상품(Product) 식별자)"),
                                        fieldWithPath("content[].orderItems[].productVariantId").description("주문 상품 변형 ID"),
                                        fieldWithPath("content[].orderItems[].productName").description("주문 상품 이름"),
                                        fieldWithPath("content[].orderItems[].thumbNailUrl").description("주문 상품 썸네일"),
                                        fieldWithPath("content[].orderItems[].quantity").description("주문 수량"),
                                        fieldWithPath("content[].orderItems[].unitPrice.originalPrice").description("주문 상품 원본 가격"),
                                        fieldWithPath("content[].orderItems[].unitPrice.discountRate").description("상품 할인율"),
                                        fieldWithPath("content[].orderItems[].unitPrice.discountAmount").description("상품 할인 금액"),
                                        fieldWithPath("content[].orderItems[].unitPrice.discountedPrice").description("할인된 가격"),
                                        fieldWithPath("content[].orderItems[].lineTotal").description("주문 항목 총액"),
                                        fieldWithPath("content[].orderItems[].options[].optionTypeName").description("주문 상품 옵션 타입 (예: 사이즈)"),
                                        fieldWithPath("content[].orderItems[].options[].optionValueName").description("주문 상품 옵션 값 (예: XL)"),

                                        fieldWithPath("currentPage").description("현재 페이지"),
                                        fieldWithPath("totalPage").description("총 페이지"),
                                        fieldWithPath("pageSize").description("페이지 크기"),
                                        fieldWithPath("totalElement").description("총 데이터 양")
                                )
                        )
                );
    }

    private OrderListResponse createOrderListResponse() {
        return OrderListResponse.builder()
                .orderNo(ORDER_NO)
                .orderStatus("COMPLETED")
                .orderItems(createOrderItems())
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    private OrderDetailResponse createOrderResponse() {
//        return OrderDetailResponse.builder()
//                .orderNo(ORDER_NO)
//                .userId(1L)
//                .orderStatus("COMPLETED")
//                .orderName("상품1")
//                .deliveryAddress("서울시 테헤란로 123")
//                .orderPriceResponse(
//                        OrderDetailResponse.OrderPriceResponse.builder()
//                                .totalOriginPrice(30000L)
//                                .totalProductDiscount(3000L)
//                                .couponDiscount(1000L)
//                                .pointDiscount(1000L)
//                                .finalPaymentAmount(25000L)
//                                .build()
//                )
//                .couponResponse(
//                        OrderDetailResponse.CouponResponse.builder()
//                                .couponId(1L)
//                                .couponName("1000원 할인 쿠폰")
//                                .couponDiscount(1000L)
//                                .build()
//                )
//                .paymentResponse(
//                        OrderDetailResponse.PaymentResponse.builder()
//                                .paymentId(1L)
//                                .paymentKey("paymentKey")
//                                .amount(25000L)
//                                .method("CARD")
//                                .approvedAt(LocalDateTime.now().toString())
//                                .build()
//                )
//                .orderItems(createOrderItems())
//                .createdAt(LocalDateTime.now().toString())
//                .build();
        return null;
    }

    private List<OrderItemResponse> createOrderItems() {
        return List.of(
                OrderItemResponse.builder()
                        .productId(1L)
                        .productVariantId(1L)
                        .productName("상품1")
                        .thumbnailUrl("http://thumbanil.jpg")
                        .quantity(1)
                        .unitPrice(
                                OrderItemResponse.OrderItemPriceResponse.builder()
                                        .originalPrice(30000L)
                                        .discountAmount(3000L)
                                        .discountRate(10)
                                        .discountedPrice(27000L).build()
                        )
                        .lineTotal(27000L)
                        .options(
                                List.of(OrderItemResponse.OrderItemOptionResponse.builder()
                                        .optionTypeName("사이즈")
                                        .optionValueName("XL")
                                        .build())
                        )
                        .build()
        );
    }

    private HttpHeaders createUserHeader(String userRole){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
    }

}

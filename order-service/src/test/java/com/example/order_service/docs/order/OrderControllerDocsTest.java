package com.example.order_service.docs.order;

import com.example.order_service.api.cart.infrastructure.client.dto.ItemOption;
import com.example.order_service.api.cart.infrastructure.client.dto.UnitPrice;
import com.example.order_service.api.order.application.OrderApplicationService;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.controller.OrderController;
import com.example.order_service.api.order.controller.dto.response.OrderItemResponse;
import com.example.order_service.api.order.controller.dto.response.OrderResponse;
import com.example.order_service.api.common.util.validator.OrderPageableValidator;
import com.example.order_service.api.common.util.validator.PageableValidatorFactory;
import com.example.order_service.docs.RestDocSupport;
import com.example.order_service.api.order.controller.dto.request.CreateOrderItemRequest;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.dto.response.*;
import com.example.order_service.entity.DomainType;
import com.example.order_service.api.order.domain.service.OrderService;
import com.example.order_service.service.SseConnectionService;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerDocsTest extends RestDocSupport {
    private OrderService orderService = mock(OrderService.class);
    private OrderApplicationService orderApplicationService = mock(OrderApplicationService.class);
    private SseConnectionService sseConnectionService = mock(SseConnectionService.class);
    private PageableValidatorFactory factory = mock(PageableValidatorFactory.class);

    @BeforeEach
    void validatorSetUp(){
        given(factory.getValidator(DomainType.ORDER))
                .willReturn(new OrderPageableValidator());
    }

    @Override
    protected Object initController() {
        return new OrderController(orderApplicationService, orderService, factory);
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
                .message("상품1 외 1건")
                .finalPaymentAmount(5400L)
                .createAt(LocalDateTime.now())
                .build();

        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        given(orderService.saveOrder(any(CreateOrderDto.class)))
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
                                        fieldWithPath("message").description("주문 설명"),
                                        fieldWithPath("finalPaymentAmount").description("최종 결제 금액")
                                )
                        )
                );
    }

    @Test
    @DisplayName("주문 목록 조회 API")
    void getOrders() throws Exception {
        //given
        LocalDateTime createAt = LocalDateTime.of(2025, 11, 27, 15, 30, 30);
        OrderItemResponse orderItem = createOrderItemResponse(1L, "상품1", "http://product1.jpg");
        OrderResponse orderResponse = createOrderResponse(1L, "COMPLETED", createAt, List.of(orderItem));
        given(orderService.getOrderList(any(Pageable.class), anyLong(), anyString(), anyString()))
                .willReturn(
                        PageDto.<OrderResponse>builder()
                                .content(List.of(orderResponse))
                                .currentPage(0)
                                .totalPage(10)
                                .pageSize(10)
                                .totalElement(100)
                                .build());
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc")
                        .param("keyword", "keyword")
                        .param("year", "2025"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("getOrderList",
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호(0-based)"),
                                parameterWithName("size").description("페이지 크기"),
                                parameterWithName("sort").description("정렬 기준 속성, 뱡향 (예: id,asc)"),
                                parameterWithName("year").description("조회 연도"),
                                parameterWithName("keyword").description("검색 키워드")
                        ),
                        responseFields(
                                fieldWithPath("content[].id").description("주문 ID"),
                                fieldWithPath("content[].status").description("주문 상태"),
                                fieldWithPath("content[].createAt").description("주문 생성 시각"),
                                fieldWithPath("content[].orderItems[].productId").description("주문 상품 ID(상품(Product) 식별자)"),
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
                                fieldWithPath("pageSize").description("페이지 사이즈"),
                                fieldWithPath("totalElement").description("총 Element")
                        )
                ));
    }

    private OrderItemResponse createOrderItemResponse(Long productId, String productName, String thumbNailUrl){
        return OrderItemResponse.builder()
                .productId(productId)
                .productName(productName)
                .thumbNailUrl(thumbNailUrl)
                .quantity(2)
                .unitPrice(
                        UnitPrice.builder()
                                .originalPrice(3000)
                                .discountRate(10)
                                .discountAmount(300)
                                .discountedPrice(2700)
                                .build()
                )
                .lineTotal(5700)
                .options(List.of(                        ItemOption.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL")
                        .build()))
                .build();
    }

    private OrderResponse createOrderResponse(Long orderId, String status, LocalDateTime createAt, List<OrderItemResponse> orderItemResponses){
        return OrderResponse.builder()
                .id(orderId)
                .status(status)
                .createAt(createAt)
                .orderItems(orderItemResponses)
                .build();
    }

    private HttpHeaders createUserHeader(String userRole){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
    }

}

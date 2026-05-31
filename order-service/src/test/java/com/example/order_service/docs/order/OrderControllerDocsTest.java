package com.example.order_service.docs.order;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.OrderDescriptor;
import com.example.order_service.order.api.OrderController;
import com.example.order_service.order.api.dto.request.OrderConfirmRequest;
import com.example.order_service.order.api.dto.request.OrderRequest;
import com.example.order_service.order.api.dto.response.OrderResponse;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import com.example.order_service.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.support.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.example.order_service.api.support.fixture.order.OrderResponseFixture.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerDocsTest extends RestDocSupport {
    private static final String ORDER_NO = "ORD-20260101-AB12FVC";
    private OrderFacade orderFacade = mock(OrderFacade.class);
    private OrderQueryService orderQueryService = mock(OrderQueryService.class);

    private static final String TAG = "ORDER";

    @Override
    protected String getTag() {
        return "ORDER";
    }

    @Override
    protected Object initController() {
        return new OrderController(orderFacade, orderQueryService);
    }

    @Test
    @DisplayName("주문 생성 API")
    void createOrder() throws Exception {
        //given
        OrderRequest.Create createOrderRequest = OrderRequest.Create.builder()
                .orderSheetId("sheetId")
                .build();

        OrderResult.Create result = OrderResult.Create.builder()
                .orderNo("ORDER_NO")
                .status(OrderStatus.PENDING)
                .orderName("상품 1외 1건")
                .totalPaymentAmount(Money.wons(9000L))
                .createdAt(LocalDateTime.now())
                .build();
        OrderResponse.Create response = OrderResponse.Create.from(result);
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        given(orderFacade.initialOrder(any(OrderCommand.Create.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andDo(print())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andExpect(status().isAccepted())
                .andDo(createSecuredDocument("01-order-01-create",
                        "주문 생성",
                        "주문을 생성한다",
                        OrderDescriptor.getOrderCreateRequest(),
                        OrderDescriptor.getOrderCreateResponse())
                );
    }

    @Test
    @DisplayName("주문 결제 승인 API")
    void confirm() throws Exception {
        //given
        OrderConfirmRequest request = OrderConfirmRequest.builder()
                .orderNo(ORDER_NO)
                .paymentKey("paymentKey")
                .amount(1000L)
                .build();
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        OrderDetailResponse response = anOrderDetailResponse()
                .payment(anPaymentResponse().build()).build();
        given(orderFacade.confirmOrderPayment(anyString(), anyLong(), anyString(), anyLong()))
                .willReturn(response);

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[]{
                headerWithName("Authorization").description("JWT Access Token")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[]{
                fieldWithPath("orderNo").description("주문 번호"),
                fieldWithPath("paymentKey").description("결제 키"),
                fieldWithPath("amount").description("결제 금액")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[]{
                fieldWithPath("orderNo").description("주문 번호"),
                fieldWithPath("status").description("주문 상태"),
                fieldWithPath("orderName").description("주문 이름"),

                fieldWithPath("orderer.userId").description("주문자 아이디"),
                fieldWithPath("orderer.userName").description("주문자 이름"),
                fieldWithPath("orderer.phoneNumber").description("주문자 전화번호"),

                fieldWithPath("orderPrice.totalOriginPrice").description("할인 전 주문 금액"),
                fieldWithPath("orderPrice.totalProductDiscount").description("상품 총 할인 금액"),
                fieldWithPath("orderPrice.couponDiscount").description("쿠폰 할인 금액"),
                fieldWithPath("orderPrice.pointDiscount").description("포인트 할인 금액"),
                fieldWithPath("orderPrice.finalPaymentAmount").description("최종 주문 금액"),

                fieldWithPath("coupon.couponId").description("쿠폰 ID").optional(),
                fieldWithPath("coupon.couponName").description("쿠폰 이름").optional(),
                fieldWithPath("coupon.couponDiscount").description("쿠폰 할인금").optional(),

                fieldWithPath("deliveryAddress").description("배송지"),

                fieldWithPath("payment.paymentId").description("결제 ID"),
                fieldWithPath("payment.paymentKey").description("결제 키"),
                fieldWithPath("payment.amount").description("결제 금액"),
                fieldWithPath("payment.status").description("결제 상태"),
                fieldWithPath("payment.method").description("결제 방법"),
                fieldWithPath("payment.approvedAt").description("결제 시각"),

                fieldWithPath("createdAt").description("주문 시각"),

                fieldWithPath("orderItems[].productId").description("주문 상품 ID(상품(ProductDeprecated) 식별자)"),
                fieldWithPath("orderItems[].productVariantId").description("주문 상품 변형 ID"),
                fieldWithPath("orderItems[].productName").description("주문 상품 이름"),
                fieldWithPath("orderItems[].thumbnailUrl").description("주문 상품 썸네일"),
                fieldWithPath("orderItems[].quantity").description("주문 수량"),
                fieldWithPath("orderItems[].unitPrice.originalPrice").description("주문 상품 원본 가격"),
                fieldWithPath("orderItems[].unitPrice.discountRate").description("상품 할인율"),
                fieldWithPath("orderItems[].unitPrice.discountAmount").description("상품 할인 금액"),
                fieldWithPath("orderItems[].unitPrice.discountedPrice").description("할인된 가격"),
                fieldWithPath("orderItems[].lineTotal").description("주문 항목 총액"),
                fieldWithPath("orderItems[].options[].optionTypeName").description("주문 상품 옵션 타입 (예: 사이즈)"),
                fieldWithPath("orderItems[].options[].optionValueName").description("주문 상품 옵션 값 (예: XL)")
        };
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("01-order-02-confirm",
                        preprocessRequest(prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                                        .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag(TAG)
                                        .summary("결제 승인")
                                        .description("주문 결제를 승인한다")
                                        .requestHeaders(requestHeaders)
                                        .requestFields(requestFields)
                                        .responseFields(responseFields)
                                        .build()
                        ),
                        requestHeaders(requestHeaders),
                        requestFields(requestFields),
                        responseFields(responseFields))
                );
    }

    @Test
    @DisplayName("주문 정보를 조회한다")
    void getOrder() throws Exception {
        //given
        OrderResult.Detail result = createDetailResult();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        given(orderQueryService.getOrder(anyString(), anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/orders/{orderNo}", ORDER_NO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(createSecuredDocument("01-order-03-get",
                        "주문 조회",
                        "주문을 조회한다",
                        OrderDescriptor.getOrderDetailResponse(),
                        parameterWithName("orderNo").description("조회할 주문 번호"))
                );

    }

    @Test
    @DisplayName("주문 목록 조회 API")
    void getOrders() throws Exception {
        //given
        Page<OrderResult.Summary> summaryResult = createSummaryResult();
        given(orderQueryService.getOrders(anyLong(), any(OrderSearchCommand.class), any(Pageable.class)))
                .willReturn(summaryResult);
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
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
                        createSecuredDocumentQuery("01-order-04-get-list",
                                "주문 목록 조회",
                                "주문 목록을 조회한다",
                                OrderDescriptor.getOrderSummaryResponse(),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 크기"),
                                parameterWithName("sort").description("정렬 기준"),
                                parameterWithName("year").description("조회 연도 필터"),
                                parameterWithName("productName").description("상품명"))
                );
    }

    private HttpHeaders createUserHeader(String userRole) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
    }

    private OrderResult.Detail createDetailResult() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(1L, "장바구니 1000원 할인", Money.wons(1000L));
        return OrderResult.Detail.builder()
                .orderNo("orderNo")
                .status(OrderStatus.COMPLETED)
                .orderName("청바지")
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .cartCoupon(cartCoupon)
                .items(createItems())
                .totalOriginalPrice(Money.wons(10000L))
                .totalProductDiscountAmount(Money.wons(1000L))
                .totalCouponDiscountAmount(Money.wons(2000L))
                .usedPoints(Money.wons(1000L))
                .totalPaymentAmount(Money.wons(6000L))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Page<OrderResult.Summary> createSummaryResult() {
        OrderResult.Summary summary = OrderResult.Summary.builder()
                .orderNo("orderNo")
                .status(OrderStatus.COMPLETED)
                .orderName("청바지")
                .orderItems(createItems())
                .createdAt(LocalDateTime.now())
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        return new PageImpl<>(
                List.of(summary),
                pageable,
                1
        );
    }

    private List<OrderResult.OrderedItem> createItems() {
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "하의 1000원 할인", Money.wons(1000L));
        ProductOptionSnapshot xl = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot blue = ProductOptionSnapshot.of("색상", "BLUE");
        return List.of(
                OrderResult.OrderedItem.builder()
                        .product(product)
                        .productPrice(price)
                        .itemCoupon(itemCoupon)
                        .quantity(1)
                        .options(List.of(xl, blue))
                        .build()
        );
    }
}

package com.example.order_service.docs.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.OrderDescriptor;
import com.example.order_service.order.api.web.OrderController;
import com.example.order_service.order.api.web.dto.order.request.OrderCreateRequest;
import com.example.order_service.order.api.web.dto.order.response.OrderResponse;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerDocsTest extends RestDocSupport {
    private OrderFacade orderFacade = mock(OrderFacade.class);
    private OrderQueryService orderQueryService = mock(OrderQueryService.class);

    @Override
    protected Object initController() {
        return new OrderController(orderFacade, orderQueryService);
    }

    @Test
    @DisplayName("주문 생성 API")
    void createOrder() throws Exception {
        //given
        Long orderSheetId = 1L;
        OrderCreateRequest createOrderRequest = OrderCreateRequest.builder()
                .orderSheetId(orderSheetId)
                .build();

        OrderResult.Create result = OrderResult.Create.builder()
                .orderId(1L)
                .orderNo("ORDER_NO")
                .status(OrderStatus.PENDING)
                .orderName("상품 1외 1건")
                .totalPaymentAmount(Money.wons(9000L))
                .createdAt(LocalDateTime.now())
                .build();
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
                .andExpect(status().isCreated())
                .andDo(document(
                        "orders/create",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(OrderDescriptor.orderCreateRequest()),
                        responseFields(OrderDescriptor.orderCreateResponse())
                ));
    }

    @Test
    @DisplayName("주문 정보를 조회한다")
    void getOrder() throws Exception {
        //given
        Long orderSheetId = 1L;
        OrderResult.Detail result = createDetailResult();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        given(orderQueryService.getOrder(anyString(), anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/orders/{orderNo}", "ORDER_NO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "orders/get",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(OrderDescriptor.getOrderDetailResponse()),
                        pathParameters(
                                parameterWithName("orderNo")
                                        .description("주문 번호")
                        )
                ));

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
                .andDo(document(
                        "orders/get-list",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(OrderDescriptor.getOrderSummaryResponse()),
                        queryParameters(
                                parameterWithName("page")
                                        .description("페이지 번호"),
                                parameterWithName("size")
                                        .description("페이지 크기"),
                                parameterWithName("sort")
                                        .description("정렬 기준"),
                                parameterWithName("year")
                                        .description("주문 연도"),
                                parameterWithName("productName")
                                        .description("상품 이름")
                        )
                ));
    }

    private OrderResult.Detail createDetailResult() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
//        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인", Money.wons(1000L));
        return OrderResult.Detail.builder()
                .orderNo("orderNo")
                .status(OrderStatus.COMPLETED)
                .orderName("청바지")
                .orderer(orderer)
                .shippingAddress(shippingAddress)
//                .cartCoupon(cartCoupon)
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
//        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "하의 1000원 할인", Money.wons(1000L));
        ProductOptionSnapshot xl = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot blue = ProductOptionSnapshot.of("색상", "BLUE");
        return List.of(
                OrderResult.OrderedItem.builder()
                        .product(product)
                        .productPrice(price)
//                        .itemCoupon(itemCoupon)
                        .quantity(1)
                        .options(List.of(xl, blue))
                        .build()
        );
    }
}

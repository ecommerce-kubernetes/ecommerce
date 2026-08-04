package com.example.order_service.docs.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.OrderDescriptor;
import com.example.order_service.order.api.web.OrderController;
import com.example.order_service.order.api.web.dto.order.request.OrderCreateRequest;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

        OrderCreateResult result = OrderCreateResult.builder()
                .orderId(1L)
                .build();

        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        given(orderFacade.createOrder(any(CreateOrderCommand.class)))
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
        Long orderId = 1L;
        OrderResult result = createOrderResult();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        given(orderQueryService.getOrder(anyLong(), anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/orders/{orderId}", orderId)
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
                        responseFields(OrderDescriptor.orderResponse()),
                        pathParameters(
                                parameterWithName("orderId")
                                        .description("주문 아이디 (주문 식별자)")
                        )
                ));

    }

    @Test
    @DisplayName("주문 목록 조회 API")
    void getOrders() throws Exception {
        //given
        Page<OrderSummaryResult> summaryResult = createSummaryResult();
        given(orderQueryService.getOrders(anyLong(), any(OrderSearchCommand.class)))
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
                        responseFields(OrderDescriptor.orderSummaryResponse()),
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

    private OrderResult createOrderResult() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");

        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1_XL_BLUE", "청바지", "product/product/jean.jpg");
        ProductOptionSnapshot option1 = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot option2 = ProductOptionSnapshot.of("색상", "BLUE");
        OrderItemAmount orderItemAmount = OrderItemAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(27000L),
                Money.wons(1000L),
                Money.wons(26000L)
        );

        OrderResult.OrderItemResult orderItem = OrderResult.OrderItemResult.builder()
                .orderItemId(100L)
                .product(product)
                .options(List.of(option1, option2))
                .quantity(3)
                .orderItemAmount(orderItemAmount)
                .build();

        OrderAmount orderAmount = OrderAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(1000L), Money.wons(1000L),
                Money.wons(1000L), Money.wons(24000L));

        return OrderResult.builder()
                .orderId(1L)
                .status(OrderStatus.COMPLETED)
                .orderName("상품")
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .orderItems(List.of(orderItem))
                .orderAmount(orderAmount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Page<OrderSummaryResult> createSummaryResult() {
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1_XL_BLUE", "청바지", "product/product/jean.jpg");
        ProductOptionSnapshot option1 = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot option2 = ProductOptionSnapshot.of("색상", "BLUE");
        OrderSummaryResult.ItemPayment itemPayment = OrderSummaryResult.ItemPayment.builder()
                .lineTotal(Money.wons(27000L))
                .couponDiscount(Money.wons(1000L))
                .finalItemAmount(Money.wons(26000L))
                .build();

        OrderSummaryResult.OrderItemResult orderItem = OrderSummaryResult.OrderItemResult.builder()
                .orderItemId(100L)
                .product(product)
                .options(List.of(option1, option2))
                .quantity(3)
                .itemPayment(itemPayment)
                .build();

        OrderSummaryResult orderSummary = OrderSummaryResult.builder()
                .orderId(1L)
                .status(OrderStatus.COMPLETED)
                .orderItems(List.of(orderItem))
                .createdAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        return new PageImpl<>(List.of(orderSummary), pageable, 2);
    }
}

package com.example.order_service.order.api.web;

import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.order.api.web.dto.order.request.OrderCreateRequest;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.support.annotation.WithCustomMockUser;
import com.example.order_service.support.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrderFacade orderFacade;
    @MockitoBean
    private OrderQueryService orderQueryService;

    @Test
    @DisplayName("주문을 생성한다")
    @WithCustomMockUser
    void createOrder() throws Exception {
        //given
        Long orderSheetId = 1L;
        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderSheetId(orderSheetId)
                .build();

        OrderResult.Create result = Instancio.create(OrderResult.Create.class);
        given(orderFacade.initialOrder(any(OrderCommand.Create.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(result.orderId()));
    }

    @Test
    @DisplayName("주문 요청시 권한은 유저여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void createOrderWithAdminPrincipal() throws Exception {
        //given
        Long orderSheetId = 1L;
        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderSheetId(orderSheetId)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 주문을 생성할 수 없다")
    void createOrder_unAuthorized() throws Exception {
        //given
        Long orderSheetId = 1L;
        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderSheetId(orderSheetId)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("주문 요청시 유효성 검증에 실패하면 400 에러를 반환한다")
    @MethodSource("provideInvalidCreateOrderRequest")
    @WithCustomMockUser
    void createOrder_validation(String description, OrderCreateRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        //when, then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Nested
    @DisplayName("주문 조회")
    class GetOrder {

        @Test
        @DisplayName("주문 정보를 조회한다")
        @WithCustomMockUser
        void getOrder() throws Exception {
            //given
            String orderNo = "orderNo";
            OrderResult.Detail result = Instancio.create(OrderResult.Detail.class);
            given(orderQueryService.getOrder(anyString(), anyLong()))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(get("/orders/{orderNo}", orderNo)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderNo").value(result.orderNo()))
                    .andExpect(jsonPath("$.status").isString())
                    .andExpect(jsonPath("$.totalOriginalPrice").value(result.totalOriginalPrice().longValue()))
                    .andExpect(jsonPath("$.totalProductDiscountAmount").value(result.totalProductDiscountAmount().longValue()))
                    .andExpect(jsonPath("$.totalCouponDiscountAmount").value(result.totalCouponDiscountAmount().longValue()))
                    .andExpect(jsonPath("$.usedPoints").value(result.usedPoints().longValue()))
                    .andExpect(jsonPath("$.totalPaymentAmount").value(result.totalPaymentAmount().longValue()));
        }

        @Test
        @DisplayName("주문 정보를 조회할때는 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void getOrder_Admin_role() throws Exception {
            //given
            String orderNo = "orderNo";
            //when
            //then
            mockMvc.perform(get("/orders/{orderNo}", orderNo)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders/orderNo"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문 정보를 조회할 수 없다")
        void getOrder_unAuthorized() throws Exception {
            //given
            String orderNo = "orderNo";
            //when
            //then
            mockMvc.perform(get("/orders/{orderNo}", orderNo)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders/orderNo"));
        }
    }

    @Nested
    @DisplayName("주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("주문 목록 조회")
        @WithCustomMockUser
        void getOrders() throws Exception {
            //given
            List<OrderResult.Summary> summaries = Instancio.ofList(OrderResult.Summary.class)
                    .size(2)
                    .create();
            Pageable pageable = PageRequest.of(0, 10);
            PageImpl<OrderResult.Summary> result = new PageImpl<>(summaries, pageable, 2);
            given(orderQueryService.getOrders(anyLong(), any(OrderSearchCommand.class), any(Pageable.class)))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(get("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "latest"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.pageSize").value(10))
                    .andExpect(jsonPath("$.totalElement").value(2))
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[*].orderNo", containsInAnyOrder(
                            summaries.get(0).orderNo(),
                            summaries.get(1).orderNo()
                    )))
                    .andExpect(jsonPath("$.content[0].orderItems").isArray())
                    .andExpect(jsonPath("$.content[0].orderItems.length()").value(summaries.get(0).orderItems().size()));
        }

        @Test
        @DisplayName("주문 목록 조회시 권한은 유저여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void getOrders_Admin_role() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(get("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "latest"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문 목록을 조회할 수 없다")
        void getOrders_unAuthorized() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(get("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "latest"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }
    }

    private static Stream<Arguments> provideInvalidCreateOrderRequest() {
        return Stream.of(
                Arguments.of(
                        "주문서 아이디가 없는 경우 검증에 실패한다",
                        OrderCreateRequest.builder()
                                .orderSheetId(null)
                                .build(),
                        "orderSheetId",
                        "주문서 식별자(orderSheetId)는 필수 입니다."
                )
        );
    }
}
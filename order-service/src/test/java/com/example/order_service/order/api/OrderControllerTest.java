package com.example.order_service.order.api;

import com.example.order_service.common.dto.PageDto;
import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.order.api.dto.request.OrderRequest;
import com.example.order_service.order.api.dto.response.OrderResponse;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.support.annotation.WithCustomMockUser;
import com.example.order_service.support.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Stream;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @Nested
    @DisplayName("주문 생성")
    class Create {

        @Test
        @DisplayName("주문을 생성한다")
        @WithCustomMockUser
        void createOrder() throws Exception {
            //given
            OrderRequest.Create request = fixtureMonkey.giveMeOne(OrderRequest.Create.class);
            OrderResult.Create result = fixtureMonkey.giveMeOne(OrderResult.Create.class);
            given(orderFacade.initialOrder(any(OrderCommand.Create.class)))
                    .willReturn(result);
            OrderResponse.Create response = OrderResponse.Create.from(result);
            //when
            //then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isAccepted())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("주문 요청시 권한은 유저여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void createOrderWithAdminPrincipal() throws Exception {
            //given
            OrderRequest.Create request = fixtureMonkey.giveMeOne(OrderRequest.Create.class);
            //when
            //then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문을 생성할 수 없다")
        void createOrder_unAuthorized() throws Exception {
            //given
            OrderRequest.Create request = fixtureMonkey.giveMeOne(OrderRequest.Create.class);
            //when
            //then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("주문 요청시 유효성 검증에 실패하면 400 에러를 반환한다")
        @MethodSource("provideInvalidCreateOrderRequest")
        @WithCustomMockUser
        void createOrder_validation(String description, OrderRequest.Create request, String errorMessage) throws Exception {
            //given
            //when, then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

        private static Stream<Arguments> provideInvalidCreateOrderRequest() {
            return Stream.of(
                    Arguments.of(
                            "orderSheet id null",
                            OrderRequest.Create.builder()
                                    .orderSheetId(null)
                                    .build(),
                            "주문서 ID는 필수 입니다"
                    )
            );
        }
    }

    @Nested
    @DisplayName("주문 조회")
    class GetOrder {

        @Test
        @DisplayName("주문 정보를 조회한다")
        @WithCustomMockUser
        void getOrder() throws Exception {
            //given
            OrderResult.Detail result = fixtureMonkey.giveMeOne(OrderResult.Detail.class);
            given(orderQueryService.getOrder(anyString(), anyLong()))
                    .willReturn(result);
            OrderResponse.Detail response = OrderResponse.Detail.from(result);
            //when
            //then
            mockMvc.perform(get("/orders/{orderId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("주문 정보를 조회할때는 유저 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void getOrder_Admin_role() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(get("/orders/{orderNo}", "orderNo")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders/orderNo"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문 정보를 조회할 수 없다")
        void getOrder_unAuthorized() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(get("/orders/{orderNo}", "orderNo")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
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
            List<OrderResult.Summary> summaries = fixtureMonkey.giveMe(OrderResult.Summary.class, 2);
            Pageable pageable = PageRequest.of(0, 10);
            PageImpl<OrderResult.Summary> result = new PageImpl<>(summaries, pageable, 2);
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("page", "1");
            paramMap.add("size", "10");
            paramMap.add("sort", "latest");
            PageDto<OrderResponse.Summary> response = PageDto.of(result, OrderResponse.Summary::from);
            given(orderQueryService.getOrders(anyLong(), any(OrderSearchCommand.class), any(Pageable.class)))
                    .willReturn(result);

            //when
            //then
            mockMvc.perform(get("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .params(paramMap))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("주문 목록 조회시 권한은 유저여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void getOrders_Admin_role() throws Exception {
            //given
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("page", "1");
            paramMap.add("size", "10");
            paramMap.add("sort", "latest");
            //when
            //then
            mockMvc.perform(get("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .params(paramMap))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문 목록을 조회할 수 없다")
        void getOrders_unAuthorized() throws Exception {
            //given
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("page", "1");
            paramMap.add("size", "10");
            paramMap.add("sort", "latest");
            //when
            //then
            mockMvc.perform(get("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .params(paramMap))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

    }
}
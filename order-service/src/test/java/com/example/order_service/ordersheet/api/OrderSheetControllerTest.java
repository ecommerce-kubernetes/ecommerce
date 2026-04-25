package com.example.order_service.ordersheet.api;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.support.ControllerTestSupport;
import com.example.order_service.api.support.TestUtil;
import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import com.example.order_service.ordersheet.api.dto.request.OrderSheetRequest;
import com.example.order_service.ordersheet.api.dto.response.OrderSheetResponse;
import com.example.order_service.ordersheet.service.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.service.dto.result.OrderSheetResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
class OrderSheetControllerTest extends ControllerTestSupport {

    @Nested
    @DisplayName("주문서 저장")
    class Create {

        @Test
        @DisplayName("주문서를 저장한다")
        @WithCustomMockUser
        void createOrderSheet() throws Exception {
            //given
            OrderSheetRequest.Create request = TestUtil.nonNull(fixtureMonkey.giveMeOne(OrderSheetRequest.Create.class));
            OrderSheetResult.Default result = TestUtil.nonNull(fixtureMonkey.giveMeOne(OrderSheetResult.Default.class));
            given(orderSheetService.createOrderSheet(any(OrderSheetCommand.Create.class)))
                    .willReturn(result);
            OrderSheetResponse.Create response = OrderSheetResponse.Create.from(result);
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문서를 저장할 수 없다")
        void createOrderSheet_unAuthorized() throws Exception {
            //given
            OrderSheetRequest.Create request = TestUtil.nonNull(fixtureMonkey.giveMeOne(OrderSheetRequest.Create.class));
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets"));
        }

        @Test
        @DisplayName("유저 권한이 아니면 주문서를 저장할 수 없다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void createOrderSheet_forbidden() throws Exception {
            //given
            OrderSheetRequest.Create request = TestUtil.nonNull(fixtureMonkey.giveMeOne(OrderSheetRequest.Create.class));
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("주문서 저장 입력 검증 테스트")
        @MethodSource("provideInvalidCreateRequest")
        void createOrderSheet_validate(String description, OrderSheetRequest.Create req, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets"));
        }

        private static Stream<Arguments> provideInvalidCreateRequest() {
            OrderSheetRequest.OrderItem VALID_BASE_ITEM = OrderSheetRequest.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(3)
                    .build();
            return Stream.of(
                    Arguments.of(
                            "상품이 없음",
                            OrderSheetRequest.Create.builder().build(),
                            "주문 상품은 한개 이상이여야 합니다"
                    ),
                    Arguments.of(
                            "상품 변형 id가 null",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().productVariantId(null).build()))
                                    .build(),
                            "productVariantId는 필수값입니다"
                    ),
                    Arguments.of(
                            "수량이 null",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().quantity(null).build()))
                                    .build(),
                            "quantity는 필수값입니다"
                    ),
                    Arguments.of(
                            "수량이 1미만",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().quantity(0).build()))
                                    .build(),
                            "quantity는 1이상 이여야 합니다"
                    )
            );
        }
    }

}
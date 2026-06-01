package com.example.order_service.payment.api;

import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.payment.api.dto.request.PaymentRequest;
import com.example.order_service.payment.api.dto.response.PaymentResponse;
import com.example.order_service.payment.application.service.PaymentCommandService;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = PaymentController.class)
@Import(TestSecurityConfig.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PaymentFacade paymentFacade;

    @Nested
    @DisplayName("결제 승인")
    class Confirm {

        @Test
        @DisplayName("주문 결제를 승인한다")
        @WithCustomMockUser
        void paymentConfirm() throws Exception {
            //given
            PaymentRequest.Confirm request = fixtureMonkey.giveMeOne(PaymentRequest.Confirm.class);
            PaymentResult.PaymentApproval result = fixtureMonkey.giveMeOne(PaymentResult.PaymentApproval.class);
            PaymentResponse.PaymentApproval response = PaymentResponse.PaymentApproval.from(result);
            given(paymentFacade.confirm(any())).willReturn(result);
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 결제 승인 할 수 없다")
        void paymentConfirm_unAuthorized() throws Exception {
            //given
            PaymentRequest.Confirm request = fixtureMonkey.giveMeOne(PaymentRequest.Confirm.class);
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/payments/confirm"));
        }

        @Test
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        @DisplayName("권한이 없는 사용자는 결제 승인 할 수 없다")
        void paymentConfirm_forbidden() throws Exception {
            //given
            PaymentRequest.Confirm request = fixtureMonkey.giveMeOne(PaymentRequest.Confirm.class);
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/payments/confirm"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("결제 승인 입력 검증 테스트")
        @MethodSource("provideInvalidConfirm")
        @WithCustomMockUser
        void paymentConfirm_validation(String description, PaymentRequest.Confirm request, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/payments/confirm"));
        }

        static Stream<Arguments> provideInvalidConfirm() {
            PaymentRequest.Confirm BASE = PaymentRequest.Confirm.builder()
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(10000L)
                    .build();
            return Stream.of(
                    Arguments.of(
                            "주문 번호 미입력",
                            BASE.toBuilder().orderNo(null).build(),
                            "주문 번호는 필수입니다"
                    ),
                    Arguments.of(
                            "결제 키 미입력",
                            BASE.toBuilder().paymentKey(null).build(),
                            "결제 키는 필수입니다"
                    ),
                    Arguments.of(
                            "결제 금액 미입력",
                            BASE.toBuilder().amount(null).build(),
                            "결제 금액은 필수입니다"
                    ),
                    Arguments.of(
                            "결제 금액 0 미만 입력",
                            BASE.toBuilder().amount(0L).build(),
                            "결제 금액은 1원 미만일 수 없습니다"
                    )
            );
        }
    }
}

package com.example.order_service.payment.api.web;

import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.payment.api.web.dto.request.PaymentRequest;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.support.annotation.WithCustomMockUser;
import com.example.order_service.support.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
            PaymentRequest.Confirm request = Instancio.of(PaymentRequest.Confirm.class)
                    .set(field("amount"), 1000L)
                    .create();
            PaymentResult.PaymentApproval result = Instancio.create(PaymentResult.PaymentApproval.class);
            given(paymentFacade.confirm(any())).willReturn(result);
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentKey").value(result.paymentKey()))
                    .andExpect(jsonPath("$.orderNo").value(result.orderNo()))
                    .andExpect(jsonPath("$.totalAmount").isNumber());
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 결제 승인 할 수 없다")
        void paymentConfirm_unAuthorized() throws Exception {
            //given
            PaymentRequest.Confirm request = Instancio.of(PaymentRequest.Confirm.class)
                    .set(field("amount"), 1000L)
                    .create();
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/payments/confirm"));
        }

        @Test
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        @DisplayName("권한이 없는 사용자는 결제 승인 할 수 없다")
        void paymentConfirm_forbidden() throws Exception {
            //given
            PaymentRequest.Confirm request = Instancio.of(PaymentRequest.Confirm.class)
                    .set(field("amount"), 1000L)
                    .create();
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/payments/confirm"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("결제 승인 입력 검증 테스트")
        @MethodSource("provideInvalidConfirm")
        @WithCustomMockUser
        void paymentConfirm_validation(String description, PaymentRequest.Confirm request, String expectedField, String expectedMessage) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                    .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                    .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
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
                            "주문 번호가 없으면 검증에 실패한다",
                            BASE.toBuilder().orderNo(null).build(),
                            "orderNo",
                            "주문 번호는 필수입니다."
                    ),
                    Arguments.of(
                            "결제 키가 없으면 검증에 실패한다",
                            BASE.toBuilder().paymentKey(null).build(),
                            "paymentKey",
                            "결제 키는 필수입니다."
                    ),
                    Arguments.of(
                            "결제 금액이 없으면 검증에 실패한다",
                            BASE.toBuilder().amount(null).build(),
                            "amount",
                            "결제 금액은 필수입니다."
                    ),
                    Arguments.of(
                            "결제 금액이 1원보다 작으면 검증에 실패한다.",
                            BASE.toBuilder().amount(0L).build(),
                            "amount",
                            "결제 금액은 1원 이상이어야 합니다."
                    )
            );
        }
    }
}

package com.example.order_service.payment.adapter.in.web;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.payment.adapter.in.web.dto.request.PaymentConfirmRequest;
import com.example.order_service.payment.adapter.in.web.dto.request.PaymentCreateRequest;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentConfirmResult;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.support.annotation.WithCustomMockUser;
import com.example.order_service.support.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("결제를 생성한다.")
    @WithCustomMockUser
    void createPayment() throws Exception {
        //given
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(1L)
                .build();

        PaymentCreateResult result = PaymentCreateResult.from(1L);

        given(paymentFacade.create(any(PaymentCreateCommand.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(result.paymentId()));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 결제를 생성할 수 없다.")
    void createPayment_unAuthorized() throws Exception {
        //given
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(1L)
                .build();
        //when
        //then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/payments"));
    }

    @Test
    @DisplayName("권한이 부족한 사용자는 결제를 생성할 수 없다.")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void createPayment_forbidden() throws Exception {
        //given
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(1L)
                .build();
        //when
        //then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/payments"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("결제 생성 입력 검증")
    @MethodSource("provideInvalidCreateRequest")
    @WithCustomMockUser
    void createPayment_validation(String description, PaymentCreateRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/payments"));
    }

    @Test
    @DisplayName("주문 결제를 승인한다")
    @WithCustomMockUser
    void paymentConfirm() throws Exception {
        //given
        Long paymentId = 1L;
        PaymentConfirmRequest request = Instancio.of(PaymentConfirmRequest.class)
                .set(field("amount"), 1000L)
                .set(field("provider"), "TOSS")
                .create();
        PaymentConfirmResult result = Instancio.create(PaymentConfirmResult.class);
        given(paymentFacade.approve(any(PaymentConfirmCommand.class))).willReturn(result);
        //when
        //then
        mockMvc.perform(post("/payments/{paymentId}/confirm", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(result.paymentId()));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 결제 승인 할 수 없다")
    void paymentConfirm_unAuthorized() throws Exception {
        //given
        Long paymentId = 1L;
        PaymentConfirmRequest request = Instancio.of(PaymentConfirmRequest.class)
                .set(field("amount"), 1000L)
                .set(field("provider"), "TOSS")
                .create();
        //when
        //then
        mockMvc.perform(post("/payments/{paymentId}/confirm", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/payments/" + paymentId + "/confirm"));
    }

    @Test
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    @DisplayName("권한이 없는 사용자는 결제 승인 할 수 없다")
    void paymentConfirm_forbidden() throws Exception {
        //given
        Long paymentId = 1L;
        PaymentConfirmRequest request = Instancio.of(PaymentConfirmRequest.class)
                .set(field("amount"), 1000L)
                .set(field("provider"), "TOSS")
                .create();
        //when
        //then
        mockMvc.perform(post("/payments/{paymentId}/confirm", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/payments/" + paymentId + "/confirm"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("결제 승인 입력 검증 테스트")
    @MethodSource("provideInvalidConfirm")
    @WithCustomMockUser
    void paymentConfirm_validation(String description, PaymentConfirmRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long paymentId = 1L;
        //when
        //then
        mockMvc.perform(post("/payments/{paymentId}/confirm", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/payments/" + paymentId + "/confirm"));
    }

    private static Stream<Arguments> provideInvalidConfirm() {
        return Stream.of(
                Arguments.of(
                        "결제 키가 없으면 검증에 실패한다",
                        PaymentConfirmRequest.builder()
                                .paymentKey(null)
                                .amount(1000L)
                                .provider("TOSS")
                                .build(),
                        "paymentKey",
                        "결제 키는 필수입니다."
                ),
                Arguments.of(
                        "결제 금액이 없으면 검증에 실패한다",
                        PaymentConfirmRequest.builder()
                                .paymentKey("paymentKey")
                                .amount(null)
                                .provider("TOSS")
                                .build(),
                        "amount",
                        "결제 금액은 필수입니다."
                ),
                Arguments.of(
                        "결제 금액이 1원보다 작으면 검증에 실패한다.",
                        PaymentConfirmRequest.builder()
                                .paymentKey("paymentKey")
                                .amount(0L)
                                .provider("TOSS")
                                .build(),
                        "amount",
                        "결제 금액은 1원 이상이어야 합니다."
                ),
                Arguments.of(
                        "결제사가 누락되면 검증에 실패한다.",
                        PaymentConfirmRequest.builder()
                                .paymentKey("paymentKey")
                                .amount(1000L)
                                .provider(null)
                                .build(),
                        "provider",
                        "결제사는 필수입니다."
                )
        );
    }

    private static Stream<Arguments> provideInvalidCreateRequest() {
        return Stream.of(
                Arguments.of(
                        "주문 아이디가 누락되면 검증에 실패한다.",
                        PaymentCreateRequest.builder()
                                .orderId(null)
                                .build(),
                        "orderId",
                        "주문 식별자는 필수입니다."
                )
        );
    }
}

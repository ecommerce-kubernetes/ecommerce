package com.example.order_service.docs.payment;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.PaymentDescriptor;
import com.example.order_service.payment.api.web.PaymentController;
import com.example.order_service.payment.api.web.dto.request.PaymentConfirmRequest;
import com.example.order_service.payment.api.web.dto.request.PaymentCreateRequest;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentConfirmResult;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.support.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerDocsTest extends RestDocSupport {
    private PaymentFacade paymentFacade = mock(PaymentFacade.class);

    @Override
    protected Object initController() {
        return new PaymentController(paymentFacade);
    }

    @Test
    @DisplayName("결제를 생성한다.")
    void createPayment() throws Exception {
        //given
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(1L)
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_USER");
        PaymentCreateResult result = PaymentCreateResult.builder()
                .paymentId(1L)
                .orderId(1L)
                .orderName("상품")
                .totalAmount(Money.wons(10000L))
                .build();
        given(paymentFacade.create(any(PaymentCreateCommand.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document(
                        "payments/create",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(PaymentDescriptor.createRequest()),
                        responseFields(PaymentDescriptor.createResponse())
                ));
    }

    @Test
    @DisplayName("주문 결제를 승인한다")
    void paymentConfirm() throws Exception {
        //given
        Long paymentId = 1L;
        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .paymentKey("paymentKey")
                .amount(10000L)
                .provider("TOSS")
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_USER");
        PaymentConfirmResult result = PaymentConfirmResult.builder()
                .paymentId(1L)
                .build();
        given(paymentFacade.approve(any(PaymentConfirmCommand.class))).willReturn(result);
        //when
        //then
        mockMvc.perform(post("/payments/{paymentId}/confirm", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document(
                        "payments/confirm",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(PaymentDescriptor.confirmRequest()),
                        responseFields(PaymentDescriptor.confirmResponse())
                ));
    }

}

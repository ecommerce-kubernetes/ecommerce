package com.example.order_service.docs.payment;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.PaymentDescriptor;
import com.example.order_service.payment.api.web.PaymentController;
import com.example.order_service.payment.api.web.dto.request.PaymentConfirmRequest;
import com.example.order_service.payment.api.web.dto.request.PaymentCreateRequest;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.example.order_service.support.RestDocSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

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
        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .orderId(1L)
                .paymentKey("paymentKey")
                .amount(10000L)
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_USER");
        PaymentResult.PaymentApproval result = PaymentResult.PaymentApproval.builder()
                .paymentId(1L)
                .paymentKey("paymentKey")
                .orderNo("orderNo")
                .totalAmount(Money.wons(10000L))
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.DONE)
                .approvedAt(LocalDateTime.now())
                .build();
        given(paymentFacade.confirm(any())).willReturn(result);
        //when
        //then
        mockMvc.perform(post("/payments/confirm")
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
                        requestFields(PaymentDescriptor.getConfirmRequest()),
                        responseFields(PaymentDescriptor.getApprovalResponse())
                ));
    }

}

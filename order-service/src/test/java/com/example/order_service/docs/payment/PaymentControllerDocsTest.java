package com.example.order_service.docs.payment;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.PaymentDescriptor;
import com.example.order_service.payment.api.PaymentController;
import com.example.order_service.payment.api.dto.request.PaymentRequest;
import com.example.order_service.payment.api.dto.response.PaymentResponse;
import com.example.order_service.payment.application.service.PaymentCommandService;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.example.order_service.support.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerDocsTest extends RestDocSupport {
    private PaymentFacade paymentFacade = mock(PaymentFacade.class);

    @Override
    protected String getTag() {
        return "PAYMENT";
    }

    @Override
    protected Object initController() {
        return new PaymentController(paymentFacade);
    }

    @Nested
    @DisplayName("결제 승인")
    class PaymentConfirm {
        @Test
        @DisplayName("주문 결제를 승인한다")
        void paymentConfirm() throws Exception {
            //given
            PaymentRequest.Confirm request = PaymentRequest.Confirm.builder()
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(10000L)
                    .build();
            HttpHeaders authHeader = createAuthHeader("ROLE_USER");
            PaymentResult.PaymentApproval result = PaymentResult.PaymentApproval.builder()
                    .paymentKey("paymentKey")
                    .orderNo("orderNo")
                    .totalAmount(Money.wons(10000L))
                    .method(PaymentMethod.CARD)
                    .status(PaymentStatus.DONE)
                    .approvedAt(LocalDateTime.now())
                    .build();
            given(paymentFacade.confirm(any())).willReturn(result);
            PaymentResponse.PaymentApproval response = PaymentResponse.PaymentApproval.from(result);
            //when
            //then
            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(authHeader)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)))
                    .andDo(createSecuredDocument("04-payment-01-confirm",
                            "결제 승인",
                            "주문 결제를 승인한다",
                            PaymentDescriptor.getConfirmRequest(),
                            PaymentDescriptor.getApprovalResponse()
                    ));
        }
    }
}

package com.example.order_service.api.order.infrastructure.client.payment;

import com.example.order_service.api.order.infrastructure.client.payment.dto.request.TossPaymentConfirmRequest;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@AutoConfigureWireMock(port = 0)
public class TossPaymentClientTest extends ExcludeInfraTest {

    @Autowired
    private TossPaymentClient client;

    @DynamicPropertySource
    static void configureProperties (DynamicPropertyRegistry registry) {
        registry.add("payment.toss.url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    @DisplayName("토스 페이먼츠에 결제 승인을 요청할때 헤더에 시크릿 키를 추가해야한다")
    void confirmPayment_interceptor(){
        //given
        stubFor(post(urlMatching("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "status": "DONE", "method": "CARD" }
                                """)));
        TossPaymentConfirmRequest request = TossPaymentConfirmRequest
                .builder()
                .orderId(1L)
                .paymentKey("paymentKey")
                .amount(3000L)
                .build();
        //when
        client.confirmPayment(request);
        //then

        verify(postRequestedFor(urlMatching("/v1/payments/confirm"))
                .withHeader("Authorization", matching("Basic .*")));
    }
}

package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.infrastructure.dto.request.TossClientRequest;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;

import static com.example.order_service.support.TestFixtureUtil.giveMeOne;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
public class TossFeignClientTest {

    @Autowired
    private TossFeignClient client;

    // wireMock 주소를 토스 url로 변경
    @DynamicPropertySource
    static void configureProperties (DynamicPropertyRegistry registry) {
        registry.add("payment.toss.url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Nested
    @DisplayName("결제 승인")
    class Confirm {

        @Test
        @DisplayName("토스 결제 승인을 요청한다")
        void confirmPayment() {
            //given
            TossClientRequest.Confirm request = giveMeOne(TossClientRequest.Confirm.class);
            String mockJsonResponse = """
                    {
                        "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1",
                        "orderId": "a4CWyWY5m89PNh7xJwhk1",
                        "status": "DONE",
                        "method": "카드",
                        "totalAmount": 1000,
                        "approvedAt": "2024-02-13T12:18:14+09:00"
                    }
                    """;

            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockJsonResponse)));
            //when
            TossClientResponse.Confirm response = client.confirmPayment(request);
            //then
            assertThat(response.paymentKey()).isEqualTo("5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1");
            assertThat(response.orderId()).isEqualTo("a4CWyWY5m89PNh7xJwhk1");
            assertThat(response.status()).isEqualTo("DONE");
            assertThat(response.method()).isEqualTo("카드");
            assertThat(response.totalAmount()).isEqualTo(1000);
            assertThat(response.approvedAt())
                    .isEqualTo(OffsetDateTime.parse("2024-02-13T12:18:14+09:00"));
        }

        @Test
        @DisplayName("토스 결제 승인을 요청할때 헤더에 시크릿 키를 포함하여 요청한다")
        void confirmPayment_header_contain_auth_key() {
            //given
            TossClientRequest.Confirm request = giveMeOne(TossClientRequest.Confirm.class);
            String mockJsonResponse = """
                    {
                        "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1",
                        "orderId": "a4CWyWY5m89PNh7xJwhk1",
                        "status": "DONE",
                        "method": "카드",
                        "totalAmount": 1000,
                        "approvedAt": "2024-02-13T12:18:14+09:00"
                    }
                    """;
            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockJsonResponse)));
            //when
            client.confirmPayment(request);
            //then
            verify(postRequestedFor(urlMatching("/v1/payments/confirm"))
                    .withHeader("Authorization", matching("Basic .*")));
        }

        @Test
        @DisplayName("토스 페이먼츠에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
        void tossPayment_thrown_client_error_response() {
            //given
            TossClientRequest.Confirm request = giveMeOne(TossClientRequest.Confirm.class);
            String mockJsonResponse = """
                    {
                        "code": "ALREADY_PROCESSED_PAYMENT",
                        "message": "이미 처리된 결제 입니다."
                    }
                    """;
            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                            .willReturn(aResponse()
                                    .withStatus(400)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.confirmPayment(request))
                    .isInstanceOf(ExternalClientException.class)
                    .hasMessage("이미 처리된 결제 입니다.")
                    .extracting("errorCode")
                    .isEqualTo("ALREADY_PROCESSED_PAYMENT");
        }

        @Test
        @DisplayName("토스 페이먼츠에서 서버 오류 응답 반환시 서버 예외를 던진다")
        void tossPayment_thrown_server_error_response() {
            //given
            TossClientRequest.Confirm request = giveMeOne(TossClientRequest.Confirm.class);
            String mockJsonResponse = """
                    {
                        "code": "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                        "message": "결제가 완료되지 않았어요. 다시 시도해주세요."
                    }
                    """;

            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.confirmPayment(request))
                    .isInstanceOf(ExternalClientException.class)
                    .hasMessage("결제가 완료되지 않았어요. 다시 시도해주세요.")
                    .extracting("errorCode")
                    .isEqualTo("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING");
        }
    }
}

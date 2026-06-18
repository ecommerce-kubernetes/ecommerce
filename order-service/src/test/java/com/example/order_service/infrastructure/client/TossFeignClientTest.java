package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.TossClientRequest;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
public class TossFeignClientTest {

    @Autowired
    private TossFeignClient client;
    @Autowired
    private ObjectMapper objectMapper;

    // wireMock 주소를 토스 url로 변경
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.toss.url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Nested
    @DisplayName("결제 승인")
    class Confirm {

        @Test
        @DisplayName("토스 결제 승인을 요청한다")
        void confirmPayment() throws JsonProcessingException {
            //given
            TossClientRequest.Confirm request = Instancio.create(TossClientRequest.Confirm.class);
            String expectedRequestBody = objectMapper.writeValueAsString(request);
            String mockJsonResponse = """
                    {
                        "status": "DONE",
                        "method": "카드",
                        "totalAmount": 1000,
                        "approvedAt": "2024-02-13T12:18:14+09:00",
                        "lastTransactionKey": "9C62B18EEF0DE3EB7F4422EB6D14BC6E"
                    }
                    """;
            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                    .withRequestBody(equalToJson(expectedRequestBody))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockJsonResponse)));
            //when
            TossClientResponse.Confirm response = client.confirmPayment(request);
            //then
            assertThat(response.status()).isEqualTo("DONE");
            assertThat(response.method()).isEqualTo("카드");
            assertThat(response.totalAmount()).isEqualTo(1000);
            assertThat(response.lastTransactionKey()).isEqualTo("9C62B18EEF0DE3EB7F4422EB6D14BC6E");
            assertThat(response.approvedAt())
                    .isEqualTo(OffsetDateTime.parse("2024-02-13T12:18:14+09:00"));
        }

        @Test
        @DisplayName("토스 결제 승인을 요청할때 헤더에 시크릿 키를 포함하여 요청한다")
        void confirmPayment_header_contain_auth_key() throws JsonProcessingException {
            //given
            TossClientRequest.Confirm request = Instancio.create(TossClientRequest.Confirm.class);
            String expectedRequestBody = objectMapper.writeValueAsString(request);
            String mockJsonResponse = """
                    {
                        "status": "DONE",
                        "method": "카드",
                        "totalAmount": 1000,
                        "approvedAt": "2024-02-13T12:18:14+09:00",
                        "lastTransactionKey": "9C62B18EEF0DE3EB7F4422EB6D14BC6E"
                    }
                    """;
            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                    .withRequestBody(equalToJson(expectedRequestBody))
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
        void tossPayment_thrown_client_error_response() throws JsonProcessingException {
            //given
            TossClientRequest.Confirm request = Instancio.create(TossClientRequest.Confirm.class);
            String expectedRequestBody = objectMapper.writeValueAsString(request);
            String mockJsonResponse = """
                    {
                        "code": "ALREADY_PROCESSED_PAYMENT",
                        "message": "이미 처리된 결제 입니다."
                    }
                    """;
            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                    .withRequestBody(equalToJson(expectedRequestBody))
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
        void tossPayment_thrown_server_error_response() throws JsonProcessingException {
            //given
            TossClientRequest.Confirm request = Instancio.create(TossClientRequest.Confirm.class);
            String expectedRequestBody = objectMapper.writeValueAsString(request);
            String mockJsonResponse = """
                    {
                        "code": "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                        "message": "결제가 완료되지 않았어요. 다시 시도해주세요."
                    }
                    """;

            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                    .withRequestBody(equalToJson(expectedRequestBody))
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

    @Nested
    @DisplayName("결제 취소")
    class Cancel {

        @Test
        @DisplayName("토스 결제 취소를 요청한다")
        void cancelPayment() throws JsonProcessingException {
            //given
            String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
            TossClientRequest.Cancel request = Instancio.create(TossClientRequest.Cancel.class);
            String expectedRequestBody = objectMapper.writeValueAsString(request);
            String mockJsonResponse = """
                    {
                        "status": "CANCELED",
                        "cancels": [
                          {
                            "transactionKey": "090A796806E726BBB929F4A2CA7DB9A7",
                            "cancelReason": "테스트 결제 취소",
                            "canceledAt": "2024-02-13T12:20:23+09:00",
                            "cancelAmount": 1000
                          }
                        ]
                    }
                    """;
            stubFor(post(urlEqualTo("/v1/payments/" + paymentKey + "/cancel"))
                    .withRequestBody(equalToJson(expectedRequestBody))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockJsonResponse)));
            //when
            TossClientResponse.Cancel response = client.cancelPayment(paymentKey, request);
            //then
            assertThat(response.status()).isEqualTo("CANCELED");
            TossClientResponse.CancelReceipt cancel = response.cancels().getFirst();

            assertThat(cancel.transactionKey()).isEqualTo("090A796806E726BBB929F4A2CA7DB9A7");
            assertThat(cancel.cancelReason()).isEqualTo("테스트 결제 취소");
            assertThat(cancel.cancelAmount()).isEqualTo(1000L);
            assertThat(cancel.canceledAt())
                    .isEqualTo(OffsetDateTime.parse("2024-02-13T12:20:23+09:00"));
        }

        @Test
        @DisplayName("토스 결제 취소를 요청할때 헤더에 시크릿 키를 포함하여 요청한다")
        void cancelPayment_header_contain_auth_key() throws JsonProcessingException {
            //given
            String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
            TossClientRequest.Cancel request = Instancio.create(TossClientRequest.Cancel.class);
            String expectedRequestBody = objectMapper.writeValueAsString(request);
            String mockJsonResponse = """
                    {
                        "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1",
                        "orderId": "a4CWyWY5m89PNh7xJwhk1",
                        "status": "CANCELED",
                        "approvedAt": "2024-02-13T12:18:14+09:00",
                        "totalAmount": 1000,
                        "method": "카드"
                    }
                    """;
            stubFor(post(urlEqualTo("/v1/payments/" + paymentKey + "/cancel"))
                    .withRequestBody(equalToJson(expectedRequestBody))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockJsonResponse)));
            //when
            client.cancelPayment(paymentKey, request);
            //then
            verify(postRequestedFor(urlMatching("/v1/payments/" + paymentKey + "/cancel"))
                    .withHeader("Authorization", matching("Basic .*")));
        }

        @Test
        @DisplayName("토스 페이먼츠에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
        void tossPayment_thrown_client_error_response() throws JsonProcessingException {
            //given
            String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
            TossClientRequest.Cancel request = Instancio.create(TossClientRequest.Cancel.class);
            String expectedRequestBody = objectMapper.writeValueAsString(request);
            String mockJsonResponse = """
                    {
                        "code": "ALREADY_CANCELED_PAYMENT",
                        "message": "이미 취소된 결제 입니다."
                    }
                    """;
            stubFor(post(urlEqualTo("/v1/payments/" + paymentKey + "/cancel"))
                    .withRequestBody(equalToJson(expectedRequestBody))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.cancelPayment(paymentKey, request))
                    .isInstanceOf(ExternalClientException.class)
                    .hasMessage("이미 취소된 결제 입니다.")
                    .extracting("errorCode")
                    .isEqualTo("ALREADY_CANCELED_PAYMENT");
        }

        @Test
        @DisplayName("토스 페이먼츠에서 서버 오류 응답 반환시 서버 예외를 던진다")
        void tossPayment_thrown_server_error_response() throws JsonProcessingException {
            //given
            String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
            TossClientRequest.Cancel request = Instancio.create(TossClientRequest.Cancel.class);
            String expectedRequestBody = objectMapper.writeValueAsString(request);
            String mockJsonResponse = """
                    {
                        "code": "FAILED_REFUND_PROCESS",
                        "message": "은행 응답시간 지연이나 일시적인 오류로 환불요청에 실패했습니다."
                    }
                    """;

            stubFor(post(urlEqualTo("/v1/payments/" + paymentKey + "/cancel"))
                    .withRequestBody(equalToJson(expectedRequestBody))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.cancelPayment(paymentKey, request))
                    .isInstanceOf(ExternalServerException.class)
                    .hasMessage("은행 응답시간 지연이나 일시적인 오류로 환불요청에 실패했습니다.")
                    .extracting("errorCode")
                    .isEqualTo("FAILED_REFUND_PROCESS");
        }
    }
}

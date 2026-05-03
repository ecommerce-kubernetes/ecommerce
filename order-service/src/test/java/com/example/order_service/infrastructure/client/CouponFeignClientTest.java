package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.support.TestFixtureUtil;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
public class CouponFeignClientTest {

    @Autowired
    private CouponFeignClient client;

    @Test
    @DisplayName("쿠폰 서비스에서 쿠폰 정보를 조회한다")
    void calculate() {
        //given
        // 요청 응답 모킹
        CouponClientRequest.Calculate request = TestFixtureUtil.giveMeOne(CouponClientRequest.Calculate.class);
        String mockJsonResponse = """
                {
                    "code": "SUCCESS",
                    "discountBenefit": {
                        "couponId": 1,
                        "couponName": "쿠폰 이름",
                        "discountAmount": 5000
                    }
                }
                """;

        stubFor(post(urlEqualTo("/internal/coupons/calculate"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        CouponClientResponse.Calculate response = client.calculate(request);
        //then
        assertThat(response.code()).isEqualTo("SUCCESS");
        assertThat(response.discountBenefit().couponId()).isEqualTo(1L);
        assertThat(response.discountBenefit().couponName()).isEqualTo("쿠폰 이름");
        assertThat(response.discountBenefit().discountAmount()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("쿠폰 서비스에서 서버 오류 응답 반환시 서버 예외를 던진다")
    void couponService_thrown_server_error_response(){
        //given
        CouponClientRequest.Calculate request = TestFixtureUtil.giveMeOne(CouponClientRequest.Calculate.class);
        String mockJsonResponse = """
                {
                    "code": "COUPON_SERVER_ERROR",
                    "message": "에러가 발생했습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/coupons/calculate"
                }
                """;

        stubFor(post(urlEqualTo("/internal/coupons/calculate"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.calculate(request))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("에러가 발생했습니다");
    }

    @Test
    @DisplayName("")
    void couponService_thrown_client_error_response(){
        //given
        CouponClientRequest.Calculate request = TestFixtureUtil.giveMeOne(CouponClientRequest.Calculate.class);
        String mockJsonResponse = """
                {
                    "code": "COUPON_CLIENT_ERROR",
                    "message": "에러가 발생했습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/coupons/calculate"
                }
                """;
        stubFor(post(urlEqualTo("/internal/coupons/calculate"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.calculate(request))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("에러가 발생했습니다");
    }
}

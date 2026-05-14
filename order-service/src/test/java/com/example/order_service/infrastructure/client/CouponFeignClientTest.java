package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
public class CouponFeignClientTest {

    @Autowired
    private CouponFeignClient client;

    @Test
    @DisplayName("적용하려는 쿠폰의 할인 정보와 적용 가능 여부를 계산한다")
    void calculate() {
        //given
        CouponClientRequest.Item item = CouponClientRequest.Item.builder()
                .productVariantId(1L)
                .price(10000L)
                .quantity(3)
                .itemCouponId(5L)
                .build();
        CouponClientRequest.Calculate request = CouponClientRequest.Calculate.builder()
                .userId(1L)
                .cartCouponId(1L)
                .items(List.of(item))
                .build();
        String mockJsonResponse = """
                {
                    "cartCoupon": {
                        "couponId": 1,
                        "couponName": "최초 주문 10% 할인",
                        "discountAmount": 15000
                    },
                    "itemCoupons": [
                        {
                            "productVariantId": 1,
                            "couponId": 2,
                            "couponName": "아우터 1000원 할인",
                            "discountAmount": 1000
                        }
                    ]
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
        assertThat(response.cartCoupon())
                .extracting("couponId", "couponName", "discountAmount")
                .containsExactlyInAnyOrder(1L, "최초 주문 10% 할인", 15000L);

        assertThat(response.itemCoupons())
                .extracting("productVariantId", "couponId", "couponName", "discountAmount")
                .containsExactlyInAnyOrder(
                        tuple(1L, 2L, "아우터 1000원 할인", 1000L)
                );
    }

    @Test
    @DisplayName("쿠폰 검증 클라이언트 오류 응답 반환시 예외가 발생한다")
    void calculate_thrown_client_error() {
        //given
        CouponClientRequest.Item item = CouponClientRequest.Item.builder()
                .productVariantId(1L)
                .price(10000L)
                .quantity(3)
                .itemCouponId(5L)
                .build();
        CouponClientRequest.Calculate request = CouponClientRequest.Calculate.builder()
                .userId(1L)
                .cartCouponId(1L)
                .items(List.of(item))
                .build();

        String mockJsonResponse = """
                {
                    "code": "COUPON_EXPIRED",
                    "message": "쿠폰이 만료되었습니다",
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
                .hasMessage("쿠폰이 만료되었습니다")
                .extracting("errorCode")
                .isEqualTo("COUPON_EXPIRED");
    }

    @Test
    @DisplayName("쿠폰 검증 서버 오류 응답 반환시 예외가 발생한다")
    void calculate_thrown_server_error() {
        //given
        CouponClientRequest.Item item = CouponClientRequest.Item.builder()
                .productVariantId(1L)
                .price(10000L)
                .quantity(3)
                .itemCouponId(5L)
                .build();
        CouponClientRequest.Calculate request = CouponClientRequest.Calculate.builder()
                .userId(1L)
                .cartCouponId(1L)
                .items(List.of(item))
                .build();

        String mockJsonResponse = """
                {
                    "code": "FAILED_INTERNAL_SYSTEM_PROCESSING",
                    "message": "처리중 알 수 없는 오류가 발생했습니다",
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
                .hasMessage("처리중 알 수 없는 오류가 발생했습니다")
                .extracting("errorCode")
                .isEqualTo("FAILED_INTERNAL_SYSTEM_PROCESSING");
    }
}

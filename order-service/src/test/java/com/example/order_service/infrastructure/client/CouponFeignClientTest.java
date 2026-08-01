package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.ItemCouponsRequest;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
public class CouponFeignClientTest {

    @Autowired
    private CouponFeignClient client;
    @Autowired
    private ObjectMapper objectMapper;

    private String readJson(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("상품 쿠폰 정보를 조회한다. (정액 쿠폰)")
    void getItemCoupon_fixed() throws IOException {
        //given
        Long userId = 1L;
        Long itemCouponId = 1L;
        String mockJsonResponse = readJson("coupon/fix-item-coupon-response.json");
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/item-coupons/" + itemCouponId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        ItemCouponResponse response = client.getItemCoupon(userId, itemCouponId);
        //then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.applyQuantityLimit()).isEqualTo(3);
        assertThat(response.discountType()).isEqualTo("FIXED");
        assertThat(response.discountAmount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("상품 쿠폰 정보를 조회한다. (정률 쿠폰)")
    void getItemCoupon_rate() throws IOException {
        //given
        Long userId = 1L;
        Long itemCouponId = 1L;
        String mockJsonResponse = readJson("coupon/rate-item-coupon-response.json");
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/item-coupons/" + itemCouponId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        ItemCouponResponse response = client.getItemCoupon(userId, itemCouponId);
        //then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.applyQuantityLimit()).isEqualTo(1);
        assertThat(response.discountType()).isEqualTo("RATE");
        assertThat(response.discountRate()).isEqualTo(10);
        assertThat(response.maxDiscountAmount()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("상품 쿠폰 조회 클라이언트 오류 응답 반환시 예외가 발생한다")
    void getItemCoupon_thrown_client_error() {
        //given
        Long userId =1L;
        Long itemCouponId = 1L;
        String mockJsonResponse = """
                {
                    "code": "COUPON_EXPIRED",
                    "message": "쿠폰이 만료되었습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/coupons/calculate"
                }
                """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/item-coupons/" + itemCouponId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getItemCoupon(userId, itemCouponId))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("쿠폰이 만료되었습니다")
                .extracting("errorCode")
                .isEqualTo("COUPON_EXPIRED");
    }

    @Test
    @DisplayName("상품 쿠폰 조회 서버 오류 응답 반환시 예외가 발생한다")
    void getItemCoupon_thrown_server_error() {
        //given
        Long userId =1L;
        Long itemCouponId = 1L;
        String mockJsonResponse = """
                {
                    "code": "FAILED_INTERNAL_SYSTEM_PROCESSING",
                    "message": "처리중 알 수 없는 오류가 발생했습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/coupons/calculate"
                }
                """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/item-coupons/" + itemCouponId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getItemCoupon(userId, itemCouponId))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("처리중 알 수 없는 오류가 발생했습니다")
                .extracting("errorCode")
                .isEqualTo("FAILED_INTERNAL_SYSTEM_PROCESSING");
    }

    @Test
    @DisplayName("상품 쿠폰 정보를 조회한다.")
    void getItemCoupons() throws IOException {
        //given
        Long userId =1L;
        List<Long> itemCouponIds = List.of(1L, 2L);
        ItemCouponsRequest request = ItemCouponsRequest.builder().itemCouponIds(itemCouponIds).build();
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = readJson("coupon/item-coupons-response.json");

        stubFor(post(urlEqualTo("/internal/users/" + userId + "/item-coupons"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        ItemCouponsResponse response = client.getItemCoupons(userId, request);
        //then
        assertThat(response.itemCoupons()).hasSize(2);
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(expected());
    }

    @Test
    @DisplayName("상품 쿠폰 정보 조회시 클라이언트 에러 응답이 반환된 경우 예외가 발생한다.")
    void getItemCoupons_thrown_client_error() throws JsonProcessingException {
        //given
        Long userId =1L;
        List<Long> itemCouponIds = List.of(1L, 2L);
        ItemCouponsRequest request = ItemCouponsRequest.builder().itemCouponIds(itemCouponIds).build();
        String expectedRequestBody = objectMapper.writeValueAsString(request);

        String mockJsonResponse = """
                {
                    "code": "PERMISSION_DENIED",
                    "message": "조회 권한이 부족합니다.",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/users/1/item-coupons"
                }
                """;

        stubFor(post(urlEqualTo("/internal/users/" + userId + "/item-coupons"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.FORBIDDEN.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getItemCoupons(userId, request))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("조회 권한이 부족합니다.")
                .extracting("errorCode")
                .isEqualTo("PERMISSION_DENIED");
    }

    @Test
    @DisplayName("상품 쿠폰 정보 조회시 서버 에러 응답이 반환된 경우 예외가 발생한다.")
    void getItemCoupons_thrown_server_error() throws JsonProcessingException {
        //given
        Long userId =1L;
        List<Long> itemCouponIds = List.of(1L, 2L);
        ItemCouponsRequest request = ItemCouponsRequest.builder().itemCouponIds(itemCouponIds).build();
        String expectedRequestBody = objectMapper.writeValueAsString(request);

        String mockJsonResponse = """
                {
                    "code": "INTERNAL_SERVER_ERROR",
                    "message": "알 수 없는 에러가 발생했습니다.",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/users/1/item-coupons"
                }
                """;

        stubFor(post(urlEqualTo("/internal/users/" + userId + "/item-coupons"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getItemCoupons(userId, request))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("알 수 없는 에러가 발생했습니다.")
                .extracting("errorCode")
                .isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("장바구니 쿠폰을 조회한다 (정액 할인)")
    void getCartCoupon_fixed() throws IOException {
        //given
        Long userId =1L;
        Long cartCouponId = 1L;
        String mockJsonResponse = readJson("coupon/fix-cart-coupon-response.json");

        stubFor(get(urlEqualTo("/internal/users/" + userId + "/cart-coupons/" + cartCouponId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        CartCouponResponse response = client.getCartCoupon(userId, cartCouponId);
        //then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.cartCouponId()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("장바구니 1000원 할인 쿠폰");
        assertThat(response.minimumPaymentAmount()).isEqualTo(50000L);
        assertThat(response.discountType()).isEqualTo("FIXED");
        assertThat(response.discountAmount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 조회한다 (정률 할인)")
    void getCartCoupon_rate() throws IOException {
        //given
        Long userId =1L;
        Long cartCouponId = 1L;
        String mockJsonResponse = readJson("coupon/rate-cart-coupon-response.json");

        stubFor(get(urlEqualTo("/internal/users/" + userId + "/cart-coupons/" + cartCouponId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        CartCouponResponse response = client.getCartCoupon(userId, cartCouponId);
        //then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.cartCouponId()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("장바구니 5% 할인 쿠폰");
        assertThat(response.minimumPaymentAmount()).isEqualTo(50000L);
        assertThat(response.discountType()).isEqualTo("RATE");
        assertThat(response.discountRate()).isEqualTo(5);
        assertThat(response.maxDiscountAmount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회 클라이언트 오류 응답 반환시 예외가 발생한다")
    void getCartCoupon_thrown_client_error() {
        //given
        Long userId =1L;
        Long cartCouponId = 1L;
        String mockJsonResponse = """
                {
                    "code": "COUPON_EXPIRED",
                    "message": "쿠폰이 만료되었습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/coupons/calculate"
                }
                """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/cart-coupons/" + cartCouponId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getCartCoupon(userId, cartCouponId))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("쿠폰이 만료되었습니다")
                .extracting("errorCode")
                .isEqualTo("COUPON_EXPIRED");
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회 서버 오류 응답 반환시 예외가 발생한다")
    void getCartCoupon_thrown_server_error() {
        //given
        Long userId =1L;
        Long cartCouponId = 1L;
        String mockJsonResponse = """
                {
                    "code": "FAILED_INTERNAL_SYSTEM_PROCESSING",
                    "message": "처리중 알 수 없는 오류가 발생했습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/coupons/calculate"
                }
                """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/cart-coupons/" + cartCouponId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getCartCoupon(userId, cartCouponId))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("처리중 알 수 없는 오류가 발생했습니다")
                .extracting("errorCode")
                .isEqualTo("FAILED_INTERNAL_SYSTEM_PROCESSING");
    }

    private ItemCouponsResponse expected() {
        ItemCouponsResponse.ItemCoupon itemCoupon1 = ItemCouponsResponse.ItemCoupon.builder()
                .itemCouponId(1L)
                .status("AVAILABLE")
                .name("청바지 1000원 할인 쿠폰")
                .applyQuantityLimit(1)
                .discountType("FIXED")
                .discountAmount(1000L)
                .expiresAt(LocalDateTime.of(2026, 12, 25, 23, 59, 59))
                .build();

        ItemCouponsResponse.ItemCoupon itemCoupon2 = ItemCouponsResponse.ItemCoupon.builder()
                .itemCouponId(2L)
                .status("AVAILABLE")
                .name("반팔티 10% 할인 쿠폰")
                .applyQuantityLimit(1)
                .discountType("RATE")
                .discountRate(10)
                .maxDiscountAmount(10000L)
                .expiresAt(LocalDateTime.of(2026, 12, 25, 23, 59, 59))
                .build();

        return ItemCouponsResponse.builder()
                .userId(1L)
                .itemCoupons(List.of(itemCoupon1, itemCoupon2))
                .build();
    }
}

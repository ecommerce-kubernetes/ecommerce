package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
class ProductFeignClientTest {

    @Autowired
    private ProductFeignClient client;

    private String readJson(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("주문 상품 조회")
    class GetProductsForOrder {

        @Test
        @DisplayName("주문할 상품을 조회한다")
        void getProductsForOrder() throws IOException {
            //given
            String mockJsonResponse = readJson("product/validate-for-order-response.json");
            ProductClientRequest.Validate request = fixtureMonkey.giveMeOne(ProductClientRequest.Validate.class);

            ProductClientResponse.Product expected = createExpected();
            stubFor(post(urlEqualTo("/internal/variants/validate-for-order"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            ProductClientResponse.ProductList response = client.getProductsForOrder(request);
            //then
            assertThat(response.products()).hasSize(2);
            assertThat(response.products().get(0))
                    .usingRecursiveComparison()
                    .isEqualTo(expected);
        }
        
        @Test
        @DisplayName("주문 상품 조회시 클라이언트 에러 응답이 반환되면 예외가 발생한다")
        void getProductsForOrder_client_error() {
            //given
            ProductClientRequest.Validate request = fixtureMonkey.giveMeOne(ProductClientRequest.Validate.class);
            String mockJsonResponse = """
                    {
                        "code": "INSUFFICIENT_STOCK",
                        "message": "재고가 부족합니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/variants/validate-for-order"
                    }
                    """;
            stubFor(post(urlEqualTo("/internal/variants/validate-for-order"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.BAD_REQUEST.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.getProductsForOrder(request))
                    .isInstanceOf(ExternalClientException.class)
                    .hasMessage("재고가 부족합니다")
                    .extracting("errorCode")
                    .isEqualTo("INSUFFICIENT_STOCK");
        }

        @Test
        @DisplayName("주문 상품 조회시 클라이언트 서버 에러 응답이 반환되면 예외가 발생한다")
        void getProductsForOrder_server_error() {
            //given
            ProductClientRequest.Validate request = fixtureMonkey.giveMeOne(ProductClientRequest.Validate.class);
            String mockJsonResponse = """
                    {
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "알 수 없는 오류가 발생했습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/variants/validate-for-order"
                    }
                    """;
            stubFor(post(urlEqualTo("/internal/variants/validate-for-order"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.getProductsForOrder(request))
                    .isInstanceOf(ExternalServerException.class)
                    .hasMessage("알 수 없는 오류가 발생했습니다")
                    .extracting("errorCode")
                    .isEqualTo("INTERNAL_SERVER_ERROR");
        }

        private ProductClientResponse.Product createExpected() {
            ProductClientResponse.UnitPrice unitPrice = ProductClientResponse.UnitPrice.builder()
                    .originalPrice(10000L)
                    .discountRate(10)
                    .discountAmount(1000L)
                    .discountedPrice(9000L)
                    .build();
            ProductClientResponse.ProductOption xl = ProductClientResponse.ProductOption.builder()
                    .optionTypeName("사이즈")
                    .optionValueName("XL")
                    .build();
            ProductClientResponse.ProductOption blue = ProductClientResponse.ProductOption.builder()
                    .optionTypeName("색상")
                    .optionValueName("BLUE")
                    .build();
            return ProductClientResponse.Product.builder()
                    .productId(1L)
                    .productVariantId(1L)
                    .sku("PROD-XL-BLUE")
                    .productName("청바지")
                    .thumbnail("/product/product/jean_1.jpg")
                    .unitPrice(unitPrice)
                    .options(List.of(xl, blue))
                    .build();
        }
    }

    @Nested
    @DisplayName("장바구니 추가 상품 조회")
    class GetProductsForCart {

        @Test
        @DisplayName("장바구니에 추가할 상품을 조회한다")
        void getProductsForCart() throws IOException {
            //given
            String mockJsonResponse = readJson("product/validate-for-cart-response.json");
            ProductClientRequest.Validate request = fixtureMonkey.giveMeOne(ProductClientRequest.Validate.class);

            ProductClientResponse.Product expected = createExpected();
            stubFor(post(urlEqualTo("/internal/variants/validate-for-cart"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            ProductClientResponse.ProductList response = client.getProductsForCart(request);
            //then
            assertThat(response.products()).hasSize(2);
            assertThat(response.products().get(0))
                    .usingRecursiveComparison()
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("장바구니에 추가할 상품 조회시 클라이언트 에러 응답이 반환되면 예외가 발생한다")
        void getProductsForCart_client_error() {
            //given
            ProductClientRequest.Validate request = fixtureMonkey.giveMeOne(ProductClientRequest.Validate.class);
            String mockJsonResponse = """
                    {
                        "code": "INSUFFICIENT_STOCK",
                        "message": "재고가 부족합니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/variants/validate-for-cart"
                    }
                    """;
            stubFor(post(urlEqualTo("/internal/variants/validate-for-cart"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.BAD_REQUEST.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.getProductsForCart(request))
                    .isInstanceOf(ExternalClientException.class)
                    .hasMessage("재고가 부족합니다")
                    .extracting("errorCode")
                    .isEqualTo("INSUFFICIENT_STOCK");
        }

        @Test
        @DisplayName("장바구니에 추가할 상품 조회시 클라이언트 서버 에러 응답이 반환되면 예외가 발생한다")
        void getProductsForCart_server_error() {
            //given
            ProductClientRequest.Validate request = fixtureMonkey.giveMeOne(ProductClientRequest.Validate.class);
            String mockJsonResponse = """
                    {
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "알 수 없는 오류가 발생했습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/variants/validate-for-cart"
                    }
                    """;
            stubFor(post(urlEqualTo("/internal/variants/validate-for-cart"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.getProductsForCart(request))
                    .isInstanceOf(ExternalServerException.class)
                    .hasMessage("알 수 없는 오류가 발생했습니다")
                    .extracting("errorCode")
                    .isEqualTo("INTERNAL_SERVER_ERROR");
        }

        private ProductClientResponse.Product createExpected() {
            ProductClientResponse.UnitPrice unitPrice = ProductClientResponse.UnitPrice.builder()
                    .originalPrice(10000L)
                    .discountRate(10)
                    .discountAmount(1000L)
                    .discountedPrice(9000L)
                    .build();
            ProductClientResponse.ProductOption xl = ProductClientResponse.ProductOption.builder()
                    .optionTypeName("사이즈")
                    .optionValueName("XL")
                    .build();
            ProductClientResponse.ProductOption blue = ProductClientResponse.ProductOption.builder()
                    .optionTypeName("색상")
                    .optionValueName("BLUE")
                    .build();
            return ProductClientResponse.Product.builder()
                    .productId(1L)
                    .productVariantId(1L)
                    .sku("PROD-XL-BLUE")
                    .productName("청바지")
                    .thumbnail("/product/product/jean_1.jpg")
                    .unitPrice(unitPrice)
                    .options(List.of(xl, blue))
                    .build();
        }
    }
}
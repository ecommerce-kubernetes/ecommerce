package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
class ProductFeignClientTest {

    @Autowired
    private ProductFeignClient client;

    @Test
    @DisplayName("상품 서비스에서 상품 목록 정보를 조회한다")
    void getProductsByVariantIds() {
        //given
        //요청, 응답 객체 모킹
        List<Long> productVariantIds = List.of(1L, 2L);
        ProductClientRequest.ProductVariantIds request = ProductClientRequest.ProductVariantIds.of(productVariantIds);

        String mockJsonResponse = """
                [
                    {
                        "productId": 1,
                        "productVariantId": 1,
                        "status": "ON_SALE",
                        "sku": "PROD-XL-BLUE",
                        "productName": "청바지",
                        "thumbnail": "/product/product/jean_1.jpg",
                        "unitPrice": {
                            "originalPrice": 10000,
                            "discountRate": 10,
                            "discountAmount": 1000,
                            "discountedPrice": 9000
                        },
                        "stockQuantity": 100,
                        "options": [
                            {
                                "optionTypeName": "사이즈",
                                "optionValueName": "XL"
                            },
                            {
                                "optionTypeName": "색상",
                                "optionValueName": "BLUE"
                            }
                        ]
                    },
                    {
                        "productId": 1,
                        "productVariantId": 2,
                        "status": "ON_SALE",
                        "sku": "PROD-XL-RED",
                        "productName": "청바지",
                        "thumbnail": "/product/product/jean_1.jpg",
                        "unitPrice": {
                            "originalPrice": 10000,
                            "discountRate": 10,
                            "discountAmount": 1000,
                            "discountedPrice": 9000
                        },
                        "stockQuantity": 100,
                        "options": [
                            {
                                "optionTypeName": "사이즈",
                                "optionValueName": "XL"
                            },
                            {
                                "optionTypeName": "색상",
                                "optionValueName": "RED"
                            }
                        ]
                    }
                ]
                """;

        //외부 서비스 호출 모킹
        stubFor(post(urlEqualTo("/internal/variants/by-ids"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        List<ProductClientResponse.Product> responses = client.getProductsByVariantIds(request);
        //then
        ProductClientResponse.Product product = responses.get(0);
        assertThat(product).hasNoNullFieldsOrProperties();
        assertThat(product.unitPrice()).hasNoNullFieldsOrProperties();
        assertThat(product.sku()).isEqualTo("PROD-XL-BLUE");
    }

    @Test
    @DisplayName("상품 서비스에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
    void productService_thrown_client_error_response() {
        //given
        ProductClientRequest.ProductVariantIds request = ProductClientRequest.ProductVariantIds.of(List.of());
        //클라이언트 에러 응답 모킹
        String mockJsonResponse = """
                {
                    "code": "PROD_CLIENT_ERROR",
                    "message": "에러가 발생했습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/variants/by-ids"
                }
                """;

        stubFor(post(urlEqualTo("/internal/variants/by-ids"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getProductsByVariantIds(request))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("에러가 발생했습니다");
    }

    @Test
    @DisplayName("상품 서비스에서 서버 오류 응답 반환시 서버 예외를 던진다")
    void productService_thrown_server_error_response() {
        //given
        ProductClientRequest.ProductVariantIds request = ProductClientRequest.ProductVariantIds.of(List.of());

        String mockJsonResponse = """
                {
                    "code": "PROD_SERVER_ERROR",
                    "message": "에러가 발생했습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/variants/by-ids"
                }
                """;

        stubFor(post(urlEqualTo("/internal/variants/by-ids"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getProductsByVariantIds(request))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("에러가 발생했습니다");
    }
}
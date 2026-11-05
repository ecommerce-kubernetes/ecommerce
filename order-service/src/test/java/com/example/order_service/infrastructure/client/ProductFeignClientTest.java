package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.ProductBulkSearchRequest;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
class ProductFeignClientTest {

    @Autowired
    private ProductFeignClient client;
    @Autowired
    private ObjectMapper objectMapper;

    private String readJson(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("상품 정보를 조회한다")
    void getProducts() throws IOException {
        //given
        ProductBulkSearchRequest request = Instancio.create(ProductBulkSearchRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = readJson("product/product-response.json");
        stubFor(post(urlEqualTo("/internal/products/search"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        ProductResponse response = client.getProducts(request);
        //then
        assertThat(response.products()).hasSize(2);
        assertThat(response.products().getFirst())
                .usingRecursiveComparison()
                .isEqualTo(expected());
    }

    @Test
    @DisplayName("상품 조회시 클라이언트 에러 응답이 반환되면 예외가 발생한다")
    void getProducts_client_error() throws JsonProcessingException {
        //given
        ProductBulkSearchRequest request = Instancio.create(ProductBulkSearchRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "code": "INVALID_PRODUCT_REQUEST",
                        "message": "잘못된 상품 조회 요청입니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/products/search"
                    }
                    """;
        stubFor(post(urlEqualTo("/internal/products/search"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.CONFLICT.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getProducts(request))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("잘못된 상품 조회 요청입니다")
                .extracting("errorCode")
                .isEqualTo("INVALID_PRODUCT_REQUEST");
    }

    @Test
    @DisplayName("상품 조회시 서버 에러 응답이 반환되면 예외가 발생한다")
    void getProducts_server_error() throws JsonProcessingException {
        //given
        ProductBulkSearchRequest request = Instancio.create(ProductBulkSearchRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "알 수 없는 오류가 발생했습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/items"
                    }
                    """;
        stubFor(post(urlEqualTo("/internal/products/search"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getProducts(request))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("알 수 없는 오류가 발생했습니다")
                .extracting("errorCode")
                .isEqualTo("INTERNAL_SERVER_ERROR");
    }

    private ProductResponse.ProductDetail expected() {
        ProductResponse.UnitPrice unitPrice = ProductResponse.UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .build();

        ProductResponse.ProductOption xl = ProductResponse.ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();
        ProductResponse.ProductOption blue = ProductResponse.ProductOption.builder()
                .optionTypeName("색상")
                .optionValueName("BLUE")
                .build();
        return ProductResponse.ProductDetail.builder()
                .productId(1L)
                .productVariantId(1L)
                .status("ON_SALE")
                .stock(100)
                .sku("PROD-XL-BLUE")
                .productName("청바지")
                .thumbnail("/product/product/jean_1.jpg")
                .unitPrice(unitPrice)
                .options(List.of(xl, blue))
                .build();
    }
}
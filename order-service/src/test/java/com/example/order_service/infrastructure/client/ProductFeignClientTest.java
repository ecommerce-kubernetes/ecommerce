package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ClientErrorResponse;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
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
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 서비스에서 상품 목록 정보를 조회한다")
    void getProductsByVariantIds() throws JsonProcessingException {
        //given
        //요청, 응답 객체 모킹
        List<Long> productVariantIds = List.of(1L, 2L);
        ProductClientRequest.ProductVariantIds request = ProductClientRequest.ProductVariantIds.of(productVariantIds);
        List<ProductClientResponse.Product> productResponses = productVariantIds.stream()
                .map(id -> fixtureMonkey.giveMeBuilder(ProductClientResponse.Product.class)
                        .set("productVariantId", id)
                        .sample()).toList();

        //외부 서비스 호출 모킹
        stubFor(post(urlEqualTo("/internal/variants/by-ids"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(productResponses))));
        //when
        List<ProductClientResponse.Product> responses = client.getProductsByVariantIds(request);
        //then
        assertThat(responses)
                .extracting("productVariantId")
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("상품 서비스에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
    void productService_thrown_client_error_response() throws JsonProcessingException {
        //given
        ProductClientRequest.ProductVariantIds request = ProductClientRequest.ProductVariantIds.of(List.of());
        //클라이언트 에러 응답 모킹
        ClientErrorResponse errorResponse = ClientErrorResponse.builder()
                .code("PROD_CLIENT_ERROR")
                .message("잘못된 요청입니다")
                .timestamp(LocalDateTime.now())
                .path("/internal/variants/by-ids")
                .build();

        stubFor(post(urlEqualTo("/internal/variants/by-ids"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(errorResponse))));
        //when
        //then
        assertThatThrownBy(() -> client.getProductsByVariantIds(request))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("잘못된 요청입니다");
    }

    @Test
    @DisplayName("상품 서비스에서 클라이언트 오류 응답 반환시 서버 예외를 던진다")
    void productService_thrown_server_error_response() throws JsonProcessingException {
        //given
        ProductClientRequest.ProductVariantIds request = ProductClientRequest.ProductVariantIds.of(List.of());

        ClientErrorResponse errorResponse = ClientErrorResponse.builder()
                .code("PROD_SERVER_ERROR")
                .message("에러가 발생했습니다")
                .timestamp(LocalDateTime.now())
                .path("/internal/variants/by-ids")
                .build();

        stubFor(post(urlEqualTo("/internal/variants/by-ids"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(errorResponse))));
        //when
        //then
        assertThatThrownBy(() -> client.getProductsByVariantIds(request))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("에러가 발생했습니다");
    }
}
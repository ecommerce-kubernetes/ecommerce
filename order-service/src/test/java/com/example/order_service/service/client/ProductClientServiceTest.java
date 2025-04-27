package com.example.order_service.service.client;

import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "product.service.url=http://localhost:${wiremock.server.port}"
})
class ProductClientServiceTest {

    @Autowired
    ProductClientService productClientService;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void fetchProductTest() throws JsonProcessingException {
        ProductResponseDto productResponseDto =
                new ProductResponseDto(1L, "TestProduct", "description", 500, 20, 1L,"http://test.jpg");

        String content = mapper.writeValueAsString(productResponseDto);
        WireMock.stubFor(WireMock.get("/products/1")
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(content)));

        ProductResponseDto product = productClientService.fetchProduct(1L);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("TestProduct");
    }

    @Test
    void fetchProductTest_NotFoundProduct(){
        WireMock.stubFor(WireMock.get("/product/999")
                .willReturn(WireMock.aResponse().withStatus(404)));

        assertThatThrownBy(() -> productClientService.fetchProduct(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Not Found Product");
    }

    @Test
    void fetchProductTest_ServerError(){
        WireMock.stubFor(WireMock.get("/products/500")
                .willReturn(WireMock.aResponse().withStatus(500)));

        assertThatThrownBy(() -> productClientService.fetchProduct(500L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    ResponseStatusException ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getReason()).contains("Product Service Error");
                });
    }
}
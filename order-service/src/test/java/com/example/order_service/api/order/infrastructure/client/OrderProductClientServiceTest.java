package com.example.order_service.api.order.infrastructure.client;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.order.infrastructure.client.dto.OrderProductResponse;
import com.example.order_service.api.support.ExcludeInfraServiceTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

public class OrderProductClientServiceTest extends ExcludeInfraServiceTest {

    @Autowired
    private OrderProductClientService orderProductClientService;

    @MockitoBean
    private OrderProductClient orderProductClient;

    @Test
    @DisplayName("상품 서비스에서 상품들의 정보를 조회한다")
    void getProducts(){
        //given
        OrderProductResponse product1 = createOrderProductResponse(1L, 1L,
                "상품1", 3000L, 10, "http://thumbnail1.jpg",
                Map.of("사이즈", "XL"));

        OrderProductResponse product2 = createOrderProductResponse(2L, 2L,
                "상품2", 5000L, 10, "http://thumbnail2.jpg",
                Map.of("용량", "256GB"));

        given(orderProductClient.getProductVariantByIds(anyList()))
                .willReturn(List.of(product1, product2));
        //when
        List<OrderProductResponse> responses = orderProductClientService.getProducts(List.of(1L, 2L));
        //then
        assertThat(responses).hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        item1 -> {
                            assertThat(item1)
                                    .extracting("productId", "productVariantId", "productName", "thumbnailUrl")
                                    .contains(1L, 1L, "상품1", "http://thumbnail1.jpg");

                            assertThat(item1.getUnitPrice())
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .contains(3000L, 10, 300L, 2700L);

                            assertThat(item1.getItemOptions()).hasSize(1)
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactlyInAnyOrder(
                                            tuple("사이즈", "XL")
                                    );
                        },
                        item2 -> {
                            assertThat(item2)
                                    .extracting("productId", "productVariantId", "productName", "thumbnailUrl")
                                    .contains(2L, 2L, "상품2", "http://thumbnail2.jpg");
                            assertThat(item2.getUnitPrice())
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .contains(5000L, 10, 500L, 4500L);

                            assertThat(item2.getItemOptions()).hasSize(1)
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactlyInAnyOrder(
                                            tuple("용량", "256GB")
                                    );
                        }
                );
    }

    @Test
    @DisplayName("상품 서비스에서 상품을 조회할때 응답이 요청 id와 정확히 일치하지 않으면 예외를 던진다")
    void getProductsWhenMissingProduct(){
        //given
        OrderProductResponse product = createOrderProductResponse(1L, 1L,
                "상품1", 3000L, 10, "http://thumbnail1.jpg",
                Map.of("사이즈", "XL"));
        given(orderProductClient.getProductVariantByIds(anyList()))
                .willReturn(List.of(product));
        //when
        //then
        assertThatThrownBy(() -> orderProductClientService.getProducts(List.of(1L, 2L)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문상품중 존재하지 않은 상품이 있습니다");
    }

    @Test
    @DisplayName("서킷브레이커가 열리면 예외를 던진다")
    void getProductsWhenOpenCircuitBreaker(){
        //given
        willThrow(CallNotPermittedException.class).given(orderProductClient).getProductVariantByIds(anyList());
        //when
        //then
        assertThatThrownBy(() -> orderProductClientService.getProducts(List.of(1L, 2L)))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("상품 서비스가 응답하지 않습니다");
    }

    private OrderProductResponse createOrderProductResponse(Long productId, Long productVariantId, String productName,
                                                            Long originalPrice, int discountRate, String thumbnailUrl,
                                                            Map<String, String> optionMap) {

        return OrderProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .unitPrice(
                        OrderProductResponse.UnitPrice.builder()
                                .originalPrice(originalPrice)
                                .discountRate(discountRate)
                                .discountAmount(originalPrice / discountRate)
                                .discountedPrice(originalPrice - (originalPrice / discountRate))
                                .build())
                .thumbnailUrl(thumbnailUrl)
                .itemOptions(
                        optionMap.entrySet().stream()
                                .map(entry -> OrderProductResponse.ItemOption.builder()
                                        .optionTypeName(entry.getKey())
                                        .optionValueName(entry.getValue())
                                        .build()
                                )
                                .toList()
                )
                .build();
    }
}

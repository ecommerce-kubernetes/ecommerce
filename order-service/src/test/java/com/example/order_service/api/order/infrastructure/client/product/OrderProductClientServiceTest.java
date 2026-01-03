package com.example.order_service.api.order.infrastructure.client.product;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.support.ExcludeInfraTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

public class OrderProductClientServiceTest extends ExcludeInfraTest {

    @Autowired
    private OrderProductClientService orderProductClientService;

    @MockitoBean
    private OrderProductClient orderProductClient;

    @Test
    @DisplayName("상품 서비스에서 상품들의 정보를 조회한다")
    void getProducts(){
        //given
        OrderProductResponse product1 = createProductResponse(1L);
        OrderProductResponse product2 = createProductResponse(2L);

        given(orderProductClient.getProductVariantByIds(anyList()))
                .willReturn(List.of(product1, product2));
        //when
        List<OrderProductResponse> responses = orderProductClientService.getProducts(List.of(1L, 2L));
        //then
        assertThat(responses).hasSize(2)
                .extracting(OrderProductResponse::getProductId)
                .contains(1L, 2L);
    }

    @Test
    @DisplayName("서킷브레이커가 열렸을때 상품 목록 정보를 조회하면 예외를 던진다")
    void getProducts_When_OpenCircuitBreaker(){
        //given
        willThrow(CallNotPermittedException.class).given(orderProductClient).getProductVariantByIds(anyList());
        //when
        //then
        assertThatThrownBy(() -> orderProductClientService.getProducts(List.of(1L, 2L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.UNAVAILABLE);
    }

    @Test
    @DisplayName("상품 목록을 조회할때 알 수 없는 에러가 발생한 경우 서비스 장애 예외를 던진다")
    void getProducts_When_Unknown_Exception() {
        //given
        willThrow(new RuntimeException("상품 서비스 장애 발생"))
                .given(orderProductClient).getProductVariantByIds(anyList());
        //when
        //then
        assertThatThrownBy(() -> orderProductClientService.getProducts(List.of(1L, 2L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.SYSTEM_ERROR);
    }

    private OrderProductResponse createProductResponse(Long productId) {
        return OrderProductResponse.builder()
                .productId(productId)
                .productName("상품")
                .productVariantId(productId)
                .unitPrice(
                        OrderProductResponse.UnitPrice.builder()
                                .originalPrice(3000L)
                                .discountRate(10)
                                .discountAmount(300L)
                                .discountedPrice(2700L)
                                .build()
                )
                .stockQuantity(100)
                .thumbnailUrl("http://thumbnail.com")
                .itemOptions(List.of())
                .build();
    }
}

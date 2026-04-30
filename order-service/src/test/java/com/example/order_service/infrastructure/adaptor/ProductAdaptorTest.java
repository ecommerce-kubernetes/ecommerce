package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.api.common.exception.external.ExternalSystemException;
import com.example.order_service.api.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.support.annotation.ExcludeInfraTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.*;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExcludeInfraTest
public class ProductAdaptorTest {
    @Autowired
    private ProductAdaptor productAdaptor;
    @MockitoBean
    private ProductFeignClient client;

    @Test
    @DisplayName("상품 서비스에 상품을 조회한다")
    void getProductsByVariantIds() {
        //given
        List<Long> productVariantIds = List.of(1L, 2L);
        List<ProductClientResponse.Product> productResponses = productVariantIds.stream()
                .map(id -> sample(fixtureMonkey.giveMeBuilder(ProductClientResponse.Product.class)
                        .set("productVariantId", id))).toList();
        given(client.getProductsByVariantIds(any(ProductClientRequest.ProductVariantIds.class)))
                .willReturn(productResponses);
        //when
        List<ProductClientResponse.Product> response = productAdaptor.getProductsByVariantIds(productVariantIds);
        //then
        assertThat(response)
                .extracting("productVariantId")
                .containsExactlyInAnyOrder(
                        1L, 2L
                );
    }

    @Test
    @DisplayName("상품 서비스에 상품을 조회할때 서킷브레이커가 열렸다면 시스템 예외로 변환하여 예외를 던진다")
    void getProductsByVariantIds_circuitbreaker_open() {
        //given
        List<Long> productVariantIds = List.of(1L, 2L);
        CallNotPermittedException circuitException = CallNotPermittedException
                .createCallNotPermittedException(ofDefaults("test"));
        willThrow(circuitException).given(client)
                .getProductsByVariantIds(any());
        //when
        //then
        assertThatThrownBy(() -> productAdaptor.getProductsByVariantIds(productVariantIds))
                .isInstanceOf(ExternalSystemUnavailableException.class)
                .hasMessage("CircuitBreaker Open");
    }


    @Test
    @DisplayName("상품 서비스에서 상품을 조회할때 external System 예외가 던져지면 그대로 던진다")
    void getProductsByVariantIds_external_system_exception() {
        //given
        List<Long> productVariantIds = List.of(1L, 2L);
        willThrow(ExternalSystemException.class).given(client)
                .getProductsByVariantIds(any());
        //when
        //then
        assertThatThrownBy(() -> productAdaptor.getProductsByVariantIds(productVariantIds))
                .isInstanceOf(ExternalSystemException.class);
    }

    @Test
    @DisplayName("상품 서비스에서 상품을 조회할때 예외(error decoder 변환 x)가 던져지면 시스템 예외로 변환하여 예외를 던진다")
    void getProductsByVariantIds_other_exception() {
        //given
        List<Long> productVariantIds = List.of(1L, 2L);
        willThrow(RuntimeException.class).given(client)
                .getProductsByVariantIds(any());
        //when
        //then
        assertThatThrownBy(() -> productAdaptor.getProductsByVariantIds(productVariantIds))
                .isInstanceOf(ExternalSystemException.class);
    }
}

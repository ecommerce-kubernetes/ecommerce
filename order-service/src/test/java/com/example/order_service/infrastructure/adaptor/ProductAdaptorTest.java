package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@IsolatedTest
public class ProductAdaptorTest {
    @Autowired
    private ProductAdaptor productAdaptor;
    @MockitoBean
    private ProductFeignClient client;
    @MockitoBean
    private ExternalExceptionTranslator translator;

    @Nested
    @DisplayName("주문 상품 조회")
    class GetProductsForOrder {

        @Test
        @DisplayName("주문 상품 정보를 조회한다")
        void getProductsForOrder(){
            //given
            ProductCommand.Validate command = fixtureMonkey.giveMeOne(ProductCommand.Validate.class);
            List<ProductClientResponse.Product> mockResponses = fixtureMonkey.giveMe(ProductClientResponse.Product.class, 2);
            given(client.getProductsForOrder(any()))
                    .willReturn(mockResponses);
            //when
            List<ProductClientResponse.Product> response = productAdaptor.getProductsForOrder(command);
            //then
            assertThat(response).containsExactlyElementsOf(mockResponses);
        }

        @Test
        @DisplayName("주문 상품 조회시 예외가 발생하면 translator를 호출하여 예외를 변환한다")
        void getProductsForOrder_fallback_delegate_to_translator() throws Throwable {
            //given
            ProductCommand.Validate command = fixtureMonkey.giveMeOne(ProductCommand.Validate.class);
            RuntimeException feignException = new RuntimeException("feignClient 예외");
            ExternalSystemUnavailableException translatedException =
                    new ExternalSystemUnavailableException("CODE", "변환된 예외", feignException);
            willThrow(feignException).given(client).getProductsForOrder(any());
            given(translator.translate(anyString(), any(Throwable.class))).willReturn(translatedException);
            //when
            //then
            assertThatThrownBy(() -> productAdaptor.getProductsForOrder(command))
                    .isInstanceOf(ExternalSystemUnavailableException.class);
        }
    }

    @Test
    @DisplayName("상품 서비스에 상품을 조회한다")
    void getProductsByVariantIds() {
        //given
        List<Long> productVariantIds = List.of(1L, 2L);
        List<ProductClientResponse.ProductDeprecated> mockResponses =
                fixtureMonkey.giveMe(ProductClientResponse.ProductDeprecated.class, 2);
        given(client.getProductsByVariantIds(any(ProductClientRequest.ProductVariantIds.class)))
                .willReturn(mockResponses);
        //when
        List<ProductClientResponse.ProductDeprecated> response = productAdaptor.getProductsByVariantIds(productVariantIds);
        //then
        assertThat(response).containsExactlyElementsOf(mockResponses);
    }

    @Test
    @DisplayName("상품 조회시 예외가 발생하면 translator를 호출하여 반환된 예외를 변환한다")
    void getProductsByVariantIds_fallback_delegate_to_translator() throws Throwable {
        //given
        List<Long> productVariantIds = List.of(1L, 2L);
        // 발생한 예외
        RuntimeException feignException = new RuntimeException("feignClient 예외");
        // 변환된 예외
        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 예외", feignException);
        // feignClient가 예외를 던짐
        willThrow(feignException).given(client).getProductsByVariantIds(any(ProductClientRequest.ProductVariantIds.class));
        // translator가 예외를 변환
        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> productAdaptor.getProductsByVariantIds(productVariantIds))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }

}

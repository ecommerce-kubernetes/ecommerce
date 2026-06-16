package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
    @DisplayName("상품 조회")
    class GetProducts {

        @Test
        @DisplayName("상품을 조회한다")
        void getProducts(){
            //given
            ProductCommand.BulkSearch command = Instancio.create(ProductCommand.BulkSearch.class);
            ProductClientResponse.ProductList response = Instancio.create(ProductClientResponse.ProductList.class);
            given(client.getProducts(any())).willReturn(response);
            //when
            ProductClientResponse.ProductList products = productAdaptor.getProducts(command);
            //then
            assertThat(products).isNotNull();
        }

        @Test
        @DisplayName("상품 조회시 예외가 발생하면 translator를 호출하여 예외를 변환한다")
        void getProducts_fallback_delegate_to_translator() throws Throwable {
            //given
            ProductCommand.BulkSearch command = Instancio.create(ProductCommand.BulkSearch.class);
            RuntimeException feignException = new RuntimeException("feignClient 예외");
            ExternalSystemUnavailableException translatedException =
                    new ExternalSystemUnavailableException("CODE", "변환된 예외", feignException);
            willThrow(feignException).given(client).getProducts(any());
            given(translator.translate(anyString(), any(Throwable.class))).willReturn(translatedException);
            //when
            //then
            assertThatThrownBy(() -> productAdaptor.getProducts(command)).isInstanceOf(ExternalSystemUnavailableException.class);
        }
    }

    @Nested
    @DisplayName("주문 상품 조회")
    class GetProductsForOrder {

        @Test
        @DisplayName("주문 상품 정보를 조회한다")
        void getProductsForOrder(){
            //given
            ProductCommand.Validate command = Instancio.create(ProductCommand.Validate.class);
            ProductClientResponse.ProductList mockResponse = Instancio.create(ProductClientResponse.ProductList.class);
            given(client.getProductsForOrder(any()))
                    .willReturn(mockResponse);
            //when
            ProductClientResponse.ProductList response = productAdaptor.getProductsForOrder(command);
            //then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("주문 상품 조회시 예외가 발생하면 translator를 호출하여 예외를 변환한다")
        void getProductsForOrder_fallback_delegate_to_translator() throws Throwable {
            //given
            ProductCommand.Validate command = Instancio.create(ProductCommand.Validate.class);
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
}

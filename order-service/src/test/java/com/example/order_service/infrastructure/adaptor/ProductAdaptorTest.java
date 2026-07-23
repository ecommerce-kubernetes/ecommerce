package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.request.ProductBulkSearchRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

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

    @Test
    @DisplayName("상품을 조회한다")
    void getProducts(){
        //given
        List<Long> productVariantIds = List.of(1L, 2L);
        ProductResponse response = Instancio.create(ProductResponse.class);
        given(client.getProducts(any(ProductBulkSearchRequest.class))).willReturn(response);
        //when
        ProductResponse products = productAdaptor.getProducts(productVariantIds);
        //then
        assertThat(products).isNotNull();
    }

    @Test
    @DisplayName("상품 조회시 예외가 발생하면 translator를 호출하여 예외를 변환한다")
    void getProducts_fallback_delegate_to_translator() throws Throwable {
        //given
        List<Long> productVariantIds = List.of(1L, 2L);
        RuntimeException feignException = new RuntimeException("feignClient 예외");
        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 예외", feignException);
        willThrow(feignException).given(client).getProducts(any(ProductBulkSearchRequest.class));
        given(translator.translate(anyString(), any(Throwable.class))).willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> productAdaptor.getProducts(productVariantIds))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }
}

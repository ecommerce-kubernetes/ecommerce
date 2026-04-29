package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.api.support.ExcludeInfraTest;
import com.example.order_service.infrastructure.client.ProductFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ProductAdaptorTest extends ExcludeInfraTest {
    @InjectMocks
    private ProductAdaptor productAdaptor;

    @Mock
    private ProductFeignClient client;

    @Test
    @DisplayName("상품 서비스에 상품을 조회한다")
    void getProductsByVariantIds(){
        //given
        //when
        //then
    }

    @Test
    @DisplayName("상품 서비스에 상품을 조회할때 서킷브레이커가 열렸다면 시스템 예외로 변환하여 예외를 던진다")
    void getProductsByVariantIds_circuitbreaker_open(){
        //given
        //when
        //then
    }


    @Test
    @DisplayName("상품 서비스에서 상품을 조회할때 external System 예외가 던져지면 그대로 던진다")
    void getProductsByVariantIds_external_system_exception(){
        //given
        //when
        //then
    }

    @Test
    @DisplayName("상품 서비스에서 상품을 조회할때 예외(error decoder 변환 x)가 던져지면 시스템 예외로 변환하여 예외를 던진다")
    void getProductsByVariantIds_other_exception(){
        //given
        //when
        //then
    }
}

package com.example.order_service.api.common.client.product;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/*
   @Configuration 을 추가하면 스프링 자동 스캔 대상이 되므로 다른 모든 Client 에서도 해당 디코더를 사용하게 됨
   따라서 @Configuration 제외
   Client 가 같은 디코더를 사용하는 경우 다른 예외 메시지를 내보낼 수 없음
 */
public class ProductFeignConfig {
    @Bean
    public ErrorDecoder productErrorDecoder(){
        return new ProductErrorDecoder();
    }
}

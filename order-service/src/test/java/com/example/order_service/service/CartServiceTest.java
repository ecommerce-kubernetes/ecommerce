package com.example.order_service.service.cart;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.service.ExcludedExternalInfraTestSupport;
import com.example.order_service.service.client.ProductClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;

class CartServiceTest extends ExcludedExternalInfraTestSupport {

    @Autowired
    private ApplicationContext ctx;
    @MockitoBean
    private ProductClientService productClientService;
    @MockitoBean
    private MessageSourceUtil messageSourceUtil;
    @MockitoBean
    private FeignClientFactory feignClientFactory;
    @MockitoBean
    private FeignLoggerFactory factory;
    @Test
    void listAllBeans() {
        System.out.println("===== Loaded Beans =====");
        Arrays.stream(ctx.getBeanDefinitionNames())
                .sorted()
                .forEach(beanName -> System.out.println(beanName + " : " + ctx.getBean(beanName).getClass().getName()));
        System.out.println("========================");
    }
}
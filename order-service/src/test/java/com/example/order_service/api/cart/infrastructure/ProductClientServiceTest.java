package com.example.order_service.api.cart.infrastructure;

import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class ProductClientServiceTest {
    @Autowired
    private CartProductClientService cartProductClientService;

    @Test
    @DisplayName("")
    void test(){
        //given
        //when
        log.info("test");
        //then
    }
}

package com.example.order_service.service;

import com.example.common.DeductedProduct;
import com.example.common.ItemOption;
import com.example.common.PriceInfo;
import com.example.common.ProductStockDeductedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SagaManagerTest {

    @Autowired
    SagaManager sagaManager;

    @Test
    void test(){
        ProductStockDeductedEvent event = new ProductStockDeductedEvent(1L, List.of(new DeductedProduct(1L, 1L, "product", "http://test.jpg",
                new PriceInfo(10, 10, 10, 10), 10, List.of(new ItemOption("색상", "RED")))));

        sagaManager.processSagaSuccess(event);
    }

}
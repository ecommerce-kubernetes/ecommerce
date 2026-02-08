package com.example.product_service.api.product.saga.listener;

import com.example.common.product.Item;
import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.product_service.api.product.saga.service.SagaProcessor;
import com.example.product_service.support.IncludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class SagaEventListerTest extends IncludeInfraTest {

    private static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @MockitoBean
    private SagaProcessor sagaProcessor;

    @Test
    @DisplayName("주문 사가 이벤트를 수신하면 sagaProcessor를 호출한다")
    void handleOrderEvent(){
        //given
        Long sagaId = 1L;
        Item item = Item.of(1L, 2);
        List<Item> items = List.of(item);
        ProductSagaCommand command = ProductSagaCommand.of(ProductCommandType.DEDUCT_STOCK, sagaId, ORDER_NO, 1L, items, LocalDateTime.now());
        //when
        kafkaTemplate.send(ORDER_SAGA_COMMAND_TOPIC, String.valueOf(command.getSagaId()), command);
        //then
        verify(sagaProcessor, timeout(1000).times(1)).productSagaProcess(command);
    }
}

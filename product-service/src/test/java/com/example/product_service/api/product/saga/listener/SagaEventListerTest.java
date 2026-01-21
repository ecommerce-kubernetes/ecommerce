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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class SagaEventListerTest extends IncludeInfraTest {

    private static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @MockitoBean
    private SagaProcessor sagaProcessor;

    @Test
    @DisplayName("")
    void handleOrderEvent(){
        //given
        Long sagaId = 1L;
        Item item = Item.of(1L, 2);
        List<Item> items = List.of(item);
        ProductSagaCommand command = ProductSagaCommand.of(ProductCommandType.DEDUCT_STOCK, sagaId, ORDER_NO, 1L, items, LocalDateTime.now());
        //when
        kafkaTemplate.send(ORDER_REQUEST_TOPIC_NAME, String.valueOf(command.getSagaId()), command);
        //then
        verify(sagaProcessor, timeout(10000).times(1)).productSagaProcess(argThat(actual -> {
            assertThat(actual)
                    .usingRecursiveComparison() // 1. equals가 없어도 필드값끼리 비교합니다.
                    .isEqualTo(command); // 3. 비교 실행
            return true; // 에러가 안 나면 통과
        }));
    }
}

package com.example.product_service.api.product.saga.service;

import com.example.common.product.Item;
import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.product.saga.producer.SagaEventProducer;
import com.example.product_service.api.product.service.VariantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SagaProcessorTest {

    @InjectMocks
    private SagaProcessor sagaProcessor;

    @Mock
    private ProductSagaCommandExecutor executor;
    @Mock
    private SagaEventProducer sagaEventProducer;

    @Test
    @DisplayName("재고 감소 메시지 수신시 재고 감소 후 재고 감소 성공 메시지를 보낸다")
    void productSagaProcess_deduct_stock_success(){
        //given
        List<Item> items = List.of(Item.builder().productVariantId(1L).quantity(3).build());
        ProductSagaCommand command = ProductSagaCommand.of(ProductCommandType.DEDUCT_STOCK, 1L, "ORDER_NO", 1L, items, LocalDateTime.now());
        given(executor.processSagaCommand(command)).willReturn(false);
        //when
        sagaProcessor.productSagaProcess(command);
        //then
        verify(sagaEventProducer, times(1)).sendSagaSuccess(anyLong(), anyString());
    }

    @Test
    @DisplayName("재고 감소 메시지 수신후 재고 감소 진행중 재고감소에 실패한 경우 재고감소 실패 메시지를 보낸다")
    void productSagaProcess_deduct_stock_failure(){
        //given
        List<Item> items = List.of(Item.builder().productVariantId(1L).quantity(3).build());
        ProductSagaCommand command = ProductSagaCommand.of(ProductCommandType.DEDUCT_STOCK, 1L, "ORDER_NO", 1L, items, LocalDateTime.now());
        willThrow(new BusinessException(ProductErrorCode.VARIANT_OUT_OF_STOCK))
                .given(executor).processSagaCommand(any());
        //when
        sagaProcessor.productSagaProcess(command);
        //then
        verify(sagaEventProducer, times(1)).sendSagaFailure(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("재고 감소 메시지 수신후 재고 감소 진행중 예외가 발생한 경우 재고 감소 실패 메시지를 보낸다")
    void productSagaProcess_deduct_stock_exception(){
        //given
        List<Item> items = List.of(Item.builder().productVariantId(1L).quantity(3).build());
        ProductSagaCommand command = ProductSagaCommand.of(ProductCommandType.DEDUCT_STOCK, 1L, "ORDER_NO", 1L, items, LocalDateTime.now());
        willThrow(new RuntimeException()).given(executor).processSagaCommand(command);
        //when
        sagaProcessor.productSagaProcess(command);
        //then
        verify(sagaEventProducer, times(1)).sendSagaFailure(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("이미 처리된 SAGA를 중복 수신시 성공 이벤트를 재발행한다")
    void productSagaProcess_duplicate_event(){
        //given
        List<Item> items = List.of(Item.builder().productVariantId(1L).quantity(3).build());
        ProductSagaCommand command = ProductSagaCommand.of(ProductCommandType.DEDUCT_STOCK, 1L, "ORDER_NO", 1L, items, LocalDateTime.now());
        given(executor.processSagaCommand(command)).willReturn(true);
        //when
        sagaProcessor.productSagaProcess(command);
        //then
        verify(sagaEventProducer, times(1)).sendSagaSuccess(anyLong(), anyString());
    }

    @Test
    @DisplayName("재고 복구 메시지 수신후 재고 복구를 진행한다")
    void productSagaProcess_restore_stock(){
        //given
        List<Item> items = List.of(Item.builder().productVariantId(1L).quantity(3).build());
        ProductSagaCommand command = ProductSagaCommand.of(ProductCommandType.RESTORE_STOCK, 1L, "ORDER_NO", 1L, items, LocalDateTime.now());
        given(executor.processSagaCommand(command)).willReturn(false);
        //when
        sagaProcessor.productSagaProcess(command);
        //then
        verify(executor, times(1)).processSagaCommand(command);
    }
}

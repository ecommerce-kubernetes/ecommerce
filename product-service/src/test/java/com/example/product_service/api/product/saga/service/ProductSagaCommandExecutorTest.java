package com.example.product_service.api.product.saga.service;

import com.example.common.product.Item;
import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.product_service.api.product.saga.domain.model.ProcessedSagaEvent;
import com.example.product_service.api.product.saga.domain.repository.ProcessedSagaEventRepository;
import com.example.product_service.api.product.service.VariantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ProductSagaCommandExecutorTest {

    @InjectMocks
    private ProductSagaCommandExecutor executor;

    @Mock
    private VariantService variantService;
    @Mock
    private ProcessedSagaEventRepository repository;

    @Nested
    @DisplayName("상품 재고 사가")
    class ProcessSagaCommand {

        @Test
        @DisplayName("상품 재고 차감과 처리 내역을 저장한다")
        void processSagaCommand_deduct_stock() {
            //given
            ProductSagaCommand command = ProductSagaCommand
                    .of(ProductCommandType.DEDUCT_STOCK, 1L, "ORDER_NO", 1L, List.of(Item.of(1L, 3)), LocalDateTime.now());
            given(repository.existsBySagaIdAndCommandType(1L, command.getType().name()))
                    .willReturn(false);
            //when
            boolean isProcessed = executor.processSagaCommand(command);
            //then
            assertThat(isProcessed).isFalse();
            verify(variantService, times(1)).deductVariantsStock(anyList());
            verify(repository, times(1)).save(any(ProcessedSagaEvent.class));
        }

        @Test
        @DisplayName("중복 요청시 재고 차감과 처리 내역 저장은 건너뛴다")
        void processSagaCommand_deduct_stock_duplicate_skip() {
            //given
            ProductSagaCommand command = ProductSagaCommand
                    .of(ProductCommandType.DEDUCT_STOCK, 1L, "ORDER_NO", 1L, List.of(Item.of(1L, 3)), LocalDateTime.now());
            given(repository.existsBySagaIdAndCommandType(command.getSagaId(), command.getType().name()))
                    .willReturn(true);
            //when
            boolean isProcessed = executor.processSagaCommand(command);
            //then
            assertThat(isProcessed).isTrue();
            verify(variantService, never()).deductVariantsStock(anyList());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("상품 재고 복구와 처리 내역을 저장한다")
        void processSagaCommand_restore_stock() {
            //given
            ProductSagaCommand command = ProductSagaCommand
                    .of(ProductCommandType.RESTORE_STOCK, 1L, "ORDER_NO", 1L, List.of(Item.of(1L, 3)), LocalDateTime.now());
            given(repository.existsBySagaIdAndCommandType(1L, command.getType().name()))
                    .willReturn(false);
            //when
            boolean isProcessed = executor.processSagaCommand(command);
            //then
            assertThat(isProcessed).isFalse();
            verify(variantService, times(1)).restoreVariantsStock(anyList());
            verify(repository, times(1)).save(any(ProcessedSagaEvent.class));
        }

        @Test
        @DisplayName("중복 요청시 재고 복구와 처리 내역 저장은 건너뛴다")
        void processSagaCommand_duplicate_skip() {
            //given
            ProductSagaCommand command = ProductSagaCommand
                    .of(ProductCommandType.RESTORE_STOCK, 1L, "ORDER_NO", 1L, List.of(Item.of(1L, 3)), LocalDateTime.now());
            given(repository.existsBySagaIdAndCommandType(command.getSagaId(), command.getType().name()))
                    .willReturn(true);
            //when
            boolean isProcessed = executor.processSagaCommand(command);
            //then
            assertThat(isProcessed).isTrue();
            verify(variantService, never()).restoreVariantsStock(anyList());
            verify(repository, never()).save(any());
        }
    }
}

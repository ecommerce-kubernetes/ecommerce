package com.example.order_service.ordersheet.application;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderSheetErrorCode;
import com.example.order_service.api.support.BaseTestSupport;
import com.example.order_service.api.support.TestUtil;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.ordersheet.application.dto.result.ProductStatus;
import com.example.order_service.ordersheet.domain.OrderSheet;
import com.example.order_service.ordersheet.domain.OrderSheetItem;
import com.example.order_service.ordersheet.domain.OrderSheetRepository;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemProductSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

public class OrderSheetServiceTest extends BaseTestSupport {
    @InjectMocks
    private OrderSheetService orderSheetService;
    @Mock
    private OrderSheetProductService orderSheetProductService;
    @Mock
    private OrderSheetRepository repository;

    @Nested
    @DisplayName("주문서 저장")
    class Create {

        @Test
        @DisplayName("주문 상품 정보를 조회하여 주문서를 저장한다")
        void createOrderSheet() {
            //given
            Long targetVariantId = 1L;
            int quantity = 1;
            long discountedPrice = 9000L;
            OrderSheetCommand.OrderItem orderItem = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(targetVariantId)
                    .quantity(quantity)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(orderItem))
                    .build();
            OrderSheetProductResult.Info productInfo = TestUtil.sample(fixtureMonkey.giveMeBuilder(OrderSheetProductResult.Info.class)
                    .set("productId", 1L)
                    .set("status", ProductStatus.ON_SALE)
                    .set("stock", 100)
                    .set("productVariantId", targetVariantId)
                    .set("discountedPrice", discountedPrice));

            given(orderSheetProductService.getProducts(anyList()))
                    .willReturn(List.of(productInfo));
            given(repository.save(any(OrderSheet.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            //when
            OrderSheetResult.Default result = orderSheetService.createOrderSheet(command);
            //then
            assertThat(result.sheetId()).isNotNull();
            assertThat(result.expiresAt()).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.summary().totalBasePaymentAmount()).isEqualTo(9000L);
        }

        @Test
        @DisplayName("주문 상품이 판매중이 아니면 주문할 수 없다")
        void createOrderSheet_not_on_sale() {
            //given
            Long targetVariantId = 1L;
            int quantity = 10;
            //주문 command
            OrderSheetCommand.OrderItem orderItem = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(targetVariantId)
                    .quantity(quantity)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(orderItem))
                    .build();
            // 판매 중지된 상품
            OrderSheetProductResult.Info productInfo = TestUtil.sample(fixtureMonkey.giveMeBuilder(OrderSheetProductResult.Info.class)
                    .set("productVariantId", targetVariantId)
                    .set("status", ProductStatus.STOP_SALE)
                    .set("stock", 100));
            given(orderSheetProductService.getProducts(anyList()))
                    .willReturn(List.of(productInfo));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.createOrderSheet(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_NOT_ON_SALE);
        }

        @Test
        @DisplayName("주문 상품 재고가 주문 수량보다 적으면 주문할 수 없다")
        void createOrderSheet_insufficient_stock() {
            //given
            Long targetVariantId = 1L;
            int quantity = 100;
            //주문 command
            OrderSheetCommand.OrderItem orderItem = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(targetVariantId)
                    .quantity(quantity)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(orderItem))
                    .build();

            //상품 조회 결과
            OrderSheetProductResult.Info productInfo = TestUtil.sample(fixtureMonkey.giveMeBuilder(OrderSheetProductResult.Info.class)
                    .set("productVariantId", targetVariantId)
                    .set("stock", 10));

            given(orderSheetProductService.getProducts(anyList()))
                    .willReturn(List.of(productInfo));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.createOrderSheet(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_NOT_ON_SALE);
        }
    }
}

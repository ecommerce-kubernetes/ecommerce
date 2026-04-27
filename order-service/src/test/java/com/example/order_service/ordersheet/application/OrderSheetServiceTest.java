package com.example.order_service.ordersheet.application;

import com.example.order_service.api.support.BaseTestSupport;
import com.example.order_service.api.support.TestUtil;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
        void createOrderSheet(){
            //given
            Long targetVariantId = 1L;
            int quantity = 1;
            long discountedPrice = 9000L;
            OrderSheetCommand.Create command = TestUtil.sample(fixtureMonkey.giveMeBuilder(OrderSheetCommand.Create.class)
                    .set("items[0].productVariantId", targetVariantId)
                    .set("items[0].quantity", quantity));

            OrderSheetProductResult.Info productInfo = TestUtil.sample(fixtureMonkey.giveMeBuilder(OrderSheetProductResult.Info.class)
                    .set("productId", 1L)
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
    }
}

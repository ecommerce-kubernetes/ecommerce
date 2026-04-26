package com.example.order_service.ordersheet.application;

import com.example.order_service.api.support.BaseTestSupport;
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
            OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .build();
            OrderSheetProductResult.Option option = OrderSheetProductResult.Option.builder()
                    .optionTypeName("사이즈")
                    .optionValueName("XL")
                    .build();
            OrderSheetProductResult.Info productInfo = OrderSheetProductResult.Info.builder()
                    .productId(1L)
                    .productVariantId(1L)
                    .sku("PROD1_XL")
                    .productName("상품1")
                    .originalPrice(10000L)
                    .discountRate(10)
                    .discountAmount(1000L)
                    .discountedPrice(9000L)
                    .thumbnail("/product/product/prod1.png")
                    .options(List.of(option))
                    .build();
            OrderSheetItemPriceSnapshot itemPrice = OrderSheetItemPriceSnapshot.of(productInfo.originalPrice(),
                    productInfo.discountRate(), productInfo.discountAmount(), productInfo.discountedPrice());
            OrderSheetItemProductSnapshot itemInfo = OrderSheetItemProductSnapshot.of(productInfo.productId(),
                    productInfo.productVariantId(), productInfo.sku(), productInfo.productName(), productInfo.thumbnail());
            List<OrderSheetItemOptionSnapshot> options = productInfo.options().stream().map(itemOption ->
                    OrderSheetItemOptionSnapshot.of(itemOption.optionTypeName(), itemOption.optionValueName())).toList();
            OrderSheetItem orderSheetItem = OrderSheetItem.create(itemInfo, itemPrice, 1, options);
            OrderSheet orderSheet = OrderSheet.create("sheetId", List.of(orderSheetItem), LocalDateTime.now());
            given(orderSheetProductService.getProducts(anyList()))
                    .willReturn(List.of(productInfo));
            given(repository.save(any(OrderSheet.class)))
                    .willReturn(Optional.of(orderSheet));
            //when
            OrderSheetResult.Default result = orderSheetService.createOrderSheet(command);
            //then
            assertThat(result.sheetId()).isNotNull();
            assertThat(result.expiresAt()).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.items())
                    .extracting("productId", "productVariantId", "productName", "thumbnail", "quantity", "lineTotal")
                    .containsExactlyInAnyOrder(
                            tuple(1L, 1L, "상품1", "/product/product/prod1.png", 1, 9000L)
                    );
        }
    }
}

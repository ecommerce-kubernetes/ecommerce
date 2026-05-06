package com.example.order_service.ordersheet.domain.repository;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemProductSnapshot;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.WithRedis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRedis @MockKafka
class OrderSheetRepositoryTest {

    @Autowired
    private OrderSheetRepository repository;

    @Test
    @DisplayName("주문서 데이터를 저장한다")
    void save() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        //when
        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(30));
        //then
        assertThat(orderSheet)
                .usingRecursiveComparison()
                .isEqualTo(savedOrderSheet);
    }

    private OrderSheet createOrderSheet() {
        return OrderSheet.create(
                "test",
                List.of(createOrderSheetItem()),
                LocalDateTime.now()
        );
    }

    private OrderSheetItem createOrderSheetItem() {
        return OrderSheetItem.create(
                createProductSnapshot(),
                createPriceSnapshot(),
                1,
                List.of(createProductOptionSnapshot())
        );
    }

    private OrderSheetItemProductSnapshot createProductSnapshot() {
        return OrderSheetItemProductSnapshot.of(
                1L, 1L, "PROD_XL", "테스트 상품", "/product/product/thumbnail"
        );
    }

    private OrderSheetItemPriceSnapshot createPriceSnapshot(){
        return OrderSheetItemPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
    }

    private OrderSheetItemOptionSnapshot createProductOptionSnapshot() {
        return OrderSheetItemOptionSnapshot.of("사이즈", "XL");
    }
}
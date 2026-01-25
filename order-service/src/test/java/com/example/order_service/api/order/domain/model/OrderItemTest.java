package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.order.domain.model.vo.OrderItemPrice;
import com.example.order_service.api.order.domain.model.vo.OrderedProduct;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
import com.example.order_service.api.support.fixture.OrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.example.order_service.api.support.fixture.OrderFixture.anOrderItemCreationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderItemTest {

    @Nested
    @DisplayName("주문 상품 생성")
    class Create {
        @Test
        @DisplayName("주문 상품을 생성한다")
        void createOrderItem(){
            //given
            OrderItemCreationContext context = anOrderItemCreationContext().build();
            //when
            OrderItem orderItem = OrderItem.create(context);
            //then
            assertThat(orderItem)
                    .extracting(OrderItem::getOrderedProduct, OrderItem::getOrderItemPrice, OrderItem::getLineTotal, OrderItem::getQuantity)
                    .containsExactly(
                            OrderedProduct.of(1L, 1L, "TEST", "상품", "http://thumbnail.jpg"),
                            OrderItemPrice.of(10000L, 10, 1000L, 9000L),
                            9000L,
                            1
                    );

            assertThat(orderItem.getOrderItemOptions())
                    .extracting(OrderItemOption::getOptionTypeName, OrderItemOption::getOptionValueName)
                    .containsExactlyInAnyOrder(
                            tuple("사이즈", "XL"));
        }
    }
}

package com.example.order_service.api.support.fixture.order;

import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderItem;
import com.example.order_service.api.order.domain.model.OrderStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepositoryTestFixture {
    private static final Long DEFAULT_USER_ID = 1L;
    private static final String DEFAULT_ORDER_NAME = "테스트 주문";
    private static final String DEFAULT_ADDRESS = "서울시 테헤란로 123";
    private static final String ORDER_NO = "ORD-20260101-AB12FVC";

    public static TestOrderBuilder testOrderBuilder() {
        return new  TestOrderBuilder();
    }

    public static class TestOrderBuilder {
        private Long userId = DEFAULT_USER_ID;
        private String orderNo = ORDER_NO;
        private OrderStatus status = OrderStatus.COMPLETED;
        private String orderName = DEFAULT_ORDER_NAME;
        private String deliveryAddress = DEFAULT_ADDRESS;
        private OrderFailureCode failureCode = null;
        private LocalDateTime createdAt = LocalDateTime.now();

        private final List<SimpleItemInfo> itemsToAdd = new ArrayList<>();

        public TestOrderBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public TestOrderBuilder addItem(String productName) {
            this.itemsToAdd.add(new SimpleItemInfo(productName, 10000L, 1));
            return this;
        }

        public Order build() {
            Order order = newInstance(Order.class);
            ReflectionTestUtils.setField(order, "orderNo", orderNo);
            ReflectionTestUtils.setField(order, "userId", userId);
            ReflectionTestUtils.setField(order, "status", status);
            ReflectionTestUtils.setField(order, "orderName", orderName);
            ReflectionTestUtils.setField(order, "deliveryAddress", deliveryAddress);
            ReflectionTestUtils.setField(order, "failureCode", failureCode);

            ReflectionTestUtils.setField(order, "createdAt", createdAt);

            for (SimpleItemInfo info : itemsToAdd) {
                OrderItem item = newInstance(OrderItem.class);
                ReflectionTestUtils.setField(item, "productId", 1L);
                ReflectionTestUtils.setField(item, "productVariantId", 1L);
                ReflectionTestUtils.setField(item, "productName", info.name);
                ReflectionTestUtils.setField(item, "lineTotal", info.price);
                ReflectionTestUtils.setField(item, "quantity", info.quantity);
                ReflectionTestUtils.setField(item, "thumbnail", "http://dummy.jpg");
                ReflectionTestUtils.invokeMethod(order, "addOrderItem", item);
            }

            return order;
        }
    }

    private static class SimpleItemInfo {
        String name;
        Long price;
        int quantity;

        public SimpleItemInfo(String name, Long price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("픽스처 객체 생성 실패: " + clazz.getSimpleName(), e);
        }
    }
}

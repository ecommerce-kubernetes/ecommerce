package com.example.order_service.order.application.orchestrator;


import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.application.service.saga.OrderSagaService;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderItem;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.order.domain.saga.OrderSagaInstance;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.infrastructure.messaging.dto.SagaReplyMessage;
import com.example.order_service.order.infrastructure.messaging.dto.SagaResult;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@MockRedis
@MockKafka
class OrderSagaManagerTest {

    @Autowired
    private OrderSagaManager orderSagaManager;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderSagaInstanceRepository sagaRepository;
    @MockitoSpyBean
    private OrderSagaService orderSagaService;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        sagaRepository.deleteAll();
    }

    @Nested
    @DisplayName("SAGA 시작")
    class StartSaga {

        @Test
        @DisplayName("주문을 결제 상태로 변경하고 재고 차감 Saga 인스턴스를 생성한다")
        void startSaga() {
            //given
            String orderNo = "orderNo";
            Order order = createOrder(orderNo);
            orderRepository.save(order);
            //when
            orderSagaManager.startSaga(orderNo);
            //then
            Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
            OrderSagaInstance findSaga = sagaRepository.findByOrderNo(orderNo).orElseThrow();
            assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(findSaga.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_DEDUCT_PENDING);
            assertThat(findSaga.getStatus()).isEqualTo(SagaStatus.STARTED);
        }

        @Test
        @DisplayName("Saga 인스턴스 생성 오류 발생시 주문 상태는 롤백되어야 한다")
        void startSaga_rollback_order_status_saga_creation_fail() {
            //given
            String orderNo = "orderNo";
            Order order = createOrder(orderNo);
            orderRepository.save(order);
            doThrow(new RuntimeException())
                    .when(orderSagaService).createSaga(any());
            //when
            assertThatThrownBy(() -> orderSagaManager.startSaga(orderNo))
                    .isInstanceOf(RuntimeException.class);
            //then
            Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
            assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(sagaRepository.findByOrderNo(orderNo)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Saga 메시지 수신")
    class HandleReply {

        @Test
        @DisplayName("재고 감소 성공 메시지를 수신하면 saga history를 저장하고 주문이 쿠폰을 사용했다면 쿠폰 단계로 넘어간다")
        void handleReply_success_inventory_deducted_when_using_coupon(){
            //given
            String orderNo = "orderNo";
            Long userId = 1L;
            SagaReplyMessage message = SagaReplyMessage.builder().result(SagaResult.SUCCESS).orderNo(orderNo)
                    .step(SagaStep.INVENTORY_DEDUCT_PENDING).code("INVENTORY_DEDUCT_SUCCESS")
                    .build();
            SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
            SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
            SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
            SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
            OrderSagaInstance instance = OrderSagaInstance.create(orderNo, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
            sagaRepository.save(instance);
            //when
            orderSagaManager.handleReply(message);
            //then
            OrderSagaInstance findInstance = sagaRepository.findByOrderNo(orderNo).orElseThrow();
            assertThat(findInstance.getHistories()).hasSize(1);
            assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.STARTED);
            assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.COUPON_USE_PENDING);
        }

        @Test
        @DisplayName("재고 감소 성공 메시지를 수신하면 saga history를 저장하고 주문이 쿠폰을 사용하지 않았다면 포인트 차감 단계로 넘어간다")
        @Transactional
        void handleReply_success_inventory_deducted_skip_coupon(){
            //given
            String orderNo = "orderNo";
            Long userId = 1L;
            SagaReplyMessage message = SagaReplyMessage.builder().result(SagaResult.SUCCESS).orderNo(orderNo)
                    .step(SagaStep.INVENTORY_DEDUCT_PENDING).code("INVENTORY_DEDUCT_SUCCESS")
                    .build();
            SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(null, List.of());
            SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
            SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
            SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
            OrderSagaInstance instance = OrderSagaInstance.create(orderNo, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
            sagaRepository.save(instance);
            //when
            orderSagaManager.handleReply(message);
            //then
            assertThat(instance.getHistories()).hasSize(1);
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.STARTED);
            assertThat(instance.getCurrentStep()).isEqualTo(SagaStep.POINTS_DEDUCT_PENDING);
        }

        @Test
        @DisplayName("재고 감소 성공 메시지를 수신하면 saga history를 저장하고 쿠폰과 포인트를 사용하지 않았다면 Saga 와 주문을 완료 처리 한다")
        @Transactional
        void handleReply_success_inventory_deducted_skip_coupon_and_point(){
            //given
            String orderNo = "orderNo";
            Long userId = 1L;
            Order order = createOrder(orderNo);
            order.paid();
            SagaReplyMessage message = SagaReplyMessage.builder().result(SagaResult.SUCCESS).orderNo(orderNo)
                    .step(SagaStep.INVENTORY_DEDUCT_PENDING).code("INVENTORY_DEDUCT_SUCCESS")
                    .build();
            SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(null, List.of());
            SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
            SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.ZERO);
            SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
            OrderSagaInstance instance = OrderSagaInstance.create(orderNo, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
            sagaRepository.save(instance);
            orderRepository.save(order);
            //when
            orderSagaManager.handleReply(message);
            //then
            assertThat(instance.getHistories()).hasSize(1);
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.COMPLETE);
            assertThat(instance.getCurrentStep()).isEqualTo(SagaStep.END);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }
    }

    private Order createOrder(String orderNo) {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(1L, "장바구니 1000원 할인", Money.wons(1000L));
        List<OrderItem> orderItems = createOrderItems();
        return Order.init(orderNo, orderer, shippingAddress, cartCoupon, orderItems, Money.wons(10000L),
                Money.wons(1000L), Money.wons(2000L), Money.wons(1000L), Money.wons(6000L));
    }

    private List<OrderItem> createOrderItems() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot productPriceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(2L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        OrderItem orderItem = OrderItem.create(productSnapshot, productPriceSnapshot, itemCoupon, 1, List.of());
        return List.of(orderItem);
    }
}
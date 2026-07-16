package com.example.order_service.order.application.orchestrator;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.messaging.SagaEventListener;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

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
    @MockitoBean
    private SagaEventListener sagaEventListener;

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
            Long paymentId = 1L;
            Order order = createOrder(orderNo);
            orderRepository.save(order);
            //when
            orderSagaManager.startSaga(orderNo, paymentId);
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
            Long paymentId = 1L;
            Order order = createOrder(orderNo);
            orderRepository.save(order);
            doThrow(new RuntimeException())
                    .when(orderSagaService).createSaga(any());
            //when
            assertThatThrownBy(() -> orderSagaManager.startSaga(orderNo, paymentId))
                    .isInstanceOf(RuntimeException.class);
            //then
            Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
            assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(sagaRepository.findByOrderNo(orderNo)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Saga 진행")
    class HandleReply {

        @Test
        @DisplayName("수신한 메시지의 step 이 현재 saga step과 일치하지 않으면 saga를 진행하지 않고 무시한다")
        void handleReply_message_step_not_match_saga_step(){
            //given
            String orderNo = "orderNo";
            Long paymentId = 1L;
            Long userId = 1L;
            SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
            SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
            SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
            SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
            OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.COUPON_USE_PENDING, payload);
            sagaRepository.save(instance);
            SagaReplyMessage message = SagaReplyMessage.builder()
                    .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                    .step(SagaStep.INVENTORY_DEDUCT_PENDING).code("INVENTORY_DEDUCT_SUCCESS")
                    .build();
            //when
            orderSagaManager.handleReply(message);
            //then
            then(orderSagaService).should(never()).proceed(any(), any(), any());
        }

        @Nested
        @DisplayName("Saga 처리 성공 메시지 수신시")
        class SUCCESS {

            @Nested
            @DisplayName("재고 감소 성공 메시지 수신")
            class INVENTORY_DEDUCTED {

                @Test
                @DisplayName("재고 감소 성공 메시지를 수신하면 saga history를 저장하고 주문이 쿠폰을 사용했다면 쿠폰 단계로 넘어간다")
                void handleReply_success_inventory_deducted(){
                    //given
                    String orderNo = "orderNo";
                    Long paymentId = 1L;
                    Long userId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                            .step(SagaStep.INVENTORY_DEDUCT_PENDING).code("INVENTORY_DEDUCT_SUCCESS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.STARTED);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.COUPON_USE_PENDING);
                }

                @Test
                @DisplayName("재고 감소 성공 메시지를 수신하면 saga history를 저장하고 주문이 쿠폰을 사용하지 않았다면 포인트 차감 단계로 넘어간다")
                void handleReply_success_inventory_deducted_skip_coupon(){
                    //given
                    String orderNo = "orderNo";
                    Long paymentId = 1L;
                    Long userId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(null, List.of());
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                            .step(SagaStep.INVENTORY_DEDUCT_PENDING).code("INVENTORY_DEDUCT_SUCCESS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.STARTED);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.POINTS_DEDUCT_PENDING);
                }

                @Test
                @DisplayName("재고 감소 성공 메시지를 수신하면 saga history를 저장하고 쿠폰과 포인트를 사용하지 않았다면 Saga 와 주문을 완료 처리 한다")
                void handleReply_success_inventory_deducted_skip_coupon_and_point(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    Order order = createOrder(orderNo);
                    order.paid();
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(null, List.of());
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.ZERO);
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
                    sagaRepository.save(instance);
                    orderRepository.save(order);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                            .step(SagaStep.INVENTORY_DEDUCT_PENDING).code("INVENTORY_DEDUCT_SUCCESS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPLETE);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.END);
                    assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
                }
            }

            @Nested
            class INVENTORY_RESTORE {

                @Test
                @DisplayName("재고 보상 성공 메시지를 수신하면 saga history를 저장하고 saga와 주문을 실패 처리한다")
                void handleReply_success_inventory_compensate(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    Order order = createOrder(orderNo);
                    order.paid();
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of());
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.ZERO);
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.COUPON_USE_PENDING, payload);
                    instance.compensateTo(SagaStep.INVENTORY_RESTORE_PENDING);
                    sagaRepository.save(instance);
                    orderRepository.save(order);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                            .step(SagaStep.INVENTORY_RESTORE_PENDING).code("INVENTORY_RESTORE_SUCCESS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.FAILED);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.END);
                    assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
                }
            }

            @Nested
            @DisplayName("쿠폰 무효화 성공 메시지 수신")
            class COUPON_USED {

                @Test
                @DisplayName("쿠폰 무효화 성공 메시지를 수신하면 saga history를 저장하고 포인트 차감 단계를 진행한다")
                void handleReply_success_coupon_used() {
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(null, List.of());
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.COUPON_USE_PENDING, payload);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                            .step(SagaStep.COUPON_USE_PENDING).code("COUPON_USED_SUCCESS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.STARTED);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.POINTS_DEDUCT_PENDING);
                }

                @Test
                @DisplayName("쿠폰 무효화 성공 메시지를 수신하면 saga history를 저장하고 주문이 포인트를 사용하지 않았다면 saga와 주문을 완료 처리한다")
                void handleReply_success_coupon_used_skip_point_deduct() {
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    Order order = createOrder(orderNo);
                    order.paid();
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(2L, 3L));
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.ZERO);
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.COUPON_USE_PENDING, payload);
                    sagaRepository.save(instance);
                    orderRepository.save(order);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                            .step(SagaStep.COUPON_USE_PENDING).code("COUPON_USED_SUCCESS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPLETE);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.END);
                    assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
                }
            }

            @Nested
            @DisplayName("쿠폰 보상 성공 메시지 수신")
            class COUPON_RESTORE {

                @Test
                @DisplayName("쿠폰 보상 성공 메시지를 수신 시 history를 저장하고 재고 복구를 진행한다")
                void handleReply_success_coupon_restore(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(2L, 3L));
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.ZERO);
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.POINTS_DEDUCT_PENDING, payload);
                    instance.compensateTo(SagaStep.COUPON_RESTORE_PENDING);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                            .step(SagaStep.COUPON_RESTORE_PENDING).code("COUPON_RESTORE_SUCCESS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_RESTORE_PENDING);
                }
            }

            @Nested
            @DisplayName("포인트 차감 성공 메시지 수신")
            class POINT_DEDUCTED {

                @Test
                @DisplayName("포인트 성공 메시지 수신 시 saga 상태변경과 history를 저장하고 주문상태를 완료 처리한다")
                void handleReply_success_point_deducted(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    Order order = createOrder(orderNo);
                    order.paid();
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(null, List.of());
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.POINTS_DEDUCT_PENDING, payload);
                    sagaRepository.save(instance);
                    orderRepository.save(order);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.SUCCESS).orderNo(orderNo)
                            .step(SagaStep.POINTS_DEDUCT_PENDING).code("POINTS_DEDUCT_PENDING")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPLETE);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.END);
                    assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
                }
            }
        }

        @Nested
        @DisplayName("Saga 처리 실패 메시지 수신시")
        class FAIL {

            @Nested
            @DisplayName("재고 감소 실패 메시지 수신")
            class INVENTORY_DEDUCT {

                @Test
                @DisplayName("재고 감소 실패 메시지 수신시 history를 저장하고 saga와 order 를 실패 처리한다")
                void handleReply_fail_inventory_deducted(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    Order order = createOrder(orderNo);
                    order.paid();
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(null, List.of());
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
                    sagaRepository.save(instance);
                    orderRepository.save(order);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.FAILURE).orderNo(orderNo)
                            .step(SagaStep.INVENTORY_DEDUCT_PENDING).code("INSUFFICIENT_STOCK")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    Order findOrder = orderRepository.findByOrderNo(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.FAILED);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.END);
                    assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
                }
            }

            @Nested
            @DisplayName("재고 복구 실패 메시지 수신시")
            class INVENTORY_RESTORE {

                @Test
                @DisplayName("재고 보상 실패 메시지 수신시 saga 상태를 변경하지 않고 기존 상태를 유지한다")
                void handleReply_fail_inventory_compensate(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L));
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.ZERO);
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.COUPON_USE_PENDING, payload);
                    instance.compensateTo(SagaStep.INVENTORY_RESTORE_PENDING);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.FAILURE).orderNo(orderNo)
                            .step(SagaStep.INVENTORY_RESTORE_PENDING).code("DB_DEADLOCK_ERROR")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_RESTORE_PENDING);
                    assertThat(findInstance.getHistories()).hasSize(1);
                }
            }

            @Nested
            @DisplayName("쿠폰 무효화 실패 메시지 수신")
            class COUPON_USED {

                @Test
                @DisplayName("쿠폰 무효화 실패 메시지 수신시 history를 저장 후 saga 상태를 변경하고 재고 보상 단계를 진행한다")
                void handleReply_fail_coupon_used(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(2L, 3L));
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.COUPON_USE_PENDING, payload);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.FAILURE).orderNo(orderNo)
                            .step(SagaStep.COUPON_USE_PENDING).code("COUPON_EXPIRED")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_RESTORE_PENDING);
                }
            }

            @Nested
            @DisplayName("쿠폰 보상 실패")
            class COUPON_RESTORE {

                @Test
                @DisplayName("쿠폰 보상 실패 메시지 수신시 saga 상태를 변경하지 않고 기존 상태를 유지한다")
                void handleReply_fail_coupon_compensate(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L));
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.POINTS_DEDUCT_PENDING, payload);
                    instance.compensateTo(SagaStep.COUPON_RESTORE_PENDING);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.FAILURE).orderNo(orderNo)
                            .step(SagaStep.COUPON_RESTORE_PENDING).code("DB_DEADLOCK_ERROR")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.COUPON_RESTORE_PENDING);
                    assertThat(findInstance.getHistories()).hasSize(1);
                }
            }

            @Nested
            @DisplayName("포인트 무효화 실패 메시지 수신")
            class POINTS_DEDUCTED {

                @Test
                @DisplayName("포인트 차감 실패 메시지 수신시 history를 저장 후 saga 상태를 변경하고 쿠폰 보상을 진행한다")
                void handleReply_fail_point_deducted(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(2L, 3L));
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.POINTS_DEDUCT_PENDING, payload);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.FAILURE).orderNo(orderNo)
                            .step(SagaStep.POINTS_DEDUCT_PENDING).code("INSUFFICIENT_POINTS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.COUPON_RESTORE_PENDING);
                }

                @Test
                @DisplayName("포인트 차감 실패 메시지 수신시 history를 저장 후 saga 상태를 변경하고 쿠폰을 사용하지 않은 경우 재고 감소를 진행한다")
                void handleReply_fail_point_deducted_skip_coupon(){
                    //given
                    String orderNo = "orderNo";
                    Long userId = 1L;
                    Long paymentId = 1L;
                    SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(null, List.of());
                    SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
                    SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
                    SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
                    OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.POINTS_DEDUCT_PENDING, payload);
                    sagaRepository.save(instance);
                    SagaReplyMessage message = SagaReplyMessage.builder()
                            .sagaId(instance.getId()).result(SagaResult.FAILURE).orderNo(orderNo)
                            .step(SagaStep.POINTS_DEDUCT_PENDING).code("INSUFFICIENT_POINTS")
                            .build();
                    //when
                    orderSagaManager.handleReply(message);
                    //then
                    OrderSagaInstance findInstance = sagaRepository.findByOrderNoWithHistories(orderNo).orElseThrow();
                    assertThat(findInstance.getHistories()).hasSize(1);
                    assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
                    assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_RESTORE_PENDING);
                }
            }
        }
    }

    private Order createOrder(String orderNo) {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인", Money.wons(1000L));
        List<OrderItem> orderItems = createOrderItems();
        return Order.init(orderNo, orderer, shippingAddress, cartCoupon, orderItems, Money.wons(10000L),
                Money.wons(1000L), Money.wons(2000L), Money.wons(1000L), Money.wons(6000L));
    }

    private List<OrderItem> createOrderItems() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot productPriceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
//        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(2L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
//        OrderItem orderItem = OrderItem.create(productSnapshot, productPriceSnapshot, itemCoupon, 1, List.of());
//        return List.of(orderItem);
        return null;
    }
}
package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.domain.model.ItemOption;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.*;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.ItemOptionDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class OrderServiceTest extends ExcludeInfraTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

    private OrderCreationContext mockOrderCreationContext(List<OrderItemCreationContext> itemContexts) {
        Orderer orderer = Orderer.of(1L, "유저", "010-1234-5678");
        long totalOriginPrice = 0;
        long totalProductDiscount = 0;
        long couponDiscount = 1000L;
        long usedPoint = 1000L;
        for (OrderItemCreationContext itemContext : itemContexts) {
            totalOriginPrice += itemContext.getOrderItemPrice().getDiscountedPrice() * itemContext.getQuantity();
            totalProductDiscount += itemContext.getOrderItemPrice().getDiscountAmount() * itemContext.getQuantity();
        }
        long finalPaymentAmount = totalOriginPrice - totalProductDiscount - couponDiscount - usedPoint;
        OrderPriceDetail orderPriceDetail = OrderPriceDetail.of(totalOriginPrice, totalProductDiscount, couponDiscount,
                usedPoint, finalPaymentAmount);
        CouponInfo couponInfo = CouponInfo.of(1L, "1000원 할인 쿠폰", couponDiscount);

        return OrderCreationContext.builder()
                .orderer(orderer)
                .orderPriceDetail(orderPriceDetail)
                .couponInfo(couponInfo)
                .orderItemCreationContexts(itemContexts)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
    }

    private OrderItemCreationContext mockItemCreationContext(Long variantId, long originalPrice, int discountRate, int quantity) {
        long discountAmount = (long) (originalPrice * (discountRate / 100.0));
        OrderedProduct orderedProduct = OrderedProduct.of(1L, variantId, "TEST", "상품", "http://test.jpg");
        OrderItemPrice orderItemPrice = OrderItemPrice.of(originalPrice, discountRate, discountAmount, originalPrice - discountAmount);
        long lineTotal = orderItemPrice.getDiscountedPrice() * quantity;
        List<OrderItemCreationContext.ItemOption> options = List.of(OrderItemCreationContext.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build());
        return OrderItemCreationContext.builder()
                .orderedProduct(orderedProduct)
                .orderItemPrice(orderItemPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .itemOptions(options)
                .build();
    }

    @Nested
    @DisplayName("주문 저장")
    class Save {

        @Test
        @DisplayName("주문을 저장한다")
        void saveOrder(){
            //given
            OrderItemCreationContext item1 = mockItemCreationContext(1L, 10000L, 10, 3);
            OrderItemCreationContext item2 = mockItemCreationContext(2L, 20000L, 20, 5);
            OrderCreationContext creationContext = mockOrderCreationContext(List.of(item1, item2));
            //when
            OrderDto result = orderService.saveOrder(creationContext);
            //then
            // 주문 ID, 주문 이름, 주문일자 생성 검증
            assertThat(result).satisfies(order -> {
                assertThat(order.getId()).isNotNull();
                assertThat(order.getOrderNo()).isNotNull();
                assertThat(order.getOrderedAt()).isNotNull();
            });

            // 주문 생성 기본 정보 검증 (주문 상태, 주문 이름, 주문자, 주문 가격정보, 쿠폰 정보, 배송지 정보, 결제 정보, 실패 코드)
            assertThat(result)
                    .extracting(OrderDto::getStatus, OrderDto::getOrderName, OrderDto::getOrderer, OrderDto::getOrderPriceDetail,
                            OrderDto::getCouponInfo, OrderDto::getDeliveryAddress, OrderDto::getPaymentInfo, OrderDto::getOrderFailureCode)
                    .containsExactly(OrderStatus.PENDING, "상품 외 1건", creationContext.getOrderer(),
                            creationContext.getOrderPriceDetail(), creationContext.getCouponInfo(), creationContext.getDeliveryAddress(), null, null);

            // 주문 상품 수량 및 ID 검증
            assertThat(result.getOrderItems())
                    .hasSize(2)
                    .allSatisfy(item -> assertThat(item.getId()).isNotNull());

            // 주문 상품 기본 정보 검증 (상품 정보, 상품 가격정보, 주문 수량, 라인 금액)
            assertThat(result.getOrderItems())
                    .extracting(OrderItemDto::getOrderedProduct, OrderItemDto::getOrderItemPrice, OrderItemDto::getQuantity, OrderItemDto::getLineTotal)
                    .containsExactlyInAnyOrder(
                            tuple(item1.getOrderedProduct(), item1.getOrderItemPrice(), item1.getQuantity(), item1.getLineTotal()),
                            tuple(item2.getOrderedProduct(), item2.getOrderItemPrice(), item2.getQuantity(), item2.getLineTotal())
                    );

            assertThat(result.getOrderItems())
                    .flatExtracting(OrderItemDto::getItemOptions)
                    .extracting(ItemOptionDto::getOptionTypeName, ItemOptionDto::getOptionValueName)
                    .containsExactlyInAnyOrder(
                            tuple("사이즈", "XL"),
                            tuple("사이즈", "XL"));
        }
    }

//    @Test
//    @DisplayName("주문을 저장한다")
//    void saveOrder(){
//        //given
//        OrderCreationContext creationContext = null;
//        //when
//        OrderDto orderDto = orderService.saveOrder(creationContext);
//        //then
//        assertThat(orderDto.getOrderId()).isNotNull();
//        assertThat(orderDto)
//                .extracting(OrderDto::getStatus, OrderDto::getOrderName, OrderDto::getOrderFailureCode)
//                .contains(OrderStatus.PENDING, "상품1 외 1건", null);
//        assertThat(orderDto.getOrderedAt()).isNotNull();
//
//        assertThat(orderDto.getOrderPriceDetail())
//                .extracting(
//                        OrderPriceDetail::getTotalOriginPrice,
//                        OrderPriceDetail::getTotalProductDiscount,
//                        OrderPriceDetail::getCouponDiscount,
//                        OrderPriceDetail::getPointDiscount,
//                        OrderPriceDetail::getFinalPaymentAmount
//                )
//                .contains(
//                        TOTAL_ORIGIN_PRICE,
//                        TOTAL_PROD_DISCOUNT,
//                        COUPON_DISCOUNT,
//                        USE_POINT,
//                        FINAL_PRICE
//                );
//
//        assertThat(orderDto.getOrderItemDtoList())
//                .hasSize(2)
//                        .extracting(
//                                OrderItemDto::getProductId,
//                                OrderItemDto::getProductName,
//                                OrderItemDto::getQuantity,
//                                OrderItemDto::getLineTotal
//                        )
//                .containsExactlyInAnyOrder(
//                        tuple(PROD1_ID, PROD1_NAME, PROD1_QTY, PROD1_LINE_TOTAL),
//                        tuple(PROD2_ID, PROD2_NAME, PROD2_QTY, PROD2_LINE_TOTAL)
//                );
//
//        assertThat(orderDto.getCouponInfo())
//                .extracting(CouponInfo::getCouponId, CouponInfo::getCouponName, CouponInfo::getDiscountAmount)
//                .containsExactly(1L, "1000원 할인 쿠폰", COUPON_DISCOUNT);
//    }

//    @Test
//    @DisplayName("주문 상태를 변경한다")
//    void changeOrderStatus() {
//        //given
//        OrderCreationContext context = createDefaultContext();
//        Order order = Order.create(context);
//        Order savedOrder = orderRepository.save(order);
//        //when
//        OrderDto result = orderService.changeOrderStatus(savedOrder.getOrderNo(), OrderStatus.PAYMENT_WAITING);
//        //then
//        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
//        assertThat(result.getOrderId()).isEqualTo(savedOrder.getId());
//        assertThat(result.getOrderFailureCode()).isNull();
//    }
//
//    @Test
//    @DisplayName("주문의 상태를 변경할때 주문을 찾을 수 없으면 예외를 던진다")
//    void changeOrderStatus_notFound() {
//        //given
//        //when
//        //then
//        assertThatThrownBy(() -> orderService.changeOrderStatus(ORDER_NO, OrderStatus.PAYMENT_WAITING))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("주문을 조회한다")
//    void getOrder(){
//        //given
//        OrderCreationContext context = createDefaultContext();
//        Order savedOrder = orderRepository.save(Order.create(context));
//        //when
//        OrderDto result = orderService.getOrder(savedOrder.getOrderNo(), USER_ID);
//        //then
//        assertThat(result.getOrderId()).isEqualTo(savedOrder.getId());
//        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
//
//        assertThat(result.getOrderPriceDetail())
//                .extracting(OrderPriceDetail::getTotalOriginPrice, OrderPriceDetail::getTotalProductDiscount,
//                        OrderPriceDetail::getCouponDiscount, OrderPriceDetail::getPointDiscount, OrderPriceDetail::getFinalPaymentAmount)
//                .contains(TOTAL_ORIGIN_PRICE, TOTAL_PROD_DISCOUNT, COUPON_DISCOUNT, USE_POINT, FINAL_PRICE);
//
//        assertThat(result.getOrderItemDtoList())
//                .hasSize(2)
//                        .extracting(
//                                OrderItemDto::getProductId,
//                                OrderItemDto::getProductName,
//                                OrderItemDto::getQuantity,
//                                OrderItemDto::getLineTotal
//                        )
//                        .containsExactly(
//                                tuple(PROD1_ID, PROD1_NAME, PROD1_QTY, PROD1_LINE_TOTAL),
//                                tuple(PROD2_ID, PROD2_NAME, PROD2_QTY, PROD2_LINE_TOTAL)
//                        );
//
//        assertThat(result.getCouponInfo())
//                .extracting(CouponInfo::getCouponId, CouponInfo::getCouponName, CouponInfo::getDiscountAmount)
//                .contains(1L, "1000원 할인 쿠폰", COUPON_DISCOUNT);
//    }
//
//    @Test
//    @DisplayName("주문을 조회할때 주문을 찾을 수 없으면 예외를 던진다")
//    void getOrder_notFound(){
//        //given
//        //when
//        //then
//        assertThatThrownBy(() -> orderService.getOrder("NOT_EXIST_ORDER_NO", USER_ID))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("주문을 조회할때 주문의 사용자 Id 가 요청 사용자 Id와 다른 경우 예외를 던진다")
//    void getOrder_noPermission() {
//        //given
//        Long otherUserId = 20L;
//        OrderCreationContext context = createDefaultContext();
//        Order savedOrder = orderRepository.save(Order.create(context));
//        //when
//        //then
//        assertThatThrownBy(() -> orderService.getOrder(savedOrder.getOrderNo(), otherUserId))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(OrderErrorCode.ORDER_NO_PERMISSION);
//    }
//
//    @Test
//    @DisplayName("주문을 실패 상태로 변경한다")
//    void canceledOrder() {
//        //given
//        OrderCreationContext context = createDefaultContext();
//        Order savedOrder = orderRepository.save(Order.create(context));
//        OrderFailureCode failureCode = OrderFailureCode.OUT_OF_STOCK;
//        //when
//        OrderDto result = orderService.canceledOrder(savedOrder.getOrderNo(), failureCode);
//        //then
//        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELED);
//        assertThat(result.getOrderFailureCode()).isEqualTo(OrderFailureCode.OUT_OF_STOCK);
//        assertThat(result.getOrderId()).isEqualTo(savedOrder.getId());
//    }
//
//    @Test
//    @DisplayName("주문을 실패 상태로 변경할때 주문을 찾을 수 없으면 예외를 던진다")
//    void canceledOrder_notFound() {
//        //given
//        //when
//        //then
//        assertThatThrownBy(() -> orderService.canceledOrder(ORDER_NO, OrderFailureCode.OUT_OF_STOCK))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("유저 ID 와 조회 커맨드로 주문 목록을 조회한다")
//    void getOrders(){
//        //given
//        OrderCreationContext context = createDefaultContext();
//        Order savedOrder1 = orderRepository.save(Order.create(context));
//        Order savedOrder2 = orderRepository.save(Order.create(context));
//        OrderSearchCondition condition = OrderSearchCondition.builder()
//                .page(1)
//                .size(10)
//                .sort("latest").build();
//        //when
//        Page<OrderDto> result = orderService.getOrders(USER_ID, condition);
//        //then
//
//        assertThat(result.getTotalElements()).isEqualTo(2);
//        assertThat(result.getTotalPages()).isEqualTo(1);
//        assertThat(result.getContent()).hasSize(2);
//        assertThat(result.getNumber()).isEqualTo(0);
//
//        assertThat(result.getContent())
//                .extracting(
//                        OrderDto::getUserId,
//                        OrderDto::getStatus,
//                        o -> o.getOrderPriceDetail().getFinalPaymentAmount()
//                )
//                .contains(
//                        tuple(USER_ID, OrderStatus.PENDING, FINAL_PRICE),
//                        tuple(USER_ID, OrderStatus.PENDING, FINAL_PRICE)
//                );
//
//        assertThat(result.getContent())
//                .extracting(OrderDto::getOrderId)
//                .containsExactly(savedOrder2.getId(), savedOrder1.getId());
//    }
//
//    @Test
//    @DisplayName("주문에 결제 정보를 저장한다")
//    void completedOrder(){
//        //given
//        String paymentKey = "paymentKey";
//        OrderCreationContext context = createDefaultContext();
//        Order order = Order.create(context);
//        order.changeStatus(OrderStatus.PAYMENT_WAITING);
//        Order savedOrder = orderRepository.save(order);
//        PaymentCreationCommand command = PaymentCreationCommand.builder()
//                .orderNo(savedOrder.getOrderNo())
//                .paymentKey(paymentKey)
//                .amount(order.getPriceInfo().getFinalPaymentAmount())
//                .method("CARD")
//                .approvedAt(LocalDateTime.now())
//                .build();
//        //when
//        OrderDto orderDto = orderService.completedOrder(command);
//        //then
//        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.COMPLETED);
//        assertThat(orderDto.getPaymentInfo().getId()).isNotNull();
//        assertThat(orderDto.getPaymentInfo())
//                .extracting(PaymentInfo::getPaymentKey, PaymentInfo::getAmount, PaymentInfo::getMethod)
//                .containsExactly(paymentKey, order.getPriceInfo().getFinalPaymentAmount(), "CARD");
//    }
}

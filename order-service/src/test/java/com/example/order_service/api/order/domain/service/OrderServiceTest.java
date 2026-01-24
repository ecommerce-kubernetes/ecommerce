package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.*;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemOptionDto;
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

    private OrderCreationContext createBaseOrderContext(List<OrderItemCreationContext> itemContexts) {
        Orderer orderer = Orderer.of(1L, "유저", "010-1234-5678");
        long totalOriginPrice = 0;
        long totalProductDiscount = 0;
        long usedPoint = 1000L;

        for (OrderItemCreationContext itemContext : itemContexts) {
            totalOriginPrice += itemContext.getOrderItemPrice().getDiscountedPrice() * itemContext.getQuantity();
            totalProductDiscount += itemContext.getOrderItemPrice().getDiscountAmount() * itemContext.getQuantity();
        }

        long finalPaymentAmount = totalOriginPrice - totalProductDiscount - usedPoint;
        OrderPriceDetail priceDetail = OrderPriceDetail.of(totalOriginPrice, totalProductDiscount, 0, usedPoint, finalPaymentAmount);

        return OrderCreationContext.builder()
                .orderer(orderer)
                .orderPriceDetail(priceDetail)
                .couponInfo(null)
                .orderItemCreationContexts(itemContexts)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
    }

    private OrderCreationContext applyCoupon(OrderCreationContext context, CouponInfo couponInfo) {
        OrderPriceDetail oldPrice = context.getOrderPriceDetail();
        long newFinalAmount = oldPrice.getFinalPaymentAmount() - couponInfo.getDiscountAmount();

        OrderPriceDetail newPrice = OrderPriceDetail.of(
                oldPrice.getTotalOriginPrice(),
                oldPrice.getTotalProductDiscount(),
                couponInfo.getDiscountAmount(),
                oldPrice.getPointDiscount(),
                newFinalAmount
        );

        return OrderCreationContext.builder()
                .orderer(context.getOrderer())
                .orderPriceDetail(newPrice)
                .couponInfo(couponInfo)
                .orderItemCreationContexts(context.getOrderItemCreationContexts())
                .deliveryAddress(context.getDeliveryAddress())
                .build();
    }

    private OrderItemCreationContext mockItemCreationContext(Long variantId, long originalPrice, int discountRate, int quantity) {
        long discountAmount = (long) (originalPrice * (discountRate / 100.0));
        OrderedProduct orderedProduct = OrderedProduct.of(1L, variantId, "TEST", "상품", "http://test.jpg");
        OrderItemPrice orderItemPrice = OrderItemPrice.of(originalPrice, discountRate, discountAmount, originalPrice - discountAmount);
        long lineTotal = orderItemPrice.getDiscountedPrice() * quantity;
        List<OrderItemCreationContext.CreateItemOptionSpec> options = List.of(OrderItemCreationContext.CreateItemOptionSpec.builder().optionTypeName("사이즈").optionValueName("XL").build());
        return OrderItemCreationContext.builder()
                .orderedProduct(orderedProduct)
                .orderItemPrice(orderItemPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .createItemOptionSpecs(options)
                .build();
    }

    @Nested
    @DisplayName("주문 저장")
    class Save {

        @Test
        @DisplayName("쿠폰 사용 주문을 저장한다")
        void saveOrder_use_coupon(){
            //given
            OrderItemCreationContext item1 = mockItemCreationContext(1L, 10000L, 10, 3);
            OrderItemCreationContext item2 = mockItemCreationContext(2L, 20000L, 20, 5);
            OrderCreationContext baseOrderContext = createBaseOrderContext(List.of(item1, item2));
            CouponInfo couponInfo = CouponInfo.of(1L, "1000원 할인 쿠폰", 1000L);
            OrderCreationContext applyCouponOrderContext = applyCoupon(baseOrderContext, couponInfo);
            //when
            OrderDto result = orderService.saveOrder(applyCouponOrderContext);
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
                    .containsExactly(OrderStatus.PENDING, "상품 외 1건", applyCouponOrderContext.getOrderer(), applyCouponOrderContext.getOrderPriceDetail(),
                            applyCouponOrderContext.getCouponInfo(), applyCouponOrderContext.getDeliveryAddress(), null, null);

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

            // 주문 상품 옵션 정보 검증
            assertThat(result.getOrderItems())
                    .flatExtracting(OrderItemDto::getItemOptions)
                    .extracting(OrderItemOptionDto::getOptionTypeName, OrderItemOptionDto::getOptionValueName)
                    .containsExactlyInAnyOrder(
                            tuple("사이즈", "XL"),
                            tuple("사이즈", "XL"));
        }

        @Test
        @DisplayName("쿠폰을 사용하지 않은 주문을 생성한다")
        void saveOrder_not_use_coupon(){
            //given
            OrderItemCreationContext item1 = mockItemCreationContext(1L, 10000L, 10, 3);
            OrderItemCreationContext item2 = mockItemCreationContext(2L, 20000L, 20, 5);
            OrderCreationContext creationContext = createBaseOrderContext(List.of(item1, item2));
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
                    .containsExactly(OrderStatus.PENDING, "상품 외 1건", creationContext.getOrderer(), creationContext.getOrderPriceDetail(),
                            null, creationContext.getDeliveryAddress(), null, null);

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

            // 주문 상품 옵션 정보 검증
            assertThat(result.getOrderItems())
                    .flatExtracting(OrderItemDto::getItemOptions)
                    .extracting(OrderItemOptionDto::getOptionTypeName, OrderItemOptionDto::getOptionValueName)
                    .containsExactlyInAnyOrder(
                            tuple("사이즈", "XL"),
                            tuple("사이즈", "XL"));
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("주문 상태를 변경한다")
        void changeOrderStatus(){
            //given
            OrderItemCreationContext item1 = mockItemCreationContext(1L, 10000L, 10, 3);
            OrderItemCreationContext item2 = mockItemCreationContext(2L, 20000L, 20, 5);
            OrderCreationContext creationContext = createBaseOrderContext(List.of(item1, item2));
            Order savedOrder = orderRepository.save(Order.create(creationContext));
            //when
            OrderDto result = orderService.changeOrderStatus(savedOrder.getOrderNo(), OrderStatus.PAYMENT_WAITING);
            //then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
        }

        @Test
        @DisplayName("주문 상품을 찾을 수 없으면 상태를 변경할 수 없다")
        void changeOrderStatus_order_not_found(){
            //given
            //when
            //then
            assertThatThrownBy(() -> orderService.changeOrderStatus("UNKNOWN", OrderStatus.PAYMENT_WAITING))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("주문 상태를 취소로 변경한다")
        void canceledOrder(){
            //given
            OrderItemCreationContext item1 = mockItemCreationContext(1L, 10000L, 10, 3);
            OrderItemCreationContext item2 = mockItemCreationContext(2L, 20000L, 20, 5);
            OrderCreationContext creationContext = createBaseOrderContext(List.of(item1, item2));
            Order savedOrder = orderRepository.save(Order.create(creationContext));
            //when
            OrderDto result = orderService.canceledOrder(savedOrder.getOrderNo(), OrderFailureCode.OUT_OF_STOCK);
            //then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("주문 상품을 찾을 수 없으면 주문 취소를 할 수 없다")
        void canceledOrder_order_not_found(){
            //given
            //when
            //then
            assertThatThrownBy(() -> orderService.canceledOrder("UNKNOWN", OrderFailureCode.OUT_OF_STOCK))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }
    }

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

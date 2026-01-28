package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.common.entity.BaseEntity;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.model.vo.OrderPriceDetail;
import com.example.order_service.api.order.domain.model.vo.Orderer;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrderPriceSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrdererSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNo;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String orderName;
    @Embedded
    private Orderer orderer;
    @Embedded
    private OrderPriceDetail orderPriceDetail;
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    private OrderFailureCode failureCode;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Coupon coupon;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<Payment> payments = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Order(String orderNo, OrderStatus status, String orderName, Orderer orderer, OrderPriceDetail orderPriceDetail, String deliveryAddress, OrderFailureCode failureCode) {
        this.orderNo = orderNo;
        this.orderer = orderer;
        this.status = status;
        this.orderName = orderName;
        this.deliveryAddress = deliveryAddress;
        this.orderPriceDetail = orderPriceDetail;
        this.failureCode = failureCode;
    }

    private void addOrderItem(OrderItem orderItem){
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    private void addCoupon(Coupon coupon){
        this.coupon = coupon;
        coupon.setOrder(this);
    }

    public void changeStatus(OrderStatus orderStatus){
        this.status = orderStatus;
    }

    public boolean isOwner(Long accessUserId) {
        return this.orderer.getUserId().equals(accessUserId);
    }

    // 유효한 결제 정보를 추출
    public Payment getValidPayment() {
        return this.payments.stream()
                // 타입이 결제인것
                .filter(p -> p.getType() == PaymentType.PAYMENT)
                // 상태는 완료 또는 입금 대기인것
                .filter(p -> p.getStatus() == PaymentStatus.DONE || p.getStatus() == PaymentStatus.WAITING_FOR_DEPOSIT)
                // 동일한 결제 엔티티가 있다면 가장 최신의 결제 정보
                .max(Comparator.comparing(Payment::getId))
                .orElse(null);
    }

    public void canceled(OrderFailureCode code) {
        this.status = OrderStatus.CANCELED;
        this.failureCode = code;
    }

    public static Order create(OrderCreationContext context) {
        validateOrderItems(context.getOrderItemCreationContexts());
        String orderNo = generatedOrderNo();
        String orderName = generateOrderName(context.getOrderItemCreationContexts());
        Order order = createOrder(context, orderNo, orderName);
        for (OrderItemCreationContext itemCtx : context.getOrderItemCreationContexts()) {
            OrderItem orderItem = OrderItem.create(itemCtx);
            order.addOrderItem(orderItem);
        }

        if (context.getCoupon() != null) {
            Coupon coupon = Coupon.create(context.getCoupon());
            order.addCoupon(coupon);
        }

        return order;
    }

    private static void validateOrderItems(List<OrderItemCreationContext> orderItemsContext) {
        if (orderItemsContext == null || orderItemsContext.isEmpty()) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEM_MINIMUM_ONE_REQUIRED);
        }
    }

    private static String generateOrderName(List<OrderItemCreationContext> itemContexts){
        String firstProductName = itemContexts.get(0).getProductSpec().getProductName();
        int size = itemContexts.size();
        if(size == 1){
            return firstProductName;
        }
        return firstProductName + " 외 " + (size - 1) + "건";
    }

    private static String generatedOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + date + "-" + randomStr;
    }

    private static Order createOrder(OrderCreationContext context, String orderNo, String orderName){
        return Order.builder()
                .orderNo(orderNo)
                .orderer(mapToOrderer(context.getOrderer()))
                .status(OrderStatus.PENDING)
                .orderName(orderName)
                .deliveryAddress(context.getDeliveryAddress())
                .orderPriceDetail(mapToOrderPriceDetail(context.getOrderPrice()))
                .failureCode(null)
                .build();
    }

    private static Orderer mapToOrderer(OrdererSpec ordererSpec) {
        return Orderer.of(ordererSpec.getUserId(), ordererSpec.getUserName(), ordererSpec.getPhoneNumber());
    }

    private static OrderPriceDetail mapToOrderPriceDetail(OrderPriceSpec priceSpec) {
        return OrderPriceDetail.of(priceSpec.getTotalOriginPrice(), priceSpec.getTotalProductDiscount(),
                priceSpec.getCouponDiscount(), priceSpec.getPointDiscount(), priceSpec.getFinalPaymentAmount());
    }
}

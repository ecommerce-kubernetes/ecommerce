package com.example.order_service.order.application;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.dto.PageDto;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.ErrorCode;
import com.example.order_service.order.api.dto.request.OrderSearchCondition;
import com.example.order_service.order.application.dto.command.OrderCommand;
import com.example.order_service.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.order.application.dto.result.OrderListResponse;
import com.example.order_service.order.application.dto.result.OrderPaymentResult;
import com.example.order_service.order.application.dto.result.OrderResult;
import com.example.order_service.order.application.event.OrderFailedEvent;
import com.example.order_service.order.application.event.OrderPaymentReadyEvent;
import com.example.order_service.order.application.event.PaymentCompletedEvent;
import com.example.order_service.order.application.event.PaymentFailedEvent;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderPaymentGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.command.OrderProductCommand;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.domain.model.OrderFailureCode;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.model.vo.PaymentStatus;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.domain.service.OrderService;
import com.example.order_service.order.domain.service.dto.command.PaymentCreationContext;
import com.example.order_service.order.domain.service.dto.result.OrderDto;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService {

    private final OrderUserGateway orderUserGateway;
    private final OrderProductGateway orderProductGateway;
    private final OrderPaymentGateway orderPaymentGateway;
    private final OrderCouponGateway orderCouponGateway;
    private final OrderSheetRepository orderSheetRepository;
    private final OrderCreationContextMapper mapper;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderResult.Create initialOrder(OrderCommand.Create command) {
        OrderSheet orderSheet = findOrderSheetById(command.orderSheetId());
        if (!orderSheet.isOwner(command.userId())) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_ACCESS_DENIED);
        }
        if (orderSheet.isExpired()){
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }
        OrderProductResult.ProductList products = getOrderedProducts(orderSheet.getItems());
        OrderCouponResult.Calculate appliedCoupons = getAppliedCoupons(orderSheet);
        OrderUserResult.UserPoint userPoints = getUserPoints(orderSheet);
        //주문 생성
        //saga 이벤트 시작
        return null;
    }

    private OrderProductResult.ProductList getOrderedProducts(List<OrderSheetItem> items) {
        List<OrderProductCommand.OrderItem> command = items.stream().map(item ->
                        OrderProductCommand.OrderItem.of(item.getProductVariantId(), item.getQuantity())).toList();
        return orderProductGateway.getProducts(command);
    }

    private OrderCouponResult.Calculate getAppliedCoupons(OrderSheet orderSheet) {
        List<OrderCouponCommand.AppliedCouponItem> itemCouponCommand = orderSheet.getItems().stream().map(
                item -> OrderCouponCommand.AppliedCouponItem.of(item.getProductVariantId(),
                        item.getDiscountedPrice(),
                        item.getQuantity(),
                        item.getCouponId())
        ).toList();
        OrderCouponCommand.Calculate command = OrderCouponCommand.Calculate
                .of(orderSheet.getOrderer().getUserId(),
                        orderSheet.getCartCoupon().getCouponId(),
                        itemCouponCommand);
        return orderCouponGateway.calculate(command);
    }

    private OrderUserResult.UserPoint getUserPoints(OrderSheet orderSheet) {
        Long userId = orderSheet.getOrderer().getUserId();
        Money pointEligibleAmount = orderSheet.getPointEligibleAmount();
        Money usedPoints = orderSheet.getUsedPoints();
        return orderUserGateway.getUserPointsForOrder(userId, pointEligibleAmount, usedPoints);
    }

    public void preparePayment(String orderNo) {
        OrderDto orderDto = orderService.preparePaymentWaiting(orderNo);
        // SSE 메시지 전송을 위한 이벤트 발행
        eventPublisher.publishEvent(OrderPaymentReadyEvent.from(orderDto));
    }

    public void processOrderFailure(String orderNo, OrderFailureCode orderFailureCode){
        OrderDto orderDto = orderService.canceledOrder(orderNo, orderFailureCode);
        // SSE 메시지 전송을 위한 이벤트 발행
        eventPublisher.publishEvent(OrderFailedEvent.from(orderDto));
    }

    public OrderDetailResponse confirmOrderPayment(String orderNo, Long userId, String paymentKey, Long amount) {
        OrderDto order = orderService.getOrder(orderNo, userId);
        // 결제 가능한 상태인지 검증
        validBeforePayment(order, amount);
        // 결제 서비스 결제 승인 요청
        OrderPaymentResult.Payment orderPaymentInfo = confirmPayment(order.getOrderNo(), paymentKey, order.getOrderPriceInfo().getFinalPaymentAmount());
        if (orderPaymentInfo.status() != PaymentStatus.DONE) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_APPROVAL_FAIL);
        }
        PaymentCreationContext paymentContext = mapper.mapPaymentCreationContext(orderPaymentInfo);
        OrderDto orderDto = orderService.completePayment(paymentContext);
        List<Long> orderedItemVariantIds = orderDto.getOrderItems().stream().map(i -> i.getOrderedProduct().getProductVariantId()).toList();
        eventPublisher.publishEvent(PaymentCompletedEvent.of(orderDto.getOrderNo(), orderDto.getOrderer().getUserId(), orderedItemVariantIds));
        return OrderDetailResponse.from(orderDto);
    }

    public OrderDetailResponse getOrder(Long userId, String orderNo) {
        OrderDto order = orderService.getOrder(orderNo, userId);
        return OrderDetailResponse.from(order);
    }

    public PageDto<OrderListResponse> getOrders(Long userId, OrderSearchCondition condition){
        Page<OrderDto> orders = orderService.getOrders(userId, condition);
        return PageDto.of(orders, OrderListResponse::from);
    }

    private OrderPaymentResult.Payment confirmPayment(String orderNo, String paymentKey, Long amount) {
        try {
            // 결제 서비스를 호출해 결제를 진행
            return orderPaymentGateway.confirmOrderPayment(orderNo, paymentKey, amount);
        } catch (BusinessException e) {
            // 결제 서비스 호출중 예외 발생시 주문 상태를 변경하고 saga 롤백을 위한 이벤트를 발행
            OrderFailureCode orderFailureCode = mapToOrderFailureCode(e.getErrorCode());
            OrderDto failOrderDto = orderService.failPayment(orderNo, orderFailureCode);
            eventPublisher.publishEvent(PaymentFailedEvent
                    .of(failOrderDto.getOrderNo(),
                            failOrderDto.getOrderer().getUserId(),
                            e.getErrorCode().name(),
                            e.getErrorCode().getMessage()));
            throw e;
        }
    }

    private void validBeforePayment(OrderDto order, Long amount) {
        if (!order.getStatus().equals(OrderStatus.PAYMENT_WAITING)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_PAYABLE);
        }

        if (order.getOrderPriceInfo().getFinalPaymentAmount() != amount) {
            throw new BusinessException(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }
    }

    private OrderFailureCode mapToOrderFailureCode(ErrorCode errorCode) {
        if (errorCode == PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE) {
            return OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE;
        }
        if (errorCode == PaymentErrorCode.PAYMENT_TIMEOUT) {
            return OrderFailureCode.PAYMENT_TIMEOUT;
        }
        if (errorCode == PaymentErrorCode.PAYMENT_NOT_FOUND) {
            return OrderFailureCode.PAYMENT_NOT_FOUND;
        }
        return OrderFailureCode.UNKNOWN;
    }

    private OrderSheet findOrderSheetById(String sheetId) {
        return orderSheetRepository.findById(sheetId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));
    }
}

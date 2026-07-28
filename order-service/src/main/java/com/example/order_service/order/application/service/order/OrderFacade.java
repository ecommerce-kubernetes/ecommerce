package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.mapper.OrderMapper;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.application.port.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.port.dto.result.OrderCouponResult;
import com.example.order_service.order.application.port.dto.result.OrderUserResult;
import com.example.order_service.order.application.service.order.dto.command.OrderCreateCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.infrastructure.adaptor.client.OrderCouponAdaptor;
import com.example.order_service.order.infrastructure.adaptor.client.OrderProductAdaptor;
import com.example.order_service.order.infrastructure.adaptor.client.OrderUserAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

/**
 * 주문 관련 플로우를 담당하는 오케스트레이션 서비스
 * <p>
 * 외부 MSA 도메인과의 통신을 통해 주문 생성 검증, 주문을 생성하는 플로우를 담당
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 02.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderMapper orderMapper;
    private final PointUsagePolicy pointPolicy;
    private final OrderUserAdaptor orderUserAdaptor;
    private final OrderProductAdaptor orderProductAdaptor;
    private final OrderCouponAdaptor orderCouponAdaptor;
    private final OrderSheetRepository orderSheetRepository;
    private final OrderCommandService orderCommandService;
    private final Clock clock;

    /**
     * 주문 생성
     * <p>
     * 주문서가 주문이 가능한 상태인지 검증 후 대기상태의 주문을 생성
     * </p>
     *
     * @param command 주문 생성 커맨드
     * @return 생성된 주문 정보
     */
    public OrderCreateResult initialOrder(OrderCreateCommand command) {
        return null;
    }

    private OrderCouponResult.Calculate getAppliedCoupons(OrderSheet orderSheet) {
        if (!orderSheet.hasAnyCoupon()) {
            return OrderCouponResult.Calculate.empty();
        }
        // [WARING] 페이로드 최적화를 위해 쿠폰 적용 상 filter를 걸면 안됨
        // 쿠폰 미적용 상품도 페이로드에 포함되어야 장바구니 쿠폰의 '최소 결제 금액', '제외 상품'등의 제약을 검사하고 할인 금액을 계산할 수 있음
        List<OrderCouponCommand.AppliedCouponItem> itemCouponCommand = orderSheet.getItems().stream().map(
                item -> OrderCouponCommand.AppliedCouponItem.of(item.getProductVariantId(),
                        item.getDiscountedPrice(),
                        item.getQuantity(),
                        item.getCouponId())
        ).toList();
        OrderCouponCommand.Calculate command = OrderCouponCommand.Calculate
                .of(orderSheet.getOrderer().getUserId(),
                        orderSheet.getCartCoupon().getCartCouponId(),
                        itemCouponCommand);
        return orderCouponAdaptor.calculate(command);
    }

    private OrderUserResult.UserPoint getUserPoints(OrderSheet orderSheet) {
        Long userId = orderSheet.getOrderer().getUserId();
        return orderUserAdaptor.getUserPoints(userId);
    }

    private OrderSheet findOrderSheetById(String sheetId) {
        return null;
    }
}

package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.mapper.OrderMapper;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.application.service.order.dto.command.OrderCreateCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
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

    public OrderCreateResult initialOrder(OrderCreateCommand command) {
        return null;
    }

    private OrderSheet findOrderSheetById(String sheetId) {
        return null;
    }
}

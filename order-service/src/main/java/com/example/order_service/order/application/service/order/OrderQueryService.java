package com.example.order_service.order.application.service.order;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.domain.repository.OrderSearchRepository;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 조회 담당 서비스
 * <p>
 *     주문 조회 로직을 담당하는 서비스
 * </p>
 * @author 최민식
 * @since 2026. 06. 02
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderSearchRepository orderSearchRepository;

    /**
     * 주문 정보 조회
     * <p>
     * 주문 번호의 주문 상세 정보를 조회한다
     * </p>
     *
     * @param userId  유저 아이디
     * @param orderNo 주문 번호
     * @return 주문 상세 정보
     */
    public OrderResult.Detail getOrder(String orderNo, Long userId) {
        Order order = orderRepository.findByOrderNoAndOrderer_UserId(orderNo, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResult.Detail.from(order);
    }

    /**
     * 주문 목록 조회
     * <p>
     * 유저의 주문 목록을 조회한다
     * </p>
     *
     * @param userId   유저 아이디
     * @param command  조회 필터
     * @param pageable 페이지네이션
     * @return 주문 목록
     */
    public Page<OrderResult.Summary> getOrders(Long userId, OrderSearchCommand command, Pageable pageable) {
        Page<Order> orders = orderSearchRepository.searchOrders(userId, command, pageable);
        return orders.map(OrderResult.Summary::from);
    }
}

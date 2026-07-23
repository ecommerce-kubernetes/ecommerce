package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 쿠폰 도메인과의 통신을 담당하는 Adaptor
 * <p>
 * 쿠폰 도메인 서비스 FeignClient 호출, 쿠폰 도메인 서비스에 에러 발생시 서킷 브레이커를 통해 예외 전파를 관리
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponAdaptor {
    private final CouponFeignClient client;
    private final ExternalExceptionTranslator translator;

    @CircuitBreaker(name = "couponService", fallbackMethod = "getItemCouponFallback")
    public ItemCouponResponse getItemCoupon(Long userId, Long itemCouponId) {
        return client.getItemCoupon(userId, itemCouponId);
    }

    private ItemCouponResponse getItemCouponFallback(Long userId, Long itemCouponId, Throwable throwable) throws Throwable {
        throw translator.translate("COUPON-SERVICE", throwable);
    }

    @CircuitBreaker(name = "couponService", fallbackMethod = "getCartCouponFallback")
    public CartCouponResponse getCartCoupon(Long userId, Long cartCouponId) {
        return client.getCartCoupon(userId, cartCouponId);
    }

    private CartCouponResponse getCartCouponFallback(Long userId, Long cartCouponId, Throwable throwable) throws Throwable {
        throw translator.translate("COUPON-SERVICE", throwable);
    }

    @Deprecated
    @CircuitBreaker(name = "couponService")
    public CouponClientResponse.Calculate calculate(CouponCommand.Calculate command) {
        CouponClientRequest.Calculate request = CouponClientRequest.Calculate.from(command);
        return client.calculate(request);
    }
}

package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.application.DefaultGatewayException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.mapper.OrderProductMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 주문 상품 도메인 통신을 담당하는 Gateway 서비스
 * <p>
 * 상품 도메인의 응답을 서비스 레이어의 Result로 매핑하여 반환
 * 상품 도메인 통신중 발생하는 예외를 비지니스 예외로 변환
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 22
 */
@Service
@RequiredArgsConstructor
public class OrderProductGateway {
    private final ProductAdaptor productAdaptor;
    private final OrderProductMapper mapper;

    public OrderProductResult.ProductList getProducts(List<Long> variantIds) {
        ProductCommand.BulkSearch command = ProductCommand.BulkSearch.from(variantIds);
        ProductClientResponse.ProductList productList = fetchProductsWithTranslation(command);
        return mapper.toResult(productList);
    }

    private ProductClientResponse.ProductList fetchProductsWithTranslation(ProductCommand.BulkSearch command) {
        try {
            return productAdaptor.getProducts(command);
        } catch (ExternalClientException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_PRODUCT_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_PRODUCT_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_PRODUCT_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_PRODUCT_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        }
    }
}

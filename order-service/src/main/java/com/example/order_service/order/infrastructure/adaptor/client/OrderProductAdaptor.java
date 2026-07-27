package com.example.order_service.order.infrastructure.adaptor.client;

import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.DefaultPortException;
import com.example.order_service.common.exception.gateway.ProductGatewayErrorCode;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.infrastructure.gateway.ProductGateway;
import com.example.order_service.order.application.port.OrderProductPort;
import com.example.order_service.order.application.port.dto.result.OrderProductsResult;
import com.example.order_service.order.infrastructure.adaptor.mapper.OrderProductPortMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderProductAdaptor implements OrderProductPort {
    private final ProductGateway productGateway;
    private final OrderProductPortMapper orderProductPortMapper;

    @Override
    public OrderProductsResult getProducts(List<Long> productVariantIds) {
        ProductResponse response = executeGetProducts(productVariantIds);
        return orderProductPortMapper.mapToOrderProductsResult(response);
    }

    private ProductResponse executeGetProducts(List<Long> productVariantIds) {
        try {
            return productGateway.getProducts(productVariantIds);
        } catch (ExternalClientException e) {
            throw new DefaultPortException(ProductGatewayErrorCode.PRODUCT_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultPortException(ProductGatewayErrorCode.PRODUCT_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultPortException(ProductGatewayErrorCode.PRODUCT_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultPortException(ProductGatewayErrorCode.PRODUCT_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        }
    }
}

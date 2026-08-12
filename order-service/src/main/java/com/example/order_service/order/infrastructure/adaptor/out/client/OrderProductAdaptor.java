package com.example.order_service.order.infrastructure.adaptor.out.client;

import com.example.order_service.common.exception.PortException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.infrastructure.gateway.ProductGateway;
import com.example.order_service.order.application.port.OrderProductPort;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.exception.OrderProductPortErrorCode;
import com.example.order_service.order.infrastructure.adaptor.out.client.mapper.OrderProductAdaptorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderProductAdaptor implements OrderProductPort {
    private final ProductGateway productGateway;
    private final OrderProductAdaptorMapper orderProductAdaptorMapper;

    @Override
    public OrderProductsResult getProducts(List<Long> productVariantIds) {
        ProductResponse response = executeGetProducts(productVariantIds);
        return orderProductAdaptorMapper.mapToOrderProductsResult(response);
    }

    private ProductResponse executeGetProducts(List<Long> productVariantIds) {
        try {
            return productGateway.getProducts(productVariantIds);
        } catch (ExternalClientException e) {
            throw new PortException(OrderProductPortErrorCode.PRODUCT_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new PortException(OrderProductPortErrorCode.PRODUCT_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new PortException(OrderProductPortErrorCode.PRODUCT_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new PortException(OrderProductPortErrorCode.PRODUCT_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        }
    }
}

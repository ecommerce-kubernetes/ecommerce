package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.application.mapper.OrderProductMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProductGateway {
    private final ProductAdaptor productAdaptor;
    private final OrderProductMapper mapper;

    public List<OrderProductResult.Info> getProducts(List<Long> productVariantIds) {
        List<ProductClientResponse.Product> products = fetchProductsWithTranslation(productVariantIds);
        return products.stream()
                .map(mapper::toResult)
                .toList();
    }

    private List<ProductClientResponse.Product> fetchProductsWithTranslation(List<Long> ids) {
        try {
            return productAdaptor.getProductsByVariantIds(ids);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}

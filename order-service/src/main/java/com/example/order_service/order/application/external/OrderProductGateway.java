package com.example.order_service.order.application.external;


import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderErrorCode;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.dto.command.CreateOrderItemCommand;
import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.application.mapper.OrderProductMapper;
import com.example.order_service.order.domain.model.ProductStatus;
import com.example.order_service.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.order.infrastructure.client.product.OrderProductAdaptor;
import com.example.order_service.order.infrastructure.client.product.dto.OrderProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProductGateway {

    private final OrderProductAdaptor orderProductAdaptor;
    private final ProductAdaptor productAdaptor;
    private final OrderProductMapper mapper;

    public List<OrderProductInfo> getProductsdeprecated(List<CreateOrderItemCommand> dtoList) {
        // 주문 상품 Map
        Map<Long, Integer> orderProductMap = mapToOrderProduct(dtoList);
        // 상품 정보 조회
        List<OrderProductResponse> products = orderProductAdaptor.getProducts(new ArrayList<>(orderProductMap.keySet()));
        // 조회한 상품 검증
        validateOrderProduct(orderProductMap, products);
        return products.stream().map(this::mapToInfo)
                .toList();
    }

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

    private Map<Long, Integer> mapToOrderProduct(List<CreateOrderItemCommand> dtoList) {
        return dtoList.stream()
                .collect(Collectors.toMap(
                        CreateOrderItemCommand::getProductVariantId,
                        CreateOrderItemCommand::getQuantity,
                        Integer::sum
                ));
    }

    private void validateOrderProduct(Map<Long, Integer> orderProductMap, List<OrderProductResponse> products) {
        // 조회한 상품과 응답 상품 개수가 다른 경우
        if (orderProductMap.size() != products.size()) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
        }

        for (OrderProductResponse product : products) {
            ProductStatus status = ProductStatus.from(product.getStatus());
            // 상품이 판매중이 아닌 경우
            if (!status.equals(ProductStatus.ON_SALE)) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_ON_SALE);
            }

            // 상품 수량이 부족한 경우
            if (orderProductMap.get(product.getProductVariantId()) > product.getStockQuantity()) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
            }
        }
    }

    private OrderProductInfo mapToInfo(OrderProductResponse response) {
        List<OrderProductInfo.ProductOption> productOptions = response.getItemOptions().stream()
                .map(o -> OrderProductInfo.ProductOption.of(o.getOptionTypeName(), o.getOptionValueName())).toList();
        return OrderProductInfo.builder()
                .productId(response.getProductId())
                .productVariantId(response.getProductVariantId())
                .sku(response.getSku())
                .productName(response.getProductName())
                .originalPrice(response.getUnitPrice().getOriginalPrice())
                .discountRate(response.getUnitPrice().getDiscountRate())
                .discountAmount(response.getUnitPrice().getDiscountAmount())
                .discountedPrice(response.getUnitPrice().getDiscountedPrice())
                .thumbnail(response.getThumbnailUrl())
                .productOption(productOptions)
                .build();
    }
}

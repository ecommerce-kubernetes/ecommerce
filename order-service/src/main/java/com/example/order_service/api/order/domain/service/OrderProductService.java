package com.example.order_service.api.order.domain.service;


import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.model.ProductStatus;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.infrastructure.client.product.OrderProductAdaptor;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderProductService {

    private final OrderProductAdaptor orderProductAdaptor;

    public List<OrderProductInfo> getProducts(List<CreateOrderItemCommand> dtoList) {
        // 주문 상품 Map
        Map<Long, Integer> orderProductMap = mapToOrderProduct(dtoList);
        // 상품 정보 조회
        List<OrderProductResponse> products = orderProductAdaptor.getProducts(new ArrayList<>(orderProductMap.keySet()));
        // 조회한 상품 검증
        validateOrderProduct(orderProductMap, products);
        return products.stream().map(this::mapToInfo)
                .toList();
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
        List<OrderProductInfo.ProductOption> productOptions = response.getProductOptionInfos().stream()
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

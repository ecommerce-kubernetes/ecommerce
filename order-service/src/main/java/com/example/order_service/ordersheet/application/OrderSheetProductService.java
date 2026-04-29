package com.example.order_service.ordersheet.application;

import com.example.order_service.api.common.exception.business.BusinessException;
import com.example.order_service.api.common.exception.business.code.OrderSheetErrorCode;
import com.example.order_service.api.common.exception.external.ExternalClientException;
import com.example.order_service.api.common.exception.external.ExternalServerException;
import com.example.order_service.api.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.ProductStatus;
import com.example.order_service.ordersheet.application.mapper.OrderSheetProductMapper;
import com.example.order_service.ordersheet.infrastructure.client.OrderSheetAdaptor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OrderSheetProductService {
    private final ProductAdaptor productAdaptor;
    private final OrderSheetProductMapper mapper;

    public List<OrderSheetProductResult.Info> getProducts(List<Long> productVariantIds) {
        List<ProductClientResponse.Product> products = fetchProductsWithTranslation(productVariantIds);
        return products.stream()
                .map(mapper::toResult)
                .toList();
    }

    // fallback 메서드 (예외 변환 및 fallback)
    private List<ProductClientResponse.Product> fetchProductsWithTranslation(List<Long> ids) {
        try {
            //정상 응답
            return productAdaptor.getProductsByVariantIds(ids);
        } catch (ExternalClientException e) {
            //400번대 에러
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            //500번대 에러
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            //서킷브레이커 열림(503 에러)
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}

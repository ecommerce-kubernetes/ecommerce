package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.command.OrderProductCommand;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.mapper.OrderProductMapper;
import com.example.order_service.order.exception.OrderSheetErrorCode;
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


    /**
     * 상품 도메인에 주문 상품 정보를 요청하여 상품의 정보를 반환
     *
     * @param items 주문 상품 정보
     * @return 상품 정보 결과를 반환
     * @throws BusinessException 상품 도메인 통신중 발생한 예외를 비지니스 예외로 변환
     */
    public OrderProductResult.ProductList getProducts(List<OrderProductCommand.OrderItem> items) {
        List<ProductCommand.Item> commandItems = items.stream()
                .map(item -> ProductCommand.Item.of(item.productVariantId(), item.quantity())).toList();
        ProductCommand.Validate command = ProductCommand.Validate.of(commandItems);
        ProductClientResponse.ProductList productList = fetchProductsWithTranslation(command);
        return mapper.toResult(productList);
    }

    private ProductClientResponse.ProductList fetchProductsWithTranslation(ProductCommand.Validate command) {
        try {
            return productAdaptor.getProductsForOrder(command);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}

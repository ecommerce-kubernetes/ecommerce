package com.example.order_service.cart.application.external;

import com.example.order_service.cart.application.dto.result.CartProductResult;
import com.example.order_service.cart.application.mapper.CartProductMapper;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartProductGateway {
    private final ProductAdaptor adaptor;
    private final CartProductMapper mapper;

    public List<CartProductResult.Info> getProducts(List<Long> variantIds) {
        return null;
    }

    // fallback
    private List<ProductClientResponse> fetchProductWithTranslation(List<Long> ids) {
        return null;
    }
}

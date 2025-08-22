package com.example.product_service.controller.validator;

import com.example.product_service.controller.util.validator.ProductPageableValidator;
import com.example.product_service.entity.DomainType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProductPageableValidatorTest {

    @InjectMocks
    ProductPageableValidator productPageableValidator;

    @Test
    @DisplayName("상품 Pageable-support 매치")
    void supportTest_support_matched(){
        assertThat(productPageableValidator.support(DomainType.PRODUCT))
                .isTrue();
    }
    @Test
    @DisplayName("상품 Pageable-support 매치안됨")
    void supportTest_support_notMatched(){
        assertThat(productPageableValidator.support(DomainType.REVIEW))
                .isFalse();
    }

    @Test
    @DisplayName("상품 Pageable 검증")
    void validateTest(){
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "price");
        Pageable result = productPageableValidator.validate(pageable);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);

        assertThat(result.getSort().stream())
                .anyMatch(order -> order.getProperty().equals("minimumPrice"));
    }

    @Test
    @DisplayName("상품 Pageable 검증")
    void validateTest_unSupportField(){
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "money");
        Pageable result = productPageableValidator.validate(pageable);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);

        assertThat(result.getSort().stream())
                .anyMatch(order -> order.getProperty().equals("id"));
    }
}
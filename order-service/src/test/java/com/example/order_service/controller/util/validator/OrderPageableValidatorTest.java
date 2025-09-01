package com.example.order_service.controller.util.validator;

import com.example.order_service.entity.DomainType;
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
class OrderPageableValidatorTest {
    @InjectMocks
    OrderPageableValidator orderPageableValidator;

    @Test
    @DisplayName("주문 Pageable-support 매치")
    void supportTest_support_matched(){
        assertThat(orderPageableValidator.support(DomainType.ORDER))
                .isTrue();
    }
    @Test
    @DisplayName("주문 Pageable-support 매치안됨")
    void supportTest_support_notMatched(){
        assertThat(orderPageableValidator.support(DomainType.CART))
                .isFalse();
    }

    @Test
    @DisplayName("상품 Pageable 검증")
    void validateTest(){
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "createdAt");
        Pageable result = orderPageableValidator.validate(pageable);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);

        assertThat(result.getSort().stream())
                .anyMatch(order -> order.getProperty().equals("createdAt"));
    }

    @Test
    @DisplayName("상품 Pageable 검증")
    void validateTest_unSupportField(){
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "price");
        Pageable result = orderPageableValidator.validate(pageable);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);

        assertThat(result.getSort().stream())
                .anyMatch(order -> order.getProperty().equals("createdAt"));
    }
}
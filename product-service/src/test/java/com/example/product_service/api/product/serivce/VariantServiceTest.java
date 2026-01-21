package com.example.product_service.api.product.serivce;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import com.example.product_service.api.product.service.VariantService;
import com.example.product_service.api.product.service.dto.result.InternalVariantResponse;
import com.example.product_service.support.ExcludeInfraTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class VariantServiceTest extends ExcludeInfraTest {

    @Autowired
    private VariantService variantService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OptionTypeRepository optionTypeRepository;
    @Autowired
    private EntityManager em;

    private Category saveCategory() {
        return categoryRepository.save(Category.create("카테고리", null, "http://image.jpg"));
    }

    private Product saveProduct(Category category) {
        return productRepository.save(Product.create("상품", "설명", category));
    }

    private OptionType saveOptionType(String name, List<String> valueNames) {
        return optionTypeRepository.save(OptionType.create(name, valueNames));
    }

    private OptionValue findOptionValue(OptionType optionType, String name) {
        return optionType.getOptionValues().stream().filter(v -> v.getName().equals(name)).findFirst().orElseThrow();
    }

    @Nested
    @DisplayName("상품 변형 조회")
    class GetVariant {
        @Test
        @DisplayName("상품 변형이 없으면 조회할 수 없다")
        void getVariant_not_found_variant() {
            //given
            //when
            //then
            assertThatThrownBy(() -> variantService.getVariant(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }
    }
}

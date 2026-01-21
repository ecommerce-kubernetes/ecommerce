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

import static org.assertj.core.api.Assertions.*;

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

    private void settingProduct(Product product, List<OptionType> optionTypes, List<ProductVariant> variants) {
        product.updateOptions(optionTypes);
        product.replaceImages(List.of("http://image.jpg"));
        for (ProductVariant variant : variants) {
            product.addVariant(variant);
        }
        em.flush();
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

        @Test
        @DisplayName("상품 변형을 조회한다")
        void getVariant(){
            //given
            OptionType size = saveOptionType("사이즈", List.of("XL", "L"));
            OptionType color = saveOptionType("색상", List.of("RED", "BLUE"));
            OptionValue xl = findOptionValue(size, "XL");
            OptionValue blue = findOptionValue(color, "BLUE");
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            variant.addProductVariantOptions(List.of(xl, blue));
            Category category = saveCategory();
            Product product = saveProduct(category);
            settingProduct(product, List.of(size, color), List.of(variant));
            //when
            InternalVariantResponse result = variantService.getVariant(variant.getId());
            //then
            assertThat(result)
                    .extracting(InternalVariantResponse::getProductId,
                            InternalVariantResponse::getProductVariantId,
                            InternalVariantResponse::getSku,
                            InternalVariantResponse::getStatus)
                    .containsExactly(product.getId(), variant.getId(), variant.getSku(), product.getStatus());

            assertThat(result.getUnitPrice())
                    .extracting(InternalVariantResponse.UnitPrice::getOriginalPrice,
                            InternalVariantResponse.UnitPrice::getDiscountRate,
                            InternalVariantResponse.UnitPrice::getDiscountAmount,
                            InternalVariantResponse.UnitPrice::getDiscountedPrice)
                    .containsExactly(10000L, 10, 1000L, 9000L);

            assertThat(result.getItemOptions())
                    .extracting(InternalVariantResponse.ItemOption::getOptionTypeName, InternalVariantResponse.ItemOption::getOptionValueName)
                    .containsExactlyInAnyOrder(
                            tuple(size.getName(), xl.getName()),
                            tuple(color.getName(), blue.getName())
                    );
        }

        @Test
        @DisplayName("상품 변형 목록을 조회한다")
        void getVariants(){
            //given
            OptionType size = saveOptionType("사이즈", List.of("XL", "L"));
            OptionType color = saveOptionType("색상", List.of("RED", "BLUE"));
            OptionValue xl = findOptionValue(size, "XL");
            OptionValue blue = findOptionValue(color, "BLUE");
            OptionValue red = findOptionValue(color, "RED");
            Category category = saveCategory();
            ProductVariant variant1 = ProductVariant.create("TEST1", 10000L, 100, 10);
            variant1.addProductVariantOptions(List.of(xl, blue));
            ProductVariant variant2 = ProductVariant.create("TEST2", 20000L, 100, 10);
            variant2.addProductVariantOptions(List.of(xl, red));
            Product product = saveProduct(category);
            settingProduct(product, List.of(size, color), List.of(variant1, variant2));
            //when
            List<InternalVariantResponse> result = variantService.getVariants(List.of(variant1.getId(), variant2.getId()));
            //then
            assertThat(result).hasSize(2);
        }
    }
}

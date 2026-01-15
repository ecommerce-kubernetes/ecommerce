package com.example.product_service.api.product.serivce;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.result.ProductCreateResponse;
import com.example.product_service.api.product.service.dto.result.ProductOptionSpecResponse;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class ProductServiceTest extends ExcludeInfraTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OptionTypeRepository optionTypeRepository;

    private ProductCreateCommand createProductCommand(Long categoryId){
        return ProductCreateCommand.builder()
                .name("상품")
                .categoryId(categoryId)
                .description("상품 설명")
                .build();
    }

    @Nested
    @DisplayName("상품 생성")
    class Create {

        @Test
        @DisplayName("상품을 생성한다")
        void createProduct(){
            //given
            Category category = Category.create("카테고리", null, "http://image.jpg");
            Category savedCategory = categoryRepository.save(category);
            ProductCreateCommand command = createProductCommand(savedCategory.getId());
            //when
            ProductCreateResponse result = productService.createProduct(command);
            //then
            assertThat(result.getProductId()).isNotNull();
        }

        @Test
        @DisplayName("카테고리를 찾을 수 없으면 예외를 던진다")
        void createProduct_category_notFound(){
            //given
            ProductCreateCommand command = createProductCommand(999L);
            //when
            //then
            assertThatThrownBy(() -> productService.createProduct(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("최하위 카테고리가 아니면 예외를 던진다")
        void createProduct_not_leaf_category(){
            //given
            Category parent = Category.create("부모 카테고리", null, "http://parent.jpg");
            Category parentCategory = categoryRepository.save(parent);
            Category child = Category.create("자식 카테고리", parent, "http://child.jpg");
            categoryRepository.save(child);

            ProductCreateCommand command = createProductCommand(parentCategory.getId());
            //when
            //then
            assertThatThrownBy(() -> productService.createProduct(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.CATEGORY_NOT_LEAF);
        }
    }

    @Nested
    @DisplayName("상품 옵션 정의")
    class OptionSpecs {

        @Test
        @DisplayName("상품 옵션 스펙을 설정한다")
        void registerOptionSpecs(){
            //given
            OptionType optionType = OptionType.create("사이즈", List.of("XL", "L", "M", "S"));
            optionTypeRepository.save(optionType);
            Category category = Category.create("카테고리", null, "http://image.jpg");
            Category savedCategory = categoryRepository.save(category);
            Product product = Product.create("상품", "상품 설명", savedCategory);
            Product savedProduct = productRepository.save(product);
            //when
            ProductOptionSpecResponse response = productService.registerOptionSpec(savedProduct.getId(), List.of(optionType.getId()));
            //then
            assertThat(response.getProductId()).isEqualTo(savedProduct.getId());
            assertThat(response.getOptions())
                    .extracting(ProductOptionSpecResponse.OptionSpec::getOptionTypeId, ProductOptionSpecResponse.OptionSpec::getName,
                            ProductOptionSpecResponse.OptionSpec::getPriority)
                    .containsExactly(
                            tuple(optionType.getId(), optionType.getName(), 1)
                    );
        }

        @Test
        @DisplayName("옵션 스펙을 정할때 상품을 찾을 수 없으면 예외를 던진다")
        void registerOptionSpecs_not_found_product(){
            //given
            //when
            //then
            assertThatThrownBy(() -> productService.registerOptionSpec(999L, List.of(1L,2L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품이 판매중이라면 옵션 스펙을 정할 수 없다")
        void registerOptionSpecs_on_sale(){
            //given
            Category category = Category.create("카테고리", null, "http://image.jpg");
            Category savedCategory = categoryRepository.save(category);
            Product product = Product.create("상품", "상품 설명", savedCategory);
            ReflectionTestUtils.setField(product, "status", ProductStatus.ON_SALE);
            Product savedProduct = productRepository.save(product);
            //when
            //then
            assertThatThrownBy(() -> productService.registerOptionSpec(savedProduct.getId(), List.of(999L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.CANNOT_MODIFY_ON_SALE);
        }

        @Test
        @DisplayName("상품이 상품 변형을 가지고 있는 경우 옵션 스펙을 정할 수 없다")
        void registerOptionSpecs_has_variants(){
            //given
            Category category = Category.create("카테고리", null, "http://image.jpg");
            Category savedCategory = categoryRepository.save(category);
            Product product = Product.create("상품", "상품 설명", savedCategory);
            ProductVariant variant = ProductVariant.create("PROD", 3000L, 100, 10);
            product.addVariant(variant);
            Product savedProduct = productRepository.save(product);
            //when
            //then
            assertThatThrownBy(() -> productService.registerOptionSpec(savedProduct.getId(), List.of(1L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.HAS_VARIANTS);
        }

        @Test
        @DisplayName("상품 옵션은 최대 3개 까지 설정 가능하다")
        void registerOptionSpecs_exceed_max_optionSpec_count(){
            //given
            OptionType optionType1 = OptionType.create("사이즈", List.of("XL", "L", "M", "S"));
            OptionType optionType2 = OptionType.create("색상", List.of("RED", "BLUE", "ORANGE"));
            OptionType optionType3 = OptionType.create("재질", List.of("WOOL", "COTTON", "LINEN"));
            OptionType optionType4 = OptionType.create("용량", List.of("256GB", "128GB"));
            optionTypeRepository.saveAll(List.of(optionType1, optionType2, optionType3, optionType4));
            Category category = Category.create("카테고리", null, "http://image.jpg");
            Category savedCategory = categoryRepository.save(category);
            Product product = Product.create("상품", "상품 설명", savedCategory);
            Product savedProduct = productRepository.save(product);
            //when
            //then
            assertThatThrownBy(() -> productService.registerOptionSpec(savedProduct.getId(), List.of(optionType1.getId(),
                    optionType2.getId(), optionType3.getId(), optionType4.getId())))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.EXCEED_OPTION_SPEC_COUNT);
        }

        @Test
        @DisplayName("옵션 타입을 찾을 수 없는 경우 예외를 던진다")
        void registerOptionSpecs_not_found_optionType(){
            //given
            OptionType optionType = OptionType.create("사이즈", List.of("XL", "L", "M", "S"));
            OptionType savedOptionType = optionTypeRepository.save(optionType);
            Category category = Category.create("카테고리", null, "http://image.jpg");
            Category savedCategory = categoryRepository.save(category);
            Product product = Product.create("상품", "상품 설명", savedCategory);
            Product savedProduct = productRepository.save(product);
            //when
            //then
            assertThatThrownBy(() -> productService.registerOptionSpec(savedProduct.getId(), List.of(savedOptionType.getId(), 999L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_NOT_FOUND);
        }
    }
}
